package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CLTSBuilder
{
	public static final boolean CONSIDER_FULL_PATH = true;
	private final AutGraph autGraph;
	private final HashSet<String> specLabels;

	public CLTSBuilder(final AutGraph autGraph,
					   final ArrayList<String> specLabels)
	{
		this.autGraph = autGraph.copy();
		this.specLabels = new HashSet<>(specLabels);
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
	 * @return
	 */
	public AutGraph buildCLTS()
	{
		final HashMap<AutNode, HashSet<String>> reachableTransitionLabels = new HashMap<>();
		this.computeReachableTransitionLabels(this.autGraph.startNode(), new HashSet<>(), reachableTransitionLabels);
		final HashSet<AutNode> nodesToCutBefore = new HashSet<>();
		this.computeNodesToRemove(this.autGraph.startNode(), new HashSet<>(), nodesToCutBefore, reachableTransitionLabels);

		System.out.println("Nodes to cut before: " + nodesToCutBefore);

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
		this.computeReducingCorrespondences(this.autGraph.startNode(), reducingCorrespondences, new HashSet<>());
		System.out.println("Reducing correspondences: " + reducingCorrespondences);

		for (AutNode key : reducingCorrespondences.keySet())
		{
			final HashSet<AutNode> correspondences = reducingCorrespondences.get(key);
			final HashSet<AutEdge> goodEdges = new HashSet<>();

			for (AutEdge outgoingEdge : key.outgoingEdges())
			{
				final String labelToConsider =
					CONSIDER_FULL_PATH ?
					outgoingEdge.label().replace(" !ACC", "").replace("\"","") :
					outgoingEdge.label()
				;

				if (this.specLabels.contains(labelToConsider))
				{
					goodEdges.add(outgoingEdge);
				}
			}

			if (goodEdges.size() == key.outgoingEdges().size())
			{
				/*
					This node only has good edges, so there is nothing to do
				 */
				continue;
			}

			key.outgoingEdges().clear();

			for (AutNode correspondence : correspondences)
			{
				for (Iterator<AutEdge> iterator = correspondence.outgoingEdges().iterator(); iterator.hasNext(); )
				{
					final AutEdge outgoingEdge = iterator.next();
					final AutEdge newEdge = new AutEdge(key, outgoingEdge.label(), outgoingEdge.targetNode());
					key.addOutgoingEdge(newEdge);
					outgoingEdge.targetNode().removeIncomingEdge(new AutEdge(correspondence, outgoingEdge.label(), outgoingEdge.targetNode()));
					iterator.remove();
					outgoingEdge.targetNode().addIncomingEdge(newEdge);
				}
			}

			for (AutEdge goodEdge : goodEdges)
			{
				key.addOutgoingEdge(goodEdge);
			}
		}

		//Add curvatures
		this.autGraph.setCurvatures();

		return this.autGraph;
	}

	//Private methods

	private void computeReducingCorrespondences(final AutNode currentNode,
												final HashMap<AutNode, HashSet<AutNode>> reducingCorrespondences,
												final HashSet<AutNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		final HashSet<AutNode> correspondences = reducingCorrespondences.computeIfAbsent(currentNode, h -> new HashSet<>());

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			final String labelToConsider =
				CONSIDER_FULL_PATH ?
				outgoingEdge.label().replace(" !ACC", "").replace("\"","") :
				outgoingEdge.label()
			;
			System.out.println("Label to consider: " + labelToConsider);

			if (!this.specLabels.contains(labelToConsider))
			{
				//Unknown label => find all the nodes to merge
				final HashSet<AutNode> nodesToMerge = new HashSet<>();
				this.findAllNodesToMerge(currentNode, new HashSet<>(), nodesToMerge);
				correspondences.addAll(nodesToMerge);
			}
			else
			{
				this.computeReducingCorrespondences(outgoingEdge.targetNode(), reducingCorrespondences, visitedNodes);
			}
		}

		for (AutNode nodeToMerge : correspondences)
		{
			this.computeReducingCorrespondences(nodeToMerge, reducingCorrespondences, visitedNodes);
		}
	}

	private void findAllNodesToMerge(final AutNode currentNode,
									 final HashSet<AutNode> visitedNodes,
									 final HashSet<AutNode> nodesToMerge)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		boolean validNode = false;

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			final String labelToConsider =
				CONSIDER_FULL_PATH ?
				outgoingEdge.label().replace(" !ACC", "").replace("\"","") :
				outgoingEdge.label()
			;

			if (this.specLabels.contains(labelToConsider))
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
				this.findAllNodesToMerge(outgoingEdge.targetNode(), visitedNodes, nodesToMerge);
			}
		}
	}

	private void computeReachableTransitionLabels(final AutNode currentNode,
												  final HashSet<AutNode> visitedNodes,
												  final HashMap<AutNode, HashSet<String>> reachableTransitionLabels)
	{
		final HashSet<String> currentSet = reachableTransitionLabels.computeIfAbsent(currentNode, h -> new HashSet<>());

		if (!visitedNodes.contains(currentNode))
		{
			visitedNodes.add(currentNode);

			for (AutEdge outgoingTransition : currentNode.outgoingEdges())
			{
				currentSet.add(outgoingTransition.label());
				this.computeReachableTransitionLabels(outgoingTransition.targetNode(), visitedNodes, reachableTransitionLabels);
			}
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
