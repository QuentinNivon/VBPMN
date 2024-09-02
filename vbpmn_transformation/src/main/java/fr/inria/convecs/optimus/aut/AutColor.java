package fr.inria.convecs.optimus.aut;

public enum AutColor
{


	BLACK("BLACK", "#000000"),
	GREEN("GREEN", "#389725"),
	RED("RED", "#FF0000");

	private final String value;
	private final String clearColor;

	AutColor(final String value,
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