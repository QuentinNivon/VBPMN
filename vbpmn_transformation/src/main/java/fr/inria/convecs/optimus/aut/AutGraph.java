package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class AutGraph
{
	private final AutState startNode;
	private final HashSet<AutState> autStates;
	private final HashSet<AutEdge> autEdges;

	public AutGraph(final AutState startNode)
	{
		this.startNode = startNode;
		this.autStates = new HashSet<>();
		this.autEdges = new HashSet<>();
	}

	public AutState startNode()
	{
		return this.startNode;
	}

	public void addNodes(final Collection<AutState> nodes)
	{
		this.autStates.addAll(nodes);
	}

	public int sourceStateLabel()
	{
		return this.startNode.label();
	}

	public Pair<HashSet<AutState>, HashSet<AutEdge>> nodesAndEdges()
	{
		final HashSet<AutState> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		return new Pair<>(nodes, edges);
	}

	public AutGraph copy()
	{
		final HashSet<AutState> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);
		System.out.println("Nodes: " + nodes);
		System.out.println("Edges: " + edges);

		final HashMap<AutState, AutState> correspondences = new HashMap<>();

		for (AutState autState : nodes)
		{
			correspondences.put(autState, new AutState(autState.label()));
		}

		System.out.println("Correspondences: " + correspondences);

		for (AutEdge autEdge : edges)
		{
			final AutState newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutState newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public AutGraph copyAndShift(final int shift)
	{
		final HashSet<AutState> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		final HashMap<AutState, AutState> correspondences = new HashMap<>();

		for (AutState autState : nodes)
		{
			correspondences.put(autState, new AutState(autState.label() + shift));
		}

		for (AutEdge autEdge : edges)
		{
			final AutState newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutState newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public AutGraph copy(final HashMap<AutState, AutState> correspondences)
	{
		final HashSet<AutState> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);
		System.out.println("Nodes: " + nodes);
		System.out.println("Edges: " + edges);

		for (AutState autState : nodes)
		{
			correspondences.put(autState, new AutState(autState.label()));
		}

		System.out.println("Correspondences: " + correspondences);

		for (AutEdge autEdge : edges)
		{
			final AutState newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutState newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public AutGraph copyAndShift(final int shift,
								 final HashMap<AutState, AutState> correspondences)
	{
		final HashSet<AutState> nodes = new HashSet<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.retrieveNodesAndEdges(this.startNode, nodes, edges);

		for (AutState autState : nodes)
		{
			correspondences.put(autState, new AutState(autState.label() + shift));
		}

		for (AutEdge autEdge : edges)
		{
			final AutState newSourceNode = correspondences.get(autEdge.sourceNode());
			final AutState newTargetNode = correspondences.get(autEdge.targetNode());
			final AutEdge copy = new AutEdge(newSourceNode, autEdge.label(), newTargetNode, autEdge.getColor());
			newSourceNode.addOutgoingEdge(copy);
			newTargetNode.addIncomingEdge(copy);
		}

		return new AutGraph(correspondences.get(this.startNode));
	}

	public int getMaxStateLabel()
	{
		final int maxLabel = this.getMaxStateLabel(this.startNode, new HashSet<>());

		if (maxLabel + 1 < this.nbNodes())
		{
			throw new IllegalStateException();
		}

		return maxLabel;
	}

	public int nbNodes()
	{
		final HashSet<AutState> nodes = new HashSet<>();
		this.getNbNodes(this.startNode, nodes);
		return nodes.size();
	}

	//Private methods

	private void getNbNodes(final AutState currentNode,
							final HashSet<AutState> visitedNodes)
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

	private int getMaxStateLabel(final AutState currentNode,
								 final HashSet<AutState> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return -1;
		}

		visitedNodes.add(currentNode);

		int max = currentNode.label();

		for (AutEdge autEdge : currentNode.outgoingEdges())
		{
			max = Math.max(max, getMaxStateLabel(autEdge.targetNode(), visitedNodes));
		}

		return max;
	}

	private void retrieveNodesAndEdges(final AutState currentNode,
									   final HashSet<AutState> nodes,
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
