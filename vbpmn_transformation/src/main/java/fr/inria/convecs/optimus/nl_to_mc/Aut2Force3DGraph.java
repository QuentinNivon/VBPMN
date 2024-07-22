package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;

import java.io.File;

public class Aut2Force3DGraph
{
	String s = "{\n" +
			"  \"nodes\": [\n" +
			"    {\"id\": \"Valjean\", \"group\": 2},\n" +
			"    {\"id\": \"Javert\", \"group\": 4},\n" +
			"    {\"id\": \"Cosette\", \"group\": 5},\n" +
			"    {\"id\": \"Marius\", \"group\": 8},\n" +
			"    {\"id\": \"Thenardier\", \"group\": 4}\n" +
			"  ],\n" +
			"  \"links\": [\n" +
			"    {\"source\": \"Valjean\", \"target\": \"Cosette\", \"value\": 3},\n" +
			"    {\"source\": \"Valjean\", \"target\": \"Javert\", \"value\": 7},\n" +
			"    {\"source\": \"Marius\", \"target\": \"Thenardier\", \"value\": 7},\n" +
			"    {\"source\": \"Marius\", \"target\": \"Cosette\", \"value\": 1},\n" +
			"    {\"source\": \"Thenardier\", \"target\": \"Valjean\", \"value\": 8},\n" +
			"    {\"source\": \"Javert\", \"target\": \"Thenardier\", \"value\": 5},\n" +
			"    {\"source\": \"Marius\", \"target\": \"Javert\", \"value\": 5}\n" +
			"  ]\n" +
			"}";

	private static final String NODES_KEYWORD = "nodes";
	private static final String EDGES_KEYWORD = "edges";
	private static final String ID_KEYWORD = "id";
	private static final String LABEL_KEYWORD = "label";
	private static final String COLOR_KEYWORD = "color";
	private static final String FROM_KEYWORD = "from";
	private static final String TO_KEYWORD = "to";
	private final File workingDirectory;
	private final File autFile;
	private final File visFile;
	private final AutGraph autGraph;

	public Aut2Force3DGraph(final File workingDirectory,
							final File autFile,
							final File visFile,
							final AutGraph autGraph)
	{
		this.workingDirectory = workingDirectory;
		this.autFile = autFile;
		this.visFile = visFile;
		this.autGraph = autGraph;
	}

	public void generateForce3DGraph()
	{

	}
}
