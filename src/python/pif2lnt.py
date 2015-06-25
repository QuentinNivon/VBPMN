#
# Name:    pif2lnt.py - Classes for loading a PIF model
#                        and translating it to LNT 
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

from subprocess import *
import os.path

import pyxb
import time
import pif
import sys
import itertools

# Dumps alphabet (list of strings) in the given file
def dumpAlphabet(alph,f,addany):

    nbelem=len(alph)
    if (nbelem>0):
        f.write("[")
        cter=1
        for e in alph:
            f.write(e)
            if addany:
                f.write(":any")
            cter=cter+1
            if (cter<=nbelem):
                f.write(", ")
        f.write("] ")

# Computes all permutations, any possible number, given a list of elements
def computeAllCombinations(l):
    nbelem=len(l)
    i=1
    res=[]
    while (i<=nbelem):
        restmp=itertools.permutations(l,i)
        for r in restmp:
            res.append(r)
        i=i+1
    return res


##
# Abstract class for Nodes
# Should not be directly used. Use child classes instead.
class Node:

    def __init__(self,ident,inc,out):
        self.ident=ident
        self.incomingFlows=inc
        self.outgoingFlows=out

    # This method dumps a textual version of a node (useful for debugging purposes)
    def dump(self):
        print "Node "+self.ident+" in: ",
        for f in self.incomingFlows:
            print f.ident+" ",
        print " out: ",
        for f in self.outgoingFlows:
            print f.ident+" ",
        print ""

##
# Class for Flows
class Flow:

    tolnt=False # indicates if the LNT process has already been generated

    def __init__(self,ident,source,target):
        self.ident=ident
        self.source=source
        self.target=target

    # This method dumps a textual version of a flow (useful for debugging purposes)
    def dump(self):
        print "Flow "+self.source.ident+"--"+self.ident+"-->"+self.target.ident

    # Generates the (generic) process for flows, only once
    def lnt(self,f):
        if not(Flow.tolnt):
            f.write("process flow [begin:any, finish:any] is\n")
            f.write(" begin ; finish\n")
            f.write("end process\n")
            Flow.tolnt=True

##
# Class for ConditionalFlows
class ConditionalFlow(Flow):

    def __init__(self,ident,source,target,cond):
        Flow.__init__(self,ident,source,target)
        self.cond=cond

    # Generates the process for conditional flows
    def lnt(self,f):
        pass # TODO 

##
# Class for Initial Event
class InitialEvent(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

    # Generates the (generic) process for the initial event, only once
    def lnt(self,f):
        f.write("process init [begin:any, outf:any] is\n")
        f.write(" begin ; outf \n")
        f.write("end process\n")

##
# Class for End Event
class EndEvent(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

    # Generates the (generic) process for final events, only once
    def lnt(self,f):
        f.write("process final [incf:any, finish:any] is\n")
        f.write(" incf; finish\n")
        f.write("end process\n")

##
# Abstract Class for Communication
class Communication(Node):

    def __init__(self,ident,inc,out,msg):
        Node.__init__(self,ident,inc,out)
        self.msg=msg

##
# Class for Interaction
class Interaction(Communication):

    tolnt=False # indicates if the LNT process has already been generated

    def __init__(self,ident,inc,out,msg,sender,receivers):
        Communication.__init__(self,ident,inc,out,msg)
        self.sender=sender
        self.receivers=receivers

    # Generates the (generic) process for interactions, only once
    def lnt(self,f):
        if not(Interaction.tolnt):
            f.write("process interaction [incf:any, inter:any, outf:any] is\n")
            f.write(" incf; inter; outf \n")
            f.write("end process\n")
            Interaction.tolnt=True

    # Computes alphabet for an interaction
    def alpha(self):
        return [self.sender+str(self.receivers)+self.msg] # TODO: refine

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        # we assume one incoming flow and one outgoing flow
        f.write("interaction ["+self.incomingFlows[0].ident+"_finish,")
        f.write(self.sender+str(self.receivers)+self.msg+",")
        f.write(self.outgoingFlows[0].ident+"_begin]")

##
# Abstract Class for MessageCommunication
class MessageCommunication(Communication):

    def __init__(self,ident,inc,out,msg):
        Communication.__init__(self,ident,inc,out,msg)

##
# Class for MessageSending
class MessageSending(MessageCommunication):

    tolnt=False # indicates if the LNT process has already been generated

    def __init__(self,ident,inc,out,msg):
        MessageCommunication.__init__(self,ident,inc,out,msg)

    # Generates the (generic) process for message sending, only once
    def lnt(self,f):
        if not(MessageSending.tolnt):
            f.write("process messagesending [incf:any, msg:any, outf:any] is\n")
            f.write(" incf; msg; outf \n")
            f.write("end process\n")
            MessageSending.tolnt=True

    # Computes alphabet for a message sending
    def alpha(self):
        return [self.msg+"_EM"]

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        # we assume one incoming flow and one outgoing flow
        f.write("messagesending ["+self.incomingFlows[0].ident+"_finish,")
        f.write(self.msg+"_EM,")
        f.write(self.outgoingFlows[0].ident+"_begin]")

##
# Class for MessageReception
class MessageReception(MessageCommunication):

    tolnt=False # indicates if the LNT process has already been generated

    def __init__(self,ident,inc,out,msg):
        MessageCommunication.__init__(self,ident,inc,out,msg)

    # Generates the (generic) process for message reception, only once
    def lnt(self,f):
        if not(MessageReception.tolnt):
            f.write("process messagereception [incf:any, msg:any, outf:any] is\n")
            f.write(" incf; msg; outf \n")
            f.write("end process\n")
            MessageReception.tolnt=True

    # Computes alphabet for a message reception
    def alpha(self):
        return [self.msg+"_REC"] 

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        # we assume one incoming flow and one outgoing flow
        f.write("messagereception ["+self.incomingFlows[0].ident+"_finish,")
        f.write(self.msg+"_REC,")
        f.write(self.outgoingFlows[0].ident+"_begin]")

##
# Class for Task
class Task(Node):

    tolnt=False # indicates if the LNT process has already been generated

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

    # Generates the (generic) process for task, only once
    def lnt(self,f):
        if not(Task.tolnt):
            f.write("process task [incf:any, task:any, outf:any] is\n")
            f.write(" incf; task; outf \n")
            f.write("end process\n")
            Task.tolnt=True

    # Computes alphabet for a task
    def alpha(self):
        return [self.ident]

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        # we assume one incoming flow and one outgoing flow
        f.write("task ["+self.incomingFlows[0].ident+"_finish,")
        f.write(self.ident+",")
        f.write(self.outgoingFlows[0].ident+"_begin]")

##
# Abstract Class for Gateway
class Gateway(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

    # Computes alphabet for a message sending
    def alpha(self):
        return [] 

##
# Abstract Class for SplitGateway
class SplitGateway(Gateway):

    def __init__(self,ident,inc,out):
        Gateway.__init__(self,ident,inc,out)

    # Generates process instantiation for all split gateways
    def mainlnt(self,f):
        # we assume one incoming flow 
        nboutf=len(self.outgoingFlows)
        f.write("[")
        f.write(self.incomingFlows[0].ident+"_finish,")
        i=0
        while (i<nboutf):
            f.write(self.outgoingFlows[i].ident+"_begin")
            i=i+1
            if (i<nboutf):
                f.write(",")
        f.write("]")


##
# Class for OrSplitGateway
class OrSplitGateway(SplitGateway):

    tolnt=[] # contains a table containing the number of outgoing flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)

    # Generates the process for inclusive split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f):
        nboutf=len(self.outgoingFlows)
        if not(nboutf in OrSplitGateway.tolnt):
            f.write("process orsplit_"+str(nboutf)+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" select ")

            # We translate the inclusive split by enumerating all combinations in a select

            #alphaout=[]
            #nb=1
            #while (nb<=nboutf):
            #    alphaout.append("outf_"+str(nb))
            #    nb=nb+1
            #allcombi=computeAllCombinations(alphaout)
            #nbt=len(allcombi)
            #nb=1
            #for t in allcombi:
            #    nbelem=len(t)
            #    nb2=1
            #    for e in t:
            #        f.write(e)
            #        nb2=nb2+1
            #        if (nb2<=nbelem):
            #            f.write(";")
            #    nb=nb+1
            #    if (nb<=nbt):
            #        f.write(" [] ")

            # Caution : the translation pattern below may not be semantically correct
            #  because it discards the decision for some of the outgoing branches
            # The solution above using flattening might be an alternative option

            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+";")
                nb2=1
                nbbar=1
                while (nb2<=nboutf):
                    if (nb!=nb2):
                        f.write("select i; outf_"+str(nb2)+" [] i; null end select ")
                        nbbar=nbbar+1
                        if (nbbar<nboutf):
                            f.write("||")
                    nb2=nb2+1
                nb=nb+1
                if (nb<=nboutf):
                    f.write("[]")

            f.write(" end select\n")
            f.write("end process\n")
            OrSplitGateway.tolnt.append(nboutf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("orsplit_"+str(len(self.outgoingFlows)))
        SplitGateway.mainlnt(self,f)

##
# Class for XOrSplitGateway
class XOrSplitGateway(SplitGateway):

    tolnt=[] # contains a table containing the number of outgoing flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)

    # Generates the process for exclusive split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f):
        nboutf=len(self.outgoingFlows)
        if not(nboutf in XOrSplitGateway.tolnt):
            f.write("process xorsplit_"+str(nboutf)+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" select ")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+"")
                nb=nb+1
                if (nb<=nboutf):
                    f.write("[]")
            f.write(" end select\n")
            f.write("end process\n")
            XOrSplitGateway.tolnt.append(nboutf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("xorsplit_"+str(len(self.outgoingFlows)))
        SplitGateway.mainlnt(self,f)

##
# Class for AndSplitGateway
class AndSplitGateway(SplitGateway):

    tolnt=[] # contains a table containing the number of outgoing flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)

    # Generates the process for parallel split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f):
        nboutf=len(self.outgoingFlows)
        if not(nboutf in AndSplitGateway.tolnt):
            f.write("process andsplit_"+str(nboutf)+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" par ")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+str(nb)+"")
                nb=nb+1
                if (nb<=nboutf):
                    f.write("||")
            f.write(" end par\n")
            f.write("end process\n")
            AndSplitGateway.tolnt.append(nboutf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("andsplit_"+str(len(self.outgoingFlows)))
        SplitGateway.mainlnt(self,f)

##
# Abstract Class for JoinGateway
class JoinGateway(Gateway):

    def __init__(self,ident,inc,out):
        Gateway.__init__(self,ident,inc,out)

    # Generates process instantiation for all join gateways
    def mainlnt(self,f):
        # we assume one outgoing flow 
        nbincf=len(self.incomingFlows)
        f.write("[")
        i=0
        while (i<nbincf):
            f.write(self.incomingFlows[i].ident+"_finish")
            i=i+1
            f.write(",")
        f.write(self.outgoingFlows[0].ident+"_begin]")

##
# Class for OrJoinGateway
class OrJoinGateway(JoinGateway):

    tolnt=[] # contains a table containing the number of incoming flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)

    # Generates the process for inclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f):
        nbincf=len(self.incomingFlows)
        if not(nbincf in OrJoinGateway.tolnt):
            f.write("process orjoin_"+str(nbincf)+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+str(nb)+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")

            #f.write(" par ")
            #nb=1
            #while (nb<=nbincf):
            #    f.write("select incf_"+str(nb)+" [] null end select ")
            #    nb=nb+1
            #    if (nb<=nbincf):
            #        f.write("||")
            #f.write(" end par ; outf\n")

            f.write(" select ")
            nb=1
            # we can execute at least one branch to all for merges as well
            while (nb<=nbincf):
                f.write(" par ")
                f.write("incf_"+str(nb)+"||")
                nb2=1
                nbbar=1
                while (nb2<=nbincf):
                    if (nb!=nb2):
                        f.write("select incf_"+str(nb2)+" [] null end select ")
                        nbbar=nbbar+1
                        if (nbbar<nbincf):
                            f.write("||")
                    nb2=nb2+1
                f.write(" end par ")
                nb=nb+1
                if (nb<=nbincf):
                    f.write("[]")
            f.write(" end select ; outf\n")

            f.write("end process\n")
            OrJoinGateway.tolnt.append(nbincf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("orjoin_"+str(len(self.incomingFlows)))
        JoinGateway.mainlnt(self,f)

##
# Class for XOrJoinGateway
class XOrJoinGateway(JoinGateway):

    tolnt=[] # contains a table containing the number of incoming flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)

    # Generates the process for exclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f):
        nbincf=len(self.incomingFlows)
        if not(nbincf in XOrJoinGateway.tolnt):
            f.write("process xorjoin_"+str(nbincf)+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+str(nb)+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")
            f.write(" select ")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+str(nb))
                nb=nb+1
                if (nb<=nbincf):
                    f.write("[]")
            f.write(" end select ; outf\n")
            f.write("end process\n")
            XOrJoinGateway.tolnt.append(nbincf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("xorjoin_"+str(len(self.incomingFlows)))
        JoinGateway.mainlnt(self,f)

##
# Class for AndJoinGateway
class AndJoinGateway(JoinGateway):

    tolnt=[] # contains a table containing the number of incoming flows
             # for which LNT processes have already been generated

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)

    # Generates the process for parallel join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f):
        nbincf=len(self.incomingFlows)
        if not(nbincf in AndJoinGateway.tolnt):
            f.write("process andjoin_"+str(nbincf)+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+str(nb)+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")
            f.write(" par ")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+str(nb)+"")
                nb=nb+1
                if (nb<=nbincf):
                    f.write("||")
            f.write(" end par ; outf\n")
            f.write("end process\n")
            AndJoinGateway.tolnt.append(nbincf)

    # Generates process instantiation for main LNT process
    def mainlnt(self,f):
        f.write("andjoin_"+str(len(self.incomingFlows)))
        JoinGateway.mainlnt(self,f)

##
# Class for Processes described in PIF
# Attributes: a name, a list of nodes, a list of flows, an initial node, a list of final nodes
class Process:

    def __init__(self):
        self.name=""
        self.nodes=[] # contains all nodes but initial and final nodes
        self.flows=[]
        self.initial=None
        self.finals=[]

    # This method dumps a textual version of a process (useful for debugging purposes)
    def dump(self):
        print "NAME: "+self.name
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
        alph=[]
        for n in self.nodes:
            alph=alph+n.alpha()
        return alph

    # Generates an LNT module and process for a BPMN 2.0 process
    def genLNT(self,name=""):
        if name=="":
            filename=self.name+".lnt"
        else:
            filename=name+".lnt"
        f=open(filename, 'w')
        f.write("module "+self.name+" with \"get\" is\n\n")

        if (self.initial!=None):
            self.initial.lnt(f)
        # Generates one process for final events and events, this is enough because generic processes
        if (self.finals!=[]):
            self.finals[0].lnt(f)
        if (self.flows!=[]):
            self.flows[0].lnt(f)

        # Generates LNT processes for all other nodes
        for n in self.nodes:
            n.lnt(f) 

        # Note: up to here, translation patterns are independent of the actual tasks, comm, etc.
        # The actual names will be used only in the MAIN process when computing the process alphabet
        #  and instantiating processes

        f.write("\nprocess MAIN ")
        alph=self.alpha()
        dumpAlphabet(alph,f,True)
        f.write(" is\n")
        f.write(" hide begin:any, finish:any")
        nbflows=len(self.flows)
        if (nbflows>0):
            f.write(", ")
            cter=1
            for fl in self.flows:
                f.write(fl.ident+"_begin:any, "+fl.ident+"_finish:any")
                cter=cter+1
                if (cter<=nbflows):
                    f.write(", ")        
        f.write(" in\n")
        f.write("par ")
        # synchronizations on all begin/finish flows
        if (nbflows>0):
            cter=1
            for fl in self.flows:
                f.write(fl.ident+"_begin, "+fl.ident+"_finish")
                cter=cter+1
                if (cter<=nbflows):
                    f.write(", ")
        f.write(" in\n")

        # interleaving of all flow processes
        f.write(" par \n")
        cter=1
        for fl in self.flows:
            f.write("flow["+fl.ident+"_begin, "+fl.ident+"_finish]")
            cter=cter+1
            if (cter<=nbflows):
                f.write(" || ")
        f.write("\n end par \n")
        f.write("\n||\n")

        # interleaving of all node processes
        f.write(" par \n")
        # process instantiation for initial node
        f.write("init[begin,"+self.initial.outgoingFlows[0].ident+"_begin] || ") # TODO: on suppose un seul out flow, a affiner ! 
        nbfinals=len(self.finals)
        cter=1
        # processes instantiation for final nodes
        for n in self.finals:
            f.write("final["+n.incomingFlows[0].ident+"_finish, finish]") # TODO: on suppose un seul incoming flow, a affiner !
            cter=cter+1
            if (cter<=nbflows):
                f.write(" || ")
        # processes instantiation for all other nodes 
        nbnodes=len(self.nodes)
        cter=1
        for n in self.nodes:
            n.mainlnt(f)
            cter=cter+1
            if (cter<=nbnodes):
                f.write(" || ")
        f.write("\n end par \n")

        f.write("\n end par\n")
        f.write(" end hide\n")
        f.write("\nend process\n")

        f.write("\nend module\n")

        f.close()

    # Generates an SVL file
    def genSVL(self, smartReduction = True):
        filename=self.name+".svl"
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n") #\"% CADP_TIME=\"memtime\"\n\n")
        f.write ("% DEFAULT_PROCESS_FILE=" + self.name + ".lnt\n\n")
        # process generation (LTS)
        #f.write("\"" + self.name + ".bcg\" = safety reduction of tau*.a reduction of branching reduction of \"MAIN")
        f.write("\"" + self.name + ".bcg\" = branching reduction of \"MAIN")
        alpha=self.alpha()
        dumpAlphabet(alpha,f,False)
        f.write("\";\n\n")
        f.close()

    # This method takes as input a file.pif and generates a PIF Python object
    def buildProcessFromFile(self, filename, debug = False):
        # open xml document specified in fileName
        xml = file(filename).read()
        try:
            proc = pif.CreateFromDocument(xml)
            self.name = proc.name

            # we first create all nodes without incoming/outgoing flows
            for n in proc.behaviour.nodes:
                # initial and final events
                if isinstance(n, pif.InitialEvent_):
                    node=InitialEvent(n.id, [], [])
                    self.initial=node
                if isinstance(n, pif.EndEvent_):
                    node=EndEvent(n.id, [], [])
                    self.finals.append(node)

                #  tasks / emissions / receptions / interactions
                if isinstance(n, pif.Task_):
                    node=Task(n.id, [], [])
                if isinstance(n, pif.MessageSending_):
                    node=MessageSending(n.id, [], [], n.message)
                if isinstance(n, pif.MessageReception_):
                    node=MessageReception(n.id, [], [], n.message)
                if isinstance(n, pif.Interaction_):
                    node=Interaction(n.id, [], [], n.message, n.initiatingPeer, n.receivingPeers)

                # split gateways
                if isinstance(n, pif.AndSplitGateway_):
                    node=AndSplitGateway(n.id, [], [])
                if isinstance(n, pif.OrSplitGateway_):
                    node=OrSplitGateway(n.id, [], [])
                if isinstance(n, pif.XOrSplitGateway_):
                    node=XOrSplitGateway(n.id, [], [])

                # join gateways
                if isinstance(n, pif.AndJoinGateway_):
                    node=AndJoinGateway(n.id, [], [])
                if isinstance(n, pif.OrJoinGateway_):
                    node=OrJoinGateway(n.id, [], [])
                if isinstance(n, pif.XOrJoinGateway_):
                    node=XOrJoinGateway(n.id, [], [])

                if not(isinstance(n, pif.InitialEvent_)) and not(isinstance(n, pif.EndEvent_)):
                    self.nodes.append(node)
                
            # creation of flow Objects
            for sf in proc.behaviour.sequenceFlows:
                flow=Flow(sf.id,self.getNode(sf.source),self.getNode(sf.target))
                self.flows.append(flow)
                self.addFlow(flow)

        except pyxb.UnrecognizedContentError, e:
            print 'An error occured while parsing xml document ' + filename
            print 'Unrecognized element, the message was "%s"' % (e.message)


    # Takes as input a node identifier and returns the corresponding object
    def getNode(self,nident):
        res=None
        if (nident==self.initial.ident):
            return self.initial
        for n in self.finals:
            if (nident==n.ident):
                return n 
        for n in self.nodes:
            if (nident==n.ident):
                return n 

    # Updates the list of incoming/outgoing flows for all nodes given a flow in parameter
    def addFlow(self,flow):
        if (flow.source.ident==self.initial.ident):
            self.initial.outgoingFlows.append(flow)
        for n in self.finals:
            if (flow.target.ident==n.ident):
                n.incomingFlows.append(flow)
        for n in self.nodes:
            if (flow.source.ident==n.ident):
                n.outgoingFlows.append(flow)
            if (flow.target.ident==n.ident):
                n.incomingFlows.append(flow)

# This class generates an LTS (bcg format) from a PIF process 
class Generator:

    # generates LNT, SVL, and finally call the method above to obtain the LTS
    def generateLTS(self, filename, smartReduction = True, debug = False):
        proc = Process()
        proc.buildProcessFromFile(filename)

        name = proc.name
        proc.genLNT()
        # proc.dump()

        proc.genSVL(smartReduction)
        process = Popen (["svl",name], shell = False, stdout=sys.stdout)

        return (name,proc.alpha())
 

##############################################################################################
if __name__ == '__main__':

    import itertools

    filename=sys.argv[1]
    print "converting " + filename + " to LTS.."
    (name,alpha)=Generator().generateLTS(filename)

