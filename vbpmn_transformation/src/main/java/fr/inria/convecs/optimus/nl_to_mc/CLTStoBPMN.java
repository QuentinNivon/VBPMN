package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutColor;
import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutState;
import fr.inria.convecs.optimus.bpmn.BpmnColor;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;
import fr.inria.convecs.optimus.bpmn.types.process.Task;
import fr.inria.convecs.optimus.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CLTStoBPMN
{
	private final AutGraph clts;
	private final HashSet<Task> originalTasks;
	private final Graph originalBpmn;
	private Graph bpmn;

	public CLTStoBPMN(final AutGraph clts,
					  final HashSet<Task> originalTasks,
					  final Graph originalBpmn)
	{
		this.clts = clts;
		this.originalTasks = originalTasks;
		this.originalBpmn = originalBpmn;
		BpmnProcessFactory.setObjectIDs(new ArrayList<>());
	}

	public Graph convert()
	{
		final Graph graph = new Graph(new Node(BpmnProcessFactory.generateStartEvent()));
		final Node firstFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
		graph.initialNode().addChild(firstFlow);
		firstFlow.addParent(graph.initialNode());
		this.buildGraph(this.clts.startNode(), firstFlow, new HashSet<>(), new HashMap<>());
		//Add the end event(s)
		final HashSet<Node> lastFlows = new HashSet<>();
		this.getLastFlows(graph.initialNode(), lastFlows, new HashSet<>());

		//MyOwnLogger.append("CURRENT PROCESS:\n\n" + graph.toString());

		if (lastFlows.isEmpty())
		{
			/*
				Happens when the exit node of the loop (the exclusive split gateway)
				is directly connected to the end event.
				In this case, we need to find the last loop node of the original
				process (i.e., the one directly connected to the exit node of the loop)
				and connect it to an exclusive split gateway being the exit node of the
				new loop.
			 */
			this.manageLastFlows();
		}
		else
		{
			//TODO Revoir car on peut avoir des last flows et une boucle terminale mal gérée
			for (Node lastFlow : lastFlows)
			{
				final Node endEvent = new Node(BpmnProcessFactory.generateEndEvent());
				lastFlow.addChild(endEvent);
				endEvent.addParent(lastFlow);
			}
		}

		return this.bpmn = graph;
	}

	public Graph getBpmnProcess()
	{
		return this.bpmn;
	}

	//Private methods

	//TODO A REVOIR
	private void manageLastFlows()
	{
		/*final HashSet<Node> endEvents = this.originalBpmn.lastNodes();
		final Node endEvent = endEvents.iterator().next();
		final HashSet<Node> closestAncestorTasks = new HashSet<>();
		this.findClosestAncestorTasks(endEvent, closestAncestorTasks, new HashSet<>());

		if (closestAncestorTasks.isEmpty()) throw new IllegalStateException();*/


	}

	private void findClosestAncestorTasks(final Node currentNode,
										  final HashSet<Node> closestAncestorTasks,
										  final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject() instanceof Task)
		{
			closestAncestorTasks.add(currentNode);
		}
		else
		{
			for (Node parent : currentNode.parentNodes())
			{
				this.findClosestAncestorTasks(parent, closestAncestorTasks, visitedNodes);
			}
		}
	}

	private void getLastFlows(final Node currentNode,
							  final HashSet<Node> lastFlows,
							  final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.childNodes().isEmpty())
		{
			lastFlows.add(currentNode);
		}

		for (Node child : currentNode.childNodes())
		{
			this.getLastFlows(child, lastFlows, visitedNodes);
		}
	}

	private void buildGraph(final AutState currentCltsNode,
							final Node currentBpmnNode,
							final HashSet<AutState> visitedNodes,
							final HashMap<AutState, Pair<Node, Node>> correspondences)
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
			final Node task =  new Node(BpmnProcessFactory.generateTask("Task_" + BpmnProcessFactory.generateID(15), this.getRealName(outgoingEdge.label())));

			if (outgoingEdge.getColor() == AutColor.GREEN)
			{
				task.bpmnObject().setBpmnColor(BpmnColor.GREEN);
			}
			else if (outgoingEdge.getColor() == AutColor.RED)
			{
				task.bpmnObject().setBpmnColor(BpmnColor.RED);
			}

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

	private String getRealName(final String upperCaseName)
	{
		for (Task t : this.originalTasks)
		{
			if (t.id().toUpperCase().equals(upperCaseName))
			{
				return t.name();
			}
		}

		throw new IllegalStateException("No task of name \"" + upperCaseName + "\" found in the process!");
	}
}
