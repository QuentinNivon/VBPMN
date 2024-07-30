package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutColor;
import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutState;

import java.util.HashSet;

public class FullCLTSBuilder
{
	private final AutGraph specification;
	private final AutGraph fullCLTS;
	private int lastNodeLabel;

	public FullCLTSBuilder(final AutGraph specification,
						   final AutGraph clts)
	{
		this.fullCLTS = clts.copy();
		this.specification = specification;
		this.lastNodeLabel = this.fullCLTS.getMaxStateLabel();
	}

	public AutGraph build()
	{
		final HashSet<AutEdge> edgesToAdd = new HashSet<>();
		this.buildFullCLTS(this.specification.startNode(), this.fullCLTS.startNode(), edgesToAdd, new HashSet<>());

		for (AutEdge autEdge : edgesToAdd)
		{
			autEdge.sourceNode().addOutgoingEdge(autEdge);
			autEdge.targetNode().addIncomingEdge(autEdge);
		}

		return this.fullCLTS;
	}

	public AutGraph fullCLTS()
	{
		return this.fullCLTS;
	}

	//Private methods

	private void buildFullCLTS(final AutState currentSpecNode,
							   final AutState currentFullCltsNode,
							   final HashSet<AutEdge> edgesToAdd,
							   final HashSet<AutState> visitedNodes)
	{
		if (visitedNodes.contains(currentFullCltsNode))
		{
			return;
		}

		visitedNodes.add(currentFullCltsNode);

		for (AutEdge specEdge : currentSpecNode.outgoingEdges())
		{
			if (!specEdge.label().contains("DUMMY"))
			{
				AutEdge matchingEdge = null;

				for (AutEdge cltsEdge : currentFullCltsNode.outgoingEdges())
				{
					if (cltsEdge.label().equals(specEdge.label()))
					{
						matchingEdge = cltsEdge;
						break;
					}
				}

				if (matchingEdge != null)
				{
					//There is an outgoing edge of the spec that goes to an incorrect state
					matchingEdge.setColor(AutColor.RED);
					this.buildFullCLTS(specEdge.targetNode(), matchingEdge.targetNode(), edgesToAdd, visitedNodes);
				}
				else
				{
					/*
						The current spec edge is correct (it does not belong to the CLTS).
						Thus, we add all the sub-lts of the specification starting from the next node
					 */
					System.out.println("Spec edge \"" + specEdge + "\" has no matching edge in the CLTS.");
					final AutGraph greenSpecPart = new AutGraph(specEdge.targetNode()).copyAndShift(this.lastNodeLabel + 1);
					this.lastNodeLabel = greenSpecPart.getMaxStateLabel();
					final AutEdge edgeToAdd = new AutEdge(currentFullCltsNode, specEdge.label(), greenSpecPart.startNode(), AutColor.GREEN);
					edgesToAdd.add(edgeToAdd);
				}
			}
		}
	}
}
