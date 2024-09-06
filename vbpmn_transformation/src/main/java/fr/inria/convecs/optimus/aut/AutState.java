package fr.inria.convecs.optimus.aut;

import java.util.HashSet;

public class AutState
{
	private final int label;
	private final HashSet<AutEdge> incomingEdges;
	private final HashSet<AutEdge> outgoingEdges;
	private AutxStateType autxStateType;

	public AutState(final int label)
	{
		this.label = label;
		this.autxStateType = null;
		this.incomingEdges = new HashSet<>();
		this.outgoingEdges = new HashSet<>();
	}

	public AutState(final int label,
					final AutxStateType autxStateType)
	{
		this.label = label;
		this.autxStateType = autxStateType;
		this.incomingEdges = new HashSet<>();
		this.outgoingEdges = new HashSet<>();
	}

	public void setStateType(final AutxStateType autxStateType)
	{
		this.autxStateType = autxStateType;
	}

	public AutxStateType getStateType()
	{
		return this.autxStateType;
	}

	public int label()
	{
		return this.label;
	}

	public String labelString()
	{
		return String.valueOf(this.label);
	}

	public void addIncomingEdge(final AutEdge edge)
	{
		this.incomingEdges.add(edge);
	}

	public void removeIncomingEdge(final AutEdge autEdge)
	{
		this.incomingEdges.remove(autEdge);
	}

	public void removeIncomingEdges()
	{
		this.incomingEdges.clear();
	}

	public void addOutgoingEdge(final AutEdge edge)
	{
		this.outgoingEdges.add(edge);
	}

	public void removeOutgoingEdge(final AutEdge edge)
	{
		this.outgoingEdges.remove(edge);
	}

	public HashSet<AutEdge> incomingEdges()
	{
		return this.incomingEdges;
	}

	public HashSet<AutEdge> outgoingEdges()
	{
		return this.outgoingEdges;
	}

	public AutState copy()
	{
		return new AutState(this.label, this.autxStateType);
	}

	//Override
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof AutState))
		{
			return false;
		}

		return ((AutState) o).label() == this.label();
	}

	@Override
	public int hashCode()
	{
		return this.label;
	}

	@Override
	public String toString()
	{
		return String.valueOf(this.label);
	}
}
