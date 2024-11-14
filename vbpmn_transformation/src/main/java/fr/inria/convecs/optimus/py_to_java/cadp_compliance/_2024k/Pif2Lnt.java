package fr.inria.convecs.optimus.py_to_java.cadp_compliance._2024k;

import fr.inria.convecs.optimus.pif.Peer;
import fr.inria.convecs.optimus.pif.SequenceFlow;
import fr.inria.convecs.optimus.pif.WorkflowNode;
import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.Pif2LntGeneric;
import fr.inria.convecs.optimus.util.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Pif2Lnt extends Pif2LntGeneric
{
	private static final String LTS_SUFFIX = ".bcg";
	private static final String LNT_SUFFIX = ".lnt";
	private static final String SYNC_STORE = "syncstore";
	private static final String MERGE_STORE = "mergestore";
	private static final String PAR_STORE = "parstore";
	private static final int MAX_CHAR_PER_LINE = 79;
	private static final int MAX_VARS_PER_LINE = 8; //After 8 variables, the line size necessarily exceeds 79 chars thus multiple lines are required
	private static final int PROCESS_INDENT_LENGTH = 8;
	//79 chars correspond to the standard line size in LNT.
	private static final String STANDARD_LNT_SEPARATOR = "----------------------------------------------------------" +
			"--------------------\n\n";
	private static final String COMMENTS_DASHES = "-----";

	public Pif2Lnt(boolean isBalanced)
	{
		super(isBalanced);
	}

	public Pif2Lnt()
	{

	}

	public boolean pairListContainsIdentifier(final Collection<Pair<String, Integer>> pairList,
											  final String identifier)
	{
		for (Pair<String, Integer> pair : pairList)
		{
			if (pair.getLeft().equals(identifier))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Dumps alphabet (list of strings) in the given file.
	 *
	 * @param alphabet      is the alphabet to dump
	 * @param stringBuilder is the stringBuilder in which the alphabet is dumped
	 * @param addAny        is a boolean indicating whether to add "any" or not
	 */
	public boolean dumpAlphabet(final ArrayList<String> alphabet,
								final StringBuilder stringBuilder,
								final boolean addAny)
	{
		final int nbElem = alphabet.size();
		boolean lineJumped = false;

		if (nbElem > 0)
		{
			stringBuilder.append("[");
			int counter = 1;
			int nbCharCurrentLine = 14;

			for (String element : alphabet)
			{
				if (counter != 1)
				{
					if (nbCharCurrentLine + element.length() + 1 > MAX_CHAR_PER_LINE)
					{
						lineJumped = true;
						stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
						nbCharCurrentLine = PROCESS_INDENT_LENGTH + element.length();
					}
					else
					{
						nbCharCurrentLine += element.length();
					}
				}
				else
				{
					nbCharCurrentLine += element.length();
				}

				stringBuilder.append(element);

				counter++;

				if (counter <= nbElem)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			if (addAny)
			{
				stringBuilder.append(": any");
			}

			stringBuilder.append("]");
			//stringBuilder.close();
		}

		return lineJumped;
	}

	/**
	 * Computes all combinations, in sorted order, any possible number from 1 to size(list)
	 *
	 * @param list the list on which combinations should be computed
	 */
	public ArrayList<ArrayList<String>> computeAllCombinations(final ArrayList<String> list)
	{
		final Collection<Collection<String>> allCombinations = PyToJavaUtils.getCombinationsOf(list);

		/*
			The PyToJavaUtils.getCombinationsOf(list) method does not necessarily return the combinations
			in ascending size, thus we need to reorder the combinations to match with the Python implementation
		 */

		final ArrayList<ArrayList<String>> orderedCombinations = new ArrayList<>();

		int currentSize = 1;
		boolean found = true;

		while (found)
		{
			found = false;

			for (Collection<String> combination : allCombinations)
			{
				if (combination.size() == currentSize)
				{
					orderedCombinations.add((ArrayList<String>) combination);
					found = true;
				}
			}

			currentSize++;
		}

		return orderedCombinations;
	}

	/**
	 * Takes a list of couples (ident,depth) resulting from the reachableOrJoin() method call
	 * and a number of outgoing flows. Checks if all flows lead to a same join.
	 *
	 * @param couplesList is the list of couples
	 * @param nbFlows     is the number of flows
	 * @return the join identifier if yes, "" otherwise.
	 */
	public String analyzeReachabilityResults(final ArrayList<Pair<String, Integer>> couplesList,
											 final int nbFlows)
	{
		//First, we check whether there is at least a corresponding join with depth 0 (there is at most one)
		boolean existJoin = false;
		String joinIdent = "";

		for (Pair<String, Integer> couple : couplesList)
		{
			if (couple.getRight() == 0)
			{
				joinIdent = couple.getLeft();
				existJoin = true;
				break;
			}
		}

		if (existJoin)
		{
			//We check if there are as many couples with the join identifiers as the number of flows
			int counter = 0;

			for (Pair<String, Integer> couple : couplesList)
			{
				if (couple.getLeft().equals(joinIdent)
						&& couple.getRight() == 0)
				{
					counter++;
				}
			}

			if (counter >= nbFlows) //You can have more splits in-between, thus more flows...
			{
				return joinIdent;
			}
			else
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}

	/**
	 * Abstract class for Nodes.
	 * Should not be directly used. Use child classes instead.
	 */
	abstract class Node
	{
		protected final String identifier;
		protected final ArrayList<Flow> incomingFlows;
		protected final ArrayList<Flow> outgoingFlows;
		protected final ArrayList<String> alphabet;

		Node(final String identifier,
			 final ArrayList<Flow> incomingFlows,
			 final ArrayList<Flow> outgoingFlows)
		{
			this.identifier = identifier;
			this.incomingFlows = incomingFlows;
			this.outgoingFlows = outgoingFlows;
			this.alphabet = new ArrayList<>();
		}

		ArrayList<String> alpha()
		{
			return this.alphabet;
		}

		String identifier()
		{
			return this.identifier;
		}

		void addIncomingFlow(final Flow flow)
		{
			this.incomingFlows.add(flow);
		}

		void addOutgoingFlow(final Flow flow)
		{
			this.outgoingFlows.add(flow);
		}

		ArrayList<Flow> incomingFlows()
		{
			return this.incomingFlows;
		}

		ArrayList<Flow> outgoingFlows()
		{
			return this.outgoingFlows;
		}

		Flow firstIncomingFlow()
		{
			return this.incomingFlows.get(0);
		}

		Flow firstOutgoingFlow()
		{
			return this.outgoingFlows.get(0);
		}

		abstract void writeMainLnt(final StringBuilder stringBuilder,
								   final int baseIndent);

		abstract void processLnt(final StringBuilder stringBuilder);

		abstract void writeLnt(final StringBuilder stringBuilder,
							   final int baseIndent);

		abstract ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
																  final int depth);
	}

	/**
	 * Class for Flows.
	 */
	class Flow
	{
		protected final String identifier;
		protected final Node source;
		protected final Node target;

		Flow(final String identifier,
			 final Node source,
			 final Node target)
		{
			this.identifier = identifier;
			this.source = source;
			this.target = target;
		}

		//Generates the (generic) process for flows, only once
		void writeLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("process flow [begin, finish: any] (ident:ID) is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("begin (ident);\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("finish (ident)\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end loop\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		//Returns the source node
		Node getSource()
		{
			return this.source;
		}

		//Returns the target node
		Node getTarget()
		{
			return this.target;
		}

		String identifier()
		{
			return this.identifier;
		}

		void processLnt(final StringBuilder stringBuilder)
		{
			final int argsMinIndent = 21 + 2;
			int nbCharCurrentLine = argsMinIndent + this.identifier.length() + 2;

			stringBuilder.append("flow (");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");

			if (nbCharCurrentLine + this.source.identifier().length() + 2 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + this.source.identifier().length();
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine += this.source.identifier().length();
			}

			stringBuilder.append(this.source.identifier());
			stringBuilder.append(",");

			if (nbCharCurrentLine + this.target.identifier().length() + 2 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
			}
			else
			{
				stringBuilder.append(" ");
			}

			stringBuilder.append(this.target.identifier());
			stringBuilder.append(")");
		}
	}

	/**
	 * Class for InitialEvent
	 */
	class InitialEvent extends Node
	{
		InitialEvent(String identifier,
					 ArrayList<Flow> incomingFlows,
					 ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on InitialEvent!");
		}

		//Generates the (generic) process for the initial event, only once
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("process init [begin, outf: any] is\n")
					.append(Utils.indentLNT(1))
					.append("var ident: ID in\n")
					.append(Utils.indentLNT(2))
					.append("begin;\n")
					.append(Utils.indentLNT(2))
					.append("outf (?ident of ID)\n")
					.append(Utils.indentLNT(1))
					.append("end var\n")
					.append("end process\n\n")
					.append(STANDARD_LNT_SEPARATOR);
		}

		/**
		 * Seeks or joins, for an initial event, just a recursive call on the target node of the outgoing flow.
		 * Returns the list of reachable or joins.
		 *
		 * @param visited the list of visited elements
		 * @param depth   the current depth
		 * @return the list of reachable or joins
		 */
		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(ArrayList<Pair<String, Integer>> visited,
														 int depth)
		{
			final ArrayList<Pair<String, Integer>> newVisited = new ArrayList<>(visited);
			newVisited.add(Pair.of(this.identifier, depth));

			return this.outgoingFlows.get(0).getTarget().reachableOrJoin(newVisited, depth);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			final String initial = "initial (";

			stringBuilder.append(initial)
					.append(this.identifier)
					.append(", ")
			;

			if (21 + this.identifier.length() + initial.length() + 3 +
				this.outgoingFlows.get(0).identifier().length() > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(21 + initial.length()));
			}

			stringBuilder.append(this.outgoingFlows.get(0).identifier()).append(")");
		}
	}

	/**
	 * Class for End Event
	 */
	class EndEvent extends Node
	{
		EndEvent(String identifier,
				 ArrayList<Flow> incomingFlows,
				 ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on EndEvent!");
		}

		//Generates the (generic) process for final events, only once
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("process final [incf, finish: any] is\n")
					.append(Utils.indentLNT(1));

			if (isBalanced)
			{
				stringBuilder.append("var ident: ID in\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("incf (?ident of ID);\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("finish\n");
				stringBuilder.append(Utils.indentLNT(1));
				stringBuilder.append("end var\n");
			}
			else
			{
				stringBuilder.append("var ident: ID in\n")
						.append(Utils.indentLNT(2))
						.append("loop\n")
						.append(Utils.indentLNT(3))
						.append("incf (?ident of ID); finish\n")
						.append(Utils.indentLNT(2))
						.append("end loop\n")
						.append(Utils.indentLNT(1))
						.append("end var\n");
			}

			stringBuilder.append("end process\n\n")
					.append(STANDARD_LNT_SEPARATOR);
		}

		/**
		 * Seeks an or join, for an initial event, just a recursive call on the target node of the outgoing flow
		 *
		 * @param visited the list of visited nodes
		 * @param depth the current depth
		 * @return the list of reachable or joins if any
		 */
		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(ArrayList<Pair<String, Integer>> visited,
														 int depth)
		{
			return new ArrayList<>();
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			final String finalStr = "final (";
			final int argsIndent = finalStr.length();
			final int flowMinIndent;
			int nbCharCurrentLine;

			stringBuilder.append(finalStr)
					.append(this.identifier)
					.append(",");

			if (this.incomingFlows.isEmpty())
				throw new IllegalStateException("End event \"" + this.identifier + "\" has no incoming flow!");

			final Flow firstFlow = this.incomingFlows.remove(0);

			if (firstFlow.identifier().length() + 21 + argsIndent + this.identifier.length() + 4 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(21 + argsIndent));
				nbCharCurrentLine = 21 + argsIndent;
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine = 21 + argsIndent + this.identifier.length() + 2;
			}

			flowMinIndent = nbCharCurrentLine + 1;
			stringBuilder.append("{").append(firstFlow.identifier());
			nbCharCurrentLine += firstFlow.identifier().length() + 1;

			for (Flow inFlow : this.incomingFlows)
			{
				stringBuilder.append(", ");
				nbCharCurrentLine += 2;

				if (nbCharCurrentLine + inFlow.identifier().length() + 2 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(flowMinIndent));
					nbCharCurrentLine = flowMinIndent + inFlow.identifier().length() + 2;
				}
				else
				{
					nbCharCurrentLine += inFlow.identifier().length() + 2;
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("})");

			this.incomingFlows.add(0, firstFlow);
		}
	}

	/**
	 * Abstract class for Communication
	 */
	abstract class Communication extends Node
	{
		protected final String message;

		Communication(String identifier,
					  ArrayList<Flow> incomingFlows,
					  ArrayList<Flow> outgoingFlows,
					  String message)
		{
			super(identifier, incomingFlows, outgoingFlows);
			this.message = message;
		}

		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(ArrayList<Pair<String, Integer>> visited, int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}
			else
			{
				final ArrayList<Pair<String, Integer>> newVisited = new ArrayList<>(visited);
				newVisited.add(Pair.of(this.identifier, depth));

				return this.outgoingFlows.get(0).getTarget().reachableOrJoin(newVisited, depth);
			}
		}
	}

	/**
	 * Class for Interaction
	 */
	class Interaction extends Communication
	{
		private final String sender;
		private final ArrayList<String> receivers;

		Interaction(String identifier,
					ArrayList<Flow> incomingFlows,
					ArrayList<Flow> outgoingFlows,
					String message,
					String sender,
					ArrayList<String> receivers)
		{
			super(identifier, incomingFlows, outgoingFlows, message);
			this.sender = sender;
			this.receivers = receivers;
		}

		//Generates the (generic) process for interactions, only once
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("process interaction [incf:any, inter:any, outf:any] is\n")
					.append(" var ident: ID in loop incf (?ident of ID); inter; outf (?ident of ID) end loop end var \n")
					.append("end process\n");
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			//TODO Vérifier
			this.writeLnt(stringBuilder, 21);
		}

		/**
		 * Computes alphabet for an interaction.
		 *
		 * @return the alphabet
		 */
		ArrayList<String> alpha()
		{
			final ArrayList<String> alphabet = new ArrayList<>();
			final StringBuilder res = new StringBuilder(this.sender);
			res.append("_");

			for (String e : this.receivers)
			{
				res.append(e)
						.append("_");
			}

			res.append(this.message);
			alphabet.add(res.toString());

			return alphabet;
		}

		/**
		 * Generates process instantiation for main LNT process
		 */
		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			//We assume one incoming flow and one outgoing flow
			stringBuilder.append("interaction [");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append("_finish,");
			stringBuilder.append(this.sender);
			stringBuilder.append("_");

			for (String e : this.receivers)
			{
				stringBuilder.append(e);
				stringBuilder.append("_");
			}

			stringBuilder.append(this.message);
			stringBuilder.append(",");
			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append("_begin]");
		}
	}

	/**
	 * Abstract class for MessageCommunication
	 */
	abstract class MessageCommunication extends Communication
	{
		MessageCommunication(String identifier,
							 ArrayList<Flow> incomingFlows,
							 ArrayList<Flow> outgoingFlows,
							 String message)
		{
			super(identifier, incomingFlows, outgoingFlows, message);
		}
	}

	/**
	 * Class for MessageSending
	 */
	class MessageSending extends MessageCommunication
	{
		MessageSending(String identifier,
					   ArrayList<Flow> incomingFlows,
					   ArrayList<Flow> outgoingFlows,
					   String message)
		{
			super(identifier, incomingFlows, outgoingFlows, message);
		}

		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("process messagesending [incf:any, msg:any, outf:any] is\n");
			stringBuilder.append(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var \n");
			stringBuilder.append("end process\n");
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> res = new ArrayList<>();
			res.add(this.message + "_EM");
			return res;
		}

		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("messagesending [");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append("_finish,");
			stringBuilder.append(this.message);
			stringBuilder.append("_EM,");
			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append("_begin]");
		}
	}

	/**
	 * Class for MessageReception
	 */
	class MessageReception extends MessageCommunication
	{
		MessageReception(String identifier,
						 ArrayList<Flow> incomingFlows,
						 ArrayList<Flow> outgoingFlows,
						 String message)
		{
			super(identifier, incomingFlows, outgoingFlows, message);
		}

		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("process messagereception [incf:any, msg:any, outf:any] is\n");
			stringBuilder.append(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var\n");
			stringBuilder.append("end process\n");
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> result = new ArrayList<>();
			result.add(this.message + "_REC");
			return result;
		}

		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("messagereception [");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append("_finish");
			stringBuilder.append(this.message);
			stringBuilder.append("_REC");
			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append("_begin]");
		}
	}

	/**
	 * Class for Task
	 */
	class Task extends Node
	{
		Task(String identifier,
			 ArrayList<Flow> incomingFlows,
			 ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			final int argsMinIndent = 21 + 6;
			final int incFlowsMinIndent;
			final int outFlowMinIndent;

			stringBuilder.append("task (");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			int nbCharCurrentLine = argsMinIndent + this.identifier.length() + 1;

			if (this.incomingFlows.isEmpty())
				throw new IllegalStateException("Task \"" + this.identifier + "\" has no incoming flows!");

			final Flow firstIncFlow = this.incomingFlows.remove(0);

			if (nbCharCurrentLine + firstIncFlow.identifier().length() + 3 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + firstIncFlow.identifier().length() + 1;
				incFlowsMinIndent = argsMinIndent + 1;
			}
			else
			{
				stringBuilder.append(" ");
				incFlowsMinIndent = nbCharCurrentLine + 2;
				nbCharCurrentLine += firstIncFlow.identifier().length() + 2;
			}

			stringBuilder.append("{").append(firstIncFlow.identifier());

			for (Flow inFlow : this.incomingFlows)
			{
				stringBuilder.append(", ");
				nbCharCurrentLine += 2;

				if (nbCharCurrentLine + inFlow.identifier().length() > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(incFlowsMinIndent));
					nbCharCurrentLine = incFlowsMinIndent + inFlow.identifier().length();
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("},");
			nbCharCurrentLine += 2;

			if (this.outgoingFlows.isEmpty())
				throw new IllegalStateException("Task \"" + this.identifier + "\" has no outgoing flows!");

			final Flow firstOutFlow = this.outgoingFlows.remove(0);

			if (nbCharCurrentLine + firstOutFlow.identifier().length() + 3 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + firstOutFlow.identifier().length() + 1;
				outFlowMinIndent = argsMinIndent + 1;
			}
			else
			{
				stringBuilder.append(" ");
				outFlowMinIndent = nbCharCurrentLine + 2;
				nbCharCurrentLine += firstIncFlow.identifier().length() + 2;
			}

			stringBuilder.append("{").append(firstOutFlow.identifier());

			for (Flow outFlow : this.outgoingFlows)
			{
				stringBuilder.append(",");
				nbCharCurrentLine += 2;

				if (nbCharCurrentLine + outFlow.identifier().length() > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(outFlowMinIndent));
					nbCharCurrentLine = outFlowMinIndent + outFlow.identifier().length();
				}

				stringBuilder.append(outFlow.identifier());
			}

			stringBuilder.append("})");

			this.incomingFlows.add(0, firstIncFlow);
			this.outgoingFlows.add(0, firstOutFlow);
		}

		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();
			final String toWrite = "process task_" + nbInc + "_" + nbOut + " [";
			boolean lineJumped = false;
			stringBuilder.append(toWrite);

			int nbCharCurrentLine = toWrite.length();

			if (nbInc == 1)
			{
				stringBuilder.append("incf, ");
				nbCharCurrentLine = toWrite.length() + 6;
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					final String flowId = "incf" + incCounter;

					if (nbCharCurrentLine + flowId.length() + 1 > MAX_CHAR_PER_LINE)
					{
						lineJumped = true;
						stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
						nbCharCurrentLine = PROCESS_INDENT_LENGTH + flowId.length() + 2;
					}
					else
					{
						nbCharCurrentLine += flowId.length() + 2;
					}

					stringBuilder.append(flowId)
							.append(", ");
					incCounter++;
				}
			}

			if (nbCharCurrentLine + 6 > MAX_CHAR_PER_LINE)
			{
				lineJumped = true;
				stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
				nbCharCurrentLine = PROCESS_INDENT_LENGTH + 6;
			}
			else
			{
				nbCharCurrentLine += 6;
			}

			stringBuilder.append("task, ");

			if (nbOut == 1)
			{
				if (nbCharCurrentLine + 9 > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
				}

				stringBuilder.append("outf");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					final String flowId = "outf" + outCounter;

					if (nbCharCurrentLine + flowId.length() + 1 > MAX_CHAR_PER_LINE)
					{
						lineJumped = true;
						stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
						nbCharCurrentLine = PROCESS_INDENT_LENGTH + flowId.length() + 2;
					}
					else
					{
						nbCharCurrentLine += flowId.length() + 2;
					}

					stringBuilder.append(flowId);
					outCounter++;

					if (outCounter < nbOut)
					{
						stringBuilder.append(", ");
						nbCharCurrentLine += 2;
					}
				}
			}

			stringBuilder.append(": any]");
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var ident: ID in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(3));

			if (nbInc == 1)
			{
				final String flowId = "incf (?ident of ID);";
				stringBuilder.append(flowId);
			}
			else
			{
				int incCounter = 0;
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("alt\n");
				stringBuilder.append(Utils.indentLNT(4));

				while (incCounter < nbInc)
				{
					stringBuilder.append("incf");
					stringBuilder.append(incCounter);
					stringBuilder.append(" (?ident of ID)");
					incCounter++;

					if (incCounter < nbInc)
					{
						stringBuilder.append("\n")
								.append(Utils.indentLNT(3))
								.append("[]\n")
								.append(Utils.indentLNT(4));
					}
				}

				stringBuilder.append("\nend alt;");
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("task;\n");
			stringBuilder.append(Utils.indentLNT(3));

			if (nbOut == 1)
			{
				stringBuilder.append("outf (?ident of ID)\n");
			}
			else
			{
				int outCounter = 0;
				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("alt\n");

				while (outCounter < nbOut)
				{
					stringBuilder.append(Utils.indentLNT(4));
					stringBuilder.append("outf");
					stringBuilder.append(outCounter);
					stringBuilder.append(" (?ident of ID)");
					outCounter++;

					if (outCounter < nbOut)
					{
						stringBuilder.append("\n")
								.append(Utils.indentLNT(3))
								.append("[]\n")
								.append(Utils.indentLNT(4));
					}
				}

				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("end alt\n");
			}

			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("end loop\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(ArrayList<Pair<String, Integer>> visited,
														 int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}
			else
			{
				if (this.outgoingFlows.size() == 1)
				{
					final ArrayList<Pair<String, Integer>> newVisited = new ArrayList<>(visited);
					newVisited.add(Pair.of(this.identifier, depth));
					return this.outgoingFlows.get(0).getTarget().reachableOrJoin(newVisited, depth);
				}
				else
				{
					final ArrayList<Pair<String, Integer>> res = new ArrayList<>();

					for (Flow f : this.outgoingFlows)
					{
						final ArrayList<Pair<String, Integer>> newVisited = new ArrayList<>(visited);
						newVisited.add(Pair.of(this.identifier, depth));
						res.addAll(f.getTarget().reachableOrJoin(newVisited, depth));
					}

					return res;
				}
			}
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> res = new ArrayList<>();
			res.add(this.identifier);
			return res;
		}

		@Override
		void writeMainLnt(final StringBuilder stringBuilder,
						  final int baseIndent)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();
			final String taskIdentifier = "task_" + nbInc + "_" + nbOut + " [";
			stringBuilder.append(taskIdentifier);
			int nbCharCurrentLine = baseIndent + taskIdentifier.length();

			int incCounter = 0;

			while (incCounter < nbInc)
			{
				final String incFlowIdentifier = this.incomingFlows.get(incCounter).identifier() + "_finish, ";

				if (incCounter == 0)
				{
					nbCharCurrentLine += incFlowIdentifier.length();
				}
				else
				{
					if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(baseIndent + taskIdentifier.length()));
						nbCharCurrentLine = baseIndent + taskIdentifier.length() + incFlowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += incFlowIdentifier.length();
					}
				}

				stringBuilder.append(incFlowIdentifier);
				incCounter++;
			}

			if (nbCharCurrentLine + this.identifier.length() + 1 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(baseIndent + taskIdentifier.length()));
				nbCharCurrentLine = baseIndent + taskIdentifier.length() + this.identifier.length() + 2;
			}
			else
			{
				nbCharCurrentLine += this.identifier.length() + 2;
			}

			stringBuilder.append(this.identifier);
			stringBuilder.append(", ");

			int outCounter = 0;

			while (outCounter < nbOut)
			{
				final String outFlowIdentifier = this.outgoingFlows.get(outCounter).identifier() + "_begin";

				if (nbCharCurrentLine + outFlowIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(baseIndent + taskIdentifier.length()));
					nbCharCurrentLine = baseIndent + taskIdentifier.length() + outFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += outFlowIdentifier.length();
				}

				stringBuilder.append(outFlowIdentifier);
				outCounter++;

				if (outCounter < nbOut)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append("]");
		}
	}

	/**
	 * Abstract class for Gateway
	 */
	abstract class Gateway extends Node
	{
		Gateway(String identifier,
				ArrayList<Flow> incomingFlows,
				ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		ArrayList<String> alpha()
		{
			return new ArrayList<>();
		}

		void processLnt(final StringBuilder stringBuilder,
						final String pattern,
						final String type)
		{
			final int argsMinIndent = 21 + 9;
			final int incFlowMinIndent;
			final int outFlowMinIndent;
			int nbCharCurrentLine = argsMinIndent + this.identifier.length() + 2;

			stringBuilder.append("gateway (");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");

			if (nbCharCurrentLine + pattern.length() > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + pattern.length();
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine += pattern.length();
			}

			stringBuilder.append(pattern);
			stringBuilder.append(",");

			if (nbCharCurrentLine + type.length() > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + type.length();
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine += type.length();
			}

			stringBuilder.append(type);
			stringBuilder.append(",");

			if (this.incomingFlows.isEmpty())
				throw new IllegalStateException("Gateway \"" + this.identifier + "\" has no incoming flows!");

			final Flow firstIncFlow = this.incomingFlows.remove(0);

			if (nbCharCurrentLine + firstIncFlow.identifier().length() + 3 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + firstIncFlow.identifier().length() + 3;
				incFlowMinIndent = argsMinIndent + 1;
			}
			else
			{
				incFlowMinIndent = nbCharCurrentLine + 5;
				nbCharCurrentLine += firstIncFlow.identifier().length() + 3;
				stringBuilder.append(" ");
			}

			stringBuilder.append("{");
			stringBuilder.append(firstIncFlow.identifier());

			for (Flow inFlow : this.incomingFlows)
			{
				stringBuilder.append(", ");
				nbCharCurrentLine += 2;

				if (inFlow.identifier().length() + nbCharCurrentLine + 2 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(incFlowMinIndent));
					nbCharCurrentLine = incFlowMinIndent + inFlow.identifier().length();
				}
				else
				{
					nbCharCurrentLine += inFlow.identifier().length();
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("},");

			if (this.outgoingFlows.isEmpty())
				throw new IllegalStateException("Gateway \"" + this.identifier + "\" has no outgoing flows!");

			final Flow firstOutFlow = this.outgoingFlows.remove(0);

			if (nbCharCurrentLine + firstOutFlow.identifier().length() + 3 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(argsMinIndent));
				nbCharCurrentLine = argsMinIndent + firstOutFlow.identifier().length() + 3;
				outFlowMinIndent = argsMinIndent + 1;
			}
			else
			{
				outFlowMinIndent = nbCharCurrentLine + 5;
				nbCharCurrentLine += firstOutFlow.identifier().length() + 3;
				stringBuilder.append(" ");
			}

			stringBuilder.append("{");
			stringBuilder.append(firstOutFlow.identifier());

			for (Flow outFlow : this.outgoingFlows)
			{
				stringBuilder.append(", ");
				nbCharCurrentLine += 2;

				if (outFlow.identifier().length() + nbCharCurrentLine + 2 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(outFlowMinIndent));
					nbCharCurrentLine = incFlowMinIndent + outFlow.identifier().length();
				}
				else
				{
					nbCharCurrentLine += outFlow.identifier().length();
				}

				stringBuilder.append(outFlow.identifier());
			}

			stringBuilder.append("})");

			this.incomingFlows.add(0, firstIncFlow);
			this.outgoingFlows.add(0, firstOutFlow);
		}
	}

	/**
	 * Abstract graph for Split Gateway
	 */
	abstract class SplitGateway extends Gateway
	{
		SplitGateway(String identifier,
					 ArrayList<Flow> incomingFlows,
					 ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		/**
		 * Generates process instantiation for all split gateways
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		void writeMainLnt(final StringBuilder stringBuilder,
						  final int baseIndent)
		{
			final String incFlowIdentifier = " [" + this.incomingFlows.get(0).identifier() + "_finish, ";
			final int nbOut = this.outgoingFlows.size();
			int i = 0;
			int nbCharCurrentLine = baseIndent + incFlowIdentifier.length();

			stringBuilder.append(incFlowIdentifier);

			while (i < nbOut)
			{
				final String outFlowIdentifier = this.outgoingFlows.get(i).identifier() + "_begin";

				if (nbCharCurrentLine + outFlowIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(baseIndent));
					nbCharCurrentLine = baseIndent + outFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += outFlowIdentifier.length();
				}

				stringBuilder.append(outFlowIdentifier);
				i++;

				if (i < nbOut)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append("]");
		}

		/**
		 * For a split (generic), if not visited yet, recursive call on the target nodes of all outgoing flows.
		 * Returns the list of reachable or joins.
		 */
		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}

			final ArrayList<Pair<String, Integer>> res = new ArrayList<>();

			for (Flow f : this.outgoingFlows)
			{
				final ArrayList<Pair<String, Integer>> temp = new ArrayList<>(visited);
				temp.add(Pair.of(this.identifier, depth));
				res.addAll(f.getTarget().reachableOrJoin(temp, depth));
			}

			return res;
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			stringBuilder.append("        split(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			stringBuilder.append("inclusive");
			stringBuilder.append(",");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append(",(");
			String separator = "";

			for (Flow ofl : this.outgoingFlows)
			{
				stringBuilder.append(separator);
				stringBuilder.append(ofl.identifier());
				separator = ",";
			}

			stringBuilder.append("))");
		}
	}

	/**
	 * Class for OrSplitGateway
	 */
	class OrSplitGateway extends SplitGateway
	{
		private String correspOrJoin;

		OrSplitGateway(String identifier,
					   ArrayList<Flow> incomingFlows,
					   ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
			this.correspOrJoin = "";
		}

		String getCorrespOrJoin()
		{
			return this.correspOrJoin;
		}

		void setCorrespOrJoin(final String correspOrJoin)
		{
			this.correspOrJoin = correspOrJoin;
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		@Override
		void writeLnt(final StringBuilder stringBuilder,
					  final int baseIndent)
		{
			final int nbOut = this.outgoingFlows.size();
			//TODO: update the translation to consider properly the default semantics (if there is such a branch)

			//We translate the inclusive split by enumerating all combinations in a alt / par
			final ArrayList<String> alphaOut = new ArrayList<>();
			int nb = 1;

			while (nb <= nbOut)
			{
				alphaOut.add("outf_" + nb);
				nb++;
			}

			final ArrayList<ArrayList<String>> allCombi = computeAllCombinations(alphaOut);
			final int nbt = allCombi.size();

			final String processIdentifier = "process orsplit_" + this.identifier + " [incf, ";
			stringBuilder.append(processIdentifier);

			//We dump the process alphabet (flows + synchronization points if necessary)
			int nbg = 1;
			int nbCharCurrentLine = processIdentifier.length();
			boolean lineJumped = false;

			while (nbg <= nbOut)
			{
				final String outFlowIdentifier = "outf_" + nbg;

				if (nbCharCurrentLine + outFlowIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + outFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += outFlowIdentifier.length();
				}

				stringBuilder.append(outFlowIdentifier);
				nbg++;

				if (nbg <= nbOut)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			if (nbt > 0
				&& (!isBalanced || !this.correspOrJoin.isEmpty()))
			{
				stringBuilder.append(", ");
				nbCharCurrentLine += 2;
				int counter = 1;

				for (Collection<String> ignored : allCombi) //TODO Bizarre ....
				{
					final String identifier = (isBalanced ? this.correspOrJoin : this.identifier) + "_" + counter;

					if (identifier.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
					{
						lineJumped = true;
						stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
						nbCharCurrentLine = PROCESS_INDENT_LENGTH + identifier.length();
					}
					else
					{
						nbCharCurrentLine += identifier.length();
					}

					stringBuilder.append(identifier);
					counter++;

					if (counter <= nbt)
					{
						stringBuilder.append(", ");
						nbCharCurrentLine += 2;
					}
				}
			}

			stringBuilder.append(": any]");
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var");

			int counterVar = allCombi.size();
			final int minIndent;

			if (counterVar > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indentLNT(2));
				nbCharCurrentLine = 6;
				minIndent = 6;
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine = 7;
				minIndent = 7;
			}

			while (counterVar > 0)
			{
				final String identifier = "ident" + counterVar;

				if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent));
					nbCharCurrentLine = minIndent + identifier.length();
				}
				else
				{
					nbCharCurrentLine += identifier.length();
				}

				stringBuilder.append(identifier);
				counterVar--;

				if (counterVar > 0)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			//TODO We generate unnecessary variables
			stringBuilder.append(": ID");

			if (allCombi.size() > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n")
						.append(Utils.indentLNT(1));
			}
			else
			{
				stringBuilder.append(" ");
			}

			stringBuilder.append("in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("var ident: ID in\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("incf (?ident of ID);\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("alt\n");

			nb = 1;
			//Counter for generating synchro points
			int counter = 1;

			for (Collection<String> element : allCombi)
			{
				final int nbElem = element.size();
				int nb2 = 1;
				stringBuilder.append(Utils.indentLNT(5));

				if (!isBalanced)
				{
					stringBuilder.append(this.identifier);
					stringBuilder.append("_");
					stringBuilder.append(counter);
					stringBuilder.append(";\n");
					stringBuilder.append(Utils.indentLNT(5));
					counter++;
				}

				if (nbElem > 1)
				{
					counterVar = allCombi.size();
					stringBuilder.append("par\n");

					for (String s : element)
					{
						stringBuilder.append(Utils.indentLNT(6));
						stringBuilder.append(s);
						stringBuilder.append(" (?ident");
						stringBuilder.append(counterVar);
						stringBuilder.append(" of ID)");
						counterVar--;
						nb2++;

						if (nb2 <= nbElem)
						{
							stringBuilder.append("\n")
									.append(Utils.indentLNT(5))
									.append("||\n")
							;
						}
					}

					stringBuilder.append("\n")
							.append(Utils.indentLNT(5))
							.append("end par");
				}
				else
				{
					stringBuilder.append(element.iterator().next());
					stringBuilder.append(" (?ident of ID)");
				}

				if (isBalanced)
				{
					//Add synchronization points if there's a corresponding join
					if (!this.correspOrJoin.isEmpty())
					{
						stringBuilder.append(";\n");
						stringBuilder.append(Utils.indentLNT(5));
						stringBuilder.append(this.correspOrJoin);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						counter++;
					}
				}

				nb++;

				if (nb <= nbt)
				{
					stringBuilder.append("\n")
							.append(Utils.indentLNT(4))
							.append("[]\n");
				}
			}

			stringBuilder.append("\n")
					.append(Utils.indentLNT(4))
					.append("end alt\n")
					.append(Utils.indentLNT(3))
					.append("end loop\n")
					.append(Utils.indentLNT(2))
					.append("end var\n")
					.append(Utils.indentLNT(1))
					.append("end var\n")
					.append("end process\n\n")
					.append(STANDARD_LNT_SEPARATOR);
		}

		/**
		 * Generates process instantiation for main LNT process.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		void writeMainLnt(final StringBuilder stringBuilder,
						  final int baseIndent)
		{
			if (!this.correspOrJoin.isEmpty()
					|| !isBalanced)
			{
				final int nbOut = this.outgoingFlows.size();
				final ArrayList<String> alphaOut = new ArrayList<>();
				int nb = 1;

				while (nb <= nbOut)
				{
					alphaOut.add("outf_" + nb);
					nb++;
				}

				final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaOut);
				final int nbCombi = allCombinations.size();
				int nbCharCurrentLine = baseIndent;

				if (isBalanced)
				{
					//We dump the synchronisation points
					if (nbCombi > 0)
					{
						int counter = 1;

						for (ArrayList<String> ignored : allCombinations) //TODO Bizarre...
						{
							final String identifier = this.correspOrJoin + "_" + counter;

							if (identifier.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
							{
								stringBuilder.append("\n").append(Utils.indent(baseIndent));
								nbCharCurrentLine = baseIndent + identifier.length();
							}
							else
							{
								nbCharCurrentLine += identifier.length();
							}

							stringBuilder.append(identifier);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(", ");
								nbCharCurrentLine += 2;
							}
						}

						stringBuilder.append(" ->\n");
						stringBuilder.append(Utils.indent(baseIndent + 3));
						nbCharCurrentLine = baseIndent + 3;
					}
				}

				//Process call + alphabet
				final String processIdentifier = "orsplit_" + this.identifier + " [";
				final String incFlowIdentifier = this.incomingFlows().get(0).identifier() + "_finish, ";
				final int minIndent = nbCharCurrentLine + processIdentifier.length();
				nbCharCurrentLine += processIdentifier.length() + incFlowIdentifier.length();

				stringBuilder.append(processIdentifier)
						.append(incFlowIdentifier);

				int i = 0;

				while (i < nbOut)
				{
					final String outFlowIdentifier = this.outgoingFlows.get(i).identifier() + "_begin";

					if (nbCharCurrentLine + outFlowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(minIndent));
						nbCharCurrentLine = minIndent + outFlowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += outFlowIdentifier.length();
					}

					stringBuilder.append(outFlowIdentifier);
					i++;

					if (i < nbOut)
					{
						stringBuilder.append(", ");
						nbCharCurrentLine += 2;
					}
				}

				if (nbCombi > 0)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
					int counter = 1;

					for (ArrayList<String> ignored : allCombinations)
					{
						final String identifier = (isBalanced ? this.correspOrJoin : this.identifier) + "_" + counter;

						if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
						{
							stringBuilder.append("\n").append(Utils.indent(minIndent));
							nbCharCurrentLine = minIndent + identifier.length();
						}
						else
						{
							nbCharCurrentLine += identifier.length();
						}

						stringBuilder.append(identifier);
						counter++;

						if (counter <= nbCombi)
						{
							stringBuilder.append(", ");
							nbCharCurrentLine += 2;
						}
					}
				}

				stringBuilder.append("]");
			}
			else
			{
				stringBuilder.append("orsplit_");
				stringBuilder.append(this.identifier);
				super.writeMainLnt(stringBuilder, baseIndent + this.identifier.length() + 9);
			}
		}

		/**
		 * For an or split, if not visited yet, recursive call on the target nodes of all outgoing flows.
		 * We increase the depth, to distinguish it from the split or being analyzed.
		 *
		 * @param visited the list of visited nodes
		 * @param depth the depth
		 * @return the list of reachable or joins.
		 */
		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}

			final ArrayList<Pair<String, Integer>> result = new ArrayList<>();

			for (Flow f : this.outgoingFlows)
			{
				final ArrayList<Pair<String, Integer>> temp = new ArrayList<>(visited);
				temp.add(Pair.of(this.identifier, depth));
				result.addAll(f.getTarget().reachableOrJoin(temp, depth + 1));
			}

			return result;
		}

		public void dumpMaude(final StringBuilder stringBuilder)
		{
			if (isBalanced)
			{
				super.dumpMaude(stringBuilder);
			}
			else
			{
				stringBuilder.append("        split(");
				stringBuilder.append(this.identifier);
				stringBuilder.append(",inclusive,");
				stringBuilder.append(this.incomingFlows.get(0).identifier());
				stringBuilder.append(",(");
				int counter = this.outgoingFlows.size();

				for (Flow outFlow : this.outgoingFlows)
				{
					final Random random = new Random();
					final double proba = Math.round(random.nextDouble() * 100.0) / 100.0;
					counter--;
					stringBuilder.append("(");
					stringBuilder.append(outFlow.identifier());
					stringBuilder.append(",");
					stringBuilder.append(proba);
					stringBuilder.append(")");

					if (counter > 0) stringBuilder.append(" ");
				}

				stringBuilder.append("))");
			}
		}
	}

	/**
	 * Class for XOrSplitGateway
	 */
	class XOrSplitGateway extends SplitGateway
	{
		XOrSplitGateway(String identifier,
						ArrayList<Flow> incomingFlows,
						ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			final int nbOut = this.outgoingFlows.size();
			boolean lineJumped = false;
			final String processIdentifier = "process xorsplit_" + this.identifier + " [incf, ";
			stringBuilder.append(processIdentifier);
			int nb = 1;
			int nbCharCurrentLine = processIdentifier.length();

			while (nb <= nbOut)
			{
				final String outFlowIdentifier = "outf_" + nb;

				if (nbCharCurrentLine + outFlowIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + outFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += outFlowIdentifier.length();
				}

				stringBuilder.append(outFlowIdentifier);
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append(": any]");
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var ident: ID in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("incf (?ident of ID);\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("alt\n");
			nb = 1;

			while (nb <= nbOut)
			{
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident of ID)");
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append("\n");
					stringBuilder.append(Utils.indentLNT(3));
					stringBuilder.append("[]\n");
				}
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("end alt\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("end loop\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("xorsplit_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder, baseIndent + this.identifier.length() + 11);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}
	}

	/**
	 * Class for AndSplitGateway
	 */
	class AndSplitGateway extends SplitGateway
	{
		AndSplitGateway(String identifier,
						ArrayList<Flow> incomingFlows,
						ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		@Override
		void writeLnt(final StringBuilder stringBuilder,
					  final int baseIndent)
		{
			final int nbOut = this.outgoingFlows.size();
			boolean lineJumped = false;
			final String processIdentifier = "process andsplit_" + this.identifier + " [incf, ";
			stringBuilder.append(processIdentifier);
			int nb = 1;

			int nbCharCurrentLine = processIdentifier.length();

			while (nb <= nbOut)
			{
				final String flowIdentifier = "outf_" + nb;

				if (nbCharCurrentLine + flowIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + flowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += flowIdentifier.length();
				}

				stringBuilder.append(flowIdentifier);
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append(": any]");
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var");

			int variablesCounter = nbOut;
			final int minIndentation;

			if (nbOut > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indentLNT(2));
				nbCharCurrentLine = 6;
				minIndentation = 6;
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine = 7;
				minIndentation = 7;
			}

			while (variablesCounter > 0)
			{
				final String flowIdentifier = "ident" + variablesCounter;

				if (variablesCounter != nbOut)
				{
					if (nbCharCurrentLine + flowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(minIndentation));
						nbCharCurrentLine = flowIdentifier.length() + minIndentation;
					}
					else
					{
						nbCharCurrentLine += flowIdentifier.length();
					}
				}
				else
				{
					nbCharCurrentLine += flowIdentifier.length();
				}

				stringBuilder.append(flowIdentifier);
				variablesCounter--;

				if (variablesCounter > 0)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append(": ID");

			if (nbOut > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n")
						.append(Utils.indentLNT(1));
			}
			else
			{
				stringBuilder.append(" ");
			}

			stringBuilder.append("in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("var ident: ID in\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("incf (?ident of ID);\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("par\n");
			nb = 1;
			variablesCounter = nbOut;

			while (nb <= nbOut)
			{
				stringBuilder.append(Utils.indentLNT(5));
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append("\n");
					stringBuilder.append(Utils.indentLNT(4));
					stringBuilder.append("||\n");
				}
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("end par\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("end loop\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("end var\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		void writeMainLnt(final StringBuilder stringBuilder,
						  final int baseIndent)
		{
			stringBuilder.append("andsplit_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder, baseIndent + 11 + this.identifier.length());
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}
	}

	/**
	 * Abstract class for JoinGateway
	 */
	abstract class JoinGateway extends Gateway
	{
		JoinGateway(String identifier,
					ArrayList<Flow> incomingFlows,
					ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		/**
		 * Generates process instantiation for all join gateways.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		void writeMainLnt(final StringBuilder stringBuilder,
						  final int baseIndent)
		{
			//We assume one outgoing flow
			final int nbInc = this.incomingFlows.size();
			stringBuilder.append(" [");
			int i = 0;
			int nbCharCurrentLine = baseIndent;

			while (i < nbInc)
			{
				final String flowIdentifier = this.incomingFlows().get(i).identifier() + "_finish, ";

				if (i == 0)
				{
					nbCharCurrentLine += flowIdentifier.length();
				}
				else
				{
					if (nbCharCurrentLine + flowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(baseIndent));
						nbCharCurrentLine = baseIndent + flowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += flowIdentifier.length();
					}
				}

				stringBuilder.append(flowIdentifier);
				i++;
			}

			final String outgoingFlowIdentifier = this.outgoingFlows.get(0).identifier() + "_begin]";

			if (nbCharCurrentLine + outgoingFlowIdentifier.length() > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(baseIndent));
			}

			stringBuilder.append(outgoingFlowIdentifier);
		}

		/**
		 * For a join (generic), if not visited yet, recursive call on the target node of the outgoing flow.
		 *
		 * @param visited the list of visited nodes
		 * @param depth the depth
		 * @return the list of reachable or joins
		 */
		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}

			final ArrayList<Pair<String, Integer>> copy = new ArrayList<>(visited);
			copy.add(Pair.of(this.identifier, depth));
			return this.outgoingFlows.get(0).getTarget().reachableOrJoin(copy, depth);
		}
	}

	/**
	 * Class for OrJoinGateway
	 */
	class OrJoinGateway extends JoinGateway
	{
		private String correspondingOrSplit;

		OrJoinGateway(String identifier,
					  ArrayList<Flow> incomingFlows,
					  ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
			this.correspondingOrSplit = ""; //contains the identifier of the corresponding split (if there is one)
		}

		void setCorrespondingOrSplit(final String correspondingOrSplit)
		{
			this.correspondingOrSplit = correspondingOrSplit;
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		/**
		 * Generates the process for inclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			final int nbInc = this.incomingFlows.size();

			if (isBalanced)
			{
				boolean lineJumped = false;
				final ArrayList<String> alphaInc = new ArrayList<>();
				int nb = 1;

				while (nb <= nbInc)
				{
					alphaInc.add("incf_" + nb);
					nb++;
				}

				final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaInc);
				final int nbCombi = allCombinations.size();
				final String processIdentifier = "process orjoin_" + this.identifier + " [";
				stringBuilder.append(processIdentifier);

				nb = 1;
				int nbCharCurrentLine = processIdentifier.length();

				while (nb <= nbInc)
				{
					final String incFlowIdentifier = "incf_" + nb + ", ";

					if (nb == 1)
					{
						nbCharCurrentLine += incFlowIdentifier.length();
					}
					else
					{
						if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
						{
							stringBuilder.append("\n")
									.append(Utils.indent(PROCESS_INDENT_LENGTH));
							lineJumped = true;
							nbCharCurrentLine = PROCESS_INDENT_LENGTH + incFlowIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += incFlowIdentifier.length();
						}
					}

					stringBuilder.append(incFlowIdentifier);
					nb++;
				}

				if (nbCharCurrentLine + 4 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					lineJumped = true;
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + 4;
				}
				else
				{
					nbCharCurrentLine += 4;
				}

				stringBuilder.append("outf");

				//we add to the alphabet potential additional synchronization points
				if (nbCombi > 0
						&& !this.correspondingOrSplit.isEmpty())
				{
					int counter = 1;
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;

					for (ArrayList<String> ignored : allCombinations)
					{
						final String identifier = this.identifier + "_" + counter;

						if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
						{
							lineJumped = true;
							stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
							nbCharCurrentLine = PROCESS_INDENT_LENGTH + identifier.length();
						}
						else
						{
							nbCharCurrentLine += identifier.length();
						}

						stringBuilder.append(identifier);
						counter++;

						if (counter <= nbCombi)
						{
							stringBuilder.append(", ");
							nbCharCurrentLine += 2;
						}
					}
				}

				stringBuilder.append(": any]");
				stringBuilder.append(lineJumped ? "\n" : " ");
				stringBuilder.append("is\n");
				stringBuilder.append(Utils.indentLNT(1));
				stringBuilder.append("var");

				int variablesCounter = allCombinations.size();
				final int minIndent;

				if (allCombinations.size() > MAX_VARS_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indentLNT(2));
					nbCharCurrentLine = 6;
					minIndent = 6;
				}
				else
				{
					stringBuilder.append(" ");
					nbCharCurrentLine = 7;
					minIndent = 7;
				}

				while (variablesCounter > 0) //TODO: we generate unnecessary variables
				{
					final String identifier = "ident" + variablesCounter;

					if (variablesCounter == allCombinations.size())
					{
						nbCharCurrentLine += identifier.length();
					}
					else
					{
						if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
						{
							stringBuilder.append("\n").append(Utils.indent(minIndent));
							nbCharCurrentLine = minIndent + identifier.length();
						}
						else
						{
							nbCharCurrentLine += identifier.length();
						}
					}

					stringBuilder.append(identifier);
					variablesCounter--;

					if (variablesCounter > 0)
					{
						stringBuilder.append(", ");
						nbCharCurrentLine += 2;
					}
				}

				stringBuilder.append(": ID");

				if (allCombinations.size() > MAX_VARS_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indentLNT(1));
				}
				else
				{
					stringBuilder.append(" ");
				}

				stringBuilder.append("in\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("var ident: ID in\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("loop\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("alt\n");
				nb = 1;
				int counter = 1;

				for (ArrayList<String> combination : allCombinations)
				{
					stringBuilder.append(Utils.indentLNT(5));
					int nbElem = combination.size();
					int nb2 = 1;

					// add synchronization points if there's a corresponding split
					if (!this.correspondingOrSplit.isEmpty())
					{
						stringBuilder.append(this.identifier);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						stringBuilder.append(";\n");
						stringBuilder.append(Utils.indentLNT(5));
						counter++;
					}

					if (nbElem > 1)
					{
						variablesCounter = allCombinations.size();
						stringBuilder.append("par\n");

						for (String element : combination)
						{
							stringBuilder.append(Utils.indentLNT(6));
							stringBuilder.append(element);
							stringBuilder.append(" (?ident");
							stringBuilder.append(variablesCounter);
							stringBuilder.append(" of ID)");
							variablesCounter--;
							nb2++;

							if (nb2 <= nbElem)
							{
								stringBuilder.append("\n")
										.append(Utils.indentLNT(5))
										.append("||\n")
								;
							}
						}

						stringBuilder.append("\n")
								.append(Utils.indentLNT(5))
								.append("end par");
					}
					else
					{
						stringBuilder.append(combination.iterator().next());
						stringBuilder.append(" (?ident of ID)");
					}

					nb++;

					if (nb <= nbCombi)
					{
						stringBuilder.append("\n")
								.append(Utils.indentLNT(4))
								.append("[]\n")
						;
					}
				}

				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("end alt;\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("outf (?ident of ID)\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("end loop\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("end var\n");
			}
			else
			{
				final String processIdentifier = "process orjoin_" + this.identifier + " [";
				stringBuilder.append(processIdentifier);
				int nb = 1;
				int nbCharCurrentLine = processIdentifier.length();
				boolean lineJumped = false;

				while (nb <= nbInc)
				{
					final String incFlowIdentifier = "incf_" + nb + ", ";

					if (nb == 1)
					{
						nbCharCurrentLine += incFlowIdentifier.length();
					}
					else
					{
						if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
						{
							lineJumped = true;
							stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
							nbCharCurrentLine = PROCESS_INDENT_LENGTH + incFlowIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += incFlowIdentifier.length();
						}
					}

					stringBuilder.append(incFlowIdentifier);
					nb++;
				}

				if (nbCharCurrentLine + 5 > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + 5;
				}
				else
				{
					nbCharCurrentLine += 5;
				}

				stringBuilder.append("outf, ");

				if (nbCharCurrentLine + 12 > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH + 12;
				}
				else
				{
					nbCharCurrentLine += 12;
				}

				stringBuilder.append("MoveOn: any] ");

				final int nbCharToConsider = lineJumped ? 14 : 17;

				if (nbCharCurrentLine + nbCharToConsider > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
				}

				stringBuilder.append("(mergeid: ID)");
				stringBuilder.append(lineJumped ? "\n" : " ");
				stringBuilder.append("is\n");
				stringBuilder.append(Utils.indentLNT(1));
				stringBuilder.append("var mergestatus: Bool, ident: ID in\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("loop\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("mergestatus := False;\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("while mergestatus == False loop\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("alt\n");

				nb = 1;

				while (nb <= nbInc)
				{
					stringBuilder.append(Utils.indentLNT(5));
					stringBuilder.append("incf_");
					stringBuilder.append(nb);
					stringBuilder.append(" (?ident of ID)");
					nb++;

					if (nb <= nbInc)
					{
						stringBuilder.append("\n")
								.append(Utils.indentLNT(4))
								.append("[]\n")
						;
					}
				}

				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("[]\n");
				stringBuilder.append(Utils.indentLNT(5));
				stringBuilder.append("MoveOn (mergeid);\n");
				stringBuilder.append(Utils.indentLNT(5));
				stringBuilder.append("mergestatus := True\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("end alt\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("end loop;\n");
				stringBuilder.append(Utils.indentLNT(3));
				stringBuilder.append("outf (?ident of ID)\n");
				stringBuilder.append(Utils.indentLNT(2));
				stringBuilder.append("end loop\n");
			}

			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		//Generates process instantiation for main LNT process
		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			if (isBalanced)
			{
				if (!this.correspondingOrSplit.isEmpty())
				{
					final int nbInc = this.incomingFlows.size();
					final ArrayList<String> alphaInc = new ArrayList<>();
					int nb = 1;

					while (nb <= nbInc)
					{
						alphaInc.add("incf_" + nb);
						nb++;
					}

					final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaInc);
					final int nbCombi = allCombinations.size();
					int nbCharCurrentLine = baseIndent;

					//We dump synchronization points
					if (nbCombi > 0)
					{
						int counter = 1;

						for (ArrayList<String> ignored : allCombinations)
						{
							final String identifier = this.identifier + "_" + counter;

							if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
							{
								stringBuilder.append("\n").append(Utils.indent(baseIndent));
								nbCharCurrentLine = baseIndent + identifier.length();
							}
							else
							{
								nbCharCurrentLine += identifier.length();
							}

							stringBuilder.append(identifier);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(", ");
								nbCharCurrentLine += 2;
							}
						}

						stringBuilder.append(" ->\n");
						stringBuilder.append(Utils.indent(baseIndent + 3));
						nbCharCurrentLine = baseIndent + 3;
					}

					//Process call + alphabet
					final String processIdentifier = "orjoin_" + this.identifier + " [";
					final int minIndent = nbCharCurrentLine + processIdentifier.length();
					stringBuilder.append(processIdentifier);
					nbCharCurrentLine = baseIndent + processIdentifier.length();
					int i = 0;

					while (i < nbInc)
					{
						final String incFlowIdentifier = this.incomingFlows.get(i).identifier() + "_finish, ";

						if (i == 0)
						{
							nbCharCurrentLine += incFlowIdentifier.length();
						}
						else
						{
							if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
							{
								stringBuilder.append("\n").append(Utils.indent(minIndent));
								nbCharCurrentLine = minIndent + incFlowIdentifier.length();
							}
							else
							{
								nbCharCurrentLine += incFlowIdentifier.length();
							}
						}

						stringBuilder.append(incFlowIdentifier);
						i++;
					}

					final String outFlowIdentifier = this.outgoingFlows.get(0).identifier() + "_begin";

					if (nbCharCurrentLine + outFlowIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(minIndent));
						nbCharCurrentLine = minIndent + outFlowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += outFlowIdentifier.length();
					}

					stringBuilder.append(outFlowIdentifier);

					if (nbCombi > 0)
					{
						int counter = 1;
						stringBuilder.append(", ");
						nbCharCurrentLine += 2;

						for (ArrayList<String> ignored : allCombinations)
						{
							final String identifier = this.identifier + "_" + counter;

							if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
							{
								stringBuilder.append("\n").append(Utils.indent(minIndent));
								nbCharCurrentLine = minIndent + identifier.length();
							}
							else
							{
								nbCharCurrentLine += identifier.length();
							}

							stringBuilder.append(identifier);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(", ");
								nbCharCurrentLine += 2;
							}
						}
					}

					stringBuilder.append("]");
				}
				else
				{
					stringBuilder.append("orjoin_");
					stringBuilder.append(this.identifier);
					super.writeMainLnt(stringBuilder, baseIndent);
				}
			}
			else
			{
				final String processIdentifier = "orjoin_" + this.identifier + " [";
				stringBuilder.append(processIdentifier);
				int nbCharCurrentLine = baseIndent + processIdentifier.length();
				final int minIndent = nbCharCurrentLine + 1;

				//We assume one outgoing flow
				final int nbInc = this.incomingFlows.size();
				int i = 0;

				while (i < nbInc)
				{
					final String incFlowIdentifier = this.incomingFlows.get(i).identifier() + "_finish, ";

					if (i == 0)
					{
						nbCharCurrentLine += incFlowIdentifier.length();
					}
					else
					{
						if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
						{
							stringBuilder.append("\n").append(Utils.indent(minIndent));
							nbCharCurrentLine = minIndent + incFlowIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += incFlowIdentifier.length();
						}
					}

					stringBuilder.append(incFlowIdentifier);
					i++;
				}

				final String outFlowIdentifier = this.outgoingFlows.get(0).identifier() + "_begin, ";

				if (nbCharCurrentLine + outFlowIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent));
					nbCharCurrentLine = minIndent + outFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += outFlowIdentifier.length();
				}

				stringBuilder.append(outFlowIdentifier);

				if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent));
					nbCharCurrentLine = minIndent + 7;
				}
				else
				{
					nbCharCurrentLine += 7;
				}

				stringBuilder.append("MoveOn]");

				final String params = " (" + this.identifier + ")";

				if (nbCharCurrentLine + params.length() > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent));
				}

				stringBuilder.append(params);
			}
		}

		/**
		 * For an or join, if not visited yet, recursive call on the target node of the outgoing flow.
		 * We store the result and we decrease the depth.
		 *
		 * @param visited the list of visited nodes
		 * @param depth the depth
		 * @return the list of reachable or joins
		 */
		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			if (pairListContainsIdentifier(visited, this.identifier))
			{
				return new ArrayList<>();
			}

			final ArrayList<Pair<String, Integer>> temp = new ArrayList<>(visited);
			temp.add(Pair.of(this.identifier, depth));

			final ArrayList<Pair<String, Integer>> result = new ArrayList<>();
			result.add(Pair.of(this.identifier, depth));
			result.addAll(this.outgoingFlows.get(0).getTarget().reachableOrJoin(temp, depth - 1));

			return result;
		}
	}

	/**
	 * Class for XOrJoinGateway
	 */
	class XOrJoinGateway extends JoinGateway
	{
		XOrJoinGateway(String identifier,
					   ArrayList<Flow> incomingFlows,
					   ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			final int nbInc = this.incomingFlows.size();
			boolean lineJumped = false;
			final String processIdentifier = "process xorjoin_" + this.identifier + " [";
			stringBuilder.append(processIdentifier);
			int nbCharCurrentLine = processIdentifier.length();
			int nb = 1;

			while (nb <= nbInc)
			{
				final String incFlowIdentifier = "incf_" + nb + ", ";

				if (nb == 1)
				{
					nbCharCurrentLine += incFlowIdentifier.length();
				}
				else
				{
					if (nbCharCurrentLine + incFlowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						lineJumped = true;
						stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
						nbCharCurrentLine = PROCESS_INDENT_LENGTH + incFlowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += incFlowIdentifier.length();
					}
				}

				stringBuilder.append(incFlowIdentifier);
				nb++;
			}

			final String outFlowIdentifier = "outf: any]";
			final int nbCharToConsider = lineJumped ? outFlowIdentifier.length() : outFlowIdentifier.length() + 3;

			if (nbCharToConsider + nbCharCurrentLine > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
			}

			stringBuilder.append(outFlowIdentifier);
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var ident: ID in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("alt\n");
			nb = 1;

			while (nb <= nbInc)
			{
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident of ID)");
				nb++;

				if (nb <= nbInc)
				{
					stringBuilder.append("\n");
					stringBuilder.append(Utils.indentLNT(3));
					stringBuilder.append("[]\n");
				}
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("end alt;\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("outf (?ident of ID)\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("end loop\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("xorjoin_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder, baseIndent + this.identifier.length() + 10);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}
	}

	/**
	 * Class for AndJoinGateway
	 */
	class AndJoinGateway extends JoinGateway
	{
		AndJoinGateway(String identifier,
					   ArrayList<Flow> incomingFlows,
					   ArrayList<Flow> outgoingFlows)
		{
			super(identifier, incomingFlows, outgoingFlows);
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder, 21); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder the builder to which the gateway should be dumped
		 */
		@Override
		void writeLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			final int nbInc = this.incomingFlows.size();
			boolean lineJumped = false;
			final String toWrite = "process andjoin_" + this.identifier + " [";
			stringBuilder.append(toWrite);
			int nb = 1;

			int nbCharCurrentLine = toWrite.length();

			while (nb <= nbInc)
			{
				final String flowName = "incf_" + nb + ", ";

				if (nbCharCurrentLine + flowName.length() > MAX_CHAR_PER_LINE)
				{
					lineJumped = true;
					stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
					nbCharCurrentLine = PROCESS_INDENT_LENGTH;
				}
				else
				{
					nbCharCurrentLine += flowName.length();
				}

				stringBuilder.append(flowName);
				nb++;
			}

			final String outFlowName = "outf: any]";
			final int nbCharToConsider = lineJumped ? outFlowName.length() : outFlowName.length() + 3;

			if (nbCharCurrentLine + nbCharToConsider > MAX_CHAR_PER_LINE)
			{
				lineJumped = true;
				stringBuilder.append("\n").append(Utils.indent(PROCESS_INDENT_LENGTH));
			}

			stringBuilder.append(outFlowName);
			stringBuilder.append(lineJumped ? "\n" : " ");
			stringBuilder.append("is\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("var");

			int variablesCounter = nbInc;
			final int minIndent;

			if (nbInc > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indentLNT(2));
				nbCharCurrentLine = 6;
				minIndent = 6;
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine = 7;
				minIndent = 7;
			}

			while (variablesCounter > 0)
			{
				final String identifier = "ident" + variablesCounter;

				if (variablesCounter != nbInc)
				{
					if (nbCharCurrentLine + identifier.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(minIndent));
						nbCharCurrentLine = minIndent + identifier.length();
					}
					else
					{
						nbCharCurrentLine += identifier.length();
					}
				}
				else
				{
					nbCharCurrentLine += identifier.length();
				}

				stringBuilder.append(identifier);
				variablesCounter--;

				if (variablesCounter > 0)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			stringBuilder.append(":ID");

			if (nbInc > MAX_VARS_PER_LINE)
			{
				stringBuilder.append("\n")
						.append(Utils.indentLNT(1));
			}
			else
			{
				stringBuilder.append(" ");
			}

			stringBuilder.append("in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("var ident:ID in\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("loop\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("par\n");
			nb = 1;
			variablesCounter = nbInc;

			while (nb <= nbInc)
			{
				stringBuilder.append(Utils.indentLNT(5));
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbInc)
				{
					stringBuilder.append("\n");
					stringBuilder.append(Utils.indentLNT(4));
					stringBuilder.append("||\n");
					//stringBuilder.append(Utils.indentLNT(4));
				}
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("end par;\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("outf (?ident of ID)\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("end loop\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("end var\n");
			stringBuilder.append(Utils.indentLNT(1));
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		void writeMainLnt(final StringBuilder stringBuilder, final int baseIndent)
		{
			stringBuilder.append("andjoin_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder, baseIndent + 10 + this.identifier.length());
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}
	}

	/**
	 * Class for Processes described in PIF.
	 * Attributes: a name, a list of nodes, a list of flows, an initial node, a list of final nodes.
	 */
	class Process
	{
		private final ArrayList<Node> nodes;
		private final ArrayList<Node> finals;
		private final ArrayList<Flow> flows;
		private String name;
		private Node initial;

		Process()
		{
			this.name = "";
			this.nodes = new ArrayList<>();
			this.flows = new ArrayList<>();
			this.initial = null;
			this.finals = new ArrayList<>();
		}

		String name()
		{
			return this.name;
		}

		Node getNode(final String identifier)
		{
			if (identifier.equals(this.initial.identifier()))
			{
				return this.initial;
			}

			for (Node n : this.finals)
			{
				if (n.identifier().equals(identifier))
				{
					return n;
				}
			}

			for (Node n : this.nodes)
			{
				if (n.identifier().equals(identifier))
				{
					return n;
				}
			}

			throw new IllegalStateException("No node found with identifier \"" + identifier + "\".");
		}

		void addFlow(final Flow flow)
		{
			if (flow.getSource().identifier().equals(this.initial.identifier()))
			{
				this.initial.addOutgoingFlow(flow);
			}

			for (Node n : this.finals)
			{
				if (flow.getTarget().identifier().equals(n.identifier()))
				{
					n.addIncomingFlow(flow);
				}
			}

			for (Node n : this.nodes)
			{
				if (flow.getSource().identifier().equals(n.identifier()))
				{
					n.addOutgoingFlow(flow);
				}
				if (flow.getTarget().identifier().equals(n.identifier()))
				{
					n.addIncomingFlow(flow);
				}
			}
		}

		/**
		 * Computes the process alphabet
		 *
		 * @return the process alphabet
		 */
		ArrayList<String> alpha()
		{
			final ArrayList<String> alphabet = new ArrayList<>();

			for (Node n : this.nodes)
			{
				alphabet.addAll(n.alpha());
			}

			return alphabet;
		}

		/**
		 * This method applies a pre-processing to the whole process
		 * and computes correspondences between or splits/merges.
		 */
		void reachableOrJoin()
		{
			//We traverse all process nodes and call this computation for all inclusive splits
			for (Node n : this.nodes)
			{
				if (n instanceof OrSplitGateway)
				{
					final ArrayList<Pair<String, Integer>> resTmp = n.reachableOrJoin(new ArrayList<>(), -1);
					final String res = analyzeReachabilityResults(resTmp, n.outgoingFlows().size());

					if (!res.isEmpty())
					{
						((OrSplitGateway) n).setCorrespOrJoin(res); //we update the split attribute
						final Node joinNode = this.getNode(res); //we retrieve the object corresponding to the join id
						((OrJoinGateway) joinNode).setCorrespondingOrSplit(n.identifier()); //we update the join attribute
					}
				}
			}
		}

		/**
		 * Computes the list with the additional synchronization points for corresponding or splits/joins.
		 *
		 * @return the list of synchronisation points
		 */
		ArrayList<String> computeAddSynchroPoints(final boolean needCorrespondingJoin)
		{
			final ArrayList<String> res = new ArrayList<>();

			for (Node n : this.nodes)
			{
				if (n instanceof OrSplitGateway)
				{
					if (needCorrespondingJoin
						&& ((OrSplitGateway) n).getCorrespOrJoin().isEmpty())
					{
						continue;
					}

					final ArrayList<String> alphaOut = new ArrayList<>();
					int nb = 1;

					while (nb <= n.outgoingFlows().size())
					{
						alphaOut.add("outf_" + nb);
						nb++;
					}

					final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaOut);
					int counter = 1;

					for (ArrayList<String> ignored : allCombinations)
					{
						res.add(n.identifier() + "_" + counter);
						counter++;
					}
				}
			}

			return res;
		}

		Pair<String, Integer> getFlowMsgsAndLineLength(final int minIndent,
													   final int nbCharsAlreadyWritten)
		{
			final StringBuilder flowBuilder = new StringBuilder();
			final int nbFlows = this.flows.size();
			int counter = 1;
			int nbCharCurrentLine = nbCharsAlreadyWritten;

			for (Flow flow : this.flows)
			{
				final String beginFlowIdentifier = flow.identifier() + "_begin, ";
				final String finishFlowIdentifier = flow.identifier() + "_finish";

				if (counter == 1)
				{
					nbCharCurrentLine += beginFlowIdentifier.length();
				}
				else
				{
					if (nbCharCurrentLine + beginFlowIdentifier.length() > MAX_CHAR_PER_LINE)
					{
						flowBuilder.append("\n").append(Utils.indent(minIndent));
						nbCharCurrentLine = minIndent + beginFlowIdentifier.length();
					}
					else
					{
						nbCharCurrentLine += beginFlowIdentifier.length();
					}
				}

				flowBuilder.append(beginFlowIdentifier);

				if (nbCharCurrentLine + finishFlowIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					flowBuilder.append("\n").append(Utils.indent(minIndent));
					nbCharCurrentLine = minIndent + finishFlowIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += finishFlowIdentifier.length();
				}

				flowBuilder.append(finishFlowIdentifier);

				counter++;

				if (counter <= nbFlows)
				{
					flowBuilder.append(", ");
					nbCharCurrentLine += 2;
				}
			}

			return Pair.of(flowBuilder.toString(), nbCharCurrentLine);
		}

		void processDump(final StringBuilder stringBuilder)
		{
			final String returnProc = "return proc (";
			final int argsIndent = returnProc.length() + 3;

			stringBuilder.append("function p1 (): BPROCESS is\n")
					.append(Utils.indentLNT(1))
					.append("return proc (")
					.append(this.name)
					.append(",\n")
					.append(Utils.indent(argsIndent))
					.append("{i (")
			;

			this.initial.processLnt(stringBuilder);
			stringBuilder.append("),\n")
					.append(Utils.indent(argsIndent + 1))
			;

			//handle final
			boolean first = true;
			stringBuilder.append("f ({");

			for (Node fnode : this.finals)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",\n").append(Utils.indent(argsIndent + 5));
				}

				fnode.processLnt(stringBuilder);
			}

			stringBuilder.append("}),\n");

			//TODO: eliminate iterating twice / Separate printer class?
			//handle tasks
			stringBuilder.append(Utils.indent(argsIndent + 1))
					.append("t ({");
			first = true;

			for (Node pNode : this.nodes)
			{
				if (pNode instanceof Task)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						stringBuilder.append(",\n").append(Utils.indent(argsIndent + 5));
					}

					pNode.processLnt(stringBuilder);
				}
			}

			stringBuilder.append("}),\n");

			//handle gateways
			stringBuilder.append(Utils.indent(argsIndent + 1));
			stringBuilder.append("g ({");
			first = true;

			for (Node pNode : this.nodes)
			{
				if (pNode instanceof Gateway)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						stringBuilder.append(",\n").append(Utils.indent(argsIndent + 5));
					}

					if (pNode instanceof XOrJoinGateway)
					{
						((XOrJoinGateway) pNode).processLnt(stringBuilder, "merge", "xor");
					}
					if (pNode instanceof XOrSplitGateway)
					{
						((XOrSplitGateway) pNode).processLnt(stringBuilder, "split", "xor");
					}
					if (pNode instanceof OrJoinGateway)
					{
						((OrJoinGateway) pNode).processLnt(stringBuilder, "merge", "or");
					}
					if (pNode instanceof OrSplitGateway)
					{
						((OrSplitGateway) pNode).processLnt(stringBuilder, "split", "or");
					}
					if (pNode instanceof AndJoinGateway)
					{
						((AndJoinGateway) pNode).processLnt(stringBuilder, "merge", "and");
					}
					if (pNode instanceof AndSplitGateway)
					{
						((AndSplitGateway) pNode).processLnt(stringBuilder, "split", "and");
					}
				}
			}

			stringBuilder.append("})},\n");
			//flows
			stringBuilder.append(Utils.indent(argsIndent));
			stringBuilder.append("{");
			first = true;

			for (Flow flow : this.flows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",\n").append(Utils.indent(argsIndent + 1));
				}

				flow.processLnt(stringBuilder);
			}

			stringBuilder.append("})\n");
			stringBuilder.append("end function\n\n");
			stringBuilder.append(STANDARD_LNT_SEPARATOR);
		}

		//TODO A vérifier : passage de networkx à JGraphT
		boolean checkInclusiveCycle()
		{
			final DefaultDirectedGraph<Node, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

			for (Flow flow : this.flows)
			{
				directedGraph.addVertex(flow.getSource());
				directedGraph.addVertex(flow.getTarget());
				directedGraph.addEdge(flow.getSource(), flow.getTarget());
			}

			final CycleDetector<Node, DefaultEdge> cycleDetector = new CycleDetector<>(directedGraph);

			for (Node node : directedGraph.vertexSet())
			{
				if (node instanceof OrJoinGateway)
				{
					if (cycleDetector.detectCyclesContainingVertex(node))
					{
						return true;
					}
				}
			}

			return false;
		}

		void generateScheduler(final StringBuilder stringBuilder)
		{
			final String processIdentifier = "process scheduler [";
			stringBuilder.append(processIdentifier);
			final Pair<String, Integer> flowMsgsAndPosition = this.getFlowMsgsAndLineLength(processIdentifier.length(), processIdentifier.length());
			stringBuilder.append(flowMsgsAndPosition.getLeft());
			//Add split synchro params
			final ArrayList<String> synchroParams = this.computeAddSynchroPoints(false);
			int nbCharCurrentLine = flowMsgsAndPosition.getRight();

			if (!synchroParams.isEmpty())
			{
				for (String synchroParam : synchroParams)
				{
					stringBuilder.append(", ");
					nbCharCurrentLine += 2;

					if (nbCharCurrentLine + synchroParam.length() > MAX_CHAR_PER_LINE)
					{
						stringBuilder.append("\n").append(Utils.indent(processIdentifier.length()));
						nbCharCurrentLine = processIdentifier.length() + synchroParam.length();
					}
					else
					{
						nbCharCurrentLine += synchroParam.length();
					}

					stringBuilder.append(synchroParam);
				}
			}

			//This parameter stores the set of active flows/tokens
			stringBuilder.append(", ");
			nbCharCurrentLine += 2;

			if (nbCharCurrentLine + 11 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(processIdentifier.length()));
			}

			stringBuilder.append("MoveOn:any]\n");
			stringBuilder.append(Utils.indent(processIdentifier.length() - 1));
			stringBuilder.append("(activeflows: IDS, bpmn: BPROCESS, syncstore: IDS,\n");
			stringBuilder.append(Utils.indent(processIdentifier.length()));
			stringBuilder.append("mergestore: IDS, parstore: IDS)\n");
			stringBuilder.append("is\n");

			final ArrayList<String> identSet = new ArrayList<>();
			final ArrayList<String> flowAltStrings = new ArrayList<>();
			final ArrayList<String> incJoinBegin = new ArrayList<>();
			final ArrayList<String> parJoinBegin = new ArrayList<>();
			final int nodeMinIndent = 9;
			final String ident = "ident";
			final String ident1 = ident + "1";
			final String ident2 = ident + "2";

			for (Node node : this.nodes)
			{
				final StringBuilder nodeBuilder = new StringBuilder();

				if (node instanceof Task)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append("(*")
							.append(COMMENTS_DASHES)
							.append(" Task of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent));

					final String incFlowIdentifier = node.incomingFlows().get(0).identifier() + "_finish (?" + ident1 + " of ID);\n";
					nodeBuilder.append(incFlowIdentifier);
					nodeBuilder.append(Utils.indent(nodeMinIndent));

					final String outFlowIdentifier = node.outgoingFlows().get(0).identifier() + "_begin (?" + ident2 + " of ID);\n";
					nodeBuilder.append(outFlowIdentifier);
					//nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append(this.getSchedulerString(
							Collections.singletonList(ident1),
							Collections.singletonList(ident2),
							SYNC_STORE,
							MERGE_STORE,
							PAR_STORE,
							nodeMinIndent
					));

					identSet.add(ident1);
					identSet.add(ident2);
				}
				else if (node instanceof XOrSplitGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append("(*")
							.append(COMMENTS_DASHES)
							.append(" XOrSplitGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append(node.firstIncomingFlow().identifier())
							.append("_finish (?")
							.append(ident1)
							.append(" of ID);\n")
							.append(Utils.indent(nodeMinIndent));

					identSet.add(ident1);
					boolean first = true;
					int counter = 2;
					nodeBuilder.append("alt\n");

					for (Flow flow : node.outgoingFlows())
					{
						final String currentIdent = ident + counter;

						if (first)
						{
							first = false;
						}
						else
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("[]\n")
							;
						}

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(flow.identifier())
								.append("_begin (?")
								.append(currentIdent)
								.append(" of ID);\n")
						;

						identSet.add(currentIdent);
						nodeBuilder.append(this.getSchedulerString(
								Collections.singletonList(ident1),
								Collections.singletonList(currentIdent),
								SYNC_STORE,
								MERGE_STORE,
								PAR_STORE,
								nodeMinIndent + 3
						));
						counter++;
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end alt\n")
					;
				}
				else if (node instanceof XOrJoinGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append("(*")
							.append(COMMENTS_DASHES)
							.append(" XOrJoinGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent));
					nodeBuilder.append("alt\n");

					boolean first = true;

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
						}
						else
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("[]\n")
							;
						}

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(flow.identifier())
								.append("_finish (?")
								.append(ident2)
								.append(" of ID)")
						;
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end alt;\n")
							.append(Utils.indent(nodeMinIndent))
							.append(node.firstOutgoingFlow().identifier())
							.append("_begin (?")
							.append(ident1)
							.append(" of ID);\n")
					;

					identSet.add(ident1);
					identSet.add(ident2);
					nodeBuilder.append(this.getSchedulerString(
							Collections.singletonList(ident2),
							Collections.singletonList(ident1),
							SYNC_STORE,
							MERGE_STORE,
							PAR_STORE,
							nodeMinIndent
					));
				}
				else if (node instanceof AndSplitGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("(*")
							.append(COMMENTS_DASHES)
							.append(" AndSplitGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append(node.firstIncomingFlow().identifier())
							.append("_finish (?")
							.append(ident1)
							.append(" of ID);\n")
							.append(Utils.indent(nodeMinIndent))
							.append("par\n")
					;

					boolean first = true;
					int counter = 2;
					final ArrayList<String> outIds = new ArrayList<>();

					for (Flow flow : node.outgoingFlows())
					{
						final String currentIdent = ident + counter;

						if (first)
						{
							first = false;
						}
						else
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("||\n")
							;
						}

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(flow.identifier())
								.append("_begin (?")
								.append(currentIdent)
								.append(" of ID)")
						;
						identSet.add(currentIdent);
						outIds.add(currentIdent);
						counter++;
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end par;\n")
							.append(this.getSchedulerString(
									Collections.singletonList(ident1),
									outIds,
									SYNC_STORE,
									MERGE_STORE,
									PAR_STORE,
									nodeMinIndent
							))
					;
				}
				else if (node instanceof AndJoinGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("(*")
							.append(COMMENTS_DASHES)
							.append(" AndJoinGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("alt\n");

					boolean first = true;

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
							identSet.add(ident);
						}
						else
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("[]\n")
							;
						}

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(flow.identifier())
								.append("_finish (?" + ident + " of ID);\n")
								.append(this.getSchedulerString(
										new ArrayList<>(),
										new ArrayList<>(),
										"insert (" + ident + ", syncstore)",
										MERGE_STORE,
										"insert (" + node.identifier() + ", parstore)",
										nodeMinIndent + 3
								))
						;
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end alt\n")
					;

					identSet.add(ident1);

					final StringBuilder parJoinBeginBuilder = new StringBuilder();

					//Parallel merge join TODO: Clean up
					parJoinBeginBuilder.append(Utils.indentLNT(5))
							.append(node.firstOutgoingFlow().identifier())
							.append("_begin (?")
							.append(ident1)
							.append(" of ID);\n")
							.append(Utils.indentLNT(5))
							.append("scheduler [")
					;

					final int minIndent = 26;
					final Pair<String, Integer> flowMsgsAndLineLength = this.getFlowMsgsAndLineLength(minIndent, minIndent);
					parJoinBeginBuilder.append(flowMsgsAndLineLength.getLeft())
							.append(", ");
					final ArrayList<String> synchroPoints = this.computeAddSynchroPoints(false);
					nbCharCurrentLine = flowMsgsAndLineLength.getRight() + 2;

					for (String synchroPoint : synchroPoints)
					{
						final String synchroPointWithComa = synchroPoint + ", ";

						if (synchroPointWithComa.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
						{
							parJoinBeginBuilder.append("\n").append(Utils.indent(minIndent));
							nbCharCurrentLine = minIndent + synchroPointWithComa.length();
						}
						else
						{
							nbCharCurrentLine += synchroPointWithComa.length();
						}

						parJoinBeginBuilder.append(synchroPointWithComa);
					}

					if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
					{
						parJoinBeginBuilder.append("\n").append(Utils.indent(minIndent));
					}

					parJoinBeginBuilder.append("MoveOn]\n")
							.append(Utils.indent(minIndent - 1))
							.append("(union ({")
							.append(ident1)
							.append("},\n")
							.append(Utils.indent(minIndent + 7))
							.append("remove_incf (bpmn, activeflows, mergeid)),\n")
							.append(Utils.indent(minIndent))
							.append("bpmn, remove_sync (bpmn, syncstore, mergeid),\n")
							.append(Utils.indent(minIndent))
							.append("mergestore, remove (mergeid, parstore))\n")
					;

					parJoinBegin.add(parJoinBeginBuilder.toString());
				}
				else if (node instanceof OrSplitGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("(*")
							.append(COMMENTS_DASHES)
							.append(" OrSplitGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append(node.firstIncomingFlow().identifier())
							.append("_finish (?")
							.append(ident1)
							.append(" of ID);\n")
							.append(Utils.indent(nodeMinIndent))
							.append("alt\n")
					;

					identSet.add(ident1);

					//We translate the inclusive split by enumerating all combinations in a alt/par
					final ArrayList<String> flowAlpha = new ArrayList<>();
					int counter = 2;

					for (Flow flow : node.outgoingFlows())
					{
						flowAlpha.add(flow.identifier() + "_begin");
						identSet.add(ident + counter);
						counter++;
					}

					final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(flowAlpha);
					final int nbCombinations = allCombinations.size();
					final ArrayList<String> outIds = new ArrayList<>();
					int nb = 1;
					int cter = 1;

					for (ArrayList<String> combination : allCombinations)
					{
						final int nbElemCombi = combination.size();
						int nb2 = 1;

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(node.identifier())
								.append("_")
								.append(cter)
								.append(";\n")
								.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
						;

						cter++;

						if (nbElemCombi > 1)
						{
							int combiCounter = nbCombinations;
							nodeBuilder.append("par\n");

							for (String element : combination)
							{
								final String currentIdent = ident + combiCounter;
								nodeBuilder.append(Utils.indent(nodeMinIndent))
										.append(Utils.indentLNT(2))
										.append(element)
										.append(" (?")
										.append(currentIdent)
										.append(" of ID)")
								;
								outIds.add(currentIdent);
								identSet.add(currentIdent);
								combiCounter--;
								nb2++;

								if (nb2 <= nbElemCombi)
								{
									nodeBuilder.append("\n")
											.append(Utils.indent(nodeMinIndent))
											.append(Utils.indentLNT(1))
											.append("||\n")
									;
								}
							}

							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append(Utils.indentLNT(1))
									.append("end par;\n")
									.append(this.getSchedulerString(
											Collections.singletonList(ident1),
											outIds,
											SYNC_STORE,
											MERGE_STORE,
											PAR_STORE,
											nodeMinIndent + 3
									))
							;
						}
						else
						{
							nodeBuilder.append(combination.iterator().next())
									.append(" (?")
									.append(ident)
									.append(" of ID);\n")
									.append(this.getSchedulerString(
											Collections.singletonList(ident1),
											Collections.singletonList(ident),
											SYNC_STORE,
											MERGE_STORE,
											PAR_STORE,
											nodeMinIndent + 3
									))
							;
							identSet.add(ident);
						}

						nb++;

						if (nb <= nbCombinations)
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("[]\n")
							;
						}
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end alt\n")
					;
				}
				else if (node instanceof OrJoinGateway)
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("(*")
							.append(COMMENTS_DASHES)
							.append(" OrJoinGateway of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("alt\n");
					boolean first = true;

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
							identSet.add(ident);
						}
						else
						{
							nodeBuilder.append("\n")
									.append(Utils.indent(nodeMinIndent))
									.append("[]\n")
							;
						}

						nodeBuilder.append(Utils.indent(nodeMinIndent))
								.append(Utils.indentLNT(1))
								.append(flow.identifier())
								.append("_finish (?")
								.append(ident)
								.append(" of ID);\n")
								.append(this.getSchedulerString(
										new ArrayList<>(),
										new ArrayList<>(),
										"insert (" + ident + ", syncstore)",
										"insert (" + node.identifier() + ", mergestore)",
										PAR_STORE,
										nodeMinIndent + 3
								))
						;
					}

					nodeBuilder.append("\n")
							.append(Utils.indent(nodeMinIndent))
							.append("end alt\n")
					;

					identSet.add(ident1);

					//Inclusive merge join TODO: Clean up
					final StringBuilder incJoinBeginBuilder = new StringBuilder();

					incJoinBeginBuilder.append(Utils.indentLNT(5))
							.append(node.firstOutgoingFlow().identifier())
							.append("_begin (?")
							.append(ident1)
							.append(" of ID);\n")
							.append(Utils.indentLNT(5))
							.append("scheduler [");

					final int minIndent = 26;
					final Pair<String, Integer> flowMsgsAndLineLength = this.getFlowMsgsAndLineLength(minIndent, minIndent);
					incJoinBeginBuilder.append(flowMsgsAndLineLength.getLeft());

					nbCharCurrentLine = minIndent + flowMsgsAndLineLength.getRight();

					final ArrayList<String> synchroPoints = this.computeAddSynchroPoints(false);

					incJoinBeginBuilder.append(", ");
					nbCharCurrentLine += 2;

					for (String synchroPoint : synchroPoints)
					{
						final String synchroPointWithComa = synchroPoint + ", ";

						if (nbCharCurrentLine + synchroPointWithComa.length() > MAX_CHAR_PER_LINE)
						{
							incJoinBeginBuilder.append("\n").append(Utils.indent(minIndent));
							nbCharCurrentLine = minIndent + synchroPointWithComa.length();
						}
						else
						{
							nbCharCurrentLine += synchroPointWithComa.length();
						}

						incJoinBeginBuilder.append(synchroPointWithComa);
					}

					if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
					{
						incJoinBeginBuilder.append("\n").append(Utils.indent(minIndent));
					}

					incJoinBeginBuilder.append("MoveOn]\n")
							.append(Utils.indent(minIndent - 1))
							.append("(union ({")
							.append(ident1)
							.append("},\n")
							.append(Utils.indent(minIndent + 7))
							.append("remove_incf (bpmn, activeflows, mergeid)),\n")
							.append(Utils.indent(minIndent))
							.append("bpmn, remove_sync (bpmn, syncstore, mergeid),\n")
							.append(Utils.indent(minIndent))
							.append("remove (mergeid, mergestore), parstore)\n");

					incJoinBegin.add(incJoinBeginBuilder.toString());
				}
				else
				{
					nodeBuilder.append(Utils.indent(nodeMinIndent))
							.append("(*")
							.append(COMMENTS_DASHES)
							.append(" ERROR: Unable to select element of ID \"")
							.append(node.identifier())
							.append("\" ")
							.append(COMMENTS_DASHES)
							.append("*)\n");
				}

				flowAltStrings.add(nodeBuilder.toString());
			}

			//Generate var
			identSet.add(ident1); //For initial/final
			stringBuilder.append(Utils.indentLNT(1))
					.append("var");
			final int minIndent;
			final boolean newLineRequiredForVars = identSet.size() > MAX_VARS_PER_LINE - 1; //-1 because we add ``mergeid'' at the end

			if (newLineRequiredForVars)
			{
				stringBuilder.append("\n").append(Utils.indentLNT(2));
				nbCharCurrentLine = minIndent = 6;
			}
			else
			{
				stringBuilder.append(" ");
				nbCharCurrentLine = minIndent = 7;
			}

			for (String currentIdent : new HashSet<>(identSet)) //TODO Intéret ? Randomiser ?
			{
				final String identAndComa = currentIdent + ", ";

				if (identAndComa.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent));
					nbCharCurrentLine = minIndent + identAndComa.length();
				}
				else
				{
					nbCharCurrentLine += identAndComa.length();
				}

				stringBuilder.append(identAndComa);
			}

			final int nbCharToConsider = newLineRequiredForVars ? 11 : 14;

			if (nbCharCurrentLine + nbCharToConsider > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(minIndent));
			}

			stringBuilder.append("mergeid: ID");

			if (newLineRequiredForVars)
			{
				stringBuilder.append("\n").append(Utils.indentLNT(1));
			}
			else
			{
				stringBuilder.append(" ");
			}

			stringBuilder.append("in\n");
			stringBuilder.append(Utils.indentLNT(2));
			stringBuilder.append("alt\n");

			//Handle initial node
			stringBuilder.append(Utils.indent(nodeMinIndent))
					.append("(*")
					.append(COMMENTS_DASHES)
					.append(" Initial node ")
					.append(COMMENTS_DASHES)
					.append("*)\n");
			stringBuilder.append(Utils.indent(nodeMinIndent))
					.append(this.initial.firstOutgoingFlow().identifier())
					.append("_begin (?")
					.append(ident1)
					.append(" of ID);\n")
			;
			stringBuilder.append(this.getSchedulerString(
					new ArrayList<>(),
					Collections.singletonList(ident1),
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE,
					9
			));

			for (String flow : flowAltStrings)
			{
				stringBuilder.append("\n")
						.append(Utils.indentLNT(2))
						.append("[]\n")
						.append(flow);
			}

			stringBuilder.append("\n")
					.append(Utils.indentLNT(2))
					.append("[]\n")
			;

			//Handle final node
			stringBuilder.append(Utils.indent(nodeMinIndent))
					.append("(*")
					.append(COMMENTS_DASHES)
					.append(" Final node ")
					.append(COMMENTS_DASHES)
					.append("*)\n");

			stringBuilder.append(Utils.indent(nodeMinIndent))
					.append(this.initial.firstOutgoingFlow().identifier())
					.append("_finish (?")
					.append(ident1)
					.append(" of ID);\n");

			stringBuilder.append(this.getSchedulerString(
					Collections.singletonList(ident1),
					new ArrayList<>(),
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE,
					9
			));

			stringBuilder.append("\n")
					.append(Utils.indentLNT(2))
					.append("[]\n")
					.append(Utils.indentLNT(3))
					.append("mergeid := any ID where member (mergeid, mergestore);\n")
					.append(Utils.indentLNT(3))
					.append("if is_merge_possible_v2 (bpmn, activeflows, mergeid) and\n")
					.append(Utils.indentLNT(3))
					.append(Utils.indent(3))
					.append("is_sync_done (bpmn, activeflows, syncstore, mergeid) then\n")
					.append(Utils.indentLNT(4))
					.append("MoveOn (mergeid);\n")
			;

			if (incJoinBegin.isEmpty())
			{
				stringBuilder.append(this.getSchedulerString(
					new ArrayList<>(),
					new ArrayList<>(),
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE,
					12
				));
			}
			else
			{
				int i = 1;
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("alt\n");

				for (String incJoin : incJoinBegin)
				{
					stringBuilder.append(incJoin);

					if (i < incJoinBegin.size())
					{
						stringBuilder.append("\n");
						stringBuilder.append(Utils.indentLNT(4));
						stringBuilder.append("[]\n");
					}

					i++;
				}

				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("end alt");
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("else\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("scheduler [");

			final Pair<String, Integer> flowMsgsAndLineLength = this.getFlowMsgsAndLineLength(23, 23);

			stringBuilder.append(flowMsgsAndLineLength.getLeft());
			ArrayList<String> synchroPoints = this.computeAddSynchroPoints(false);
			nbCharCurrentLine = flowMsgsAndLineLength.getRight() + 2;
			stringBuilder.append(", ");

			for (String synchroPoint : synchroPoints)
			{
				final String synchroPointWithComa = synchroPoint + ", ";

				if (synchroPointWithComa.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(23));
					nbCharCurrentLine = 23 + synchroPointWithComa.length();
				}
				else
				{
					nbCharCurrentLine += synchroPointWithComa.length();
				}

				stringBuilder.append(synchroPointWithComa);
			}

			if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(23));
			}

			stringBuilder.append("MoveOn]\n")
					.append(Utils.indent(22))
					.append("(activeflows, bpmn, syncstore, mergestore, parstore)\n")
					.append(Utils.indentLNT(3))
					.append("end if\n")
					.append(Utils.indentLNT(2))
					.append("[]\n")
					.append(Utils.indentLNT(3))
					.append("mergeid := any ID where member (mergeid, parstore);\n")
					.append(Utils.indentLNT(3))
					.append("if is_merge_possible_par (bpmn, syncstore, mergeid) then\n")
			;

			if (parJoinBegin.isEmpty())
			{
				stringBuilder.append(this.getSchedulerString(
						new ArrayList<>(),
						new ArrayList<>(),
						SYNC_STORE,
						MERGE_STORE,
						PAR_STORE,
						12
				));
			}
			else
			{
				int i = 1;
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("alt\n");

				for (String parJoin : parJoinBegin)
				{
					stringBuilder.append(parJoin);

					if (i < parJoinBegin.size())
					{
						stringBuilder.append("\n");
						stringBuilder.append(Utils.indentLNT(4));
						stringBuilder.append("[]\n");
					}

					i++;
				}

				stringBuilder.append("\n");
				stringBuilder.append(Utils.indentLNT(4));
				stringBuilder.append("end alt");
			}

			stringBuilder.append("\n");
			stringBuilder.append(Utils.indentLNT(3));
			stringBuilder.append("else\n");
			stringBuilder.append(Utils.indentLNT(4));
			stringBuilder.append("scheduler [");

			final int minIndent2 = 23;
			final Pair<String, Integer> flowMsgsAndLineLength2 = this.getFlowMsgsAndLineLength(minIndent2, minIndent2);
			stringBuilder.append(flowMsgsAndLineLength2.getLeft())
					.append(", ");
			final ArrayList<String> synchroPoints2 = this.computeAddSynchroPoints(false);
			nbCharCurrentLine = flowMsgsAndLineLength2.getRight() + 2;

			for (String synchroPoint : synchroPoints2)
			{
				final String synchroPointWithComa = synchroPoint + ", ";

				if (synchroPointWithComa.length() + nbCharCurrentLine > MAX_CHAR_PER_LINE)
				{
					stringBuilder.append("\n").append(Utils.indent(minIndent2));
					nbCharCurrentLine = minIndent2 + synchroPointWithComa.length();
				}
				else
				{
					nbCharCurrentLine += synchroPointWithComa.length();
				}

				stringBuilder.append(synchroPointWithComa);
			}

			if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
			{
				stringBuilder.append("\n").append(Utils.indent(minIndent2));
			}

			stringBuilder.append("MoveOn]\n")
					.append(Utils.indent(minIndent2 - 1))
					.append("(activeflows, bpmn, syncstore, mergestore, parstore)\n")
					.append(Utils.indentLNT(3))
					.append("end if\n")
					.append(Utils.indentLNT(2))
					.append("end alt\n")
					.append(Utils.indentLNT(1))
					.append("end var\n")
					.append("end process\n\n")
					.append(STANDARD_LNT_SEPARATOR)
			;
		}

		String getSchedulerString(final List<String> incIds,
								  final List<String> outIds,
								  final String syncString,
								  final String mergeStoreString,
								  final String parStoreString,
								  final int minIndent)
		{
			final StringBuilder schedulerStringBuilder = new StringBuilder();
			schedulerStringBuilder.append(Utils.indent(minIndent));
			final String schedulerString = "scheduler [...] (";
			final String unionString = "union (";
			schedulerStringBuilder.append(schedulerString)
					.append(unionString)
					.append("{");

			int i = 1;
			int nbCharCurrentLine = schedulerStringBuilder.length();

			for (String outId : outIds)
			{
				if (i == 1)
				{
					nbCharCurrentLine += outId.length();
				}
				else
				{
					if (nbCharCurrentLine + outId.length() > MAX_CHAR_PER_LINE)
					{
						final int indentation = schedulerString.length() + unionString.length() + minIndent + 1;
						schedulerStringBuilder.append("\n")
								.append(Utils.indent(indentation));
						nbCharCurrentLine = indentation + outId.length();
					}
					else
					{
						nbCharCurrentLine += outId.length();
					}
				}

				schedulerStringBuilder.append(outId);

				if (i < outIds.size())
				{
					schedulerStringBuilder.append(", ");
					nbCharCurrentLine += 2;
				}

				i++;
			}

			schedulerStringBuilder.append("}, ");
			nbCharCurrentLine += 3;

			final String firstIncId = incIds.isEmpty() ? "" : incIds.get(0);
			final String removeIdsString = "remove_ids_from_set ({" + firstIncId;

			if (nbCharCurrentLine + removeIdsString.length() > MAX_CHAR_PER_LINE)
			{
				final int indentation = minIndent + schedulerString.length() + unionString.length();
				schedulerStringBuilder.append("\n").append(Utils.indent(indentation));
				nbCharCurrentLine = indentation + removeIdsString.length();
			}
			else
			{
				nbCharCurrentLine += removeIdsString.length();
			}

			schedulerStringBuilder.append(removeIdsString);
			final int removeIdsParamsMinIndent = nbCharCurrentLine - firstIncId.length() - 1;

			for (int j = 1; j < incIds.size(); j++)
			{
				final String incId = incIds.get(j);
				schedulerStringBuilder.append(", ");
				nbCharCurrentLine += 2;

				if (nbCharCurrentLine + incId.length() > MAX_CHAR_PER_LINE)
				{
					schedulerStringBuilder.append("\n").append(Utils.indent(removeIdsParamsMinIndent));
					nbCharCurrentLine = removeIdsParamsMinIndent + incId.length();
				}
				else
				{
					nbCharCurrentLine += incId.length();
				}

				schedulerStringBuilder.append(incId);
			}

			schedulerStringBuilder.append("}, ");
			nbCharCurrentLine += 3;

			final String activeFlows = "activeflows)), ";

			if (nbCharCurrentLine + activeFlows.length() > MAX_CHAR_PER_LINE)
			{
				schedulerStringBuilder.append("\n").append(Utils.indent(removeIdsParamsMinIndent));
				nbCharCurrentLine = removeIdsParamsMinIndent + activeFlows.length();
			}
			else
			{
				nbCharCurrentLine += activeFlows.length();
			}

			schedulerStringBuilder.append(activeFlows);

			if (nbCharCurrentLine + 6 > MAX_CHAR_PER_LINE)
			{
				schedulerStringBuilder.append("\n").append(Utils.indent(minIndent + schedulerString.length()));
				nbCharCurrentLine = minIndent + schedulerString.length() + 6;
			}
			else
			{
				nbCharCurrentLine += 6;
			}

			schedulerStringBuilder.append("bpmn, ");

			if (nbCharCurrentLine + syncString.length() + 2 > MAX_CHAR_PER_LINE)
			{
				schedulerStringBuilder.append("\n").append(Utils.indent(minIndent + schedulerString.length()));
				nbCharCurrentLine = minIndent + schedulerString.length() + syncString.length() + 2;
			}
			else
			{
				nbCharCurrentLine += syncString.length() + 2;
			}

			schedulerStringBuilder.append(syncString)
					.append(", ");

			if (nbCharCurrentLine + mergeStoreString.length() + 2 > MAX_CHAR_PER_LINE)
			{
				schedulerStringBuilder.append("\n").append(Utils.indent(minIndent + schedulerString.length()));
				nbCharCurrentLine = minIndent + schedulerString.length() + mergeStoreString.length() + 2;
			}
			else
			{
				nbCharCurrentLine += mergeStoreString.length() + 2;
			}

			schedulerStringBuilder.append(mergeStoreString)
					.append(", ");

			if (nbCharCurrentLine + parStoreString.length() + 1 > MAX_CHAR_PER_LINE)
			{
				schedulerStringBuilder.append("\n").append(Utils.indent(minIndent + schedulerString.length()));
			}

			schedulerStringBuilder.append(parStoreString)
					.append(")");

			return schedulerStringBuilder.toString();
		}

		/**
		 * Generates file with process element ids
		 */
		void generateIdFile()
		{
			final String fileName = "id.lnt";
			final StringBuilder idFileBuilder = new StringBuilder();

			//Generates an ID type for all identifiers
			idFileBuilder.append("module id with get, <, == is\n\n")
					.append("(* Data type for identifiers, useful for scheduling purposes *)\n")
					.append("type ID is\n")
					.append(this.name);

			for (Node node : this.nodes)
			{
				idFileBuilder.append(",\n")
						.append(node.identifier());
			}

			idFileBuilder.append(",\n")
					.append(this.initial.identifier());

			for (Node node : this.finals)
			{
				idFileBuilder.append(",\n")
						.append(node.identifier());
			}

			for (Flow flow : this.flows)
			{
				idFileBuilder.append(", \n")
						.append(flow.identifier());
			}

			idFileBuilder.append(", DummyId\n")
					.append("with ==, !=\n")
					.append("end type\n\n")
					.append("end module\n");

			final File file = new File(outputFolder + File.separator + fileName);
			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(file);
				printWriter.write(idFileBuilder.toString());
				printWriter.flush();
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		//Generates an LNT module and process for a BPMN 2.0 process
		void genLNT()
		{
			if (!isBalanced)
			{
				this.generateIdFile();
			}

			final String fileName = this.name + LNT_SUFFIX;
			final File file = new File(outputFolder + File.separator + fileName);
			final StringBuilder lntBuilder = new StringBuilder();

			lntBuilder.append("module ")
					.append(this.name)
					.append(isBalanced ? "" : "(bpmntypes)")
					.append(" with get, <, == is\n\n")
					.append(STANDARD_LNT_SEPARATOR);

			if (isBalanced)
			{
				//Generates an ID type for all flow identifiers
				lntBuilder.append("(* Data type for flow identifiers, useful for scheduling purposes *)\n")
						.append("type ID is\n")
						.append(Utils.indentLNT(1));

				int counter = this.flows.size();

				for (Flow f : this.flows)
				{
					lntBuilder.append(f.identifier());
					counter--;

					if (counter > 0)
					{
						lntBuilder.append(",\n");
						lntBuilder.append(Utils.indentLNT(1));
					}
				}

				lntBuilder.append("\n")
						.append(Utils.indentLNT(1))
						.append("with ==, !=\n")
						.append("end type\n\n")
						.append(STANDARD_LNT_SEPARATOR);
			}

			if (this.initial != null)
			{
				this.initial.writeLnt(lntBuilder, 0);
			}

			//Generates one process for final events and events, this is enough because generic processes
			if (!this.finals.isEmpty())
			{
				this.finals.get(0).writeLnt(lntBuilder, 0);
			}

			if (!this.flows.isEmpty())
			{
				this.flows.get(0).writeLnt(lntBuilder); //TODO: ConditionalFlow?
			}

			//Generates LNT processes for all other nodes
			final ArrayList<String> specialNodes = new ArrayList<>(); //We keep track of nodes that need to be translated only once

			for (Node n : this.nodes)
			{
				if (n instanceof Interaction
					|| n instanceof MessageSending
					|| n instanceof MessageReception
					|| n instanceof Task)
				{
					if (!specialNodes.contains(n.getClass().getName()))
					{
						if (n instanceof Task)
						{
							//a task is identified with its number of incoming and outgoing flows
							final StringBuilder classNameBuilder = new StringBuilder(n.getClass().getName());
							classNameBuilder.append("_")
									.append(n.incomingFlows().size())
									.append("_")
									.append(n.outgoingFlows().size());

							if (!specialNodes.contains(classNameBuilder.toString()))
							{
								specialNodes.add(classNameBuilder.toString());
								n.writeLnt(lntBuilder, 0);
							}
						}
						else
						{
							specialNodes.add(n.getClass().getName());
							n.writeLnt(lntBuilder, 0);
						}
					}
				}
				else
				{
					n.writeLnt(lntBuilder, 0);
				}
			}

			/*
				Note: up to here, translation patterns are independent of the actual tasks, comm, etc.
				The actual names will be used only in the MAIN process when computing the process alphabet
				and instantiating processes.
			 */

			if (!isBalanced)
			{
				//Scheduler process generation
				this.generateScheduler(lntBuilder);

				//Generate process
				this.processDump(lntBuilder);
			}

			lntBuilder.append("process MAIN ");
			final ArrayList<String> alpha = this.alpha();
			final boolean lineJumped = dumpAlphabet(alpha, lntBuilder, true);
			lntBuilder.append(lineJumped ? "\n" : " ");
			lntBuilder.append("is\n");

			//Computes additional synchros for or splits/joins
			final ArrayList<String> synchroPoints = this.computeAddSynchroPoints(isBalanced);
			final int nbSync = synchroPoints.size();
			lntBuilder.append(Utils.indentLNT(1));
			lntBuilder.append("hide\n");
			lntBuilder.append(Utils.indentLNT(2));
			lntBuilder.append("begin, finish");
			final int nbFlows = this.flows.size();
			final int indentBase;

			int nbCharCurrentLine = 21;

			if (isBalanced)
			{
				indentBase = 0;

				if (nbFlows > 0)
				{
					lntBuilder.append(", ");
					nbCharCurrentLine += 2;
					int cter = 1;

					for (Flow f : this.flows)
					{
						final String beginIdentifier = f.identifier() + "_begin, ";
						final String finishIdentifier = f.identifier() + "_finish";

						if (nbCharCurrentLine + beginIdentifier.length() > MAX_CHAR_PER_LINE)
						{
							lntBuilder.append("\n").append(Utils.indentLNT(2));
							nbCharCurrentLine = 6 + beginIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += beginIdentifier.length();
						}

						lntBuilder.append(beginIdentifier);

						if (nbCharCurrentLine + finishIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
						{
							lntBuilder.append("\n").append(Utils.indentLNT(2));
							nbCharCurrentLine = 6 + finishIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += finishIdentifier.length();
						}

						lntBuilder.append(finishIdentifier);
						cter++;

						if (cter <= nbFlows)
						{
							lntBuilder.append(", ");
							nbCharCurrentLine += 2;
							//we hide additional synchros for or splits/joins as well
						}
					}

					int nb = 0;

					if (nbSync > 0)
					{
						lntBuilder.append(", ");
						nbCharCurrentLine += 2;

						for (String synchroPoint : synchroPoints)
						{
							if (nbCharCurrentLine + synchroPoint.length() + 1 > MAX_CHAR_PER_LINE)
							{
								lntBuilder.append("\n").append(Utils.indentLNT(2));
								nbCharCurrentLine = 6 + synchroPoint.length();
							}
							else
							{
								nbCharCurrentLine += synchroPoint.length();
							}

							lntBuilder.append(synchroPoint);
							nb++;

							if (nb < nbSync)
							{
								lntBuilder.append(", ");
								nbCharCurrentLine += 2;
							}
						}
					}
				}

				lntBuilder.append(": any\n")
						.append(Utils.indentLNT(1))
						.append("in\n")
						.append(Utils.indentLNT(2))
						.append("par\n")
						.append(Utils.indentLNT(3));
				//Synchronizations on all begin/finish flows
				nbCharCurrentLine = 9;

				if (nbFlows > 0)
				{
					int cter = 1;

					for (Flow f : this.flows)
					{
						final String beginIdentifier = f.identifier() + "_begin, ";
						final String finishIdentifier = f.identifier() + "_finish";

						if (nbCharCurrentLine + beginIdentifier.length() > MAX_CHAR_PER_LINE)
						{
							lntBuilder.append("\n").append(Utils.indentLNT(3));
							nbCharCurrentLine = 9 + beginIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += beginIdentifier.length();
						}

						lntBuilder.append(beginIdentifier);

						if (nbCharCurrentLine + finishIdentifier.length() + 1 > MAX_CHAR_PER_LINE)
						{
							lntBuilder.append("\n").append(Utils.indentLNT(3));
							nbCharCurrentLine = 9 + finishIdentifier.length();
						}
						else
						{
							nbCharCurrentLine += finishIdentifier.length();
						}

						lntBuilder.append(finishIdentifier);
						cter++;

						if (cter <= nbFlows)
						{
							lntBuilder.append(", ");
							nbCharCurrentLine += 2;
						}
					}
				}
			}
			else
			{
				indentBase = 3;

				if (nbFlows > 0)
				{
					lntBuilder.append(", ");
					final Pair<String, Integer> flowMsgsAndLineLength = this.getFlowMsgsAndLineLength(6, 15);
					lntBuilder.append(flowMsgsAndLineLength.getLeft());
					lntBuilder.append(", ");
					nbCharCurrentLine = flowMsgsAndLineLength.getRight();
					//We hide additional synchros for or splits/joins as well

					if (nbSync > 0)
					{
						nbCharCurrentLine += 2;

						for (String synchroPoint : synchroPoints)
						{
							final String synchroPointWithComa = synchroPoint + ", ";

							if (nbCharCurrentLine + synchroPointWithComa.length() > MAX_CHAR_PER_LINE)
							{
								lntBuilder.append("\n").append(Utils.indentLNT(2));
								nbCharCurrentLine = synchroPointWithComa.length() + 6;
							}
							else
							{
								nbCharCurrentLine += synchroPointWithComa.length();
							}

							lntBuilder.append(synchroPointWithComa);
						}
					}
				}

				lntBuilder.append("MoveOn: any\n");
				lntBuilder.append(Utils.indentLNT(1));
				lntBuilder.append("in\n");
				lntBuilder.append(Utils.indentLNT(2));
				lntBuilder.append("par\n");
				lntBuilder.append(Utils.indentLNT(3));
				lntBuilder.append("MoveOn, ");

				//We start with the scheduler
				//Synchronization on all begin/finish flows
				nbCharCurrentLine = 17;

				if (nbFlows > 0)
				{
					final Pair<String, Integer> flowMsgsAndLineLength = this.getFlowMsgsAndLineLength(9, 17);
					lntBuilder.append(flowMsgsAndLineLength.getLeft());
					nbCharCurrentLine = flowMsgsAndLineLength.getRight();
				}

				for (String synchroPoint : synchroPoints)
				{
					lntBuilder.append(", ");
					nbCharCurrentLine += 2;

					if (nbCharCurrentLine + synchroPoint.length() + 2 > MAX_CHAR_PER_LINE)
					{
						lntBuilder.append("\n").append(Utils.indentLNT(3));
						nbCharCurrentLine = synchroPoint.length() + 9;
					}
					else
					{
						nbCharCurrentLine += synchroPoint.length();
					}

					lntBuilder.append(synchroPoint);
				}

				lntBuilder.append("\n")
						.append(Utils.indentLNT(2))
						.append("in\n")
						.append(Utils.indentLNT(3))
						.append("(* we first generate the scheduler, necessary for keeping\n")
						.append(Utils.indentLNT(3))
						.append("track of tokens, and triggering inclusive merge gateways *)\n\n")
						.append(Utils.indentLNT(3))
						.append("scheduler [...] (nil, p1 (), nil, nil, nil)\n")
						.append(Utils.indentLNT(2))
						.append("||\n")
						.append(Utils.indentLNT(3))
						.append("par (* synchronizations on all begin/finish flow messages *)\n");

				//Synchronizations on all begin/finish flows
				if (nbFlows > 0)
				{
					lntBuilder.append(Utils.indentLNT(4));
					lntBuilder.append(this.getFlowMsgsAndLineLength(12, 12).getLeft());
				}
			}

			lntBuilder.append("\n");
			lntBuilder.append(Utils.indentLNT(2));
			lntBuilder.append(Utils.indent(indentBase));
			lntBuilder.append("in\n");
			lntBuilder.append(Utils.indentLNT(3));
			lntBuilder.append(Utils.indent(indentBase));

			//Interleaving of all flow processes
			lntBuilder.append("par (* we then generate interleaving of all flow processes *)\n");
			int cter = 1;

			for (Flow f : this.flows)
			{
				nbCharCurrentLine = 18 + indentBase;
				//TODO: take conditional flows into account
				lntBuilder.append(Utils.indentLNT(4));
				lntBuilder.append(Utils.indent(indentBase));
				lntBuilder.append("flow [");

				final String beginIdentifier = f.identifier() + "_begin, ";
				final String finishIdentifier = f.identifier() + "_finish]";

				lntBuilder.append(beginIdentifier);
				nbCharCurrentLine += beginIdentifier.length();

				if (nbCharCurrentLine + finishIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					lntBuilder.append("\n").append(Utils.indent(18 + indentBase));
					nbCharCurrentLine = 18 + indentBase + finishIdentifier.length();
				}
				else
				{
					nbCharCurrentLine += finishIdentifier.length();
				}

				lntBuilder.append(finishIdentifier);

				final String paramIdentifier = " (" + f.identifier() + ")";

				if (nbCharCurrentLine + paramIdentifier.length() > MAX_CHAR_PER_LINE)
				{
					lntBuilder.append("\n").append(Utils.indent(16 + indentBase));
				}

				lntBuilder.append(paramIdentifier);
				cter++;

				if (cter <= nbFlows)
				{
					lntBuilder.append("\n");
					lntBuilder.append(Utils.indentLNT(3));
					lntBuilder.append(Utils.indent(indentBase));
					lntBuilder.append("||\n");
				}
			}

			lntBuilder.append("\n");
			lntBuilder.append(Utils.indentLNT(3));
			lntBuilder.append(Utils.indent(indentBase));
			lntBuilder.append("end par\n");
			lntBuilder.append(Utils.indentLNT(2));
			lntBuilder.append(Utils.indent(indentBase));
			lntBuilder.append("||\n");

			//Interleaving of all node processes
			lntBuilder.append(Utils.indentLNT(3));
			lntBuilder.append(Utils.indent(indentBase));
			lntBuilder.append("par (* we finally generate interleaving of all node processes *)\n");
			lntBuilder.append(Utils.indentLNT(4));
			lntBuilder.append(Utils.indent(indentBase));

			//Process instantiation for initial node
			final String beginIdentifier = this.initial.outgoingFlows().get(0).identifier() + "_begin]";
			lntBuilder.append("init [begin, "); //We assume a single output flow

			nbCharCurrentLine = 25 + indentBase;

			if (nbCharCurrentLine + beginIdentifier.length() > MAX_CHAR_PER_LINE)
			{
				lntBuilder.append("\n").append(Utils.indent(18 + indentBase));
			}

			lntBuilder.append(beginIdentifier);
			lntBuilder.append("\n");
			lntBuilder.append(Utils.indentLNT(3));
			lntBuilder.append(Utils.indent(indentBase));
			lntBuilder.append("||\n");

			cter = 1;

			//Processes instantiations for final nodes
			for (Node n : this.finals)
			{
				lntBuilder.append(Utils.indentLNT(4));
				lntBuilder.append(Utils.indent(indentBase));

				final String finalNodeWithIdentifier = "final [" + n.incomingFlows().get(0).identifier() + "_finish, ";
				lntBuilder.append(finalNodeWithIdentifier);
				nbCharCurrentLine = finalNodeWithIdentifier.length() + 12 + indentBase;

				if (nbCharCurrentLine + 7 > MAX_CHAR_PER_LINE)
				{
					lntBuilder.append("\n").append(Utils.indent(24));
					lntBuilder.append(Utils.indent(indentBase));
				}

				lntBuilder.append("finish]"); //We assume a single incoming flow
				cter++;

				if (cter <= nbFlows)
				{
					lntBuilder.append("\n");
					lntBuilder.append(Utils.indentLNT(3));
					lntBuilder.append(Utils.indent(indentBase));
					lntBuilder.append("||\n");
				}
			}

			//Processes instantiations for all other nodes
			final int nbNodes = this.nodes.size();
			cter = 1;

			for (Node n : this.nodes)
			{
				lntBuilder.append(Utils.indentLNT(4));
				lntBuilder.append(Utils.indent(indentBase));
				n.writeMainLnt(lntBuilder, 12 + indentBase);
				cter++;

				if (cter <= nbNodes)
				{
					lntBuilder.append("\n");
					lntBuilder.append(Utils.indentLNT(3));
					lntBuilder.append(Utils.indent(indentBase));
					lntBuilder.append("||\n");
				}
			}

			lntBuilder.append("\n")
					.append(Utils.indentLNT(3))
					.append(Utils.indent(indentBase))
					.append("end par\n")
					.append(Utils.indentLNT(2))
					.append(Utils.indent(indentBase))
					.append("end par\n")
					.append(isBalanced ? "" : Utils.indentLNT(2))
					.append(isBalanced ? "" : "end par\n")
					.append(Utils.indentLNT(1))
					.append("end hide\n")
					.append("end process\n\n")
					.append(STANDARD_LNT_SEPARATOR)
					.append("end module\n")
			;

			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(file);
				printWriter.write(lntBuilder.toString());
				printWriter.flush();
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		/**
		 * Generates an SVL file
		 */
		void genSVL()
		{
			final String fileName = this.name + ".svl";
			final StringBuilder svlCommandBuilder = new StringBuilder();

			svlCommandBuilder.append("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n")
					.append(isBalanced ? "% CAESAR_OPTIONS=\"-more cat\"\n\n" : "% CAESAR_OPTIONS=\"-more cat -gc\"\n\n")
					.append("% DEFAULT_PROCESS_FILE=\"")
					.append(this.name)
					.append(".lnt\"\n\n")
					.append("\"")
					.append(this.name)
					.append("_raw.bcg\" = generation of \"MAIN");

			final ArrayList<String> alpha = this.alpha();
			dumpAlphabet(alpha, svlCommandBuilder, false);

			svlCommandBuilder.append("\";\n\n")
					.append("\"")
					.append(this.name)
					.append(".bcg\" = branching reduction of \"")
					.append(this.name)
					.append("_raw.bcg\";\n\n");

			final File svlFile = new File(outputFolder + File.separator + fileName);

			//System.out.println("Absolute path: " + svlFile.getAbsolutePath());
			//System.out.println("Working Directory = " + System.getProperty("user.dir"));

			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(svlFile);
				printWriter.write(svlCommandBuilder.toString());
				printWriter.flush();
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			if (!svlFile.setExecutable(true))
			{
				throw new IllegalStateException("Unable to make the SVL script executable. Please check your rights" +
						" on the current working directory.");
			}
		}

		/**
		 * This method takes as input a file.pif and generates a PIF Python object
		 *
		 * @param filename the pif filename
		 */
		void buildProcessFromFile(final String filename)
		{
			//Open XML document specified in the filename
			final File file = new File(filename);
			final fr.inria.convecs.optimus.pif.Process process;

			try
			{
				final JAXBContext jaxbContext = JAXBContext.newInstance(fr.inria.convecs.optimus.pif.Process.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				process = (fr.inria.convecs.optimus.pif.Process) jaxbUnmarshaller.unmarshal(file);
			}
			catch (JAXBException e)
			{
				System.out.println("An error occured while parsing xml document \"" + filename + "\".");
				System.out.println("Unrecognized element, the message was \"" + e.getMessage() + "\".");
				throw new RuntimeException(e);
			}

			this.name = process.getName();

			//We first create all nodes without incoming/outgoing flows
			for (WorkflowNode n : process.getBehaviour().getNodes())
			{
				//Initial and final events
				if (n instanceof fr.inria.convecs.optimus.pif.InitialEvent)
				{
					this.initial = new InitialEvent(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					);
				}
				if (n instanceof fr.inria.convecs.optimus.pif.EndEvent)
				{
					this.finals.add(new EndEvent(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>())
					);
				}

				//Tasks / Emissions / Receptions / Interactions
				if (n instanceof fr.inria.convecs.optimus.pif.Task)
				{
					this.nodes.add(new Task(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>())
					);
				}
				if (n instanceof fr.inria.convecs.optimus.pif.MessageSending
						&& isBalanced)
				{
					this.nodes.add(new MessageSending(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>(),
							((fr.inria.convecs.optimus.pif.MessageSending) n).getMessage().getId()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.MessageReception
						&& isBalanced)
				{
					this.nodes.add(new MessageReception(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>(),
							((fr.inria.convecs.optimus.pif.MessageReception) n).getMessage().getId()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.Interaction
						&& isBalanced)
				{
					final ArrayList<String> receivingPeers = new ArrayList<>();

					for (JAXBElement<Object> JAXBObject : ((fr.inria.convecs.optimus.pif.Interaction) n).getReceivingPeers())
					{
						final Peer peer = (Peer) JAXBObject.getValue();
						receivingPeers.add(peer.getId());
					}

					this.nodes.add(new Interaction(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>(),
							((fr.inria.convecs.optimus.pif.Interaction) n).getMessage().getId(),
							((fr.inria.convecs.optimus.pif.Interaction) n).getInitiatingPeer().getId(),
							receivingPeers
					));
				}

				//Split gateways
				if (n instanceof fr.inria.convecs.optimus.pif.AndSplitGateway)
				{
					this.nodes.add(new AndSplitGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.OrSplitGateway)
				{
					this.nodes.add(new OrSplitGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.XOrSplitGateway)
				{
					this.nodes.add(new XOrSplitGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}

				//Join gateways
				if (n instanceof fr.inria.convecs.optimus.pif.AndJoinGateway)
				{
					this.nodes.add(new AndJoinGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.OrJoinGateway)
				{
					this.nodes.add(new OrJoinGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}
				if (n instanceof fr.inria.convecs.optimus.pif.XOrJoinGateway)
				{
					this.nodes.add(new XOrJoinGateway(
							n.getId(),
							new ArrayList<>(),
							new ArrayList<>()
					));
				}
			}

			//Creation of flow objects
			for (SequenceFlow sequenceFlow : process.getBehaviour().getSequenceFlows())
			{
				final Flow flow = new Flow(
						sequenceFlow.getId(),
						this.getNode(sequenceFlow.getSource().getId()),
						this.getNode(sequenceFlow.getTarget().getId())
				);
				this.flows.add(flow);
				this.addFlow(flow);
			}
		}
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName)
	{
		return this.generate(pifFileName, true, true, !isBalanced);
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																final boolean generateLTS)
	{
		return this.generate(pifFileName, generateLTS, true, !isBalanced);
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName    is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug          is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																final boolean generateLTS,
																final boolean smartReduction,
																final boolean debug)
	{
		final Process process = new Process();
		//Load PIF model
		process.buildProcessFromFile(pifFileName);
		final String pifModelName = process.name();
		//Pre-processing: compute correspondences between or splits/joins
		process.reachableOrJoin();

		if (!isBalanced)
		{
			//Check for cycles in process involving inclusive gateway
			final boolean cycleExists = process.checkInclusiveCycle();

			if (cycleExists)
			{
				return Triple.of(ReturnCodes.TERMINATION_UNBALANCED_INCLUSIVE_CYCLE, pifModelName, process.alpha());
			}
		}

		//Generate the LNT code for the model
		process.genLNT();

		if (generateLTS)
		{
			//Compute the LTS from the LNT code using SVL, possibly with a smart reduction
			process.genSVL();

			final CommandManager commandManager = new CommandManager("svl", new File(outputFolder), pifModelName);

			try
			{
				commandManager.execute();
			}
			catch (IOException | InterruptedException e)
			{
				return Triple.of(ReturnCodes.TERMINATION_ERROR, pifModelName, process.alpha());
			}
		}

		return Triple.of(ReturnCodes.TERMINATION_OK, pifModelName, process.alpha()); //TODO use return value from SVL call
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> load(final String pifFileName)
	{
		return this.load(pifFileName, true, true, false);
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> load(final String pifFileName,
															final boolean generateLTS)
	{
		return this.load(pifFileName, generateLTS, true, false);
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName    is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug          is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	@Override
	public Triple<Integer, String, Collection<String>> load(final String pifFileName,
															final boolean generateLTS,
															final boolean smartReduction,
															final boolean debug)
	{
		final Process process = new Process();
		process.buildProcessFromFile(pifFileName);
		final String pifModelName = process.name();
		final String ltsFileName = process.name() + LTS_SUFFIX;

		if (this.needsRebuild(pifFileName, ltsFileName))
		{
			return this.generate(pifFileName, generateLTS, smartReduction, debug);
		}
		else
		{
			return Triple.of(ReturnCodes.TERMINATION_OK, pifModelName, process.alpha());
		}
	}

	/**
	 * Decides if the LTS for the pifFileName has to be recomputed.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @param ltsFileName is the name of the LTS file
	 * @return true if the LTS must be rebuilt from the PIF file, false otherwise
	 */
	boolean needsRebuild(final String pifFileName,
						 final String ltsFileName)
	{
		final File pifFile = new File(pifFileName);
		final File ltsFile = new File(ltsFileName);

		//If the LTS file does not exist -> rebuilt
		if (!ltsFile.exists())
		{
			return true;
		}

		//If the timestamp of the LTS file is older than the timestamp of the PIF file -> rebuild
		return ltsFile.lastModified() < pifFile.lastModified();
	}
}
