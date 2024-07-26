package fr.inria.convecs.optimus.bpmn;

public enum BpmnColor
{
	RED("#FF0000", "#FFA0A0"),
	GREEN("#0D9319", "#ABF5B2");

	private final String strokeColor;
	private final String fillColor;

	BpmnColor(final String strokeColor,
			  final String fillColor)
	{
		this.strokeColor = strokeColor;
		this.fillColor = fillColor;
	}

	public String getStrokeColor()
	{
		return this.strokeColor;
	}

	public String getFillColor()
	{
		return this.fillColor;
	}
}
