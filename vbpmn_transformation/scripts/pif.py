# ./pif.py
# -*- coding: utf-8 -*-
# PyXB bindings for NM:b01d317243bbb4790921b3422744d96a69dec30d
# Generated 2021-07-22 15:44:46.728904 by PyXB version 1.2.6 using Python 3.8.10.final.0
# Namespace http://www.example.org/PIF

from __future__ import unicode_literals
import pyxb
import pyxb.binding
import pyxb.binding.saxer
import io
import pyxb.utils.utility
import pyxb.utils.domutils
import sys
import pyxb.utils.six as _six
# Unique identifier for bindings created at the same time
_GenerationUID = pyxb.utils.utility.UniqueIdentifier('urn:uuid:fa464658-eaf2-11eb-90fe-0b42d1e22c80')

# Version of PyXB used to generate the bindings
_PyXBVersion = '1.2.6'
# Generated bindings are not compatible across PyXB versions
if pyxb.__version__ != _PyXBVersion:
    raise pyxb.PyXBVersionError(_PyXBVersion)

# A holder for module-level binding classes so we can access them from
# inside class definitions where property names may conflict.
_module_typeBindings = pyxb.utils.utility.Object()

# Import bindings for namespaces imported into schema
import pyxb.binding.datatypes

# NOTE: All namespace declarations are reserved within the binding
Namespace = pyxb.namespace.NamespaceForURI('http://www.example.org/PIF', create_if_missing=True)
Namespace.configureCategories(['typeBinding', 'elementBinding'])

def CreateFromDocument (xml_text, default_namespace=None, location_base=None):
    """Parse the given XML and use the document element to create a
    Python instance.

    @param xml_text An XML document.  This should be data (Python 2
    str or Python 3 bytes), or a text (Python 2 unicode or Python 3
    str) in the L{pyxb._InputEncoding} encoding.

    @keyword default_namespace The L{pyxb.Namespace} instance to use as the
    default namespace where there is no default namespace in scope.
    If unspecified or C{None}, the namespace of the module containing
    this function will be used.

    @keyword location_base: An object to be recorded as the base of all
    L{pyxb.utils.utility.Location} instances associated with events and
    objects handled by the parser.  You might pass the URI from which
    the document was obtained.
    """

    if pyxb.XMLStyle_saxer != pyxb._XMLStyle:
        dom = pyxb.utils.domutils.StringToDOM(xml_text)
        return CreateFromDOM(dom.documentElement, default_namespace=default_namespace)
    if default_namespace is None:
        default_namespace = Namespace.fallbackNamespace()
    saxer = pyxb.binding.saxer.make_parser(fallback_namespace=default_namespace, location_base=location_base)
    handler = saxer.getContentHandler()
    xmld = xml_text
    if isinstance(xmld, _six.text_type):
        xmld = xmld.encode(pyxb._InputEncoding)
    saxer.parse(io.BytesIO(xmld))
    instance = handler.rootObject()
    return instance

def CreateFromDOM (node, default_namespace=None):
    """Create a Python instance from the given DOM node.
    The node tag must correspond to an element declaration in this module.

    @deprecated: Forcing use of DOM interface is unnecessary; use L{CreateFromDocument}."""
    if default_namespace is None:
        default_namespace = Namespace.fallbackNamespace()
    return pyxb.binding.basis.element.AnyCreateFromDOM(node, default_namespace)


# Complex type {http://www.example.org/PIF}Peer with content type EMPTY
class Peer (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}Peer with content type EMPTY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Peer')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 10, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'id'), 'id', '__httpwww_example_orgPIF_Peer_id', pyxb.binding.datatypes.ID, required=True)
    __id._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 11, 5)
    __id._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 11, 5)
    
    id = property(__id.value, __id.set, None, None)

    _ElementMap.update({
        
    })
    _AttributeMap.update({
        __id.name() : __id
    })
_module_typeBindings.Peer = Peer
Namespace.addCategoryObject('typeBinding', 'Peer', Peer)


# Complex type {http://www.example.org/PIF}Message with content type EMPTY
class Message (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}Message with content type EMPTY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Message')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 14, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'id'), 'id', '__httpwww_example_orgPIF_Message_id', pyxb.binding.datatypes.ID, required=True)
    __id._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 15, 5)
    __id._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 15, 5)
    
    id = property(__id.value, __id.set, None, None)

    _ElementMap.update({
        
    })
    _AttributeMap.update({
        __id.name() : __id
    })
_module_typeBindings.Message = Message
Namespace.addCategoryObject('typeBinding', 'Message', Message)


# Complex type {http://www.example.org/PIF}Workflow with content type ELEMENT_ONLY
class Workflow (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}Workflow with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Workflow')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 18, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}nodes uses Python identifier nodes
    __nodes = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'nodes'), 'nodes', '__httpwww_example_orgPIF_Workflow_httpwww_example_orgPIFnodes', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 20, 6), )

    
    nodes = property(__nodes.value, __nodes.set, None, None)

    
    # Element {http://www.example.org/PIF}sequenceFlows uses Python identifier sequenceFlows
    __sequenceFlows = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'sequenceFlows'), 'sequenceFlows', '__httpwww_example_orgPIF_Workflow_httpwww_example_orgPIFsequenceFlows', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 23, 6), )

    
    sequenceFlows = property(__sequenceFlows.value, __sequenceFlows.set, None, None)

    
    # Element {http://www.example.org/PIF}initialNode uses Python identifier initialNode
    __initialNode = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'initialNode'), 'initialNode', '__httpwww_example_orgPIF_Workflow_httpwww_example_orgPIFinitialNode', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 26, 6), )

    
    initialNode = property(__initialNode.value, __initialNode.set, None, '')

    
    # Element {http://www.example.org/PIF}finalNodes uses Python identifier finalNodes
    __finalNodes = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'finalNodes'), 'finalNodes', '__httpwww_example_orgPIF_Workflow_httpwww_example_orgPIFfinalNodes', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 35, 6), )

    
    finalNodes = property(__finalNodes.value, __finalNodes.set, None, None)

    _ElementMap.update({
        __nodes.name() : __nodes,
        __sequenceFlows.name() : __sequenceFlows,
        __initialNode.name() : __initialNode,
        __finalNodes.name() : __finalNodes
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.Workflow = Workflow
Namespace.addCategoryObject('typeBinding', 'Workflow', Workflow)


# Complex type {http://www.example.org/PIF}WorkflowNode with content type ELEMENT_ONLY
class WorkflowNode (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}WorkflowNode with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'WorkflowNode')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 42, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}incomingFlows uses Python identifier incomingFlows
    __incomingFlows = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows'), 'incomingFlows', '__httpwww_example_orgPIF_WorkflowNode_httpwww_example_orgPIFincomingFlows', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6), )

    
    incomingFlows = property(__incomingFlows.value, __incomingFlows.set, None, None)

    
    # Element {http://www.example.org/PIF}outgoingFlows uses Python identifier outgoingFlows
    __outgoingFlows = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows'), 'outgoingFlows', '__httpwww_example_orgPIF_WorkflowNode_httpwww_example_orgPIFoutgoingFlows', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6), )

    
    outgoingFlows = property(__outgoingFlows.value, __outgoingFlows.set, None, None)

    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'id'), 'id', '__httpwww_example_orgPIF_WorkflowNode_id', pyxb.binding.datatypes.ID, required=True)
    __id._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 51, 5)
    __id._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 51, 5)
    
    id = property(__id.value, __id.set, None, None)

    _ElementMap.update({
        __incomingFlows.name() : __incomingFlows,
        __outgoingFlows.name() : __outgoingFlows
    })
    _AttributeMap.update({
        __id.name() : __id
    })
_module_typeBindings.WorkflowNode = WorkflowNode
Namespace.addCategoryObject('typeBinding', 'WorkflowNode', WorkflowNode)


# Complex type {http://www.example.org/PIF}SequenceFlow with content type EMPTY
class SequenceFlow (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}SequenceFlow with content type EMPTY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'SequenceFlow')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 54, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute id uses Python identifier id
    __id = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'id'), 'id', '__httpwww_example_orgPIF_SequenceFlow_id', pyxb.binding.datatypes.ID, required=True)
    __id._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 55, 5)
    __id._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 55, 5)
    
    id = property(__id.value, __id.set, None, None)

    
    # Attribute source uses Python identifier source
    __source = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'source'), 'source', '__httpwww_example_orgPIF_SequenceFlow_source', pyxb.binding.datatypes.IDREF, required=True)
    __source._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 56, 5)
    __source._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 56, 5)
    
    source = property(__source.value, __source.set, None, '')

    
    # Attribute target uses Python identifier target
    __target = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'target'), 'target', '__httpwww_example_orgPIF_SequenceFlow_target', pyxb.binding.datatypes.IDREF, required=True)
    __target._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 65, 5)
    __target._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 65, 5)
    
    target = property(__target.value, __target.set, None, '')

    _ElementMap.update({
        
    })
    _AttributeMap.update({
        __id.name() : __id,
        __source.name() : __source,
        __target.name() : __target
    })
_module_typeBindings.SequenceFlow = SequenceFlow
Namespace.addCategoryObject('typeBinding', 'SequenceFlow', SequenceFlow)


# Complex type {http://www.example.org/PIF}Condition with content type EMPTY
class Condition (pyxb.binding.basis.complexTypeDefinition):
    """Complex type {http://www.example.org/PIF}Condition with content type EMPTY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_EMPTY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Condition')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 171, 4)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Attribute value uses Python identifier value_
    __value = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'value'), 'value_', '__httpwww_example_orgPIF_Condition_value', pyxb.binding.datatypes.string, required=True)
    __value._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 172, 5)
    __value._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 172, 5)
    
    value_ = property(__value.value, __value.set, None, None)

    _ElementMap.update({
        
    })
    _AttributeMap.update({
        __value.name() : __value
    })
_module_typeBindings.Condition = Condition
Namespace.addCategoryObject('typeBinding', 'Condition', Condition)


# Complex type [anonymous] with content type ELEMENT_ONLY
class CTD_ANON (pyxb.binding.basis.complexTypeDefinition):
    """Complex type [anonymous] with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = None
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 218, 8)
    _ElementMap = {}
    _AttributeMap = {}
    # Base type is pyxb.binding.datatypes.anyType
    
    # Element {http://www.example.org/PIF}name uses Python identifier name
    __name = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'name'), 'name', '__httpwww_example_orgPIF_CTD_ANON_httpwww_example_orgPIFname', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 220, 16), )

    
    name = property(__name.value, __name.set, None, None)

    
    # Element {http://www.example.org/PIF}documentation uses Python identifier documentation
    __documentation = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'documentation'), 'documentation', '__httpwww_example_orgPIF_CTD_ANON_httpwww_example_orgPIFdocumentation', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 223, 16), )

    
    documentation = property(__documentation.value, __documentation.set, None, None)

    
    # Element {http://www.example.org/PIF}peers uses Python identifier peers
    __peers = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'peers'), 'peers', '__httpwww_example_orgPIF_CTD_ANON_httpwww_example_orgPIFpeers', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 224, 16), )

    
    peers = property(__peers.value, __peers.set, None, None)

    
    # Element {http://www.example.org/PIF}messages uses Python identifier messages
    __messages = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'messages'), 'messages', '__httpwww_example_orgPIF_CTD_ANON_httpwww_example_orgPIFmessages', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 227, 16), )

    
    messages = property(__messages.value, __messages.set, None, None)

    
    # Element {http://www.example.org/PIF}behaviour uses Python identifier behaviour
    __behaviour = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'behaviour'), 'behaviour', '__httpwww_example_orgPIF_CTD_ANON_httpwww_example_orgPIFbehaviour', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 228, 16), )

    
    behaviour = property(__behaviour.value, __behaviour.set, None, None)

    _ElementMap.update({
        __name.name() : __name,
        __documentation.name() : __documentation,
        __peers.name() : __peers,
        __messages.name() : __messages,
        __behaviour.name() : __behaviour
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.CTD_ANON = CTD_ANON


# Complex type {http://www.example.org/PIF}InitialEvent with content type ELEMENT_ONLY
class InitialEvent (WorkflowNode):
    """Complex type {http://www.example.org/PIF}InitialEvent with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'InitialEvent')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 76, 4)
    _ElementMap = WorkflowNode._ElementMap.copy()
    _AttributeMap = WorkflowNode._AttributeMap.copy()
    # Base type is WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.InitialEvent = InitialEvent
Namespace.addCategoryObject('typeBinding', 'InitialEvent', InitialEvent)


# Complex type {http://www.example.org/PIF}EndEvent with content type ELEMENT_ONLY
class EndEvent (WorkflowNode):
    """Complex type {http://www.example.org/PIF}EndEvent with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'EndEvent')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 82, 4)
    _ElementMap = WorkflowNode._ElementMap.copy()
    _AttributeMap = WorkflowNode._AttributeMap.copy()
    # Base type is WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.EndEvent = EndEvent
Namespace.addCategoryObject('typeBinding', 'EndEvent', EndEvent)


# Complex type {http://www.example.org/PIF}Communication with content type ELEMENT_ONLY
class Communication (WorkflowNode):
    """Complex type {http://www.example.org/PIF}Communication with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Communication')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 88, 4)
    _ElementMap = WorkflowNode._ElementMap.copy()
    _AttributeMap = WorkflowNode._AttributeMap.copy()
    # Base type is WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message uses Python identifier message
    __message = pyxb.binding.content.AttributeUse(pyxb.namespace.ExpandedName(None, 'message'), 'message', '__httpwww_example_orgPIF_Communication_message', pyxb.binding.datatypes.IDREF, required=True)
    __message._DeclarationLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 91, 7)
    __message._UseLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 91, 7)
    
    message = property(__message.value, __message.set, None, '')

    _ElementMap.update({
        
    })
    _AttributeMap.update({
        __message.name() : __message
    })
_module_typeBindings.Communication = Communication
Namespace.addCategoryObject('typeBinding', 'Communication', Communication)


# Complex type {http://www.example.org/PIF}Gateway with content type ELEMENT_ONLY
class Gateway (WorkflowNode):
    """Complex type {http://www.example.org/PIF}Gateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Gateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 143, 4)
    _ElementMap = WorkflowNode._ElementMap.copy()
    _AttributeMap = WorkflowNode._AttributeMap.copy()
    # Base type is WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.Gateway = Gateway
Namespace.addCategoryObject('typeBinding', 'Gateway', Gateway)


# Complex type {http://www.example.org/PIF}ConditionalSequenceFlow with content type ELEMENT_ONLY
class ConditionalSequenceFlow (SequenceFlow):
    """Complex type {http://www.example.org/PIF}ConditionalSequenceFlow with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'ConditionalSequenceFlow')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 161, 4)
    _ElementMap = SequenceFlow._ElementMap.copy()
    _AttributeMap = SequenceFlow._AttributeMap.copy()
    # Base type is SequenceFlow
    
    # Element {http://www.example.org/PIF}condition uses Python identifier condition
    __condition = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'condition'), 'condition', '__httpwww_example_orgPIF_ConditionalSequenceFlow_httpwww_example_orgPIFcondition', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 165, 8), )

    
    condition = property(__condition.value, __condition.set, None, None)

    
    # Attribute id inherited from {http://www.example.org/PIF}SequenceFlow
    
    # Attribute source inherited from {http://www.example.org/PIF}SequenceFlow
    
    # Attribute target inherited from {http://www.example.org/PIF}SequenceFlow
    _ElementMap.update({
        __condition.name() : __condition
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.ConditionalSequenceFlow = ConditionalSequenceFlow
Namespace.addCategoryObject('typeBinding', 'ConditionalSequenceFlow', ConditionalSequenceFlow)


# Complex type {http://www.example.org/PIF}Task with content type ELEMENT_ONLY
class Task (WorkflowNode):
    """Complex type {http://www.example.org/PIF}Task with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Task')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 211, 4)
    _ElementMap = WorkflowNode._ElementMap.copy()
    _AttributeMap = WorkflowNode._AttributeMap.copy()
    # Base type is WorkflowNode
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.Task = Task
Namespace.addCategoryObject('typeBinding', 'Task', Task)


# Complex type {http://www.example.org/PIF}Interaction with content type ELEMENT_ONLY
class Interaction (Communication):
    """Complex type {http://www.example.org/PIF}Interaction with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'Interaction')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 105, 4)
    _ElementMap = Communication._ElementMap.copy()
    _AttributeMap = Communication._AttributeMap.copy()
    # Base type is Communication
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element {http://www.example.org/PIF}initiatingPeer uses Python identifier initiatingPeer
    __initiatingPeer = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'initiatingPeer'), 'initiatingPeer', '__httpwww_example_orgPIF_Interaction_httpwww_example_orgPIFinitiatingPeer', False, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 109, 8), )

    
    initiatingPeer = property(__initiatingPeer.value, __initiatingPeer.set, None, '')

    
    # Element {http://www.example.org/PIF}receivingPeers uses Python identifier receivingPeers
    __receivingPeers = pyxb.binding.content.ElementDeclaration(pyxb.namespace.ExpandedName(Namespace, 'receivingPeers'), 'receivingPeers', '__httpwww_example_orgPIF_Interaction_httpwww_example_orgPIFreceivingPeers', True, pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 118, 8), )

    
    receivingPeers = property(__receivingPeers.value, __receivingPeers.set, None, None)

    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    _ElementMap.update({
        __initiatingPeer.name() : __initiatingPeer,
        __receivingPeers.name() : __receivingPeers
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.Interaction = Interaction
Namespace.addCategoryObject('typeBinding', 'Interaction', Interaction)


# Complex type {http://www.example.org/PIF}MessageCommunication with content type ELEMENT_ONLY
class MessageCommunication (Communication):
    """Complex type {http://www.example.org/PIF}MessageCommunication with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'MessageCommunication')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 125, 4)
    _ElementMap = Communication._ElementMap.copy()
    _AttributeMap = Communication._AttributeMap.copy()
    # Base type is Communication
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.MessageCommunication = MessageCommunication
Namespace.addCategoryObject('typeBinding', 'MessageCommunication', MessageCommunication)


# Complex type {http://www.example.org/PIF}SplitGateway with content type ELEMENT_ONLY
class SplitGateway (Gateway):
    """Complex type {http://www.example.org/PIF}SplitGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'SplitGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 149, 4)
    _ElementMap = Gateway._ElementMap.copy()
    _AttributeMap = Gateway._AttributeMap.copy()
    # Base type is Gateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.SplitGateway = SplitGateway
Namespace.addCategoryObject('typeBinding', 'SplitGateway', SplitGateway)


# Complex type {http://www.example.org/PIF}JoinGateway with content type ELEMENT_ONLY
class JoinGateway (Gateway):
    """Complex type {http://www.example.org/PIF}JoinGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = True
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'JoinGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 155, 4)
    _ElementMap = Gateway._ElementMap.copy()
    _AttributeMap = Gateway._AttributeMap.copy()
    # Base type is Gateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.JoinGateway = JoinGateway
Namespace.addCategoryObject('typeBinding', 'JoinGateway', JoinGateway)


# Complex type {http://www.example.org/PIF}MessageSending with content type ELEMENT_ONLY
class MessageSending (MessageCommunication):
    """Complex type {http://www.example.org/PIF}MessageSending with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'MessageSending')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 131, 4)
    _ElementMap = MessageCommunication._ElementMap.copy()
    _AttributeMap = MessageCommunication._AttributeMap.copy()
    # Base type is MessageCommunication
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.MessageSending = MessageSending
Namespace.addCategoryObject('typeBinding', 'MessageSending', MessageSending)


# Complex type {http://www.example.org/PIF}MessageReception with content type ELEMENT_ONLY
class MessageReception (MessageCommunication):
    """Complex type {http://www.example.org/PIF}MessageReception with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'MessageReception')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 137, 4)
    _ElementMap = MessageCommunication._ElementMap.copy()
    _AttributeMap = MessageCommunication._AttributeMap.copy()
    # Base type is MessageCommunication
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute message inherited from {http://www.example.org/PIF}Communication
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.MessageReception = MessageReception
Namespace.addCategoryObject('typeBinding', 'MessageReception', MessageReception)


# Complex type {http://www.example.org/PIF}OrJoinGateway with content type ELEMENT_ONLY
class OrJoinGateway (JoinGateway):
    """Complex type {http://www.example.org/PIF}OrJoinGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'OrJoinGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 175, 4)
    _ElementMap = JoinGateway._ElementMap.copy()
    _AttributeMap = JoinGateway._AttributeMap.copy()
    # Base type is JoinGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.OrJoinGateway = OrJoinGateway
Namespace.addCategoryObject('typeBinding', 'OrJoinGateway', OrJoinGateway)


# Complex type {http://www.example.org/PIF}XOrJoinGateway with content type ELEMENT_ONLY
class XOrJoinGateway (JoinGateway):
    """Complex type {http://www.example.org/PIF}XOrJoinGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'XOrJoinGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 181, 4)
    _ElementMap = JoinGateway._ElementMap.copy()
    _AttributeMap = JoinGateway._AttributeMap.copy()
    # Base type is JoinGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.XOrJoinGateway = XOrJoinGateway
Namespace.addCategoryObject('typeBinding', 'XOrJoinGateway', XOrJoinGateway)


# Complex type {http://www.example.org/PIF}AndJoinGateway with content type ELEMENT_ONLY
class AndJoinGateway (JoinGateway):
    """Complex type {http://www.example.org/PIF}AndJoinGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'AndJoinGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 187, 4)
    _ElementMap = JoinGateway._ElementMap.copy()
    _AttributeMap = JoinGateway._AttributeMap.copy()
    # Base type is JoinGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.AndJoinGateway = AndJoinGateway
Namespace.addCategoryObject('typeBinding', 'AndJoinGateway', AndJoinGateway)


# Complex type {http://www.example.org/PIF}OrSplitGateway with content type ELEMENT_ONLY
class OrSplitGateway (SplitGateway):
    """Complex type {http://www.example.org/PIF}OrSplitGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'OrSplitGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 193, 4)
    _ElementMap = SplitGateway._ElementMap.copy()
    _AttributeMap = SplitGateway._AttributeMap.copy()
    # Base type is SplitGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.OrSplitGateway = OrSplitGateway
Namespace.addCategoryObject('typeBinding', 'OrSplitGateway', OrSplitGateway)


# Complex type {http://www.example.org/PIF}XOrSplitGateway with content type ELEMENT_ONLY
class XOrSplitGateway (SplitGateway):
    """Complex type {http://www.example.org/PIF}XOrSplitGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'XOrSplitGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 199, 4)
    _ElementMap = SplitGateway._ElementMap.copy()
    _AttributeMap = SplitGateway._AttributeMap.copy()
    # Base type is SplitGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.XOrSplitGateway = XOrSplitGateway
Namespace.addCategoryObject('typeBinding', 'XOrSplitGateway', XOrSplitGateway)


# Complex type {http://www.example.org/PIF}AndSplitGateway with content type ELEMENT_ONLY
class AndSplitGateway (SplitGateway):
    """Complex type {http://www.example.org/PIF}AndSplitGateway with content type ELEMENT_ONLY"""
    _TypeDefinition = None
    _ContentTypeTag = pyxb.binding.basis.complexTypeDefinition._CT_ELEMENT_ONLY
    _Abstract = False
    _ExpandedName = pyxb.namespace.ExpandedName(Namespace, 'AndSplitGateway')
    _XSDLocation = pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 205, 4)
    _ElementMap = SplitGateway._ElementMap.copy()
    _AttributeMap = SplitGateway._AttributeMap.copy()
    # Base type is SplitGateway
    
    # Element incomingFlows ({http://www.example.org/PIF}incomingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Element outgoingFlows ({http://www.example.org/PIF}outgoingFlows) inherited from {http://www.example.org/PIF}WorkflowNode
    
    # Attribute id inherited from {http://www.example.org/PIF}WorkflowNode
    _ElementMap.update({
        
    })
    _AttributeMap.update({
        
    })
_module_typeBindings.AndSplitGateway = AndSplitGateway
Namespace.addCategoryObject('typeBinding', 'AndSplitGateway', AndSplitGateway)


Process = pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'Process'), CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 217, 4))
Namespace.addCategoryObject('elementBinding', Process.name().localName(), Process)



Workflow._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'nodes'), WorkflowNode, scope=Workflow, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 20, 6)))

Workflow._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'sequenceFlows'), SequenceFlow, scope=Workflow, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 23, 6)))

Workflow._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'initialNode'), pyxb.binding.datatypes.IDREF, scope=Workflow, documentation='', location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 26, 6)))

Workflow._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'finalNodes'), pyxb.binding.datatypes.IDREF, scope=Workflow, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 35, 6)))

def _BuildAutomaton ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton
    del _BuildAutomaton
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 23, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 35, 6))
    counters.add(cc_1)
    states = []
    final_update = None
    symbol = pyxb.binding.content.ElementUse(Workflow._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'nodes')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 20, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(Workflow._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'sequenceFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 23, 6))
    st_1 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    final_update = set()
    symbol = pyxb.binding.content.ElementUse(Workflow._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'initialNode')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 26, 6))
    st_2 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_2)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(Workflow._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'finalNodes')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 35, 6))
    st_3 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_3)
    transitions = []
    transitions.append(fac.Transition(st_0, [
         ]))
    transitions.append(fac.Transition(st_1, [
         ]))
    transitions.append(fac.Transition(st_2, [
         ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_2, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_1._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_3, [
         ]))
    st_2._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_3, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_3._set_transitionSet(transitions)
    return fac.Automaton(states, counters, False, containing_state=None)
Workflow._Automaton = _BuildAutomaton()




WorkflowNode._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows'), pyxb.binding.datatypes.IDREF, scope=WorkflowNode, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6)))

WorkflowNode._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows'), pyxb.binding.datatypes.IDREF, scope=WorkflowNode, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6)))

def _BuildAutomaton_ ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_
    del _BuildAutomaton_
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(WorkflowNode._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(WorkflowNode._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
WorkflowNode._Automaton = _BuildAutomaton_()




CTD_ANON._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'name'), pyxb.binding.datatypes.string, scope=CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 220, 16)))

CTD_ANON._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'documentation'), pyxb.binding.datatypes.string, scope=CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 223, 16)))

CTD_ANON._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'peers'), Peer, scope=CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 224, 16)))

CTD_ANON._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'messages'), Message, scope=CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 227, 16)))

CTD_ANON._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'behaviour'), Workflow, scope=CTD_ANON, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 228, 16)))

def _BuildAutomaton_2 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_2
    del _BuildAutomaton_2
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 224, 16))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 227, 16))
    counters.add(cc_1)
    states = []
    final_update = None
    symbol = pyxb.binding.content.ElementUse(CTD_ANON._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'name')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 220, 16))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(CTD_ANON._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'documentation')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 223, 16))
    st_1 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(CTD_ANON._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'peers')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 224, 16))
    st_2 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_2)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(CTD_ANON._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'messages')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 227, 16))
    st_3 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_3)
    final_update = set()
    symbol = pyxb.binding.content.ElementUse(CTD_ANON._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'behaviour')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 228, 16))
    st_4 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_4)
    transitions = []
    transitions.append(fac.Transition(st_1, [
         ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_2, [
         ]))
    transitions.append(fac.Transition(st_3, [
         ]))
    transitions.append(fac.Transition(st_4, [
         ]))
    st_1._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_2, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_3, [
        fac.UpdateInstruction(cc_0, False) ]))
    transitions.append(fac.Transition(st_4, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_2._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_3, [
        fac.UpdateInstruction(cc_1, True) ]))
    transitions.append(fac.Transition(st_4, [
        fac.UpdateInstruction(cc_1, False) ]))
    st_3._set_transitionSet(transitions)
    transitions = []
    st_4._set_transitionSet(transitions)
    return fac.Automaton(states, counters, False, containing_state=None)
CTD_ANON._Automaton = _BuildAutomaton_2()




def _BuildAutomaton_3 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_3
    del _BuildAutomaton_3
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(InitialEvent._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(InitialEvent._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
InitialEvent._Automaton = _BuildAutomaton_3()




def _BuildAutomaton_4 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_4
    del _BuildAutomaton_4
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(EndEvent._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(EndEvent._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
EndEvent._Automaton = _BuildAutomaton_4()




def _BuildAutomaton_5 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_5
    del _BuildAutomaton_5
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(Communication._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(Communication._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
Communication._Automaton = _BuildAutomaton_5()




def _BuildAutomaton_6 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_6
    del _BuildAutomaton_6
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(Gateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(Gateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
Gateway._Automaton = _BuildAutomaton_6()




ConditionalSequenceFlow._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'condition'), Condition, scope=ConditionalSequenceFlow, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 165, 8)))

def _BuildAutomaton_7 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_7
    del _BuildAutomaton_7
    import pyxb.utils.fac as fac

    counters = set()
    states = []
    final_update = set()
    symbol = pyxb.binding.content.ElementUse(ConditionalSequenceFlow._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'condition')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 165, 8))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    transitions = []
    st_0._set_transitionSet(transitions)
    return fac.Automaton(states, counters, False, containing_state=None)
ConditionalSequenceFlow._Automaton = _BuildAutomaton_7()




def _BuildAutomaton_8 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_8
    del _BuildAutomaton_8
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(Task._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(Task._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
Task._Automaton = _BuildAutomaton_8()




Interaction._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'initiatingPeer'), pyxb.binding.datatypes.IDREF, scope=Interaction, documentation='', location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 109, 8)))

Interaction._AddElement(pyxb.binding.basis.element(pyxb.namespace.ExpandedName(Namespace, 'receivingPeers'), pyxb.binding.datatypes.IDREF, scope=Interaction, location=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 118, 8)))

def _BuildAutomaton_9 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_9
    del _BuildAutomaton_9
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = None
    symbol = pyxb.binding.content.ElementUse(Interaction._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(Interaction._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    final_update = None
    symbol = pyxb.binding.content.ElementUse(Interaction._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'initiatingPeer')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 109, 8))
    st_2 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_2)
    final_update = set()
    symbol = pyxb.binding.content.ElementUse(Interaction._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'receivingPeers')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 118, 8))
    st_3 = fac.State(symbol, is_initial=False, final_update=final_update, is_unordered_catenation=False)
    states.append(st_3)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    transitions.append(fac.Transition(st_2, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    transitions.append(fac.Transition(st_2, [
        fac.UpdateInstruction(cc_1, False) ]))
    st_1._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_3, [
         ]))
    st_2._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_3, [
         ]))
    st_3._set_transitionSet(transitions)
    return fac.Automaton(states, counters, False, containing_state=None)
Interaction._Automaton = _BuildAutomaton_9()




def _BuildAutomaton_10 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_10
    del _BuildAutomaton_10
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(MessageCommunication._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(MessageCommunication._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
MessageCommunication._Automaton = _BuildAutomaton_10()




def _BuildAutomaton_11 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_11
    del _BuildAutomaton_11
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(SplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(SplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
SplitGateway._Automaton = _BuildAutomaton_11()




def _BuildAutomaton_12 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_12
    del _BuildAutomaton_12
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(JoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(JoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
JoinGateway._Automaton = _BuildAutomaton_12()




def _BuildAutomaton_13 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_13
    del _BuildAutomaton_13
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(MessageSending._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(MessageSending._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
MessageSending._Automaton = _BuildAutomaton_13()




def _BuildAutomaton_14 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_14
    del _BuildAutomaton_14
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(MessageReception._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(MessageReception._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
MessageReception._Automaton = _BuildAutomaton_14()




def _BuildAutomaton_15 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_15
    del _BuildAutomaton_15
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(OrJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(OrJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
OrJoinGateway._Automaton = _BuildAutomaton_15()




def _BuildAutomaton_16 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_16
    del _BuildAutomaton_16
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(XOrJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(XOrJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
XOrJoinGateway._Automaton = _BuildAutomaton_16()




def _BuildAutomaton_17 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_17
    del _BuildAutomaton_17
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(AndJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(AndJoinGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
AndJoinGateway._Automaton = _BuildAutomaton_17()




def _BuildAutomaton_18 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_18
    del _BuildAutomaton_18
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(OrSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(OrSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
OrSplitGateway._Automaton = _BuildAutomaton_18()




def _BuildAutomaton_19 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_19
    del _BuildAutomaton_19
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(XOrSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(XOrSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
XOrSplitGateway._Automaton = _BuildAutomaton_19()




def _BuildAutomaton_20 ():
    # Remove this helper function from the namespace after it is invoked
    global _BuildAutomaton_20
    del _BuildAutomaton_20
    import pyxb.utils.fac as fac

    counters = set()
    cc_0 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    counters.add(cc_0)
    cc_1 = fac.CounterCondition(min=0, max=None, metadata=pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    counters.add(cc_1)
    states = []
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_0, False))
    symbol = pyxb.binding.content.ElementUse(AndSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'incomingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 44, 6))
    st_0 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_0)
    final_update = set()
    final_update.add(fac.UpdateInstruction(cc_1, False))
    symbol = pyxb.binding.content.ElementUse(AndSplitGateway._UseForTag(pyxb.namespace.ExpandedName(Namespace, 'outgoingFlows')), pyxb.utils.utility.Location('/home/silverquick/Documents/pyxb/pif.xsd', 47, 6))
    st_1 = fac.State(symbol, is_initial=True, final_update=final_update, is_unordered_catenation=False)
    states.append(st_1)
    transitions = []
    transitions.append(fac.Transition(st_0, [
        fac.UpdateInstruction(cc_0, True) ]))
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_0, False) ]))
    st_0._set_transitionSet(transitions)
    transitions = []
    transitions.append(fac.Transition(st_1, [
        fac.UpdateInstruction(cc_1, True) ]))
    st_1._set_transitionSet(transitions)
    return fac.Automaton(states, counters, True, containing_state=None)
AndSplitGateway._Automaton = _BuildAutomaton_20()