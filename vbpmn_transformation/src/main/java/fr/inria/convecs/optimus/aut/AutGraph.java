package fr.inria.convecs.optimus.aut;

import jdk.internal.net.http.common.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class AutGraph
{
	private final AutNode startNode;
	private final HashSet<AutNode> autNodes;
	private final HashSet<AutEdge> autEdges;

	public AutGraph(final AutNode startNode)
	{
		this.startNode = startNode;
		this.autNodes = new HashSet<>();
		this.autEdges = new HashSet<>();
	}

	public AutNode startNode()
	{
		return this.startNode;
	}

	public int nbNodes()
	{
		return this.autNodes.size();
	}

	public void addNodes(final Collection<AutNode> nodes)
	{
		this.autNodes.addAll(nodes);
	}

	public int nbEdges()
	{
		return this.autEdges.size();
	}

	public int sourceStateLabel()
	{
		return this.startNode.label();
	}

	public Pair<HashSet<AutNode>, HashSet<AutEdge>> nodesAndEdges()
	{
		final HashSet<AutNode> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		return new Pair<>(nodes, edges);
	}

	public AutGraph copy()
	{
		final HashSet<AutNode> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		final HashMap<AutNode, AutNode> correspondences = new HashMap<>();

		for (AutNode autNode : nodes)
		{
			correspondences.put(autNode, new AutNode(autNode.label()));
		}

		for (AutEdge autEdge : edges)
		{
			final AutNode newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutNode newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode);
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	//Private methods

	private void retrieveNodesAndEdges(final AutNode currentNode,
									   final HashSet<AutNode> nodes,
									   final HashSet<AutEdge> edges)
	{
		if (nodes.contains(currentNode))
		{
			return;
		}

		nodes.add(currentNode);
		edges.addAll(currentNode.incomingEdges());
		edges.addAll(currentNode.outgoingEdges());

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			this.retrieveNodesAndEdges(outgoingEdge.targetNode(), nodes, edges);
		}
	}
}
