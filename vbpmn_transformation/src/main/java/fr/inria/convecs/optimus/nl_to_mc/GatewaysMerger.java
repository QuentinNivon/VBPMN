package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;
import fr.inria.convecs.optimus.util.Pair;

import java.util.HashSet;
import java.util.Set;

public class GatewaysMerger
{
	private final Graph graph;

	public GatewaysMerger(final Graph graph)
	{
		this.graph = graph;
	}

	public void mergeGateways()
	{
		Pair<Node, Node> mergeableGateways = this.getMergeableGateways(this.graph.initialNode(), new HashSet<>());
		System.out.println("Mergeable gateway: " + mergeableGateways);

		while (mergeableGateways != null)
		{
			final Node firstGateway = mergeableGateways.getFirst();
			final Node secondGateway = mergeableGateways.getSecond();
			final Node flowToRemove = secondGateway.parentNodes().iterator().next();
			final Set<Node> flowsToReplug = secondGateway.childNodes();
			firstGateway.removeChild(flowToRemove);

			for (Node flowToReplug : flowsToReplug)
			{
				firstGateway.addChild(flowToReplug);
				flowToReplug.removeParent(secondGateway);
				flowToReplug.addParent(firstGateway);
			}

			mergeableGateways = this.getMergeableGateways(this.graph.initialNode(), new HashSet<>());
		}
	}

	//Private methods

	private Pair<Node, Node> getMergeableGateways(final Node currentNode,
												  final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return null;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject() instanceof Gateway)
		{
			for (Node child : currentNode.childNodes())
			{
				final Node realChild = child.childNodes().iterator().next();

				if (realChild.bpmnObject() instanceof Gateway)
				{
					if (currentNode.bpmnObject().type() == realChild.bpmnObject().type())
					{
						final Gateway currentGateway = (Gateway) currentNode.bpmnObject();
						final Gateway childGateway = (Gateway) realChild.bpmnObject();

						if (currentGateway.isSplitGateway()
							&& childGateway.isSplitGateway())
						{
							return new Pair<>(currentNode, realChild);
						}
					}
				}
			}
		}

		for (Node child : currentNode.childNodes())
		{
			final Pair<Node, Node> mergeableGateways = this.getMergeableGateways(child, visitedNodes);

			if (mergeableGateways != null)
			{
				return mergeableGateways;
			}
		}

		return null;
	}
}
