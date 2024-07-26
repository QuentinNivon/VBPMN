package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;
import fr.inria.convecs.optimus.bpmn.types.process.SequenceFlow;
import fr.inria.convecs.optimus.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CLTStoBPMN
{
	private final AutGraph clts;
	private Graph bpmn;

	public CLTStoBPMN(final AutGraph clts)
	{
		this.clts = clts;
		BpmnProcessFactory.setObjectIDs(new ArrayList<>());
	}

	public Graph convert()
	{
		final Graph graph = new Graph(new Node(BpmnProcessFactory.generateStartEvent()));
		final Node firstFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
		graph.initialNode().addChild(firstFlow);
		firstFlow.addParent(graph.initialNode());
		this.buildGraph(this.clts.startNode(), firstFlow, new HashSet<>(), new HashMap<>());
		//Add the end event
		final Node lastFlow = this.getLastFlow(graph.initialNode(), new HashSet<>());
		if (lastFlow == null) throw new IllegalStateException();
		final Node endEvent = new Node(BpmnProcessFactory.generateEndEvent());
		lastFlow.addChild(endEvent);
		endEvent.addParent(lastFlow);

		return this.bpmn = graph;
	}

	public Graph getBpmnProcess()
	{
		return this.bpmn;
	}

	//Private methods

	private Node getLastFlow(final Node currentNode,
							 final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return null;
		}

		visitedNodes.add(currentNode);

		if (currentNode.childNodes().isEmpty())
		{
			return currentNode;
		}

		for (Node child : currentNode.childNodes())
		{
			final Node lastFlow = this.getLastFlow(child, visitedNodes);

			if (lastFlow != null)
			{
				return lastFlow;
			}
		}

		return null;
	}

	private void buildGraph(final AutNode currentCltsNode,
							final Node currentBpmnNode,
							final HashSet<AutNode> visitedNodes,
							final HashMap<AutNode, Pair<Node, Node>> correspondences)
	{
		if (visitedNodes.contains(currentCltsNode))
		{
			if (currentCltsNode.incomingEdges().size() > 1)
			{
				final Pair<Node, Node> correspondence = correspondences.computeIfAbsent(currentCltsNode, p -> new Pair<>());

				if (correspondence.getFirst() == null)
				{
					throw new IllegalStateException();
				}

				currentBpmnNode.addChild(correspondence.getFirst());
				correspondence.getFirst().addParent(currentBpmnNode);
			}

			return;
		}

		visitedNodes.add(currentCltsNode);

		final Node nodeToUse;

		if (currentCltsNode.incomingEdges().size() > 1)
		{
			final Pair<Node, Node> correspondence = correspondences.computeIfAbsent(currentCltsNode, p -> new Pair<>());

			if (correspondence.getFirst() == null)
			{
				//The merge node was not created yet
				final Node mergeGateway = new Node(BpmnProcessFactory.generateExclusiveGateway());
				((Gateway) mergeGateway.bpmnObject()).markAsMergeGateway();
				correspondence.setFirst(mergeGateway);
			}

			currentBpmnNode.addChild(correspondence.getFirst());
			correspondence.getFirst().addParent(currentBpmnNode);
			final Node outgoingFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
			correspondence.getFirst().addChild(outgoingFlow);
			outgoingFlow.addParent(correspondence.getFirst());

			nodeToUse = outgoingFlow;
		}
		else
		{
			nodeToUse = currentBpmnNode;
		}

		final Node secondNodeToUse;

		if (currentCltsNode.outgoingEdges().size() > 1)
		{
			final Pair<Node, Node> correspondence = correspondences.computeIfAbsent(currentCltsNode, p -> new Pair<>());

			if (correspondence.getSecond() == null)
			{
				final Node splitGateway = new Node(BpmnProcessFactory.generateExclusiveGateway());
				correspondence.setSecond(splitGateway);
			}

			nodeToUse.addChild(correspondence.getSecond());
			correspondence.getSecond().addParent(nodeToUse);
			secondNodeToUse = correspondence.getSecond();
		}
		else
		{
			secondNodeToUse = nodeToUse;
		}

		for (AutEdge outgoingEdge : currentCltsNode.outgoingEdges())
		{
			final Node task =  new Node(BpmnProcessFactory.generateTask("Task_" + BpmnProcessFactory.generateID(15), outgoingEdge.label()));

			if (secondNodeToUse.bpmnObject().type() == BpmnProcessType.SEQUENCE_FLOW)
			{
				secondNodeToUse.addChild(task);
				task.addParent(secondNodeToUse);
			}
			else
			{
				final Node incomingFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
				secondNodeToUse.addChild(incomingFlow);
				incomingFlow.addParent(secondNodeToUse);
				incomingFlow.addChild(task);
				task.addParent(incomingFlow);
			}

			final Node outgoingFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
			task.addChild(outgoingFlow);
			outgoingFlow.addParent(task);

			this.buildGraph(outgoingEdge.targetNode(), outgoingFlow, visitedNodes, correspondences);
		}
	}
}
