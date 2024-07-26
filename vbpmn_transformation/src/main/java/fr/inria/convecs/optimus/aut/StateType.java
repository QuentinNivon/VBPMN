package fr.inria.convecs.optimus.aut;

public enum StateType
{
	GREEN_BLACK("G"),
	GREEN_RED("GR"),
	RED_BLACK("R"),
	GREEN_RED_BLACK("GRB");

	private final String value;

	StateType(final String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}
}
