package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;
import fr.inria.convecs.optimus.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CLTSBuilder
{
	public CLTSBuilder()
	{

	}

	/**
	 * This method takes as input the synchronous product between
	 * the negation of the LTL property and the LTS corresponding
	 * to the specification.
	 * In this product, the correct branches are the ones not leading
	 * to a transition whose label contains "!ACC".
	 * Thus, the CLTS is the LTS composed of all the paths eventually
	 * leading to a transition whose label contains "!ACC".
	 * All the transitions that are not labeled with elements of the
	 * original specification are also removed.
	 *
	 * @param autGraph
	 * @return
	 */
	public AutGraph buildCLTS(final AutGraph autGraph,
							  final ArrayList<String> specLabels)
	{
		final AutGraph copy = autGraph.copy();
		final HashMap<AutNode, HashSet<String>> reachableTransitionLabels = new HashMap<>();
		final HashSet<AutNode> nodesToCutBefore = new HashSet<>();
		this.computeNodesToRemove(copy.startNode(), new HashSet<>(), nodesToCutBefore, reachableTransitionLabels);

		for (AutNode node : nodesToCutBefore)
		{
			for (AutEdge autEdge : node.incomingEdges())
			{
				autEdge.sourceNode().outgoingEdges().remove(autEdge);
			}
		}

		/*
			We built the CLTS. We now have to remove all the transitions that are not transitions of the original
			specification.
		 */
		final HashMap<AutNode, HashSet<AutNode>> reducingCorrespondences = new HashMap<>();
		this.computeReducingCorrespondences(copy.startNode(), reducingCorrespondences, new HashSet<>(specLabels));

		for (AutNode key : reducingCorrespondences.keySet())
		{
			final HashSet<AutNode> correspondences = reducingCorrespondences.get(key);



			final HashSet<AutEdge> goodEdges = new HashSet<>();

			for (AutEdge outgoingEdge : key.outgoingEdges())
			{
				if (specLabels.contains(outgoingEdge.label()))
				{
					goodEdges.add(outgoingEdge);
				}
			}

			//key.outgoingEdges().clear();
			final HashSet<AutNode> parentNodes = new HashSet<>();

			for (AutEdge incomingEdge : key.incomingEdges())
			{
				parentNodes.add(incomingEdge.sourceNode());
			}

			for (AutNode parent : parentNodes)
			{
				AutEdge edgeToConsider = null;

				for (AutEdge autEdge : parent.outgoingEdges())
				{
					if (autEdge.targetNode().equals(key))
					{
						edgeToConsider = autEdge;
						break;
					}
				}

				if (edgeToConsider == null) throw new IllegalStateException();

				parent.outgoingEdges().remove(edgeToConsider);

				for (AutNode correspondence : correspondences)
				{

					final AutEdge newEdge = new AutEdge(parent, edgeToConsider.label(), correspondence);
					parent.addOutgoingEdge(newEdge);
					correspondence.addIncomingEdge(newEdge);
				}

				for (AutEdge goodEdge : goodEdges)
				{
					final AutEdge newEdge = new AutEdge(parent, goodEdge.label(), goodEdge.targetNode());
					parent.addOutgoingEdge(newEdge);
					goodEdge.targetNode().incomingEdges().remove(goodEdge);
					goodEdge.targetNode().addIncomingEdge(newEdge);
				}
			}
		}
	}

	//Private methods

	private void computeReducingCorrespondences(final AutNode currentNode,
												final HashMap<AutNode, HashSet<AutNode>> reducingCorrespondences,
												final HashSet<String> specLabels)
	{
		final HashSet<AutNode> correspondences = reducingCorrespondences.computeIfAbsent(currentNode, h -> new HashSet<>());

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			if (!specLabels.contains(outgoingEdge.label()))
			{
				//Unknown label => find all the nodes to merge
				final HashSet<AutNode> nodesToMerge = new HashSet<>();
				this.findAllNodesToMerge(currentNode, new HashSet<>(), nodesToMerge, specLabels);
				correspondences.addAll(nodesToMerge);
			}
		}

		for (AutNode nodeToMerge : correspondences)
		{
			this.computeReducingCorrespondences(nodeToMerge, reducingCorrespondences, specLabels);
		}
	}

	private void findAllNodesToMerge(final AutNode currentNode,
									 final HashSet<AutNode> visitedNodes,
									 final HashSet<AutNode> nodesToMerge,
									 final HashSet<String> specLabels)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		boolean validNode = false;

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			if (specLabels.contains(outgoingEdge.label()))
			{
				validNode = true;
				break;
			}
		}

		if (validNode)
		{
			nodesToMerge.add(currentNode);
		}
		else
		{
			for (AutEdge outgoingEdge : currentNode.outgoingEdges())
			{
				this.findAllNodesToMerge(outgoingEdge.targetNode(), visitedNodes, nodesToMerge, specLabels);
			}
		}
	}

	private void computeReachableTransitionLabels(final AutNode currentNode,
												  final HashMap<AutNode, HashSet<String>> reachableTransitionLabels)
	{
		final HashSet<String> currentSet = reachableTransitionLabels.computeIfAbsent(currentNode, h -> new HashSet<>());

		for (AutEdge outgoingTransition : currentNode.outgoingEdges())
		{
			currentSet.add(outgoingTransition.label());
			this.computeReachableTransitionLabels(outgoingTransition.targetNode(), reachableTransitionLabels);
		}

		for (AutEdge outgoingTransition : currentNode.outgoingEdges())
		{
			currentSet.addAll(reachableTransitionLabels.get(outgoingTransition.targetNode()));
		}
	}

	private void computeNodesToRemove(final AutNode currentNode,
									  final HashSet<AutNode> visitedNodes,
									  final HashSet<AutNode> nodesToCutAfter,
									  final HashMap<AutNode, HashSet<String>> reachableTransitionLabels)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		final HashSet<String> currentReachableTransitionLabels = reachableTransitionLabels.get(currentNode);

		boolean canBeRemoved = true;

		for (String label : currentReachableTransitionLabels)
		{
			if (label.contains("!ACC"))
			{
				canBeRemoved = false;
				break;
			}
		}

		if (canBeRemoved)
		{
			nodesToCutAfter.add(currentNode);
		}
		else
		{
			for (AutEdge outgoingTransition : currentNode.outgoingEdges())
			{
				this.computeNodesToRemove(outgoingTransition.targetNode(), visitedNodes, nodesToCutAfter, reachableTransitionLabels);
			}
		}
	}
}
