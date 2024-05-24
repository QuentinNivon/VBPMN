package fr.inria.convecs.optimus.aut;

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

	public int nbEdges()
	{
		return this.autEdges.size();
	}

	public int sourceStateLabel()
	{
		return this.startNode.label();
	}
}
