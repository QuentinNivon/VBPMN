package fr.inria.convecs.optimus.aut;

public class AutEdge
{
	private final String label;
	private final AutState sourceNode;
	private final AutState targetNode;
	private boolean curved;
	private AutColor color;

	public AutEdge(final AutState sourceNode,
				   final String label,
				   final AutState targetNode)
	{
		this.sourceNode = sourceNode;
		this.label = label;
		this.targetNode = targetNode;
		this.color = AutColor.BLACK;
	}

	public AutEdge(final AutState sourceNode,
				   final String label,
				   final AutState targetNode,
				   final AutColor color)
	{
		this.sourceNode = sourceNode;
		this.label = label;
		this.targetNode = targetNode;
		this.color = color;
	}

	public void setColor(final AutColor color)
	{
		this.color = color;
	}

	public AutColor getColor()
	{
		return this.color;
	}

	public AutState sourceNode()
	{
		return this.sourceNode;
	}

	public AutState targetNode()
	{
		return this.targetNode;
	}

	public String label()
	{
		return this.label;
	}

	public boolean isCurved()
	{
		return this.curved;
	}

	public void setCurved()
	{
		this.curved = true;
	}

	//Override

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof AutEdge))
		{
			return false;
		}

		return ((AutEdge) o).sourceNode().equals(this.sourceNode)
				&& ((AutEdge) o).targetNode().equals(this.targetNode)
				&& ((AutEdge) o).label().equals(this.label);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;

		hash = hash * 31 + this.sourceNode.hashCode();

		for (int i = 0; i < this.label.length(); i++)
		{
			hash = hash * 31 + this.label.charAt(i);
		}

		hash = hash * 31 + this.targetNode.hashCode();

		return hash;
	}

	@Override
	public String toString()
	{
		return this.sourceNode.label() + " --> " + this.targetNode.label();
	}
}