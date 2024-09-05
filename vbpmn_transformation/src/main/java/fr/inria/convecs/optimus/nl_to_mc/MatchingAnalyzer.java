package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.*;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.GraphToList;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.SequenceFlow;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import fr.inria.convecs.optimus.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MatchingAnalyzer
{
	private final AutGraph clts;
	private final Graph originalProcess;

	public MatchingAnalyzer(final AutGraph clts,
							final Graph originalProcess) throws ExpectedException
	{
		this.clts = clts;
		this.originalProcess = originalProcess;
	}

	public boolean matches()
	{
		if (!checkTransitions()) return false;

		final HashSet<AutState> faultyStates = this.getFaultyStates();
		final HashMap<AutState, Pair<HashSet<String>, HashSet<String>>> inOutFaultyTransitions = this.getFaultyTransitions(faultyStates);
		final HashSet<Node> graphNodes = new HashSet<>();
		this.getGraphNodes(this.originalProcess.initialNode(), graphNodes);
		final HashMap<AutState, Node> matching = new HashMap<>();

		for (AutState autState : inOutFaultyTransitions.keySet())
		{
			final HashSet<String> incomingTransitionLabels = inOutFaultyTransitions.get(autState).getFirst();
			final HashSet<String> outgoingTransitionLabels = inOutFaultyTransitions.get(autState).getSecond();


		}
	}

	//Private methods

	private void getGraphNodes(final Node currentNode,
							   final HashSet<Node> graphNodes)
	{
		if (graphNodes.contains(currentNode))
		{
			return;
		}

		graphNodes.add(currentNode);
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
			faultyStates.add(currentState);
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
