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
    pass

##
# Class for Final Event
class FinalEvent(Node):
    pass

##
# Abstract Class for Communication
class Communication(Node):

    def __init__(self,msg):
        self.msg=msg

##
# Class for Interaction
class Interaction(Communication):

    def __init__(self,sender,receiver):
        self.sender=sender
        self.receiver=receiver

##
# Abstract Class for MessageCommunication
class MessageCommunication(Communication):
    pass

##
# Class for MessageSending
class MessageSending(MessageCommunication):
    pass

##
# Class for MessageReception
class MessageReception(MessageCommunication):
    pass

##
# Class for Task
class Task(Node):
    pass

##
# Abstract Class for Gateway
class Gateway(Node):
    pass

##
# Abstract Class for SplitGateway
class SplitGateway(Gateway):
    pass

##
# Class for OrSplitGateway
class OrSplitGateway(SplitGateway):
    pass

##
# Class for XOrSplitGateway
class XOrSplitGateway(SplitGateway):
    pass

##
# Class for AndSplitGateway
class AndSplitGateway(SplitGateway):
    pass

##
# Abstract Class for JoinGateway
class JoinGateway(Gateway):
    pass

##
# Class for OrJoinGateway
class OrJoinGateway(JoinGateway):
    pass

##
# Class for XOrJoinGateway
class XOrJoinGateway(JoinGateway):
    pass

##
# Class for AndJoinGateway
class AndJoinGateway(JoinGateway):
    pass

##
# Class for Processes described in PIF
# Attributes: a name, a list of nodes, a list of flows, an initial node, a list of final nodes
class Process:

    def __init__(self):
        self.name=""
        self.nodes=[]
        self.flows=[]
        self.initial=""
        self.finals=[]

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

    # this method takes as input a file.pif and generates a PIF Python object
    def buildProcessFromFile(self, filename, debug = False):
        # open xml document specified in fileName
        xml = file(filename).read()
        try:
            proc = pif.CreateFromDocument(xml)
            self.name = proc.name

            # TODO: completer cette methode, voir vbpmn.py

        except pyxb.UnrecognizedContentError, e:
            print 'An error occured while parsing xml document ' + filename
            print 'Unrecognized element, the message was "%s"' % (e.message)


# This class generates an LTS (bcg format) from a PIF process 
class Generator:

    # generates LNT, SVL, and finally call the method above to obtain the LTS
    def generateLTS(self, filename, smartReduction = True, debug = False):
        proc = Process()
        proc.buildProcessFromFile(filename)

        name = proc.name
        proc.genLNT()

        #proc.genSVL(smartReduction)
        #process = Popen (["svl",name], shell = False, stdout=sys.stdout)
            
        #return (name,proc.alpha())
        return (name,[])


##############################################################################################
if __name__ == '__main__':

        filename=sys.argv[1]
        print "converting " + filename + " to LTS.."
        (name,alpha)=Generator().generateLTS(filename)
