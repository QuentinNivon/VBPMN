package fr.inria.convecs.optimus.aut;

public enum AutColor
{
	BLACK("BLACK"),
	GREEN("GREEN"),
	RED("RED");

	private final String value;

	AutColor(final String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}

	public static AutColor strToColor(final String s)
	{
		for (AutColor autColor : AutColor.values())
		{
			if (autColor.getValue().equals(s))
			{
				return autColor;
			}
		}

		return null;
	}
}
