package fr.inria.convecs.optimus.bpmn.graph;

import fr.inria.convecs.optimus.bpmn.BpmnColor;
import fr.inria.convecs.optimus.bpmn.constants.BpmnType;
import fr.inria.convecs.optimus.bpmn.constants.Constants;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;
import fr.inria.convecs.optimus.bpmn.types.process.events.Event;
import fr.inria.convecs.optimus.nl_to_mc.MyOwnLogger;
import fr.inria.convecs.optimus.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Graph
{
    protected final String id;
    protected final Node initialNode;
    protected boolean hasStrongFlows;

    public Graph(Node initialNode)
    {
        this.initialNode = initialNode;
        this.id = Utils.generateRandomIdentifier();
        this.hasStrongFlows = false;
    }

    public BpmnColor getColor()
    {
        return this.getBpmnColor(this.initialNode, new HashSet<>());
    }

    public Node getNodeFromObject(BpmnProcessObject object)
    {
        return this.getNodeFromObject(this.initialNode, object, new HashSet<>());
    }

    public boolean hasNode(Node n)
    {
        if (n == null)
        {
            return false;
        }

        return this.getNodeFromID(n.bpmnObject().id()) != null;
    }

    public boolean hasNode(BpmnProcessObject object)
    {
        if (object == null)
        {
            return false;
        }

        return this.getNodeFromID(object.id()) != null;
    }

    public boolean hasNodeOfId(final String id)
    {
        if (id == null)
        {
            return false;
        }

        return this.getNodeFromID(id) != null;
    }

    public Node getNodeFromID(final String id)
    {
        return this.getNodeFromIDRec(id, new HashSet<>(), this.initialNode);
    }

    public Node initialNode()
    {
        return this.initialNode;
    }

    /**
     * CAUTION: In this implementation, we assume that there exists a SINGLE end event
     * inside the BPMN process, that is considered as the end node of our graph
     *
     * @return the end node of the process
     */
    public Node lastNode()
    {
        return this.findLastNodeRec(this.initialNode, new HashSet<>());
    }

    public HashSet<Node> lastNodes()
    {
        final HashSet<Node> lastNodes = new HashSet<>();
        this.findLastNodes(this.initialNode, lastNodes, new HashSet<>());

        if (lastNodes.isEmpty())
        {
            System.out.println("No end event was found in the process!");
            MyOwnLogger.append("No end event was found in the process!");
        }

        for (Node node : lastNodes)
        {
            if (node.bpmnObject().type() != BpmnProcessType.END_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_MESSAGE_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_ESCALATION_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_ERROR_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_COMPENSATE_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_SIGNAL_EVENT
                && node.bpmnObject().type() != BpmnProcessType.END_TERMINATE_EVENT)
            {
                System.out.println(
                    "Warning: Node \"" + node.bpmnObject().id() + "\" (\"" + node.bpmnObject().name() + "\") has no" +
                    " child but and is not an end event."
                );
                MyOwnLogger.append(
                    "Warning: Node \"" + node.bpmnObject().id() + "\" (\"" + node.bpmnObject().name() + "\") has no" +
                    " child but and is not an end event."
                );
            }
        }

        return lastNodes;
    }

    public Graph weakCopy()
    {
        final Node initialNode = new Node(this.initialNode.bpmnObject());
        final Graph copiedGraph = new Graph(initialNode);
        //Correspondences between the node of the old graph, and the node of the new graph
        final HashMap<Node, Node> correspondences = new HashMap<>();
        correspondences.put(this.initialNode, initialNode);

        this.copyRec(this.initialNode, initialNode, new HashSet<>(), correspondences, false);

        return copiedGraph;
    }

    public Graph weakCopyFrom(final Node n)
    {
        final Node initialNode = new Node(n.bpmnObject());
        final Graph copiedGraph = new Graph(initialNode);
        //Correspondences between the node of the old graph, and the node of the new graph
        final HashMap<Node, Node> correspondences = new HashMap<>();
        correspondences.put(n, initialNode);

        this.copyRec(n, initialNode, new HashSet<>(), correspondences, false);

        return copiedGraph;
    }

    public Graph deepCopy()
    {
        final Node initialNode = new Node(this.initialNode.bpmnObject().copy());
        final Graph copiedGraph = new Graph(initialNode);

        final HashMap<Node, Node> correspondences = new HashMap<>();
        correspondences.put(this.initialNode, initialNode);

        this.copyRec(this.initialNode, initialNode, new HashSet<>(), correspondences, true);

        return copiedGraph;
    }

    public Graph deepCopyFrom(final Node n)
    {
        final Node initialNode = new Node(n.bpmnObject().copy());
        final Graph copiedGraph = new Graph(initialNode);
        //Correspondences between the node of the old graph, and the node of the new graph
        final HashMap<Node, Node> correspondences = new HashMap<>();
        correspondences.put(n, initialNode);

        this.copyRec(n, initialNode, new HashSet<>(), correspondences, true);

        return copiedGraph;
    }

    public Graph cutAt(final Node n)
    {
        this.cutAtRec(this.initialNode, n, new HashSet<>());
        return this;
    }

    public void markAsContainingStrongFlows()
    {
        this.hasStrongFlows = true;
    }

    public boolean hasStrongFlows()
    {
        return this.hasStrongFlows;
    }

    public void clearParallelGatewaysTime()
    {
        this.clearParallelGatewayTime(this.initialNode, new HashSet<>());
    }

    //Private methods

    private BpmnColor getBpmnColor(final Node currentNode,
                                   final HashSet<Node> visitedNodes)
    {
        if (visitedNodes.contains(currentNode))
        {
            return null;
        }

        visitedNodes.add(currentNode);

        if (currentNode.bpmnObject().type() == BpmnProcessType.TASK)
        {
            return currentNode.bpmnObject().getBpmnColor() == null ? BpmnColor.BLACK : currentNode.bpmnObject().getBpmnColor();
        }

        for (Node child : currentNode.childNodes())
        {
            final BpmnColor childColor = this.getBpmnColor(child, visitedNodes);

            if (childColor != null)
            {
                return childColor;
            }
        }

        return null;
    }

    private void clearParallelGatewayTime(final Node currentNode,
                                          final HashSet<Node> visitedNodes)
    {
        if (visitedNodes.contains(currentNode))
        {
            return;
        }

        visitedNodes.add(currentNode);

        if (currentNode.bpmnObject().type() == BpmnProcessType.PARALLEL_GATEWAY
            && ((Gateway) currentNode.bpmnObject()).isMergeGateway())
        {
            ((Gateway) currentNode.bpmnObject()).parallelPathsExecutionTimes().clear();
        }

        for (Node child : currentNode.childNodes())
        {
            this.clearParallelGatewayTime(child, visitedNodes);
        }
    }

    private void cutAtRec(final Node currentNode,
                          final Node nodeToReach,
                          final HashSet<Node> visitedNodes)
    {
        if (visitedNodes.contains(currentNode))
        {
            return;
        }

        visitedNodes.add(currentNode);

        if (currentNode.equals(nodeToReach))
        {
            //We remove the current node from the list of parents of the child node
            for (Node child : currentNode.childNodes())
            {
                child.parentNodes().remove(currentNode);
            }
            //We remove all the children of the current node
            currentNode.childNodes().clear();
            return;
        }

        for (Node child : currentNode.childNodes())
        {
            this.cutAtRec(child, nodeToReach, visitedNodes);
        }
    }

    private void copyRec(final Node currentOldNode,
                         final Node currentNewNode,
                         final Set<Node> visitedNodes,
                         final HashMap<Node, Node> correspondences,
                         final boolean deepCopy)
    {
        if (visitedNodes.contains(currentOldNode))
        {
            return;
        }

        visitedNodes.add(currentOldNode);

        for (Node oldChild : currentOldNode.childNodes())
        {
            final Node newChild = correspondences.computeIfAbsent(oldChild, n -> deepCopy ? new Node(oldChild.bpmnObject().copy()) : new Node(oldChild.bpmnObject()));
            currentNewNode.addChild(newChild);
            newChild.addParent(currentNewNode);

            this.copyRec(oldChild, newChild, visitedNodes, correspondences, deepCopy);
        }
    }

    private Node getNodeFromObject(Node node,
                                   BpmnProcessObject object,
                                   Set<Node> nodesAlreadyVisited)
    {
        if (node.bpmnObject().equals(object))
        {
            return node;
        }

        if (nodesAlreadyVisited.contains(node))
        {
            return null;
        }

        nodesAlreadyVisited.add(node);

        if (node.hasChilds())
        {
            for (Node childNode : node.childNodes())
            {
                Node foundNode = getNodeFromObject(childNode, object, nodesAlreadyVisited);

                if (foundNode != null)
                {
                    return foundNode;
                }
            }
        }

        return null;
    }

    private Node getNodeFromIDRec(final String id,
                                  final Set<Node> visitedNodes,
                                  final Node currentNode)
    {
        if (currentNode.bpmnObject().id().equals(id))
        {
            return currentNode;
        }

        if (visitedNodes.contains(currentNode))
        {
            return null;
        }

        visitedNodes.add(currentNode);

        for (Node child : currentNode.childNodes())
        {
            Node existingNode = getNodeFromIDRec(id, visitedNodes, child);

            if (existingNode != null)
            {
                return existingNode;
            }
        }

        return null;
    }

    private void findLastNodes(final Node currentNode,
                               final HashSet<Node> lastNodes,
                               final HashSet<Node> visitedNodes)
    {
        if (visitedNodes.contains(currentNode))
        {
            return;
        }

        visitedNodes.add(currentNode);

        if (currentNode.childNodes().isEmpty())
        {
            //A node with no child is considered as an end event
            lastNodes.add(currentNode);
        }

        for (Node child : currentNode.childNodes())
        {
            this.findLastNodes(child, lastNodes, visitedNodes);
        }
    }

    private Node findLastNodeRec(final Node currentNode,
                                 final HashSet<Node> visitedNodes)
    {
        if (visitedNodes.contains(currentNode))
        {
            return null;
        }

        visitedNodes.add(currentNode);

        if (currentNode.childNodes().isEmpty())
        {
            //An event with no child is considered as the end event
            return currentNode;
        }

        for (Node child : currentNode.childNodes())
        {
            final Node endEvent = findLastNodeRec(child, visitedNodes);

            if (endEvent != null)
            {
                return endEvent;
            }
        }

        return null;
    }

    //Overrides

    @Override
    public String toString()
    {
        return this.initialNode.stringify(0, new ArrayList<>());
        //return this.lastNode().stringifyRevert(0, new ArrayList<>());
    }

    public String stringify()
    {
        final StringBuilder builder = new StringBuilder();

        for (Node lastNode : this.lastNodes())
        {
            builder.append(lastNode.stringifyRevert(0, new ArrayList<>()));
        }

        return builder.toString();
    }

    //TODO Check if the fact that we consider two graph equals by checking
    //TODO only their first node can be a problem or not.
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Graph))
        {
            return false;
        }

        return ((Graph) o).initialNode.bpmnObject().id().equals(this.initialNode.bpmnObject().id());
    }

    @Override
    public int hashCode()
    {
        int hash = 7;

        for (int i = 0; i < this.initialNode.bpmnObject().id().length(); i++)
        {
            hash = hash * 31 + this.initialNode.bpmnObject().id().charAt(i);
        }

        return hash;
    }
}
