package fr.inria.convecs.optimus.aut;

public enum AutxStateType
{
	GREEN_BLACK("G"),
	GREEN_RED("GR"),
	RED_BLACK("R"),
	GREEN_RED_BLACK("GRB");

	private final String value;

	AutxStateType(final String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}

	public static AutxStateType strToStateType(final String s)
	{
		for (AutxStateType autxStateType : AutxStateType.values())
		{
			if (autxStateType.getValue().equals(s))
			{
				return autxStateType;
			}
		}

		return null;
	}
}
