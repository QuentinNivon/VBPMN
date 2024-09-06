package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.nl_to_mc.MyOwnLogger;
import fr.inria.convecs.optimus.util.Pair;
import fr.inria.convecs.optimus.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class AutParser
{
	private final File autFile;
	private final HashMap<Integer, AutState> correspondences;
	private final boolean enhance;

	public AutParser(final File file)
	{
		this.autFile = file;
		this.correspondences = new HashMap<>();
		this.enhance = false;
	}

	public AutParser(final File file,
					 final boolean enhance)
	{
		this.autFile = file;
		this.correspondences = new HashMap<>();
		this.enhance = enhance;
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

			if (!this.enhance
				&& (!Utils.isAnInt(sourceStateIndexStr) || !Utils.isAnInt(targetStateIndexStr)))
			{
				throw new IllegalStateException("Either source state (\"" + sourceStateIndexStr + "\"), target state (\"" + targetStateIndexStr + "\"), or both, are not integers.");
			}

			//Do not parse DUMMY_LOOPY transitions
			if (label.contains("DUMMY")) return -1;

			final Pair<Integer, AutxStateType> sourceStateInfo = this.parseState(sourceStateIndexStr);
			final Pair<Integer, AutxStateType> targetStateInfo = this.parseState(targetStateIndexStr);
			final Pair<String, AutColor> labelInfo = this.parseLabel(label);
			final AutState sourceState = this.correspondences.computeIfAbsent(sourceStateInfo.getFirst(), n -> new AutState(sourceStateInfo.getFirst()));
			sourceState.setStateType(sourceStateInfo.getSecond());
			//MyOwnLogger.append("Source state " + sourceState.label() + " is of type " + (sourceState.getStateType() == null ? null : sourceState.getStateType().toString()));
			final AutState targetState = this.correspondences.computeIfAbsent(targetStateInfo.getFirst(), n -> new AutState(targetStateInfo.getFirst()));
			//targetState.setStateType(targetStateInfo.getSecond());
			//MyOwnLogger.append("Target state " + targetState.label() + " is of type " + (targetState.getStateType() == null ? null : targetState.getStateType().toString()));
			final AutEdge autEdge = new AutEdge(sourceState, labelInfo.getFirst(), targetState);
			autEdge.setColor(labelInfo.getSecond());
			sourceState.addOutgoingEdge(autEdge);
			targetState.addIncomingEdge(autEdge);

			return -1;
		}
	}

	private Pair<Integer, AutxStateType> parseState(final String stateStr)
	{
		if (!this.enhance)
		{
			return new Pair<>(Integer.parseInt(stateStr), null);
		}

		if (Utils.isAnInt(stateStr))
		{
			return new Pair<>(Integer.parseInt(stateStr), null);
		}

		final int firstColonIndex = stateStr.indexOf(':');
		final int lastColonIndex = stateStr.lastIndexOf(':');

		if (firstColonIndex == -1
			|| lastColonIndex == -1)
		{
			throw new IllegalStateException("Enhanced state information \"" + stateStr + "\" is not compliant with" +
					" the AUTX format.");
		}

		final String indexStr = stateStr.substring(0, firstColonIndex);
		final String n = stateStr.substring(firstColonIndex + 1, lastColonIndex);
		final String stateTypeStr = stateStr.substring(lastColonIndex + 1);

		if (!Utils.isAnInt(indexStr))
		{
			throw new IllegalStateException("State index in state information \"" + stateStr + "\" is not an integer" +
					" (" + indexStr + ").");
		}

		if (!n.equals("N"))
		{
			throw new IllegalStateException("Second information in state information \"" + stateStr + "\" should be" +
					" equal to \"N\". Got \"" + n + "\"");
		}

		final AutxStateType autxStateType = AutxStateType.strToStateType(stateTypeStr);

		if (autxStateType == null)
		{
			throw new IllegalStateException("Third information in state information \"" + stateStr + "\" should be" +
					" a valid state type (i.e., \"G\", \"R\", \"GR\" or \"GRB\". Got \"" + stateTypeStr + "\".");
		}

		return new Pair<>(Integer.parseInt(indexStr), autxStateType);
	}

	private Pair<String, AutColor> parseLabel(final String labelInfo)
	{
		if (!this.enhance)
		{
			return new Pair<>(labelInfo, AutColor.BLACK);
		}

		final int lastColonIndex = labelInfo.lastIndexOf(':');

		if (lastColonIndex == -1)
		{
			throw new IllegalStateException("AUTX edge label should contain the edge type (i.e. \"BLACK\", \"GREEN\"" +
					" or \"RED\" as last element. It was not provided here (" + labelInfo + ").");
		}

		final String label = labelInfo.substring(0, lastColonIndex);
		final AutColor color = AutColor.strToColor(labelInfo.substring(lastColonIndex + 1));

		if (color == null)
		{
			throw new IllegalStateException("AUTX edge label should contain the edge type (i.e. \"BLACK\", \"GREEN\"" +
					" or \"RED\" as last element, not \"" + labelInfo + "\".");
		}

		return new Pair<>(label, color);
	}
}
