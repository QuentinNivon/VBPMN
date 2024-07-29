package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.util.Pair;

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

	public void addNodes(final Collection<AutNode> nodes)
	{
		this.autNodes.addAll(nodes);
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
		System.out.println("Nodes: " + nodes);
		System.out.println("Edges: " + edges);

		final HashMap<AutNode, AutNode> correspondences = new HashMap<>();

		for (AutNode autNode : nodes)
		{
			correspondences.put(autNode, new AutNode(autNode.label()));
		}

		System.out.println("Correspondences: " + correspondences);

		for (AutEdge autEdge : edges)
		{
			final AutNode newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutNode newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public AutGraph copyAndShift(final int shift)
	{
		final HashSet<AutNode> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		final HashMap<AutNode, AutNode> correspondences = new HashMap<>();

		for (AutNode autNode : nodes)
		{
			correspondences.put(autNode, new AutNode(autNode.label() + shift));
		}

		for (AutEdge autEdge : edges)
		{
			final AutNode newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutNode newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public int getMaxNodeLabel()
	{
		final int maxLabel = this.getMaxNodeLabel(this.startNode, new HashSet<>());

		if (maxLabel + 1 < this.nbNodes())
		{
			throw new IllegalStateException();
		}

		return maxLabel;
	}

	public int nbNodes()
	{
		final HashSet<AutNode> nodes = new HashSet<>();
		this.getNbNodes(this.startNode, nodes);
		return nodes.size();
	}

	//Private methods

	private void getNbNodes(final AutNode currentNode,
							final HashSet<AutNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			this.getNbNodes(outgoingEdge.targetNode(), visitedNodes);
		}
	}

	private int getMaxNodeLabel(final AutNode currentNode,
								final HashSet<AutNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return -1;
		}

		visitedNodes.add(currentNode);

		int max = currentNode.label();

		for (AutEdge autEdge : currentNode.outgoingEdges())
		{
			max = Math.max(max, getMaxNodeLabel(autEdge.targetNode(), visitedNodes));
		}

		return max;
	}

	private void retrieveNodesAndEdges(final AutNode currentNode,
									   final HashSet<AutNode> nodes,
									   final HashSet<AutEdge> edges)
	{
		if (nodes.contains(currentNode))
		{
			return;
		}

		nodes.add(currentNode);
		//edges.addAll(currentNode.incomingEdges());
		edges.addAll(currentNode.outgoingEdges());

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			this.retrieveNodesAndEdges(outgoingEdge.targetNode(), nodes, edges);
		}
	}
}
