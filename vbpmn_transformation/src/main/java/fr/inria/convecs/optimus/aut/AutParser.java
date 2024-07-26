package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class AutParser
{
	private final File autFile;
	private final HashMap<Integer, AutNode> correspondences;

	public AutParser(final File file)
	{
		this.autFile = file;
		this.correspondences = new HashMap<>();
	}

	public AutGraph parse() throws IOException
	{
		final FileInputStream inputStream = new FileInputStream(this.autFile);
		final Scanner scanner = new Scanner(inputStream);
		int firstNodeIndex = -1;

		while (scanner.hasNextLine())
		{
			final String line = scanner.nextLine();
			final int newIndex = this.parseLine(line);

			if (newIndex != -1)
			{
				firstNodeIndex = newIndex;
			}
		}

		scanner.close();
		inputStream.close();

		return new AutGraph(this.correspondences.get(firstNodeIndex));
	}

	//Private methods

	private int parseLine(final String line)
	{
		if (line.startsWith("des"))
		{
			final String firstStateIndexStr = Utils.trim(line.substring(line.indexOf('(') + 1, line.indexOf(',')));

			if (!Utils.isAnInt(firstStateIndexStr))
			{
				throw new IllegalStateException("The index of the initial state is not an int (\"" + firstStateIndexStr + "\").");
			}

			return Integer.parseInt(firstStateIndexStr);
		}
		else
		{
			final int firstParenthesisIndex = line.indexOf('(');
			final int firstComaIndex = line.indexOf(',');
			final int lastComaIndex = line.lastIndexOf(',');
			final int lastParenthesisIndex = line.lastIndexOf(')');

			if (firstParenthesisIndex == -1
				|| firstComaIndex == -1
				|| lastComaIndex == -1
				|| lastParenthesisIndex == -1)
			{
				throw new IllegalStateException("Line \"" + line + "\" is badly formed (missing coma or parenthesis).");
			}

			final String sourceStateIndexStr = Utils.trim(line.substring(firstParenthesisIndex + 1, firstComaIndex));
			final String label = Utils.trim(line.substring(firstComaIndex + 1, lastComaIndex));
			final String targetStateIndexStr = Utils.trim(line.substring(lastComaIndex + 1, lastParenthesisIndex));

			if (!Utils.isAnInt(sourceStateIndexStr)
				|| !Utils.isAnInt(targetStateIndexStr))
			{
				throw new IllegalStateException("Either source state (\"" + sourceStateIndexStr + "\"), target state (\"" + targetStateIndexStr + "\"), or both, are not integers.");
			}

			//Do not parse DUMMY_LOOPY transitions
			if (label.contains("DUMMY")) return -1;

			final int sourceStateIndex = Integer.parseInt(sourceStateIndexStr);
			final int targetStateIndex = Integer.parseInt(targetStateIndexStr);
			final AutNode sourceState = this.correspondences.computeIfAbsent(sourceStateIndex, n -> new AutNode(sourceStateIndex));
			final AutNode targetState = this.correspondences.computeIfAbsent(targetStateIndex, n -> new AutNode(targetStateIndex));
			final AutEdge autEdge = new AutEdge(sourceState, label, targetState);
			sourceState.addOutgoingEdge(autEdge);
			targetState.addIncomingEdge(autEdge);

			return -1;
		}
	}
}
