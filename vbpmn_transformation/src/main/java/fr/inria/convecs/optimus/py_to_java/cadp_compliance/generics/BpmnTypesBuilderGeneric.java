package fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics;

public abstract class BpmnTypesBuilderGeneric
{
	protected String outputDirectory;

	public BpmnTypesBuilderGeneric()
	{

	}

	public void setOutputDirectory(final String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}

	public abstract void dumpBpmnTypesFile();
}
