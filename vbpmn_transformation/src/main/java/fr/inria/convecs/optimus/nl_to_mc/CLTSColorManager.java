package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.*;
import fr.inria.convecs.optimus.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

public class CLTSColorManager
{
	private final AutGraph clts;

	public CLTSColorManager(final AutGraph clts)
	{
		this.clts = clts;
	}

	public void setProperColors()
	{
		final HashMap<AutNode, HashSet<AutEdge>> reachableTransitions = new HashMap<>();
		final Pair<HashSet<AutNode>, HashSet<AutEdge>> allNodes = this.clts.nodesAndEdges();
		this.computeReachableTransitions(this.clts.startNode(), new HashSet<>(), reachableTransitions);
		this.colorEdges(reachableTransitions, allNodes.getSecond());
		this.setFaultyStates(allNodes.getFirst());
	}

	//Private methods

	private void setFaultyStates(final HashSet<AutNode> nodes)
	{
		for (AutNode autNode : nodes)
		{
			boolean hasBlackOut = false;
			boolean hasGreenOut = false;
			boolean hasRedOut = false;

			for (AutEdge autEdge : autNode.outgoingEdges())
			{
				if (autEdge.getColor() == AutColor.BLACK)
				{
					hasBlackOut = true;
				}
				else if (autEdge.getColor() == AutColor.GREEN)
				{
					hasGreenOut = true;
				}
				else if (autEdge.getColor() == AutColor.RED)
				{
					hasRedOut = true;
				}
			}

			if (hasBlackOut
				&& hasGreenOut
				&& hasRedOut)
			{
				autNode.setStateType(StateType.GREEN_RED_BLACK);
			}
			else if (hasBlackOut
					&& hasGreenOut)
			{
				autNode.setStateType(StateType.GREEN_BLACK);
			}
			else if (hasBlackOut
					&& hasRedOut)
			{
				autNode.setStateType(StateType.RED_BLACK);
			}
			else if (hasGreenOut
					&& hasRedOut)
			{
				autNode.setStateType(StateType.GREEN_RED);
			}
		}
	}

	private void colorEdges(final HashMap<AutNode, HashSet<AutEdge>> reachableTransitions,
							final HashSet<AutEdge> allEdges)
	{
		for (AutEdge autEdge : allEdges)
		{
			if (autEdge.getColor() == AutColor.GREEN)
			{
				//The successor edges must be green too
				for (AutEdge successorEdge : reachableTransitions.get(autEdge.targetNode()))
				{
					successorEdge.setColor(AutColor.GREEN);
				}
			}
			else if (autEdge.getColor() == AutColor.RED)
			{
				//Check whether it has only red successors or not
				boolean onlyRed = true;

				for (AutEdge successorEdge : reachableTransitions.get(autEdge.targetNode()))
				{
					if (successorEdge.getColor() != AutColor.RED)
					{
						onlyRed = false;
						break;
					}
				}

				if (!onlyRed)
				{
					autEdge.setColor(AutColor.BLACK);
				}
			}
		}
	}

	private void computeReachableTransitions(final AutNode currentNode,
											 final HashSet<AutNode> visitedNodes,
											 final HashMap<AutNode, HashSet<AutEdge>> reachableTransitions)
	{
		final HashSet<AutEdge> currentSet = reachableTransitions.computeIfAbsent(currentNode, h -> new HashSet<>());

		if (!visitedNodes.contains(currentNode))
		{
			visitedNodes.add(currentNode);

			for (AutEdge outgoingTransition : currentNode.outgoingEdges())
			{
				currentSet.add(outgoingTransition);
				this.computeReachableTransitions(outgoingTransition.targetNode(), visitedNodes, reachableTransitions);
			}
		}

		for (AutEdge outgoingTransition : currentNode.outgoingEdges())
		{
			currentSet.addAll(reachableTransitions.get(outgoingTransition.targetNode()));
		}
	}
}
