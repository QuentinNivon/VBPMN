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

    def __init__(self,ident,source,target):
        self.ident=ident
        self.source=source
        self.target=target
        self.tolnt=False # indicates if the LNT process has already been generated

    # This method dumps a textual version of a flow (useful for debugging purposes)
    def dump(self):
        print "Flow "+self.source.ident+"--"+self.ident+"-->"+self.target.ident

    # Generates the (generic) process for flows, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process flow [begin:any, end:any] is\n")
            f.write(" begin ; end\n")
            f.write("end process\n")
            self.tolnt=True

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
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for the initial event, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process init [begin:any, outf:any] is\n")
            f.write(" begin ; outf \n")
            f.write("end process\n")
            self.tolnt=True

##
# Class for End Event
class EndEvent(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for final events, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process final [incf:any, end:any] is\n")
            f.write(" incf; end\n")
            f.write("end process\n")
            self.tolnt=True

##
# Abstract Class for Communication
class Communication(Node):

    def __init__(self,ident,inc,out,msg):
        Node.__init__(self,ident,inc,out)
        self.msg=msg

##
# Class for Interaction
class Interaction(Communication):

    def __init__(self,ident,inc,out,msg,sender,receivers):
        Communication.__init__(self,ident,inc,out,msg)
        self.sender=sender
        self.receivers=receivers
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for interactions, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process interaction [incf:any, inter:any, outf:any] is\n")
            f.write(" incf; inter; outf \n")
            f.write("end process\n")
            self.tolnt=True

##
# Abstract Class for MessageCommunication
class MessageCommunication(Communication):

    def __init__(self,ident,inc,out,msg):
        Communication.__init__(self,ident,inc,out,msg)

##
# Class for MessageSending
class MessageSending(MessageCommunication):

    def __init__(self,ident,inc,out,msg):
        MessageCommunication.__init__(self,ident,inc,out,msg)
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for message sending, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process messagesending [incf:any, msg:any, outf:any] is\n")
            f.write(" incf; msg; outf \n")
            f.write("end process\n")
            self.tolnt=True

##
# Class for MessageReception
class MessageReception(MessageCommunication):

    def __init__(self,ident,inc,out,msg):
        MessageCommunication.__init__(self,ident,inc,out,msg)
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for message reception, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process messagereception [incf:any, msg:any, outf:any] is\n")
            f.write(" incf; msg; outf \n")
            f.write("end process\n")
            self.tolnt=True

##
# Class for Task
class Task(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)
        self.tolnt=False # indicates if the LNT process has already been generated

    # Generates the (generic) process for task, only once
    def lnt(self,f):
        if not(self.tolnt):
            f.write("process task [incf:any, task:any, outf:any] is\n")
            f.write(" incf; task; outf \n")
            f.write("end process\n")
            self.tolnt=True

##
# Abstract Class for Gateway
class Gateway(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

##
# Abstract Class for SplitGateway
class SplitGateway(Gateway):

    def __init__(self,ident,inc,out):
        Gateway.__init__(self,ident,inc,out)

##
# Class for OrSplitGateway
class OrSplitGateway(SplitGateway):

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of outgoing flows
                      # for which LNT processes have already been generated

    # Generates the process for inclusive split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f,nboutf):
        if not(nboutf in self.tolnt):
            f.write("process orsplit_"+nboutf+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+nb+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" par ")
            nb=1
            while (nb<=nboutf):
                f.write("select outf_"+nb+" [] null end select ")
                nb=nb+1
                if (nb<=nboutf):
                    f.write("||")
            f.write(" end par ")
            f.write("end process\n")
            self.tolnt.append(nboutf)

##
# Class for XOrSplitGateway
class XOrSplitGateway(SplitGateway):

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of outgoing flows
                      # for which LNT processes have already been generated

    # Generates the process for exclusive split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f,nboutf):
        if not(nboutf in self.tolnt):
            f.write("process xorsplit_"+nboutf+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+nb+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" select ")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+nb+"")
                nb=nb+1
                if (nb<=nboutf):
                    f.write("[]")
            f.write(" end select ")
            f.write("end process\n")
            self.tolnt.append(nboutf)

##
# Class for AndSplitGateway
class AndSplitGateway(SplitGateway):

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of outgoing flows
                      # for which LNT processes have already been generated

    # Generates the process for parallel split gateway
    # Takes as input the number of outgoing flows
    def lnt(self,f,nboutf):
        if not(nboutf in self.tolnt):
            f.write("process andsplit_"+nboutf+" [incf:any,")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+nb+":any")
                nb=nb+1
                if (nb<=nboutf):
                    f.write(",")
            f.write(" ] is \n")
            f.write(" incf; \n")
            f.write(" par ")
            nb=1
            while (nb<=nboutf):
                f.write("outf_"+nb+"")
                nb=nb+1
                if (nb<=nboutf):
                    f.write("||")
            f.write(" end par ")
            f.write("end process\n")
            self.tolnt.append(nboutf)


##
# Abstract Class for JoinGateway
class JoinGateway(Gateway):

    def __init__(self,ident,inc,out):
        Gateway.__init__(self,ident,inc,out)

##
# Class for OrJoinGateway
class OrJoinGateway(JoinGateway):

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of incoming flows
                      # for which LNT processes have already been generated

    # Generates the process for inclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f,nbincf):
        if not(nbincf in self.tolnt):
            f.write("process orjoin_"+nbincf+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+nb+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")
            f.write(" par ")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+nb+"")
                nb=nb+1
                if (nb<=nbincf):
                    f.write("||")
            f.write(" end par ; outf")
            f.write("end process\n")
            self.tolnt.append(nbincf)
##
# Class for XOrJoinGateway
class XOrJoinGateway(JoinGateway):

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of incoming flows
                      # for which LNT processes have already been generated

    # Generates the process for exclusive join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f,nbincf):
        if not(nbincf in self.tolnt):
            f.write("process xorjoin_"+nbincf+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+nb+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")
            f.write(" select ")
            nb=1
            while (nb<=nbincf):
                f.write("select incf_"+nb+" [] null end select ")
                nb=nb+1
                if (nb<=nbincf):
                    f.write("[]")
            f.write(" end select ; outf")
            f.write("end process\n")
            self.tolnt.append(nbincf)

##
# Class for AndJoinGateway
class AndJoinGateway(JoinGateway):
    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)
        self.tolnt=[] # contains a table containing the number of incoming flows
                      # for which LNT processes have already been generated

    # Generates the process for parallel join gateway
    # Takes as input the number of incoming flows
    def lnt(self,f,nbincf):
        if not(nbincf in self.tolnt):
            f.write("process andjoin_"+nbincf+" [")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+nb+":any")
                nb=nb+1
                f.write(",")
            f.write("outf:any] is \n")
            f.write(" par ")
            nb=1
            while (nb<=nbincf):
                f.write("incf_"+nb+"")
                nb=nb+1
                if (nb<=nbincf):
                    f.write("||")
            f.write(" end par ; outf")
            f.write("end process\n")
            self.tolnt.append(nbincf)

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
            pass # n.lnt(f) 

        # Note: up to here, translation patterns are independent of the actual tasks, comm, etc.
        # The actual names will be used only in the MAIN process when computing the process alphabet
        #  and instantiating processes

        # TODO : generate LNT pour le process MAIN

        f.write("\nend module\n")

        f.close()

    # Generates an SVL file
    def genSVL(self, smartReduction = True):
        filename=self.name+".svl"
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n") #\"% CADP_TIME=\"memtime\"\n\n")
        f.write ("% DEFAULT_PROCESS_FILE=" + self.name + ".lnt\n\n")
        # process generation (LTS)
        f.write("\"" + self.name + ".bcg\" = safety reduction of tau*.a reduction of branching reduction of \"MAIN")
        #alpha=self.alpha()
        #if not(emptyAlphabet([alpha])):
        #    f.write(" [")
        #dumpAlphabet(alpha,f,False)
        #if not(emptyAlphabet([alpha])):
        #    f.write("]")
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

        proc.dump()

        #proc.genSVL(smartReduction)
        #process = Popen (["svl",name], shell = False, stdout=sys.stdout)
            
        #return (name,proc.alpha())
        return (name,[])


##############################################################################################
if __name__ == '__main__':

        filename=sys.argv[1]
        print "converting " + filename + " to LTS.."
        (name,alpha)=Generator().generateLTS(filename)
