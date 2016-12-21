#
# Name:    pif2lnt.py - Classes for loading a PIF model
#                        and translating it to LNT 
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

import itertools
import random
import sys
from subprocess import *

import pyxb

import pif

LTS_SUFFIX = ".bcg"
LNT_SUFFIX = ".lnt"

# TODO: use values that must exist in some python library instead
class ReturnCodes:
    TERM_OK, TERM_ERROR, TERM_PROBLEM = (0, 1, 2)

# Dumps alphabet (list of strings) in the given file
# Inputs: a list of strings, a file identifier, a Boolean indicating whether to add "any" or not
def dumpAlphabet(alph, f, addany):
    nbelem = len(alph)
    if (nbelem > 0):
        f.write("[")
        cter = 1
        for e in alph:
            f.write(e)
            if addany:
                f.write(":any")
            cter = cter + 1
            if (cter <= nbelem):
                f.write(", ")
        f.write("]")


# Computes all combinations, in sorted order, any possible number from 1 to len(l)
# Inputs: a list of strings l
def computeAllCombinations(l):
    nbelem = len(l)
    i = 1
    res = []
    while (i <= nbelem):
        restmp = itertools.combinations(l, i)
        for r in restmp:
            res.append(r)
        i = i + 1
    return res


# Takes a list of couple (ident,depth) resulting from the reachableOrJoin method call
#  and a number of outgoing flows. Checks if all flows lead to a same join. 
# If yes, returns that join identifier, else returns "". 
def analyzeReachabilityResults(lc, nbflows):
    # first we check whether there is at least a corresponding join with depth 0 
    # (there is at most one)
    existjoin = False
    for c in lc:
        if (c[1] == 0):
            joinident = c[0]
            existjoin = True
            break
    if existjoin:
        # we check if there is as many couples with the join identifiers as flows number
        cter = 0
        for c in lc:
            if (c[0] == joinident) and (c[1] == 0):
                cter = cter + 1
        if (cter >= nbflows):  # you can have more splits in-between, thus more flows..
            return joinident
        else:
            return ""
    else:
        return ""


##
# Abstract class for Nodes
# Should not be directly used. Use child classes instead.
class Node:
    def __init__(self, ident, inc, out):
        self.ident = ident
        self.incomingFlows = inc
        self.outgoingFlows = out

    # This method dumps a textual version of a node (useful for debugging purposes)
    def dump(self):
        print "Node " + self.ident + " in: ",
        for f in self.incomingFlows:
            print f.ident + " ",
        print " out: ",
        for f in self.outgoingFlows:
            print f.ident + " ",
        print ""


##
# Class for Flows
class Flow:
    def __init__(self, ident, source, target):
        self.ident = ident
        self.source = source
        self.target = target

    # This method dumps a textual version of a flow (useful for debugging purposes)
    def dump(self):
        print "Flow " + self.source.ident + "--" + self.ident + "-->" + self.target.ident

    # Generates the (generic) process for flows, only once
    def lnt(self, f):
        f.write("process flow [begin:any, finish:any] (ident: ID) is\n")
        f.write(" loop begin (!ident) ; finish (!ident) end loop\n")
        f.write("end process\n")

    # A normal flow cannot be a default flow
    def isDefault(self):
        return False

    # Returns the source node
    def getSource(self):
        return self.source

    # Returns the target node
    def getTarget(self):
        return self.target


##
# Class for ConditionalFlows
class ConditionalFlow(Flow):
    def __init__(self, ident, source, target, cond):
        Flow.__init__(self, ident, source, target)
        self.cond = cond

    # Generates the process for conditional flows
    def lnt(self, f):
        # TODO: translate the condition too
        f.write("process conditionalflow [begin:any, finish:any] (ident: ID) is\n")
        f.write(" loop begin (!ident) ; finish (!ident) end loop\n")
        f.write("end process\n")

    # A conditional flow is default iff the condition attribute contains "default"
    def isDefault(self):
        return self.cond == "default"


##
# Class for Initial Event
class InitialEvent(Node):
    def __init__(self, ident, inc, out):
        Node.__init__(self, ident, inc, out)

    # Generates the (generic) process for the initial event, only once
    def lnt(self, f):
        f.write("process init [begin:any, outf:any] is\n")
        f.write(" var ident: ID in begin ; outf (?ident of ID) end var \n")
        f.write("end process\n")

    # Seeks or joins, for an initial event, just a recursive call on the target node of the outgoing flow
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        return self.outgoingFlows[0].getTarget().reachableOrJoin(visited + [self.ident], depth)


##
# Class for End Event
class EndEvent(Node):
    def __init__(self, ident, inc, out):
        Node.__init__(self, ident, inc, out)

    # Generates the (generic) process for final events, only once
    def lnt(self, f):
        f.write("process final [incf:any, finish:any] is\n")
        f.write(" var ident: ID in incf (?ident of ID); finish end var\n")
        f.write("end process\n")

    # Seeks an or join, for an initial event, just a recursive call on the target node of the outgoing flow
    def reachableOrJoin(self, visited, depth):
        return []


##
# Abstract Class for Communication
class Communication(Node):
    def __init__(self, ident, inc, out, msg):
        Node.__init__(self, ident, inc, out)
        self.msg = msg

    # For a communication, if not visited yet, recursive call on the target node of the outgoing flow
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:
            return self.outgoingFlows[0].getTarget().reachableOrJoin(visited + [self.ident], depth)


##
# Class for Interaction
class Interaction(Communication):
    def __init__(self, ident, inc, out, msg, sender, receivers):
        Communication.__init__(self, ident, inc, out, msg)
        self.sender = sender
        self.receivers = receivers

    # Generates the (generic) process for interactions, only once
    def lnt(self, f):
        f.write("process interaction [incf:any, inter:any, outf:any] is\n")
        f.write(" var ident: ID in loop incf (?ident of ID); inter; outf (?ident of ID) end loop end var \n")
        f.write("end process\n")

    # Computes alphabet for an interaction
    def alpha(self):
        res = self.sender + "_"
        for e in self.receivers:
            res = res + e + "_"
        res = res + self.msg
        return [res]

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        # we assume one incoming flow and one outgoing flow
        f.write("interaction [" + self.incomingFlows[0].ident + "_finish,")
        res = self.sender + "_"
        for e in self.receivers:
            res = res + e + "_"
        res = res + self.msg
        f.write(res + ",")
        f.write(self.outgoingFlows[0].ident + "_begin]")


##
# Abstract Class for MessageCommunication
class MessageCommunication(Communication):
    def __init__(self, ident, inc, out, msg):
        Communication.__init__(self, ident, inc, out, msg)


##
# Class for MessageSending
class MessageSending(MessageCommunication):
    def __init__(self, ident, inc, out, msg):
        MessageCommunication.__init__(self, ident, inc, out, msg)

    # Generates the (generic) process for message sending, only once
    def lnt(self, f):
        f.write("process messagesending [incf:any, msg:any, outf:any] is\n")
        f.write(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var \n")
        f.write("end process\n")

    # Computes alphabet for a message sending
    def alpha(self):
        return [self.msg + "_EM"]

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        # we assume one incoming flow and one outgoing flow
        f.write("messagesending [" + self.incomingFlows[0].ident + "_finish,")
        f.write(self.msg + "_EM,")
        f.write(self.outgoingFlows[0].ident + "_begin]")


##
# Class for MessageReception
class MessageReception(MessageCommunication):
    def __init__(self, ident, inc, out, msg):
        MessageCommunication.__init__(self, ident, inc, out, msg)

    # Generates the (generic) process for message reception, only once
    def lnt(self, f):
        f.write("process messagereception [incf:any, msg:any, outf:any] is\n")
        f.write(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var \n")
        f.write("end process\n")

    # Computes alphabet for a message reception
    def alpha(self):
        return [self.msg + "_REC"]

        # Generates process instantiation for main LNT process

    def mainlnt(self, f):
        # we assume one incoming flow and one outgoing flow
        f.write("messagereception [" + self.incomingFlows[0].ident + "_finish,")
        f.write(self.msg + "_REC,")
        f.write(self.outgoingFlows[0].ident + "_begin]")


##
# Class for Task
class Task(Node):
    def __init__(self, ident, inc, out):
        Node.__init__(self, ident, inc, out)

    # Generates the (generic) process for task, only once
    def lnt(self, f):
        nbinc=len(self.incomingFlows)
        nbout=len(self.outgoingFlows)
        f.write("process task_"+str(nbinc)+"_"+str(nbout)+" [")
        # we check the number of incoming / outgoing flows
        if (nbinc==1):
            f.write("incf:any,")
        else:
            cptinc=0
            while (cptinc<nbinc):
                f.write("incf"+str(cptinc)+":any,")
                cptinc=cptinc+1
        f.write("task:any,")
        if (nbout==1):
            f.write("outf:any")
        else:
            cptout=0
            while (cptout<nbout):
                f.write("outf"+str(cptout)+":any")
                cptout=cptout+1
                if (cptout<nbout):
                    f.write(",")
        f.write("] is\n")

        f.write(" var ident: ID in loop ")

        if (nbinc==1):
            f.write(" incf (?ident of ID); ")
        else:
            cptinc=0
            f.write(" select ")
            while (cptinc<nbinc):
                f.write("incf"+str(cptinc)+" (?ident of ID)")
                cptinc=cptinc+1
                if (cptinc<nbinc):
                    f.write(" [] ")
            f.write(" end select ; \n")
        f.write("task ; ")
        if (nbout==1):
            f.write(" outf (?ident of ID)")
        else:
            cptout=0
            f.write(" select ")
            while (cptout<nbout):
                f.write("outf"+str(cptout)+" (?ident of ID)")
                cptout=cptout+1
                if (cptout<nbout):
                    f.write(" [] ")
            f.write(" end select \n")

        f.write(" end loop end var\n")

        f.write("end process\n")

    # Computes alphabet for a task
    def alpha(self):
        return [self.ident]

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        # we assume one incoming flow and one outgoing flow
        nbinc=len(self.incomingFlows)
        nbout=len(self.outgoingFlows)
        f.write(" task_"+str(nbinc)+"_"+str(nbout)+" [")

        if (nbinc==1):
            f.write(self.incomingFlows[0].ident + "_finish,")
        else:
            cptinc=0
            while (cptinc<nbinc):
                f.write(self.incomingFlows[cptinc].ident + "_finish,")
                cptinc=cptinc+1
        f.write(self.ident + ",")
        if (nbout==1):
            f.write(self.outgoingFlows[0].ident + "_begin")
        else:
            cptout=0
            while (cptout<nbout):
                f.write(self.outgoingFlows[cptout].ident + "_begin")
                cptout=cptout+1
                if (cptout<nbout):
                    f.write(",")
        f.write("] ")

    # For a task, if not visited yet, recursive call on the target node of the outgoing flow
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:

            if (len(self.outgoingFlows)==1):
                return self.outgoingFlows[0].getTarget().reachableOrJoin(visited + [self.ident], depth)
            else:
                res = []
                for f in self.outgoingFlows:
                    res = res + f.getTarget().reachableOrJoin(visited + [self.ident], depth)
                return res

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        t=random.randint(0,4) # we generate a random time !
        # CAUTION : we assume one incoming flow and one outgoing flow
        f.write("        task("+self.ident+",\""+self.ident+"\","+self.incomingFlows[0].ident+","+self.outgoingFlows[0].ident+","+str(t)+")")


##
# Abstract Class for Gateway
class Gateway(Node):
    def __init__(self, ident, inc, out):
        Node.__init__(self, ident, inc, out)

    # Computes alphabet for a message sending
    def alpha(self):
        return []



# Abstract Class for SplitGateway
class SplitGateway(Gateway):
    def __init__(self, ident, inc, out):
        Gateway.__init__(self, ident, inc, out)

    # Generates process instantiation for all split gateways
    def mainlnt(self, f):
        # we assume one incoming flow 
        nboutf = len(self.outgoingFlows)
        f.write("[")
        f.write(self.incomingFlows[0].ident + "_finish,")
        i = 0
        while (i < nboutf):
            f.write(self.outgoingFlows[i].ident + "_begin")
            i = i + 1
            if (i < nboutf):
                f.write(",")
        f.write("]")

    # For a split (generic), if not visited yet, recursive call on the target nodes of all outgoing flows
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:
            res = []
            for f in self.outgoingFlows:
                res = res + f.getTarget().reachableOrJoin(visited + [self.ident], depth)
            return res

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f,nameop):
        f.write("        split("+self.ident+","+nameop+","+self.incomingFlows[0].ident+",")
        cpt=len(self.outgoingFlows)
        f.write("(")
        for ofl in self.outgoingFlows:
            cpt=cpt-1
            f.write(ofl.ident)
            if (cpt>0):
                f.write(",")
        f.write("))")


##
# Class for OrSplitGateway
class OrSplitGateway(SplitGateway):
    def __init__(self, ident, inc, out):
        SplitGateway.__init__(self, ident, inc, out)
        self.correspOrJoin = ""  # contains the identifier of the corresponding join (if there is one)

    # Checks whether the set of outgoing flows contains a default flow
    # Returns a Boolean value
    def existDefaultFlow(self):
        res = False
        for f in self.outgoingFlows:
            if f.isDefault():
                res = True
        return res

    # Generates the process for inclusive split gateway
    def lnt(self, f):
        nboutf = len(self.outgoingFlows)
        default = self.existDefaultFlow()
        # TODO: update the translation to consider properly the default semantics (if there is such a branch)

        # We translate the inclusive split by enumerating all combinations in a select / par
        alphaout = []
        nb = 1
        while (nb <= nboutf):
            alphaout.append("outf_" + str(nb))
            nb = nb + 1
        allcombi = computeAllCombinations(alphaout)
        # print allcombi
        nbt = len(allcombi)

        f.write("process orsplit_" + self.ident + " [incf:any,")
        # We dumps the process alphabet (flows + synchronization points if necessary)
        nbg = 1
        while (nbg <= nboutf):
            f.write("outf_" + str(nbg) + ":any")
            nbg = nbg + 1
            if (nbg <= nboutf):
                f.write(",")

        if (nbt > 0) and (self.correspOrJoin != ""):
            f.write(", ")
            cter = 1
            for elem in allcombi:
                f.write(self.correspOrJoin + "_" + str(cter) + ":any")
                cter = cter + 1
                if (cter <= nbt):
                    f.write(",")

        f.write(" ] is \n")

        ctervar=len(allcombi)
        f.write(" var ")
        while (ctervar>0):
            f.write("ident"+str(ctervar)+":ID")
            ctervar=ctervar-1
            if (ctervar>0):
                f.write(",")
        f.write(" in ")
        f.write(" var ident: ID in loop incf (?ident of ID); \n") # TODO: we generate unnecessary variables..
        f.write(" select ")

        nb = 1
        # counter for generating synchro points
        cter = 1
        for t in allcombi:
            nbelem = len(t)
            nb2 = 1
            if (nbelem > 1):
                ctervar=len(allcombi)
                f.write(" par ")
                for e in t:
                    f.write(e+" (?ident"+str(ctervar)+" of ID)")
                    ctervar=ctervar-1
                    nb2 = nb2 + 1
                    if (nb2 <= nbelem):
                        f.write("||")
                f.write(" end par ")
            else:
                f.write(t[0]+" (?ident of ID)")
            # add synchronization points if there's a corresponding join
            if (self.correspOrJoin != ""):
                f.write(" ; " + self.correspOrJoin + "_" + str(cter))
                cter = cter + 1

            nb = nb + 1
            if (nb <= nbt):
                f.write(" [] ")

                # Caution : the translation pattern below may not be semantically correct
                #  because it discards the decision for some of the outgoing branches
                # Moreover, it adds many unnecessary internal actions

                # nb=1
                # while (nb<=nboutf):
                #    f.write("outf_"+str(nb)+";")
                #    nb2=1
                #    nbbar=1
                #    f.write(" par ")
                #    while (nb2<=nboutf):
                #        if (nb!=nb2):
                #            # the internal choice here ic crucial, otherwise the env decides
                #            # and the corresponding LTS is erroneous
                #            f.write("select i; outf_"+str(nb2)+" [] i; null end select ")
                #            nbbar=nbbar+1
                #            if (nbbar<nboutf):
                #                f.write("||")
                #        nb2=nb2+1
                #    nb=nb+1
                #    f.write(" end par ")
                #    if (nb<=nboutf):
                #        f.write("[]")

        f.write(" end select end loop end var end var\n")
        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):

        if (self.correspOrJoin != ""):

            nboutf = len(self.outgoingFlows)
            alphaout = []
            nb = 1
            while (nb <= nboutf):
                alphaout.append("outf_" + str(nb))
                nb = nb + 1
            allcombi = computeAllCombinations(alphaout)
            nbt = len(allcombi)

            # we dump the synchronization points
            if (nbt > 0):
                cter = 1
                for elem in allcombi:
                    f.write(self.correspOrJoin + "_" + str(cter))
                    cter = cter + 1
                    if (cter <= nbt):
                        f.write(",")
                f.write(" -> ")

            # process call + alphabet
            f.write("orsplit_" + self.ident)

            f.write("[")
            f.write(self.incomingFlows[0].ident + "_finish,")
            i = 0
            while (i < nboutf):
                f.write(self.outgoingFlows[i].ident + "_begin")
                i = i + 1
                if (i < nboutf):
                    f.write(",")

            if (nbt > 0):
                f.write(", ")
                cter = 1
                for elem in allcombi:
                    f.write(self.correspOrJoin + "_" + str(cter))
                    cter = cter + 1
                    if (cter <= nbt):
                        f.write(",")
            f.write("]")

        else:
            f.write("orsplit_" + self.ident)
            SplitGateway.mainlnt(self, f)

    # For an or split, if not visited yet, recursive call on the target nodes of all outgoing flows
    # We increase the depth, to distinguish it from the split or being analyzed
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:
            res = []
            for f in self.outgoingFlows:
                res = res + f.getTarget().reachableOrJoin(visited + [self.ident], depth + 1)
            return res

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        SplitGateway.dumpMaude(self,f,"inclusive")


##
# Class for XOrSplitGateway
class XOrSplitGateway(SplitGateway):
    def __init__(self, ident, inc, out):
        SplitGateway.__init__(self, ident, inc, out)

    # Generates the process for exclusive split gateway
    # Takes as input the number of outgoing flows
    def lnt(self, f):
        nboutf = len(self.outgoingFlows)
        f.write("process xorsplit_" + self.ident + " [incf:any,")
        nb = 1
        while (nb <= nboutf):
            f.write("outf_" + str(nb) + ":any")
            nb = nb + 1
            if (nb <= nboutf):
                f.write(",")
        f.write(" ] is \n")
        f.write(" var ident: ID in loop incf (?ident of ID); \n")
        f.write(" select ")
        nb = 1
        while (nb <= nboutf):
            f.write("outf_" + str(nb) + "(?ident of ID)")
            nb = nb + 1
            if (nb <= nboutf):
                f.write("[]")
        f.write(" end select end loop end var\n")
        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        f.write("xorsplit_" + self.ident)
        SplitGateway.mainlnt(self, f)

    # For an xor split, call to the super class
    def reachableOrJoin(self, visited, depth):
        return SplitGateway.reachableOrJoin(self, visited, depth)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        SplitGateway.dumpMaude(self,f,"exclusive")

##
# Class for AndSplitGateway
class AndSplitGateway(SplitGateway):
    def __init__(self, ident, inc, out):
        SplitGateway.__init__(self, ident, inc, out)

    # Generates the process for parallel split gateway
    # Takes as input the number of outgoing flows
    def lnt(self, f):
        nboutf = len(self.outgoingFlows)
        f.write("process andsplit_" + self.ident + " [incf:any,")
        nb = 1
        while (nb <= nboutf):
            f.write("outf_" + str(nb) + ":any")
            nb = nb + 1
            if (nb <= nboutf):
                f.write(",")
        f.write(" ] is \n")

        ctervar=nboutf
        f.write(" var ")
        while (ctervar>0):
            f.write("ident"+str(ctervar)+":ID")
            ctervar=ctervar-1
            if (ctervar>0):
                f.write(",")
        f.write(" in ")

        f.write(" var ident: ID in loop incf (?ident of ID); \n")
        f.write(" par ")
        nb = 1
        ctervar=nboutf
        while (nb <= nboutf):
            f.write("outf_" + str(nb) + "(?ident"+str(ctervar)+" of ID)")
            ctervar=ctervar-1
            nb = nb + 1
            if (nb <= nboutf):
                f.write("||")
        f.write(" end par end loop end var end var\n")
        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        f.write("andsplit_" + self.ident)
        SplitGateway.mainlnt(self, f)

    # For an and split, call to the super class
    def reachableOrJoin(self, visited, depth):
        return SplitGateway.reachableOrJoin(self, visited, depth)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        SplitGateway.dumpMaude(self,f,"parallel")

##
# Abstract Class for JoinGateway
class JoinGateway(Gateway):
    def __init__(self, ident, inc, out):
        Gateway.__init__(self, ident, inc, out)

    # Generates process instantiation for all join gateways
    def mainlnt(self, f):
        # we assume one outgoing flow 
        nbincf = len(self.incomingFlows)
        f.write("[")
        i = 0
        while (i < nbincf):
            f.write(self.incomingFlows[i].ident + "_finish")
            i = i + 1
            f.write(",")
        f.write(self.outgoingFlows[0].ident + "_begin]")

    # For a join (generic), if not visited yet, recursive call on the target node of the outgoing flow
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:
            return self.outgoingFlows[0].getTarget().reachableOrJoin(visited + [self.ident], depth)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f,nameop):
        f.write("        merge("+self.ident+","+nameop+",")
        cpt=len(self.incomingFlows)
        f.write("(")
        for ofl in self.incomingFlows:
            cpt=cpt-1
            f.write(ofl.ident)
            if (cpt>0):
                f.write(",")
        f.write("),"+self.outgoingFlows[0].ident+")")


##
# Class for OrJoinGateway
class OrJoinGateway(JoinGateway):
    def __init__(self, ident, inc, out):
        JoinGateway.__init__(self, ident, inc, out)
        self.correspOrSplit = ""  # contains the identifier of the corresponding split (if there is one)

    # Generates the process for inclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self, f):
        nbincf = len(self.incomingFlows)

        alphainc = []
        nb = 1
        while (nb <= nbincf):
            alphainc.append("incf_" + str(nb))
            nb = nb + 1
        allcombi = computeAllCombinations(alphainc)
        nbt = len(allcombi)

        f.write("process orjoin_" + self.ident + " [")
        nb = 1
        while (nb <= nbincf):
            f.write("incf_" + str(nb) + ":any")
            nb = nb + 1
            f.write(",")
        f.write("outf:any ")

        # we add to the alphabet potential additional synchronization points
        if (nbt > 0) and (self.correspOrSplit != ""):
            cter = 1
            f.write(",")
            for elem in allcombi:
                f.write(self.ident + "_" + str(cter) + ":any")
                cter = cter + 1
                if (cter <= nbt):
                    f.write(",")

        f.write("] is \n")

        ctervar=len(allcombi)
        f.write(" var ")
        while (ctervar>0):      # TODO: we generate unnecessary variables..
            f.write("ident"+str(ctervar)+":ID")
            ctervar=ctervar-1
            if (ctervar>0):
                f.write(",")
        f.write(" in ")

        f.write(" var ident: ID in loop select ")

        nb = 1
        cter = 1
        for t in allcombi:
            nbelem = len(t)
            nb2 = 1

            # add synchronization points if there's a corresponding split
            if (self.correspOrSplit != ""):
                f.write(self.ident + "_" + str(cter) + ";")
                cter = cter + 1

            if (nbelem > 1):
                ctervar=len(allcombi)
                f.write(" par ")
                for e in t:
                    f.write(e+" (?ident"+str(ctervar)+" of ID)")
                    ctervar=ctervar-1
                    nb2 = nb2 + 1
                    if (nb2 <= nbelem):
                        f.write("||")
                f.write(" end par ")
            else:
                f.write(t[0]+" (?ident of ID)")

            nb = nb + 1
            if (nb <= nbt):
                f.write(" [] ")

                # nb=1
                ## we can execute at least one branch to all for merges as well
                # while (nb<=nbincf):
                #    f.write("incf_"+str(nb)+";")
                #    nb2=1
                #    nbbar=1
                #    f.write(" par ")
                #    while (nb2<=nbincf):
                #        if (nb!=nb2):
                #            f.write("select incf_"+str(nb2)+" [] null end select ")
                #            nbbar=nbbar+1
                #            if (nbbar<nbincf):
                #                f.write("||")
                #        nb2=nb2+1
                #    f.write(" end par ")
                #    nb=nb+1
                #    if (nb<=nbincf):
                #        f.write("[]")

        f.write(" end select ; outf (?ident of ID) end loop end var end var \n")

        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):

        if (self.correspOrSplit != ""):

            nbincf = len(self.incomingFlows)
            alphainc = []
            nb = 1
            while (nb <= nbincf):
                alphainc.append("incf_" + str(nb))
                nb = nb + 1
            allcombi = computeAllCombinations(alphainc)
            nbt = len(allcombi)

            # we dump synchronization points
            if (nbt > 0):
                cter = 1
                for elem in allcombi:
                    f.write(self.ident + "_" + str(cter))
                    cter = cter + 1
                    if (cter <= nbt):
                        f.write(",")
                f.write(" -> ")

            # process call + alphabet
            f.write("orjoin_" + self.ident)

            f.write("[")
            i = 0
            while (i < nbincf):
                f.write(self.incomingFlows[i].ident + "_finish")
                i = i + 1
                f.write(",")
            f.write(self.outgoingFlows[0].ident + "_begin")

            if (nbt > 0):
                cter = 1
                f.write(",")
                for elem in allcombi:
                    f.write(self.ident + "_" + str(cter))
                    cter = cter + 1
                    if (cter <= nbt):
                        f.write(",")

            f.write("]")

        else:
            f.write("orjoin_" + self.ident)
            JoinGateway.mainlnt(self, f)

    # For an or join, if not visited yet, recursive call on the target node of the outgoing flow
    # We store the result and we decrease the depth
    # Returns the list of reachable or joins
    def reachableOrJoin(self, visited, depth):
        if self.ident in visited:
            return []
        else:
            return [(self.ident, depth)] + self.outgoingFlows[0].getTarget().reachableOrJoin(visited + [self.ident],
                                                                                             depth - 1)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        JoinGateway.dumpMaude(self,f,"inclusive")


##
# Class for XOrJoinGateway
class XOrJoinGateway(JoinGateway):
    def __init__(self, ident, inc, out):
        JoinGateway.__init__(self, ident, inc, out)

    # Generates the process for exclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self, f):
        nbincf = len(self.incomingFlows)
        f.write("process xorjoin_" + self.ident + " [")
        nb = 1
        while (nb <= nbincf):
            f.write("incf_" + str(nb) + ":any")
            nb = nb + 1
            f.write(",")
        f.write("outf:any] is \n")
        f.write(" var ident: ID in loop select ")
        nb = 1
        while (nb <= nbincf):
            f.write("incf_" + str(nb)+" (?ident of ID)")
            nb = nb + 1
            if (nb <= nbincf):
                f.write("[]")
        f.write(" end select ; outf (?ident of ID) end loop end var \n")
        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        f.write("xorjoin_" + self.ident)
        JoinGateway.mainlnt(self, f)

    # For an and split, call to the super class
    def reachableOrJoin(self, visited, depth):
        return JoinGateway.reachableOrJoin(self, visited, depth)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        JoinGateway.dumpMaude(self,f,"exclusive")

##
# Class for AndJoinGateway
class AndJoinGateway(JoinGateway):
    def __init__(self, ident, inc, out):
        JoinGateway.__init__(self, ident, inc, out)

    # Generates the process for parallel join gateway
    # Takes as input the number of incoming flows
    def lnt(self, f):
        nbincf = len(self.incomingFlows)
        f.write("process andjoin_" + self.ident + " [")
        nb = 1
        while (nb <= nbincf):
            f.write("incf_" + str(nb) + ":any")
            nb = nb + 1
            f.write(",")
        f.write("outf:any] is \n")

        ctervar=nbincf
        f.write(" var ")
        while (ctervar>0):
            f.write("ident"+str(ctervar)+":ID")
            ctervar=ctervar-1
            if (ctervar>0):
                f.write(",")
        f.write(" in ")

        f.write(" var ident:ID in loop par ")
        nb = 1
        ctervar=nbincf
        while (nb <= nbincf):
            f.write("incf_" + str(nb) + " (?ident"+str(ctervar)+" of ID)")
            ctervar=ctervar-1
            nb = nb + 1
            if (nb <= nbincf):
                f.write("||")
        f.write(" end par ; outf (?ident of ID) end loop end var end var \n")
        f.write("end process\n")

    # Generates process instantiation for main LNT process
    def mainlnt(self, f):
        f.write("andjoin_" + self.ident)
        JoinGateway.mainlnt(self, f)

    # For an and split, call to the super class
    def reachableOrJoin(self, visited, depth):
        return JoinGateway.reachableOrJoin(self, visited, depth)

    # Dumps a Maude line of code into the given file
    def dumpMaude(self,f):
        JoinGateway.dumpMaude(self,f,"parallel")

##
# Class for Processes described in PIF
# Attributes: a name, a list of nodes, a list of flows, an initial node, a list of final nodes
class Process:
    def __init__(self):
        self.name = ""
        self.nodes = []  # contains all nodes but initial and final nodes
        self.flows = []
        self.initial = None
        self.finals = []

    # This method dumps a textual version of a process (useful for debugging purposes)
    def dump(self):
        print "NAME: " + self.name
        print "INITIAL NODE"
        self.initial.dump()
        print "FINAL NODES"
        for n in self.finals:
            n.dump()
        print "NODES"
        for n in self.nodes:
            n.dump()
        print "FLOWS"
        for f in self.flows:
            f.dump()

    # Computes the process alphabet
    def alpha(self):
        alph = []
        for n in self.nodes:
            alph = alph + n.alpha()
        return alph

    # This method applies a pre-processing to the whole process
    # and computes correspondences between or splits/merges
    def reachableOrJoin(self):
        # we traverse all process nodes and call this computation for all inclusive splits
        for n in self.nodes:
            if isinstance(n, OrSplitGateway):
                restmp = n.reachableOrJoin([], -1)
                # print restmp
                res = analyzeReachabilityResults(restmp, len(n.outgoingFlows))
                # print res
                if (res != ""):
                    n.correspOrJoin = res  # we update the split attribute
                    njoin = self.getNode(res)  # we retrieve the object corresponding to the join id
                    njoin.correspOrSplit = n.ident  # we update the join attribute

    # Computes the list with the additionnal synchronization points for corresponding or splits/joins
    def computeAddSynchroPoints(self):
        res = []
        for n in self.nodes:
            if isinstance(n, OrSplitGateway):
                if (n.correspOrJoin != ""):
                    alphaout = []
                    nb = 1
                    while (nb <= len(n.outgoingFlows)):
                        alphaout.append("outf_" + str(nb))
                        nb = nb + 1
                    allcombi = computeAllCombinations(alphaout)
                    nbt = len(allcombi)
                    cter = 1
                    for elem in allcombi:
                        res.append(n.correspOrJoin + "_" + str(cter))
                        cter = cter + 1
        return res

    # Generates the scheduler process
    def generateScheduler(self,f):
        f.write("\nprocess scheduler [")
        nbflows = len(self.flows)
        cter = 1
        for fl in self.flows:
            f.write(fl.ident + "_begin:any, " + fl.ident + "_finish:any")
            cter = cter + 1
            if (cter <= nbflows):
                f.write(", ")
        f.write("] (activeflows: IDS) is\n") # this parameter stores the set of active flows / tokens

        f.write("var ident: ID in\n")
        f.write("select \n")
        cter = 1
        for fl in self.flows:
            f.write(fl.ident + "_begin (?ident of ID) \n") # TODO : add ident activeflows + rec. call
            f.write(" []\n")
            f.write(fl.ident + "_finish (?ident of ID) \n") # TODO : remove ident from activeflows + rec. call
            cter = cter + 1
            if (cter <= nbflows):
                f.write("[]\n")
        f.write("end select\n")
        f.write("end var\n")

        f.write("end process\n")


    # Generates an LNT module and process for a BPMN 2.0 process
    def genLNT(self, name=""):
        if name == "":
            filename = self.name + ".lnt"
        else:
            filename = name + ".lnt"
        f = open(filename, 'w')
        f.write("module " + self.name + " with \"get\" is\n\n")

        # Generates an ID type for all flow identifiers
        f.write("(* Data type for flow identifiers, useful for scheduling purposes *)\n")
        f.write("type ID is\n")
        counter=len(self.flows)
        for fl in self.flows:
            f.write(fl.ident)
            counter=counter-1
            if (counter>0):
                f.write(",")
        f.write("\n")
        f.write("with \"==\",\"!=\"\n")
        f.write("end type\n\n")

        # Generates an type for identifier set
        f.write("type IDS is\n")
        f.write("    set of ID\n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type\n\n")

        if (self.initial != None):
            self.initial.lnt(f)
        # Generates one process for final events and events, this is enough because generic processes
        if (self.finals != []):
            self.finals[0].lnt(f)
        if (self.flows != []):
            self.flows[0].lnt(f)  # TODO: ConditionalFlow?

        # Generates LNT processes for all other nodes
        specialnodes = []  # we keep track of nodes that need to be translated only once
        for n in self.nodes:
            if isinstance(n, Interaction) or isinstance(n, MessageSending) or isinstance(n, MessageReception) or isinstance(n, Task):
                if (n.__class__.__name__ in specialnodes):
                    pass
                else:
                    if isinstance(n, Task):
                        # a task is identified with its number of incoming and outgoing flows
                        nclass=n.__class__.__name__
                        nclass=nclass+"_"+str(len(n.incomingFlows))+"_"+str(len(n.outgoingFlows))
                        #print nclass
                        if (nclass in specialnodes):
                            pass
                        else:
                            specialnodes.append(nclass)
                            n.lnt(f)
                    else:
                        specialnodes.append(n.__class__.__name__)
                        n.lnt(f)
            else:
                n.lnt(f)

        # Note: up to here, translation patterns are independent of the actual tasks, comm, etc.
        # The actual names will be used only in the MAIN process when computing the process alphabet
        #  and instantiating processes

        # scheduler process generation
        self.generateScheduler(f)

        f.write("\nprocess MAIN ")
        alph = self.alpha()
        dumpAlphabet(alph, f, True)
        f.write(" is\n")
        # computes additional synchros for or splits/joins
        addSynchro = self.computeAddSynchroPoints()
        nbsync = len(addSynchro)

        f.write(" hide begin:any, finish:any")
        nbflows = len(self.flows)
        if (nbflows > 0):
            f.write(", ")
            cter = 1
            for fl in self.flows:
                f.write(fl.ident + "_begin:any, " + fl.ident + "_finish:any")
                cter = cter + 1
                if (cter <= nbflows):
                    f.write(", ")
                    # we hide additional synchros for or splits/joins as well
            nb = 0
            if (nbsync > 0):
                f.write(", ")
                for e in addSynchro:
                    f.write(e + ":any")
                    nb = nb + 1
                    if (nb < nbsync):
                        f.write(", ")

        f.write(" in\n")
        f.write("par ")
        # synchronizations on all begin/finish flows 
        if (nbflows > 0):
            cter = 1
            for fl in self.flows:
                f.write(fl.ident + "_begin, " + fl.ident + "_finish")
                cter = cter + 1
                if (cter <= nbflows):
                    f.write(", ")

        f.write(" in\n")

        # interleaving of all flow processes
        f.write(" par \n")
        cter = 1
        for fl in self.flows:
            # TODO: take ConditionalFlow into account
            f.write("flow [" + fl.ident + "_begin, " + fl.ident + "_finish] ("+fl.ident+")")
            cter = cter + 1
            if (cter <= nbflows):
                f.write(" || ")
        f.write("\n end par \n")
        f.write("\n||\n")

        # interleaving of all node processes
        f.write(" par ")

        # process instantiation for initial node
        f.write("init [begin," + self.initial.outgoingFlows[0].ident + "_begin] || ")  # we assume a single output flow
        nbfinals = len(self.finals)
        cter = 1
        # processes instantiation for final nodes
        for n in self.finals:
            f.write("final [" + n.incomingFlows[0].ident + "_finish, finish]")  # we assume a single incoming flow
            cter = cter + 1
            if (cter <= nbflows):
                f.write(" || ")
        # processes instantiation for all other nodes 
        nbnodes = len(self.nodes)
        cter = 1
        for n in self.nodes:
            n.mainlnt(f)
            cter = cter + 1
            if (cter <= nbnodes):
                f.write(" || ")
        f.write("\n end par \n")

        f.write("\n end par\n")
        f.write(" end hide\n")
        f.write("\nend process\n")

        f.write("\nend module\n")

        f.close()

    # Generates an SVL file
    def genSVL(self, smartReduction=True):
        filename = self.name + ".svl"
        f = open(filename, 'w')
        f.write(
            "% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n")  # \"% CADP_TIME=\"memtime\"\n\n")
        f.write("% DEFAULT_PROCESS_FILE=" + self.name + ".lnt\n\n")
        # process generation (LTS)
        # f.write("\"" + self.name + ".bcg\" = safety reduction of tau*.a reduction of branching reduction of \"MAIN")
        # generation of the raw bcg
        f.write("\"" + self.name + "_raw.bcg\" = generation of \"MAIN")
        alpha = self.alpha()
        dumpAlphabet(alpha, f, False)
        f.write("\";\n\n")
        # reduction of the raw bcg
        #f.write("\"" + self.name + ".bcg\" = tau*.a reduction of branching reduction of " + "\"" + self.name + "_raw.bcg\";\n\n")
        f.write("\"" + self.name + ".bcg\" = branching reduction of " + "\"" + self.name + "_raw.bcg\";\n\n")
        f.close()

        # make the SVL file executable
        import os
        import stat
        st = os.stat(filename)
        os.chmod(filename, st.st_mode | 0111)

    # This method takes as input a file.pif and generates a PIF Python object
    def buildProcessFromFile(self, filename, debug=False):
        # open xml document specified in fileName
        xml = file(filename).read()
        try:
            proc = pif.CreateFromDocument(xml)
            self.name = proc.name

            # we first create all nodes without incoming/outgoing flows
            for n in proc.behaviour.nodes:
                # initial and final events
                if isinstance(n, pif.InitialEvent_):
                    node = InitialEvent(n.id, [], [])
                    self.initial = node
                if isinstance(n, pif.EndEvent_):
                    node = EndEvent(n.id, [], [])
                    self.finals.append(node)

                # tasks / emissions / receptions / interactions
                if isinstance(n, pif.Task_):
                    node = Task(n.id, [], [])
                if isinstance(n, pif.MessageSending_):
                    node = MessageSending(n.id, [], [], n.message)
                if isinstance(n, pif.MessageReception_):
                    node = MessageReception(n.id, [], [], n.message)
                if isinstance(n, pif.Interaction_):
                    node = Interaction(n.id, [], [], n.message, n.initiatingPeer, n.receivingPeers)

                # split gateways
                if isinstance(n, pif.AndSplitGateway_):
                    node = AndSplitGateway(n.id, [], [])
                if isinstance(n, pif.OrSplitGateway_):
                    node = OrSplitGateway(n.id, [], [])
                if isinstance(n, pif.XOrSplitGateway_):
                    node = XOrSplitGateway(n.id, [], [])

                # join gateways
                if isinstance(n, pif.AndJoinGateway_):
                    node = AndJoinGateway(n.id, [], [])
                if isinstance(n, pif.OrJoinGateway_):
                    node = OrJoinGateway(n.id, [], [])
                if isinstance(n, pif.XOrJoinGateway_):
                    node = XOrJoinGateway(n.id, [], [])

                if not (isinstance(n, pif.InitialEvent_)) and not (isinstance(n, pif.EndEvent_)):
                    self.nodes.append(node)

            # creation of flow Objects
            for sf in proc.behaviour.sequenceFlows:
                flow = Flow(sf.id, self.getNode(sf.source), self.getNode(sf.target))
                self.flows.append(flow)
                self.addFlow(flow)

        except pyxb.UnrecognizedContentError, e:
            print 'An error occured while parsing xml document ' + filename
            print 'Unrecognized element, the message was "%s"' % (e.message)

    # Takes as input a node identifier and returns the corresponding object
    def getNode(self, nident):
        res = None
        if (nident == self.initial.ident):
            return self.initial
        for n in self.finals:
            if (nident == n.ident):
                return n
        for n in self.nodes:
            if (nident == n.ident):
                return n

                # Updates the list of incoming/outgoing flows for all nodes given a flow in parameter

    def addFlow(self, flow):
        if (flow.source.ident == self.initial.ident):
            self.initial.outgoingFlows.append(flow)
        for n in self.finals:
            if (flow.target.ident == n.ident):
                n.incomingFlows.append(flow)
        for n in self.nodes:
            if (flow.source.ident == n.ident):
                n.outgoingFlows.append(flow)
            if (flow.target.ident == n.ident):
                n.incomingFlows.append(flow)


# This class generates an LTS (BCG format) from a PIF model
class Generator:
    # computes the LTS model (BCG file) for a PIF model
    # @param pifFilename String, name of the PIF file
    # @param smartReduction boolean, true if a smart reduction is done on the LTS when loading it, false else
    # @param debug boolean, true to get debug information, false else
    # @return (Integer, String, Collection<String>), return code, name of the model (can be different from the filename) and its alphabet
    def __call__(self, pifFilename, smartReduction=True, debug=False):

        import subprocess
        proc = Process()
        # load PIF model
        proc.buildProcessFromFile(pifFilename)
        pifModelName = proc.name
        # pre-processing: compute correspondences between or splits/joins
        proc.reachableOrJoin()
        # generate the LNT code for the model
        proc.genLNT()
        # compute the LTS from the LNT code using SVL, possibly with a smart reduction
        proc.genSVL(smartReduction)
        pr = Popen(["svl", pifModelName], shell=False, stdout=sys.stdout)
        #pr = Popen("env", shell=True, stdout=sys.stdout)
        pr.communicate()
        # return name and alphabet
        return (ReturnCodes.TERM_OK, pifModelName,proc.alpha()) # TODO: use return value from SVL call


# This class gets loads an LTS (BCG format) for a PIF model
# - if the LTS file already exists and has a timestamp greater than the PIF file, no transformation is done
# - else, transformation is first achieved (using Generator)
class Loader:
    # gets the name and the alphabet of the LTS for the PIF model
    # @param pifFilename String, name of the PIF file
    # @param smartReduction boolean, true if a smart reduction is done on the LTS when loading it, false else
    # @param debug boolean, true to get debug information, false else
    # @return (Integer, String, Collection<String>), return code, name of the model (can be different from the filename) and its alphabet
    def __call__(self, pifFilename, smartReduction=True, debug=False):
        proc = Process()
        proc.buildProcessFromFile(pifFilename)
        pifModelName = proc.name
        ltsFilename = proc.name + LTS_SUFFIX
        if self.__needsRebuild(pifFilename, ltsFilename):
            generator = Generator()
            return generator(pifFilename, smartReduction=smartReduction, debug=debug)
        else:
            return (ReturnCodes.TERM_OK, pifModelName, proc.alpha())

    # decides if the LTS for the pifFileName has to be recomputed
    # @param pifFilename String, name of the PIF file
    # @param ltsFilename String, name of the LTS file
    # @return boolean, true if the LTS must be rebuilt from the PIF file, false else
    def __needsRebuild(self, pifFilename, ltsFilename):
        import os
        # if the LTS file does not exists -> rebuild
        if not os.path.isfile(ltsFilename):
            return True
        # if the timestamp of the LTS file is older than the PIF file -> rebuild
        timePifFile = os.stat(pifFilename).st_mtime
        timeLtsFile = os.stat(ltsFilename).st_mtime
        if timeLtsFile < timePifFile:
            return True
        # else -> no need to rebuild
        return False

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(prog='pif2lnt', description='Computes a BCG model from a PIF model.')
    parser.add_argument('--version', action='version', version='%(prog)s 1.0')
    parser.add_argument('model', metavar='Model',
                    help='the PIF model to transform (filename of PIF file)')
    parser.add_argument('--lazy', action='store_true',
                    help='does not recompute the BCG model if it already exists and is more recent than the PIF model')

    # parse arguments
    try:
        args = parser.parse_args()
    except:
        parser.print_help()
        sys.exit(ReturnCodes.TERM_PROBLEM)

    if args.lazy:
        # generate BCG only if needed (does not exist or exists and is older than PIF)
        loader = Loader()
        (res, model, alphabet) = loader(args.model)
    else:
        # generate BCG in any case
        generator = Generator()
        (res, model, alphabet) = generator(args.model)

    sys.exit(res)
