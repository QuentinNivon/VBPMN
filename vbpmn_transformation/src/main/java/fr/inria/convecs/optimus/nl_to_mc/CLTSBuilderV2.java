package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
		final HashMap<AutState, HashSet<String>> reachableTransitionLabels = new HashMap<>();
		//Compute reachable transitions twice to properly manage loops
		this.computeReachableTransitionLabels(this.autGraph.startNode(), new HashSet<>(), reachableTransitionLabels);
		this.computeReachableTransitionLabels(this.autGraph.startNode(), new HashSet<>(), reachableTransitionLabels);
		MyOwnLogger.append("Reachable transitions:\n\n" + reachableTransitionLabels.toString());

		for (AutState autState : this.autGraph.nodesAndEdges().getFirst())
		{
			if (reachableTransitionLabels.get(autState) == null)
			{
				throw new IllegalStateException("State " + autState.label() + " has no reachable transitions!");
			}
		}

		final HashSet<AutState> nodesToCutBefore = new HashSet<>();
		this.computeNodesToRemove(this.autGraph.startNode(), new HashSet<>(), nodesToCutBefore, reachableTransitionLabels);

		System.out.println("Nodes to cut before: " + nodesToCutBefore);
		MyOwnLogger.append("Nodes to cut before: " + nodesToCutBefore);

		for (AutState node : nodesToCutBefore)
		{
			for (AutEdge autEdge : node.incomingEdges())
			{
				autEdge.sourceNode().outgoingEdges().remove(autEdge);
			}
		}

		//Remove non-specification edges
		this.removeUnnecessaryEdges(this.autGraph.startNode(), new HashSet<>());

		return this.autGraph;
	}

	//Private methods

	private void removeUnnecessaryEdges(final AutState currentNode,
										final HashSet<AutState> visitedNodes)
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

	private void computeReducingCorrespondences(final AutState currentNode,
												final HashMap<AutState, HashSet<AutState>> reducingCorrespondences,
												final HashSet<AutState> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		final HashSet<AutState> correspondences = reducingCorrespondences.computeIfAbsent(currentNode, h -> new HashSet<>());

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
				final HashSet<AutState> nodesToMerge = new HashSet<>();
				this.findAllNodesToMerge(currentNode, new HashSet<>(), nodesToMerge);
				correspondences.addAll(nodesToMerge);
			}
			else
			{
				System.out.println("Label \"" + labelToConsider + "\" belongs to the specification.");
				this.computeReducingCorrespondences(outgoingEdge.targetNode(), reducingCorrespondences, visitedNodes);
			}
		}

		for (AutState nodeToMerge : correspondences)
		{
			this.computeReducingCorrespondences(nodeToMerge, reducingCorrespondences, visitedNodes);
		}
	}

	private void findAllNodesToMerge(final AutState currentNode,
									 final HashSet<AutState> visitedNodes,
									 final HashSet<AutState> nodesToMerge)
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

	private void computeReachableTransitionLabels(final AutState currentNode,
												  final HashSet<AutState> visitedNodes,
												  final HashMap<AutState, HashSet<String>> reachableTransitionLabels)
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

	private void computeNodesToRemove(final AutState currentNode,
									  final HashSet<AutState> visitedNodes,
									  final HashSet<AutState> nodesToCutBefore,
									  final HashMap<AutState, HashSet<String>> reachableTransitionLabels)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		//if (currentNode.outgoingEdges().isEmpty()) return; //TODO CHECK FONCTIONNEMENT

		final HashSet<String> currentReachableTransitionLabels = reachableTransitionLabels.get(currentNode);

		if (currentReachableTransitionLabels == null)
		{
			throw new IllegalStateException("Node " + currentNode.label() + " does not have any reachable transition!");
		}

		final boolean canBeRemoved = !currentReachableTransitionLabels.contains("\"DUMMY_LOOPY_LABEL !ACC\"");

		if (canBeRemoved)
		{
			nodesToCutBefore.add(currentNode);
		}
		else
		{
			for (AutEdge outgoingTransition : currentNode.outgoingEdges())
			{
				this.computeNodesToRemove(outgoingTransition.targetNode(), visitedNodes, nodesToCutBefore, reachableTransitionLabels);
			}
		}
	}
}
