# ./pif.py
# PyXB bindings for NamespaceModule
# NSM:b01d317243bbb4790921b3422744d96a69dec30d
# Generated 2015-01-28 09:23:28.072492 by PyXB version 1.1.1
import pyxb
import pyxb.binding
import pyxb.binding.saxer
import StringIO
import pyxb.utils.utility
import pyxb.utils.domutils
import sys

# Unique identifier for bindings created at the same time
_GenerationUID = pyxb.utils.utility.UniqueIdentifier('urn:uuid:ef85f851-a6c6-11e4-aabc-3c15c2d813b2')

# Import bindings for namespaces imported into schema
import pyxb.binding.datatypes

Namespace = pyxb.namespace.NamespaceForURI(u'http://www.example.org/PIF', create_if_missing=True)
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


# Complex type WorkflowNode_ with content type ELEMENT_ONLY
class WorkflowNode_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'WorkflowNode')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}outgoingFlows uses Python identifier outgoingFlows
    __outgoingFlows = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'), 'outgoingFlows', '__httpwww_example_orgPIF_WorkflowNode__httpwww_example_orgPIFoutgoingFlows', True)

    
    outgoingFlows = property(__outgoingFlows.value, __outgoingFlows.set, None, u'')

    
    # Element {http://www.example.org/PIF}incomingFlows uses Python identifier incomingFlows
    __incomingFlows = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'), 'incomingFlows', '__httpwww_example_orgPIF_WorkflowNode__httpwww_example_orgPIFincomingFlows', True)

    
    incomingFlows = property(__incomingFlows.value, __incomingFlows.set, None, u'')

    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'id'), 'id', '__httpwww_example_orgPIF_WorkflowNode__id', pyxb.binding.datatypes.ID, required=True)
    
    id = property(__id.value, __id.set, None, None)


    _ElementMap = {
        __outgoingFlows.name() : __outgoingFlows,
        __incomingFlows.name() : __incomingFlows
    }
    _AttributeMap = {
        __id.name() : __id
    }
Namespace.addCategoryObject('typeBinding', u'WorkflowNode', WorkflowNode_)


# Complex type Gateway_ with content type ELEMENT_ONLY
class Gateway_ (WorkflowNode_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Gateway')
    # Base type is WorkflowNode_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = WorkflowNode_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = WorkflowNode_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'Gateway', Gateway_)


# Complex type SplitGateway_ with content type ELEMENT_ONLY
class SplitGateway_ (Gateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'SplitGateway')
    # Base type is Gateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = Gateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = Gateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'SplitGateway', SplitGateway_)


# Complex type AndSplitGateway_ with content type ELEMENT_ONLY
class AndSplitGateway_ (SplitGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'AndSplitGateway')
    # Base type is SplitGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = SplitGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = SplitGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'AndSplitGateway', AndSplitGateway_)


# Complex type JoinGateway_ with content type ELEMENT_ONLY
class JoinGateway_ (Gateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'JoinGateway')
    # Base type is Gateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = Gateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = Gateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'JoinGateway', JoinGateway_)


# Complex type AndJoinGateway_ with content type ELEMENT_ONLY
class AndJoinGateway_ (JoinGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'AndJoinGateway')
    # Base type is JoinGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = JoinGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = JoinGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'AndJoinGateway', AndJoinGateway_)


# Complex type Communication_ with content type ELEMENT_ONLY
class Communication_ (WorkflowNode_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Communication')
    # Base type is WorkflowNode_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message uses Python identifier message
    __message = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'message'), 'message', '__httpwww_example_orgPIF_Communication__message', pyxb.binding.datatypes.IDREF, required=True)
    
    message = property(__message.value, __message.set, None, u'')

    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = WorkflowNode_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = WorkflowNode_._AttributeMap.copy()
    _AttributeMap.update({
        __message.name() : __message
    })
Namespace.addCategoryObject('typeBinding', u'Communication', Communication_)


# Complex type MessageCommunication_ with content type ELEMENT_ONLY
class MessageCommunication_ (Communication_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'MessageCommunication')
    # Base type is Communication_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = Communication_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = Communication_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'MessageCommunication', MessageCommunication_)


# Complex type XOrJoinGateway_ with content type ELEMENT_ONLY
class XOrJoinGateway_ (JoinGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'XOrJoinGateway')
    # Base type is JoinGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = JoinGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = JoinGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'XOrJoinGateway', XOrJoinGateway_)


# Complex type OrJoinGateway_ with content type ELEMENT_ONLY
class OrJoinGateway_ (JoinGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'OrJoinGateway')
    # Base type is JoinGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = JoinGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = JoinGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'OrJoinGateway', OrJoinGateway_)


# Complex type SequenceFlow_ with content type EMPTY
class SequenceFlow_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'SequenceFlow')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute source uses Python identifier source
    __source = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'source'), 'source', '__httpwww_example_orgPIF_SequenceFlow__source', pyxb.binding.datatypes.IDREF, required=True)
    
    source = property(__source.value, __source.set, None, u'')

    
    # Attribute target uses Python identifier target
    __target = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'target'), 'target', '__httpwww_example_orgPIF_SequenceFlow__target', pyxb.binding.datatypes.IDREF, required=True)
    
    target = property(__target.value, __target.set, None, u'')

    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'id'), 'id', '__httpwww_example_orgPIF_SequenceFlow__id', pyxb.binding.datatypes.ID, required=True)
    
    id = property(__id.value, __id.set, None, None)


    _ElementMap = {
        
    }
    _AttributeMap = {
        __source.name() : __source,
        __target.name() : __target,
        __id.name() : __id
    }
Namespace.addCategoryObject('typeBinding', u'SequenceFlow', SequenceFlow_)


# Complex type ConditionalSequenceFlow_ with content type ELEMENT_ONLY
class ConditionalSequenceFlow_ (SequenceFlow_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'ConditionalSequenceFlow')
    # Base type is SequenceFlow_
    
    # Element {http://www.example.org/PIF}condition uses Python identifier condition
    __condition = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'condition'), 'condition', '__httpwww_example_orgPIF_ConditionalSequenceFlow__httpwww_example_orgPIFcondition', False)

    
    condition = property(__condition.value, __condition.set, None, None)

    
    # Attribute source inherited from {http://www.example.org/PIF}SequenceFlow
    
    # Attribute target inherited from {http://www.example.org/PIF}SequenceFlow
    
    # Attribute id inherited from {http://www.example.org/PIF}SequenceFlow

    _ElementMap = SequenceFlow_._ElementMap.copy()
    _ElementMap.update({
        __condition.name() : __condition
    })
    _AttributeMap = SequenceFlow_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'ConditionalSequenceFlow', ConditionalSequenceFlow_)


# Complex type Peer_ with content type EMPTY
class Peer_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Peer')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'id'), 'id', '__httpwww_example_orgPIF_Peer__id', pyxb.binding.datatypes.ID, required=True)
    
    id = property(__id.value, __id.set, None, None)


    _ElementMap = {
        
    }
    _AttributeMap = {
        __id.name() : __id
    }
Namespace.addCategoryObject('typeBinding', u'Peer', Peer_)


# Complex type Condition_ with content type EMPTY
class Condition_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Condition')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute value uses Python identifier value_
    __value = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'value'), 'value_', '__httpwww_example_orgPIF_Condition__value', pyxb.binding.datatypes.string, required=True)
    
    value_ = property(__value.value, __value.set, None, None)


    _ElementMap = {
        
    }
    _AttributeMap = {
        __value.name() : __value
    }
Namespace.addCategoryObject('typeBinding', u'Condition', Condition_)


# Complex type Process_ with content type ELEMENT_ONLY
class Process_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Process')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}peers uses Python identifier peers
    __peers = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'peers'), 'peers', '__httpwww_example_orgPIF_Process__httpwww_example_orgPIFpeers', True)

    
    peers = property(__peers.value, __peers.set, None, None)

    
    # Element {http://www.example.org/PIF}documentation uses Python identifier documentation
    __documentation = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'documentation'), 'documentation', '__httpwww_example_orgPIF_Process__httpwww_example_orgPIFdocumentation', False)

    
    documentation = property(__documentation.value, __documentation.set, None, None)

    
    # Element {http://www.example.org/PIF}behaviour uses Python identifier behaviour
    __behaviour = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'behaviour'), 'behaviour', '__httpwww_example_orgPIF_Process__httpwww_example_orgPIFbehaviour', False)

    
    behaviour = property(__behaviour.value, __behaviour.set, None, None)

    
    # Element {http://www.example.org/PIF}messages uses Python identifier messages
    __messages = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'messages'), 'messages', '__httpwww_example_orgPIF_Process__httpwww_example_orgPIFmessages', True)

    
    messages = property(__messages.value, __messages.set, None, None)

    
    # Element {http://www.example.org/PIF}name uses Python identifier name
    __name = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'name'), 'name', '__httpwww_example_orgPIF_Process__httpwww_example_orgPIFname', False)

    
    name = property(__name.value, __name.set, None, None)


    _ElementMap = {
        __peers.name() : __peers,
        __documentation.name() : __documentation,
        __behaviour.name() : __behaviour,
        __messages.name() : __messages,
        __name.name() : __name
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'Process', Process_)


# Complex type Message_ with content type EMPTY
class Message_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Message')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, u'id'), 'id', '__httpwww_example_orgPIF_Message__id', pyxb.binding.datatypes.ID, required=True)
    
    id = property(__id.value, __id.set, None, None)


    _ElementMap = {
        
    }
    _AttributeMap = {
        __id.name() : __id
    }
Namespace.addCategoryObject('typeBinding', u'Message', Message_)


# Complex type Interaction_ with content type ELEMENT_ONLY
class Interaction_ (Communication_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Interaction')
    # Base type is Communication_
    
    # Element {http://www.example.org/PIF}initiatingPeer uses Python identifier initiatingPeer
    __initiatingPeer = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'initiatingPeer'), 'initiatingPeer', '__httpwww_example_orgPIF_Interaction__httpwww_example_orgPIFinitiatingPeer', False)

    
    initiatingPeer = property(__initiatingPeer.value, __initiatingPeer.set, None, u'')

    
    # Element {http://www.example.org/PIF}receivingPeers uses Python identifier receivingPeers
    __receivingPeers = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'receivingPeers'), 'receivingPeers', '__httpwww_example_orgPIF_Interaction__httpwww_example_orgPIFreceivingPeers', True)

    
    receivingPeers = property(__receivingPeers.value, __receivingPeers.set, None, u'')

    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = Communication_._ElementMap.copy()
    _ElementMap.update({
        __initiatingPeer.name() : __initiatingPeer,
        __receivingPeers.name() : __receivingPeers
    })
    _AttributeMap = Communication_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'Interaction', Interaction_)


# Complex type InitialEvent_ with content type ELEMENT_ONLY
class InitialEvent_ (WorkflowNode_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'InitialEvent')
    # Base type is WorkflowNode_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = WorkflowNode_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = WorkflowNode_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'InitialEvent', InitialEvent_)


# Complex type Workflow_ with content type ELEMENT_ONLY
class Workflow_ (pyxb.binding.basis.complexTypeDefinition):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Workflow')
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}nodes uses Python identifier nodes
    __nodes = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'nodes'), 'nodes', '__httpwww_example_orgPIF_Workflow__httpwww_example_orgPIFnodes', True)

    
    nodes = property(__nodes.value, __nodes.set, None, None)

    
    # Element {http://www.example.org/PIF}sequenceFlows uses Python identifier sequenceFlows
    __sequenceFlows = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'sequenceFlows'), 'sequenceFlows', '__httpwww_example_orgPIF_Workflow__httpwww_example_orgPIFsequenceFlows', True)

    
    sequenceFlows = property(__sequenceFlows.value, __sequenceFlows.set, None, None)

    
    # Element {http://www.example.org/PIF}finalNodes uses Python identifier finalNodes
    __finalNodes = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'finalNodes'), 'finalNodes', '__httpwww_example_orgPIF_Workflow__httpwww_example_orgPIFfinalNodes', True)

    
    finalNodes = property(__finalNodes.value, __finalNodes.set, None, u'')

    
    # Element {http://www.example.org/PIF}initialNode uses Python identifier initialNode
    __initialNode = pyxb.binding.content.ElementUse(pyxb.namespace.ExpandedName(Namespace, u'initialNode'), 'initialNode', '__httpwww_example_orgPIF_Workflow__httpwww_example_orgPIFinitialNode', False)

    
    initialNode = property(__initialNode.value, __initialNode.set, None, u'')


    _ElementMap = {
        __nodes.name() : __nodes,
        __sequenceFlows.name() : __sequenceFlows,
        __finalNodes.name() : __finalNodes,
        __initialNode.name() : __initialNode
    }
    _AttributeMap = {
        
    }
Namespace.addCategoryObject('typeBinding', u'Workflow', Workflow_)


# Complex type EndEvent_ with content type ELEMENT_ONLY
class EndEvent_ (WorkflowNode_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'EndEvent')
    # Base type is WorkflowNode_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = WorkflowNode_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = WorkflowNode_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'EndEvent', EndEvent_)


# Complex type MessageSending_ with content type ELEMENT_ONLY
class MessageSending_ (MessageCommunication_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'MessageSending')
    # Base type is MessageCommunication_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = MessageCommunication_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = MessageCommunication_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'MessageSending', MessageSending_)


# Complex type MessageReception_ with content type ELEMENT_ONLY
class MessageReception_ (MessageCommunication_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'MessageReception')
    # Base type is MessageCommunication_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = MessageCommunication_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = MessageCommunication_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'MessageReception', MessageReception_)


# Complex type Task_ with content type ELEMENT_ONLY
class Task_ (WorkflowNode_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'Task')
    # Base type is WorkflowNode_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = WorkflowNode_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = WorkflowNode_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'Task', Task_)


# Complex type OrSplitGateway_ with content type ELEMENT_ONLY
class OrSplitGateway_ (SplitGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'OrSplitGateway')
    # Base type is SplitGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = SplitGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = SplitGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'OrSplitGateway', OrSplitGateway_)


# Complex type XOrSplitGateway_ with content type ELEMENT_ONLY
class XOrSplitGateway_ (SplitGateway_):
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, u'XOrSplitGateway')
    # Base type is SplitGateway_
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode

    _ElementMap = SplitGateway_._ElementMap.copy()
    _ElementMap.update({
        
    })
    _AttributeMap = SplitGateway_._AttributeMap.copy()
    _AttributeMap.update({
        
    })
Namespace.addCategoryObject('typeBinding', u'XOrSplitGateway', XOrSplitGateway_)


AndSplitGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'AndSplitGateway'), AndSplitGateway_)
Namespace.addCategoryObject('elementBinding', AndSplitGateway.name().localName(), AndSplitGateway)

AndJoinGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'AndJoinGateway'), AndJoinGateway_)
Namespace.addCategoryObject('elementBinding', AndJoinGateway.name().localName(), AndJoinGateway)

MessageCommunication = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'MessageCommunication'), MessageCommunication_)
Namespace.addCategoryObject('elementBinding', MessageCommunication.name().localName(), MessageCommunication)

XOrJoinGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'XOrJoinGateway'), XOrJoinGateway_)
Namespace.addCategoryObject('elementBinding', XOrJoinGateway.name().localName(), XOrJoinGateway)

OrJoinGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'OrJoinGateway'), OrJoinGateway_)
Namespace.addCategoryObject('elementBinding', OrJoinGateway.name().localName(), OrJoinGateway)

SequenceFlow = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'SequenceFlow'), SequenceFlow_)
Namespace.addCategoryObject('elementBinding', SequenceFlow.name().localName(), SequenceFlow)

ConditionalSequenceFlow = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'ConditionalSequenceFlow'), ConditionalSequenceFlow_)
Namespace.addCategoryObject('elementBinding', ConditionalSequenceFlow.name().localName(), ConditionalSequenceFlow)

Condition = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Condition'), Condition_)
Namespace.addCategoryObject('elementBinding', Condition.name().localName(), Condition)

Process = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Process'), Process_)
Namespace.addCategoryObject('elementBinding', Process.name().localName(), Process)

Peer = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Peer'), Peer_)
Namespace.addCategoryObject('elementBinding', Peer.name().localName(), Peer)

Message = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Message'), Message_)
Namespace.addCategoryObject('elementBinding', Message.name().localName(), Message)

Interaction = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Interaction'), Interaction_)
Namespace.addCategoryObject('elementBinding', Interaction.name().localName(), Interaction)

InitialEvent = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'InitialEvent'), InitialEvent_)
Namespace.addCategoryObject('elementBinding', InitialEvent.name().localName(), InitialEvent)

Workflow = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Workflow'), Workflow_)
Namespace.addCategoryObject('elementBinding', Workflow.name().localName(), Workflow)

WorkflowNode = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'WorkflowNode'), WorkflowNode_)
Namespace.addCategoryObject('elementBinding', WorkflowNode.name().localName(), WorkflowNode)

EndEvent = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'EndEvent'), EndEvent_)
Namespace.addCategoryObject('elementBinding', EndEvent.name().localName(), EndEvent)

Communication = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Communication'), Communication_)
Namespace.addCategoryObject('elementBinding', Communication.name().localName(), Communication)

MessageSending = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'MessageSending'), MessageSending_)
Namespace.addCategoryObject('elementBinding', MessageSending.name().localName(), MessageSending)

MessageReception = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'MessageReception'), MessageReception_)
Namespace.addCategoryObject('elementBinding', MessageReception.name().localName(), MessageReception)

Gateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Gateway'), Gateway_)
Namespace.addCategoryObject('elementBinding', Gateway.name().localName(), Gateway)

Task = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'Task'), Task_)
Namespace.addCategoryObject('elementBinding', Task.name().localName(), Task)

SplitGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'SplitGateway'), SplitGateway_)
Namespace.addCategoryObject('elementBinding', SplitGateway.name().localName(), SplitGateway)

JoinGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'JoinGateway'), JoinGateway_)
Namespace.addCategoryObject('elementBinding', JoinGateway.name().localName(), JoinGateway)

OrSplitGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'OrSplitGateway'), OrSplitGateway_)
Namespace.addCategoryObject('elementBinding', OrSplitGateway.name().localName(), OrSplitGateway)

XOrSplitGateway = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'XOrSplitGateway'), XOrSplitGateway_)
Namespace.addCategoryObject('elementBinding', XOrSplitGateway.name().localName(), XOrSplitGateway)



WorkflowNode_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'), pyxb.binding.datatypes.IDREF, scope=WorkflowNode_, documentation=u''))

WorkflowNode_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'), pyxb.binding.datatypes.IDREF, scope=WorkflowNode_, documentation=u''))
WorkflowNode_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=WorkflowNode_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=WorkflowNode_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


Gateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Gateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Gateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


SplitGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=SplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=SplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


AndSplitGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=AndSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=AndSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


JoinGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=JoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=JoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


AndJoinGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=AndJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=AndJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


Communication_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Communication_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Communication_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


MessageCommunication_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageCommunication_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageCommunication_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


XOrJoinGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=XOrJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=XOrJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


OrJoinGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=OrJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=OrJoinGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})



ConditionalSequenceFlow_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'condition'), Condition_, scope=ConditionalSequenceFlow_))
ConditionalSequenceFlow_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=ConditionalSequenceFlow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'condition'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
    ])
})



Process_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'peers'), Peer_, scope=Process_))

Process_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'documentation'), pyxb.binding.datatypes.string, scope=Process_))

Process_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'behaviour'), Workflow_, scope=Process_))

Process_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'messages'), Message_, scope=Process_))

Process_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'name'), pyxb.binding.datatypes.string, scope=Process_))
Process_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=Process_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'name'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=Process_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'documentation'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=Process_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'peers'))),
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=Process_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'messages'))),
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=Process_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'behaviour'))),
    ])
})



Interaction_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'initiatingPeer'), pyxb.binding.datatypes.IDREF, scope=Interaction_, documentation=u''))

Interaction_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'receivingPeers'), pyxb.binding.datatypes.IDREF, scope=Interaction_, documentation=u''))
Interaction_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=Interaction_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'initiatingPeer'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Interaction_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Interaction_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=Interaction_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'receivingPeers'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=Interaction_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'receivingPeers'))),
    ])
})


InitialEvent_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=InitialEvent_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=InitialEvent_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})



Workflow_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'nodes'), WorkflowNode_, scope=Workflow_))

Workflow_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'sequenceFlows'), SequenceFlow_, scope=Workflow_))

Workflow_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'finalNodes'), pyxb.binding.datatypes.IDREF, scope=Workflow_, documentation=u''))

Workflow_._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, u'initialNode'), pyxb.binding.datatypes.IDREF, scope=Workflow_, documentation=u''))
Workflow_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'nodes'))),
    ])
    , 2 : pyxb.binding.content.ContentModelState(state=2, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'finalNodes'))),
    ])
    , 3 : pyxb.binding.content.ContentModelState(state=3, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=3, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'nodes'))),
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'sequenceFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'initialNode'))),
    ])
    , 4 : pyxb.binding.content.ContentModelState(state=4, is_final=False, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=2, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'initialNode'))),
        pyxb.binding.content.ContentModelTransition(next_state=4, element_use=Workflow_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'sequenceFlows'))),
    ])
})


EndEvent_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=EndEvent_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=EndEvent_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


MessageSending_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageSending_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageSending_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


MessageReception_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageReception_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=MessageReception_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


Task_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Task_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=Task_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


OrSplitGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=OrSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=OrSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})


XOrSplitGateway_._ContentModel = pyxb.binding.content.ContentModel(state_map = {
      1 : pyxb.binding.content.ContentModelState(state=1, is_final=True, transitions=[
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=XOrSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'outgoingFlows'))),
        pyxb.binding.content.ContentModelTransition(next_state=1, element_use=XOrSplitGateway_._UseForTag(pyxb.namespace.ExpandedName(Namespace, u'incomingFlows'))),
    ])
})
