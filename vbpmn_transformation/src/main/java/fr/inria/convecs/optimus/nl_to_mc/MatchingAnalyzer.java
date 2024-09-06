package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.*;
import fr.inria.convecs.optimus.bpmn.BpmnColor;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.Task;
import fr.inria.convecs.optimus.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

public class MatchingAnalyzer
{
	private final AutGraph clts;
	private final Graph originalProcess;
	private final HashMap<AutState, Node> matching;

	public MatchingAnalyzer(final AutGraph clts,
							final Graph originalProcess)
	{
		this.clts = clts;
		this.originalProcess = originalProcess;
		this.matching = new HashMap<>();
	}

	public boolean matches()
	{
		//Check whether CLTS is entirely red or not
		boolean cltsIsEntirelyRed = true;

		for (AutEdge autEdge : this.clts.startNode().outgoingEdges())
		{
			if (autEdge.getColor() != AutColor.RED)
			{
				cltsIsEntirelyRed = false;
				break;
			}
		}

		if (cltsIsEntirelyRed)
		{
			this.colorTaskAndSuccessors(this.originalProcess.initialNode(), AutColor.RED, new HashSet<>());
			return true;
		}

		//Check transitions color
		if (!checkTransitions()) return false;

		//Compute successor tasks reachable from each graph node
		final HashMap<Node, HashSet<String>> oldSuccessorReachableTasks = new HashMap<>();
		final HashMap<Node, HashSet<String>> newSuccessorReachableTasks = new HashMap<>();
		this.computeSuccessorReachableTasksLabels(this.originalProcess.initialNode(), new HashSet<>(), newSuccessorReachableTasks);

		while (!oldSuccessorReachableTasks.equals(newSuccessorReachableTasks))

		{
			oldSuccessorReachableTasks.clear();
			oldSuccessorReachableTasks.putAll(newSuccessorReachableTasks);
			this.computeSuccessorReachableTasksLabels(this.originalProcess.initialNode(), new HashSet<>(), newSuccessorReachableTasks);
		}

		//Compute ancestor tasks reachable from each graph node
		final HashMap<Node, HashSet<String>> oldAncestorReachableTasks = new HashMap<>();
		final HashMap<Node, HashSet<String>> newAncestorReachableTasks = new HashMap<>();

		for (Node lastNode : this.originalProcess.lastNodes())
		{
			this.computeAncestorReachableTasksLabels(lastNode, new HashSet<>(), newAncestorReachableTasks);
		}

		while (!oldAncestorReachableTasks.equals(newAncestorReachableTasks))

		{
			oldAncestorReachableTasks.clear();
			oldAncestorReachableTasks.putAll(newAncestorReachableTasks);
			for (Node lastNode : this.originalProcess.lastNodes())
			{
				this.computeAncestorReachableTasksLabels(lastNode, new HashSet<>(), newAncestorReachableTasks);
			}
		}

		//Try to match each faulty state to a BPMN node
		final HashSet<AutState> faultyStates = this.getFaultyStates();
		MyOwnLogger.append(faultyStates.size() + " faulty states found in the CLTS.");
		final HashMap<AutState, Pair<HashSet<String>, HashSet<String>>> inOutFaultyTransitions = this.getFaultyTransitions(faultyStates);
		final HashSet<Node> graphNodes = new HashSet<>();
		this.getGraphNodes(this.originalProcess.initialNode(), graphNodes);

		for (AutState autState : inOutFaultyTransitions.keySet())
		{
			final HashSet<String> incomingTransitionLabels = inOutFaultyTransitions.get(autState).getFirst();
			final HashSet<String> outgoingTransitionLabels = inOutFaultyTransitions.get(autState).getSecond();
			MyOwnLogger.append("Faulty state " + autState.label() + " has incoming transitions " +
					incomingTransitionLabels + " and outgoing transitions " + outgoingTransitionLabels);

			for (Node graphNode : graphNodes)
			{
				final HashSet<String> incomingTaskLabels = newAncestorReachableTasks.get(graphNode);
				final HashSet<String> outgoingTaskLabels = newSuccessorReachableTasks.get(graphNode);
				MyOwnLogger.append("BPMN node " + graphNode.bpmnObject().id() + " has ancestor tasks " +
						incomingTaskLabels + " and successors tasks " + outgoingTaskLabels);

				if (incomingTaskLabels.equals(incomingTransitionLabels)
					&& outgoingTaskLabels.equals(outgoingTransitionLabels))
				{
					MyOwnLogger.append("AUT state " + autState.label() + " was matched with node " + graphNode.bpmnObject().id());

					//We found a matching
					if (this.matching.containsKey(autState))
					{
						//We already found a matching => what to do?
						MyOwnLogger.append(
							"Warning: AUT state " + autState.label() + " matches node " + graphNode.bpmnObject().id() +
							" but was already matched with node " + graphNode.bpmnObject().id() + "."
						);
					}

					this.matching.put(autState, graphNode);
					break;
				}
			}

			if (!this.matching.containsKey(autState))
			{
				//No matching was found for the current faulty state
				MyOwnLogger.append("No matching was found for faulty state " + autState.label());
				this.matching.clear();
				return false;
			}
		}

		return true;
	}

	public void colorOriginalProcess()
	{
		for (AutState autState : this.matching.keySet())
		{
			final Node bpmnNode = this.matching.get(autState);
			MyOwnLogger.append("CLTS state " + autState.label() + " was matched with BPMN node \"" + bpmnNode.bpmnObject().id() + "\".");
			final HashSet<Node> firstReachableTasks = new HashSet<>();
			this.getFirstReachableTasks(bpmnNode, firstReachableTasks, new HashSet<>());
			MyOwnLogger.append("First reachable tasks of node \"" + bpmnNode.bpmnObject().id() + "\" are " + firstReachableTasks);

			for (AutEdge outgoingEdge : autState.outgoingEdges())
			{
				if (outgoingEdge.getColor() != AutColor.BLACK)
				{
					for (Node firstTaskToColor : firstReachableTasks)
					{
						if (firstTaskToColor.bpmnObject().name().toUpperCase().equals(outgoingEdge.label()))
						{
							MyOwnLogger.append("Task " + firstTaskToColor.bpmnObject().name() + " and its successors" +
									" will be colored in " + (outgoingEdge.getColor() == AutColor.GREEN ? "green" : "red"));
							this.colorTaskAndSuccessors(firstTaskToColor, outgoingEdge.getColor(), new HashSet<>());
						}
					}
				}
			}
		}
	}

	//Private methods

	private void colorTaskAndSuccessors(final Node currentNode,
										final AutColor color,
										final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject() instanceof Task)
		{
			currentNode.bpmnObject().setBpmnColor(color == AutColor.GREEN ? BpmnColor.GREEN : BpmnColor.RED);
		}

		for (Node child : currentNode.childNodes())
		{
			this.colorTaskAndSuccessors(child, color, visitedNodes);
		}
	}

	private void getFirstReachableTasks(final Node currentNode,
										final HashSet<Node> firstReachableTasks,
										final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject() instanceof Task)
		{
			firstReachableTasks.add(currentNode);
		}
		else
		{
			for (Node child : currentNode.childNodes())
			{
				this.getFirstReachableTasks(child, firstReachableTasks, visitedNodes);
			}
		}
	}

	private void computeSuccessorReachableTasksLabels(final Node currentNode,
													  final HashSet<Node> visitedNodes,
													  final HashMap<Node, HashSet<String>> reachableTransitionLabels)
	{
		final HashSet<String> currentSet = reachableTransitionLabels.computeIfAbsent(currentNode, h -> new HashSet<>());

		if (!visitedNodes.contains(currentNode))
		{
			visitedNodes.add(currentNode);

			if (currentNode.bpmnObject() instanceof Task)
			{
				currentSet.add(currentNode.bpmnObject().name().toUpperCase());
			}

			for (Node childNode : currentNode.childNodes())
			{
				if (childNode.bpmnObject() instanceof Task)
				{
					currentSet.add(childNode.bpmnObject().name().toUpperCase());
				}

				this.computeSuccessorReachableTasksLabels(childNode, visitedNodes, reachableTransitionLabels);
			}
		}

		for (Node childNode : currentNode.childNodes())
		{
			currentSet.addAll(reachableTransitionLabels.get(childNode));
		}
	}

	private void computeAncestorReachableTasksLabels(final Node currentNode,
													 final HashSet<Node> visitedNodes,
													 final HashMap<Node, HashSet<String>> reachableTransitionLabels)
	{
		final HashSet<String> currentSet = reachableTransitionLabels.computeIfAbsent(currentNode, h -> new HashSet<>());

		if (!visitedNodes.contains(currentNode))
		{
			visitedNodes.add(currentNode);

			if (currentNode.bpmnObject() instanceof Task)
			{
				currentSet.add(currentNode.bpmnObject().name().toUpperCase());
			}

			for (Node parentNode : currentNode.parentNodes())
			{
				if (parentNode.bpmnObject() instanceof Task)
				{
					currentSet.add(parentNode.bpmnObject().name().toUpperCase());
				}

				this.computeAncestorReachableTasksLabels(parentNode, visitedNodes, reachableTransitionLabels);
			}
		}

		for (Node parentNode : currentNode.parentNodes())
		{
			currentSet.addAll(reachableTransitionLabels.get(parentNode));
		}
	}

	private void getGraphNodes(final Node currentNode,
							   final HashSet<Node> graphNodes)
	{
		if (graphNodes.contains(currentNode))
		{
			return;
		}

		graphNodes.add(currentNode);

		for (Node childNode : currentNode.childNodes())
		{
			this.getGraphNodes(childNode, graphNodes);
		}
	}

	private boolean checkTransitions()
	{
		final HashSet<String> blackLabels = new HashSet<>();
		final HashSet<String> redLabels = new HashSet<>();
		final HashSet<String> greenLabels = new HashSet<>();
		this.computePerColorLabels(this.clts.startNode(), new HashSet<>(), blackLabels, redLabels, greenLabels);
		final int nbBlackLabels = blackLabels.size();
		final int nbRedLabels = redLabels.size();

		blackLabels.removeAll(redLabels);
		if (blackLabels.size() != nbBlackLabels) return false;

		blackLabels.removeAll(greenLabels);
		if (blackLabels.size() != nbBlackLabels) return false;

		redLabels.removeAll(greenLabels);
		return redLabels.size() == nbRedLabels;
	}

	private HashSet<AutState> getFaultyStates()
	{
		final HashSet<AutState> faultyStates = new HashSet<>();
		this.getFaultyStates(this.clts.startNode(), new HashSet<>(), faultyStates);
		return faultyStates;
	}

	private HashMap<AutState, Pair<HashSet<String>, HashSet<String>>> getFaultyTransitions(final HashSet<AutState> faultyStates)
	{
		final HashMap<AutState, Pair<HashSet<String>, HashSet<String>>> faultyTransitions = new HashMap<>();

		for (AutState autState : faultyStates)
		{
			final HashSet<String> incomingTransitions = new HashSet<>();
			final HashSet<String> outgoingTransitions = new HashSet<>();
			this.retrieveAllIncomingTasks(autState, new HashSet<>(), incomingTransitions);
			this.retrieveAllFollowingTasks(autState, new HashSet<>(), outgoingTransitions);
			faultyTransitions.put(autState, new Pair<>(incomingTransitions, outgoingTransitions));
		}

		return faultyTransitions;
	}

	private void retrieveAllIncomingTasks(final AutState currentState,
										  final HashSet<AutState> visitedNodes,
										  final HashSet<String> incomingTasks)
	{
		if (visitedNodes.contains(currentState))
		{
			return;
		}

		visitedNodes.add(currentState);

		for (AutEdge incomingEdge : currentState.incomingEdges())
		{
			incomingTasks.add(incomingEdge.label());
			this.retrieveAllIncomingTasks(incomingEdge.sourceNode(), visitedNodes, incomingTasks);
		}
	}

	private void retrieveAllFollowingTasks(final AutState currentState,
										   final HashSet<AutState> visitedNodes,
									       final HashSet<String> outgoingTasks)
	{
		if (visitedNodes.contains(currentState))
		{
			return;
		}

		visitedNodes.add(currentState);

		for (AutEdge outgoingEdge : currentState.outgoingEdges())
		{
			outgoingTasks.add(outgoingEdge.label());
			this.retrieveAllFollowingTasks(outgoingEdge.targetNode(), visitedNodes, outgoingTasks);
		}
	}

	private void getFaultyStates(final AutState currentState,
								 final HashSet<AutState> visitedStates,
								 final HashSet<AutState> faultyStates)
	{
		if (visitedStates.contains(currentState))
		{
			return;
		}

		visitedStates.add(currentState);

		if (currentState.getStateType() != null)
		{
			MyOwnLogger.append("State " + currentState.label() + " is a faulty state");
			faultyStates.add(currentState);
		}

		for (AutEdge outgoingEdge : currentState.outgoingEdges())
		{
			this.getFaultyStates(outgoingEdge.targetNode(), visitedStates, faultyStates);
		}
	}

	private void computePerColorLabels(final AutState currentState,
									   final HashSet<AutState> visitedStates,
									   final HashSet<String> blackLabels,
									   final HashSet<String> redLabels,
									   final HashSet<String> greenLabels)
	{
		if (visitedStates.contains(currentState))
		{
			return;
		}

		visitedStates.add(currentState);

		for (AutEdge autEdge : currentState.outgoingEdges())
		{
			if (autEdge.getColor() == AutColor.BLACK)
			{
				blackLabels.add(autEdge.label());
			}
			else if (autEdge.getColor() == AutColor.GREEN)
			{
				greenLabels.add(autEdge.label());
			}
			else if (autEdge.getColor() == AutColor.RED)
			{
				redLabels.add(autEdge.label());
			}

			this.computePerColorLabels(autEdge.targetNode(), visitedStates, blackLabels, redLabels, greenLabels);
		}
	}
}
