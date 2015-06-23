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

    def print(self):
        print "Node "+self.ident+"("+self.incomingFlows+")"

##
# Class for Flows
class Flow:

    def __init__(self,ident,source,target):
        self.ident=ident
        self.source=source
        self.target=target

##
# Class for ConditionalFlows
class ConditionalFlow(Flow):

    def __init__(self,ident,source,target,cond):
        Flow.__init__(self,ident,source,target)
        self.cond=cond

##
# Class for Initial Event
class InitialEvent(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

##
# Class for End Event
class EndEvent(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

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

##
# Class for MessageReception
class MessageReception(MessageCommunication):

    def __init__(self,ident,inc,out,msg):
        MessageCommunication.__init__(self,ident,inc,out,msg)

##
# Class for Task
class Task(Node):

    def __init__(self,ident,inc,out):
        Node.__init__(self,ident,inc,out)

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

##
# Class for XOrSplitGateway
class XOrSplitGateway(SplitGateway):

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)

##
# Class for AndSplitGateway
class AndSplitGateway(SplitGateway):

    def __init__(self,ident,inc,out):
        SplitGateway.__init__(self,ident,inc,out)

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

##
# Class for XOrJoinGateway
class XOrJoinGateway(JoinGateway):

    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)

##
# Class for AndJoinGateway
class AndJoinGateway(JoinGateway):
    def __init__(self,ident,inc,out):
        JoinGateway.__init__(self,ident,inc,out)

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

    def print(self):
        print "NAME: "+self.name
        print "INITIAL NODE"
        initial.print()
        print "FINAL NODES"
        finals.print()
        print "NODES"
        nodes.print()
        print "FLOWS"
        flows.print()

    # Generates an LNT module and process for a BPMN 2.0 process
    def genLNT(self,name=""):
        if name=="":
            filename=self.name+".lnt"
        else:
            filename=name+".lnt"
        f=open(filename, 'w')
        f.write("module "+self.name+" with \"get\" is\n\n")
        # TODO : voir vbpmn.py

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

            allnodes=[]
            # we first create all nodes without incoming/outgoing flows
            for n in proc.behaviour.nodes:
                if isinstance(n, pif.InitialEvent_):
                    node=InitialEvent(n.id, [], [])
                    self.initial=node
                if isinstance(n, pif.EndEvent_):
                    node=EndEvent(n.id, [], [])
                    self.finals.append(node)

                #  tasks / emissions / receptions / interactions
                if isinstance(n, pif.Task_):
                    node=Task(n.id, [], [])
                    self.nodes.append(node)
                if isinstance(n, pif.MessageSending_):
                    node=MessageSending(n.id, [], [], n.message)
                    self.nodes.append(node)
                if isinstance(n, pif.MessageReception_):
                    node=MessageReception(n.id, [], [], n.message)
                    self.nodes.append(node)
                if isinstance(n, pif.Interaction_):
                    node=Interaction(n.id, [], [], n.message, n.initiatingPeer, n.receivingPeers)
                    self.nodes.append(node)

                # split gateways
                if isinstance(n, pif.AndSplitGateway_):
                    node=AndSplitGateway(n.id, [], [])
                    self.nodes.append(node)
                if isinstance(n, pif.OrSplitGateway_):
                    node=OrSplitGateway(n.id, [], [])
                    self.nodes.append(node)
                if isinstance(n, pif.XOrSplitGateway_):
                    node=XOrSplitGateway(n.id, [], [])
                    self.nodes.append(node)

                # join gateways
                if isinstance(n, pif.AndJoinGateway_):
                    node=AndJoinGateway(n.id, [], [])
                    self.nodes.append(node)
                if isinstance(n, pif.OrJoinGateway_):
                    node=OrJoinGateway(n.id, [], [])
                    self.nodes.append(node)
                if isinstance(n, pif.XOrJoinGateway_):
                    node=XOrJoinGateway(n.id, [], [])
                    self.nodes.append(node)
                
                # storing all nodes for having a direct access when building flows
                allnodes.append(node)

            # creation of flow Objects
            for sf in proc.behaviour.sequenceFlows:
                flow=Flow(sf.id,self.getNode(sf.source,allnodes),self.getNode(sf.target,allnodes))
                self.flows.append(flow)
                self.addFlow(flow,allnodes)

        except pyxb.UnrecognizedContentError, e:
            print 'An error occured while parsing xml document ' + filename
            print 'Unrecognized element, the message was "%s"' % (e.message)


    # Takes as input a node identifier and returns the corresponding object
    def getNode(self,nident,allnodes):
        res=None
        for n in allnodes:
            if (nident==n.ident):
                res=n
        return res

    # Updates the list of incoming/outgoing flows given a flow in parameter
    def addFlow(self,flow,allnodes):
        for n in allnodes:
            if (n.ident==flow.source):
                n.outgoingFlows.append(flow)
            if (n.ident==flow.target):
                n.incomingFlows.append(flow)

# This class generates an LTS (bcg format) from a PIF process 
class Generator:

    # generates LNT, SVL, and finally call the method above to obtain the LTS
    def generateLTS(self, filename, smartReduction = True, debug = False):
        proc = Process()
        proc.buildProcessFromFile(filename)

        name = proc.name
        proc.genLNT()

        proc.print()

        #proc.genSVL(smartReduction)
        #process = Popen (["svl",name], shell = False, stdout=sys.stdout)
            
        #return (name,proc.alpha())
        return (name,[])


##############################################################################################
if __name__ == '__main__':

        filename=sys.argv[1]
        print "converting " + filename + " to LTS.."
        (name,alpha)=Generator().generateLTS(filename)
