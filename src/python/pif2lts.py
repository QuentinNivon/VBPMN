#
# Name:   pif2lts.py - Classes for generating an LTS given a PIF model 
#                      via an encoding to LNT
# Author: Matthias Gudemann, Gwen Salaun, Pascal Poizat
# Date:   03-04-2012
###############################################################################

from subprocess import *
import os.path

import pyxb

import choreo_xml_model
import time

# Second attempt where choreographies are encoded as a graph.

syncPrefix = "synchro_"

# Dumps alphabet
def dumpAlphabet(alpha,f,any,startComma = False,syncMessage = False):
    l=0
    max=len(alpha)
    if max > 0:
        if syncMessage:
            alpha.sort()
        if startComma:
            f.write(",")
        for e in alpha:
            if syncMessage:
                e = syncPrefix + e
            f.write(e)
            if any:
                f.write(":any")
            l=l+1
            if l<max:
                f.write(",")

# Checks whether a string belongs to a list
def isInList(elem,l):
    res=False
    for e in l:
        if (e==elem):
           res=True
    return res

###
# Checks whether a list is included to another list.
# Used only in buildChoreoFromFile to sort states
# PostCondition :
#   'return false' and 'successors is not relevant'
#   'return true' and 'successors contains the list of states in dst which matches with ids in src'
def hasSuccInList(src, dst, successors):
    resGlob = True
    for succ in src:
        resLoc = False
        for e in dst: # e = instance of already encoded state
            if (succ == e.getId()):
                successors.append(e)
                resLoc = True
        resGlob = resGlob and resLoc
    return resGlob

# keeps strings from the list of couples with zero depth
# returns a list of strings
def keepZeroDepthStrings(l):
    res=[]
    for couple in l:
        if (couple[1]==0):
           res=res+[couple[0]]
    return res


# Removes double occurrences of strings in a list
def removeDoubles(l,full):
    single=[]
    double=[]
    for name in l:
        if (full.count(name))>1:
           if not(name in double):
              double.append(name)
        else:
           single.append(name)
    return single+double

# Dumps a call to the succesor state process
def dumpSucc(f,alpha,succ,semic,syncSet = []):
    if (len(succ)>1):
       print "Error: only one succesor expected here!"
    else:
       if semic:
          f.write(";")
       if isinstance(succ[0],AllJoinState):
          f.write(" synchro_"+succ[0].getId()+";null\n")
       elif isinstance(succ[0],SubsetJoinState):
          f.write(" synchro_"+succ[0].getId()+";null\n")
       elif isinstance (succ[0], AllSelectState) or isinstance(succ[0], SubsetSelectState):
           if succ[0].myChoreo.splitInOtherSplitCone(succ[0]):
               f.write(succ[0].ident + " [")
           else:
               f.write("split_" + succ[0].ident + " [")
           dumpAlphabet(alpha,f,False)

           # we need only the intersecting synchronization labels in the signature
           # i.e., those which are in the split and the caller process of that split
           alphaSync = list(set(map (lambda x : x.ident, succ[0].getSyncSet())) & set(syncSet))
           print alpha, alphaSync
           dumpAlphabet(alphaSync, f, False, True, True)
           f.write("]\n")
       else:
           f.write(succ[0].ident)
           f.write(" [")
           dumpAlphabet(alpha,f,False)
           alphaSync = map (lambda x : x.ident, succ[0].getSyncSet())
           dumpAlphabet(alphaSync, f, False, True, True)
           f.write("]\n")

# create parallel composition of all syncMessages in alphaSync
def dumpParallelSyncs(alphaSync, f):
    nb = len(alphaSync)
    if nb > 1:
        f.write ("  par ")
        for msg in alphaSync:
            f.write(syncPrefix + msg + "; null")
            if nb > 1:
                f.write(" || ")
            nb = nb - 1
        f.write("\n  end par\n")
    else:
        f.write(syncPrefix + alphaSync[0] + "; null\n")


# Checks if a default branch exists in this list of states (ident==default)
def existDefaultBranch(lstates):
    res=False
    for s in lstates:
        if (s.ident=="default"):
           res=True
    return res

##
# Computes the union of 2 lists
def union(l1,l2):
    l=[]
    for e in l1:
        if not(e in l2):
           l.append(e)
    l.extend(l2)
    return l

##
# Computes the intersection of 2 lists
def intersection(l1,l2):
    l=[]
    for e in l1:
        if e in l2:
           l.append(e)
    return l

##
# Abstract class for State
# Should not be directly used. Use child classes instead.
class State:

    def __init__(self,ident):
        self.ident=ident
        self.checked = False # to mark visited states
        self.space = " " # offset to print tree
        self.predecessors = [] # store predecessors for bi-directionial navigation on choreos
        self.syncSet = set()

    def getSyncSet(self):
        return self.syncSet

    def getId(self):
        return self.ident

    def doCheckForAllSuccessors (self, prefix, participantList, successors, messageFlows, debug):
        # do the checks for all successor states from current
        # must check for cycles!

        if debug:
            print self.getId()
        self.checked = True
        boolList = map (lambda z: z.checkConditionsFromSpec(prefix + self.space, participantList, messageFlows, debug), successors)
        if debug:
            print boolList
        return reduce (lambda x, y: x and y, boolList, True)

    # checks whether the conditions of the spec are fulfilled
    # that every participant in an Activity was also in the activity directly before
    def checkConditionsFromSpec (self, prefix = "", prePeerList = [], preMesseageFlows = [], debug = False):

        # we have been here before
        if self.checked:
            return True

        # if isinstance (self, IntermediateState):
        #     print "my predecessors are ", self.predecessors

        if (isinstance (self, InitialState)):
            if debug:
                print prefix, "Initial State -> checking all sucessors"
            return self.doCheckForAllSuccessors (prefix + self.space, [], self.getSucc (), [], debug)
        elif (isinstance (self, FinalState)):
            if debug:
                print prefix, "Final State -> No Condition"
            return True

        elif (isinstance (self, InteractionState)):
            if debug:
                print prefix, "Interaction State ", self.ident, "-> check all predecessors"

            if prePeerList == []:
                if debug:
                    print prefix, "no known predeccessor"
            else:
                if debug:
                    print prefix, "searching ", self.initiating, " in ", prePeerList

                listPeerIntersect = filter (lambda x: x == self.initiating, prePeerList)
                if len (listPeerIntersect) == 0:
                    if debug:
                        print "should not be realizable (conditions not satisfied)"
                        print "I suggest to introduce a synchronization of ", self.initiating, " with one of ", prePeerList, "over one of the messages from ", map (lambda x: x.message, preMesseageFlows)
                    return False

            return self.doCheckForAllSuccessors (prefix + self.space, self.getPeers(), self.getSucc (), self.messageflows, debug)

        elif (isinstance (self, ChoiceState)):
            if debug:
                print prefix, "Choice State ", self.ident, "-> treat all branches"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)
        elif (isinstance (self, AllSelectState)):
            if debug:
                print prefix, "All Select State ", self.ident, "-> treat all branches"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)
        elif (isinstance (self, SubsetSelectState)):
            if debug:
                print prefix, "Subset Select State ", self.ident, "-> treat all branches"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)

        #################################
        # TODO : Dominated Choice State #
        #################################

        # elif (isinstance (self, DominatedChoiceState)):

        elif (isinstance (self, SimpleJoinState)):
            if debug:
                print prefix, "Simple Join State -> treat leaving branch"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)
        elif (isinstance (self, SubsetJoinState)):
            if debug:
                print prefix, "Subset Join State -> treat leaving branch"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)
        elif (isinstance (self, AllJoinState)):
            if debug:
                print prefix, "All Join State -> treat leaving branch"
            return self.doCheckForAllSuccessors (prefix + self.space, prePeerList, self.getSucc(), preMesseageFlows, debug)

        else:
            if debug:
                print "ERROR: you forgot me", type (self)
            return False

    def addPredecessor (self, predecessor):
        self.predecessors.append (predecessor)

    def getPredecessors (self):
        return (self.predecessors)

    def getEdges(self, visited):
        if isinstance(self, FinalState):
            return set()
        successors = self.getSucc()
        edgeSet = set()
        visited.add(self)
        for s in successors:
            edgeSet.add((self, s))
            if s not in visited:
                edgeSet = edgeSet | s.getEdges(visited)

        return edgeSet

##
# Class for Initial State
class InitialState(State):

    def __init__(self,ident,succ=[]):
        State.__init__(self,ident)
        self.succ=succ
        map (lambda x: x.addPredecessor(self), succ) # add this state as predecessor for all sucessors

    def alpha(self):
        return []

    def getSucc(self):
        return self.succ

    def reachableParallelMerge(self,visited,depth):
        return self.succ[0].reachableParallelMerge(visited+[self.ident],depth)

    def reachableInclusiveMerge(self,visited,depth):
        return self.succ[0].reachableInclusiveMerge(visited+[self.ident],depth)

    def addSucc(self,state):
        self.succ.append(state)

    def getPeers(self):
        return []

##
# Class for Final State
class FinalState(State):

    def __init__(self,ident):
        State.__init__(self,ident)

    def alpha(self):
        return []

    def lnt(self,f,alpha):
        f.write("null\n")

    def addSucc(self,state):
        self.succ.append(state)

    def reachableParallelMerge(self,visited,depth):
        return []

    def reachableInclusiveMerge(self,visited,depth):
        return []

    def getPeers(self):
        return []

##
# Abstract Class for Intermediate State
class IntermediateState(State):

    def __init__(self,ident,succ=[]):
        State.__init__(self,ident)
        self.succ=succ
        map (lambda x: x.addPredecessor(self), succ) # add this state as predecessor for all successors

    def getSucc(self):
        return self.succ

    def addSucc(self,state):
        self.succ.append(state)


##
# Class for InteractionState
# Attributes: two or more participants, an initiating participant, and a messageflow
class InteractionState(IntermediateState):

    def getMsgID(self):
        return self.messageflows[0].getMessage()

    def __init__(self,ident,succ,participants,initiating,messageflows):
        IntermediateState.__init__(self,ident,succ)
        self.participants=participants
        self.initiating=initiating
        self.messageflows=messageflows

    def alpha(self):
        return self.buildList(self.initiating,self.participants,self.messageflows)

    def getPartner(self,init,part):
        for p in part:
            if (p!=init):
               return p

    def buildList(self,init,part,mf):
        partner=self.getPartner(init,part)
        if len(mf)==1:
           m=mf[0].getMessage()
           return [init+"_"+partner+"_"+m]
        else:
           m1=mf[0].getMessage()
           m2=mf[1].getMessage()
           return [init+"_"+partner+"_"+m1,partner+"_"+init+"_"+m2]

    def lnt(self,f,alpha):
        self.dumpMessage(f)
        # if successor is a merge, we have to emit the according
        # synchronization message

        # dumps succ state process call

        alphaSync = map(lambda x: x.ident, self.getSyncSet())
        # print "interaction for", self.ident, alphaSync
        dumpSucc(f,alpha,self.succ,True,alphaSync)

    def dumpMessage(self,f):
        partner=self.getPartner(self.initiating,self.participants)
        # now : only one message exchanged for each interaction state
        m=self.messageflows[0].getMessage()
        f.write(self.initiating+"_"+partner+"_"+m)

        # if len(self.messageflows)==1:
        #     m=self.messageflows[0].getMessage()
        #     f.write(self.initiating+"_"+partner+"_"+m)
        # # else:
        #    m1=self.messageflows[0].getMessage()
        #    m2=self.messageflows[1].getMessage()
        #    f.write(self.initiating+"_"+partner+"_"+m1)
        #    f.write(";")
        #    f.write(partner+"_"+self.initiating+"_"+m2)

    def reachableParallelMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return self.succ[0].reachableParallelMerge(visited+[self.ident],depth)

    def reachableInclusiveMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return self.succ[0].reachableInclusiveMerge(visited+[self.ident],depth)

    def getPeers(self):
        return self.participants

##
# Class for MessageFlow
# Attributes: a message
class MessageFlow():

    def __init__(self,message):
        self.message=message

    def getMessage(self):
        return self.message

##
# Abstract class for GatewayState
# Should not be directly used. Use child classes instead.
class GatewayState(IntermediateState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def alpha(self):
        return []

    def getPeers(self):
        return []


def isSynchroSelect(s):
    return (isinstance(s, AllSelectState) or isinstance(s, SubsetSelectState))

def isSynchroMerge(s):
    return (isinstance(s, AllJoinState) or isinstance(s, SubsetJoinState))

"""
decides if s has a merge successor m
and no other merge gate is on a path from s to m

remark: - unsure about correctness here
        - works for 4bis example

needsSync(PM,PM).
needsSync(PM,X) :-
        vorNoMerge(X,PM),
        s(PS),
        inCone(PS,PM),
        inCone(PS,X).

vorNoMerge(X,Y) :- edge(X,Y), not(m(X)).
vorNoMerge(X,Y) :- edge(X,Z), not(m(Z)), vorNoMerge(Z,Y).


"""
def successorNoMergeBetween(s, m, edgeList, visitedStates):
    visitedStates.add(s)
    if len (filter(lambda (u, v) : u == s and v == m and not isSynchroMerge(s), edgeList)) > 0:
        return True
    successors = map (lambda (u, v) : v, filter(lambda(u, v) : u == s and not isSynchroMerge(v) and v not in visitedStates, edgeList))
    return reduce (lambda x, y : x or y, map (lambda x : successorNoMergeBetween(x, m, edgeList, visitedStates), successors), False)

"""
decides if "origin" is reachable from "to"

assuming edgeList as a "set" of edges
and stateList as a "set" of states

vor(X,Y) :- edge(X,Y).
vor(X,Y) :- edge(X,Z), vor(Z,Y).
"""
def existsPath(origin, to, edgeList, visitedStates):
    visitedStates.add(origin)
    if (origin, to) in edgeList:
        return True
    successors = map (lambda (u, v) : v, filter (lambda (u, v) : u == origin and v not in visitedStates, edgeList))
    return reduce (lambda x, y : x or y, map (lambda x : existsPath(x, to, edgeList, visitedStates), successors), False)

"""
try to find a successor which is of type GatewayMergeState

inCone(PS,Y) :- n(Y), s(PS), vor(PS,Y), m(Z), vor(Y,Z).
inCone(PS,Y) :- m(Y), s(PS), vor(PS,Y).
inCone(PS,PS) :- s(PS).
"""
def existsMergeSuccessor(origin, edgeList, visitedStates):
    successors = map (lambda (u, v) : v, filter (lambda (u, v) : u == origin and v not in visitedStates, edgeList))
    visitedStates.add(origin)
    if len (filter(lambda x : isSynchroMerge(x), successors)) > 0:
        return True
    return reduce (lambda x, y: x or y, map (lambda x : existsMergeSuccessor(x, edgeList, visitedStates), successors), False)

##
# Abstract class for GatewaySplitState
# Should not be directly used. Use child classes instead.
class GatewaySplitState(GatewayState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def setConeSet(self, coneSet):
        self.coneSet = coneSet

    def getConeSet(self):
        return self.coneSet

    """
    computes the cone set of a split
    this is the set of all reachable states
    between the split and its merge

    the states in this set must potentially
    be synchronized

    pCone(PS,L) :- findall(Y,inCone(PS,Y),L1), uniq(L1,[],L).

    """
    def computeConeSet(self, edgeSet, states):
        #if self.coneSetComputedP:
        #    return self.coneSet
        coneSet = set()

        for state in states:
            if state.ident == 'pm2':
                a = 1
            if state == self:
                coneSet.add(state)
            elif isSynchroMerge(state) and existsPath(self, state, edgeSet, set()):
                coneSet.add(state)
            elif existsPath(self, state, edgeSet, set()) and existsMergeSuccessor(state, edgeSet, set()):
                coneSet.add(state)
        print self.ident, "has cone set - ",
        for state in coneSet:
            print state.ident, " - ",
        print ""
        return coneSet

    # emit synchro messages from syncSet for parallel composition
    # format: synchro_msg1, synchro_msg2 -> p1
    #         synchro_msg2               -> p2
    #         ...
    #  messages in hideSet are hidden
    #  processes in synchroProcessSet are in the parallel composition
    def computeSetParallelComposition(self, synchroProcessSet, f, alpha):

        f.write(" par\n")

        nb=len(synchroProcessSet)
        for s in synchroProcessSet:

            syncMsg = map(lambda x : x.ident, s.getSyncSet())
            self.dumpSynchronization(f, syncMsg, False, True)

            # emit parallel process call
            f.write(s.ident)
            f.write(" [")
            dumpAlphabet(alpha,f,False)
            alphaSync = map(lambda x : x.ident, s.getSyncSet())
            dumpAlphabet(list(alphaSync), f, False, True, True)
            f.write("]\n")
            nb=nb-1
            if nb>0:
                f.write(" ||\n")

        f.write(" end par\n")


    # write all synchronization labels in rpm
    def dumpSynchronization(self, file, rpm, datatype = False, arrow = False):
        nbrpm = len (rpm)
        if nbrpm > 0:
            for merge in rpm:
                if not datatype:
                    file.write(syncPrefix+merge)
                else:
                    file.write(syncPrefix + merge + datatype)
                nbrpm=nbrpm-1
                if nbrpm>0:
                    file.write(",")
            if arrow:
                file.write(" -> ")

    def reachableParallelMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           res=[]
           for s in self.succ:
               res=res+s.reachableParallelMerge(visited+[self.ident],depth)
           return res

    def reachableInclusiveMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           res=[]
           for s in self.succ:
               res=res+s.reachableInclusiveMerge(visited+[self.ident],depth)
           return res

##
# Abstract class for GatewayMergeState
# Should not be directly used. Use child classes instead.
class GatewayMergeState(GatewayState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def reachableParallelMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return self.succ[0].reachableParallelMerge(visited+[self.ident],depth)

    def reachableInclusiveMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return self.succ[0].reachableInclusiveMerge(visited+[self.ident],depth)

##
# Class for ChoiceState
# Attributes: none
class ChoiceState(GatewaySplitState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def lnt(self,f,alpha):
        f.write(" select\n")
        nb=len(self.succ)
        for s in self.succ:
            if (isinstance(s, AllSelectState) or isinstance(s, SubsetSelectState)) and not self.myChoreo.splitInOtherSplitCone(s):
                f.write("split_")
            f.write(s.ident)
            f.write(" [")
            dumpAlphabet(alpha,f,False)
            # TODO
            # to verify: if successor is an AllSelect or SubSetSelect, then there is a split_ gate which closes
            #            over its own SyncSet, i.e., only the synchro messages in the current state need to be considered
            # why not using dumpSucc here?
            #
            if (isinstance(s, AllSelectState) or isinstance(s, SubsetSelectState)) and not self.myChoreo.splitInOtherSplitCone(s):
                alphaSync = map(lambda x : x.ident, self.getSyncSet())
            else:
                alphaSync = map(lambda x : x.ident, s.getSyncSet())
            dumpAlphabet(alphaSync,f,False, True, True)
            f.write("]\n")
            f.write("\n")
            nb=nb-1
            if nb>0:
               f.write(" [] ")
        f.write(" end select\n")

    def reachableParallelMerge(self,visited,depth):
        return GatewaySplitState.reachableParallelMerge(self,visited,depth)

    def reachableInclusiveMerge(self,visited,depth):
        return GatewaySplitState.reachableInclusiveMerge(self,visited,depth)

################################
## TODO : Complete this class ##
################################
class DominatedChoiceState(GatewaySplitState):

    def __init__(self,ident,succ,dominant):
        IntermediateState.__init__(self, ident, succ)
        self.dominant = dominant

##
# Class for SimpleJoinState
# Attributes: none
class SimpleJoinState(GatewayMergeState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def lnt(self,f,alpha):
        # dumps succ state process call
        dumpSucc(f,alpha,self.succ,False)

    def reachableParallelMerge(self,visited,depth):
        return GatewayMergeState.reachableParallelMerge(self,visited,depth)

    def reachableInclusiveMerge(self,visited,depth):
        return GatewayMergeState.reachableInclusiveMerge(self,visited,depth)

##
# Class for SubsetSelectState
# Attributes: none
class SubsetSelectState(GatewaySplitState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)
        #self.default = default

    def lnt(self,f,alpha):
        # # TODO : ajouter la branche default, voir bpmn.py
        # lmerge=self.reachableInclusiveMerge([],-1)
        # lmerge=keepZeroDepthStrings(lmerge)
        # rpm=removeDoubles(lmerge,lmerge)
        default=existDefaultBranch(self.succ)

        rpm = map(lambda x : x.ident, self.getSyncSet())

        # hiding synchronization messages
        # "default" is no more used as message

        # now hidden in split_XXX process

        # if (rpm!=[]):
        #     f.write(" hide ")
        #     self.dumpSynchronization(f, rpm, ":any")
        #     f.write(" in")

        f.write("\nselect\n")

        # loop over all successors e.g. A,B,C (and default)
        # generate processes of the form:
        # sync ->     A || (B [] sync; null || C [] sync; null)
        #          [] B || (A [] null || C [] null)
        #          [] C || (A [] null || B [] null)
        #         ([] default)
        # sync; null is important!

        nb=len(self.succ)
        for activeProcess in self.succ:

            # if a default branch exists, it can be chosen XOR
            # a combination of the other branches!
            # the BPMN spec is not clear in this case!
            if default and (activeProcess.ident == "default"):
                f.write("default [")
                dumpAlphabet(alpha, f, False)
                alphaSync = map(lambda x : x.ident, activeProcess.getSyncSet())
                dumpAlphabet(alphaSync, f, False, True, True)
                f.write("]\n")
                nb = nb - 1
                break;

            # we assume that there are at least 2 alternatives, excluding the default case
            # all else does not really make sense!

            f.write ("par\n")
            alphaSync = map(lambda x : x.ident, activeProcess.getSyncSet())
            self.dumpSynchronization(f, alphaSync, False, True)
            f.write(activeProcess.ident)
            f.write(" [")
            dumpAlphabet(alpha,f,False)
            dumpAlphabet(alphaSync, f, False, True, True)
            f.write("]\n")
            f.write ("||\n")
            alternatives = len (self.succ) - 1
            alternativeDone = 0
            # self.dumpSynchronization(f, alphaSync, False, True)

            # more than 2 alternatives and no default
            # or more than 2 alternatives -> parallel composition
            if (alternatives == 2 and not default) or (alternatives > 2):
                f.write("par\n")

            # self.dumpSynchronization(f, alphaSync, False, True)
            # f.write (" select\n")
            first = True
            for perhapsProcess in self.succ:
                alternativeDone = alternativeDone + 1

                # skip current active process and default one
                if perhapsProcess.ident != activeProcess.ident and perhapsProcess.ident != "default":

                    alphaSync = map(lambda x : x.ident, perhapsProcess.getSyncSet())
                    self.dumpSynchronization(f, alphaSync, False, True)
                    if first:
                        f.write(" select\n")
                        first = False
                    f.write(perhapsProcess.ident)
                    f.write(" [")
                    dumpAlphabet(alpha,f,False)
                    dumpAlphabet(alphaSync,f,False,True,True)
                    f.write("]\n")
                    if len(alphaSync) > 0:
                        f.write("[]\n")
                        dumpParallelSyncs(alphaSync, f)
                    else:
                        f.write("[]\n null\n")

                    if (alternativeDone < alternatives and not default) or (default and alternativeDone < alternatives - 1):
                        f.write("||\n")

            f.write ("end select\n")
            if (alternatives == 2 and not default) or (alternatives > 2):
                f.write("\nend par\n")
            f.write("\nend par\n")
            nb = nb - 1
            if nb > 0:
                f.write("[]\n")

        # close alternative selection
        f.write(" end select\n")

        # s.o.

        # if (rpm!=[]):
        #     f.write("end hide\n")


    def reachableParallelMerge(self,visited,depth):
        return GatewaySplitState.reachableParallelMerge(self,visited,depth)

    def reachableInclusiveMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           res=[]
           for s in self.succ:
               res=res+s.reachableInclusiveMerge(visited+[self.ident],depth+1)
           return res

##
# Class for SubsetJoinState
# Attributes: none
class SubsetJoinState(GatewayMergeState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def lnt(self,f,alpha):
        # emit synchro message
        f.write(syncPrefix + self.ident + "; ")
        if (not isSynchroMerge(self.succ[0])):
            dumpSucc(f,alpha,self.succ,False)
        else:
            f.write(" null\n")

    def reachableParallelMerge(self,visited,depth):
        return GatewayMergeState.reachableParallelMerge(self,visited,depth)

    def reachableInclusiveMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return [(self.ident,depth)]+self.succ[0].reachableInclusiveMerge(visited+[self.ident],depth-1)


##
# Class for AllSelectState
# Attributes: none
class AllSelectState(GatewaySplitState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def lnt(self,f,alpha):
        self.computeSetParallelComposition(self.succ, f, alpha)

    def reachableParallelMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           res=[]
           for s in self.succ:
               res=res+s.reachableParallelMerge(visited+[self.ident],depth+1)
           return res

    def reachableInclusiveMerge(self,visited,depth):
        return GatewaySplitState.reachableInclusiveMerge(self,visited,depth)


##
# Class for AllJoinState
# Attributes: none
class AllJoinState(GatewayMergeState):

    def __init__(self,ident,succ):
        IntermediateState.__init__(self,ident,succ)

    def lnt(self,f,alpha):
        # emit synchro message
        f.write(syncPrefix + self.ident + "; ")
        if (not isSynchroMerge(self.succ[0])):
            dumpSucc(f,alpha,self.succ,False)
        else:
            f.write(" null\n")

    def reachableParallelMerge(self,visited,depth):
        if isInList(self.ident,visited):
           return []
        else:
           return [(self.ident,depth)]+self.succ[0].reachableParallelMerge(visited+[self.ident],depth-1)

    def reachableInclusiveMerge(self,visited,depth):
        return GatewayMergeState.reachableInclusiveMerge(self,visited,depth)

##
# Class for Choreographies described with Intermediate Format
# Attributes: a name and a list of states
class Choreography:

    def __init__(self):
        self.name=""
        self.states=[]
        self.parallelMergeMap = {}

    def getStates(self):
        return self.states


    def simpleFaultyP(self):

        choices = filter(lambda state: isinstance(state, ChoiceState), self.states)

        for choice in choices:
            interactions = filter(lambda successor: isinstance(successor, InteractionState), choice.succ)
            if len(interactions) > 0:
                if not reduce(lambda x, y: x and (interactions[0].initiating == y.initiating), interactions, True):
                    return True
        return False

    """
    must be called after choreography construction
    will create cone sets for splits and sync sets for all states
    """
    def computeSyncSets(self, debug=False):

        # compute the set of edges
        initial = self.getInitialState()
        edgeSet = initial.getEdges(set())

        # computeConeSets
        for s in self.states:
            if isSynchroSelect(s): #isinstance (s, GatewaySplitState):
                coneSet = s.computeConeSet(edgeSet, self.states)
                s.setConeSet(coneSet)

        # compute sync sets for all states
        splitStates = filter (lambda x : isSynchroSelect(x), self.states)
        mergeStates = filter (lambda x : isSynchroMerge(x), self.states)

        for merge in mergeStates:
            merge.getSyncSet().add(merge)
            for split in splitStates:
                cone = split.getConeSet()
                for s in self.states:
                    if merge in cone and s in cone:
                        if successorNoMergeBetween(s, merge, edgeSet, set()):
                            s.getSyncSet().add(merge)

        if debug:
            for s in self.states:
                syncSet = s.getSyncSet()
                if len(syncSet) > 0:
                    print "state", s.ident, "syncs with"
                    for sync in syncSet:
                        print "-> ", sync.ident



    # predicate to test whether a split state is in the cone set
    # of another split state
    def splitInOtherSplitCone(self, splitState):
        if isinstance(splitState, AllSelectState):
            testSet = filter(lambda x : isinstance(x, AllSelectState) and splitState != x, self.states)
        elif isinstance(splitState, SubsetSelectState):
            testSet = filter(lambda x : isinstance(x, SubsetSelectState) and splitState != x, self.states)
        else:
            testSet = filter(lambda x : isSynchroSelect(x) and splitState != x, self.states)
        return reduce (lambda x, y : x or splitState in y.getConeSet(), testSet, False)

    def getInitialState (self):
        initialList = filter (lambda x: isinstance (x, InitialState), self.states)
        if (len (initialList) > 1):
            print "Potential Error: More than one initial state!"
        elif (len (initialList) == 0):
            print "Potential Error: no initial state!"
        else:
            return initialList[0]

    def getName(self):
        return self.name

    def printing(self):
        print "Choreography name: "+self.name
        for s in self.states:
            print s,
            print "",
            if not(isinstance(s,FinalState)):
               print s.getSucc()

    def addState(self,state):
        self.states.append(state)
        state.myChoreo = self

    def getPeers(self):
        peers=[]
        for s in self.states:
            peers=peers+s.getPeers()
        return removeDoubles(peers,peers)

    def alpha(self):
        alpha=[]
        lmergepar=[]
        for s in self.states:
            alpha=alpha+s.alpha()
            if isinstance(s,InitialState):
               lmergepar=s.reachableParallelMerge([],-1)
        # print lmerge
        lmergeincl=[]
        for s in self.states:
            alpha=alpha+s.alpha()
            if isinstance(s,InitialState):
               lmergeincl=s.reachableInclusiveMerge([],-1)
        lmerge=lmergepar+lmergeincl

        # we do not add all synchros to all prcesses any more
        # the necessary ones are computed in the syncSet attribute
        return removeDoubles(alpha,alpha)

    # Generates a few types (string, fifo) and functions
    def generateDatatypes(self,alpha,f):
        f.write("type Message is\n")
        dumpAlphabet(alpha,f,False)
        f.write("\n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type\n\n")
        f.write("type Buffer is list of Message\n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type\n\n")
        f.write("type BoundedBuffer is bbuffer (buffer: Buffer, bound: Nat)\n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type\n\n")
        f.write("function insert (m: Message, q: Buffer): Buffer is\n")
        f.write("         case q in\n")
        f.write("         var hd: Message, tl: Buffer in\n")
        f.write("             nil         -> return cons(m,nil)\n")
        f.write("           | cons(hd,tl) -> return insert(m,tl)\n")
        f.write("         end case\n")
        f.write("end function\n\n")
        f.write("function ishead (m: Message, q: Buffer): Bool is\n")
        f.write("         case q in\n")
        f.write("         var hd: Message, tl: Buffer in\n")
        f.write("             nil         -> return false\n")
        f.write("           | cons(hd,tl) -> return (m==hd)\n")
        f.write("         end case\n")
        f.write("end function\n\n")
        f.write("function remove (q: Buffer): Buffer is\n")
        f.write("         case q in\n")
        f.write("         var hd: Message, tl: Buffer in\n")
        f.write("             nil         -> return nil\n")
        f.write("           | cons(hd,tl) -> return tl\n")
        f.write("         end case\n")
        f.write("end function\n\n")
        f.write("function count (q: Buffer): Nat is\n")
        f.write("         case q in\n")
        f.write("         var hd: Message, tl: Buffer in\n")
        f.write("             nil         -> return 0\n")
        f.write("           | cons(hd,tl) -> return (1+count(tl))\n")
        f.write("         end case\n")
        f.write("end function\n\n")
        f.write("function bisfull (bq: BoundedBuffer): Bool is\n")
        f.write("  return ((count(bq.buffer))==bq.bound)\n")
        f.write("end function\n\n")
        f.write("function binsert (m: Message, bq: BoundedBuffer): BoundedBuffer is\n")
        f.write("  if bisfull(bq) then\n")
        f.write("     return bq\n")
        f.write("  else\n")
        f.write("     return bbuffer(insert(m,bq.buffer),bq.bound)\n")
        f.write("  end if\n")
        f.write("end function\n\n")
        f.write("function bishead (m: Message, bq: BoundedBuffer): Bool is\n")
        f.write("  return ishead(m,bq.buffer)\n")
        f.write("end function\n\n")
        f.write("function bremove (bq: BoundedBuffer): BoundedBuffer is\n")
        f.write("  return bbuffer(remove(bq.buffer),bq.bound)\n")
        f.write("end function\n\n")
        f.write("function bcount (bq: BoundedBuffer): Nat is\n")
        f.write("  return count(bq.buffer)\n")
        f.write("end function\n\n")


    # Computes the set of actions to be hidden for peer projection purposes
    #  -> we return actions in which the current peer is NOT involved
    def computeHidingActions(self,pid,alpha):
        res=[]
        for act in alpha:
            l3=act.split("_")
            if (l3[0]!=pid) and (l3[1]!=pid):
               res.append(act)
        return res

    # Computes the set of actions used by a specific peer
    def alphaPeer(self,pid,alpha):
        res=[]
        for act in alpha:
            l3=act.split("_")
            if (l3[0]==pid) or (l3[1]==pid):
               res.append(act)
        return res

    # Computes the set of actions used by a specific Buffer
    def alphaBuffer(self,pid,alpha):
        res=[]
        for act in alpha:
            l3=act.split("_")
            if (l3[1]==pid):
               res.append(act)
        return res


    # Adds one direction to an alphabet (string list)
    def alphaDir(self,pid,alpha):
        res=[]
        for act in alpha:
            l3=act.split("_")
            if (l3[0]==pid):
               aux=act # +"_EM"
               if not(aux in res):
                  res.append(aux)
            if (l3[1]==pid):
               aux=act+"_REC"
               if not(aux in res):
                  res.append(aux)
        return res

    # Adds systematically both directions to an alphabet (string list)
    def alphaBothDir(self,alpha):
        res=[]
        for act in alpha:
            aux=act # +"_EM"
            if not(aux in res):
               res.append(aux)
            aux=act+"_REC"
            if not(aux in res):
               res.append(aux)
        return res

    # Generates an LNT process for each peer (synchronous communication)
    def generatePeer(self,pid,alpha,f):
        hiding=self.computeHidingActions(pid,alpha)
        f.write("process peer_"+pid+" [")
        dumpAlphabet(alpha,f,True)
        f.write("] is\n")
        if (hiding!=[]):
           f.write(" hide ")
           dumpAlphabet(hiding,f,True)
           f.write(" in\n")
        f.write("   MAIN [")
        dumpAlphabet(alpha,f,False)
        f.write("]\n")
        if (hiding!=[]):
           f.write(" end hide\n")
        f.write("end process\n\n")

    # Generates an LNT process for each peer (asynchronous communication)
    def generateAPeer(self,pid,alpha,f):
        hiding=self.computeHidingActions(pid,alpha)
        alphap=self.alphaPeer(pid,alpha)
        alphapdir=self.alphaDir(pid,alphap)
        f.write("process apeer_"+pid+" [")
        dumpAlphabet(alphapdir,f,True)
        f.write("] is\n")
        f.write("  apeer_"+pid+"_aux [")
        dumpAlphabet(alphapdir,f,False)
        f.write("]\n")
        f.write("end process\n")
        f.write("process apeer_"+pid+"_aux [")
        dumpAlphabet(alphap,f,True)
        f.write("] is\n")
        if (hiding!=[]):
           f.write(" hide ")
           dumpAlphabet(hiding,f,True)
           f.write(" in\n")
        f.write("   MAIN [")
        dumpAlphabet(alpha,f,False)
        f.write("]\n")
        if (hiding!=[]):
           f.write(" end hide\n")
        f.write("end process\n\n")

    ##
    # Computes a synchronisation set given a peer and a list of peers
    def computeSynchro(self,pid,peers,alpha):
        alphap=self.alphaPeer(pid,alpha)
        alphas=[]
        for k in peers:
            ak=self.alphaPeer(k,alpha)
            alphas=union(alphas,ak)
        return intersection(alphap,alphas)


    ##
    # Generates an LNT process for the composition of peers (synchronous comm)
    def generateComposition(self,alpha,peers,f):
        f.write("process synchronous_composition")
        f.write(" [")
        dumpAlphabet(alpha,f,True)
        f.write("] is\n")
        i=0
        max=len(peers)
        for p in peers:
           if (i+1)<max:
              f.write("par ")
              # synchro set computation
              synchro=self.computeSynchro(p,peers[i+1:],alpha)
              if synchro!=[]:
                 dumpAlphabet(synchro,f,False)
                 f.write(" in")
              f.write("\n")
           f.write("peer_"+p)
           f.write(" [")
           dumpAlphabet(alpha,f,False)
           f.write("]\n")
           i=i+1
           if (i<max):
              f.write("||\n")
        i=1
        while (i<max):
           f.write("end par\n")
           i=i+1
        f.write("end process\n\n")


    ##
    # Generates an LNT process for a peer FIFO buffer
    def generateBuffer(self,pid,alpha,f):
            f.write("process ")
            pname="buffer_"+pid
            f.write(pname)
            alphab=self.alphaBuffer(pid,alpha)
            alphabdir=self.alphaBothDir(alphab)
            if (alphabdir!=[]):
               f.write(" [")
               dumpAlphabet(alphabdir,f,True)
               f.write("]")
            f.write(" (bq: BoundedBuffer) is\n ")
            f.write(" select\n")
            for m in alphab:
                f.write("    if not(bisfull(bq)) then ")
                f.write(m+" ; "+pname)
                f.write(" [")
                dumpAlphabet(alphabdir,f,False)
                f.write("]")
                f.write(" (binsert("+m+",bq)) else stop end if\n")
                f.write("    []\n")
                gaterec=m+"_REC"
                f.write("    if bishead("+m+",bq) then "+gaterec+" ; "+pname)
                f.write(" [")
                dumpAlphabet(alphabdir,f,False)
                f.write("]")
                f.write(" (bremove(bq)) else stop end if\n")
                f.write("    []\n")

            f.write("    null\n ")
            f.write(" end select\n ")

            f.write("end process\n\n")

    # Dumps alphabet with REC suffixing each message
    def dumpAlphabetWithREC(self,alpha,f,any):
        l=0
        max=len(alpha)
        for e in alpha:
            f.write(e)
            f.write("_REC")
            if any:
               f.write(":any")
            l=l+1
            if l<max:
               f.write(",")

    ##
    # Generates an LNT process for a couple (peer,buffer)
    def generatePeerBuffer(self,pid,alpha,f):
        f.write("process ")
        pname="peer_buffer_"+pid
        f.write(pname)
        alphap=self.alphaPeer(pid,alpha)
        alphapdir=self.alphaDir(pid,alphap)
        alphab=self.alphaBuffer(pid,alpha)
        alphabdir=self.alphaBothDir(alphab)
        f.write(" [")
        dumpAlphabet(union(alphabdir,alphapdir),f,True)
        f.write("] is\n")
        f.write("  par ")
        if alphab!=[]:
           self.dumpAlphabetWithREC(alphab,f,False)
           f.write(" in\n")
        f.write("    apeer_"+pid+" [")
        dumpAlphabet(alphapdir,f,False)
        f.write("]\n")
        f.write("    ||\n")
        f.write("    buffer_"+pid+" ")
        if (alphabdir!=[]):
           f.write("[")
           dumpAlphabet(alphabdir,f,False)
           f.write("] ")
        f.write("(bbuffer(nil,1))\n")
        f.write("  end par\n")
        f.write("end process\n\n")

    ##
    # Generates an LNT process for the composition of peers (asynchronous comm)
    def generateAsynchronousComposition(self,alpha,peers,f,hide):
        f.write("process asynchronous_composition")
        f.write(" [")
        alphabdir=self.alphaBothDir(alpha)
        dumpAlphabet(alphabdir,f,True)
        f.write("] is\n")
        i=0
        max=len(peers)
        for p in peers:
           if (i+1)<max:
              f.write("par ")
              # synchro set computation
              synchro=self.computeSynchro(p,peers[i+1:],alpha)
              if synchro!=[]:
                 dumpAlphabet(synchro,f,False)
                 f.write(" in")
              f.write("\n")
           if hide:
               alphap=self.alphaPeer(p,alpha)
               if alphap!=[]:
                    f.write("hide ")
                    self.dumpAlphabetWithREC(alphap,f,True)
                    f.write(" in\n")
           f.write("peer_buffer_"+p)
           alphap=self.alphaPeer(p,alpha)
           alphapdir=self.alphaDir(p,alphap)
           alphab=self.alphaBuffer(p,alpha)
           alphabdir=self.alphaBothDir(alphab)
           f.write(" [")
           dumpAlphabet(union(alphabdir,alphapdir),f,False)
           f.write("]\n")
           if hide:
              if alphap!=[]:
                 f.write("end hide\n")
           i=i+1
           if (i<max):
              f.write("||\n")
        i=1
        while (i<max):
           f.write("end par\n")
           i=i+1
        f.write("end process\n\n")


    # Generates an LNT module and process for a BPMN 2.0 choreography
    def genLNT(self,name=""):
        if name=="":
            filename=self.name+".lnt"
        else:
            filename=name+".lnt"
        f=open(filename, 'w')
        f.write("module "+self.name+" with \"get\" is\n\n")
        alpha=self.alpha()
        self.generateDatatypes(alpha,f)
        # the process MAIN corresponds to the initial state
        # otherwise, we generate one process per state
        for s in self.states:
            if isinstance(s,InitialState):
                f.write("process MAIN [")
                dumpAlphabet(alpha,f,True)
                f.write("] is\n")
                succ=s.getSucc()
                if (len(succ)>1):
                    print "Error: only one succesor expected here!"
                else:
                    # TODO we hide synchronization messages in split_XXX processes
                    hideSet = [] #map(lambda x : x.ident, succ[0].getSyncSet())
                    nb = len(hideSet)
                    if nb > 0:
                        f.write("hide ")
                        for msg in hideSet:
                            f.write(syncPrefix + msg + ":any")
                            if nb > 1:
                                f.write(",")
                            nb = nb - 1
                        f.write(" in\n")
                    dumpSucc(f, alpha, succ, False)
                    if len(hideSet) > 0:
                        f.write("end hide\n")
                f.write("end process\n")
            else:
                f.write("\nprocess "+s.getId())
                f.write(" [")
                dumpAlphabet(alpha,f,True)
                alphaSync = map(lambda x : x.ident, s.getSyncSet())

                # print "state ", s.ident, alpha, alphaSync
                dumpAlphabet(alphaSync, f, True, True, True)
                f.write("] is\n")
                s.lnt(f,alpha)
                f.write("end process\n")

        # generate the parallel composition for the various splits
        # they are called as "split_"+name processes

        # find those who are not in the cone set of another split
        splitGatewaySet = filter(lambda x : (isinstance(x, AllSelectState) or  isinstance(x, SubsetSelectState)) and not self.splitInOtherSplitCone(x), self.states)

        for p in splitGatewaySet:
            f.write("\nprocess split_" + p.ident + " [")
            dumpAlphabet(alpha, f, True)

	    if isinstance(p, AllSelectState):
                mergeGatewaySet = set(filter(lambda x : isinstance(x, AllJoinState) and x in p.getConeSet(), self.states)) | set([p])
            elif isinstance(p, SubsetSelectState):
                mergeGatewaySet = set(filter(lambda x : isinstance(x, SubsetJoinState) and x in p.getConeSet(), self.states)) | set([p])
            
            alphaSync = map(lambda x : x.ident, mergeGatewaySet)
            #dumpAlphabet(alphaSync, f, True, True, True)
            f.write("] is\n")


            # hide the synchronizations
            if len (alphaSync) > 0:
                f.write("hide ")
                dumpAlphabet(alphaSync, f, ":any",False,True)
                f.write(" in\n")
            
            p.computeSetParallelComposition(mergeGatewaySet, f, alpha)

            if len(alphaSync) > 0:
                f.write("end hide\n")
            f.write("end process\n\n")

        # generations of one process per peer (synchronous communication)
        f.write("\n\n (* peers (synchronous communication) *)\n\n")
        peers=self.getPeers()
        for p in peers:
            self.generatePeer(p,alpha,f)
        # for checking realizability (synchronous communication)
        f.write("\n\n (* composition of peers (synchronous communication) *)\n\n")
        self.generateComposition(alpha,peers,f)
        # generations of one process per peer (asynchronous communication)
        f.write("\n\n (* peers (asynchronous communication) *)\n\n")
        for p in peers:
            self.generateAPeer(p,alpha,f)
        # Generation of FIFO buffers for each peer
        for p in peers:
           self.generateBuffer(p,alpha,f)
        # Generation of couples (peer,buffer)
        for p in peers:
           self.generatePeerBuffer(p,alpha,f)
        f.write("\n\n (* composition of peers (asynchronous communication) *)\n\n")
        self.generateAsynchronousComposition(alpha,peers,f,True)

        f.write("\nend module\n")

        f.close()

    # Generates an SVL file
    def genSVL(self, smartReduction = True):
        filename=self.name+".svl"
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n") #\"% CADP_TIME=\"memtime\"\n\n")
        f.write ("% DEFAULT_PROCESS_FILE=" + self.name + ".lnt\n\n")
        # choreography generation (LTS)
        f.write("\"" + self.name + ".bcg\" = safety reduction of tau*.a reduction of branching reduction of \"MAIN [")
        alpha=self.alpha()
        dumpAlphabet(alpha,f,False)
        f.write("]\";\n\n")
        f.close()


    def buildChoreoFromFile(self, fileName, debug = False):
        # open xml document specified in fileName
        xml = file(fileName).read()
        try:
            choreo = choreo_xml_model.CreateFromDocument(xml)
            self.name = choreo.choreoID

            if debug:
                print "choreo name: ", self.name

            stateTab = [] # python encoded states
            queue = [] # states waiting to be encoded

            # init :
            # -> fill stateTab with final states
            # -> fill queue with any other state in order to sort them according to their dependancies
            for fin in choreo.stateMachine.final:
                if debug:
                    print "final state: ", fin.stateID
                stateTab.append(FinalState(fin.stateID))

            for inter in choreo.stateMachine.interaction:
                if debug:
                        print "interaction ID", inter.stateID
                for e in choreo.alphabet.message:
                    if debug:
                            print "  message ID:", e.msgID
                    # msgId is unique to each message in the choreography
                    if (e.msgID == inter.msgID):
                        msg = e.messageContent
                        peers = [e.sender, e.receiver]
                        initiator = e.sender
                        queue.append(['interaction', inter.stateID, inter.successors, peers, initiator, [MessageFlow(msg)]])

            for ch in choreo.stateMachine.choice:
                if debug:
                    print "choice state: ", ch.stateID
                queue.append(['choice', ch.stateID, ch.successors])
            for domch in choreo.stateMachine.dominatedChoice:
                if debug:
                    print "dominated choide state: ", domch.stateID
                queue.append(['dominated', domch.stateID, domch.successors, domch.dominantPeer])
            for simple in choreo.stateMachine.simpleJoin:
                if debug:
                    print "simple join: ", simple.stateID
                queue.append(['simpleJoin', simple.stateID, simple.successors])
            for sub in choreo.stateMachine.subsetSelect:
                if debug:
                    print "simple join: ", sub.stateID
                queue.append(['subsetSelect', sub.stateID, sub.successors, sub.default])
            for join in choreo.stateMachine.subsetJoin:
                if debug:
                    print "subset join: ", join.stateID
                queue.append(['subsetJoin', join.stateID, join.successors])
            for alls in choreo.stateMachine.allSelect:
                if debug:
                    print "all select: ", alls.stateID
                queue.append(['allSelect', alls.stateID, alls.successors])
            for allj in choreo.stateMachine.allJoin:
                if debug:
                    print "all join: ", allj.stateID
                queue.append(['allJoin', allj.stateID, allj.successors])
            init = choreo.stateMachine.initial
            if debug:
                print "initial state: ", init.stateID
            queue.append(['initial', init.stateID, init.successors])

            if debug:
                print "\nqueue: length ", len(queue), queue
                print map(lambda x: x[1], queue)


            # # make states in stateTab unique
            # uniqStateTab = []
            # for s in stateTab:
            #     if not s.ident in map(lambda x: x.ident, uniqStateTab):
            #         uniqStateTab.append(s)

            # main body

            # create all states
            successors = []
            for elem in queue:
                if (elem[0] == 'interaction'):
                    stateTab.append(InteractionState(elem[1], [], elem[3], elem[4], elem[5]))
                elif (elem[0] == 'choice'):
                    stateTab.append(ChoiceState(elem[1], []))
                elif (elem[0] == 'dominated'):
                    stateTab.append(DominatedChoiceState(elem[1], [], elem[3]))
                elif (elem[0] == 'simpleJoin'):
                    stateTab.append(SimpleJoinState(elem[1], []))
                elif (elem[0] == 'subsetSelect'):
                    stateTab.append(SubsetSelectState(elem[1], [])) #, elem[3]))
                elif (elem[0] == 'subsetJoin'):
                    stateTab.append(SubsetJoinState(elem[1], []))
                elif (elem[0] == 'allSelect'):
                    stateTab.append(AllSelectState(elem[1], []))
                elif (elem[0] == 'allJoin'):
                    stateTab.append(AllJoinState(elem[1], []))
                elif (elem[0] == 'initial'):
                    stateTab.append(InitialState(elem[1], []))

            if debug:
                print "\nstateTab: length ", len(stateTab), stateTab
                print map(lambda x: x.ident, stateTab)



            # add successors to states
            for elem in queue:
                stateList = filter(lambda x: x.ident == elem[1], stateTab)
                if len(stateList) > 1:
                    print "more than one state with same ID found!"
                state = stateList[0]
                successorList = elem[2]
                succStates = filter(lambda x:  x.ident in successorList, stateTab)
                if debug:
                    print "state", state.ident, "has successors", map(lambda x: x.ident, succStates)
                map(lambda succ: state.addSucc(succ), succStates)


            # queuelen = 0
            # while (len(queue) != queuelen):
            #     print queue
            #     # if no element is removed -> terminate, else infinite loop!
            #     queuelen = len(queue)
            #     for elem in queue:
            #         successors = []
            #                  # TODO: did not work with initial state. Why?
            #                  # hm, sounds like a predicate but has surprising side-effects ...
            #         if (hasSuccInList(elem[2], stateTab, successors)):
            #             # add to stateTab list
            #             if (elem[0] == 'interaction'):
            #                 stateTab.append(InteractionState(elem[1], successors, elem[3], elem[4], elem[5]))
            #             elif (elem[0] == 'choice'):
            #                 stateTab.append(ChoiceState(elem[1], successors))
            #             elif (elem[0] == 'dominated'):
            #                 stateTab.append(DominatedChoiceState(elem[1], successors, elem[3]))
            #             elif (elem[0] == 'simpleJoin'):
            #                 stateTab.append(SimpleJoinState(elem[1], successors))
            #             elif (elem[0] == 'subsetSelect'):
            #                 stateTab.append(SubsetSelectState(elem[1], successors)) #, elem[3]))
            #             elif (elem[0] == 'subsetJoin'):
            #                 stateTab.append(SubsetJoinState(elem[1], successors))
            #             elif (elem[0] == 'allSelect'):
            #                 stateTab.append(AllSelectState(elem[1], successors))
            #             elif (elem[0] == 'allJoin'):
            #                 stateTab.append(AllJoinState(elem[1], successors))
            #             elif (elem[0] == 'initial'):
            #                 stateTab.append(InitialState(elem[1], successors))
            #             # remove from working queue
            #             queue.remove(elem)


            # dumps resulting tab
            if debug:
                for e in stateTab:
                    print '####'
                    print e.getId()
                    if (not isinstance(e, FinalState)):
                        print 'successors :'
                        for succ in e.getSucc():
                            print succ.getId()
                    print '####'

            # add all states in the choreography
            for element in stateTab:
                self.addState(element)

        except pyxb.UnrecognizedContentError, e:
            print 'An error occured while parsing xml document ' + fileName
#            print 'Unrecognized element "%s" at %s' % (e.content.expanded_name, e.content.location)
            print 'Unrecognized element, the message was "%s"' % (e.message)

class Checker:


    def generateLTS(self, choreo, debugOutput = False):
        import sys
        name = choreo.getName()
        
        if debugOutput:
             process = Popen (["svl",name], shell = False, stdout=sys.stdout)
	     #process = Popen (["svl",name], shell = False, stdout=PIPE)
        else:
            process = Popen (["svl",name], shell = False, stdin=PIPE, stdout=PIPE, stderr=PIPE)
            #process = Popen (["svl",name], shell = False, stdin=PIPE, stdout=PIPE)
        process.communicate()
            
        if process.returncode != 0:
            return False
        else:
            return True


    def checkChoreo(self, choreo, smartReduction = True, debugInfoMonitors = False):

        choreoName = choreo.getName()
        initial = choreo.getInitialState()
        conditions = initial.checkConditionsFromSpec("", [], [], True)
        
        choreo.genLNT()
        choreo.genSVL(smartReduction)
        self.generateLTS(choreo)


##############################################################################################
if __name__ == '__main__':

    import sys
    import choreo_xml_model
    import pyxb

    import os
    import glob

    checker = Checker()

    path = 'TMP/'

#    for infile in glob.glob( os.path.join(path, '*.xml') ):
#    for infile in glob.glob( os.path.join(path, 'online*v1.xml') ):
#    for infile in ['test/c0003a.xml', 'test/c0004ter.xml', 'test/c0004bis.xml']:
#    for infile in ['test/TravelAgency.xml']:

    infile=sys.argv[1]

    print "current file is: " + infile
    choreo = Choreography()
    choreo.buildChoreoFromFile(infile, True)
    choreo.computeSyncSets(True)
    # print choreo.name, "is faulty: ", choreo.simpleFaultyP()
    checker.checkChoreo(choreo)
