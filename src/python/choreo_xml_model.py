# ./choreo_xml_model.py
# PyXB bindings for NamespaceModule
# NSM:4faaf6d64aa0b3eb9eaae08fa224b1e35187c22d
# Generated 2013-07-12 10:55:40.247303 by PyXB version 1.1.1
import pyxb
import pyxb.binding
import pyxb.binding.saxer
import StringIO
import pyxb.utils.utility
import pyxb.utils.domutils
import sys

# Unique identifier for bindings created at the same time
_GenerationUID = pyxb.utils.utility.UniqueIdentifier('urn:uuid:d3cba818-ead0-11e2-84fe-080027ec9cbb')

# Import bindings for namespaces imported into schema
import pyxb.binding.datatypes

Namespace = pyxb.namespace.NamespaceForURI(u'http://convecs.inria.fr', create_if_missing=True)
Namespace.configureCategories(['typeBinding', 'elementBinding'])
ModuleRecord = Namespace.lookupModuleRecordByUID(_GenerationUID, create_if_missing=True)
ModuleRecord._setModule(sys.modules[__name__])

def CreateFromDocument (xml_text, default_namespace=None, location_base=None):
    """Parse the given XML and use the document element to create a Python instance."""
    if pyxb.XMLStyle_saxer != pyxb._XMLStyle:
        dom = pyxb.utils.domutils.StringToDOM(xml_text)
        return CreateFromDOM(dom.documentElement)
    saxer = pyxb.binding.saxer.make_parser(fallback_namespace=Namespace.fallbackNamespace(), location_base=location_base)
    handler = saxer.getContentHandler()
    saxer.parse(StringIO.StringIO(xml_text))
    instance = handler.rootObject()
    return instance

def CreateFromDOM (node, default_namespace=None):
    """Create a Python instance from the given DOM node.
    The node tag must correspond to an element declaration in this module.

    @deprecated: Forcing use of DOM interface is unnecessary; use L{CreateFromDocument}."""
    if default_namespace is None:
        default_namespace = Namespace.fallbackNamespace()
    return pyxb.binding.basis.element.AnyCreateFromDOM(node, _fallback_namespace=default_namespace)


# Atomic SimpleTypeDefinition
class id (pyxb.binding.datatypes.string):

    """An atomic simple type."""

    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'id')
    _Documentation = None
id._InitializeFacetMap()
Namespace.addCategoryObject('typeBinding', u'id', id)

# List SimpleTypeDefinition
# superclasses pyxb.binding.datatypes.anySimpleType
class successorList (pyxb.binding.basis.STD_list):

    """Simple type that is a list of id."""

    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'successorList')
    _Documentation = None

    _ItemType = id
successorList._InitializeFacetMap()
Namespace.addCategoryObject('typeBinding', u'successorList', successorList)

# List SimpleTypeDefinition
# superclasses successorList
class STD_ANON_1 (pyxb.binding.basis.STD_list):

    """Simple type that is a list of id."""

    _ExpandedName = None
    _Documentation = None

    _ItemType = id
STD_ANON_1._CF_length = pyxb.binding.facets.CF_length(value=pyxb.binding.datatypes.nonNegativeInteger(1L))
STD_ANON_1._InitializeFacetMap(STD_ANON_1._CF_length)

# List SimpleTypeDefinition
# superclasses successorList
class STD_ANON_2 (pyxb.binding.basis.STD_list):

    """Simple type that is a list of id."""

    _ExpandedName = None
    _Documentation = None

    _ItemType = id
STD_ANON_2._CF_minLength = pyxb.binding.facets.CF_minLength(value=pyxb.binding.datatypes.nonNegativeInteger(2L))
STD_ANON_2._InitializeFacetMap(STD_ANON_2._CF_minLength)

# Complex type baseState with content type ELEMENT_ONLY
class baseState (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'baseState')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}stateID uses Python identifier stateID
    __stateID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'stateID'), 'stateID', '__httpconvecs_inria_fr_baseState_httpconvecs_inria_frstateID', False)

    
    stateID = property(__stateID.value, __stateID.set, None, None)


    _ElementMap = {
        __stateID.name() : __stateID
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'baseState', baseState)


# Complex type oneSuccState with content type ELEMENT_ONLY
class oneSuccState (baseState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'oneSuccState')
    # Base type is baseState
    
    # Element {http://convecs.inria.fr}successors uses Python identifier successors
    __successors = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'successors'), 'successors', '__httpconvecs_inria_fr_oneSuccState_httpconvecs_inria_frsuccessors', False)

    
    successors = property(__successors.value, __successors.set, None, None)

    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = baseState._ElementMap.copy()
    _ElementMap.update({
        __successors.name() : __successors
    })
    _AttributeMap = baseState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'oneSuccState', oneSuccState)


# Complex type joinState with content type ELEMENT_ONLY
class joinState (oneSuccState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'joinState')
    # Base type is oneSuccState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = oneSuccState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = oneSuccState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'joinState', joinState)


# Complex type simpleJoinState with content type ELEMENT_ONLY
class simpleJoinState (joinState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'simpleJoinState')
    # Base type is joinState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = joinState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = joinState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'simpleJoinState', simpleJoinState)


# Complex type message with content type ELEMENT_ONLY
class message (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'message')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}messageContent uses Python identifier messageContent
    __messageContent = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'messageContent'), 'messageContent', '__httpconvecs_inria_fr_message_httpconvecs_inria_frmessageContent', False)

    
    messageContent = property(__messageContent.value, __messageContent.set, None, None)

    
    # Element {http://convecs.inria.fr}msgID uses Python identifier msgID
    __msgID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'msgID'), 'msgID', '__httpconvecs_inria_fr_message_httpconvecs_inria_frmsgID', False)

    
    msgID = property(__msgID.value, __msgID.set, None, None)

    
    # Element {http://convecs.inria.fr}sender uses Python identifier sender
    __sender = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'sender'), 'sender', '__httpconvecs_inria_fr_message_httpconvecs_inria_frsender', False)

    
    sender = property(__sender.value, __sender.set, None, None)

    
    # Element {http://convecs.inria.fr}receiver uses Python identifier receiver
    __receiver = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'receiver'), 'receiver', '__httpconvecs_inria_fr_message_httpconvecs_inria_frreceiver', False)

    
    receiver = property(__receiver.value, __receiver.set, None, None)


    _ElementMap = {
        __messageContent.name() : __messageContent,
        __msgID.name() : __msgID,
        __sender.name() : __sender,
        __receiver.name() : __receiver
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'message', message)


# Complex type severalSuccState with content type ELEMENT_ONLY
class severalSuccState (baseState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'severalSuccState')
    # Base type is baseState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element {http://convecs.inria.fr}successors uses Python identifier successors
    __successors = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'successors'), 'successors', '__httpconvecs_inria_fr_severalSuccState_httpconvecs_inria_frsuccessors', False)

    
    successors = property(__successors.value, __successors.set, None, None)


    _ElementMap = baseState._ElementMap.copy()
    _ElementMap.update({
        __successors.name() : __successors
    })
    _AttributeMap = baseState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'severalSuccState', severalSuccState)


# Complex type selectionState with content type ELEMENT_ONLY
class selectionState (severalSuccState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'selectionState')
    # Base type is severalSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}severalSuccState

    _ElementMap = severalSuccState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = severalSuccState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'selectionState', selectionState)


# Complex type allSelectState with content type ELEMENT_ONLY
class allSelectState (selectionState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'allSelectState')
    # Base type is selectionState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}severalSuccState

    _ElementMap = selectionState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = selectionState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'allSelectState', allSelectState)


# Complex type choiceState with content type ELEMENT_ONLY
class choiceState (selectionState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'choiceState')
    # Base type is selectionState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}severalSuccState

    _ElementMap = selectionState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = selectionState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'choiceState', choiceState)


# Complex type action with content type ELEMENT_ONLY
class action (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'action')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}actionContent uses Python identifier actionContent
    __actionContent = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'actionContent'), 'actionContent', '__httpconvecs_inria_fr_action_httpconvecs_inria_fractionContent', False)

    
    actionContent = property(__actionContent.value, __actionContent.set, None, None)

    
    # Element {http://convecs.inria.fr}actionID uses Python identifier actionID
    __actionID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'actionID'), 'actionID', '__httpconvecs_inria_fr_action_httpconvecs_inria_fractionID', False)

    
    actionID = property(__actionID.value, __actionID.set, None, None)

    
    # Element {http://convecs.inria.fr}actor uses Python identifier actor
    __actor = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'actor'), 'actor', '__httpconvecs_inria_fr_action_httpconvecs_inria_fractor', False)

    
    actor = property(__actor.value, __actor.set, None, None)


    _ElementMap = {
        __actionContent.name() : __actionContent,
        __actionID.name() : __actionID,
        __actor.name() : __actor
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'action', action)


# Complex type finalState with content type ELEMENT_ONLY
class finalState (baseState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'finalState')
    # Base type is baseState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = baseState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = baseState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'finalState', finalState)


# Complex type peer with content type ELEMENT_ONLY
class peer (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'peer')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}peerID uses Python identifier peerID
    __peerID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'peerID'), 'peerID', '__httpconvecs_inria_fr_peer_httpconvecs_inria_frpeerID', False)

    
    peerID = property(__peerID.value, __peerID.set, None, None)


    _ElementMap = {
        __peerID.name() : __peerID
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'peer', peer)


# Complex type dominatedChoiceState with content type ELEMENT_ONLY
class dominatedChoiceState (selectionState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'dominatedChoiceState')
    # Base type is selectionState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element {http://convecs.inria.fr}dominantPeer uses Python identifier dominantPeer
    __dominantPeer = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'dominantPeer'), 'dominantPeer', '__httpconvecs_inria_fr_dominatedChoiceState_httpconvecs_inria_frdominantPeer', False)

    
    dominantPeer = property(__dominantPeer.value, __dominantPeer.set, None, None)

    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}severalSuccState

    _ElementMap = selectionState._ElementMap.copy()
    _ElementMap.update({
        __dominantPeer.name() : __dominantPeer
    })
    _AttributeMap = selectionState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'dominatedChoiceState', dominatedChoiceState)


# Complex type peerList with content type ELEMENT_ONLY
class peerList (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'peerList')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}peer uses Python identifier peer
    __peer = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'peer'), 'peer', '__httpconvecs_inria_fr_peerList_httpconvecs_inria_frpeer', True)

    
    peer = property(__peer.value, __peer.set, None, None)


    _ElementMap = {
        __peer.name() : __peer
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'peerList', peerList)


# Complex type initialState with content type ELEMENT_ONLY
class initialState (oneSuccState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'initialState')
    # Base type is oneSuccState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = oneSuccState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = oneSuccState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'initialState', initialState)


# Complex type stateMachine with content type ELEMENT_ONLY
class stateMachine (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'stateMachine')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}interaction uses Python identifier interaction
    __interaction = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'interaction'), 'interaction', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frinteraction', True)

    
    interaction = property(__interaction.value, __interaction.set, None, None)

    
    # Element {http://convecs.inria.fr}simpleJoin uses Python identifier simpleJoin
    __simpleJoin = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'simpleJoin'), 'simpleJoin', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frsimpleJoin', True)

    
    simpleJoin = property(__simpleJoin.value, __simpleJoin.set, None, None)

    
    # Element {http://convecs.inria.fr}initial uses Python identifier initial
    __initial = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'initial'), 'initial', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frinitial', False)

    
    initial = property(__initial.value, __initial.set, None, None)

    
    # Element {http://convecs.inria.fr}final uses Python identifier final
    __final = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'final'), 'final', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frfinal', True)

    
    final = property(__final.value, __final.set, None, None)

    
    # Element {http://convecs.inria.fr}subsetSelect uses Python identifier subsetSelect
    __subsetSelect = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'subsetSelect'), 'subsetSelect', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frsubsetSelect', True)

    
    subsetSelect = property(__subsetSelect.value, __subsetSelect.set, None, None)

    
    # Element {http://convecs.inria.fr}allSelect uses Python identifier allSelect
    __allSelect = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'allSelect'), 'allSelect', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frallSelect', True)

    
    allSelect = property(__allSelect.value, __allSelect.set, None, None)

    
    # Element {http://convecs.inria.fr}choice uses Python identifier choice
    __choice = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'choice'), 'choice', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frchoice', True)

    
    choice = property(__choice.value, __choice.set, None, None)

    
    # Element {http://convecs.inria.fr}internalAction uses Python identifier internalAction
    __internalAction = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'internalAction'), 'internalAction', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frinternalAction', True)

    
    internalAction = property(__internalAction.value, __internalAction.set, None, None)

    
    # Element {http://convecs.inria.fr}allJoin uses Python identifier allJoin
    __allJoin = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'allJoin'), 'allJoin', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frallJoin', True)

    
    allJoin = property(__allJoin.value, __allJoin.set, None, None)

    
    # Element {http://convecs.inria.fr}subsetJoin uses Python identifier subsetJoin
    __subsetJoin = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'subsetJoin'), 'subsetJoin', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frsubsetJoin', True)

    
    subsetJoin = property(__subsetJoin.value, __subsetJoin.set, None, None)

    
    # Element {http://convecs.inria.fr}dominatedChoice uses Python identifier dominatedChoice
    __dominatedChoice = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'dominatedChoice'), 'dominatedChoice', '__httpconvecs_inria_fr_stateMachine_httpconvecs_inria_frdominatedChoice', True)

    
    dominatedChoice = property(__dominatedChoice.value, __dominatedChoice.set, None, None)


    _ElementMap = {
        __interaction.name() : __interaction,
        __simpleJoin.name() : __simpleJoin,
        __initial.name() : __initial,
        __final.name() : __final,
        __subsetSelect.name() : __subsetSelect,
        __allSelect.name() : __allSelect,
        __choice.name() : __choice,
        __internalAction.name() : __internalAction,
        __allJoin.name() : __allJoin,
        __subsetJoin.name() : __subsetJoin,
        __dominatedChoice.name() : __dominatedChoice
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'stateMachine', stateMachine)


# Complex type interactionState with content type ELEMENT_ONLY
class interactionState (oneSuccState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'interactionState')
    # Base type is oneSuccState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element {http://convecs.inria.fr}msgID uses Python identifier msgID
    __msgID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'msgID'), 'msgID', '__httpconvecs_inria_fr_interactionState_httpconvecs_inria_frmsgID', False)

    
    msgID = property(__msgID.value, __msgID.set, None, None)


    _ElementMap = oneSuccState._ElementMap.copy()
    _ElementMap.update({
        __msgID.name() : __msgID
    })
    _AttributeMap = oneSuccState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'interactionState', interactionState)


# Complex type messageList with content type ELEMENT_ONLY
class messageList (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'messageList')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}action uses Python identifier action
    __action = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'action'), 'action', '__httpconvecs_inria_fr_messageList_httpconvecs_inria_fraction', True)

    
    action = property(__action.value, __action.set, None, None)

    
    # Element {http://convecs.inria.fr}message uses Python identifier message
    __message = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'message'), 'message', '__httpconvecs_inria_fr_messageList_httpconvecs_inria_frmessage', True)

    
    message = property(__message.value, __message.set, None, None)


    _ElementMap = {
        __action.name() : __action,
        __message.name() : __message
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'messageList', messageList)


# Complex type CTD_ANON_1 with content type ELEMENT_ONLY
class CTD_ANON_1 (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = None
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://convecs.inria.fr}stateMachine uses Python identifier stateMachine
    __stateMachine = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'stateMachine'), 'stateMachine', '__httpconvecs_inria_fr_CTD_ANON_1_httpconvecs_inria_frstateMachine', False)

    
    stateMachine = property(__stateMachine.value, __stateMachine.set, None, None)

    
    # Element {http://convecs.inria.fr}alphabet uses Python identifier alphabet
    __alphabet = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'alphabet'), 'alphabet', '__httpconvecs_inria_fr_CTD_ANON_1_httpconvecs_inria_fralphabet', False)

    
    alphabet = property(__alphabet.value, __alphabet.set, None, None)

    
    # Element {http://convecs.inria.fr}choreoID uses Python identifier choreoID
    __choreoID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'choreoID'), 'choreoID', '__httpconvecs_inria_fr_CTD_ANON_1_httpconvecs_inria_frchoreoID', False)

    
    choreoID = property(__choreoID.value, __choreoID.set, None, None)

    
    # Element {http://convecs.inria.fr}participants uses Python identifier participants
    __participants = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'participants'), 'participants', '__httpconvecs_inria_fr_CTD_ANON_1_httpconvecs_inria_frparticipants', False)

    
    participants = property(__participants.value, __participants.set, None, None)


    _ElementMap = {
        __stateMachine.name() : __stateMachine,
        __alphabet.name() : __alphabet,
        __choreoID.name() : __choreoID,
        __participants.name() : __participants
    }
    _AttributeMap = {
        
    }



# Complex type internalActionState with content type ELEMENT_ONLY
class internalActionState (oneSuccState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'internalActionState')
    # Base type is oneSuccState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element {http://convecs.inria.fr}actionID uses Python identifier actionID
    __actionID = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'actionID'), 'actionID', '__httpconvecs_inria_fr_internalActionState_httpconvecs_inria_fractionID', False)

    
    actionID = property(__actionID.value, __actionID.set, None, None)


    _ElementMap = oneSuccState._ElementMap.copy()
    _ElementMap.update({
        __actionID.name() : __actionID
    })
    _AttributeMap = oneSuccState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'internalActionState', internalActionState)


# Complex type subsetJoinState with content type ELEMENT_ONLY
class subsetJoinState (joinState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'subsetJoinState')
    # Base type is joinState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = joinState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = joinState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'subsetJoinState', subsetJoinState)


# Complex type allJoinState with content type ELEMENT_ONLY
class allJoinState (joinState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'allJoinState')
    # Base type is joinState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}oneSuccState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState

    _ElementMap = joinState._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = joinState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'allJoinState', allJoinState)


# Complex type subsetSelectState with content type ELEMENT_ONLY
class subsetSelectState (selectionState):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'subsetSelectState')
    # Base type is selectionState
    
    # Element stateID ({http://convecs.inria.fr}stateID) inherited from {http://convecs.inria.fr}baseState
    
    # Element successors ({http://convecs.inria.fr}successors) inherited from {http://convecs.inria.fr}severalSuccState
    
    # Element {http://convecs.inria.fr}default uses Python identifier default
    __default = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'default'), 'default', '__httpconvecs_inria_fr_subsetSelectState_httpconvecs_inria_frdefault', False)

    
    default = property(__default.value, __default.set, None, None)


    _ElementMap = selectionState._ElementMap.copy()
    _ElementMap.update({
        __default.name() : __default
    })
    _AttributeMap = selectionState._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'subsetSelectState', subsetSelectState)


choreography = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'choreography'), CTD_ANON_1)
Namespace.addCategoryObject('elementBinding', choreography.name().localName(), choreography)



baseState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'stateID'), id, scope=baseState))
baseState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=baseState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
    ])
})



oneSuccState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'successors'), STD_ANON_1, scope=oneSuccState))
oneSuccState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=oneSuccState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=oneSuccState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


joinState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=joinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=joinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


simpleJoinState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=simpleJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=simpleJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})



message._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'messageContent'), id, scope=message))

message._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'msgID'), id, scope=message))

message._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'sender'), id, scope=message))

message._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'receiver'), id, scope=message))
message._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=message._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'msgID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=message._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'sender'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=message._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'receiver'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=5, element_use=message._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'messageContent'))),
    ])
    , 5 : pyxb.binding.content.ContentModelState(state=5, is_final=True, transitions=[
    ])
})



severalSuccState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'successors'), STD_ANON_2, scope=severalSuccState))
severalSuccState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=severalSuccState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=severalSuccState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


selectionState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=selectionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=selectionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


allSelectState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=allSelectState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=allSelectState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


choiceState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=choiceState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=choiceState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})



action._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'actionContent'), id, scope=action))

action._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'actionID'), id, scope=action))

action._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'actor'), id, scope=action))
action._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=action._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'actionID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=action._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'actor'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=action._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'actionContent'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
    ])
})


finalState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=finalState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
    ])
})



peer._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'peerID'), id, scope=peer))
peer._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=peer._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'peerID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
    ])
})



dominatedChoiceState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'dominantPeer'), id, scope=dominatedChoiceState))
dominatedChoiceState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=dominatedChoiceState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=dominatedChoiceState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=dominatedChoiceState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'dominantPeer'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
    ])
})



peerList._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'peer'), peer, scope=peerList))
peerList._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=peerList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'peer'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=peerList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'peer'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=peerList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'peer'))),
    ])
})


initialState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=initialState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=initialState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})



stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'interaction'), interactionState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'simpleJoin'), simpleJoinState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'initial'), initialState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'final'), finalState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'subsetSelect'), subsetSelectState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'allSelect'), allSelectState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'choice'), choiceState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'internalAction'), internalActionState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'allJoin'), allJoinState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'subsetJoin'), subsetJoinState, scope=stateMachine))

stateMachine._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'dominatedChoice'), dominatedChoiceState, scope=stateMachine))
stateMachine._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'initial'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'interaction'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'simpleJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'subsetSelect'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'allSelect'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'choice'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'internalAction'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'allJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'subsetJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'dominatedChoice'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'interaction'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'simpleJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'final'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'subsetSelect'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'internalAction'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'choice'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'allSelect'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'allJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'subsetJoin'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'dominatedChoice'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=stateMachine._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'final'))),
    ])
})



interactionState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'msgID'), id, scope=interactionState))
interactionState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=interactionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=interactionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=interactionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'msgID'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
    ])
})



messageList._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'action'), action, scope=messageList))

messageList._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'message'), message, scope=messageList))
messageList._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=messageList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'action'))),
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=messageList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'message'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=messageList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'action'))),
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=messageList._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'message'))),
    ])
})



CTD_ANON_1._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'stateMachine'), stateMachine, scope=CTD_ANON_1))

CTD_ANON_1._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'alphabet'), messageList, scope=CTD_ANON_1))

CTD_ANON_1._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'choreoID'), id, scope=CTD_ANON_1))

CTD_ANON_1._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'participants'), peerList, scope=CTD_ANON_1))
CTD_ANON_1._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=CTD_ANON_1._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'choreoID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=CTD_ANON_1._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'participants'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=CTD_ANON_1._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'alphabet'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=5, element_use=CTD_ANON_1._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateMachine'))),
    ])
    , 5 : pyxb.binding.content.ContentModelState(state=5, is_final=True, transitions=[
    ])
})



internalActionState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'actionID'), id, scope=internalActionState))
internalActionState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=internalActionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=internalActionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=internalActionState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'actionID'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
    ])
})


subsetJoinState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=subsetJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=subsetJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})


allJoinState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=allJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=allJoinState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
})



subsetSelectState._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'default'), id, scope=subsetSelectState))
subsetSelectState._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=subsetSelectState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'stateID'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=subsetSelectState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'successors'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=subsetSelectState._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'default'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=True, transitions=[
    ])
})
