package fr.inria.convecs.optimus.bpmn.proba;

public enum DistributionType
{
	CONSTANT("const");

	private final String label;

	DistributionType(final String s)
	{
		this.label = s;
	}

	String label()
	{
		return this.label;
	}
}
