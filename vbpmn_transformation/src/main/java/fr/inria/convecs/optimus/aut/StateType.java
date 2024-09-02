package fr.inria.convecs.optimus.aut;

public enum StateType
{
	GREEN_BLACK("G", "#B18904"),
	GREEN_RED("GR", "#FACC2E"),
	RED_BLACK("R", "#FE9A2E"),
	GREEN_RED_BLACK("GRB", "#F7FE2E");

	private final String value;
	private final String clearColor;

	StateType(final String value,
			  final String clearColor)
	{
		this.value = value;
		this.clearColor = clearColor;
	}

	public String getValue()
	{
		return this.value;
	}

	public String getClearColor()
	{
		return this.clearColor;
	}

	public static StateType strToStateType(final String s)
	{
		for (StateType stateType : StateType.values())
		{
			if (stateType.getValue().equals(s))
			{
				return stateType;
			}
		}

		return null;
	}
}