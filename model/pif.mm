
class pif.Process  {
    name : String
    documentation : String
    @contained
    peers : pif.Peer[0,*]
    @contained
    messages : pif.Message[0,*]
    @contained
    behaviour : pif.Workflow
}

class pif.Peer  {
    name : String
}

class pif.Message  {
    name : String
}

class pif.Interaction : pif.Communication {
    initiatingPeer : pif.Peer
    receivingPeers : pif.Peer[1,*]
}

class pif.InitialEvent : pif.WorkflowNode {
}

class pif.Workflow  {
    @contained
    nodes : pif.WorkflowNode[1,*]
    initialNode : pif.InitialEvent
    finalNodes : pif.EndEvent[0,*]
    @contained
    sequenceFlows : pif.SequenceFlow[0,*]
}

class pif.WorkflowNode  {
    name : String
    outgoingFlows : pif.SequenceFlow[0,*] oppositeOf source
    incomingFlows : pif.SequenceFlow[0,*] oppositeOf target
}

class pif.EndEvent : pif.WorkflowNode {
}

class pif.Communication : pif.WorkflowNode {
    message : pif.Message
}

class pif.MessageCommunication : pif.Communication {
}

class pif.MessageSending : pif.MessageCommunication {
}

class pif.MessageReception : pif.MessageCommunication {
}

class pif.Gateway : pif.WorkflowNode {
}

class pif.Task : pif.WorkflowNode {
}

class pif.SplitGateway : pif.Gateway {
}

class pif.JoinGateway : pif.Gateway {
}

class pif.OrSplitGateway : pif.SplitGateway {
}

class pif.XOrSplitGateway : pif.SplitGateway {
}

class pif.AndSplitGateway : pif.SplitGateway {
}

class pif.AndJoinGateway : pif.JoinGateway {
}

class pif.XOrJoinGateway : pif.JoinGateway {
}

class pif.OrJoinGateway : pif.JoinGateway {
}

class pif.SequenceFlow  {
    source : pif.WorkflowNode oppositeOf outgoingFlows
    target : pif.WorkflowNode oppositeOf incomingFlows
}

class pif.ConditionalSequenceFlow : pif.SequenceFlow {
    @contained
    condition : pif.Condition
}

class pif.Condition  {
}
