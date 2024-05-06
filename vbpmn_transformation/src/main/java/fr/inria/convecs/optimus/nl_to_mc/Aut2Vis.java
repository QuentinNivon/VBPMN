package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.util.Utils;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Aut2Vis
{
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

	public Aut2Vis(final File workingDirectory,
				   final File autFile,
				   final String counterexampleFileName)
	{
		this.workingDirectory = workingDirectory;
		this.autFile = autFile;
		this.visFile = new File(workingDirectory.getAbsolutePath() + File.separator + counterexampleFileName + ".vis");
	}

	public File generateVisFile() throws IOException
	{
		if (this.autFile == null) return null;

		//Parse the .aut file and generate the corresponding VIS objects
		final ArrayList<VisNode> nodes = new ArrayList<>();
		final ArrayList<VisEdge> edges = new ArrayList<>();
		final FileInputStream fileInputStream = new FileInputStream(this.autFile);
		final Scanner scanner = new Scanner(fileInputStream);
		final HashMap<Integer, VisNode> correspondences = new HashMap<>();

		while (scanner.hasNextLine())
		{
			final String line = scanner.nextLine();
			this.parseLine(line, nodes, edges, correspondences);
		}

		scanner.close();
		fileInputStream.close();

		//Write the vis elements to file
		final JSONObject visElements = new JSONObject();
		final JSONArray nodesArray = new JSONArray();
		final JSONArray edgesArray = new JSONArray();

		//Write nodes
		for (VisNode visNode : nodes)
		{
			final JSONObject nodeObject = new JSONObject();
			nodeObject.put(ID_KEYWORD, visNode.getId());
			nodeObject.put(LABEL_KEYWORD, visNode.getLabel());

			if (visNode.getColor() != null)
			{
				nodeObject.put(COLOR_KEYWORD, visNode.getColor());
			}

			nodesArray.put(nodeObject);
		}
		visElements.put(NODES_KEYWORD, nodesArray);

		//Write edges
		for (VisEdge visEdge : edges)
		{
			final JSONObject edgeObject = new JSONObject();
			edgeObject.put(FROM_KEYWORD, visEdge.getSource().getId());
			edgeObject.put(TO_KEYWORD, visEdge.getTarget().getId());
			edgeObject.put(LABEL_KEYWORD, visEdge.getLabel());

			if (visEdge.getColor() != null)
			{
				edgeObject.put(COLOR_KEYWORD, visEdge.getColor());
			}

			edgesArray.put(edgeObject);
		}
		visElements.put(EDGES_KEYWORD, edgesArray);

		final PrintWriter printWriter = new PrintWriter(this.visFile);
		printWriter.println(visElements);
		printWriter.flush();
		printWriter.close();

		return this.visFile;
	}

	public File getVisFile()
	{
		if (!this.visFile.exists())
		{
			throw new IllegalStateException("VIS file has not been generated yet!");
		}

		return this.visFile;
	}

	//Private methods

	private void parseLine(final String line,
						   final ArrayList<VisNode> nodes,
						   final ArrayList<VisEdge> edges,
						   final HashMap<Integer, VisNode> correspondences)
	{
		if (!line.startsWith("des"))
		{
			final int openingParenthesisIndex = line.indexOf('(');
			final int firstComaIndex = line.indexOf(',');
			final int lastComaIndex = line.lastIndexOf(',');
			final int closingParenthesisIndex = line.lastIndexOf(')');
			final String sourceState = Utils.trim(line.substring(openingParenthesisIndex + 1, firstComaIndex));
			final String label = Utils.trim(line.substring(firstComaIndex + 1, lastComaIndex));
			final String targetState = Utils.trim(line.substring(lastComaIndex + 1, closingParenthesisIndex));

			if (!Utils.isAnInt(sourceState)
				|| !Utils.isAnInt(targetState))
			{
				throw new IllegalStateException("Source and target states should be integers. Got \"" + sourceState + "\" and \"" + targetState + "\".");
			}

			final int sourceStateId = Integer.parseInt(sourceState);
			final int targetStateId = Integer.parseInt(targetState);
			final VisNode sourceNode;
			final VisNode targetNode;

			if (correspondences.containsKey(sourceStateId))
			{
				sourceNode = correspondences.get(sourceStateId);
			}
			else
			{
				sourceNode = new VisNode(sourceStateId, sourceState);
				correspondences.put(sourceStateId, sourceNode);
				nodes.add(sourceNode);
			}

			if (correspondences.containsKey(targetStateId))
			{
				targetNode = correspondences.get(targetStateId);
			}
			else
			{
				targetNode = new VisNode(targetStateId, targetState);
				correspondences.put(targetStateId, targetNode);
				nodes.add(targetNode);
			}

			final VisEdge visEdge = new VisEdge(sourceNode, targetNode, label);
			edges.add(visEdge);
		}
	}

	//Private classes
	static class VisNode
	{
		private final int id;
		private final String label;
		private final String color;

		VisNode(final int id,
				final String label,
				final String color)
		{
			this.id = id;
			this.label = label;
			this.color = color;
		}

		VisNode(final int id,
				final String label)
		{
			this.id = id;
			this.label = label;
			this.color = null;
		}

		public int getId()
		{
			return id;
		}

		public String getLabel()
		{
			return label;
		}

		public String getColor()
		{
			return color;
		}
	}

	static class VisEdge
	{
		private final VisNode source;
		private final VisNode target;
		private final String label;
		private final String color;

		VisEdge(final VisNode source,
				final VisNode target,
				final String label,
				final String color)
		{
			this.source = source;
			this.target = target;
			this.label = label;
			this.color = color;
		}

		VisEdge(final VisNode source,
				final VisNode target,
				final String label)
		{
			this.source = source;
			this.target = target;
			this.label = label;
			this.color = null;
		}

		public String getLabel()
		{
			return label;
		}

		public VisNode getSource()
		{
			return source;
		}

		public VisNode getTarget()
		{
			return target;
		}

		public String getColor()
		{
			return color;
		}
	}
}
