package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CLTSBuilderV2
{
	public static final boolean CONSIDER_FULL_PATH = true;
	private final AutGraph autGraph;
	private final HashSet<String> specLabels;

	public CLTSBuilderV2(final AutGraph autGraph,
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
		//Truncate correct parts of the product
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

		//Remove non-specification edges
		this.removeUnnecessaryEdges(this.autGraph.startNode(), new HashSet<>());

		//Add curvatures
		this.autGraph.setCurvatures();

		return this.autGraph;
	}

	//Private methods

	private void removeUnnecessaryEdges(final AutNode currentNode,
										final HashSet<AutNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		boolean changed = true;

		while (changed)
		{
			changed = false;

			final HashSet<AutEdge> originalOutgoingEdges = new HashSet<>(currentNode.outgoingEdges());

			for (AutEdge outgoingEdge : originalOutgoingEdges)
			{
				final String labelToConsider =
					CONSIDER_FULL_PATH ?
					outgoingEdge.label().replace(" !ACC", "").replace("\"","") :
					outgoingEdge.label()
				;

				if (!this.specLabels.contains(labelToConsider))
				{
					//Useless edge
					changed = true;
					currentNode.removeOutgoingEdge(outgoingEdge);

					for (AutEdge autEdge : outgoingEdge.targetNode().outgoingEdges())
					{
						final AutEdge newEdge = new AutEdge(currentNode, autEdge.label(), autEdge.targetNode());
						currentNode.addOutgoingEdge(newEdge);
						autEdge.targetNode().addIncomingEdge(newEdge);
						outgoingEdge.targetNode().removeIncomingEdge(outgoingEdge);

						if (outgoingEdge.targetNode().incomingEdges().isEmpty())
						{
							//The target node is no longer accessible => remove it
							autEdge.targetNode().incomingEdges().remove(new AutEdge(outgoingEdge.targetNode(), autEdge.label(), autEdge.targetNode()));
						}
					}
				}
			}
		}

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			this.removeUnnecessaryEdges(outgoingEdge.targetNode(), visitedNodes);
		}
	}

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

			if (!this.specLabels.contains(labelToConsider))
			{
				System.out.println("Label \"" + labelToConsider + "\" does not belong to the specification!");
				//Unknown label => find all the nodes to merge
				final HashSet<AutNode> nodesToMerge = new HashSet<>();
				this.findAllNodesToMerge(currentNode, new HashSet<>(), nodesToMerge);
				correspondences.addAll(nodesToMerge);
			}
			else
			{
				System.out.println("Label \"" + labelToConsider + "\" belongs to the specification.");
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

		if (currentNode.outgoingEdges().isEmpty()) return;

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
