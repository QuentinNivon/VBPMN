package fr.inria.convecs.optimus.aut;

public class AutEdge
{
	private final String label;
	private final AutNode sourceNode;
	private final AutNode targetNode;

	public AutEdge(final AutNode sourceNode,
				   final String label,
				   final AutNode targetNode)
	{
		this.sourceNode = sourceNode;
		this.label = label;
		this.targetNode = targetNode;
	}

	public AutNode sourceNode()
	{
		return this.sourceNode;
	}

	public AutNode targetNode()
	{
		return this.targetNode;
	}

	public String label()
	{
		return this.label;
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
}
