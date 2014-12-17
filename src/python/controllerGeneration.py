#
#
# Name:   cp2lnt.py - Classes for conversation protocols
# Author: Gwen Sala\"un.
# Date:   07-9-2010
# changes for projection
# Matthias Guedemann
# Start Date: 21-11-2011
##
################################################################################
# from itacalib.misc import lists
#import lists

from time import time

from subprocess import *
import os.path

tmp="tmp/"

# queue bound (constant)
# qbound=1

##
# Labels in conversation protocols tuple
#   (sender, receiver, message)
class Label:
    def __init__(self,sender,receiver,message):

        self._sender=sender
        self._receiver=receiver
        self._message=message.lower()

    def getSender(self):
        return self._sender

    def getMessage(self):
        return self._message

    def getReceiver(self):
        return self._receiver

    def printLabel(self):
        print " - (",
        self._sender.printPeer()
        print ",",
        self._receiver.printPeer()
        print ",",
        print self._message,
        print ") -> ",

##
# State class
class State:

    def __init__(self,name, interleaving = False):
        self._name=name
        self.interleavingFlag = interleaving

    def getName(self):
        return self._name

    def printState(self):
        print self._name,

    # will signal whether the state is part of a full interleaving
    def getInterleavingFlag(self):
        return self.interleavingFlag

    def setInterleavingFlag(self, flag):
        self.interleavingFlag = flag

##
# Peer class
class Peer:
    def __init__(self,name):
        self._name=name.lower()

    def getName(self):
        return self._name

    def printPeer(self):
        print self._name,

##
# Transition class -> source peer + label + target peer.
class Transition:
    def __init__(self,src,label,tgt):
        self._src=src
        self._tgt=tgt
        self._label=label

    ##
    # Returns the transition's source peer.
    # @return source peer name.
    # @defreturn String.
    def getSource(self):
        return self._src

    ##
    # Returns the transition's target peer.
    # @return target peer name.
    # @defreturn String.
    def getTarget(self):
        return self._tgt

    ##
    # Returns the transition's label.
    # @return transition label name.
    # @defreturn String.
    def getLabel(self):
        return self._label

    ##
    # Sets the transition's label.
    # @return transition label name.
    # @defreturn String.
    def setLabel(self,label):
        self._label=label

    def printTransition(self):
        self._src.printState()
        self._label.printLabel()
        self._tgt.printState()


# synchronization transition for monitors
# these are treated specially
# added 11.7. now synchronisation transitions can contain
#             multiple synchronisation messages
#             the problem is if a divergent choice is after synchronisation
#             this will now generate all possible interleavings of the syncs
#             the messages are stored as tuple (original, label)
class SyncTransition(Transition):

    def __init__ (self, src, label, tgt, original):
        Transition.__init__(self, src, Label(label.getSender(), label.getReceiver(), "synchro"), tgt)

        self.liste = []
        self.addSyncTrans(original, label)
        self.originalMessage = original

    def getOriginal(self):
        return self.originalMessage

    def getLabelSet(self):
        return map(lambda(orig, label): label, self.liste)

    def getTupelSet(self):
        return self.liste

    # will generate the necessary parallel composition of sync messages
    def generateSyncs(self, f, withInit = False, pid = False):

        if not pid:
            relevantLabels = self.getLabelSet()
        else:
            relevantLabels = filter(lambda label: label.getSender().getName() == pid, self.getLabelSet())
        i = len(relevantLabels)

        if i > 1:
            f.write("  par\n")
        for j in range(0, i):
            label = relevantLabels[j]
            if withInit:
                f.write("init_")
            f.write(label.getSender().getName() + "_" +
                    label.getReceiver().getName() + "_" +
                    label.getMessage() + "\n")
            if (j + 1 < i):
                f.write(" ||\n")

        if i > 1:
            f.write ("  end par;\n")
        else:
            f.write(";\n")

    def printTransition(self):
        self._src.printState()
        i = len(self.getLabelSet())
        print "- (\n",
        for j in range(0,i):
            print "  ",
            (orig, label) = self.getTupelSet()[j]
            print label.getSender().getName() + ", sync" + orig, ", ", label.getReceiver().getName(),
            if j + 1 < i:
                print "\n",
        print "\n) ->",
        self._tgt.printState()

    def addSyncTrans(self, original, label):
        self.liste.append((original, label))

##
# Conversation protocol class.
class CP:
    def __init__(self,name,init):
        self._name = name
        self._s0 = init
        self._S = []
        self._T = []

    def getName (self):
        return self._name

    def getInitialState(self):
        return self._s0

    def setInitialState (self, initialState):
        self._s0 = initialState

    def addState(self, state):
        if not state.getName() in map(lambda s: s.getName(), self._S):
            self._S.append(state)

    def addTransition(self,transition):
        self._T.append(transition)

    def removeTransition (self, transition):
        self._T.remove (transition)

    # checks whether there is a non-deterministic choice
    # between senders, i.e. a state where two different peers
    # want to send
    def checkNonDeterministicSenderChoice(self):
        for state in self._S:
            outgoingTransitions = filter (lambda trans: trans.getSource().getName() == state.getName(), self._T)
            senderPeers = set (map (lambda trans: trans.getLabel().getSender(), outgoingTransitions))
            if len (senderPeers) > 1:
                print "Error in Choreography", self._name, "! The state ", state.getName(), "has multiple sending peers"
                return False
        return True

    def checkAllConditions(self):
        # FIXME, this is temporary!
        #return True or self.checkNonDeterministicSenderChoice()
        return self.checkNonDeterministicSenderChoice()

    # return all the predecessors of the given state
    def getPredecessors(self, state):
        transToMe = filter (lambda trans: state.getName() == trans.getTarget().getName(), self._T)

        predecessors = []
        for predecessor in map (lambda trans: trans.getSource(), transToMe):
            if predecessor not in predecessors:
                predecessors.append (predecessor)

        return predecessors

    # creates new transition in the choreography
    # source and target must exist
    def createLabel (self, sourceName, targetName, label):

        peers = self.getPeers()

        names = map (lambda x: x._name, peers)
        if sourceName not in names or targetName not in names:
            print "source or target peer does not exist"
        else:
            return (Label (filter (lambda x: x.getName() == sourceName, peers)[0],
                           filter (lambda x: x.getName() == targetName, peers)[0],
                           label))

    #
    # returns a list of transitions with
    # the same label as offendingLabel
    # (these are the potential transtions that have to be synchronized
    #
    def findOffendingTransitions (self, offendingLabel):
        return (filter (lambda trans:
                        trans.getLabel().getSender()._name == offendingLabel.getSender()._name and
                        trans.getLabel().getReceiver()._name == offendingLabel.getReceiver()._name and
                        trans.getLabel().getMessage () == offendingLabel.getMessage(),
                        self._T))

    # inserts a synchronization label for the offending transition
    #
    def insertSynchroMessage (self, offendingTransition):

        synchroState = offendingTransition.getSource()
        offendingPeer = offendingTransition.getLabel().getSender()

        print "synchronization state is ", synchroState.getName(), " offending peer is ", offendingPeer.getName()

        synchroState.printState()

        newState = State (synchroState.getName() + "_prime")
        stateNr = 0

        self.addState(newState)


        if synchroState.getName() == self.getInitialState().getName():
            print "synchronization state is also initial state!\nThe new state will be set as initial"
            self.setInitialState(newState)

        newTrans = []
        changedTrans = []

        addedSyncTrans = False # check if at least a sync transition was added to the

        # get all transitions entering the synchronization state where:
        # - the offending peer is not the sender and
        # - the offending peer is not the receiver
        targetTrans = filter (lambda trans:
                                trans.getTarget ().getName() == synchroState.getName() and
                                trans.getLabel().getSender().getName() != offendingPeer.getName() and
                                trans.getLabel().getReceiver().getName() != offendingPeer.getName() and
                                not isinstance(trans, SyncTransition),
                                self.getAllTransitions())

        # select from targetTrans all transitions with unique workflow
        singleWorkFlowTrans = filter(lambda trans:
                                         not (trans.getTarget().getInterleavingFlag() and
                                              trans.getSource().getInterleavingFlag()),
                                         targetTrans)

        # select from targetTrans all transitions which are part of an interleaving
        # TODO: extend for possibility to have different interleavings, i.e., flags + interleavingID
        multipleWorkFlowTrans = filter(lambda trans:
                                           trans.getTarget().getInterleavingFlag() and
                                           trans.getSource().getInterleavingFlag(),
                                           targetTrans)

        # get all synchronization transitions to the synchroniztion state
        syncTransToExtend = filter(lambda trans:
                                    isinstance(trans, SyncTransition) and
                                    trans.getTarget().getName() == synchroState.getName(),
                                    #not offendingPeer.getName() in map(lambda l: l.getReceiver(),trans.getLabelSet()),
                                    self.getAllTransitions())

        print "incoming to synchro state: ", map (lambda trans: trans.printTransition (), targetTrans)
        print "incoming syncs: ", map (lambda trans: trans.printTransition (), syncTransToExtend)

        # save old transitions to synchro state to change later
        # but: do not touch the sync transitions!
        old_trans = self.getTransitions(synchroState.getName())


        # this will create a single new state and
        # a synchronization transition with potentially multiple senders
        for trans in multipleWorkFlowTrans:
            newLabel = Label (trans.getLabel().getSender(),
                                offendingPeer,
                                "sync" + trans.getSource().getName()+trans.getTarget().getName()+
                                offendingTransition.getLabel().getMessage())
            labels = map (lambda trans: trans.getLabel(), newTrans)
            labelNames = map (lambda label: label.getSender().getName() + "_" + label.getReceiver().getName() + "_" + label.getMessage(), labels)
            print labelNames
            if not newLabel.getSender().getName() + "_" + newLabel.getReceiver().getName() + "_" + newLabel.getMessage() in labelNames:
                specificSyncTrans = filter(lambda t: t.getSource().getName() == synchroState.getName() and
                                            t.getTarget().getName() == newState.getName(),
                                            newTrans)
                if reduce (lambda flagged, x: flagged or x,
                           map(lambda trans: trans.getTarget().getInterleavingFlag(), old_trans), False):
                    newState.setInterleavingFlag(True)
                if len(specificSyncTrans) > 0:
                    print "adding new sync possibility to existing trans"
                    if len(specificSyncTrans) > 1:
                        print "very strange ... should be inspected!"
                    specificSyncTrans[0].addSyncTrans(offendingTransition.getLabel().getMessage(), newLabel)
                else:
                    newTrans.append(SyncTransition (synchroState, newLabel, newState, offendingTransition.getLabel().getMessage()))
                changedTrans.append (trans)
            else:
                print "not adding label twice"

        #adjust all transitions from synchroState where offending peer is not the sender
        oldTrans = filter(lambda t: #not isinstance(t,SyncTransition)  # really no sync Transitions? or only last added ... ?
                          t.getLabel().getSender().getName() != offendingPeer.getName()
                          and t.getSource().getName() == synchroState.getName(),
                          self._T)

        # this will create multiple new states and
        # synchronization transitions with single sender
        for trans in singleWorkFlowTrans:

            newLabel = Label (trans.getLabel().getSender(),
                                offendingPeer,
                                "sync" + trans.getSource().getName()+trans.getTarget().getName()+
                                offendingTransition.getLabel().getMessage())
            labels = map (lambda trans: trans.getLabel(), newTrans)
            labelNames = map (lambda label: label.getSender().getName() + "_" + label.getReceiver().getName() + "_" + label.getMessage(), labels)
            print labelNames
            if not newLabel.getSender().getName() + "_" + newLabel.getReceiver().getName() + "_" + newLabel.getMessage() in labelNames:
                newState = State (synchroState.getName() + "_prime_" + str(stateNr))
                stateNr = stateNr + 1
                self.addState(newState)

                newTrans.append(SyncTransition (newState, newLabel, synchroState, offendingTransition.getLabel().getMessage()))
                self.removeTransition(trans)

                adjustedTrans = Transition(trans.getSource(), trans.getLabel(), newState)
                self.addTransition(adjustedTrans)
                changedTrans.append (adjustedTrans)

            else:
                print "not adding label twice"

        # add new sync possibilities to existing sync transitions
        for trans in syncTransToExtend:
            offendingMessage = offendingTransition.getLabel().getMessage()
            senderSet = set(map(lambda l: l.getSender(), trans.getLabelSet()))
            for sender in senderSet:
                newLabel = Label (sender, #trans.getLabel().getSender(),
                                    offendingPeer,
                                    "sync" + trans.getSource().getName()+trans.getTarget().getName()+
                                    offendingMessage)
                trans.addSyncTrans(offendingMessage, newLabel)
                addedSyncTrans = True

        # either we add sync trans to existing ones
        # or we introduce new sync transitions and new states!
        if len(multipleWorkFlowTrans) > 0:
            # if not addedSyncTrans:
                # adjust old transitions, remove unnecessary ones and introduce new ones from the new state
            for trans in old_trans:
                self.removeTransition (trans)
                targetState = trans.getTarget()
                if trans.getTarget().getName() == synchroState.getName():
                    print "\nself-loop at synchronization state -> using new State ", newState.getName(), " instead"
                    targetState = newState
                newTrans.append (Transition (newState,
                                             trans.getLabel(),
                                             targetState))

        # if any of the new labels was present as synchronization in the old choreo,
        # then we are in a (currently) non-fixable loop -> stop choreo extension
        filterFunc = (lambda label:
                      label.getSender().getName() + "_" + label.getReceiver().getName() + "_" + label.getMessage())
        newlabels = map (filterFunc, map (lambda trans: trans.getLabel(), newTrans))
        oldlabels = map (filterFunc, map (lambda trans: trans.getLabel(), self._T))

        if False: #reduce (lambda x, label: x or label in oldlabels, newlabels, False):
            print "old labels: ", oldlabels
            print "new labels: ", newlabels
            print "we are entering cyclic behavior here, this choreo is currently not fixable\n"
            return False
        elif len (changedTrans) == 0 and not addedSyncTrans:
            print "no fixable transition was found -> cannot fix choreography"
            return False
        else:
            map (lambda trans: self.addTransition(trans), newTrans)
            return True


    def printCP(self):
        print "-------"
        print self._name+" ( init:",
        self._s0.printState()
        print ")"
        for t in self._T:
            print "(",
            t.printTransition()
            print ")"
        print "-------"

    # Builds CP alphabet without a peer
    def buildAlphabetExcludingPeer(self,pid, skipSyncs):
        alpha=[]
        for t in self._T:
            # if skipSyncs does not hold, then all sync Transitions will
            # be generated in the alphabet!
            # the idea is that these are not part of the alphabet of the
            # peer, and therefore must be hidden in the aux peer

            if not isinstance(t, SyncTransition):
                sender= t.getLabel().getSender().getName()
                receiver= t.getLabel().getReceiver().getName()
                if (not(pid==sender) and not(pid==receiver)):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
            elif (not skipSyncs and isinstance (t, SyncTransition)):
                resultList = t.getLabelSet()
#                if pid != t.getLabel().getSender().getName() and not (pid in map(lambda l: l.getReceiver().getName(), resultList)):
                for l in resultList:
                    res = l.getSender().getName() + "_" + l.getReceiver().getName()+"_" + l.getMessage()
                    if not(res in alpha):
                        alpha.append(res)
        return alpha

    # Builds CP alphabet for a precise peer
    def buildAlphabetIncludingPeer(self,pid, skipSyncs = True):
        alpha=[]
        for t in self._T:
            if isinstance (t, SyncTransition) and skipSyncs:
                continue
            if isinstance(t, SyncTransition):
                resultList = t.getLabelSet()
                for l in resultList:
                    if pid == l.getSender().getName() or pid == l.getReceiver().getName():
                        res = l.getSender().getName() + "_" + l.getReceiver().getName()+"_" + l.getMessage()
                        if not(res in alpha):
                            alpha.append(res)
            else:
                sender=t.getLabel().getSender().getName()
                receiver=t.getLabel().getReceiver().getName()
                if (pid==sender) or (pid==receiver):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
        return alpha

    # Builds CP alphabet for a precise peer (only when pid is the receiver)
    def buildAlphabetIncludingPeerREC(self,pid, skipSyncs = True):
        alpha=[]
        for t in self._T:
            if isinstance (t, SyncTransition) and skipSyncs:
                continue
            if isinstance(t, SyncTransition):
                resultList = t.getLabelSet()
                if pid in map(lambda l: l.getReceiver().getName(), resultList):
                    for l in resultList:
                        res = sender + "_" + receiver+"_" + l.getMessage()
                        if not(res in alpha):
                            alpha.append(res)
            else:
                sender=t.getLabel().getSender().getName()
                receiver=t.getLabel().getReceiver().getName()
                if (pid==receiver):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
        return alpha

    # Builds CP alphabet for a precise peer + keeps directions (!/?)
    def buildAlphabetIncludingPeerDir(self,pid):
        alpha=[]
        for t in self._T:
            if isinstance (t, SyncTransition):
                continue
            sender=t.getLabel().getSender().getName()
            receiver=t.getLabel().getReceiver().getName()
            if (pid==sender):
                res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_EM"
                if not(res in alpha):
                    alpha.append(res)
            if (pid==receiver):
                res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_REC"
                if not(res in alpha):
                    alpha.append(res)
        return alpha

    # Builds alphabet for a precise peer: keeps only receptions (with direction)
    #  and choice actions ! TODO: a completer !!
    def buildAlphabetChoiceRec(self,pid):
        alpha=[]
        for t in self._T:
            sender=t.getLabel().getSender().getName()
            receiver=t.getLabel().getReceiver().getName()
            if (pid==receiver):
               res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_REC"
               if not(res in alpha):
                  alpha.append(res)
        return alpha

    # Builds alphabet for a precise peer: keeps only emissions (with direction)
    def buildAlphabetEm(self,pid):
        alpha=[]
        for t in self._T:
            if isinstance (t, SyncTransition):
                continue
            sender=t.getLabel().getSender().getName()
            receiver=t.getLabel().getReceiver().getName()
            if (pid==sender):
               res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_EM"
               if not(res in alpha):
                  alpha.append(res)
        return alpha


    # Builds peer alphabet for a precise peer + keeps receptions
    def buildAlphabetIncludingPeer_PQ(self,pid, skipSyncs = True):
        alpha=[]
        for t in self._T:
            if isinstance (t, SyncTransition) and skipSyncs:
                continue
            if isinstance(t, SyncTransition):
                resultList = t.getLabelSet()
                if pid == t.getLabel().getSender().getName() or pid in map(lambda l: l.getReceiver().getName(), resultList):
                    for l in resultList:
                        res = sender + "_" + receiver+"_" + l.getMessage()
                        if not(res in alpha):
                            alpha.append(res)
            else:
                sender=t.getLabel().getSender().getName()
                receiver=t.getLabel().getReceiver().getName()
                if (pid==sender):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
                if (pid==receiver):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_REC"
                    if not(res in alpha):
                        alpha.append(res)
        return alpha

    # Builds peer alphabet for a precise peer + keeps receptions
    def buildAlphabetIncludingPeer_PQbis(self,pid, skipSyncs = True):
        alpha=[]
        for t in self._T:
            sender=t.getLabel().getSender().getName()
            receiver=t.getLabel().getReceiver().getName()
            if isinstance (t, SyncTransition) and skipSyncs:
                continue
            elif isinstance (t, SyncTransition):
                resultList = t.getLabelSet()
                if pid in map(lambda l: l.getSender().getName(), t.getLabelSet()) or pid in map(lambda l: l.getReceiver().getName(), resultList):
                    for l in resultList:
                        sender = l.getSender().getName()
                        receiver = l.getReceiver().getName()
                        if pid == sender or pid == receiver:
                            res = sender + "_" + receiver+"_" + l.getMessage()
                            if not(res in alpha):
                                alpha.append(res)
            else:
                if (pid==sender):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
                if (pid==receiver):
                    res=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_REC"
                    res2=sender+"_"+receiver+"_"+t.getLabel().getMessage()
                    if not(res in alpha):
                        alpha.append(res)
                    if not(res2 in alpha):
                        alpha.append(res2)
        return alpha

    # 26/11/2011: Meriem
    # Builds alphabet for a monitor given its peer pid
    # we only need to extract the set of send messages and split them into reception (of that message by the monitor from its peer)
    # and an emission (of the same message from the monitor to the queue of the receiver peer)
    # @TODO: how to deal with the additional synchronization messages? these are detected from the extended choreography
    def buildAlphabetMonitor(self, pid, renameEmit = False, includeSyncInits = True):

        syncTransitions = filter (lambda trans:
                                  isinstance (trans, SyncTransition) and
                                  len (filter (lambda label: pid == label.getSender().getName() or
                                       pid == label.getReceiver().getName (),
                                       trans.getLabelSet())) > 0,
                                   self._T)

        sendTransitions = filter (lambda trans:
                                  pid == trans.getLabel().getSender().getName() and
                                  not isinstance (trans, SyncTransition),
                                  self._T)

        syncTransForPeer = filter (lambda trans:
                                   isinstance (trans, SyncTransition) and
                                   len(filter(lambda label: pid == label.getSender().getName(), trans.getLabelSet())) > 0,
                                   self._T)

        alpha=[]
        for t in syncTransitions:
            # first check if the transition is a synchronization transition
            # the synchronisation message to be added is of the format sync_label_src_tgt
            # 2/12/11 changed format to sender_receiver_SYNC_message

            # 11/7/12 now multiple sync messages in one synctrans possible!
            resultList = t.getLabelSet()
            for l in resultList:
                sender = l.getSender().getName()
                receiver = l.getReceiver().getName()
                if pid == sender or pid == receiver:
                    res = sender + "_" + receiver+"_" + l.getMessage()
                    if not(res in alpha):
                        alpha.append(res)
            # sender = t.getLabel().getSender().getName()
            # receiver = t.getLabel().getReceiver().getName()
            # res = sender +  "_" + receiver + "_" + t.getLabel().getMessage()
            # if not(res in alpha):
            #     alpha.append(res)

        for t in sendTransitions:
            sender = t.getLabel().getSender().getName()
            receiver = t.getLabel().getReceiver().getName()

            # split send message a one peer into a reception a emission to be in the monitor
            res1=sender+"_"+receiver+"_"+t.getLabel().getMessage()
            if not(res1 in alpha):
                alpha.append(res1)

            if renameEmit:
                res2=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_EM"
            else:
                res2=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_DLY"
            if not(res2 in alpha):
                alpha.append(res2)

        if includeSyncInits:
            for t in syncTransForPeer:
                for label in t.getLabelSet():
                    sender = label.getSender().getName()
                    if sender == pid:
                        receiver = label.getReceiver().getName()
                        res1 = "init_" + sender+"_"+receiver+"_"+label.getMessage()
                        if not(res1 in alpha):
                            alpha.append(res1)

        return alpha

    # builds alphabet for monitored peer a.k.a monitor || peer
    def buildAlphabetMonitored(self, pid, renameEmit = False, renameReceive = False):

        syncTransitions = filter (lambda trans:
                                  isinstance (trans, SyncTransition) and
                                  len (filter (lambda label: pid == label.getSender().getName() or
                                       pid == label.getReceiver().getName (),
                                       trans.getLabelSet())) > 0,
                                   self._T)

        sendTransitions = filter (lambda trans:
                                  pid == trans.getLabel().getSender().getName() and
                                  not isinstance (trans, SyncTransition),
                                  self._T)

        receiveTransitions = filter (lambda trans:
                                     pid == trans.getLabel().getReceiver().getName() and
                                     not isinstance (trans, SyncTransition),
                                     self._T)

        alpha = []
        for t in syncTransitions:
            # 7/11/12 set of synctrans
            resultList = t.getLabelSet()
            for l in resultList:
                sender = l.getSender().getName()
                receiver = l.getReceiver().getName()
                if pid == sender or pid == receiver:
                    res = sender + "_" + receiver+"_" + l.getMessage()
                    if not(res in alpha):
                        alpha.append(res)
            # sender = t.getLabel().getSender().getName()
            # receiver = t.getLabel().getReceiver().getName()
            # res = sender +  "_" + receiver + "_" + t.getLabel().getMessage()
            # if not(res in alpha):
            #     alpha.append(res)

        for t in sendTransitions:
            sender = t.getLabel().getSender().getName()
            receiver = t.getLabel().getReceiver().getName()

            # split send message a one peer into a reception a emission to be in the monitor
            res1=sender+"_"+receiver+"_"+t.getLabel().getMessage()
            if not(res1 in alpha):
                alpha.append(res1)

        for t in receiveTransitions:
            sender = t.getLabel().getSender().getName()
            receiver = t.getLabel().getReceiver().getName()

            # split send message a one peer into a reception a emission to be in the monitor
            res1=sender+"_"+receiver+"_"+t.getLabel().getMessage()
            if renameReceive:
                res1 = res1 + "_REC"
            if not(res1 in alpha):
                alpha.append(res1)

        return sorted (alpha)

    # alphabet for all synchronization process
    # onlyInits trigger the creation of only "init_" messages
    #    this is important for the parallel composition of the syncP processes
    def buildAlphabetSyncProcesses(self, pid, onlyInits = False):
        syncTransForPeer = filter (lambda trans:
                                   isinstance (trans, SyncTransition) and
                                   pid in map(lambda l: l.getSender().getName(), trans.getLabelSet()),
                                   self._T)
        messages = []
        for trans in syncTransForPeer:
            for label in trans.getLabelSet():
                if label.getSender().getName() == pid:
                    messages.append(pid + "_" + label.getReceiver().getName() + "_" + label.getMessage())
        # messages = set (map (lambda trans:
        #                      pid + "_" + trans.getLabel().getReceiver().getName() + "_" + trans.getLabel().getMessage(),
        #                      syncTransForPeer))

        initMessages = map (lambda msg: "init_" + msg, messages)

        if onlyInits:
            return initMessages
        else:
            return messages + initMessages


    # build synchronization alphabet for peer / monitor
    def buildSynchroSetPeerMonitor(self, pid):

        alpha = []

        sendTransitions = filter (lambda trans:
                                  pid == trans.getLabel().getSender().getName() and
                                  not isinstance (trans, SyncTransition),
                                  self._T)
        # synchronization is done over the messages emitted from the peer
        # these are received and cached by the monitor
        for t in sendTransitions:
            sender = t.getLabel().getSender().getName()
            receiver = t.getLabel().getReceiver().getName()

            res2=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_EM"
            if not(res2 in alpha):
                alpha.append(res2)

        return alpha

    def buildHideSetPeerMonitor(self, pid):
        return self.buildSynchroSetPeerMonitor(pid)

    # Builds alphabet for the couple (peer,queue)
    def buildAlphabetPeerQueue(self,pid):
        alpha=[]
        for t in self._T:
            sender=t.getLabel().getSender().getName()
            receiver=t.getLabel().getReceiver().getName()
            if (pid==sender):
               res=sender+"_"+receiver+"_"+t.getLabel().getMessage()
               if not(res in alpha):
                  alpha.append(res)
            if (pid==receiver):
               res1=sender+"_"+receiver+"_"+t.getLabel().getMessage()
               res2=sender+"_"+receiver+"_"+t.getLabel().getMessage()+"_REC"
               if not(res1 in alpha):
                  alpha.append(res1)
               if not(res2 in alpha):
                  alpha.append(res2)
        return alpha

    # Builds alphabet
    def buildAlphabet(self, skipSyncs = True):
        alpha=[]
        for t in self._T:
            if skipSyncs and isinstance (t, SyncTransition):
                continue
            elif isinstance (t, SyncTransition):
                resultList = t.getLabelSet()
                for l in resultList:
                    sender = l.getSender().getName()
                    receiver = l.getReceiver().getName()
                    res = sender + "_" + receiver+"_" + l.getMessage()
                    if not(res in alpha):
                        alpha.append(res)
            else:
                res=t.getLabel().getSender().getName()+"_"+t.getLabel().getReceiver().getName()+"_"+t.getLabel().getMessage()
                if not(res in alpha):
                    alpha.append(res)
        return alpha

    # Dumps alphabet
    def dumpAlphabet(self,alpha,f,any):
        l=0
        alpha = sorted (alpha)
        max=len(alpha)
        for e in alpha:
            f.write(e)
            if any:
               f.write(":any")
            l=l+1
            if l<max:
               f.write(",")

    # gets the set of synchronisation messages corresponding to one peer
    # def getSyncMsgsByPeer(self, pid):
    #     for sync in synch

    def dumpSyncMsgs(self, pid,f,any):
        l=0
        alpha = sorted (alpha)
        max = len(self.getSyncMsgsByPeer(pid))
        #max=len(alpha)
        for e in alpha:
            f.write(e)
            if any:
               f.write(":any")
            l=l+1
            if l<max:
               f.write(",")


    # Dumps alphabet with REC suffixing each message
    def dumpAlphabetWithREC(self,alpha,f,any):
        l=0
        max=len(alpha)
        alpha = sorted (alpha)
        for e in alpha:
            f.write(e)
            f.write("_REC")
            if any:
               f.write(":any")
            l=l+1
            if l<max:
               f.write(",")

    # Dumps alphabet with EM suffixing each message
    def dumpAlphabetWithEM(self,alpha,f,any):
        l=0
        max=len(alpha)
        alpha = sorted (alpha)
        for e in alpha:
            f.write(e)
            f.write("_EM")
            if any:
               f.write(":any")
            l=l+1
            if l<max:
               f.write(",")

    # returns the list of transitions with tid as target
    def getTransitionsTarget (self, state):
        #print map (lambda trans: trans.getLabel().getReceiver().getName(), self._T)
        return (filter (lambda trans: trans.getTarget ().getName() == state.getName(), self._T))

    # Returns the list of transitions with sid as source
    def getTransitions(self,sid):
        subT=[]
        for t in self._T:
            if (sid==t.getSource().getName()):
               subT.append(t)
        return subT

    def getAllTransitions(self):
        return self._T

    # returns the peer with the given name
    def getPeerFromName(self, pid):
        peers = filter (lambda peer: peer.getName() == pid, self.getPeers())
        if len (peers) > 0:
            return peers[0]
        else:
            print "Error! peer ", pid, " not found in choreo!"
            return []

    # Returns the list of peers used in the CP
    def getPeers(self):
        peers=[]
        for t in self._T:
            sender=t.getLabel().getSender()
            receiver=t.getLabel().getReceiver()
            if not(sender in peers):
               peers.append(sender)
            if not(receiver in peers):
               peers.append(receiver)
        return peers

    # Generates an LNT module for a CP
    def cp2lnt(self, generateMonitors = False, fileName = False):
        #name="tmp/"+self._name
        name=tmp+self._name
        if not fileName:
            filename=name+".lnt"
        else:
            filename = fileName
        f=open(filename, 'w')
        f.write("module "+self._name+" with \"get\" is \n\n")
        alpha=self.buildAlphabet(False)
        self.generateDatatypes(alpha,f)
        f.write("process MAIN [")
        self.dumpAlphabet(alpha,f,True)
        f.write("] is \n")
        f.write("  p_"+self._s0.getName() +"[")
        self.dumpAlphabet(alpha,f,False)
        f.write("]\n")
        f.write("end process \n\n")
        for k in self._S:
           self.state2lnt(k.getName(),alpha,f)
        peers=self.getPeers()
        for p in peers:
           self.generatePeer(p.getName(),alpha,f)


        # updated 27/11/2011: Meriem
        # adds the alphabet while generating a LNT process for the monitor
        if generateMonitors:
            for p in peers:
                self.generateMonitor (p, f)
                self.generateSyncProcesses(p,f)
                self.generateMonitoredPeers(p,f)
        for p in peers:
           self.generateQueue(p.getName(),alpha,f)
        for p in peers:
            self.generatePeerQueue(p.getName(),alpha,f, generateMonitors)
        # for checking the implements
        #self.generateComposition(alpha,peers,f,True,"composition")
        # for generating the canonical schedule removed 7.12. - not needed at the moment
        # self.generateComposition(alpha,peers,f,False,"compositionCS")

        # added 7.12. generation of synchronous composition for synchronizability check
        #self.generateComposition(alpha,peers,f,True,"composition_sync", True)

        f.write("end module \n")
        f.close()

    # Generates a few types (string, fifo) and functions
    def generateDatatypes(self,alpha,f):
        f.write("type Message is \n")
        self.dumpAlphabet(alpha,f,False)
        f.write("\n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type \n\n")
        f.write("type Queue is list of Message \n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type \n\n")
        f.write("type BoundedQueue is bqueue (queue: Queue, bound: Nat) \n")
        f.write("with \"==\", \"!=\"\n")
        f.write("end type \n\n")
        f.write("function insert (m: Message, q: Queue): Queue is \n")
        f.write("         case q in \n")
        f.write("         var hd: Message, tl: Queue in \n")
        f.write("             nil         -> return cons(m,nil) \n")
        f.write("           | cons(hd,tl) -> return insert(m,tl) \n")
        f.write("         end case \n")
        f.write("end function \n\n")
        f.write("function ishead (m: Message, q: Queue): Bool is \n")
        f.write("         case q in \n")
        f.write("         var hd: Message, tl: Queue in \n")
        f.write("             nil         -> return false \n")
        f.write("           | cons(hd,tl) -> return (m==hd) \n")
        f.write("         end case \n")
        f.write("end function \n\n")
        f.write("function remove (q: Queue): Queue is \n")
        f.write("         case q in \n")
        f.write("         var hd: Message, tl: Queue in \n")
        f.write("             nil         -> return nil \n")
        f.write("           | cons(hd,tl) -> return tl \n")
        f.write("         end case \n")
        f.write("end function \n\n")
        f.write("function count (q: Queue): Nat is \n")
        f.write("         case q in \n")
        f.write("         var hd: Message, tl: Queue in \n")
        f.write("             nil         -> return 0 \n")
        f.write("           | cons(hd,tl) -> return (1+count(tl)) \n")
        f.write("         end case \n")
        f.write("end function \n\n")
        f.write("function bisfull (bq: BoundedQueue): Bool is \n")
        f.write("  return ((count(bq.queue))==bq.bound) \n")
        f.write("end function \n\n")
        f.write("function binsert (m: Message, bq: BoundedQueue): BoundedQueue is \n")
        f.write("  if bisfull(bq) then  \n")
        f.write("     return bq  \n")
        f.write("  else  \n")
        f.write("     return bqueue(insert(m,bq.queue),bq.bound)  \n")
        f.write("  end if \n")
        f.write("end function \n\n")
        f.write("function bishead (m: Message, bq: BoundedQueue): Bool is \n")
        f.write("  return ishead(m,bq.queue) \n")
        f.write("end function \n\n")
        f.write("function bremove (bq: BoundedQueue): BoundedQueue is \n")
        f.write("  return bqueue(remove(bq.queue),bq.bound) \n")
        f.write("end function \n\n")
        f.write("function bcount (bq: BoundedQueue): Nat is \n")
        f.write("  return count(bq.queue) \n")
        f.write("end function \n\n")

    # Generates an LNT process for a state in the CP
    def state2lnt(self,sid,alpha,f):
        f.write("process p_"+sid+" [")
        self.dumpAlphabet(alpha,f,True)
        f.write("] is \n")
        subT=self.getTransitions(sid)
        max=len(subT)
        if (max>0):
          f.write(" select \n")
          i=0
          for t in subT:
              if isinstance(t, SyncTransition):
                  t.generateSyncs(f)
              else:
                  res=t.getLabel().getSender().getName()+"_"+t.getLabel().getReceiver().getName()+"_"+t.getLabel().getMessage()
                  f.write("  "+res+";")
              f.write(" p_"+t.getTarget().getName() +"[")
              self.dumpAlphabet(alpha,f,False)
              f.write("]\n")
              i=i+1
              if i<max:
                 f.write("\n[]\n")
          f.write(" end select \n")
        else:
          f.write(" null \n")
        f.write("end process \n\n")

    # Generates an LNT process for each peer
    def generatePeer(self,pid,alpha,f):
        exalphapid=self.buildAlphabetExcludingPeer(pid, False)
        incalphapid=self.buildAlphabetIncludingPeer(pid)
        incalphapid_dir=self.buildAlphabetIncludingPeerDir(pid)
        f.write("process peer_"+pid+" [")
        self.dumpAlphabet(incalphapid_dir,f,True)
        f.write("] is \n")
        f.write("  peer_"+pid+"_aux [")
        self.dumpAlphabet(incalphapid_dir,f,False)
        f.write("]\n")
        f.write("end process \n")
        f.write("process peer_"+pid+"_aux [")
        self.dumpAlphabet(incalphapid,f,True)
        f.write("] is \n")
        if (exalphapid!=[]):
           f.write(" hide ")
           self.dumpAlphabet(exalphapid,f,True)
           f.write(" in \n")
        f.write("   p_"+self._s0.getName() +" [")
        self.dumpAlphabet(alpha,f,False)
        f.write("]\n")
        if (exalphapid!=[]):
           f.write(" end hide \n")
        f.write("end process \n\n")

    ##
    # Generates the gate list for a peer queue process
    def generateAlphaQueue(self,alpha,f,any):
            max=len(alpha)
            i=0
            if alpha!=[]:
               f.write(" [")
               for m in alpha:
                   f.write(m)
                   if any:
                      f.write(":any")
                   f.write(",")
                   f.write(m+"_REC")
                   if any:
                      f.write(":any")
                   i=i+1
                   if i<max:
                      f.write(",")
               f.write("] ")

    ##
    # Generates an LNT process for a peer queue
    def generateQueue(self,pid,alpha,f):

            f.write("process ")
            pname="queue_"+pid
            f.write(pname)
            alpha=self.buildAlphabetIncludingPeerREC(pid)
            self.generateAlphaQueue(alpha,f,True)
            f.write(" (bq: BoundedQueue) is \n ")

            f.write(" select \n")
            for m in alpha:
                f.write("    if not(bisfull(bq)) then ")
                f.write(m+" ; "+pname)
                self.generateAlphaQueue(alpha,f,False)
                f.write(" (binsert("+m+",bq)) else stop end if\n")
                f.write("    [] \n")
                gaterec=m+"_REC"
                f.write("    if bishead("+m+",bq) then "+gaterec+" ; "+pname)
                self.generateAlphaQueue(alpha,f,False)
                f.write(" (bremove(bq)) else stop end if\n")
                f.write("    [] \n")

            f.write("    null \n ")
            f.write(" end select \n ")

            f.write("end process \n\n")

    # choreo is extended
    def extendedChoreoP(self):
        syncTransitions = filter (lambda trans: isinstance (trans, SyncTransition), self._T)
        return syncTransitions != []

    # checks if peer needs a monitor
    def needsMonitorP (self, pid):
        alpha = self.buildAlphabetMonitor(pid)
        return alpha != []

    # checks if peer need a synchronization process
    def needsSyncProcessP(self, pid):
        syncTransForPeer = filter (lambda trans:
                                   isinstance (trans, SyncTransition) and
                                   pid in map(lambda l: l.getSender().getName(), trans.getLabelSet()),
                                   self._T)
        return syncTransForPeer != []

    # added 5.12. Matthias
    # generates a process who does the sync message after being "activated" (init_XZY message)
    # this is required, as LNT does not allow recursive calls in parallel composition
    # even if this is justied, as it is undecidable in general
    def generateSyncProcesses(self, p, f):
        pid = p.getName()
        if self.needsMonitorP(pid) and self.needsSyncProcessP(pid):
            print "peer ", pid, " requires a synchronization process"
            syncTransForPeer = filter (lambda trans:
                                       isinstance (trans, SyncTransition) and
                                       pid in map(lambda l: l.getSender().getName(), trans.getLabelSet()),
                                       self._T)

            for syncTransLabels in map(lambda snc: snc.getLabelSet(), syncTransForPeer):
                for syncLabel in filter(lambda l: l.getSender().getName() == pid, syncTransLabels):
                    processName = "syncP_" + pid + "_" + syncLabel.getMessage()
                    f.write ("\nprocess " + processName + " [")
                    msgName = pid + "_" + syncLabel.getReceiver().getName() + "_" + syncLabel.getMessage()
                    alpha = [msgName, "init_" + msgName]
                    self.dumpAlphabet(alpha, f, True)
                    f.write ("] is\nselect\n")
                    syncMessage =  pid + "_" + syncLabel.getReceiver().getName() + "_" + syncLabel.getMessage()
                    f.write ("init_" + syncMessage + "; " + syncMessage + "; " + processName + "[")
                    self.dumpAlphabet(alpha, f, False)
                    f.write("]\n[]\nnull\nend select") # or do nothing at all
                                                       # TODO is this correct for looping behavior?
                    f.write("\nend process\n")



    # added 2.12. Matthias
    # generate peers that are monitored from parallel composition of
    # peers and their monitors, synchcronizing over the messages the peer sends
    # these are the messages the monitor will delay to repsect correct ordering
    def generateMonitoredPeers (self, p, f):

        if not self.needsMonitorP(p.getName()):
            return

        f.write ("\nprocess monitored_" + p.getName() + " [")
        alpha = self.buildAlphabetMonitored(p.getName(), False, True)
        self.dumpAlphabet(alpha, f, any)
        f.write ("] is\n")

        # hide sent "receives" from monitor
        f.write (" hide ")
        alpha = self.buildHideSetPeerMonitor(p.getName())
        self.dumpAlphabet(alpha, f, any)
        f.write (" in\n")

        # synchronize over the sent messages from peer
        # which are cached by the monitor
        f.write (" par ")
        alpha = self.buildSynchroSetPeerMonitor(p.getName())
        self.dumpAlphabet(alpha, f, False)
        f.write (" in\n")

        f.write ("  peer_" + p.getName() + " [")
        alpha = self.buildAlphabetIncludingPeerDir(p.getName())
        self.dumpAlphabet(alpha, f, False)
        f.write ("]")

        f.write ("\n  ||\n  monitor_" + p.getName() + " [")
        alpha = self.buildAlphabetMonitor(p.getName(), True, False)
        self.dumpAlphabet(alpha, f, False)
        f.write ("] ")

        f.write ("\n  end par")
        f.write ("\n end hide")
        f.write("\nend process\n")

    ## added 24.11.
    # Generate a monitor for a peer
    #
    def generateMonitor (self, peer, f):
        pid = peer.getName()
        if self.needsMonitorP(pid):
            initialState = self.getInitialState()
            f.write("\nprocess monitor_" + pid + "[")
            alpha = self.buildAlphabetMonitor(pid, False, False)
            self.dumpAlphabet(alpha, f, True)
            f.write ("] is\n")
            anzahl = 0
            if self.needsSyncProcessP(pid):
                syncTransForPeer = filter (lambda trans:
                                           isinstance (trans, SyncTransition) and
                                           pid in map(lambda l: l.getSender().getName(), trans.getLabelSet()),
                                           self._T)
                f.write ("hide ")
                alpha = self.buildAlphabetSyncProcesses(pid, True)
                self.dumpAlphabet(alpha, f, True)
                f.write (" in\npar ")

                i = 0
                numberSyncTrans = reduce(lambda x, y: x + y,
                                         map(lambda x: len(filter(lambda l: l.getSender().getName() == pid, x.getLabelSet())),
                                             syncTransForPeer))
                anzahl = numberSyncTrans
                for t in syncTransForPeer:
                    for syncLabel in t.getLabelSet():

                        if syncLabel.getSender().getName() == pid:

                            f.write ("init_" + pid + "_" + syncLabel.getReceiver().getName() + "_" + syncLabel.getMessage() + " in")
                            f.write ("\nsyncP_" + pid + "_" + syncLabel.getMessage() + "[")
                            msgName = pid + "_" + syncLabel.getReceiver().getName() + "_" + syncLabel.getMessage()
                            alpha = [msgName, "init_" + msgName]
                            self.dumpAlphabet(alpha, f, False)
                            f.write ("]\n")
                            i = i + 1
                            if i < numberSyncTrans:
                                f.write ("||\npar\n")

                f.write ("\n||\n")
            f.write ("monitor_" + pid + "_" + initialState.getName() + "[")
            alpha = self.buildAlphabetMonitor(pid)
            self.dumpAlphabet(alpha, f, False)
            f.write("]\n")
            for i in range(anzahl):
                f.write ("\nend par")
            if self.needsSyncProcessP(pid):
                f.write ("\nend hide")
            f.write ("\nend process\n")

            self.generateMonitorForState (peer, initialState, [], f)

    # added 24.11.
    # generate monitor to enforce synchronizability
    # by using the information from the diagnostics
    def generateMonitorForState (self, peer, currentState, visitedStates, f):

        if currentState not in visitedStates:
            visitedStates.append(currentState)
            f.write ("\nprocess ")

            # added 27/11/2011: Meriem
            # computes the gates for the monitor process, these are obtained from the send messages in the peer
            # and which are split into receptions of these message by the peer and their emission to other
            # peer buffers

            f.write ("monitor_" + peer.getName() + "_" + currentState.getName() + " [")
            alpha = self.buildAlphabetMonitor(peer.getName())
            self.dumpAlphabet(alpha, f, any)
            f.write(" ] is\n")
            # end of Meriem's update

            exitingTransitions = filter (lambda trans: trans.getSource().getName() == currentState.getName(),
                                         self.getAllTransitions())
            incomingSyncTransitions = filter(lambda trans: isinstance(trans, SyncTransition)
                                             and peer.getName() in map(lambda l: l.getReceiver().getName(), trans.getLabelSet()),
                                             self.getAllTransitions())


            i = 0
            f.write(" select\n")
            if len (exitingTransitions) == 0:
                f.write ("null\n")

            # print "."

            # rewrite 18.4.
            # solution has a bug and does not produce the most permissive monitors!

            # 1) peer

            for trans in exitingTransitions:
                transLabel = trans.getLabel()
                targetState = trans.getTarget()
                sender = transLabel.getSender().getName()
                receiver = transLabel.getReceiver().getName()
                message = transLabel.getMessage()

                # case 1) peer sends message -> emit local reception and relay
                if not isinstance(trans, SyncTransition) and sender == peer.getName():
                    f.write(sender + "_" + receiver + "_" + message + "_DLY;\n")
                    f.write(sender + "_" + receiver + "_" + message + ";\n (*XXX*)")
                    f.write ("monitor_" + peer.getName() + "_" + trans.getTarget().getName () + "[")
                    self.dumpAlphabet(alpha, f, False)
                    f.write ("]")


                # case 2) peer is synchronizing -> start synchronization Process
                elif isinstance (trans, SyncTransition) and peer.getName() in map(lambda l: l.getSender().getName(), trans.getLabelSet()):

                    # TODO: integrate interleaving of initiating syncs

                    trans.generateSyncs(f, True, peer.getName())
                    # f.write ("init_" + sender + "_" + receiver + "_" + transLabel.getMessage() +";\n")
                    f.write ("monitor_" + peer.getName() + "_" + trans.getTarget().getName () + "[")
                    self.dumpAlphabet(alpha, f, False)
                    f.write ("]")


                # Case 3) peer is to be synchronized
                # this is tricky
                # for each outgoing transition from the target state where peer sends a message m,
                # we must create a transition of the form "m_DLY; sync; m"
                # this assures that each "loop send message" is synchronized once, i.e., the first
                # and that a send message in the initial state is synchronized only after its first emission
                # see CAV2012 example for two of these special cases
                # 11/7/12 added support for set of syncs
                elif isinstance (trans, SyncTransition) and peer.getName() in map(lambda label: label.getReceiver().getName(), trans.getLabelSet()):

                    exitingFromTargetState = filter(lambda t: t.getSource().getName() == targetState.getName()
                                                    and t.getLabel().getSender().getName() == peer.getName(),
                                                    self.getAllTransitions())

                    f.write("select\n")
                    i2 = len(exitingFromTargetState)

                    for nextStateTrans in exitingFromTargetState:

                        nextLabel = nextStateTrans.getLabel()
                        nextReceiver = nextLabel.getReceiver().getName()
                        nextMessage = nextLabel.getMessage()


                        if nextLabel.getSender().getName() == peer.getName():
                            f.write(peer.getName() + "_" + nextReceiver + "_" + nextMessage + "_DLY; (*YYY*)\n")
                            #f.write(sender + "_" + receiver + "_" + "sync" + trans.getOriginal() + ";\n")

                            # 11/7/12 should only be one incoming sync trans ...
                            # 19/7/12 no, can be more, now multiple sender and receiver in one single SyncTrans!
                            # but then all possible sync entries must be received -> parallel interleaving of synchronization
                            resultList = filter(lambda label: label.getReceiver().getName() == peer.getName(), trans.getLabelSet())
                            if len(resultList) > 1:
                                f.write("  par\n")
                            for syncNr in range(0,len(resultList)):
                                f.write(resultList[syncNr].getSender().getName()
                                        + "_" + peer.getName() + "_" + resultList[syncNr].getMessage() + "\n")
                                if syncNr + 1 < len(resultList):
                                    f.write("\n||\n")
                            if len(resultList) > 1:
                                f.write("  end par")
                            f.write(";\n")
                            f.write(peer.getName() + "_" + nextReceiver + "_" + nextMessage + ";\n")

                        else:
                            f.write("i;\n")
                        f.write ("monitor_" + peer.getName() + "_" + nextStateTrans.getTarget().getName () + "[")
                        self.dumpAlphabet(alpha, f, False)
                        f.write ("]")

                        if i2 > 1:
                            f.write("\n[] (*BUBU*)\n")
                        i2 = i2 - 1

                    f.write("end select\n")

                # case 4) peer is not participating
                else:
                    # subcases a) there is an outgoing synchronized transition for peer in a predecessor
                    predecessorTrans = filter(lambda t: isinstance(t, SyncTransition)
                                              and t.getSource() == trans.getSource()
                                              # 11/7/12 multiple synctrans
                                              and peer.getName() in map(lambda label: label.getReceiver().getName(), t.getLabelSet()),
                                              self.getAllTransitions())

                    if len(predecessorTrans) == 0:
                        f.write("i;\n")
                        f.write ("monitor_" + peer.getName() + "_" + trans.getTarget().getName () + "[")
                        self.dumpAlphabet(alpha, f, False)
                        f.write ("]")
                    else:
                        i = i + 1

                i = i + 1
                if i < len (exitingTransitions):
                    f.write ("\n[] (*BABA*)\n")

            f.write(" end select\n")
            f.write ("end process\n\n")

            # recursive call with updated "visitedStates" information
            neighbours = map (lambda trans: trans.getTarget(), exitingTransitions)
            for neighbour in neighbours:
                self.generateMonitorForState (peer, neighbour, visitedStates, f)
        return

    ##
    # Generates an LNT process for a peer queue
    def generatePeerQueue(self,pid,alpha,f, withMonitors = False):
        f.write("process ")
        pname="peer_queue_"+pid
        f.write(pname)
        #alpha=self.buildAlphabetPeerQueue(pid)
        #alpha=self.buildAlphabetIncludingPeer(pid)
        alpha=self.buildAlphabetIncludingPeer_PQbis(pid, False)
        alphaqueue=self.buildAlphabetIncludingPeerREC(pid)
        alphapeer=self.buildAlphabetIncludingPeer_PQ(pid, False)
        f.write(" [")
        self.dumpAlphabet(alpha,f,True)
        f.write("] is \n")
        #if alphaqueue!=[]:
        #   f.write(" hide ")
        #   self.dumpAlphabetWithREC(alphaqueue,f,True)
        #   f.write(" in \n")
        f.write("  par ")
        if alphaqueue!=[]:
           self.dumpAlphabetWithREC(alphaqueue,f,False)
           f.write(" in \n")
        if withMonitors and self.needsMonitorP(pid):
            f.write("    monitored_"+pid+" [")
            alpha = self.buildAlphabetMonitored(pid, False, True)
        else:
            f.write ("    peer_" + pid + " [")
            alpha = self.buildAlphabetIncludingPeer_PQ(pid)
        self.dumpAlphabet(alpha,f, False)
        f.write("] \n")
        f.write("    || \n")
        f.write("    queue_"+pid)
        self.generateAlphaQueue(alphaqueue,f,False)
        f.write("(bqueue(nil,1)) \n")
        f.write("  end par \n")
        #if alphaqueue!=[]:
        #   f.write(" end hide \n")
        f.write("end process \n\n")

    # compute parallel composition of (monitor || peer) || buffer?
    #  - the hide parameter seems to be rather obsolete (creates the compoCS LTS)
    #  - parallelSVLSyntax triggers smart reduction (on SVL level)
    #    compatible parallel composition syntax generation
    def generateComposition(self,alpha,peers,f,hide,name, synchronousComposition = False, parallelSVLSyntax = False):
        if not parallelSVLSyntax:
            f.write("process ")
            f.write(name)
            f.write(" [")
            self.dumpAlphabet(alpha,f,True)
            if not(hide):
                f.write(",")
                # no REC for the sync messages!
                alpha = self.buildAlphabet(False)
                self.dumpAlphabetWithREC(alpha,f,True)
            f.write("] is \n")
        i=0

        alphaSync = self.getSyncLabels()

        if parallelSVLSyntax and len(alphaSync) > 0:
            f.write("(total hide ")
            self.dumpAlphabet(alphaSync,f,False)
            f.write (" in ")

        max=len(peers)
        for p in peers:
            pid=p.getName()
            if (i+1)<max:
                f.write("par ")
                # synchro set computation
                synchro=self.computeSynchro(pid,peers[i+1:])
                if synchro!=[]:
                    self.dumpAlphabet(synchro,f,False)
                    f.write(" in")
                #elif parallelSVLSyntax: # HERE HERE 
                #    f.write(" in")
                f.write("\n")
            if hide:
                if synchronousComposition:
                    alphaqueue = self.buildAlphabetIncludingPeer (pid)
                else:
                    alphaqueue=self.buildAlphabetIncludingPeerREC(pid)
                if alphaqueue!=[] and not synchronousComposition:
                    if parallelSVLSyntax:
                        f.write ("(total hide ")
                        self.dumpAlphabetWithREC(alphaqueue, f, False)
                    else:
                        f.write("hide ")
                        self.dumpAlphabetWithREC(alphaqueue,f,True)
                    f.write(" in\n")
            alpha=self.buildAlphabetIncludingPeer_PQbis(pid, False)
            if not synchronousComposition:
                pname=" peer_queue_"+pid
            else:
                alpha = self.buildAlphabetIncludingPeer (pid, False)
                if self.needsMonitorP(pid) and self.extendedChoreoP():
                    pname = "monitored_" + pid
                else:
                    pname = "peer_" + pid

            f.write(pname)
            f.write(" [")
            self.dumpAlphabet(alpha,f,False)
            f.write("]\n")
            if hide and not synchronousComposition:
                if alphaqueue!=[]:
                    if parallelSVLSyntax:
                        f.write (")\n")
                    else:
                        f.write("end hide\n")
            i=i+1
            if (i<max):
                f.write("||\n")
        i=1
        while (i<max):
           f.write("end par\n")
           i=i+1
        if not parallelSVLSyntax:
            f.write("end process\n\n")
        else:
            if parallelSVLSyntax and len(alphaSync) > 0:
                f.write(")")
            f.write (";\n\n")


    ##
    # Computes a synchronisation set given a peer and a list of peers
    # extended 5.12. Matthias:
    #   also respects the synchronization messages from the monitors
    def computeSynchro(self,pid,peers):
        alpha=self.buildAlphabetIncludingPeer(pid)
        alphas=[]

        # computes those transitions where pid is sender,
        # one peer in the set peers is receiver or vice versa
        # these also have to added as synchronization labels!
        # relevantSyncLabels =  filter (lambda trans:
        #                                      isinstance (trans, SyncTransition) and
        #                                      ((trans.getLabel().getSender().getName() == pid and
        #                                       trans.getLabel().getReceiver().getName() in map (lambda peer: peer.getName(), peers)) or
        #                                       (trans.getLabel().getReceiver().getName() == pid and
        #                                       trans.getLabel().getSender().getName() in map (lambda peer: peer.getName(), peers))),
        #                                       self._T)

        # 13/7/12 support multiple sync trans
        emitSyncTrans = filter(lambda trans: isinstance(trans, SyncTransition) and
                               pid in map(lambda l: l.getSender().getName(), trans.getLabelSet()),
                               self._T)

        # keep only those labels, where pid is sender and receiver is in "peers" set
        emitSyncLabels = []
        for t in emitSyncTrans:
            map(lambda l: emitSyncLabels.append(l),
                filter(lambda l: l.getSender().getName() == pid and l.getReceiver().getName() in map(lambda peer: peer.getName(), peers),
                       t.getLabelSet()))

        incomingSyncTrans = filter(lambda trans: isinstance(trans, SyncTransition) and
                                   pid in map(lambda l: l.getReceiver().getName(), trans.getLabelSet()) and
                                   trans.getLabel().getSender().getName() in map(lambda peer: peer.getName(), peers),
                                   self._T)

        # keep only those labels where sender in "peers" set and pid is receiver
        incSyncLabels = []
        for t in incomingSyncTrans:
            map(lambda l: incSyncLabels.append(l),
                filter(lambda l: l.getReceiver().getName() == pid and
                       l.getSender() in peers, t.getLabelSet()))

        labelSet = emitSyncLabels  + incSyncLabels


        for k in peers:
            ak=self.buildAlphabetIncludingPeer(k.getName())
            alphas=self.union(alphas,ak)

        return self.union (self.intersection(alpha,alphas),
                           map (lambda l:
                                l.getSender().getName() + "_" +
                                l.getReceiver().getName() + "_" +
                                l.getMessage(), labelSet))


    ##
    # Computes the union of 2 lists
    def union(self,l1,l2):
        l=[]
        for e in l1:
            if not(e in l2):
               l.append(e)
        l.extend(l2)
        return l

    ##
    # Computes the intersection of 2 lists
    def intersection(self,l1,l2):
        l=[]
        for e in l1:
            if e in l2:
               l.append(e)
        return l


    # return list of all synchronization labels (for hiding in SVL)
    def getSyncLabels(self):
        strans = filter(lambda trans: isinstance(trans, SyncTransition), self.getAllTransitions())

        syncLabels = []
        for trans in strans:
            for l in trans.getLabelSet():
                syncLabels.append(l.getSender().getName() + "_" + l.getReceiver().getName() + "_" + l.getMessage())

        return syncLabels


    # Generates an SVL file
    def generateSVL(self, smartComposition = False, generateDebugBCG = False):
        #name="tmp/"+self._name
        name=self._name
        filename=tmp+name+".svl"
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"% CADP_TIME=\"/usr/bin/time\"\n\n")
        #f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"% CADP_TIME=\"
        f.write ("% DEFAULT_PROCESS_FILE=" + name + ".lnt\n\n")
        # CP generation (LTS)
        f.write("\""+name+"_cp.bcg\" = tau*.a reduction of ")

        alphaSync = self.getSyncLabels()

        if len(alphaSync) > 0:
            f.write("(total hide ")
            self.dumpAlphabet(alphaSync,f,False)
            f.write (" in ")

        f.write(" \"MAIN [")
        alpha=self.buildAlphabet(False)
        self.dumpAlphabet(alpha,f,False)
        f.write("]\"")
        if len(alphaSync) > 0:
            f.write(")")
        f.write(";\n\n")

        if not smartComposition:
            # then we use compositional verification
            # faster, but larger intermediate state-spaces
            f.write("\""+name+"_compo.bcg\" = root leaf branching reduction of\n")
            alpha = self.buildAlphabet(False)
            peers=self.getPeers()
            self.generateComposition(alpha, peers, f, True, "compo", False, True)
        else:
            f.write("\""+name+"_compo.bcg\" = smart branching reduction of\n")
            alpha = self.buildAlphabet(False)
            peers=self.getPeers()
            self.generateComposition(alpha, peers, f, True, "compo", False, True)

        f.write("\""+name+"_compo_min.bcg\"= weak trace reduction of safety reduction of tau*.a reduction of branching reduction of \""+name+"_compo.bcg\" ;\n\n")

#        f.write("\"compo_min.bcg\"= safety reduction of tau*.a reduction of branching reduction of \"compo.bcg\" ;\n\n")

        # synchronous composition
        if not smartComposition:
            f.write("\""+name+"_compo_sync.bcg\" = root leaf branching reduction of\n")
            alpha = self.buildAlphabet(False)
            peers=self.getPeers()
            self.generateComposition(alpha, peers, f, True, "compo_sync", True, True)
        else:
            f.write("\""+name+"_compo_sync.bcg\" = smart branching reduction of\n")
            alpha = self.buildAlphabet(False)
            peers=self.getPeers()
            self.generateComposition(alpha, peers, f, True, "compo_sync", True, True)

        f.write("\""+name+"_compo_sync_min.bcg\"= weak trace reduction of safety reduction of tau*.a reduction of branching reduction of \""+name+"_compo_sync.bcg\" ;\n\n")
        #f.write("\"compo_sync_min.bcg\"=  safety reduction of tau*.a reduction of branching reduction of \"compo_sync.bcg\" ;\n\n")

        if generateDebugBCG:
            # peer generation
            peers=self.getPeers()
            for p in peers:
                pid=p.getName()
                incalphapid_dir=self.buildAlphabetIncludingPeerDir(pid)
                f.write("\""+name+"_peer_" + pid + ".bcg\" = ")
                f.write("safety reduction of tau*.a reduction of branching reduction of \"peer_" + pid +" [")
#                f.write("% lnt.open -more cat -root 'peer_"+pid+" [")
                self.dumpAlphabet(incalphapid_dir,f,False)
#                f.write("]' ")
                f.write("]\";\n\n")

                if  self.needsMonitorP(pid):
                    f.write("\""+name+"_monitor_" + pid + ".bcg\" = generation of \"monitor_" + pid + " [")
                    monitorAlpha = self.buildAlphabetMonitor(pid, False, False)
                    self.dumpAlphabet(monitorAlpha, f, False)
                    f.write("]\";\n\n")

                    f.write("\""+name+"_monitored_" + pid + ".bcg\" = safety reduction of tau*.a reduction of branching reduction of \"monitored_" + pid + " [")
                    monitoredAlpha = self.buildAlphabetMonitored(pid, False, False)
                    self.dumpAlphabet(monitoredAlpha, f, False)
                    f.write("]\";\n\n")

        f.close()

        filename=tmp+name+"-synchronizability.svl"
        f=open(filename, 'w')
        f.write("\"synchronizability.bcg\" =  strong comparison using bfs with bisimulator \""+name+"_compo_sync_min.bcg\" == \""+name+"_compo_min.bcg\"");
        f.close()

        filename=tmp+name+"-realizability.svl"
        f=open(filename, 'w')
        f.write("\"realizability.bcg\" = strong comparison using bfs with bisimulator  \""+name+"_cp.bcg\" == \""+name+"_compo_min.bcg\";")
        f.close()

    # Generates SVL code to compute the canonical schedule
    def generateCanonicalSchedule(self,peers,f):
        f.write("\"canonical_schedule.bcg\"=total prio\n")
        lacr=[]
        laem=[]
        for p in peers:
            pid=p.getName()
            acr=self.buildAlphabetChoiceRec(pid)
            aem=self.buildAlphabetEm(pid)
            if acr!=[]:
               lacr.append(acr)
            if aem!=[]:
               laem.append(aem)
        i=0
        max=len(lacr)
        for l in lacr:
            self.dumpAlphabet(l,f,False)
            i=i+1
            if (i<max):
               f.write(">")
        if laem!=[]:
           f.write(">")
        i=0
        max=len(laem)
        for l in laem:
            self.dumpAlphabet(l,f,False)
            i=i+1
            if (i<max):
               f.write(">")
        f.write(" in \"compoCS_min.bcg\" ;\n\n")


##
# Example database class.
class Examples:

    ##
    # Builds CP instance: first example
    def cp0001(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        # CP init
        cp0001=CP("cp0001",st0)
        # add states
        cp0001.addState(st0)
        cp0001.addState(st1)
        cp0001.addState(st2)
        cp0001.addState(st3)
        # peers
        env=Peer("env")
        p1=Peer("p1")
        p2=Peer("p2")
        p3=Peer("p3")
        #
        cp0001.addTransition(Transition(st0,Label(env,p1,"a"),st1))
        cp0001.addTransition(Transition(st1,Label(p1,p2,"b"),st2))
        cp0001.addTransition(Transition(st2,Label(p3,p1,"c"),st3))


        return cp0001


    ##
    # Builds CP instance: second example
    def cp0002(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        # CP init
        cp0002=CP("cp0002",st0)
        # add states
        cp0002.addState(st0)
        cp0002.addState(st1)
        cp0002.addState(st2)
        # peers
        p1=Peer("p1")
        p2=Peer("p2")
        p3=Peer("p3")
        #
        cp0002.addTransition(Transition(st0,Label(p1,p2,"a"),st1))
        cp0002.addTransition(Transition(st1,Label(p2,p3,"b"),st2))

        return cp0002

    ##
    # Builds CP instance: another example
    def cp0003(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        # CP init
        cp=CP("cp0003",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        # peers
        env=Peer("env")
        p1=Peer("p1")
        p2=Peer("p2")
        #
        cp.addTransition(Transition(st0,Label(env,p1,"a"),st1))
        cp.addTransition(Transition(st1,Label(p2,p1,"b"),st2))

        return cp

    ##
    # Builds CP instance: another example
    def cp0004(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        # CP init
        cp=CP("cp0004",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        # peers
        env=Peer("env")
        p1=Peer("p1")
        p2=Peer("p2")
        p3=Peer("p3")
        #
        cp.addTransition(Transition(st0,Label(env,p1,"a"),st1))
        cp.addTransition(Transition(st1,Label(p1,p2,"b"),st2))
        cp.addTransition(Transition(st1,Label(p1,p3,"c"),st3))

        return cp


    ##
    # Builds CP instance: cp0004 extended
    def cp0005(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        # CP init
        cp=CP("cp0005",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        # peers
        env=Peer("env")
        p1=Peer("p1")
        p2=Peer("p2")
        p3=Peer("p3")
        #
        cp.addTransition(Transition(st0,Label(env,p1,"a"),st1))
        cp.addTransition(Transition(st1,Label(p1,p2,"b"),st2))
        cp.addTransition(Transition(st1,Label(p1,p3,"c"),st3))
        cp.addTransition(Transition(st2,Label(p2,p3,"c"),st3))

        return cp

    ##
    # Builds CP instance: cp0004 extended
    def cp0006(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        # CP init
        cp=CP("cp0006",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        # peers
        env=Peer("env")
        p1=Peer("p1")
        p2=Peer("p2")
        p3=Peer("p3")
        #
        cp.addTransition(Transition(st0,Label(env,p1,"a"),st1))
        cp.addTransition(Transition(st1,Label(p1,p2,"b"),st2))
        cp.addTransition(Transition(st1,Label(p1,p3,"c"),st3))
        cp.addTransition(Transition(st2,Label(p3,p1,"c"),st3))

        return cp

    ##
    # First example a little bit more realistic
    def cp0007(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        st4=State("4")
        # CP init
        cp=CP("cp0007",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        # peers
        env=Peer("env")
        p1=Peer("dispatcher")
        p2=Peer("provider")
        p3=Peer("counter")
        # transitions
        cp.addTransition(Transition(st0,Label(env,p1,"request"),st1))
        cp.addTransition(Transition(st1,Label(p1,p2,"submit"),st2))
        cp.addTransition(Transition(st2,Label(p1,p3,"count"),st3))
        cp.addTransition(Transition(st3,Label(p2,p1,"result"),st4))
        cp.addTransition(Transition(st4,Label(p1,env,"request"),st0))

        return cp

    ##
    # First example a little bit more realistic
    def cp0008(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        st4=State("4")
        # CP init
        cp=CP("cp0008",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        # peers
        env=Peer("env")
        provider=Peer("provider")
        database=Peer("database")
        counter=Peer("counter")
        # transitions
        cp.addTransition(Transition(st0,Label(env,provider,"request"),st1))
        cp.addTransition(Transition(st1,Label(provider,database,"query"),st2))
        cp.addTransition(Transition(st2,Label(provider,counter,"count"),st3))
        cp.addTransition(Transition(st3,Label(database,provider,"query"),st4))
        cp.addTransition(Transition(st3,Label(provider,env,"error"),st0))
        cp.addTransition(Transition(st4,Label(provider,env,"request"),st0))

        return cp

    ##
    # First example a little bit more realistic
    def cp0009(self):

        # states
        st0=State("0")
        st1=State("1")
        st2=State("2")
        st3=State("3")
        st4=State("4")
        st5=State("5")
        # CP init
        cp=CP("cp0009",st0)
        # add states
        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)
        # peers
        env=Peer("env")
        provider=Peer("provider")
        database=Peer("database")
        counter=Peer("counter")
        # transitions
        cp.addTransition(Transition(st0,Label(env,provider,"request"),st1))
        cp.addTransition(Transition(st1,Label(provider,database,"query"),st2))
        cp.addTransition(Transition(st2,Label(provider,counter,"count"),st3))
        cp.addTransition(Transition(st3,Label(database,provider,"query"),st4))
        cp.addTransition(Transition(st4,Label(provider,env,"result"),st0))
        cp.addTransition(Transition(st3,Label(database,provider,"error"),st5))
        cp.addTransition(Transition(st4,Label(provider,env,"error"),st0))

        return cp

    # Meriem's example
    # has an emitting self-loop (in one peer, not in choreo)
    def cp0010(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP("cp0010", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p3, "a"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "b"), st2))
        cp.addTransition(Transition (st2, Label (p3, p2, "c"), st0))

        return cp

    # example with emitting self-loop in the choreography
    def cp0011(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP ("cp0011", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        cl = Peer("cl")
        int = Peer("int")
        app = Peer("app")

        cp.addTransition(Transition (st0, Label (cl, int, "connect"), st1))
        cp.addTransition(Transition (st1, Label (int, app, "setup"), st2))
        cp.addTransition(Transition (st2, Label (cl, app, "access"), st2)) # self-loop here
        cp.addTransition(Transition (st2, Label (cl, int, "logout"), st0))

        return cp

    # whiteboard example from 21.11. discussion with Meriem
    def cp0012(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP ("cp0012", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "a"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "c"), st2))
        cp.addTransition(Transition (st0, Label (p2, p3, "d"), st2))
        cp.addTransition(Transition (st2, Label (p1, p2, "b"), st0))

        return cp

    # small, but not so simple ...
    def cp0013(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0013", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p3, "a"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "c"), st2))
        cp.addTransition(Transition (st2, Label (p1, p3, "b"), st3))

        return cp

    # one main cycle and a branching/merge
    def cp0014(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP ("cp0014", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "a"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "c"), st2))
        cp.addTransition(Transition (st0, Label (p2, p3, "d"), st2))
        cp.addTransition(Transition (st2, Label (p1, p2, "b"), st0))

        return cp

    # double cycles, realistic example, deadlock (issta'09)
    def cp0015(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0015", st3)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        cl = Peer("cl")
        sv = Peer("sv")

        cp.addTransition(Transition (st0, Label (cl, sv, "req"), st1))
        cp.addTransition(Transition (st1, Label (sv, cl, "ack"), st0))
        cp.addTransition(Transition (st0, Label (sv, cl, "shutdown1"), st2))
        cp.addTransition(Transition (st1, Label (sv, cl, "shutdown2"), st2))
        cp.addTransition(Transition (st0, Label (cl, sv, "logout"), st3))
        cp.addTransition(Transition (st3, Label (cl, sv, "connect"), st0))

        return cp

    # one main cycle, one self-loop, realistic example, no deadlock
    def cp0016(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP ("cp0016", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        cl = Peer("client")
        inter = Peer("interface")
        appli = Peer("application")

        cp.addTransition(Transition (st0, Label (cl, inter, "connect"), st1))
        cp.addTransition(Transition (st1, Label (inter, appli, "setup"), st2))
        cp.addTransition(Transition (st2, Label (cl, appli, "access"), st2))
        cp.addTransition(Transition (st2, Label (cl, inter, "logout"), st0))

        return cp

    # choice
    def cp0017(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0017", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "m1"), st1))
        cp.addTransition(Transition (st1, Label (p3, p2, "m2"), st2))
        cp.addTransition(Transition (st1, Label (p1, p2, "m3"), st3))

        return cp

    # choice and loop
    def cp0018(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0018", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "m1"), st1))
        cp.addTransition(Transition (st1, Label (p3, p2, "m2"), st2))
        cp.addTransition(Transition (st1, Label (p1, p2, "m3"), st3))
        cp.addTransition(Transition (st2, Label (p1, p2, "m4"), st0))

        return cp

    # choice and two loops
    def cp0019(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0019", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "m1"), st1))
        cp.addTransition(Transition (st1, Label (p3, p2, "m2"), st2))
        cp.addTransition(Transition (st1, Label (p1, p2, "m3"), st3))
        cp.addTransition(Transition (st2, Label (p1, p2, "m4"), st0))
        cp.addTransition(Transition (st3, Label (p3, p2, "m5"), st1))

        return cp

    # two transitions in a loop
    def cp0020(self):
        st0 = State("0")
        st1 = State("1")

        cp = CP ("cp0020", st0)

        cp.addState(st0)
        cp.addState(st1)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "AAAAAA"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "BBBBBB"), st0))

        return cp

    # three transitions in a loop
    def cp0021(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")

        cp = CP ("cp0021", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp.addTransition(Transition (st0, Label (p1, p2, "AAAAAA"), st1))
        cp.addTransition(Transition (st1, Label (p2, p3, "BBBBBB"), st2))
        cp.addTransition(Transition (st2, Label (p1, p2, "EddEED"), st0))

        return cp

    # one loop and multiple sources of non-synchronizability
    def cp0022(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP ("cp0022", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        cp.addTransition(Transition (st0, Label (p1, p2, "aaa"), st1))
        cp.addTransition(Transition (st0, Label (p2, p3, "bbb"), st2))
        cp.addTransition(Transition (st1, Label (p3, p4, "ccc"), st2))
        cp.addTransition(Transition (st2, Label (p3, p4, "ddd"), st1))
        cp.addTransition(Transition (st1, Label (p3, p4, "eee"), st3))
        cp.addTransition(Transition (st2, Label (p3, p4, "fff"), st3))

        return cp

    # relatively big choreo with self loop and branching
    def cp0023(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")
        st6 = State("6")

        cp = CP("cp0023", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)
        cp.addState(st6)

        clt = Peer("Client")
        spr = Peer("Suplier")
        bnk = Peer("Bank")
        db = Peer("Database")

        cp.addTransition(Transition (st0, Label (clt, spr, "log1"), st1))
        cp.addTransition(Transition (st1, Label (clt, spr, "log2"), st1))
        cp.addTransition(Transition (st1, Label (clt, spr, "searchIt"), st2))
        cp.addTransition(Transition (st2, Label (spr, db, "checkDb"), st2))
        cp.addTransition(Transition (st2, Label (spr, clt, "reply1"), st4))
        cp.addTransition(Transition (st2, Label (spr, clt, "cancel1"), st1))
        cp.addTransition(Transition (st4, Label (clt, spr, "buy"), st6))
        #
        cp.addTransition(Transition (st1, Label (clt, bnk, "consult"), st3))
        cp.addTransition(Transition (st3, Label (bnk, clt, "reply1"), st1))
        cp.addTransition(Transition (st3, Label (clt, spr, "cancel2"), st5))

        return cp

    def cp0024(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")
        st6 = State("6")

        cp = CP("cp0024", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)
        cp.addState(st6)

        fgt = Peer("Flight")
        clt = Peer("Client")
        htl = Peer("Hotel")

        cp.addTransition(Transition (st0, Label (clt, fgt, "searchFlight1"), st1))
        cp.addTransition(Transition (st1, Label (clt, fgt, "book1"), st2))
        cp.addTransition(Transition (st2, Label (fgt, clt, "ack1"), st3))


        cp.addTransition(Transition (st0, Label (clt, htl, "searchFlight2"), st4))
        cp.addTransition(Transition (st4, Label (clt, htl, "book2"), st5))
        cp.addTransition(Transition (st5, Label (htl, clt, "ack2"), st6))

        return cp

    # it's an extension of cp0024
    def cp0025(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")
        st6 = State("6")

        cp = CP("cp0025", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)
        cp.addState(st6)

        fgt = Peer("Flight")
        clt = Peer("Client")
        htl = Peer("Hotel")

        cp.addTransition(Transition (st0, Label (clt, fgt, "searchFlight1"), st1))
        cp.addTransition(Transition (st1, Label (clt, fgt, "book1"), st2))
        cp.addTransition(Transition (st2, Label (fgt, clt, "ack1"), st0))


        cp.addTransition(Transition (st0, Label (clt, htl, "searchFlight2"), st4))
        cp.addTransition(Transition (st4, Label (clt, htl, "book2"), st5))
        cp.addTransition(Transition (st5, Label (htl, clt, "ack2"), st0))

        return cp

    # example of two clients communiting with one server
    def cp0026(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP("cp0026", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)


        clt1 = Peer("Client1")
        clt2 = Peer("Client2")
        srv = Peer("Server")

        cp.addTransition(Transition (st0, Label (clt1, srv, "getIP1"), st1))
        cp.addTransition(Transition (st0, Label (clt1, srv, "exit"), st2))
        cp.addTransition(Transition (st1, Label (srv, clt1, "sendIP1"), st0))

        cp.addTransition(Transition (st0, Label (clt2, srv, "getIP2"), st2))
        cp.addTransition(Transition (st1, Label (clt2, srv, "sendIP2"), st0))

        return cp

    # desconnected subsystems
    def cp0027(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP("cp0027", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")


        cp.addTransition(Transition (st0, Label (p1, p2, "b"), st1))
        cp.addTransition(Transition (st0, Label (p3, p2, "c"), st1))
        cp.addTransition(Transition (st3, Label (p4, p5, "a"), st3))

        return cp

    def cp0028(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP("cp0028", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")


        cp.addTransition(Transition (st0, Label (p1, p2, "a"), st1))
        cp.addTransition(Transition (st1, Label (p2, p1, "c"), st3))
        cp.addTransition(Transition (st0, Label (p3, p2, "b"), st2))
        cp.addTransition(Transition (st2, Label (p2, p3, "d"), st3))

        return cp

    def cp0029(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")

        cp = CP("cp0029", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")


        cp.addTransition(Transition (st0, Label (p1, p2, "a"), st1))
        cp.addTransition(Transition (st1, Label (p3, p1, "b"), st2))
        cp.addTransition(Transition (st2, Label (p2, p1, "c"), st1))
        cp.addTransition(Transition (st2, Label (p1, p3, "d"), st3))
        cp.addTransition(Transition (st3, Label (p2, p3, "e"), st0))

        return cp

    def simpleChoice (self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")

        cp = CP("simple_choice", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(p1, p2, "m1"), s2))
        cp.addTransition(Transition(s0, Label(p3, p4, "m2"), s3))

        return cp

    # message beofre simple choice
    def msgSimpleChoice (self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")
        p5 = Peer ("p5")
        p6 = Peer ("p6")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")
        s4 = State ("s4")

        cp = CP("msg_simple_choice", s4)
        cp.addState(s4)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(p1, p2, "m1"), s2))
        cp.addTransition(Transition(s0, Label(p3, p4, "m2"), s3))
        cp.addTransition(Transition(s4, Label(p5, p6, "m3"), s0))

        return cp

    # as simple choice, but receiver is the same peer
    def notSoSimpleChoice(self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")

        cp = CP("not_so_simple_choice", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(p1, p3, "m1"), s2))
        cp.addTransition(Transition(s0, Label(p2, p3, "m2"), s3))

        return cp

    # one initial message before the not so simple choice
    def msgNotSoSimpleChoice(self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")
        s4 = State ("s4")

        cp = CP("msg_not_so_simple_choice", s4)
        cp.addState(s4)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(p1, p3, "m1"), s2))
        cp.addTransition(Transition(s0, Label(p2, p3, "m2"), s3))
        cp.addTransition (Transition (s4, Label (p1, p4, "m3"), s0))

        return cp

    # peers starting to send to each other
    def sendEachOther(self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")

        cp = CP("send_each_other", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)

        cp.addTransition(Transition (s0, Label(p1, p2, "m1"), s1))
        cp.addTransition(Transition (s0, Label(p2, p1, "m2"), s2))

        return cp


    # peers starting to send to each other with initial message
    def msgSendEachOther(self):
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")

        cp = CP("msg_send_each_other", s3)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)


        cp.addTransition(Transition(s3, Label(p1, p3, "m0"), s0))
        cp.addTransition(Transition (s0, Label(p1, p2, "m1"), s1))
        cp.addTransition(Transition (s0, Label(p2, p1, "m2"), s2))

        return cp

    # simple example which generates two sync processes
    def twoSyncs(self):

        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")
        s4 = State ("s4")
        s5 = State ("s5")

        cp = CP("two_syncs", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1, p2, "m1"),s1))
        cp.addTransition(Transition(s1, Label(p3, p1, "m2"),s2))
        cp.addTransition(Transition(s2, Label(p1, p2, "m3"),s3))
        cp.addTransition(Transition(s3, Label(p4, p3, "m4"),s4))

        return cp

    # simple example which generates two sync processes
    def twoSyncsLoop(self):

        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")
        s4 = State ("s4")
        s5 = State ("s5")

        cp = CP("two_syncs_loop", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1, p2, "m1"),s1))
        cp.addTransition(Transition(s1, Label(p3, p1, "m2"),s2))
        cp.addTransition(Transition(s2, Label(p1, p2, "m3"),s3))
        cp.addTransition(Transition(s3, Label(p4, p3, "m4"),s4))
        cp.addTransition(Transition(s3, Label(p4, p2, "m5"),s0))

        return cp

    def twoIncomingSyncs(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")

        cp = CP("two_incoming_syncs", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0,Label(p1,p2,"m1"),s1))
        cp.addTransition(Transition(s0,Label (p1,p3,"m2"),s3))
        cp.addTransition(Transition(s2,Label (p2,p3,"m3"),s1))
        cp.addTransition(Transition(s1,Label(p4,p5,"m4"),s3))

        return cp


    def cp0030(self):
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0030", s0)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s6, Label (p9, p0, "G433"), s9))
        cp.addTransition (Transition (s6, Label (p9, p5, "G432"), s8))
        cp.addTransition (Transition (s3, Label (p1, p6, "G430"), s6))
        cp.addTransition (Transition (s3, Label (p4, p0, "G429"), s5))
        cp.addTransition (Transition (s2, Label (p5, p9, "G427"), s3))
        cp.addTransition (Transition (s1, Label (p4, p0, "G426"), s2))
        cp.addTransition (Transition (s0, Label (p2, p5, "G425"), s1))
        return cp


    def cp0031(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0031", s0)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s11, Label (p3, p8, "G961"), s12))
        cp.addTransition (Transition (s10, Label (p8, p4, "G960"), s11))
        cp.addTransition (Transition (s7, Label (p4, p6, "G959"), s10))
        cp.addTransition (Transition (s7, Label (p1, p7, "G958"), s9))
        cp.addTransition (Transition (s6, Label (p5, p1, "G956"), s7))
        cp.addTransition (Transition (s5, Label (p5, p0, "G955"), s6))
        cp.addTransition (Transition (s2, Label (p3, p6, "G954"), s5))
        cp.addTransition (Transition (s2, Label (p3, p5, "G953"), s0))
        cp.addTransition (Transition (s1, Label (p6, p3, "G951"), s2))
        cp.addTransition (Transition (s0, Label (p8, p0, "G950"), s1))
        return cp

    def cp0031(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0031", s0)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s11, Label (p0, p4, "G1511"), s12))
        cp.addTransition (Transition (s10, Label (p6, p3, "G1510"), s11))
        cp.addTransition (Transition (s9, Label (p6, p3, "G1509"), s10))
        cp.addTransition (Transition (s6, Label (p5, p6, "G1508"), s9))
        cp.addTransition (Transition (s6, Label (p5, p4, "G1507"), s2))
        cp.addTransition (Transition (s5, Label (p2, p6, "G1505"), s6))
        cp.addTransition (Transition (s4, Label (p3, p4, "G1504"), s5))
        cp.addTransition (Transition (s3, Label (p2, p0, "G1503"), s4))
        cp.addTransition (Transition (s2, Label (p0, p4, "G1502"), s3))
        cp.addTransition (Transition (s1, Label (p0, p5, "G1501"), s2))
        cp.addTransition (Transition (s0, Label (p1, p0, "G1500"), s1))
        return cp

    def cp0032(self):
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0032", s0)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s14, Label (p2, p6, "G1526"), s15))
        cp.addTransition (Transition (s13, Label (p4, p8, "G1525"), s14))
        cp.addTransition (Transition (s10, Label (p7, p1, "G1524"), s13))
        cp.addTransition (Transition (s10, Label (p7, p4, "G1523"), s12))
        cp.addTransition (Transition (s9, Label (p8, p2, "G1521"), s10))
        cp.addTransition (Transition (s6, Label (p6, p9, "G1520"), s9))
        cp.addTransition (Transition (s6, Label (p6, p3, "G1519"), s5))
        cp.addTransition (Transition (s3, Label (p8, p2, "G1517"), s6))
        cp.addTransition (Transition (s3, Label (p8, p0, "G1516"), s5))
        cp.addTransition (Transition (s0, Label (p0, p2, "G1514"), s3))
        cp.addTransition (Transition (s0, Label (p0, p5, "G1513"), s2))
        return cp

    def cp0033(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0033", s0)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s12, Label (p5, p1, "G1540"), s13))
        cp.addTransition (Transition (s11, Label (p6, p5, "G1539"), s12))
        cp.addTransition (Transition (s10, Label (p7, p1, "G1538"), s11))
        cp.addTransition (Transition (s9, Label (p5, p1, "G1537"), s10))
        cp.addTransition (Transition (s7, Label (p1, p8, "G1536"), s9))
        cp.addTransition (Transition (s7, Label (p7, p4, "G1535"), s8))
        cp.addTransition (Transition (s4, Label (p7, p4, "G1533"), s7))
        cp.addTransition (Transition (s4, Label (p7, p0, "G1532"), s6))
        cp.addTransition (Transition (s3, Label (p7, p2, "G1530"), s4))
        cp.addTransition (Transition (s0, Label (p1, p8, "G1529"), s3))
        cp.addTransition (Transition (s0, Label (p4, p5, "G1528"), s1))
        return cp

    def cp0034(self):
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0034", s0)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s9, Label (p3, p2, "G1552"), s11))
        cp.addTransition (Transition (s9, Label (p3, p7, "G1551"), s6))
        cp.addTransition (Transition (s8, Label (p3, p4, "G1549"), s9))
        cp.addTransition (Transition (s7, Label (p5, p3, "G1548"), s8))
        cp.addTransition (Transition (s6, Label (p1, p4, "G1547"), s7))
        cp.addTransition (Transition (s5, Label (p3, p7, "G1546"), s6))
        cp.addTransition (Transition (s4, Label (p4, p0, "G1545"), s5))
        cp.addTransition (Transition (s3, Label (p7, p0, "G1544"), s4))
        cp.addTransition (Transition (s0, Label (p3, p6, "G1543"), s3))
        cp.addTransition (Transition (s0, Label (p3, p4, "G1542"), s2))
        return cp

    def cp0035(self):
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0035", s0)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s11, Label (p0, p3, "G1565"), s12))
        cp.addTransition (Transition (s10, Label (p1, p5, "G1564"), s11))
        cp.addTransition (Transition (s9, Label (p4, p5, "G1563"), s10))
        cp.addTransition (Transition (s7, Label (p6, p2, "G1562"), s9))
        cp.addTransition (Transition (s7, Label (p6, p0, "G1560"), s1))
        cp.addTransition (Transition (s4, Label (p5, p3, "G1559"), s7))
        cp.addTransition (Transition (s4, Label (p5, p4, "G1558"), s6))
        cp.addTransition (Transition (s1, Label (p2, p5, "G1556"), s4))
        cp.addTransition (Transition (s1, Label (p2, p4, "G1555"), s2))
        cp.addTransition (Transition (s0, Label (p4, p2, "G1553"), s1))
        return cp

    def cp0036(self):
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0036", s0)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s13, Label (p7, p4, "G1583"), s16))
        cp.addTransition (Transition (s13, Label (p7, p3, "G1582"), s15))
        cp.addTransition (Transition (s12, Label (p0, p7, "G1580"), s13))
        cp.addTransition (Transition (s10, Label (p1, p6, "G1579"), s12))
        cp.addTransition (Transition (s10, Label (p1, p4, "G1577"), s5))
        cp.addTransition (Transition (s7, Label (p3, p1, "G1576"), s10))
        cp.addTransition (Transition (s7, Label (p6, p4, "G1575"), s9))
        cp.addTransition (Transition (s5, Label (p1, p3, "G1573"), s7))
        cp.addTransition (Transition (s5, Label (p1, p2, "G1572"), s6))
        cp.addTransition (Transition (s4, Label (p3, p6, "G1570"), s5))
        cp.addTransition (Transition (s3, Label (p3, p7, "G1569"), s4))
        cp.addTransition (Transition (s0, Label (p4, p2, "G1568"), s3))
        cp.addTransition (Transition (s0, Label (p4, p3, "G1567"), s2))
        return cp

    def cp0037(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0037", s0)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s13, Label (p3, p5, "G1597"), s14))
        cp.addTransition (Transition (s12, Label (p0, p1, "G1596"), s13))
        cp.addTransition (Transition (s9, Label (p6, p1, "G1595"), s12))
        cp.addTransition (Transition (s9, Label (p6, p2, "G1594"), s3))
        cp.addTransition (Transition (s8, Label (p0, p3, "G1592"), s9))
        cp.addTransition (Transition (s7, Label (p3, p0, "G1591"), s8))
        cp.addTransition (Transition (s4, Label (p5, p0, "G1590"), s7))
        cp.addTransition (Transition (s4, Label (p5, p3, "G1589"), s6))
        cp.addTransition (Transition (s3, Label (p6, p2, "G1587"), s4))
        cp.addTransition (Transition (s2, Label (p1, p2, "G1586"), s3))
        cp.addTransition (Transition (s1, Label (p2, p0, "G1585"), s2))
        cp.addTransition (Transition (s0, Label (p6, p5, "G1584"), s1))
        return cp

    def cp0038(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0038", s0)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s11, Label (p5, p4, "G1612"), s14))
        cp.addTransition (Transition (s11, Label (p5, p2, "G1611"), s13))
        cp.addTransition (Transition (s8, Label (p4, p1, "G1609"), s11))
        cp.addTransition (Transition (s8, Label (p4, p3, "G1608"), s10))
        cp.addTransition (Transition (s7, Label (p4, p3, "G1606"), s8))
        cp.addTransition (Transition (s6, Label (p2, p4, "G1605"), s7))
        cp.addTransition (Transition (s4, Label (p2, p4, "G1604"), s6))
        cp.addTransition (Transition (s4, Label (p2, p0, "G1602"), s2))
        cp.addTransition (Transition (s3, Label (p4, p6, "G1601"), s4))
        cp.addTransition (Transition (s2, Label (p5, p0, "G1600"), s3))
        cp.addTransition (Transition (s1, Label (p3, p4, "G1599"), s2))
        cp.addTransition (Transition (s0, Label (p0, p5, "G1598"), s1))
        return cp

    def cp0039(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0039", s0)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s13, Label (p0, p5, "G1627"), s14))
        cp.addTransition (Transition (s10, Label (p4, p3, "G1626"), s13))
        cp.addTransition (Transition (s10, Label (p4, p1, "G1625"), s11))
        cp.addTransition (Transition (s9, Label (p6, p2, "G1623"), s10))
        cp.addTransition (Transition (s7, Label (p1, p2, "G1622"), s9))
        cp.addTransition (Transition (s7, Label (p1, p3, "G1620"), s4))
        cp.addTransition (Transition (s6, Label (p6, p4, "G1619"), s7))
        cp.addTransition (Transition (s5, Label (p3, p1, "G1618"), s6))
        cp.addTransition (Transition (s4, Label (p5, p0, "G1617"), s5))
        cp.addTransition (Transition (s1, Label (p6, p0, "G1616"), s4))
        cp.addTransition (Transition (s1, Label (p3, p1, "G1615"), s3))
        cp.addTransition (Transition (s0, Label (p1, p2, "G1613"), s1))
        return cp

    def cp0040(self):
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0040", s0)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s7, Label (p3, p5, "G1635"), s8))
        cp.addTransition (Transition (s6, Label (p3, p4, "G1634"), s7))
        cp.addTransition (Transition (s5, Label (p2, p1, "G1633"), s6))
        cp.addTransition (Transition (s4, Label (p5, p2, "G1632"), s5))
        cp.addTransition (Transition (s3, Label (p5, p2, "G1631"), s4))
        cp.addTransition (Transition (s0, Label (p0, p5, "G1630"), s3))
        cp.addTransition (Transition (s0, Label (p0, p1, "G1629"), s2))
        return cp

    def cp0041(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0041", s0)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s7, Label (p4, p2, "G1643"), s8))
        cp.addTransition (Transition (s6, Label (p3, p5, "G1642"), s7))
        cp.addTransition (Transition (s5, Label (p8, p2, "G1641"), s6))
        cp.addTransition (Transition (s4, Label (p1, p7, "G1640"), s5))
        cp.addTransition (Transition (s1, Label (p2, p4, "G1639"), s4))
        cp.addTransition (Transition (s1, Label (p2, p1, "G1638"), s3))
        cp.addTransition (Transition (s0, Label (p7, p1, "G1636"), s1))
        return cp

    def cp0042(self):
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0042", s0)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s14, Label (p0, p1, "G1658"), s15))
        cp.addTransition (Transition (s13, Label (p0, p3, "G1657"), s14))
        cp.addTransition (Transition (s10, Label (p2, p1, "G1656"), s13))
        cp.addTransition (Transition (s10, Label (p2, p3, "G1655"), s12))
        cp.addTransition (Transition (s7, Label (p2, p0, "G1653"), s10))
        cp.addTransition (Transition (s7, Label (p3, p1, "G1652"), s9))
        cp.addTransition (Transition (s6, Label (p3, p2, "G1650"), s7))
        cp.addTransition (Transition (s5, Label (p1, p2, "G1649"), s6))
        cp.addTransition (Transition (s2, Label (p1, p0, "G1648"), s5))
        cp.addTransition (Transition (s2, Label (p1, p3, "G1647"), s4))
        cp.addTransition (Transition (s1, Label (p3, p1, "G1645"), s2))
        cp.addTransition (Transition (s0, Label (p1, p3, "G1644"), s1))
        return cp

    def cp0043(self):
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0043", s0)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s7, Label (p9, p0, "G1668"), s1))
        cp.addTransition (Transition (s5, Label (p6, p5, "G1667"), s7))
        cp.addTransition (Transition (s5, Label (p6, p9, "G1665"), s4))
        cp.addTransition (Transition (s4, Label (p3, p6, "G1664"), s5))
        cp.addTransition (Transition (s2, Label (p7, p0, "G1663"), s4))
        cp.addTransition (Transition (s2, Label (p7, p4, "G1661"), s0))
        cp.addTransition (Transition (s1, Label (p4, p2, "G1660"), s2))
        cp.addTransition (Transition (s0, Label (p2, p3, "G1659"), s1))
        return cp

    def cp0044(self):
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0044", s0)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s9, Label (p2, p1, "G1680"), s12))
        cp.addTransition (Transition (s9, Label (p2, p0, "G1679"), s11))
        cp.addTransition (Transition (s8, Label (p3, p0, "G1677"), s9))
        cp.addTransition (Transition (s7, Label (p1, p2, "G1676"), s8))
        cp.addTransition (Transition (s4, Label (p3, p4, "G1675"), s7))
        cp.addTransition (Transition (s4, Label (p3, p1, "G1674"), s3))
        cp.addTransition (Transition (s1, Label (p1, p2, "G1672"), s4))
        cp.addTransition (Transition (s1, Label (p4, p0, "G1671"), s3))
        cp.addTransition (Transition (s0, Label (p2, p1, "G1669"), s1))
        return cp

    def cp0045(self):
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0045", s0)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s18, Label (p5, p4, "G1700"), s19))
        cp.addTransition (Transition (s16, Label (p5, p0, "G1699"), s18))
        cp.addTransition (Transition (s16, Label (p5, p4, "G1698"), s17))
        cp.addTransition (Transition (s13, Label (p0, p1, "G1696"), s16))
        cp.addTransition (Transition (s13, Label (p2, p4, "G1695"), s15))
        cp.addTransition (Transition (s12, Label (p4, p3, "G1693"), s13))
        cp.addTransition (Transition (s9, Label (p3, p5, "G1692"), s12))
        cp.addTransition (Transition (s9, Label (p3, p0, "G1691"), s10))
        cp.addTransition (Transition (s8, Label (p3, p2, "G1689"), s9))
        cp.addTransition (Transition (s5, Label (p3, p2, "G1688"), s8))
        cp.addTransition (Transition (s5, Label (p3, p4, "G1687"), s0))
        cp.addTransition (Transition (s4, Label (p3, p1, "G1685"), s5))
        cp.addTransition (Transition (s3, Label (p5, p3, "G1684"), s4))
        cp.addTransition (Transition (s0, Label (p3, p1, "G1683"), s3))
        cp.addTransition (Transition (s0, Label (p3, p5, "G1682"), s2))
        return cp

    def cp0046(self):
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0046", s0)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s5, Label (p2, p5, "G1706"), s6))
        cp.addTransition (Transition (s4, Label (p0, p2, "G1705"), s5))
        cp.addTransition (Transition (s3, Label (p0, p2, "G1704"), s4))
        cp.addTransition (Transition (s2, Label (p4, p0, "G1703"), s3))
        cp.addTransition (Transition (s1, Label (p1, p0, "G1702"), s2))
        cp.addTransition (Transition (s0, Label (p3, p4, "G1701"), s1))
        return cp

    def cp0047(self):
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0047", s0)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s8, Label (p4, p0, "G1715"), s9))
        cp.addTransition (Transition (s7, Label (p0, p6, "G1714"), s8))
        cp.addTransition (Transition (s6, Label (p3, p0, "G1713"), s7))
        cp.addTransition (Transition (s3, Label (p4, p1, "G1712"), s6))
        cp.addTransition (Transition (s3, Label (p4, p3, "G1711"), s5))
        cp.addTransition (Transition (s2, Label (p6, p3, "G1709"), s3))
        cp.addTransition (Transition (s1, Label (p5, p4, "G1708"), s2))
        cp.addTransition (Transition (s0, Label (p3, p4, "G1707"), s1))
        return cp

    def cp0048(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0048", s0)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s11, Label (p5, p6, "G1727"), s5))
        cp.addTransition (Transition (s10, Label (p0, p4, "G1726"), s11))
        cp.addTransition (Transition (s9, Label (p5, p0, "G1725"), s10))
        cp.addTransition (Transition (s8, Label (p5, p0, "G1724"), s9))
        cp.addTransition (Transition (s7, Label (p6, p0, "G1723"), s8))
        cp.addTransition (Transition (s4, Label (p4, p6, "G1722"), s7))
        cp.addTransition (Transition (s4, Label (p4, p1, "G1721"), s0))
        cp.addTransition (Transition (s3, Label (p3, p2, "G1719"), s4))
        cp.addTransition (Transition (s0, Label (p1, p0, "G1718"), s3))
        cp.addTransition (Transition (s0, Label (p1, p5, "G1717"), s2))
        return cp

    def cp0049(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0049", s0)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s4, Label (p0, p2, "G1734"), s6))
        cp.addTransition (Transition (s4, Label (p0, p2, "G1732"), s2))
        cp.addTransition (Transition (s1, Label (p0, p5, "G1731"), s4))
        cp.addTransition (Transition (s1, Label (p0, p3, "G1730"), s3))
        cp.addTransition (Transition (s0, Label (p2, p4, "G1728"), s1))
        return cp

    def cp0050(self):
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0050", s0)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s12, Label (p3, p1, "G1748"), s13))
        cp.addTransition (Transition (s9, Label (p0, p1, "G1747"), s12))
        cp.addTransition (Transition (s9, Label (p0, p2, "G1746"), s11))
        cp.addTransition (Transition (s8, Label (p4, p2, "G1744"), s9))
        cp.addTransition (Transition (s7, Label (p4, p3, "G1743"), s8))
        cp.addTransition (Transition (s5, Label (p0, p2, "G1742"), s7))
        cp.addTransition (Transition (s5, Label (p0, p1, "G1740"), s3))
        cp.addTransition (Transition (s4, Label (p3, p4, "G1739"), s5))
        cp.addTransition (Transition (s1, Label (p4, p0, "G1738"), s4))
        cp.addTransition (Transition (s1, Label (p1, p2, "G1737"), s3))
        cp.addTransition (Transition (s0, Label (p0, p2, "G1735"), s1))
        return cp


    # Meriem
    # ts tsc11
    def cp0051(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")
        st6 = State("6")
        st7 = State("7")
        st8 = State("8")


        cp = CP("cp0051", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)
        cp.addState(st6)
        cp.addState(st7)
        cp.addState(st8)

        c = Peer("Customer")
        ts = Peer("TrainStation")
        b = Peer("Booking")
        a = Peer("Availability")


        cp.addTransition(Transition (st0, Label (c, ts, "request"), st1))
        cp.addTransition(Transition (st1, Label (ts, a, "info"), st2))
        cp.addTransition(Transition (st2, Label (a, ts, "infoAvail"), st3))
        cp.addTransition(Transition (st3, Label (a, ts, "itinerary"), st3))

        cp.addTransition(Transition (st3, Label (ts, b, "book"), st4))

        cp.addTransition(Transition (st4, Label (b, ts, "ack"), st5))
        cp.addTransition(Transition (st5, Label (ts, c, "result1"), st6))
        cp.addTransition(Transition (st6, Label (b, c, "invoice1"), st7))

        cp.addTransition(Transition (st5, Label (b, c, "invoice2"), st8))
        cp.addTransition(Transition (st8, Label (ts, c, "result2"), st7))

        return cp

    # ex2, fig3 tsc11
    def cp0052(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")


        cp = CP("cp0052", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)

        a = Peer("A")
        b = Peer("B")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (a, b, "request"), st1))
        cp.addTransition(Transition (st1, Label (c, a, "update"), st2))
        return cp

    #ex popl12, fig1
    def cp0053(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")


        cp = CP("cp0053", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)

        r1 = Peer("Role1")
        r2 = Peer("Role2")

        cp.addTransition(Transition (st0, Label (r1, r2, "o11"), st1))
        cp.addTransition(Transition (st1, Label (r2, r1, "o2"), st2))
        cp.addTransition(Transition (st2, Label (r1, r2, "o12"), st1))

        cp.addTransition(Transition (st1, Label (r2, r1, "a2"), st3))
        cp.addTransition(Transition (st2, Label (r1, r2, "a1"), st3))

        cp.addTransition(Transition (st1, Label (r2, r1, "c2"), st4))
        cp.addTransition(Transition (st2, Label (r1, r2, "c1"), st4))
        return cp

    #ex popl12, fig2
    def cp0054(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")


        cp = CP("cp0054", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)

        s = Peer("S")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (s, c, "success"), st1))

        cp.addTransition(Transition (st1, Label (c, s, "getKey"), st2))
        cp.addTransition(Transition (st1, Label (c, s, "polKey"), st3))

        cp.addTransition(Transition (st3, Label (s, c, "ackKey1"), st1))
        cp.addTransition(Transition (st3, Label (s, c, "nakKey1"), st1))

        cp.addTransition(Transition (st2, Label (s, c, "ackKey2"), st1))
        cp.addTransition(Transition (st2, Label (s, c, "nakKey2"), st1))
        return cp

    #ex fse10, fig1.1
    def cp0055(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")


        cp = CP("cp0055", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)

        s = Peer("S")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (c, s, "s"), st1))
        cp.addTransition(Transition (st1, Label (c, s, "c"), st2))
        cp.addTransition(Transition (st1, Label (s, c, "f1"), st3))
        cp.addTransition(Transition (st2, Label (s, c, "f2"), st4))
        return cp

    #ex fse10, fig1.2
    def cp0056(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")


        cp = CP("cp0056", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)

        s = Peer("S")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (c, s, "s"), st1))
        cp.addTransition(Transition (st1, Label (c, s, "c1"), st2))
        cp.addTransition(Transition (st1, Label (s, c, "f1"), st3))
        cp.addTransition(Transition (st3, Label (s, c, "c2"), st4))
        cp.addTransition(Transition (st2, Label (s, c, "f2"), st4))
        return cp

    #ex fse10, fig8
    def cp0057(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")


        cp = CP("cp0057", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)

        s = Peer("S")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (c, s, "send"), st1))
        cp.addTransition(Transition (st0, Label (c, s, "getTmpStatus1"), st2))


        cp.addTransition(Transition (st2, Label (s, c, "tmpStatus1"), st0))


        cp.addTransition(Transition (st3, Label (s, c, "sendComplete1"), st0))
        cp.addTransition(Transition (st5, Label (s, c, "tmpStatus2"), st0))


        cp.addTransition(Transition (st3, Label (c, s, "getTmpStatus2"), st4))
        cp.addTransition(Transition (st4, Label (s, c, "sendComplete2"), st5))
        cp.addTransition(Transition (st4, Label (s, c, "tmpStatus3"), st3))

        cp.addTransition(Transition (st3, Label (s, c, "ackStartSend"), st1))
        return cp

    #ex fse10, fig8, not realizable
    def cp0058(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        #st5 = State("5")


        cp = CP("cp0058", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        #cp.addState(st5)

        s = Peer("S")
        c = Peer("C")

        cp.addTransition(Transition (st0, Label (c, s, "send"), st1))
        cp.addTransition(Transition (st0, Label (c, s, "getTmpStatus1"), st2))


        cp.addTransition(Transition (st2, Label (s, c, "tmpStatus1"), st0))


        cp.addTransition(Transition (st3, Label (s, c, "sendComplete"), st0))
        #cp.addTransition(Transition (st5, Label (s, c, "tmpStatus"), st0))


        cp.addTransition(Transition (st3, Label (c, s, "getTmpStatus2"), st4))
        #cp.addTransition(Transition (st4, Label (s, c, "sendComplete"), st5))
        cp.addTransition(Transition (st4, Label (s, c, "tmpStatus2"), st3))

        cp.addTransition(Transition (st3, Label (s, c, "ackStartSend"), st1))
        return cp


    #ex fse10, fig9
    def cp0059(self):
        st0 = State("0")
        st1 = State("1")
        st2 = State("2")
        st3 = State("3")
        st4 = State("4")
        st5 = State("5")


        cp = CP("cp0059", st0)

        cp.addState(st0)
        cp.addState(st1)
        cp.addState(st2)
        cp.addState(st3)
        cp.addState(st4)
        cp.addState(st5)

        ag = Peer("Agency")
        ai = Peer("Airline")

        cp.addTransition(Transition (st0, Label (ag, ai, "askQuote"), st1))
        cp.addTransition(Transition (st1, Label (ai, ag, "timeout1"), st2))


        cp.addTransition(Transition (st1, Label (ag, ai, "orderTickets"), st3))


        cp.addTransition(Transition (st2, Label (ag, ai, "orderTickets"), st4))
        cp.addTransition(Transition (st4, Label (ai, ag, "rejected"), st1))


        cp.addTransition(Transition (st3, Label (ai, ag, "accepted"), st5))
        cp.addTransition(Transition (st3, Label (ai, ag, "timeout2"), st4))
        return cp


    # simple example for sync and choice afterwards ... (disconnected)
    def cp0060(self):
        s0 = State ("s0")
        s1 = State ("s1")

        cp = CP("cp0060", s0)
        cp.addState(s0)
        cp.addState(s1)

        p0 = Peer ("p0")
        p1 = Peer ("p1")
        p2 = Peer ("p2")
        p3 = Peer ("p3")
        p4 = Peer ("p4")

        cp.addTransition(Transition(s0, Label(p0,p1,"m1"), s1))
        cp.addTransition(Transition(s1, Label (p2,p3,"m2"), s0))
        cp.addTransition(Transition (s1, Label (p2,p4,"m3"), s0))

        return cp


    def cp0061(self):
        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")

        p0 = Peer ("p0")
        p1 = Peer ("p1")

        cp = CP("cp0061", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(p0, p1, "m1"), s1))
        cp.addTransition(Transition(s0, Label(p1, p0, "m2"), s2))
        cp.addTransition(Transition(s1, Label(p1, p0, "m3"), s3))
        cp.addTransition(Transition(s2, Label(p0, p1, "m4"), s3))

        return cp


    def cp0062(self):
        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")
        s4 = State("s4")

        p0 = Peer ("p0")
        p1 = Peer ("p1")

        cp = CP("cp0062", s4)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s4, Label(p0, p1, "m0"), s0))
        cp.addTransition(Transition(s0, Label(p0, p1, "m1"), s1))
        cp.addTransition(Transition(s0, Label(p1, p0, "m2"), s2))
        cp.addTransition(Transition(s1, Label(p1, p0, "m3"), s3))
        cp.addTransition(Transition(s2, Label(p0, p1, "m4"), s3))

        return cp

    # example CAV2012
    # s0 - connect^(cl,int) -> s1
    #s1 - setup^(int,appli) -> s2
    #s2 - access^(cl,appli) -> s2
    #s2 - logout^(cl,int) -> s3
    #s3 - log^(appli,db) -> s0
    def cp0063(self):

        s0 = State ("s0")
        s1 = State ("s1")
        s2 = State ("s2")
        s3 = State ("s3")

        cl = Peer("cl")
        INT = Peer("INT")
        appli = Peer("appli")
        db = Peer("db")

        cp = CP("CAV2012", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)

        cp.addTransition(Transition(s0, Label(cl, INT, "connect"), s1))
        cp.addTransition(Transition(s1, Label(INT, appli, "setup"), s2))
        cp.addTransition(Transition(s2, Label(cl, appli, "access"), s2))
        cp.addTransition(Transition(s2, Label(cl, INT, "logout"), s3))
        cp.addTransition(Transition(s3, Label(appli, db, "log"), s0))

        return cp

    def cp0064(self):

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp = CP("strange", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)

        cp.addTransition(Transition(s0,Label(p1,p3,"a"),s1))
        cp.addTransition(Transition(s1,Label(p2,p3,"b"),s2))

        return cp

    def cp0065(self):

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp = CP("cp0121", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)

        cp.addTransition(Transition (s0, Label (p1,p2,"init"),s1))

        # loop 1
        cp.addTransition(Transition (s1, Label(p1,p2,"acc1"),s1))
        cp.addTransition(Transition (s1, Label(p1,p2,"acc2"),s1))
        cp.addTransition(Transition (s1, Label(p1,p2,"acc3"),s1))

        cp.addTransition(Transition (s1, Label(p1,p3,"switch1"),s2))

        cp.addTransition(Transition (s2, Label(p1,p2,"switch2"),s3))

        cp.addTransition(Transition (s2, Label(p1,p3,"switch3"),s4))

        # loop 2
        cp.addTransition(Transition (s3, Label(p1,p2,"acc4"),s3))
        cp.addTransition(Transition (s3, Label(p1,p2,"acc5"),s3))
        cp.addTransition(Transition (s3, Label(p1,p2,"acc6"),s3))

        cp.addTransition(Transition (s3, Label(p1,p3,"switch4"),s4))

        # loop 3
        cp.addTransition(Transition (s4, Label(p1,p2,"acc7"),s4))
        cp.addTransition(Transition (s4, Label(p1,p2,"acc8"),s4))
        cp.addTransition(Transition (s4, Label(p1,p2,"acc9"),s4))

        cp.addTransition(Transition (s4, Label (p1,p2,"switch5"),s5))
        cp.addTransition(Transition (s5, Label(p2,p3,"save1"),s6))
        cp.addTransition(Transition (s6, Label(p2,p1,"return"),s0))
        cp.addTransition(Transition (s6, Label(p2,p1,"finish"),s7))

        return cp

    def cp0066(self):

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("s9")
        s10 = State("s10")
        s11 = State("s11")
        s12 = State("s12")
        s13 = State("s13")
        s14 = State("s14")
        s15 = State("s15")

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        cp = CP("cp0153", s0)
        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)
        cp.addState(s11)
        cp.addState(s12)
        cp.addState(s13)
        cp.addState(s14)
        cp.addState(s15)

        cp.addTransition(Transition (s0, Label (p1,p2,"init"),s1))

        # loop 1
        cp.addTransition(Transition (s1, Label(p1,p2,"acc1"),s1))

        cp.addTransition(Transition (s1, Label(p1,p3,"switch1"),s2))

        cp.addTransition(Transition (s2, Label(p1,p2,"switch2"),s3))

        cp.addTransition(Transition (s2, Label(p1,p3,"switch3"),s4))

        # loop 2
        cp.addTransition(Transition (s3, Label(p1,p2,"acc4"),s3))

        cp.addTransition(Transition (s3, Label(p1,p3,"switch4"),s4))

        # loop 3
        cp.addTransition(Transition (s4, Label(p1,p2,"acc6"),s4))
        cp.addTransition(Transition (s4, Label(p1,p3,"acc7"),s4))

        cp.addTransition(Transition (s4, Label (p1,p2,"switch5"),s5))
        cp.addTransition(Transition (s5, Label(p2,p3,"save1"),s6))
        cp.addTransition(Transition (s6, Label(p2,p1,"return"),s0))
        cp.addTransition(Transition (s6, Label(p2,p1,"finish"),s7))

        # begin larger loop
        cp.addTransition(Transition(s7, Label(p1,p3,"break"),s8))

        cp.addTransition(Transition(s8, Label (p2,p3,"acc8"),s8))
        cp.addTransition(Transition(s8, Label (p2,p1,"acc9"),s8))

        cp.addTransition(Transition(s8, Label (p2,p1,"switch6"),s9))
        cp.addTransition(Transition(s9, Label (p1,p3,"acc10"),s9))
        cp.addTransition(Transition(s9, Label (p1,p3,"switch7"),s10))

        cp.addTransition(Transition(s10, Label(p2,p3,"zurueck"),s2))

        cp.addTransition(Transition(s10, Label(p2,p3,"finish2"),s11))

        # cp.addTransition(Transition(s8, Label(p2,p3,"shortcut"),s10))

        # begin inner loop

        cp.addTransition(Transition(s8, Label(p2,p3,"inter"),s12))
        cp.addTransition(Transition(s12, Label(p1,p2,"schleife"),s12))
        cp.addTransition(Transition(s12, Label(p1,p2,"leave"),s13))
        cp.addTransition(Transition(s13, Label(p3,p1,"goto"),s14))
        cp.addTransition(Transition(s14, Label(p2,p3,"boucle"),s14))
        cp.addTransition(Transition(s14, Label(p2,p1,"ende"),s15))

        cp.addTransition(Transition(s14, Label(p2,p3,"future"),s7))

        return cp


    def cp0100(self):

        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s71 = State("s71")
        s70 = State("s70")
        s69 = State("s69")
        s68 = State("s68")
        s67 = State("s67")
        s66 = State("s66")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0100", s0)
        cp.addState(s71)
        cp.addState(s70)
        cp.addState(s69)
        cp.addState(s68)
        cp.addState(s67)
        cp.addState(s66)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s71, Label (p0, p1, "G262"), s42))
        cp.addTransition (Transition (s69, Label (p1, p0, "G261"), s71))
        cp.addTransition (Transition (s69, Label (p1, p2, "G259"), s21))
        cp.addTransition (Transition (s66, Label (p2, p4, "G258"), s69))
        cp.addTransition (Transition (s66, Label (p2, p5, "G257"), s68))
        cp.addTransition (Transition (s65, Label (p0, p5, "G255"), s66))
        cp.addTransition (Transition (s64, Label (p2, p3, "G254"), s65))
        cp.addTransition (Transition (s63, Label (p3, p0, "G253"), s64))
        cp.addTransition (Transition (s62, Label (p4, p5, "G252"), s63))
        cp.addTransition (Transition (s61, Label (p4, p2, "G251"), s62))
        cp.addTransition (Transition (s58, Label (p4, p3, "G250"), s61))
        cp.addTransition (Transition (s58, Label (p4, p0, "G249"), s60))
        cp.addTransition (Transition (s55, Label (p4, p2, "G247"), s58))
        cp.addTransition (Transition (s55, Label (p4, p3, "G246"), s57))
        cp.addTransition (Transition (s54, Label (p2, p1, "G244"), s55))
        cp.addTransition (Transition (s53, Label (p4, p3, "G243"), s54))
        cp.addTransition (Transition (s52, Label (p3, p5, "G242"), s53))
        cp.addTransition (Transition (s51, Label (p2, p0, "G241"), s52))
        cp.addTransition (Transition (s49, Label (p3, p0, "G240"), s51))
        cp.addTransition (Transition (s49, Label (p3, p2, "G238"), s27))
        cp.addTransition (Transition (s48, Label (p4, p0, "G237"), s49))
        cp.addTransition (Transition (s47, Label (p5, p4, "G236"), s48))
        cp.addTransition (Transition (s44, Label (p5, p2, "G235"), s47))
        cp.addTransition (Transition (s44, Label (p5, p4, "G234"), s0))
        cp.addTransition (Transition (s41, Label (p0, p4, "G232"), s44))
        cp.addTransition (Transition (s41, Label (p0, p5, "G231"), s43))
        cp.addTransition (Transition (s40, Label (p0, p2, "G229"), s41))
        cp.addTransition (Transition (s38, Label (p5, p0, "G228"), s40))
        cp.addTransition (Transition (s38, Label (p5, p4, "G226"), s32))
        cp.addTransition (Transition (s36, Label (p5, p0, "G225"), s38))
        cp.addTransition (Transition (s36, Label (p5, p2, "G223"), s15))
        cp.addTransition (Transition (s35, Label (p5, p1, "G222"), s36))
        cp.addTransition (Transition (s34, Label (p3, p4, "G221"), s35))
        cp.addTransition (Transition (s31, Label (p3, p4, "G220"), s34))
        cp.addTransition (Transition (s31, Label (p3, p2, "G219"), s33))
        cp.addTransition (Transition (s28, Label (p1, p2, "G217"), s31))
        cp.addTransition (Transition (s28, Label (p1, p3, "G216"), s22))
        cp.addTransition (Transition (s25, Label (p4, p3, "G214"), s28))
        cp.addTransition (Transition (s25, Label (p4, p1, "G213"), s27))
        cp.addTransition (Transition (s24, Label (p2, p5, "G211"), s25))
        cp.addTransition (Transition (s23, Label (p5, p3, "G210"), s24))
        cp.addTransition (Transition (s22, Label (p3, p0, "G209"), s23))
        cp.addTransition (Transition (s21, Label (p3, p1, "G208"), s22))
        cp.addTransition (Transition (s19, Label (p3, p4, "G207"), s21))
        cp.addTransition (Transition (s19, Label (p3, p2, "G205"), s7))
        cp.addTransition (Transition (s18, Label (p4, p5, "G204"), s19))
        cp.addTransition (Transition (s15, Label (p1, p0, "G203"), s18))
        cp.addTransition (Transition (s15, Label (p1, p4, "G202"), s17))
        cp.addTransition (Transition (s12, Label (p4, p1, "G200"), s15))
        cp.addTransition (Transition (s12, Label (p4, p3, "G199"), s14))
        cp.addTransition (Transition (s11, Label (p2, p1, "G197"), s12))
        cp.addTransition (Transition (s10, Label (p4, p0, "G196"), s11))
        cp.addTransition (Transition (s9, Label (p3, p1, "G195"), s10))
        cp.addTransition (Transition (s8, Label (p0, p3, "G194"), s9))
        cp.addTransition (Transition (s7, Label (p3, p1, "G193"), s8))
        cp.addTransition (Transition (s6, Label (p1, p4, "G192"), s7))
        cp.addTransition (Transition (s5, Label (p5, p4, "G191"), s6))
        cp.addTransition (Transition (s4, Label (p2, p4, "G190"), s5))
        cp.addTransition (Transition (s1, Label (p2, p0, "G189"), s4))
        cp.addTransition (Transition (s1, Label (p2, p4, "G188"), s2))
        cp.addTransition (Transition (s0, Label (p5, p2, "G186"), s1))
        return cp

    def cp0101(self):
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0101", s0)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s53, Label (p1, p12, "G317"), s54))
        cp.addTransition (Transition (s52, Label (p3, p1, "G316"), s53))
        cp.addTransition (Transition (s49, Label (p3, p2, "G315"), s52))
        cp.addTransition (Transition (s49, Label (p3, p0, "G314"), s51))
        cp.addTransition (Transition (s48, Label (p0, p4, "G312"), s49))
        cp.addTransition (Transition (s47, Label (p7, p4, "G311"), s48))
        cp.addTransition (Transition (s46, Label (p8, p10, "G310"), s47))
        cp.addTransition (Transition (s43, Label (p4, p7, "G309"), s46))
        cp.addTransition (Transition (s43, Label (p4, p0, "G308"), s45))
        cp.addTransition (Transition (s42, Label (p0, p1, "G306"), s43))
        cp.addTransition (Transition (s39, Label (p10, p7, "G305"), s42))
        cp.addTransition (Transition (s39, Label (p10, p6, "G304"), s41))
        cp.addTransition (Transition (s38, Label (p3, p12, "G302"), s39))
        cp.addTransition (Transition (s37, Label (p6, p2, "G301"), s38))
        cp.addTransition (Transition (s36, Label (p9, p2, "G300"), s37))
        cp.addTransition (Transition (s33, Label (p6, p9, "G299"), s36))
        cp.addTransition (Transition (s33, Label (p6, p5, "G298"), s35))
        cp.addTransition (Transition (s30, Label (p0, p11, "G296"), s33))
        cp.addTransition (Transition (s30, Label (p0, p3, "G295"), s32))
        cp.addTransition (Transition (s29, Label (p8, p4, "G293"), s30))
        cp.addTransition (Transition (s26, Label (p1, p12, "G292"), s29))
        cp.addTransition (Transition (s26, Label (p1, p6, "G291"), s15))
        cp.addTransition (Transition (s23, Label (p6, p4, "G289"), s26))
        cp.addTransition (Transition (s23, Label (p6, p11, "G288"), s25))
        cp.addTransition (Transition (s22, Label (p4, p11, "G286"), s23))
        cp.addTransition (Transition (s20, Label (p1, p8, "G285"), s22))
        cp.addTransition (Transition (s20, Label (p1, p3, "G284"), s21))
        cp.addTransition (Transition (s19, Label (p6, p1, "G282"), s20))
        cp.addTransition (Transition (s16, Label (p1, p12, "G281"), s19))
        cp.addTransition (Transition (s16, Label (p1, p6, "G280"), s18))
        cp.addTransition (Transition (s15, Label (p4, p1, "G278"), s16))
        cp.addTransition (Transition (s12, Label (p11, p8, "G277"), s15))
        cp.addTransition (Transition (s12, Label (p11, p2, "G276"), s14))
        cp.addTransition (Transition (s11, Label (p0, p10, "G274"), s12))
        cp.addTransition (Transition (s10, Label (p0, p4, "G273"), s11))
        cp.addTransition (Transition (s9, Label (p8, p7, "G272"), s10))
        cp.addTransition (Transition (s8, Label (p6, p2, "G271"), s9))
        cp.addTransition (Transition (s7, Label (p7, p9, "G270"), s8))
        cp.addTransition (Transition (s6, Label (p12, p4, "G269"), s7))
        cp.addTransition (Transition (s3, Label (p7, p0, "G268"), s6))
        cp.addTransition (Transition (s3, Label (p7, p1, "G267"), s5))
        cp.addTransition (Transition (s2, Label (p5, p12, "G265"), s3))
        cp.addTransition (Transition (s1, Label (p1, p0, "G264"), s2))
        cp.addTransition (Transition (s0, Label (p12, p10, "G263"), s1))
        return cp

    def cp0102(self):
        p14 = Peer("p14")
        p13 = Peer("p13")
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0102", s0)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s19, Label (p10, p5, "G340"), s21))
        cp.addTransition (Transition (s19, Label (p10, p13, "G339"), s20))
        cp.addTransition (Transition (s18, Label (p9, p6, "G337"), s19))
        cp.addTransition (Transition (s15, Label (p6, p3, "G336"), s18))
        cp.addTransition (Transition (s15, Label (p6, p9, "G335"), s17))
        cp.addTransition (Transition (s14, Label (p7, p3, "G333"), s15))
        cp.addTransition (Transition (s13, Label (p5, p2, "G332"), s14))
        cp.addTransition (Transition (s10, Label (p5, p0, "G331"), s13))
        cp.addTransition (Transition (s10, Label (p5, p6, "G330"), s12))
        cp.addTransition (Transition (s9, Label (p7, p0, "G328"), s10))
        cp.addTransition (Transition (s8, Label (p5, p13, "G327"), s9))
        cp.addTransition (Transition (s7, Label (p7, p0, "G326"), s8))
        cp.addTransition (Transition (s6, Label (p0, p13, "G325"), s7))
        cp.addTransition (Transition (s5, Label (p0, p12, "G324"), s6))
        cp.addTransition (Transition (s3, Label (p13, p2, "G323"), s5))
        cp.addTransition (Transition (s3, Label (p13, p6, "G321"), s2))
        cp.addTransition (Transition (s0, Label (p12, p1, "G320"), s3))
        cp.addTransition (Transition (s0, Label (p12, p5, "G319"), s2))
        return cp

    def cp0103(self):
        p14 = Peer("p14")
        p13 = Peer("p13")
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0103", s0)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s63, Label (p9, p3, "G408"), s64))
        cp.addTransition (Transition (s60, Label (p8, p3, "G407"), s63))
        cp.addTransition (Transition (s60, Label (p8, p12, "G406"), s35))
        cp.addTransition (Transition (s58, Label (p5, p4, "G404"), s60))
        cp.addTransition (Transition (s58, Label (p5, p10, "G402"), s8))
        cp.addTransition (Transition (s55, Label (p12, p5, "G401"), s58))
        cp.addTransition (Transition (s55, Label (p12, p11, "G400"), s57))
        cp.addTransition (Transition (s52, Label (p6, p5, "G398"), s55))
        cp.addTransition (Transition (s52, Label (p6, p9, "G397"), s54))
        cp.addTransition (Transition (s50, Label (p11, p12, "G395"), s52))
        cp.addTransition (Transition (s50, Label (p11, p14, "G393"), s16))
        cp.addTransition (Transition (s47, Label (p14, p7, "G392"), s50))
        cp.addTransition (Transition (s47, Label (p14, p2, "G391"), s49))
        cp.addTransition (Transition (s46, Label (p0, p13, "G389"), s47))
        cp.addTransition (Transition (s45, Label (p4, p1, "G388"), s46))
        cp.addTransition (Transition (s42, Label (p5, p3, "G387"), s45))
        cp.addTransition (Transition (s42, Label (p5, p0, "G386"), s5))
        cp.addTransition (Transition (s41, Label (p9, p7, "G384"), s42))
        cp.addTransition (Transition (s38, Label (p9, p1, "G383"), s41))
        cp.addTransition (Transition (s38, Label (p9, p2, "G382"), s22))
        cp.addTransition (Transition (s35, Label (p0, p8, "G380"), s38))
        cp.addTransition (Transition (s35, Label (p0, p3, "G379"), s37))
        cp.addTransition (Transition (s34, Label (p13, p14, "G377"), s35))
        cp.addTransition (Transition (s33, Label (p4, p11, "G376"), s34))
        cp.addTransition (Transition (s30, Label (p3, p11, "G375"), s33))
        cp.addTransition (Transition (s30, Label (p3, p1, "G374"), s5))
        cp.addTransition (Transition (s28, Label (p7, p2, "G372"), s30))
        cp.addTransition (Transition (s28, Label (p7, p10, "G370"), s10))
        cp.addTransition (Transition (s27, Label (p0, p3, "G369"), s28))
        cp.addTransition (Transition (s24, Label (p10, p4, "G368"), s27))
        cp.addTransition (Transition (s24, Label (p10, p11, "G367"), s26))
        cp.addTransition (Transition (s21, Label (p2, p9, "G365"), s24))
        cp.addTransition (Transition (s21, Label (p2, p6, "G364"), s23))
        cp.addTransition (Transition (s20, Label (p5, p14, "G362"), s21))
        cp.addTransition (Transition (s17, Label (p12, p10, "G361"), s20))
        cp.addTransition (Transition (s17, Label (p12, p9, "G360"), s19))
        cp.addTransition (Transition (s16, Label (p8, p2, "G358"), s17))
        cp.addTransition (Transition (s15, Label (p13, p9, "G357"), s16))
        cp.addTransition (Transition (s14, Label (p11, p2, "G356"), s15))
        cp.addTransition (Transition (s12, Label (p1, p5, "G355"), s14))
        cp.addTransition (Transition (s12, Label (p1, p12, "G353"), s8))
        cp.addTransition (Transition (s9, Label (p12, p0, "G352"), s12))
        cp.addTransition (Transition (s9, Label (p12, p13, "G351"), s11))
        cp.addTransition (Transition (s8, Label (p11, p7, "G349"), s9))
        cp.addTransition (Transition (s5, Label (p6, p5, "G348"), s8))
        cp.addTransition (Transition (s5, Label (p6, p12, "G347"), s7))
        cp.addTransition (Transition (s4, Label (p3, p13, "G345"), s5))
        cp.addTransition (Transition (s3, Label (p9, p8, "G344"), s4))
        cp.addTransition (Transition (s0, Label (p2, p7, "G343"), s3))
        cp.addTransition (Transition (s0, Label (p2, p4, "G342"), s2))
        return cp

    def cp0104(self):
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0104", s0)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s62, Label (p4, p0, "G484"), s64))
        cp.addTransition (Transition (s62, Label (p4, p3, "G482"), s5))
        cp.addTransition (Transition (s59, Label (p4, p0, "G481"), s62))
        cp.addTransition (Transition (s59, Label (p4, p1, "G480"), s23))
        cp.addTransition (Transition (s57, Label (p1, p2, "G478"), s59))
        cp.addTransition (Transition (s57, Label (p1, p4, "G476"), s27))
        cp.addTransition (Transition (s56, Label (p3, p2, "G475"), s57))
        cp.addTransition (Transition (s55, Label (p3, p4, "G474"), s56))
        cp.addTransition (Transition (s53, Label (p2, p3, "G473"), s55))
        cp.addTransition (Transition (s53, Label (p2, p0, "G472"), s54))
        cp.addTransition (Transition (s52, Label (p0, p2, "G470"), s53))
        cp.addTransition (Transition (s50, Label (p2, p4, "G469"), s52))
        cp.addTransition (Transition (s50, Label (p2, p0, "G467"), s31))
        cp.addTransition (Transition (s49, Label (p3, p2, "G466"), s50))
        cp.addTransition (Transition (s48, Label (p0, p1, "G465"), s49))
        cp.addTransition (Transition (s47, Label (p0, p4, "G464"), s48))
        cp.addTransition (Transition (s46, Label (p1, p3, "G463"), s47))
        cp.addTransition (Transition (s45, Label (p0, p1, "G462"), s46))
        cp.addTransition (Transition (s42, Label (p4, p3, "G461"), s45))
        cp.addTransition (Transition (s42, Label (p4, p0, "G460"), s44))
        cp.addTransition (Transition (s41, Label (p3, p0, "G458"), s42))
        cp.addTransition (Transition (s39, Label (p4, p2, "G457"), s41))
        cp.addTransition (Transition (s39, Label (p4, p3, "G455"), s19))
        cp.addTransition (Transition (s38, Label (p2, p1, "G454"), s39))
        cp.addTransition (Transition (s36, Label (p3, p1, "G453"), s38))
        cp.addTransition (Transition (s36, Label (p3, p1, "G451"), s7))
        cp.addTransition (Transition (s35, Label (p2, p4, "G450"), s36))
        cp.addTransition (Transition (s34, Label (p4, p1, "G449"), s35))
        cp.addTransition (Transition (s33, Label (p4, p0, "G448"), s34))
        cp.addTransition (Transition (s32, Label (p4, p0, "G447"), s33))
        cp.addTransition (Transition (s30, Label (p1, p3, "G446"), s32))
        cp.addTransition (Transition (s30, Label (p1, p3, "G444"), s23))
        cp.addTransition (Transition (s27, Label (p3, p4, "G443"), s30))
        cp.addTransition (Transition (s27, Label (p3, p2, "G442"), s29))
        cp.addTransition (Transition (s24, Label (p2, p3, "G440"), s27))
        cp.addTransition (Transition (s24, Label (p2, p1, "G439"), s26))
        cp.addTransition (Transition (s22, Label (p2, p0, "G437"), s24))
        cp.addTransition (Transition (s22, Label (p2, p0, "G435"), s2))
        cp.addTransition (Transition (s21, Label (p0, p3, "G434"), s22))
        cp.addTransition (Transition (s19, Label (p0, p3, "G433"), s21))
        cp.addTransition (Transition (s19, Label (p0, p2, "G431"), s18))
        cp.addTransition (Transition (s18, Label (p1, p2, "G430"), s19))
        cp.addTransition (Transition (s17, Label (p3, p4, "G429"), s18))
        cp.addTransition (Transition (s15, Label (p3, p0, "G428"), s17))
        cp.addTransition (Transition (s15, Label (p3, p4, "G426"), s1))
        cp.addTransition (Transition (s13, Label (p0, p4, "G425"), s15))
        cp.addTransition (Transition (s13, Label (p0, p3, "G423"), s3))
        cp.addTransition (Transition (s12, Label (p4, p3, "G422"), s13))
        cp.addTransition (Transition (s11, Label (p1, p2, "G421"), s12))
        cp.addTransition (Transition (s8, Label (p3, p2, "G420"), s11))
        cp.addTransition (Transition (s8, Label (p3, p0, "G419"), s10))
        cp.addTransition (Transition (s6, Label (p0, p1, "G417"), s8))
        cp.addTransition (Transition (s6, Label (p0, p2, "G416"), s7))
        cp.addTransition (Transition (s5, Label (p4, p1, "G414"), s6))
        cp.addTransition (Transition (s2, Label (p3, p1, "G413"), s5))
        cp.addTransition (Transition (s2, Label (p3, p4, "G412"), s4))
        cp.addTransition (Transition (s1, Label (p4, p3, "G410"), s2))
        cp.addTransition (Transition (s0, Label (p0, p3, "G409"), s1))
        return cp

    def cp0105(self):
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0105", s0)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s18, Label (p1, p3, "G506"), s21))
        cp.addTransition (Transition (s18, Label (p1, p5, "G505"), s20))
        cp.addTransition (Transition (s17, Label (p4, p3, "G503"), s18))
        cp.addTransition (Transition (s15, Label (p4, p1, "G502"), s17))
        cp.addTransition (Transition (s15, Label (p4, p1, "G500"), s6))
        cp.addTransition (Transition (s14, Label (p5, p2, "G499"), s15))
        cp.addTransition (Transition (s13, Label (p3, p4, "G498"), s14))
        cp.addTransition (Transition (s12, Label (p0, p5, "G497"), s13))
        cp.addTransition (Transition (s9, Label (p4, p3, "G496"), s12))
        cp.addTransition (Transition (s9, Label (p4, p5, "G495"), s10))
        cp.addTransition (Transition (s8, Label (p1, p0, "G493"), s9))
        cp.addTransition (Transition (s7, Label (p5, p3, "G492"), s8))
        cp.addTransition (Transition (s6, Label (p1, p0, "G491"), s7))
        cp.addTransition (Transition (s3, Label (p2, p3, "G490"), s6))
        cp.addTransition (Transition (s3, Label (p2, p4, "G489"), s2))
        cp.addTransition (Transition (s2, Label (p2, p3, "G487"), s3))
        cp.addTransition (Transition (s1, Label (p3, p5, "G486"), s2))
        cp.addTransition (Transition (s0, Label (p3, p5, "G485"), s1))
        return cp

    def cp0106(self):
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s73 = State("s73")
        s72 = State("s72")
        s71 = State("s71")
        s70 = State("s70")
        s69 = State("s69")
        s68 = State("s68")
        s67 = State("s67")
        s66 = State("s66")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0106", s0)
        cp.addState(s73)
        cp.addState(s72)
        cp.addState(s71)
        cp.addState(s70)
        cp.addState(s69)
        cp.addState(s68)
        cp.addState(s67)
        cp.addState(s66)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s70, Label (p9, p5, "G584"), s73))
        cp.addTransition (Transition (s70, Label (p9, p2, "G583"), s22))
        cp.addTransition (Transition (s67, Label (p4, p8, "G581"), s70))
        cp.addTransition (Transition (s67, Label (p4, p3, "G580"), s69))
        cp.addTransition (Transition (s65, Label (p1, p4, "G578"), s67))
        cp.addTransition (Transition (s65, Label (p1, p6, "G576"), s61))
        cp.addTransition (Transition (s64, Label (p9, p2, "G575"), s65))
        cp.addTransition (Transition (s61, Label (p8, p3, "G574"), s64))
        cp.addTransition (Transition (s61, Label (p8, p2, "G573"), s63))
        cp.addTransition (Transition (s58, Label (p4, p10, "G571"), s61))
        cp.addTransition (Transition (s58, Label (p4, p11, "G570"), s60))
        cp.addTransition (Transition (s57, Label (p2, p11, "G568"), s58))
        cp.addTransition (Transition (s54, Label (p2, p1, "G567"), s57))
        cp.addTransition (Transition (s54, Label (p2, p6, "G566"), s44))
        cp.addTransition (Transition (s53, Label (p2, p0, "G564"), s54))
        cp.addTransition (Transition (s51, Label (p2, p9, "G563"), s53))
        cp.addTransition (Transition (s51, Label (p2, p4, "G561"), s8))
        cp.addTransition (Transition (s50, Label (p7, p2, "G560"), s51))
        cp.addTransition (Transition (s49, Label (p2, p8, "G559"), s50))
        cp.addTransition (Transition (s48, Label (p2, p6, "G558"), s49))
        cp.addTransition (Transition (s46, Label (p1, p10, "G557"), s48))
        cp.addTransition (Transition (s46, Label (p1, p3, "G555"), s39))
        cp.addTransition (Transition (s45, Label (p0, p11, "G554"), s46))
        cp.addTransition (Transition (s43, Label (p3, p7, "G553"), s45))
        cp.addTransition (Transition (s43, Label (p3, p8, "G552"), s44))
        cp.addTransition (Transition (s40, Label (p5, p4, "G550"), s43))
        cp.addTransition (Transition (s40, Label (p5, p0, "G549"), s42))
        cp.addTransition (Transition (s39, Label (p3, p7, "G547"), s40))
        cp.addTransition (Transition (s38, Label (p6, p0, "G546"), s39))
        cp.addTransition (Transition (s37, Label (p9, p11, "G545"), s38))
        cp.addTransition (Transition (s34, Label (p2, p8, "G544"), s37))
        cp.addTransition (Transition (s34, Label (p2, p7, "G543"), s36))
        cp.addTransition (Transition (s31, Label (p0, p9, "G541"), s34))
        cp.addTransition (Transition (s31, Label (p0, p2, "G540"), s33))
        cp.addTransition (Transition (s30, Label (p5, p6, "G538"), s31))
        cp.addTransition (Transition (s29, Label (p7, p3, "G537"), s30))
        cp.addTransition (Transition (s28, Label (p10, p5, "G536"), s29))
        cp.addTransition (Transition (s25, Label (p3, p7, "G535"), s28))
        cp.addTransition (Transition (s25, Label (p3, p11, "G534"), s27))
        cp.addTransition (Transition (s22, Label (p0, p8, "G532"), s25))
        cp.addTransition (Transition (s22, Label (p0, p1, "G531"), s24))
        cp.addTransition (Transition (s21, Label (p10, p8, "G529"), s22))
        cp.addTransition (Transition (s18, Label (p10, p11, "G528"), s21))
        cp.addTransition (Transition (s18, Label (p10, p0, "G527"), s20))
        cp.addTransition (Transition (s17, Label (p4, p9, "G525"), s18))
        cp.addTransition (Transition (s16, Label (p7, p4, "G524"), s17))
        cp.addTransition (Transition (s13, Label (p11, p1, "G523"), s16))
        cp.addTransition (Transition (s13, Label (p11, p9, "G522"), s15))
        cp.addTransition (Transition (s12, Label (p6, p1, "G520"), s13))
        cp.addTransition (Transition (s11, Label (p8, p2, "G519"), s12))
        cp.addTransition (Transition (s8, Label (p9, p10, "G518"), s11))
        cp.addTransition (Transition (s8, Label (p9, p11, "G517"), s10))
        cp.addTransition (Transition (s6, Label (p6, p7, "G515"), s8))
        cp.addTransition (Transition (s6, Label (p6, p9, "G514"), s7))
        cp.addTransition (Transition (s5, Label (p2, p8, "G512"), s6))
        cp.addTransition (Transition (s4, Label (p6, p7, "G511"), s5))
        cp.addTransition (Transition (s3, Label (p3, p5, "G510"), s4))
        cp.addTransition (Transition (s2, Label (p11, p4, "G509"), s3))
        cp.addTransition (Transition (s1, Label (p7, p1, "G508"), s2))
        cp.addTransition (Transition (s0, Label (p2, p3, "G507"), s1))
        return cp

    def cp0107(self):
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0107", s0)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s6, Label (p3, p8, "G593"), s9))
        cp.addTransition (Transition (s6, Label (p3, p0, "G592"), s8))
        cp.addTransition (Transition (s3, Label (p0, p10, "G590"), s6))
        cp.addTransition (Transition (s3, Label (p0, p5, "G589"), s5))
        cp.addTransition (Transition (s2, Label (p2, p5, "G587"), s3))
        cp.addTransition (Transition (s1, Label (p3, p9, "G586"), s2))
        cp.addTransition (Transition (s0, Label (p3, p0, "G585"), s1))
        return cp

    def cp0108(self):
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s72 = State("s72")
        s71 = State("s71")
        s70 = State("s70")
        s69 = State("s69")
        s68 = State("s68")
        s67 = State("s67")
        s66 = State("s66")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0108", s0)
        cp.addState(s72)
        cp.addState(s71)
        cp.addState(s70)
        cp.addState(s69)
        cp.addState(s68)
        cp.addState(s67)
        cp.addState(s66)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s71, Label (p4, p8, "G673"), s72))
        cp.addTransition (Transition (s70, Label (p9, p3, "G672"), s71))
        cp.addTransition (Transition (s69, Label (p9, p4, "G671"), s70))
        cp.addTransition (Transition (s68, Label (p7, p1, "G670"), s69))
        cp.addTransition (Transition (s65, Label (p11, p9, "G669"), s68))
        cp.addTransition (Transition (s65, Label (p11, p4, "G668"), s8))
        cp.addTransition (Transition (s63, Label (p5, p4, "G666"), s65))
        cp.addTransition (Transition (s63, Label (p5, p10, "G664"), s21))
        cp.addTransition (Transition (s62, Label (p8, p0, "G663"), s63))
        cp.addTransition (Transition (s60, Label (p9, p6, "G662"), s62))
        cp.addTransition (Transition (s60, Label (p9, p2, "G660"), s48))
        cp.addTransition (Transition (s59, Label (p8, p3, "G659"), s60))
        cp.addTransition (Transition (s58, Label (p4, p3, "G658"), s59))
        cp.addTransition (Transition (s57, Label (p10, p0, "G657"), s58))
        cp.addTransition (Transition (s54, Label (p10, p9, "G656"), s57))
        cp.addTransition (Transition (s54, Label (p10, p0, "G655"), s56))
        cp.addTransition (Transition (s52, Label (p1, p4, "G653"), s54))
        cp.addTransition (Transition (s52, Label (p1, p8, "G652"), s53))
        cp.addTransition (Transition (s50, Label (p6, p3, "G650"), s52))
        cp.addTransition (Transition (s50, Label (p6, p3, "G648"), s21))
        cp.addTransition (Transition (s47, Label (p5, p9, "G647"), s50))
        cp.addTransition (Transition (s47, Label (p5, p10, "G646"), s49))
        cp.addTransition (Transition (s46, Label (p8, p2, "G644"), s47))
        cp.addTransition (Transition (s43, Label (p6, p7, "G643"), s46))
        cp.addTransition (Transition (s43, Label (p6, p9, "G642"), s45))
        cp.addTransition (Transition (s42, Label (p5, p1, "G640"), s43))
        cp.addTransition (Transition (s40, Label (p6, p2, "G639"), s42))
        cp.addTransition (Transition (s40, Label (p6, p5, "G638"), s41))
        cp.addTransition (Transition (s39, Label (p3, p10, "G636"), s40))
        cp.addTransition (Transition (s38, Label (p3, p6, "G635"), s39))
        cp.addTransition (Transition (s35, Label (p3, p1, "G634"), s38))
        cp.addTransition (Transition (s35, Label (p3, p7, "G633"), s37))
        cp.addTransition (Transition (s32, Label (p4, p1, "G631"), s35))
        cp.addTransition (Transition (s32, Label (p4, p6, "G630"), s1))
        cp.addTransition (Transition (s31, Label (p1, p9, "G628"), s32))
        cp.addTransition (Transition (s28, Label (p4, p1, "G627"), s31))
        cp.addTransition (Transition (s28, Label (p4, p3, "G626"), s30))
        cp.addTransition (Transition (s27, Label (p11, p9, "G624"), s28))
        cp.addTransition (Transition (s26, Label (p8, p9, "G623"), s27))
        cp.addTransition (Transition (s25, Label (p7, p6, "G622"), s26))
        cp.addTransition (Transition (s23, Label (p6, p3, "G621"), s25))
        cp.addTransition (Transition (s23, Label (p6, p5, "G620"), s24))
        cp.addTransition (Transition (s21, Label (p0, p10, "G618"), s23))
        cp.addTransition (Transition (s21, Label (p0, p5, "G616"), s9))
        cp.addTransition (Transition (s18, Label (p11, p2, "G615"), s21))
        cp.addTransition (Transition (s18, Label (p11, p4, "G614"), s20))
        cp.addTransition (Transition (s17, Label (p11, p7, "G612"), s18))
        cp.addTransition (Transition (s16, Label (p11, p4, "G611"), s17))
        cp.addTransition (Transition (s15, Label (p2, p1, "G610"), s16))
        cp.addTransition (Transition (s12, Label (p6, p10, "G609"), s15))
        cp.addTransition (Transition (s12, Label (p6, p2, "G608"), s14))
        cp.addTransition (Transition (s10, Label (p11, p8, "G606"), s12))
        cp.addTransition (Transition (s10, Label (p11, p8, "G604"), s3))
        cp.addTransition (Transition (s7, Label (p10, p1, "G603"), s10))
        cp.addTransition (Transition (s7, Label (p10, p3, "G602"), s9))
        cp.addTransition (Transition (s6, Label (p1, p0, "G600"), s7))
        cp.addTransition (Transition (s3, Label (p6, p4, "G599"), s6))
        cp.addTransition (Transition (s3, Label (p6, p7, "G598"), s5))
        cp.addTransition (Transition (s2, Label (p8, p7, "G596"), s3))
        cp.addTransition (Transition (s1, Label (p6, p5, "G595"), s2))
        cp.addTransition (Transition (s0, Label (p10, p5, "G594"), s1))
        return cp

    def cp0109(self):
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0109", s0)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s43, Label (p8, p0, "G722"), s44))
        cp.addTransition (Transition (s42, Label (p4, p5, "G721"), s43))
        cp.addTransition (Transition (s41, Label (p5, p2, "G720"), s42))
        cp.addTransition (Transition (s40, Label (p0, p6, "G719"), s41))
        cp.addTransition (Transition (s38, Label (p4, p0, "G718"), s40))
        cp.addTransition (Transition (s38, Label (p4, p2, "G717"), s39))
        cp.addTransition (Transition (s37, Label (p4, p5, "G715"), s38))
        cp.addTransition (Transition (s36, Label (p1, p8, "G714"), s37))
        cp.addTransition (Transition (s35, Label (p6, p8, "G713"), s36))
        cp.addTransition (Transition (s34, Label (p4, p0, "G712"), s35))
        cp.addTransition (Transition (s33, Label (p3, p6, "G711"), s34))
        cp.addTransition (Transition (s32, Label (p5, p4, "G710"), s33))
        cp.addTransition (Transition (s31, Label (p3, p9, "G709"), s32))
        cp.addTransition (Transition (s29, Label (p4, p2, "G708"), s31))
        cp.addTransition (Transition (s29, Label (p4, p1, "G707"), s30))
        cp.addTransition (Transition (s26, Label (p8, p9, "G705"), s29))
        cp.addTransition (Transition (s26, Label (p8, p7, "G704"), s28))
        cp.addTransition (Transition (s23, Label (p0, p5, "G702"), s26))
        cp.addTransition (Transition (s23, Label (p0, p4, "G701"), s25))
        cp.addTransition (Transition (s22, Label (p9, p2, "G699"), s23))
        cp.addTransition (Transition (s21, Label (p5, p3, "G698"), s22))
        cp.addTransition (Transition (s20, Label (p7, p4, "G697"), s21))
        cp.addTransition (Transition (s19, Label (p4, p7, "G696"), s20))
        cp.addTransition (Transition (s18, Label (p0, p4, "G695"), s19))
        cp.addTransition (Transition (s16, Label (p6, p7, "G694"), s18))
        cp.addTransition (Transition (s16, Label (p6, p8, "G692"), s11))
        cp.addTransition (Transition (s13, Label (p2, p5, "G691"), s16))
        cp.addTransition (Transition (s13, Label (p2, p3, "G690"), s15))
        cp.addTransition (Transition (s11, Label (p4, p0, "G688"), s13))
        cp.addTransition (Transition (s11, Label (p4, p7, "G686"), s4))
        cp.addTransition (Transition (s8, Label (p8, p7, "G685"), s11))
        cp.addTransition (Transition (s8, Label (p8, p2, "G684"), s10))
        cp.addTransition (Transition (s6, Label (p3, p6, "G682"), s8))
        cp.addTransition (Transition (s6, Label (p3, p5, "G680"), s0))
        cp.addTransition (Transition (s3, Label (p9, p3, "G679"), s6))
        cp.addTransition (Transition (s3, Label (p9, p6, "G678"), s5))
        cp.addTransition (Transition (s0, Label (p4, p9, "G676"), s3))
        cp.addTransition (Transition (s0, Label (p4, p5, "G675"), s2))
        return cp

    def cp0110(self):
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0110", s0)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s61, Label (p5, p0, "G790"), s62))
        cp.addTransition (Transition (s60, Label (p5, p1, "G789"), s61))
        cp.addTransition (Transition (s58, Label (p2, p1, "G788"), s60))
        cp.addTransition (Transition (s58, Label (p2, p1, "G786"), s28))
        cp.addTransition (Transition (s55, Label (p5, p3, "G785"), s58))
        cp.addTransition (Transition (s55, Label (p5, p0, "G784"), s57))
        cp.addTransition (Transition (s53, Label (p5, p1, "G782"), s55))
        cp.addTransition (Transition (s53, Label (p5, p0, "G780"), s4))
        cp.addTransition (Transition (s50, Label (p0, p5, "G779"), s53))
        cp.addTransition (Transition (s50, Label (p0, p3, "G778"), s52))
        cp.addTransition (Transition (s49, Label (p3, p0, "G776"), s50))
        cp.addTransition (Transition (s48, Label (p2, p4, "G775"), s49))
        cp.addTransition (Transition (s47, Label (p2, p5, "G774"), s48))
        cp.addTransition (Transition (s46, Label (p0, p4, "G773"), s47))
        cp.addTransition (Transition (s45, Label (p5, p2, "G772"), s46))
        cp.addTransition (Transition (s42, Label (p5, p4, "G771"), s45))
        cp.addTransition (Transition (s42, Label (p5, p0, "G770"), s44))
        cp.addTransition (Transition (s41, Label (p0, p3, "G768"), s42))
        cp.addTransition (Transition (s40, Label (p5, p2, "G767"), s41))
        cp.addTransition (Transition (s39, Label (p3, p0, "G766"), s40))
        cp.addTransition (Transition (s38, Label (p0, p1, "G765"), s39))
        cp.addTransition (Transition (s37, Label (p4, p5, "G764"), s38))
        cp.addTransition (Transition (s36, Label (p3, p5, "G763"), s37))
        cp.addTransition (Transition (s35, Label (p4, p1, "G762"), s36))
        cp.addTransition (Transition (s34, Label (p5, p2, "G761"), s35))
        cp.addTransition (Transition (s31, Label (p4, p5, "G760"), s34))
        cp.addTransition (Transition (s31, Label (p4, p0, "G759"), s33))
        cp.addTransition (Transition (s29, Label (p5, p0, "G757"), s31))
        cp.addTransition (Transition (s29, Label (p5, p1, "G755"), s4))
        cp.addTransition (Transition (s26, Label (p2, p1, "G754"), s29))
        cp.addTransition (Transition (s26, Label (p2, p4, "G753"), s28))
        cp.addTransition (Transition (s25, Label (p5, p4, "G751"), s26))
        cp.addTransition (Transition (s24, Label (p1, p2, "G750"), s25))
        cp.addTransition (Transition (s22, Label (p0, p1, "G749"), s24))
        cp.addTransition (Transition (s22, Label (p0, p3, "G747"), s10))
        cp.addTransition (Transition (s21, Label (p1, p5, "G746"), s22))
        cp.addTransition (Transition (s20, Label (p1, p3, "G745"), s21))
        cp.addTransition (Transition (s17, Label (p3, p4, "G744"), s20))
        cp.addTransition (Transition (s17, Label (p3, p0, "G743"), s19))
        cp.addTransition (Transition (s15, Label (p0, p1, "G741"), s17))
        cp.addTransition (Transition (s15, Label (p0, p2, "G740"), s16))
        cp.addTransition (Transition (s12, Label (p3, p2, "G738"), s15))
        cp.addTransition (Transition (s12, Label (p3, p0, "G737"), s14))
        cp.addTransition (Transition (s11, Label (p4, p3, "G735"), s12))
        cp.addTransition (Transition (s10, Label (p4, p2, "G734"), s11))
        cp.addTransition (Transition (s9, Label (p0, p3, "G733"), s10))
        cp.addTransition (Transition (s8, Label (p3, p1, "G732"), s9))
        cp.addTransition (Transition (s7, Label (p4, p2, "G731"), s8))
        cp.addTransition (Transition (s5, Label (p4, p1, "G730"), s7))
        cp.addTransition (Transition (s5, Label (p4, p2, "G728"), s4))
        cp.addTransition (Transition (s4, Label (p0, p5, "G727"), s5))
        cp.addTransition (Transition (s1, Label (p2, p0, "G726"), s4))
        cp.addTransition (Transition (s1, Label (p2, p5, "G725"), s3))
        cp.addTransition (Transition (s0, Label (p4, p2, "G723"), s1))
        return cp

    def cp0111(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0111", s0)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s53, Label (p5, p3, "G850"), s41))
        cp.addTransition (Transition (s52, Label (p6, p2, "G849"), s53))
        cp.addTransition (Transition (s51, Label (p6, p3, "G848"), s52))
        cp.addTransition (Transition (s50, Label (p1, p0, "G847"), s51))
        cp.addTransition (Transition (s49, Label (p5, p4, "G846"), s50))
        cp.addTransition (Transition (s48, Label (p0, p3, "G845"), s49))
        cp.addTransition (Transition (s47, Label (p0, p1, "G844"), s48))
        cp.addTransition (Transition (s46, Label (p1, p3, "G843"), s47))
        cp.addTransition (Transition (s45, Label (p1, p6, "G842"), s46))
        cp.addTransition (Transition (s44, Label (p5, p0, "G841"), s45))
        cp.addTransition (Transition (s42, Label (p3, p0, "G840"), s44))
        cp.addTransition (Transition (s42, Label (p3, p2, "G839"), s43))
        cp.addTransition (Transition (s41, Label (p2, p4, "G837"), s42))
        cp.addTransition (Transition (s40, Label (p6, p2, "G836"), s41))
        cp.addTransition (Transition (s39, Label (p0, p2, "G835"), s40))
        cp.addTransition (Transition (s38, Label (p2, p4, "G834"), s39))
        cp.addTransition (Transition (s35, Label (p0, p6, "G833"), s38))
        cp.addTransition (Transition (s35, Label (p0, p5, "G832"), s32))
        cp.addTransition (Transition (s33, Label (p4, p1, "G830"), s35))
        cp.addTransition (Transition (s33, Label (p4, p0, "G828"), s30))
        cp.addTransition (Transition (s32, Label (p2, p0, "G827"), s33))
        cp.addTransition (Transition (s31, Label (p3, p0, "G826"), s32))
        cp.addTransition (Transition (s30, Label (p6, p3, "G825"), s31))
        cp.addTransition (Transition (s29, Label (p0, p4, "G824"), s30))
        cp.addTransition (Transition (s28, Label (p0, p3, "G823"), s29))
        cp.addTransition (Transition (s27, Label (p2, p0, "G822"), s28))
        cp.addTransition (Transition (s26, Label (p2, p5, "G821"), s27))
        cp.addTransition (Transition (s24, Label (p5, p3, "G820"), s26))
        cp.addTransition (Transition (s24, Label (p5, p0, "G818"), s12))
        cp.addTransition (Transition (s22, Label (p3, p4, "G817"), s24))
        cp.addTransition (Transition (s22, Label (p3, p6, "G816"), s23))
        cp.addTransition (Transition (s20, Label (p0, p4, "G814"), s22))
        cp.addTransition (Transition (s20, Label (p0, p6, "G813"), s8))
        cp.addTransition (Transition (s19, Label (p6, p5, "G811"), s20))
        cp.addTransition (Transition (s18, Label (p2, p1, "G810"), s19))
        cp.addTransition (Transition (s17, Label (p6, p5, "G809"), s18))
        cp.addTransition (Transition (s16, Label (p2, p5, "G808"), s17))
        cp.addTransition (Transition (s15, Label (p1, p0, "G807"), s16))
        cp.addTransition (Transition (s13, Label (p3, p4, "G806"), s15))
        cp.addTransition (Transition (s13, Label (p3, p4, "G804"), s8))
        cp.addTransition (Transition (s12, Label (p0, p6, "G803"), s13))
        cp.addTransition (Transition (s9, Label (p4, p3, "G802"), s12))
        cp.addTransition (Transition (s9, Label (p4, p2, "G801"), s11))
        cp.addTransition (Transition (s8, Label (p6, p4, "G799"), s9))
        cp.addTransition (Transition (s7, Label (p6, p4, "G798"), s8))
        cp.addTransition (Transition (s4, Label (p5, p2, "G797"), s7))
        cp.addTransition (Transition (s4, Label (p5, p3, "G796"), s6))
        cp.addTransition (Transition (s3, Label (p0, p5, "G794"), s4))
        cp.addTransition (Transition (s2, Label (p4, p1, "G793"), s3))
        cp.addTransition (Transition (s1, Label (p4, p5, "G792"), s2))
        cp.addTransition (Transition (s0, Label (p5, p0, "G791"), s1))
        return cp

    def cp0112(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0112", s0)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s64, Label (p5, p3, "G918"), s65))
        cp.addTransition (Transition (s61, Label (p8, p6, "G917"), s64))
        cp.addTransition (Transition (s61, Label (p8, p2, "G916"), s63))
        cp.addTransition (Transition (s60, Label (p7, p2, "G914"), s61))
        cp.addTransition (Transition (s57, Label (p8, p4, "G913"), s60))
        cp.addTransition (Transition (s57, Label (p8, p3, "G912"), s54))
        cp.addTransition (Transition (s56, Label (p2, p7, "G910"), s57))
        cp.addTransition (Transition (s53, Label (p8, p1, "G909"), s56))
        cp.addTransition (Transition (s53, Label (p8, p7, "G908"), s55))
        cp.addTransition (Transition (s52, Label (p1, p0, "G906"), s53))
        cp.addTransition (Transition (s51, Label (p2, p0, "G905"), s52))
        cp.addTransition (Transition (s48, Label (p5, p8, "G904"), s51))
        cp.addTransition (Transition (s48, Label (p5, p7, "G903"), s50))
        cp.addTransition (Transition (s47, Label (p1, p6, "G901"), s48))
        cp.addTransition (Transition (s46, Label (p7, p4, "G900"), s47))
        cp.addTransition (Transition (s45, Label (p4, p8, "G899"), s46))
        cp.addTransition (Transition (s44, Label (p5, p6, "G898"), s45))
        cp.addTransition (Transition (s43, Label (p3, p6, "G897"), s44))
        cp.addTransition (Transition (s42, Label (p1, p3, "G896"), s43))
        cp.addTransition (Transition (s41, Label (p2, p7, "G895"), s42))
        cp.addTransition (Transition (s40, Label (p0, p2, "G894"), s41))
        cp.addTransition (Transition (s39, Label (p0, p7, "G893"), s40))
        cp.addTransition (Transition (s38, Label (p6, p2, "G892"), s39))
        cp.addTransition (Transition (s37, Label (p0, p2, "G891"), s38))
        cp.addTransition (Transition (s36, Label (p3, p0, "G890"), s37))
        cp.addTransition (Transition (s35, Label (p1, p2, "G889"), s36))
        cp.addTransition (Transition (s34, Label (p7, p0, "G888"), s35))
        cp.addTransition (Transition (s31, Label (p3, p2, "G887"), s34))
        cp.addTransition (Transition (s31, Label (p3, p5, "G886"), s33))
        cp.addTransition (Transition (s30, Label (p7, p3, "G884"), s31))
        cp.addTransition (Transition (s28, Label (p1, p7, "G883"), s30))
        cp.addTransition (Transition (s28, Label (p1, p0, "G881"), s8))
        cp.addTransition (Transition (s27, Label (p5, p3, "G880"), s28))
        cp.addTransition (Transition (s26, Label (p7, p8, "G879"), s27))
        cp.addTransition (Transition (s25, Label (p3, p5, "G878"), s26))
        cp.addTransition (Transition (s24, Label (p2, p8, "G877"), s25))
        cp.addTransition (Transition (s22, Label (p4, p2, "G876"), s24))
        cp.addTransition (Transition (s22, Label (p4, p8, "G875"), s23))
        cp.addTransition (Transition (s19, Label (p3, p6, "G873"), s22))
        cp.addTransition (Transition (s19, Label (p3, p7, "G872"), s21))
        cp.addTransition (Transition (s18, Label (p3, p6, "G870"), s19))
        cp.addTransition (Transition (s16, Label (p5, p3, "G869"), s18))
        cp.addTransition (Transition (s16, Label (p5, p2, "G867"), s11))
        cp.addTransition (Transition (s13, Label (p3, p1, "G866"), s16))
        cp.addTransition (Transition (s13, Label (p3, p8, "G865"), s15))
        cp.addTransition (Transition (s10, Label (p4, p1, "G863"), s13))
        cp.addTransition (Transition (s10, Label (p4, p8, "G862"), s12))
        cp.addTransition (Transition (s9, Label (p4, p0, "G860"), s10))
        cp.addTransition (Transition (s8, Label (p7, p0, "G859"), s9))
        cp.addTransition (Transition (s7, Label (p1, p3, "G858"), s8))
        cp.addTransition (Transition (s4, Label (p3, p4, "G857"), s7))
        cp.addTransition (Transition (s4, Label (p3, p5, "G856"), s6))
        cp.addTransition (Transition (s3, Label (p4, p0, "G854"), s4))
        cp.addTransition (Transition (s2, Label (p0, p7, "G853"), s3))
        cp.addTransition (Transition (s1, Label (p2, p7, "G852"), s2))
        cp.addTransition (Transition (s0, Label (p2, p1, "G851"), s1))
        return cp

    def cp0113(self):
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s67 = State("s67")
        s66 = State("s66")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0113", s0)
        cp.addState(s67)
        cp.addState(s66)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s66, Label (p2, p0, "G993"), s67))
        cp.addTransition (Transition (s65, Label (p0, p1, "G992"), s66))
        cp.addTransition (Transition (s64, Label (p4, p0, "G991"), s65))
        cp.addTransition (Transition (s61, Label (p2, p6, "G990"), s64))
        cp.addTransition (Transition (s61, Label (p2, p1, "G989"), s63))
        cp.addTransition (Transition (s58, Label (p6, p2, "G987"), s61))
        cp.addTransition (Transition (s58, Label (p6, p1, "G986"), s60))
        cp.addTransition (Transition (s55, Label (p5, p4, "G984"), s58))
        cp.addTransition (Transition (s55, Label (p5, p6, "G983"), s57))
        cp.addTransition (Transition (s52, Label (p6, p1, "G981"), s55))
        cp.addTransition (Transition (s52, Label (p6, p0, "G980"), s11))
        cp.addTransition (Transition (s49, Label (p6, p0, "G978"), s52))
        cp.addTransition (Transition (s49, Label (p6, p3, "G977"), s51))
        cp.addTransition (Transition (s48, Label (p1, p4, "G975"), s49))
        cp.addTransition (Transition (s47, Label (p5, p6, "G974"), s48))
        cp.addTransition (Transition (s46, Label (p4, p5, "G973"), s47))
        cp.addTransition (Transition (s44, Label (p1, p0, "G972"), s46))
        cp.addTransition (Transition (s44, Label (p1, p3, "G970"), s22))
        cp.addTransition (Transition (s43, Label (p3, p0, "G969"), s44))
        cp.addTransition (Transition (s41, Label (p3, p5, "G968"), s43))
        cp.addTransition (Transition (s41, Label (p3, p4, "G966"), s23))
        cp.addTransition (Transition (s40, Label (p2, p0, "G965"), s41))
        cp.addTransition (Transition (s39, Label (p5, p2, "G964"), s40))
        cp.addTransition (Transition (s37, Label (p4, p6, "G963"), s39))
        cp.addTransition (Transition (s37, Label (p4, p2, "G961"), s15))
        cp.addTransition (Transition (s35, Label (p3, p0, "G960"), s37))
        cp.addTransition (Transition (s35, Label (p3, p2, "G958"), s4))
        cp.addTransition (Transition (s34, Label (p2, p6, "G957"), s35))
        cp.addTransition (Transition (s33, Label (p6, p0, "G956"), s34))
        cp.addTransition (Transition (s31, Label (p3, p1, "G955"), s33))
        cp.addTransition (Transition (s31, Label (p3, p1, "G953"), s9))
        cp.addTransition (Transition (s28, Label (p1, p4, "G952"), s31))
        cp.addTransition (Transition (s28, Label (p1, p6, "G951"), s30))
        cp.addTransition (Transition (s27, Label (p0, p5, "G949"), s28))
        cp.addTransition (Transition (s26, Label (p4, p5, "G948"), s27))
        cp.addTransition (Transition (s25, Label (p2, p0, "G947"), s26))
        cp.addTransition (Transition (s22, Label (p4, p2, "G946"), s25))
        cp.addTransition (Transition (s22, Label (p4, p0, "G945"), s24))
        cp.addTransition (Transition (s19, Label (p1, p4, "G943"), s22))
        cp.addTransition (Transition (s19, Label (p1, p5, "G942"), s21))
        cp.addTransition (Transition (s16, Label (p5, p2, "G940"), s19))
        cp.addTransition (Transition (s16, Label (p5, p3, "G939"), s18))
        cp.addTransition (Transition (s14, Label (p6, p1, "G937"), s16))
        cp.addTransition (Transition (s14, Label (p6, p5, "G935"), s10))
        cp.addTransition (Transition (s13, Label (p2, p5, "G934"), s14))
        cp.addTransition (Transition (s12, Label (p2, p4, "G933"), s13))
        cp.addTransition (Transition (s10, Label (p4, p3, "G932"), s12))
        cp.addTransition (Transition (s10, Label (p4, p6, "G930"), s5))
        cp.addTransition (Transition (s7, Label (p2, p0, "G929"), s10))
        cp.addTransition (Transition (s7, Label (p2, p1, "G928"), s9))
        cp.addTransition (Transition (s4, Label (p5, p0, "G926"), s7))
        cp.addTransition (Transition (s4, Label (p5, p1, "G925"), s6))
        cp.addTransition (Transition (s2, Label (p6, p3, "G923"), s4))
        cp.addTransition (Transition (s2, Label (p6, p5, "G921"), s1))
        cp.addTransition (Transition (s1, Label (p4, p0, "G920"), s2))
        cp.addTransition (Transition (s0, Label (p2, p3, "G919"), s1))
        return cp

    def cp0114(self):
        p14 = Peer("p14")
        p13 = Peer("p13")
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0114", s0)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s57, Label (p7, p2, "G1059"), s60))
        cp.addTransition (Transition (s57, Label (p7, p12, "G1058"), s59))
        cp.addTransition (Transition (s54, Label (p11, p12, "G1056"), s57))
        cp.addTransition (Transition (s54, Label (p11, p8, "G1055"), s56))
        cp.addTransition (Transition (s53, Label (p13, p5, "G1053"), s54))
        cp.addTransition (Transition (s50, Label (p2, p5, "G1052"), s53))
        cp.addTransition (Transition (s50, Label (p2, p3, "G1051"), s52))
        cp.addTransition (Transition (s48, Label (p11, p1, "G1049"), s50))
        cp.addTransition (Transition (s48, Label (p11, p12, "G1048"), s32))
        cp.addTransition (Transition (s47, Label (p4, p5, "G1046"), s48))
        cp.addTransition (Transition (s46, Label (p8, p6, "G1045"), s47))
        cp.addTransition (Transition (s45, Label (p10, p7, "G1044"), s46))
        cp.addTransition (Transition (s44, Label (p4, p14, "G1043"), s45))
        cp.addTransition (Transition (s43, Label (p9, p4, "G1042"), s44))
        cp.addTransition (Transition (s42, Label (p13, p12, "G1041"), s43))
        cp.addTransition (Transition (s41, Label (p8, p5, "G1040"), s42))
        cp.addTransition (Transition (s39, Label (p5, p6, "G1039"), s41))
        cp.addTransition (Transition (s39, Label (p5, p3, "G1037"), s31))
        cp.addTransition (Transition (s38, Label (p9, p10, "G1036"), s39))
        cp.addTransition (Transition (s36, Label (p7, p11, "G1035"), s38))
        cp.addTransition (Transition (s36, Label (p7, p2, "G1034"), s37))
        cp.addTransition (Transition (s35, Label (p3, p8, "G1032"), s36))
        cp.addTransition (Transition (s32, Label (p5, p9, "G1031"), s35))
        cp.addTransition (Transition (s32, Label (p5, p6, "G1030"), s0))
        cp.addTransition (Transition (s31, Label (p5, p9, "G1028"), s32))
        cp.addTransition (Transition (s30, Label (p13, p14, "G1027"), s31))
        cp.addTransition (Transition (s27, Label (p7, p2, "G1026"), s30))
        cp.addTransition (Transition (s27, Label (p7, p9, "G1025"), s29))
        cp.addTransition (Transition (s26, Label (p0, p2, "G1023"), s27))
        cp.addTransition (Transition (s25, Label (p3, p2, "G1022"), s26))
        cp.addTransition (Transition (s23, Label (p5, p10, "G1021"), s25))
        cp.addTransition (Transition (s23, Label (p5, p4, "G1019"), s8))
        cp.addTransition (Transition (s21, Label (p12, p13, "G1018"), s23))
        cp.addTransition (Transition (s21, Label (p12, p6, "G1016"), s3))
        cp.addTransition (Transition (s18, Label (p11, p13, "G1015"), s21))
        cp.addTransition (Transition (s18, Label (p11, p10, "G1014"), s20))
        cp.addTransition (Transition (s17, Label (p8, p7, "G1012"), s18))
        cp.addTransition (Transition (s16, Label (p14, p7, "G1011"), s17))
        cp.addTransition (Transition (s15, Label (p4, p10, "G1010"), s16))
        cp.addTransition (Transition (s14, Label (p5, p4, "G1009"), s15))
        cp.addTransition (Transition (s13, Label (p12, p11, "G1008"), s14))
        cp.addTransition (Transition (s11, Label (p0, p8, "G1007"), s13))
        cp.addTransition (Transition (s11, Label (p0, p10, "G1005"), s2))
        cp.addTransition (Transition (s10, Label (p10, p9, "G1004"), s11))
        cp.addTransition (Transition (s7, Label (p14, p0, "G1003"), s10))
        cp.addTransition (Transition (s7, Label (p14, p7, "G1002"), s9))
        cp.addTransition (Transition (s6, Label (p0, p4, "G1000"), s7))
        cp.addTransition (Transition (s3, Label (p0, p13, "G999"), s6))
        cp.addTransition (Transition (s3, Label (p0, p14, "G998"), s5))
        cp.addTransition (Transition (s0, Label (p5, p2, "G996"), s3))
        cp.addTransition (Transition (s0, Label (p5, p1, "G995"), s2))
        return cp

    def cp0115(self):
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s80 = State("s80")
        s79 = State("s79")
        s78 = State("s78")
        s77 = State("s77")
        s76 = State("s76")
        s75 = State("s75")
        s74 = State("s74")
        s73 = State("s73")
        s72 = State("s72")
        s71 = State("s71")
        s70 = State("s70")
        s69 = State("s69")
        s68 = State("s68")
        s67 = State("s67")
        s66 = State("s66")
        s65 = State("s65")
        s64 = State("s64")
        s63 = State("s63")
        s62 = State("s62")
        s61 = State("s61")
        s60 = State("s60")
        s59 = State("s59")
        s58 = State("s58")
        s57 = State("s57")
        s56 = State("s56")
        s55 = State("s55")
        s54 = State("s54")
        s53 = State("s53")
        s52 = State("s52")
        s51 = State("s51")
        s50 = State("s50")
        s49 = State("s49")
        s48 = State("s48")
        s47 = State("s47")
        s46 = State("s46")
        s45 = State("s45")
        s44 = State("s44")
        s43 = State("s43")
        s42 = State("s42")
        s41 = State("s41")
        s40 = State("s40")
        s39 = State("s39")
        s38 = State("s38")
        s37 = State("s37")
        s36 = State("s36")
        s35 = State("s35")
        s34 = State("s34")
        s33 = State("s33")
        s32 = State("s32")
        s31 = State("s31")
        s30 = State("s30")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0115", s0)
        cp.addState(s80)
        cp.addState(s79)
        cp.addState(s78)
        cp.addState(s77)
        cp.addState(s76)
        cp.addState(s75)
        cp.addState(s74)
        cp.addState(s73)
        cp.addState(s72)
        cp.addState(s71)
        cp.addState(s70)
        cp.addState(s69)
        cp.addState(s68)
        cp.addState(s67)
        cp.addState(s66)
        cp.addState(s65)
        cp.addState(s64)
        cp.addState(s63)
        cp.addState(s62)
        cp.addState(s61)
        cp.addState(s60)
        cp.addState(s59)
        cp.addState(s58)
        cp.addState(s57)
        cp.addState(s56)
        cp.addState(s55)
        cp.addState(s54)
        cp.addState(s53)
        cp.addState(s52)
        cp.addState(s51)
        cp.addState(s50)
        cp.addState(s49)
        cp.addState(s48)
        cp.addState(s47)
        cp.addState(s46)
        cp.addState(s45)
        cp.addState(s44)
        cp.addState(s43)
        cp.addState(s42)
        cp.addState(s41)
        cp.addState(s40)
        cp.addState(s39)
        cp.addState(s38)
        cp.addState(s37)
        cp.addState(s36)
        cp.addState(s35)
        cp.addState(s34)
        cp.addState(s33)
        cp.addState(s32)
        cp.addState(s31)
        cp.addState(s30)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s79, Label (p3, p6, "G1146"), s80))
        cp.addTransition (Transition (s76, Label (p4, p3, "G1145"), s79))
        cp.addTransition (Transition (s76, Label (p4, p7, "G1144"), s78))
        cp.addTransition (Transition (s74, Label (p5, p3, "G1142"), s76))
        cp.addTransition (Transition (s74, Label (p5, p7, "G1141"), s75))
        cp.addTransition (Transition (s72, Label (p3, p5, "G1139"), s74))
        cp.addTransition (Transition (s72, Label (p3, p8, "G1138"), s73))
        cp.addTransition (Transition (s71, Label (p3, p1, "G1136"), s72))
        cp.addTransition (Transition (s70, Label (p4, p7, "G1135"), s71))
        cp.addTransition (Transition (s69, Label (p3, p6, "G1134"), s70))
        cp.addTransition (Transition (s68, Label (p6, p5, "G1133"), s69))
        cp.addTransition (Transition (s67, Label (p4, p2, "G1132"), s68))
        cp.addTransition (Transition (s66, Label (p7, p0, "G1131"), s67))
        cp.addTransition (Transition (s63, Label (p5, p0, "G1130"), s66))
        cp.addTransition (Transition (s63, Label (p5, p8, "G1129"), s65))
        cp.addTransition (Transition (s60, Label (p5, p8, "G1127"), s63))
        cp.addTransition (Transition (s60, Label (p5, p4, "G1126"), s11))
        cp.addTransition (Transition (s57, Label (p8, p1, "G1124"), s60))
        cp.addTransition (Transition (s57, Label (p8, p4, "G1123"), s59))
        cp.addTransition (Transition (s56, Label (p2, p5, "G1121"), s57))
        cp.addTransition (Transition (s54, Label (p2, p3, "G1120"), s56))
        cp.addTransition (Transition (s54, Label (p2, p7, "G1119"), s55))
        cp.addTransition (Transition (s51, Label (p5, p7, "G1117"), s54))
        cp.addTransition (Transition (s51, Label (p5, p0, "G1116"), s52))
        cp.addTransition (Transition (s50, Label (p4, p7, "G1114"), s51))
        cp.addTransition (Transition (s47, Label (p3, p8, "G1113"), s50))
        cp.addTransition (Transition (s47, Label (p3, p5, "G1112"), s49))
        cp.addTransition (Transition (s44, Label (p0, p6, "G1110"), s47))
        cp.addTransition (Transition (s44, Label (p0, p3, "G1109"), s27))
        cp.addTransition (Transition (s43, Label (p0, p2, "G1107"), s44))
        cp.addTransition (Transition (s42, Label (p4, p6, "G1106"), s43))
        cp.addTransition (Transition (s41, Label (p4, p2, "G1105"), s42))
        cp.addTransition (Transition (s38, Label (p7, p6, "G1104"), s41))
        cp.addTransition (Transition (s38, Label (p7, p4, "G1103"), s4))
        cp.addTransition (Transition (s37, Label (p0, p2, "G1101"), s38))
        cp.addTransition (Transition (s36, Label (p2, p1, "G1100"), s37))
        cp.addTransition (Transition (s35, Label (p3, p5, "G1099"), s36))
        cp.addTransition (Transition (s34, Label (p5, p3, "G1098"), s35))
        cp.addTransition (Transition (s33, Label (p0, p2, "G1097"), s34))
        cp.addTransition (Transition (s32, Label (p1, p8, "G1096"), s33))
        cp.addTransition (Transition (s29, Label (p8, p4, "G1095"), s32))
        cp.addTransition (Transition (s29, Label (p8, p1, "G1094"), s31))
        cp.addTransition (Transition (s28, Label (p4, p1, "G1092"), s29))
        cp.addTransition (Transition (s27, Label (p1, p6, "G1091"), s28))
        cp.addTransition (Transition (s26, Label (p2, p8, "G1090"), s27))
        cp.addTransition (Transition (s23, Label (p6, p1, "G1089"), s26))
        cp.addTransition (Transition (s23, Label (p6, p5, "G1088"), s25))
        cp.addTransition (Transition (s20, Label (p2, p4, "G1086"), s23))
        cp.addTransition (Transition (s20, Label (p2, p6, "G1085"), s22))
        cp.addTransition (Transition (s19, Label (p6, p2, "G1083"), s20))
        cp.addTransition (Transition (s17, Label (p0, p2, "G1082"), s19))
        cp.addTransition (Transition (s17, Label (p0, p4, "G1081"), s18))
        cp.addTransition (Transition (s14, Label (p0, p4, "G1079"), s17))
        cp.addTransition (Transition (s14, Label (p0, p3, "G1078"), s16))
        cp.addTransition (Transition (s13, Label (p1, p3, "G1076"), s14))
        cp.addTransition (Transition (s12, Label (p1, p3, "G1075"), s13))
        cp.addTransition (Transition (s10, Label (p4, p7, "G1074"), s12))
        cp.addTransition (Transition (s10, Label (p4, p5, "G1072"), s8))
        cp.addTransition (Transition (s9, Label (p4, p8, "G1071"), s10))
        cp.addTransition (Transition (s6, Label (p5, p8, "G1070"), s9))
        cp.addTransition (Transition (s6, Label (p5, p4, "G1069"), s8))
        cp.addTransition (Transition (s5, Label (p1, p6, "G1067"), s6))
        cp.addTransition (Transition (s3, Label (p5, p8, "G1066"), s5))
        cp.addTransition (Transition (s3, Label (p5, p2, "G1065"), s0))
        cp.addTransition (Transition (s1, Label (p3, p2, "G1063"), s3))
        cp.addTransition (Transition (s1, Label (p3, p6, "G1061"), s0))
        cp.addTransition (Transition (s0, Label (p3, p0, "G1060"), s1))
        return cp

    def cp0116(self):
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0116", s0)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s7, Label (p3, p4, "G1155"), s8))
        cp.addTransition (Transition (s5, Label (p2, p5, "G1154"), s7))
        cp.addTransition (Transition (s5, Label (p2, p7, "G1152"), s3))
        cp.addTransition (Transition (s2, Label (p6, p4, "G1151"), s5))
        cp.addTransition (Transition (s2, Label (p6, p3, "G1150"), s4))
        cp.addTransition (Transition (s1, Label (p4, p5, "G1148"), s2))
        cp.addTransition (Transition (s0, Label (p6, p7, "G1147"), s1))
        return cp

    def cp0117(self):
        p13 = Peer("p13")
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s29 = State("s29")
        s28 = State("s28")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0117", s0)
        cp.addState(s29)
        cp.addState(s28)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s28, Label (p9, p11, "G1186"), s29))
        cp.addTransition (Transition (s27, Label (p7, p1, "G1185"), s28))
        cp.addTransition (Transition (s26, Label (p5, p7, "G1184"), s27))
        cp.addTransition (Transition (s23, Label (p0, p12, "G1183"), s26))
        cp.addTransition (Transition (s23, Label (p0, p9, "G1182"), s25))
        cp.addTransition (Transition (s22, Label (p3, p1, "G1180"), s23))
        cp.addTransition (Transition (s20, Label (p2, p11, "G1179"), s22))
        cp.addTransition (Transition (s20, Label (p2, p4, "G1177"), s13))
        cp.addTransition (Transition (s19, Label (p11, p12, "G1176"), s20))
        cp.addTransition (Transition (s16, Label (p8, p1, "G1175"), s19))
        cp.addTransition (Transition (s16, Label (p8, p3, "G1174"), s18))
        cp.addTransition (Transition (s15, Label (p0, p10, "G1172"), s16))
        cp.addTransition (Transition (s14, Label (p13, p9, "G1171"), s15))
        cp.addTransition (Transition (s13, Label (p10, p13, "G1170"), s14))
        cp.addTransition (Transition (s12, Label (p5, p8, "G1169"), s13))
        cp.addTransition (Transition (s10, Label (p2, p9, "G1168"), s12))
        cp.addTransition (Transition (s10, Label (p2, p13, "G1167"), s11))
        cp.addTransition (Transition (s7, Label (p0, p12, "G1165"), s10))
        cp.addTransition (Transition (s7, Label (p0, p5, "G1164"), s9))
        cp.addTransition (Transition (s6, Label (p9, p2, "G1162"), s7))
        cp.addTransition (Transition (s5, Label (p10, p0, "G1161"), s6))
        cp.addTransition (Transition (s4, Label (p12, p2, "G1160"), s5))
        cp.addTransition (Transition (s3, Label (p6, p8, "G1159"), s4))
        cp.addTransition (Transition (s2, Label (p6, p0, "G1158"), s3))
        cp.addTransition (Transition (s1, Label (p4, p9, "G1157"), s2))
        cp.addTransition (Transition (s0, Label (p13, p12, "G1156"), s1))
        return cp

    def cp0118(self):
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s27 = State("s27")
        s26 = State("s26")
        s25 = State("s25")
        s24 = State("s24")
        s23 = State("s23")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0118", s0)
        cp.addState(s27)
        cp.addState(s26)
        cp.addState(s25)
        cp.addState(s24)
        cp.addState(s23)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s24, Label (p7, p4, "G1217"), s27))
        cp.addTransition (Transition (s24, Label (p7, p1, "G1216"), s26))
        cp.addTransition (Transition (s22, Label (p5, p6, "G1214"), s24))
        cp.addTransition (Transition (s22, Label (p5, p6, "G1212"), s13))
        cp.addTransition (Transition (s21, Label (p1, p4, "G1211"), s22))
        cp.addTransition (Transition (s19, Label (p10, p6, "G1210"), s21))
        cp.addTransition (Transition (s19, Label (p10, p3, "G1209"), s20))
        cp.addTransition (Transition (s18, Label (p2, p5, "G1207"), s19))
        cp.addTransition (Transition (s17, Label (p9, p10, "G1206"), s18))
        cp.addTransition (Transition (s16, Label (p1, p10, "G1205"), s17))
        cp.addTransition (Transition (s14, Label (p9, p2, "G1204"), s16))
        cp.addTransition (Transition (s14, Label (p9, p7, "G1203"), s15))
        cp.addTransition (Transition (s13, Label (p7, p1, "G1201"), s14))
        cp.addTransition (Transition (s12, Label (p5, p3, "G1200"), s13))
        cp.addTransition (Transition (s10, Label (p6, p10, "G1199"), s12))
        cp.addTransition (Transition (s10, Label (p6, p8, "G1197"), s4))
        cp.addTransition (Transition (s9, Label (p8, p0, "G1196"), s10))
        cp.addTransition (Transition (s8, Label (p3, p2, "G1195"), s9))
        cp.addTransition (Transition (s7, Label (p9, p10, "G1194"), s8))
        cp.addTransition (Transition (s6, Label (p0, p8, "G1193"), s7))
        cp.addTransition (Transition (s3, Label (p10, p2, "G1192"), s6))
        cp.addTransition (Transition (s3, Label (p10, p8, "G1191"), s5))
        cp.addTransition (Transition (s0, Label (p10, p9, "G1189"), s3))
        cp.addTransition (Transition (s0, Label (p10, p6, "G1188"), s2))
        return cp

    def cp0119(self):
        p12 = Peer("p12")
        p11 = Peer("p11")
        p10 = Peer("p10")
        p9 = Peer("p9")
        p8 = Peer("p8")
        p7 = Peer("p7")
        p6 = Peer("p6")
        p5 = Peer("p5")
        p4 = Peer("p4")
        p3 = Peer("p3")
        p2 = Peer("p2")
        p1 = Peer("p1")
        p0 = Peer("p0")
        s22 = State("s22")
        s21 = State("s21")
        s20 = State("s20")
        s19 = State("s19")
        s18 = State("s18")
        s17 = State("s17")
        s16 = State("s16")
        s15 = State("s15")
        s14 = State("s14")
        s13 = State("s13")
        s12 = State("s12")
        s11 = State("s11")
        s10 = State("s10")
        s9 = State("s9")
        s8 = State("s8")
        s7 = State("s7")
        s6 = State("s6")
        s5 = State("s5")
        s4 = State("s4")
        s3 = State("s3")
        s2 = State("s2")
        s1 = State("s1")
        s0 = State("s0")
        cp = CP("cp0119", s0)
        cp.addState(s22)
        cp.addState(s21)
        cp.addState(s20)
        cp.addState(s19)
        cp.addState(s18)
        cp.addState(s17)
        cp.addState(s16)
        cp.addState(s15)
        cp.addState(s14)
        cp.addState(s13)
        cp.addState(s12)
        cp.addState(s11)
        cp.addState(s10)
        cp.addState(s9)
        cp.addState(s8)
        cp.addState(s7)
        cp.addState(s6)
        cp.addState(s5)
        cp.addState(s4)
        cp.addState(s3)
        cp.addState(s2)
        cp.addState(s1)
        cp.addState(s0)
        cp.addTransition (Transition (s19, Label (p10, p0, "G1240"), s22))
        cp.addTransition (Transition (s19, Label (p10, p7, "G1239"), s21))
        cp.addTransition (Transition (s18, Label (p12, p0, "G1237"), s19))
        cp.addTransition (Transition (s15, Label (p2, p9, "G1236"), s18))
        cp.addTransition (Transition (s15, Label (p2, p7, "G1235"), s17))
        cp.addTransition (Transition (s14, Label (p5, p1, "G1233"), s15))
        cp.addTransition (Transition (s13, Label (p7, p12, "G1232"), s14))
        cp.addTransition (Transition (s10, Label (p10, p5, "G1231"), s13))
        cp.addTransition (Transition (s10, Label (p10, p7, "G1230"), s12))
        cp.addTransition (Transition (s9, Label (p3, p0, "G1228"), s10))
        cp.addTransition (Transition (s8, Label (p8, p0, "G1227"), s9))
        cp.addTransition (Transition (s6, Label (p7, p4, "G1226"), s8))
        cp.addTransition (Transition (s6, Label (p7, p5, "G1224"), s4))
        cp.addTransition (Transition (s5, Label (p5, p4, "G1223"), s6))
        cp.addTransition (Transition (s4, Label (p10, p8, "G1222"), s5))
        cp.addTransition (Transition (s1, Label (p8, p12, "G1221"), s4))
        cp.addTransition (Transition (s1, Label (p8, p4, "G1220"), s3))
        cp.addTransition (Transition (s0, Label (p4, p6, "G1218"), s1))
        return cp

    def cpSSP(self):

        p1 = Peer("agency")
        p2 = Peer("airline")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("SSP", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(s0, Label(p1,p2,"ask"), s1))
        cp.addTransition(Transition(s1, Label(p1,p2,"order"), s3))
        cp.addTransition(Transition(s1, Label(p2,p1,"timeout"), s2))

        cp.addTransition(Transition(s2, Label(p1,p2,"order"), s4))
        cp.addTransition(Transition(s3, Label(p2,p1,"timeout"), s4))
        cp.addTransition(Transition(s3, Label(p2,p1,"accept"), s5))

        cp.addTransition(Transition(s4, Label(p2,p1,"reject"), s6))

        return cp

    def cpSSPv2(self):

        p1 = Peer("agency")
        p2 = Peer("airline")
        p3 = Peer("bubu")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("SSPv2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(s0, Label(p1,p2,"ask"), s1))
        cp.addTransition(Transition(s1, Label(p1,p2,"order"), s3))
        cp.addTransition(Transition(s1, Label(p2,p1,"timeout"), s2))

        cp.addTransition(Transition(s2, Label(p1,p2,"order"), s4))
        cp.addTransition(Transition(s3, Label(p2,p1,"timeout"), s4))
        cp.addTransition(Transition(s3, Label(p2,p3,"accept"), s5))

        cp.addTransition(Transition(s4, Label(p2,p1,"reject"), s6))

        return cp

    def cpSSPv3(self):

        p1 = Peer("agency")
        p2 = Peer("airline")
        p3 = Peer("bubu")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("SSPv3", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(s0, Label(p1,p2,"ask"), s1))
        cp.addTransition(Transition(s1, Label(p1,p2,"order"), s3))
        cp.addTransition(Transition(s1, Label(p2,p1,"timeout"), s2))

        cp.addTransition(Transition(s2, Label(p1,p2,"order"), s4))
        cp.addTransition(Transition(s5, Label(p2,p1,"timeout"), s4))
        cp.addTransition(Transition(s3, Label(p2,p3,"accept"), s5))

        cp.addTransition(Transition(s4, Label(p2,p1,"reject"), s6))

        return cp

    def cpSSPSimple(self):

        p1 = Peer("agency")
        p2 = Peer("airline")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("SSPSimple", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)


        cp.addTransition(Transition(s0, Label(p1,p2,"ask"), s1))
        cp.addTransition(Transition(s1, Label(p1,p2,"order"), s3))
        cp.addTransition(Transition(s1, Label(p2,p1,"timeout"), s2))

        cp.addTransition(Transition(s2, Label(p1,p2,"order"), s4))
        cp.addTransition(Transition(s3, Label(p2,p1,"timeout"), s4))
        cp.addTransition(Transition(s3, Label(p2,p1,"accept"), s5))


        return cp

    # two sending to same peer, that peer sends to one of them - ok
    def cpND3v11(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v11", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them
    # one exiting transition from 1 - ok
    def cpND3v12(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v12", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p3,"e"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them
    # one exiting transition from 2 - ok
    def cpND3v13(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v13", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # two sending to same peer, that peer sends to one of them
    # continue by 2 - ok
    def cpND3v14(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")

        cp = CP("cpND3v14", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them
    # one exiting transition from 2
    # continue by 2 - ok
    def cpND3v15(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v15", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them
    # one exiting transition from 2 - ok
    # continue by 2 - ok
    def cpND3v16(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v16", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them
    # one exiting transition from 2 - ok
    # continue by 3 - nok - now ok 23/08/12
    def cpND3v17(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v17", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p3,p2,"x"), s9))

        return cp

    # two sending to same peer, that peer sends to one of them, 1 sends outwards - ok
    def cpND3v21(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")

        cp = CP("cpND3v21", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p4,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them, one sends outwards
    # one exiting transition from 1 - ok
    def cpND3v22(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v22", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))

        cp.addTransition(Transition(s7, Label(p1,p4,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them, one sends outwards
    # one exiting transition from 2 - nok - still not ok, because of exit transition
    def cpND3v23(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v23", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p1,p4,"x"), s9))

        return cp

    # two sending to same peer, that peer sends to one of them, one sends outwards
    # continue by 2 - nok - now ok 23/08/12
    def cpND3v43(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v24", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them, 1 sends outwards
    # one exiting transition from 2
    # continue by 2 - nok - now ok 23/08/12
    def cpND3v25(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v25", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them, 1 sends outwards
    # one exiting transition from 2 -
    # continue by 2 - nok - now ok 23/08/12
    def cpND3v26(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v26", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # two sending to same peer 1 , 1 sends to one of them, 1 sends outwards
    # one exiting transition from 2 -
    # continue by 3 - nok - now ok 28/08/12
    def cpND3v27(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v27", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p4,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p4,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p4,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p4,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"f"), s10))

        cp.addTransition(Transition(s7, Label(p3,p2,"x"), s9))

        return cp

    # disconnected sending 1<->2 3<->4 - ok
    def cpND3v31(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v31", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 1 - ok
    def cpND3v32(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v32", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # testing additional outgoing to validate theory for "exit
    # i.e. two different paths to exiting state without synchronizations "y"
    def cpND3v32a(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v32a", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p3,"y"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # testing additional outgoing to validate theory for "exit"
    # i.e. two different paths to exiting state without synchronizations "y"
    def cpND3v32b(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v32b", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p4,"y"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp


    # disconnected sending
    # one exiting transition from 2 - ok
    def cpND3v33(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v33", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p1,"f"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # completely disconnected sending
    def cpND3v33a(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        init = State("init")

        cp = CP("cpND3v33a", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        # cp.addState(init)
        # cp.addTransition(Transition(init, Label(p1,p2,"init"), s0))

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        # cp.addTransition(Transition(s5, Label(p1,p4,"e"), s10))
        # cp.addTransition(Transition(s4, Label(p5,p1,"f"), s10))
        # cp.addTransition(Transition(s8, Label(p3,p2,"g"), s10))

        cp.addTransition(Transition(s7, Label(p1,p3,"x"), s9))

        return cp

    # disconnected sending
    # continue by 2 - ok
    def cpND3v34(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")

        cp = CP("cpND3v34", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2
    # continue by 2 - ok
    def cpND3v35(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v35", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2 -
    # continue by 2 - ok
    def cpND3v36(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v36", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p1,"f"), s10))

        cp.addTransition(Transition(s7, Label(p2,p3,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2 -
    # continue by 3 - nok - now ok 28/08/12
    def cpND3v37(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v37", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p2,p1,"f"), s10))

        cp.addTransition(Transition(s7, Label(p3,p4,"x"), s9))

        return cp


    # disconnected sending 1->2 3->4 5->6 - nok - now ok 28/08/12
    def cpND3v41(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v41", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 1 - nok - now ok 28/08/12
    def cpND3v42(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v42", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp


    # disconnected sending, exiting from higher level
    # one exiting transition from 1 - nok - now ok 28/08/12
    def cpND3v42a(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v42a", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s2, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2 - nok, aber creation error - now ok 30/08/12
    def cpND3v43(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v43", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p5,p6,"f"), s10))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # disconnected sending
    # continue by 2 - nok - now ok 28/08/12
    def cpND3v44(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v44", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p5,p6,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2
    # continue by 2 - nok - now ok 28/08/12
    def cpND3v45(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v45", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))

        cp.addTransition(Transition(s7, Label(p5,p6,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2 -
    # continue by 2 - nok - now ok 28/08/12
    def cpND3v46(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v46", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p5,p6,"f"), s10))

        cp.addTransition(Transition(s7, Label(p5,p6,"x"), s9))

        return cp

    # disconnected sending
    # one exiting transition from 2 -
    # continue by 3 - nok - now ok 28/08/12
    def cpND3v47(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")


        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("s10")

        cp = CP("cpND3v47", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p2,"e"), s10))
        cp.addTransition(Transition(s4, Label(p5,p6,"f"), s10))

        cp.addTransition(Transition(s7, Label(p3,p4,"x"), s9))

        return cp

    # initiating transtion connected
    def cpND3v51(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        init = State("init")
        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v51", init)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(init)

        cp.addTransition(Transition (init, Label(p1, p2, "init"), s0))

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # initiating transtion from peer 2, but sending to participant at all transitions
    def cpND3v52(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        init = State("init")
        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v52", init)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(init)

        cp.addTransition(Transition (init, Label(p2, p1, "init"), s0))

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # initiating transtion disconnected, but sending to participant of two trans
    def cpND3v53(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        init = State("init")
        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v53", init)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(init)

        cp.addTransition(Transition (init, Label(p4, p2, "init"), s0))

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp

    # initiating transtion disconnected, but sending to participant of only one trans
    def cpND3v54(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        init = State("init")
        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")

        cp = CP("cpND3v54", init)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(init)

        cp.addTransition(Transition (init, Label(p4, p3, "init"), s0))

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s9))

        return cp


    # disconnected sending one entry
    def cpND3v55(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("end")
        s10 = State("init")

        cp = CP("cpND3v55", s10)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p5,p6,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))

        cp.addTransition(Transition(s1, Label(p5,p6,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))

        cp.addTransition(Transition(s4, Label(p5,p6,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s10, Label(p7,p8,"anfangen"), s0))

        return cp


    # three different senders in single state, one exiting without before criterium
    def cpND3v2(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)

        cp = CP("cpND3v2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"d"), s6))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        return cp

    # three different senders in single state, exiting with before criterium
    def cpND3v3(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")
        s8 = State("s8")

        cp = CP("cpND3v3", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))
        cp.addTransition(Transition(s5, Label(p1,p3,"d"), s6))

        return cp

    # not ok, exit transition 28/8/12
    def cpND3v4(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("s9")
        s10 = State("s10")

        cp = CP("cpND3v4", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))
        cp.addTransition(Transition(s5, Label(p1,p3,"d"), s6))

        cp.addTransition(Transition(s8, Label(p3,p4,"e"), s9))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s10))
        return cp

    # not ok, exit transition 28/8/12
    def cpND3v4a(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("s9")
        s10 = State("s10")

        cp = CP("cpND3v4a", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p1,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p1,"c"), s5))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))
        cp.addTransition(Transition(s8, Label(p3,p1,"c"), s7))

        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"d"), s6))

        #cp.addTransition(Transition(s8, Label(p3,p1,"e"), s9))

        cp.addTransition(Transition(s4, Label(p2,p1,"x"), s10))

        cp.addTransition(Transition(s7, Label(p2,p4,"y"), s10))
        return cp


    def cpND3v5(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("s9")

        cp = CP("cpND3v5", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))

        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))
        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))
        cp.addTransition(Transition(s5, Label(p1,p3,"d"), s6))

        cp.addTransition(Transition(s4, Label(p2,p3,"e"), s9))
        return cp


    # ok 28/08/12
    def cpND3v6(self):
        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("s9")
        s10 = State("end")

        cp = CP("cpND3v6", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)


        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s0, Label(p3,p4,"c"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s4))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s2, Label(p2,p1,"b"), s5))
        cp.addTransition(Transition(s3, Label(p3,p4,"c"), s5))
        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s8))
        cp.addTransition(Transition(s3, Label(p1,p2,"a"), s8))

        cp.addTransition(Transition(s8, Label(p3,p4,"c"), s7))
        cp.addTransition(Transition(s4, Label(p2,p1,"b"), s7))
        cp.addTransition(Transition(s5, Label(p1,p2,"a"), s7))

        cp.addTransition(Transition(s5, Label(p1,p3,"d"), s6))

        cp.addTransition(Transition(s4, Label(p2,p1,"e"), s9))

        cp.addTransition(Transition(s7, Label(p1,p2,"x"), s10))
        return cp



    def cpNdNonEqv1(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)

        cp = CP("NonEqv1", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))

        cp.addTransition(Transition(s3, Label(p2,p1,"b"), s5))

        return cp

    def cpNdNonEqv2(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")

        cp = CP("NonEqv2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p1,p3,"c"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))

        cp.addTransition(Transition(s3, Label(p2,p1,"b"), s5))

        return cp

    # creation error?
    def cpNdNonEqv3(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NonEqv3", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"c"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s4, Label(p3,p4,"c"), s6))

        cp.addTransition(Transition(s3, Label(p2,p1,"b"), s5))

        return cp


    def cpNdNonEqv4(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NonEqv4", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p1,"c"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))
        cp.addTransition(Transition(s4, Label(p3,p1,"c"), s6))

        cp.addTransition(Transition(s3, Label(p2,p1,"b"), s5))

        return cp

    #ok 28/08/12
    def cpNdConfLoop(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfLoop", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p1,p2,"d"), s0))

        return cp

    #ok 28/08/12
    def cpNdConfLoopv1(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfLoopv1", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s0))

        return cp

    def cpNdConfLoopv2(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfLoopv2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p3,p4,"d"), s0))

        return cp

    def cpNdConfLoopv4(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfLoopv4", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p2,p1,"d"), s0))

        return cp


    def cpNonConfv1(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NonConfv1", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))


        return cp

    def cpNonConfv2(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NonConfv2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))

        cp.addTransition(Transition(s3, Label(p1,p3,"c"), s5))
        cp.addTransition(Transition(s4, Label(p1,p3,"c"), s6))

        return cp

    def cpNonConfv3(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NonConfv3", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))

        cp.addTransition(Transition(s3, Label(p1,p3,"c"), s5))
        cp.addTransition(Transition(s4, Label(p1,p3,"d"), s6))

        return cp

    def cpNonConfv6(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")
        s7 = State("s7")

        cp = CP("NonConfv4", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s4))

        cp.addTransition(Transition(s3, Label(p1,p3,"c"), s5))
        cp.addTransition(Transition(s4, Label(p1,p3,"c"), s6))
        cp.addTransition(Transition(s4, Label(p1,p3,"d"), s6))

        return cp


    def cpNdConfExit(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExit", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        cp.addTransition(Transition(s1, Label(p2,p3,"e"), s5))

        return cp

    # one exit transition, no before criterion -> fixable
    def cpNdConfExitv1(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv1", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))

        return cp

    # two exiting transitions, no before criterion -> now ok 28/08/12
    def cpNdConfExitv2(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv2", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp

    # one continuing transition, no before criterion -> fixable
    def cpNdConfExitv3(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv3", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p1,p3,"x"), s4))

        return cp

    # one continuing transition, no before criterion -> fixable
    def cpNdConfExitv4(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv4", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p2,p4,"x"), s4))

        return cp

    # one continuing transition, no before criterion -> fixable
    def cpNdConfExitv5(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv5", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p1,p4,"x"), s6))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp

    # no peer in all outgoing ... -> not ok!
    def cpNdConfExitv5a(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv5a", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p2,p4,"x"), s6))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        cp.addTransition(Transition(s1, Label(p3,p4,"e"), s5))

        return cp

    # one continuing transition completely disconnected - nok - now ok 28/08/12
    def cpNdConfExitv6(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv6", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p3,p4,"b"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p5,p6,"x"), s6))

        cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp

    # one disconnected continuing transition
    def cpNdConfExitv7(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        s6 = State("s6")

        cp = CP("NdConfExitv7", s0)

        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)

        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(s3, Label(p5,p6,"x"), s6))

        # cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        # cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp

    # one disconnected continuing transition
    def cpNdConfEntryv1(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        init = State("init")

        cp = CP("NdConfEntryv1", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)



        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(init, Label(p5,p6,"x"), s0))

        # cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        # cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp


    # multiple sync trans test
    def cpMultSync(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")

        s0 = State("s0", True)
        s1 = State("s1", True)
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4")
        s5 = State("s5")
        init = State("init")

        cp = CP("MultSync", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)



        cp.addTransition(Transition(s0, Label(p1,p2,"a"), s1))
        cp.addTransition(Transition(s0, Label(p2,p1,"b"), s2))

        cp.addTransition(Transition(s1, Label(p2,p1,"b"), s3))
        cp.addTransition(Transition(s2, Label(p1,p2,"a"), s3))

        cp.addTransition(Transition(init, Label(p5,p6,"x"), s0))

        # cp.addTransition(Transition(s2, Label(p1,p3,"d"), s4))
        # cp.addTransition(Transition(s1, Label(p3,p1,"e"), s5))

        return cp


    # multiple incoming and outgoing and deconnected exit trans - ok 28/08/12
    def cpMultIncoming(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncoming", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s2))
        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s2))


        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s5))
        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))


        cp.addTransition(Transition(s5, Label(p1,p2,"c"), s6))

        return cp


    # mulitple incoming, outgoing and connected exit trans
    def cpMultIncomingv2(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncomingv2", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s2))
        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s2))


        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s5))
        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))


        cp.addTransition(Transition(s5, Label(p5,p2,"c"), s6))

        return cp

    # mulitple incoming, test with existing SyncTrans
    def cpMultIncomingv3(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s0_prime = State("s0_prime")
        s1_prime = State("s1_prime")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s4_prime = State("s4_prime")
        s5 = State("s5")
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncomingv3", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s0_prime)
        cp.addState(s1_prime)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s4_prime)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s0_prime))

        sync1 = SyncTransition(s0_prime, Label(p2, p7, "synchro"), s2, "a")
        sync1.addSyncTrans("b", Label(p2, p5, "sync_s0_prime_s2b"))
        cp.addTransition(sync1)

        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s1_prime))

        sync2 = SyncTransition(s1_prime, Label(p3, p7, "synchro"), s2, "a")
        sync2.addSyncTrans("b", Label(p3, p5, "sync_s1_prime_s2b"))
        cp.addTransition(sync2)

        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s4_prime))

        sync3 = SyncTransition(s4_prime, Label(p7, p5, "synchro"), s5, "c")

        cp.addTransition(sync3)

        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))


        cp.addTransition(Transition(s5, Label(p5,p2,"c"), s6))

        return cp


    # mulitple incoming, already fully syncronized and working ...
    def cpMultIncomingv4(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s0_prime = State("s0_prime")
        s1_prime = State("s1_prime")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s5_prime = State("s5_prime")
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncomingv4", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s0_prime)
        cp.addState(s1_prime)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s5_prime)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s0_prime))

        sync1 = SyncTransition(s0_prime, Label(p2, p7, "synchro"), s2, "a")
        sync1.addSyncTrans("b", Label(p2, p5, "sync_s0_prime_s2b"))
        cp.addTransition(sync1)

        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s1_prime))

        sync2 = SyncTransition(s1_prime, Label(p3, p7, "synchro"), s2, "a")
        sync2.addSyncTrans("b", Label(p3, p5, "sync_s1_prime_s2b"))
        cp.addTransition(sync2)

        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s5))

        sync3 = SyncTransition(s5, Label(p7, p5, "synchro"), s5_prime, "c")

        cp.addTransition(sync3)

        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))


        cp.addTransition(Transition(s5_prime, Label(p5,p2,"c"), s6))

        return cp



    # mulitple incoming, disconnected, already fully syncronized
    def cpMultIncomingv5(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s0_prime = State("s0_prime")
        s1_prime = State("s1_prime")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3")
        s4 = State("s4")
        s5 = State("s5")
        s5_prime = State("s5_prime")
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncomingv5", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s0_prime)
        cp.addState(s1_prime)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s5_prime)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s0_prime))

        sync1 = SyncTransition(s0_prime, Label(p2, p7, "synchro"), s2, "a")
        sync1.addSyncTrans("b", Label(p2, p5, "sync_s0_prime_s2b"))
        cp.addTransition(sync1)

        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s1_prime))

        sync2 = SyncTransition(s1_prime, Label(p3, p7, "synchro"), s2, "a")
        sync2.addSyncTrans("b", Label(p3, p5, "sync_s1_prime_s2b"))
        cp.addTransition(sync2)

        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s5))

        sync3 = SyncTransition(s5, Label(p7, p1, "synchro"), s5_prime, "c")
        sync3.addSyncTrans("c", Label(p5, p1, "sync_s5_s5_primec"))
        cp.addTransition(sync3)

        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))

        cp.addTransition(Transition(s5_prime, Label(p1,p2,"c"), s6))

        return cp

    # mulitple incoming, disconnected, manually flagged
    def cpMultIncomingv6(self):

        p1 = Peer("p1")
        p2 = Peer("p2")
        p3 = Peer("p3")
        p4 = Peer("p4")
        p5 = Peer("p5")
        p6 = Peer("p6")
        p7 = Peer("p7")
        p8 = Peer("p8")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2", True)
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6")
        init = State("init")

        cp = CP("MultIncomingv6", init)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(init)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)


        cp.addTransition(Transition(init, Label(p1,p2,"a1"), s0))
        cp.addTransition(Transition(init, Label(p1,p3,"a2"), s1))

        cp.addTransition(Transition(s0, Label(p2,p4,"b1"), s2))

        cp.addTransition(Transition(s1, Label(p3,p4,"b2"), s2))

        cp.addTransition(Transition(s2, Label(p7,p8,"a"), s3))
        cp.addTransition(Transition(s2, Label(p5,p6,"b"), s4))

        cp.addTransition(Transition(s4, Label(p7,p8,"a"), s5))

        cp.addTransition(Transition(s3, Label(p5,p6,"b"), s5))

#        cp.addTransition(Transition(s5, Label(p1,p2,"c"), s6))

        return cp


    # mulitple incoming, disconnected, manually flagged
    def cpFASEExample(self):

        cl = Peer("client")
        se = Peer("seller")
        pr = Peer("provider")
        ba = Peer("bank")

        s0 = State("s0")
        s1 = State("s1")
        s2 = State("s2")
        s3 = State("s3", True)
        s4 = State("s4", True)
        s5 = State("s5", True)
        s6 = State("s6", True)
        s7 = State("s7", True)
        s8 = State("s8", True)
        s9 = State("s9", True)
        s10 = State("s10", True)
        s11 = State("s11", True)
        s12 = State("s12", True)
        s13 = State("s13", True)
        s14 = State("s14", True)
        s15 = State("s15", True)
        s16 = State("s16", True)
        s17 = State("s17")


        cp = CP("online_shopping_v1", s0)


        cp.addState(s0)
        cp.addState(s1)
        cp.addState(s2)
        cp.addState(s3)
        cp.addState(s4)
        cp.addState(s5)
        cp.addState(s6)
        cp.addState(s7)
        cp.addState(s8)
        cp.addState(s9)
        cp.addState(s10)
        cp.addState(s11)
        cp.addState(s12)
        cp.addState(s13)
        cp.addState(s14)
        cp.addState(s15)
        cp.addState(s16)
        cp.addState(s17)


        cp.addTransition(Transition(s0, Label(cl,se,"purchase"), s17))
        cp.addTransition(Transition(s17, Label(se,ba,"checkAndCash"), s16))

        cp.addTransition(Transition(s16, Label(ba,pr,"paymentOk"), s15))
        cp.addTransition(Transition(s16, Label(ba,se,"approval"), s14))
        cp.addTransition(Transition(s16, Label(ba,se,"notifyFailure"), s13))

        cp.addTransition(Transition(s14, Label(ba,pr,"paymentOk"), s12))

        cp.addTransition(Transition(s15, Label(ba,se,"approval"), s12))
        cp.addTransition(Transition(s15, Label(pr,cl,"invoice"), s11))
        cp.addTransition(Transition(s15, Label(pr,se,"purchaseInfo"), s10))

        cp.addTransition(Transition(s13, Label(se,cl,"cardNotApproved"), s8))
        cp.addTransition(Transition(s13, Label(pr,cl,"purchaseCancelled"), s9))

        cp.addTransition(Transition(s9, Label(se,cl,"cardNotApproved"), s4))

        cp.addTransition(Transition(s8, Label(pr,cl,"purchaseCancelled"), s4))

        cp.addTransition(Transition(s12, Label(pr,cl,"invoice"), s7))
        cp.addTransition(Transition(s12, Label(pr,se,"purchaseInfo"), s6))

        cp.addTransition(Transition(s6, Label(pr,cl,"invoice"), s3))
        cp.addTransition(Transition(s6, Label(se,cl,"totalValue"), s2))

        cp.addTransition(Transition(s7, Label(pr,se,"purchaseInfo"), s3))

        cp.addTransition(Transition(s11, Label(ba,se,"approval"), s7))
        cp.addTransition(Transition(s11, Label(pr,se,"purchaseInfo"), s5))

        cp.addTransition(Transition(s5, Label(ba,se,"approval"), s3))

        cp.addTransition(Transition(s10, Label(pr,cl,"invoice"), s5))
        cp.addTransition(Transition(s10, Label(ba,se,"approval"), s6))

        cp.addTransition(Transition(s3, Label(se,cl,"totalValue"), s4))

        cp.addTransition(Transition(s2, Label(pr,cl,"invoice"), s4))

        return cp



class Checker:

    # remove all files generated by the svl execution
    def cleanUp(self, cp):
        #name = "tmp/"+cp.getName()
        name=tmp+cp.getName()
        print "removing old bcg files"
        process = Popen (["svl","-clean",name], cwd="tmp/", shell = False, stdout=PIPE)
        #print process.stdout
        output = process.communicate()
        process = Popen (["rm",name+"_compo.bcg",name+"_compo_sync.bcg",name+"_cp.bcg"], cwd="tmp/", stderr=PIPE, stdout=PIPE)
        #print process.stdout
        process.communicate()
        print "removing old diagnostics"
        process = Popen (["rm","synchronizability.bcg","realizability.bcg"], cwd="tmp/", stderr=PIPE, stdout=PIPE)
        #print process.stdout
        process.communicate()

    def generateLTS(self, cp, debugOutput = False):
        #name = "tmp/"+cp.getName()
        name=cp.getName()
        
        print "run LTS generation for choreo"
        process = Popen (["svl", name], cwd="tmp/", shell = False, stdin=PIPE, stdout=PIPE, stderr=PIPE)
        process.communicate()
        
        if debugOutput:
            print output[0]
            print output[1]
        if process.returncode != 0:
            return False
        else:
            return True

    def isSynchronizableP(self, cp, debugOutput = False):
        print "run synchronizability check"
        #name = "tmp/"+cp.getName()
        name=cp.getName()
        process = Popen (["svl",name+"-synchronizability"], cwd="tmp/", shell = False, stdout=PIPE)
        output = process.communicate()
        if debugOutput:
            if os.path.isfile ("synchronizability.bcg"):
                process = Popen (["bcg_info","-labels","synchronizability.bcg"], cwd="tmp/", shell = False, stdout=PIPE)
                print process.communicate()[0]
       # return not (os.path.isfile ("synchronizability.bcg"))
        os.chdir("tmp/")
        return not (os.path.isfile ("synchronizability.bcg"))
         

    def isRealizableP(self, cp, debugOutput = False):
        print "run realizability check"
        #name = "tmp/"+cp.getName()
        name=cp.getName()
        process = Popen (["svl",name+"-realizability"], cwd="tmp/", shell = False, stdout=PIPE)
        output = process.communicate()
        if debugOutput:
            if os.path.isfile ("realizability.bcg"):
                process = Popen (["bcg_info","-labels","realizability.bcg"], cwd="tmp/", shell = False, stdout=PIPE)
                print process.communicate()[0]
        os.chdir("tmp/")
        return not (os.path.isfile ("realizability.bcg"))
        

    def readCounterEx(self, cp, diagFile = "realizability.bcg"):
        #name="tmp/"+cp.getName()
        name=cp.getName()
        process = Popen (["bcg_info","-labels", diagFile], cwd="tmp/", shell = False, stdout=PIPE)
        output = process.communicate()[0]
        lines = output.split('\n')
        counterList = filter (lambda line: line.find('Present in '+name+'_compo_min.bcg: ') != -1, lines)
        if len (counterList) == 0:
            # counterList = filter (lambda line: line.find('Absent in compo_min.bcg: ') != -1, lines)
            # if len(counterList) == 0:
            print "Error, did not find useable counterexample in choreo!"
            return
        counterExFull = counterList[0]
        counterEx = counterExFull[len ('Present in '+name+'_compo_min.bcg: '):(len (counterExFull))]
        return counterEx


    def extendChoreo(self, cp, sender, receiver, number, msg, askIfMultiple = False):
        """ extends a choreography given a transition that was a diagnostics in the
            synchronizability check

        cp -- the choreography to extend
        sender -- the sending peer
        receiver -- the receiving peer
        msg -- the message of the transition in the diagnostic
        """
        global numTran
        print "extending choreo because of ", sender, receiver, msg, " transition"
        offendingLabel = cp.createLabel (sender, receiver, msg)
        transitionsToFix = cp.findOffendingTransitions (offendingLabel)

	numTran=len(transitionsToFix)
        if len(transitionsToFix) > 1:
            print "multiple possible transitions found\n" #TODO: read whole counterex and use breadth first search"
            if not askIfMultiple:
                return False
            i = 0
            for t in transitionsToFix:
                print "("+i+")",
                #print i,
                #print ")",
                t.printTransition()
                print "\n"
                i = i + 1
            try:
                #number = int(raw_input('which to fix:'))
                #print "you chose number", number
                transitionsToFix = [transitionsToFix[number]]
            except ValueError:
                print "Not a number"
                return False

        print "transitions needed to fix ", map (lambda trans: trans.printTransition(), transitionsToFix)
        synchroState = transitionsToFix[0].getSource()
        print "synchro State is ", synchroState.printState()
        fixTrans = transitionsToFix[0]
        return cp.insertSynchroMessage(fixTrans)


    def checkChoreo(self, cp, smartReduction = True, debugInfoMonitors = False):
        
        choreoName = tmp+cp.getName()
        
     
        synchronizabilityLoop = True
        if debugInfoMonitors: 
            if not cp.checkAllConditions():
                print "This choreography is faulty"
            else:
                print "This choreography is not a faulty one and thus repairable"
                print "======\n"
        number=0
        while True:
            startTime = time()
            print "checking this choreography"
            cp.printCP()

            print "generate LNT file"
            cp.cp2lnt (True)
            if not smartReduction:
                print "generate SVL with compositional verification"
            else:
                print "generate SVL file with smart reduction"
            cp.generateSVL(smartReduction, debugInfoMonitors)

            self.cleanUp(cp)
            if not self.generateLTS(cp, False):
                print "error in LTS generation, return svl manually to see the errors\n"
                print "===\n"
                break


            if synchronizabilityLoop:
                if self.isSynchronizableP(cp, True):
                    print "this choreography is synchronizable in this form"
                    print "===\n"
                    synchronizabilityLoop = False
                    os.chdir("../")
                else:
                    print "this choreography is not synchronizable and not realizable in this form"
                    print "===\n"
                    if not debugInfoMonitors:
                        return True
                    os.chdir("../")
                    self.isRealizableP(cp, True)
                    os.chdir("../")
                    counterex = self.readCounterEx(cp)
                        # FIXME: CADP changes to upper case!
                        # currently the examples are lower case, but this is not guaranteed
            if not synchronizabilityLoop:
                #self.isSynchronizableP(cp, True)
                if self.isRealizableP(cp, True):
                    #self.isSynchronizableP(cp, True)
                    print "the choreography is realizable in this form"
                    print "===\n"
                    if not debugInfoMonitors:
                        return True
                    elapsed = time() - startTime
                    print "iteration time: ", elapsed, "s"
                    break
                else:
                    print "the choreography is not realizable in this form"
                    print "===\n"
                    if not debugInfoMonitors:
                        return True
                    os.chdir("../")
                    counterex = self.readCounterEx(cp, "realizability.bcg")
            if counterex == "exit":
                print "found exit in bcg, unclear what to do\n===\n"
                break

            elapsed = time() - startTime
            print "iteration time: ", elapsed, "s"

		
            [sender, receiver, msg] = counterex.lower().split('_')
	    global numTran
            numTran=0
            if not self.extendChoreo(cp, sender, receiver, number, msg, True):
                 print "===\n"
                 break
            if number==numTran-1: ## GWEN SAYS: ?? pourquoi pas toujours number=0 ?
                 number=0
            else:
                 number=number+1





if __name__ == '__main__':

	import sys
	import os
	from subprocess import Popen, PIPE, call
        SUFFIX = ".bcg"
	

	if len(sys.argv) == 2:
		infile = sys.argv[1]
		# check if CIF (xml suffix)
        	elements = infile.split(SUFFIX)
       	 	if (len(elements)>=2 and elements[-1]==''):
            		filename = infile[:-(len(SUFFIX))]
                        
            		basefilename = os.path.basename(filename)
            		dirfilename = os.path.dirname(filename)
        	else:
            		print "%s: wrong resource type (should be .bcg)" % infile
            		sys.exit(1)

                cdm='cp '+infile+' tmp/'
		call(cdm, shell=True)
		s3=basefilename+".bcg"
                s4=basefilename+"_bpmnlts_min.aut"
                
                p3=Popen(['bcg_io', s3, s4], cwd="tmp/",  stdout=PIPE)
		#p3=Popen(['bcg_io', s3, '/home/linaye/copy/c0003_bpmnlts_min.aut'], stdout=PIPE)
                #p3.communicate()
	        p3.wait()
                
                s4=tmp+s4
		p4=Popen(['python', 'aut2cp.py', s4, basefilename], stdout=PIPE)
                #p4.communicate()
		p4.wait()
                
		#import cpExa
                sys.path.append("tmp/")
                cpExa1=basefilename+"_cpExa"  
                exec("from "+cpExa1+" import *")
                #import importlib
                #cpExa=importlib.import_module("%s", cpExa1)
		ex=ExternalExamples()
		checker = Checker()
                #cp1=NonSynchro_cp_example
                
		liste = [#ex.cpSSP(), ex.cpSSPv2(), ex.cpSSPSimple(),
             #ex.cpND3v33a()#, ex.cpND3v2(), ex.cpND3v3()
             #ex.cpNdNonEqv1(), ex.cpNdNonEqv2(), ex.cpNdNonEqv3(), ex.cpNdNonEqv4(),
             #ex.cpNdConfLoop(), ex.cpNdConfLoopv1(),
             #ex.cpNdConfLoopv4(),
             #ex.cpNdConfExitv7()
             # ex.cpND3v51(), ex.cpND3v52(), ex.cpND3v53(), ex.cpND3v54(),# ex.cpND3v55(), ex.cpND3v56(), ex.cpND3v57(),
             # ex.cpMultSync(),
             # ex.cpMultIncoming(),
             # ex.cpFASEExample()
            # ex.cp0032()
			ex.cp_example()
             #ex.cpMultIncomingv6()
             # ex.cpND3v55()
             #ex.cpND3v6()#, ex.cpND3v5()
             #ex.cpNonConfv1(), ex.cpNonConfv2(), ex.cpNonConfv3()
             #ex.cpNonConfv4()
             ]
                
		map (lambda ex: checker.checkChoreo(ex, True, True), liste)
                
    # checker.checkChoreo(ex.cpNdConfLoop(), False, False)
    # checker.checkChoreo(ex.cpSSP(), False)

    # checker.checkChoreo(ex.cpSSPv3(), False)
    # checker.checkChoreo(ex.cpSSPSimple(), False)

    # checker.checkChoreo(ex.cpND3(), False)
    # checker.checkChoreo(ex.cpND3v2(), False)
    # checker.checkChoreo(ex.cpND3v3(), False)

    #cp = ex.cp0031()

    # cp.printCP()

    # checker.extendChoreo(cp, "cl", "appli", "access")

    # cp.cp2lnt(True)
    # cp.generateSVL(True, True)
    # cp.printCP()


    # checker.checkChoreo(ex.cp0065(), False)
    # checker.checkChoreo(ex.cp0016(), False)
    # checker.checkChoreo(ex.cp0063(), True)
    # checker.checkChoreo(ex.cp0066(), False)

    # checker.checkChoreo(ex.cp0032(), True) # 1h 38m mit smart reduction
    #                                  # 1h 23m  roceade2

    # checker.checkChoreo(ex.cp0031(), True)
    # cp = ex.cp0031()
    # cp.cp2lnt(False)
    # cp.generateSVL(True)

    # cp = ex.cp0031()
    # trans = cp.findOffendingTransitions(cp.createLabel("p2","p0","g1503"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p3","p4","g1504"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p5","p4","g1507"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p0","p4","g1502"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p0","p4","g1511"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p2","p6","g1505"))
    # cp.insertSynchroMessage(trans[0])

    # cp.cp2lnt(True)
    # cp.generateSVL(True)

    # cp = ex.cp0032()
    # trans = cp.findOffendingTransitions(cp.createLabel("p4","p8","g1525"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p6","p3","g1519"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p7","p1","g1524"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p8","p0","g1516"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p8","p2","g1521"))
    # cp.insertSynchroMessage(trans[0])
    # trans = cp.findOffendingTransitions(cp.createLabel("p2","p6","g1526"))
    # cp.insertSynchroMessage(trans[0])

    # cp.cp2lnt(True)
    # cp.generateSVL(True)

    # exampleList = [ex.cp0100(), ex.cp0101(), ex.cp0102(),
    #                ex.cp0104(), ex.cp0105(), ex.cp0106(), ex.cp0107(),
    #                ex.cp0108(), ex.cp0109(), ex.cp0110(), ex.cp0111(),
    #                ex.cp0112(), ex.cp0113(), ex.cp0114(), ex.cp0115(),
    #                ex.cp0116(), ex.cp0117(), ex.cp0118(), ex.cp0119()]

    # map(lambda choreo: checker.checkChoreo(choreo), exampleList)
    # map(lambda choreo: choreo.cp2lnt(), exampleList)
    # map(lambda choreo: choreo.generateSVL(True), exampleList)

    #checker.checkChoreo(ex.cp0063(), True, True)

   # """
   # TODO!!!!
   # checker.checkChoreo(ex.cp0061(), False, True)
   # """
   # """
   # exampleList =[ex.simpleChoice(), ex.msgSimpleChoice(),
   #               ex.notSoSimpleChoice(), ex.msgNotSoSimpleChoice(),
   #               ex.sendEachOther(), ex.msgSendEachOther(),
   #               ex.twoSyncsLoop(),
   #               ex.cp0001(),ex.cp0002(),ex.cp0003(),ex.cp0004(),
   #               ex.cp0005(),ex.cp0006(),ex.cp0007(),ex.cp0008(),
   #               ex.cp0009(),ex.cp0010(),
   #               ex.cp0011(),ex.cp0012(),ex.cp0013(),
   #               ex.cp0014(),ex.cp0015(),ex.cp0016(),
   #               ex.cp0017(),
   #               ex.cp0018(),
   #               ex.cp0019(),ex.cp0020(),ex.cp0021(),
   #               ex.cp0022(),ex.cp0023(),ex.cp0024(),ex.cp0025(),
   #               ex.cp0026(),ex.cp0027(),ex.cp0028(),
   #               ex.cp0029(), ex.cp0030(),
   #               ex.cp0031(),
                  #ex.cp0032()]
   #               ex.cp0033(),
                  #ex.cp0034(),
   #               ex.cp0035(),
   #               ex.cp0036(),ex.cp0037(),ex.cp0038(),ex.cp0039(),
   #               ex.cp0040(),ex.cp0041(),
   #               ex.cp0042(),ex.cp0043(),ex.cp0044(),ex.cp0045(),
   #               ex.cp0046(),ex.cp0047(),ex.cp0048(),ex.cp0049(),ex.cp0050(),
   #               ex.cp0051(),
   #               ex.cp0052(),ex.cp0053(),ex.cp0054(),ex.cp0055(),
   #               ex.cp0056(),ex.cp0057(),ex.cp0058(),ex.cp0059()
   #               ]
   # map (lambda example: checker.checkChoreo(example), exampleList)
   # """
