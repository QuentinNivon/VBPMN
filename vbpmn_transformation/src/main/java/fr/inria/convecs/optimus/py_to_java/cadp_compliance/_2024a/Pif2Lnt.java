package fr.inria.convecs.optimus.py_to_java.cadp_compliance._2024a;

import fr.inria.convecs.optimus.pif.Peer;
import fr.inria.convecs.optimus.pif.SequenceFlow;
import fr.inria.convecs.optimus.pif.WorkflowNode;
import fr.inria.convecs.optimus.py_to_java.Pif2LntGeneric;
import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
	 * @param alphabet is the alphabet to dump
	 * @param printWriter is the printWriter representing the file to where the alphabet should be dumped
	 * @param addAny is a boolean indicating whether to add "any" or not
	 */
	public void dumpAlphabet(final ArrayList<String> alphabet,
							 final PrintWriter printWriter,
							 final boolean addAny)
	{
		final int nbElem = alphabet.size();

		if (nbElem > 0)
		{
			printWriter.print("[");
			int counter = 1;

			for (String element : alphabet)
			{
				printWriter.print(element);

				if (addAny)
				{
					printWriter.print(":any");
				}

				counter++;

				if (counter <= nbElem)
				{
					printWriter.print(", ");
				}
			}

			printWriter.print("]");
			//printWriter.close();
		}
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
	 * @param nbFlows is the number of flows
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

		abstract void writeMainLnt(final PrintWriter printWriter);
		abstract void processLnt(final PrintWriter printWriter);
		abstract void writeLnt(final PrintWriter printWriter);
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
		void writeLnt(final PrintWriter printWriter)
		{
			printWriter.println("process flow [begin:any, finish:any] (ident:ID) is");
			printWriter.println(" loop begin (ident) ; finish (ident) end loop");
			printWriter.println("end process");
			printWriter.println();
		}

		//A normal flow cannot be a default flow
		boolean isDefault()
		{
			return false;
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

		void processLnt(final PrintWriter printWriter)
		{
			printWriter.print("flow(");
			printWriter.print(this.identifier);
			printWriter.print(",");
			printWriter.print(this.source.identifier());
			printWriter.print(",");
			printWriter.print(this.target.identifier());
			printWriter.print(")");
		}
	}

	/**
	 * Class for ConditionalFlows
	 */
	class ConditionalFlow extends Flow
	{
		private final String condition;

		ConditionalFlow(String identifier,
						Node source,
						Node target,
						String condition)
		{
			super(identifier, source, target);
			this.condition = condition;
		}

		// Generates the process for conditional flows
		@Override
		void writeLnt(final PrintWriter printWriter)
		{
			//TODO: Translate the condition too
			printWriter.println("process conditionalflow [begin:any, finish:any] (ident: ID) is");
			printWriter.println(" loop begin (ident) ; finish (ident) end loop");
			printWriter.println("end process");
		}

		//A conditional flow is default iff the condition attribute contains "default"
		@Override
		boolean isDefault()
		{
			return this.condition.equals("default");
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
		void writeMainLnt(final PrintWriter printWriter)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on InitialEvent!");
		}

		//Generates the (generic) process for the initial event, only once
		@Override
		void writeLnt(final PrintWriter printWriter)
		{
			printWriter.println("process init [begin:any, outf:any] is");
			printWriter.println(" var ident: ID in begin ; outf (?ident of ID) end var ");
			printWriter.println("end process");
			printWriter.println();
		}

		/**
		 * Seeks or joins, for an initial event, just a recursive call on the target node of the outgoing flow.
		 * Returns the list of reachable or joins.
		 *
		 * @param visited the list of visited elements
		 * @param depth the current depth
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

		void processLnt(final PrintWriter printWriter)
		{
			printWriter.print("initial(");
			printWriter.print(this.identifier);
			printWriter.print(",");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print(")");
		}

		HashMap<String, String> schedulerLnt()
		{
			final String flowString = this.outgoingFlows.get(0).identifier() + "_begin (?ident of ID)";
			final String incIds = "{ident}";
			final String outIds = "{}";

			return new HashMap<String, String>(){{
				put("flowString", flowString);
				put("incIds", incIds);
				put("outIds", outIds);
			}};
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
		void writeMainLnt(final PrintWriter printWriter)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on EndEvent!");
		}

		//Generates the (generic) process for final events, only once
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			printWriter.println("process final [incf:any, finish:any] is");

			if (isBalanced)
			{
				printWriter.println(" var ident: ID in incf (?ident of ID); finish end var");
			}
			else
			{
				printWriter.println("var ident: ID in ");
				printWriter.println("loop ");
				printWriter.println("incf (?ident of ID); finish ");
				printWriter.println("end loop");
				printWriter.println("end var");
			}

			printWriter.println("end process");
			printWriter.println();
		}

		/**
		 * Seeks an or join, for an initial event, just a recursive call on the target node of the outgoing flow
		 *
		 * @param visited
		 * @param depth
		 * @return
		 */
		@Override
		ArrayList<Pair<String, Integer>> reachableOrJoin(ArrayList<Pair<String, Integer>> visited, int depth)
		{
			return new ArrayList<>();
		}

		void processLnt(final PrintWriter printWriter)
		{
			printWriter.print("final(");
			printWriter.print(this.identifier);
			printWriter.print(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				printWriter.print(inFlow.identifier());
			}

			printWriter.print("})");
		}

		HashMap<String, String> schedulerLnt()
		{
			final String flowString = this.incomingFlows.get(0).identifier() + "_finish (?ident of ID)";
			final String incIds = "{}";
			final String outIds = "{ident}";

			return new HashMap<String, String>(){{
				put("flowString", flowString);
				put("incIds", incIds);
				put("OutIds", outIds);
			}};
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

		String getMessage()
		{
			return this.message;
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
		void writeLnt(PrintWriter printWriter)
		{
			printWriter.println("process interaction [incf:any, inter:any, outf:any] is");
			printWriter.println(" var ident: ID in loop incf (?ident of ID); inter; outf (?ident of ID) end loop end var ");
			printWriter.println("end process");
		}

		@Override
		void processLnt(final PrintWriter printWriter)
		{
			//TODO Vérifier
			this.writeLnt(printWriter);
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
		void writeMainLnt(final PrintWriter printWriter)
		{
			//We assume one incoming flow and one outgoing flow
			printWriter.print("interaction [");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print("_finish,");
			printWriter.print(this.sender);
			printWriter.print("_");

			for (String e : this.receivers)
			{
				printWriter.print(e);
				printWriter.print("_");
			}

			printWriter.print(this.message);
			printWriter.print(",");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print("_begin]");
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
		void writeLnt(PrintWriter printWriter)
		{
			printWriter.println("process messagesending [incf:any, msg:any, outf:any] is");
			printWriter.println(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var ");
			printWriter.println("end process");
		}

		@Override
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> res = new ArrayList<>();
			res.add(this.message + "_EM");
			return res;
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("messagesending [");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print("_finish,");
			printWriter.print(this.message);
			printWriter.print("_EM,");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print("_begin]");
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
		void writeLnt(PrintWriter printWriter)
		{
			printWriter.println("process messagereception [incf:any, msg:any, outf:any] is");
			printWriter.println(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var");
			printWriter.println("end process");
		}

		@Override
		void processLnt(PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> result = new ArrayList<>();
			result.add(this.message + "_REC");
			return result;
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("messagereception [");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print("_finish");
			printWriter.print(this.message);
			printWriter.print("_REC");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print("_begin]");
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
		void processLnt(PrintWriter printWriter)
		{
			printWriter.print("task(");
			printWriter.print(this.identifier);
			printWriter.print(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				printWriter.print(inFlow.identifier());
			}

			printWriter.print("},");
			first = true;
			printWriter.print("{");

			for (Flow outFlow : this.outgoingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}
				printWriter.print(outFlow.identifier());
			}
			printWriter.print("})");
		}

		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();

			printWriter.print("process task_");
			printWriter.print(nbInc);
			printWriter.print("_");
			printWriter.print(nbOut);
			printWriter.print(" [");

			if (nbInc == 1)
			{
				printWriter.print("incf:any,");
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					printWriter.print("incf");
					printWriter.print(incCounter);
					printWriter.print(":any,");
					incCounter++;
				}
			}

			printWriter.print("task:any,");

			if (nbOut == 1)
			{
				printWriter.print("outf:any");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					printWriter.print("outf");
					printWriter.print(outCounter);
					printWriter.print(":any");
					outCounter++;

					if (outCounter < nbOut)
					{
						printWriter.print(",");
					}
				}
			}

			printWriter.println("] is");
			printWriter.print(" var ident: ID in loop ");

			if (nbInc == 1)
			{
				printWriter.print(" incf (?ident of ID); ");
			}
			else
			{
				int incCounter = 0;
				printWriter.print(" select ");

				while (incCounter < nbInc)
				{
					printWriter.print("incf");
					printWriter.print(incCounter);
					printWriter.print(" (?ident of ID)");
					incCounter++;

					if (incCounter < nbInc)
					{
						printWriter.print(" [] ");
					}
				}

				printWriter.println(" end select ; ");
			}

			printWriter.print("task ; ");

			if (nbOut == 1)
			{
				printWriter.print(" outf (?ident of ID)");
			}
			else
			{
				int outCounter = 0;
				printWriter.print(" select ");

				while (outCounter < nbOut)
				{
					printWriter.print("outf");
					printWriter.print(outCounter);
					printWriter.print(" (?ident of ID)");
					outCounter++;

					if (outCounter < nbOut)
					{
						printWriter.print(" [] ");
					}
				}

				printWriter.println(" end select ");
			}

			printWriter.println(" end loop end var");
			printWriter.println("end process");
			printWriter.println();
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

		void writeMainLnt(final PrintWriter printWriter)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();

			printWriter.print(" task_");
			printWriter.print(nbInc);
			printWriter.print("_");
			printWriter.print(nbOut);
			printWriter.print(" [");

			if (nbInc == 1)
			{
				printWriter.print(this.incomingFlows.get(0).identifier());
				printWriter.print("_finish,");
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					printWriter.print(this.incomingFlows.get(incCounter).identifier());
					printWriter.print("_finish,");
					incCounter++;
				}
			}

			printWriter.print(this.identifier);
			printWriter.print(",");

			if (nbOut == 1)
			{
				//TODO VOIR SI ON A VRAIMENT BESOIN DE DIFFÉRENCIER CES CAS
				printWriter.print(this.outgoingFlows.get(0).identifier());
				printWriter.print("_begin");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					printWriter.print(this.outgoingFlows.get(outCounter).identifier());
					printWriter.print("_begin");
					outCounter++;

					if (outCounter < nbOut)
					{
						printWriter.print(",");
					}
				}
			}

			printWriter.print("] ");
		}

		void dumpMaude(final PrintWriter printWriter)
		{
			final Random random = new Random();
			final int randomInt = random.nextInt(51);
			printWriter.print("        task(");
			printWriter.print(this.identifier);
			printWriter.print(",\"");
			printWriter.print(this.identifier);
			printWriter.print("\",");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print(",");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print(",");
			printWriter.print(randomInt);
			printWriter.print(")");
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

		void processLnt(final PrintWriter printWriter,
						final String pattern,
						final String type)
		{
			printWriter.print("gateway(");
			printWriter.print(this.identifier);
			printWriter.print(",");
			printWriter.print(pattern);
			printWriter.print(",");
			printWriter.print(type);
			printWriter.print(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				printWriter.print(inFlow.identifier());
			}

			printWriter.print("},{");
			first = true;

			for (Flow outFlow : this.outgoingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}
				printWriter.print(outFlow.identifier());
			}

			printWriter.print("})");
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
		 * @param printWriter
		 */
		void writeMainLnt(final PrintWriter printWriter)
		{
			final int nbOut = this.outgoingFlows.size();
			int i = 0;

			printWriter.print("[");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print("_finish,");

			while (i < nbOut)
			{
				printWriter.print(this.outgoingFlows.get(i).identifier());
				printWriter.print("_begin");
				i++;

				if (i < nbOut)
				{
					printWriter.print(",");
				}
			}

			printWriter.print("]");
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

		void dumpMaude(final PrintWriter printWriter,
					   final String nameOp)
		{
			printWriter.print("        split(");
			printWriter.print(this.identifier);
			printWriter.print(",");
			printWriter.print(nameOp);
			printWriter.print(",");
			printWriter.print(this.incomingFlows.get(0).identifier());
			printWriter.print(",(");
			String separator = "";

			for (Flow ofl : this.outgoingFlows)
			{
				printWriter.print(separator);
				printWriter.print(ofl.identifier());
				separator = ",";
			}

			printWriter.print("))");
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

		/**
		 * Checks whether the set of outgoing flows contains a default flow
		 * @return
		 */
		boolean existDefaultFlow()
		{
			for (Flow f : this.outgoingFlows)
			{
				if (f.isDefault())
				{
					return true;
				}
			}

			return false;
		}

		@Override
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbOut = this.outgoingFlows.size();
			final boolean existsDefault = this.existDefaultFlow();
			//TODO: update the translation to consider properly the default semantics (if there is such a branch)

			//We translate the inclusive split by enumerating all combinations in a select / par
			final ArrayList<String> alphaOut = new ArrayList<>();
			int nb = 1;

			while (nb <= nbOut)
			{
				alphaOut.add("outf_" + nb);
				nb++;
			}

			final ArrayList<ArrayList<String>> allCombi = computeAllCombinations(alphaOut);
			final int nbt = allCombi.size();

			final StringBuilder builder  = new StringBuilder();
			for (ArrayList<String> collection : allCombi)
			{
				builder.append("Collection: [");

				for (String s : collection)
				{
					builder.append(s).append(",");
				}

				builder.append("]\n");
			}

			printWriter.print("process orsplit_");
			printWriter.print(this.identifier);
			printWriter.print(" [incf:any,");

			//We dump the process alphabet (flows + synchronization points if necessary)
			int nbg = 1;

			while (nbg <= nbOut)
			{
				printWriter.print("outf_");
				printWriter.print(nbg);
				printWriter.print(":any");
				nbg++;

				if (nbg <= nbOut)
				{
					printWriter.print(",");
				}
			}

			if (nbt > 0
				&& (!isBalanced || !this.correspOrJoin.isEmpty()))
			{
				printWriter.print(", ");
				int counter = 1;

				for (Collection<String> combi : allCombi) //TODO Bizarre ....
				{
					printWriter.print(isBalanced ? this.correspOrJoin : this.identifier);
					printWriter.print("_");
					printWriter.print(counter);
					printWriter.print(":any");
					counter++;

					if (counter <= nbt)
					{
						printWriter.print(",");
					}
				}
			}

			printWriter.println(" ] is ");
			int counterVar = allCombi.size();
			printWriter.print(" var ");

			while (counterVar > 0)
			{
				printWriter.print("ident");
				printWriter.print(counterVar);
				printWriter.print(":ID");
				counterVar--;

				if (counterVar > 0)
				{
					printWriter.print(",");
				}
			}

			if (isBalanced)
			{
				printWriter.println(" in  var ident: ID in loop incf (?ident of ID); "); //TODO We generate unnecessary variables...
				printWriter.print(" select ");
			}
			else
			{
				printWriter.println(" in ");
				printWriter.println("var ident: ID in loop ");
				printWriter.println("incf (?ident of ID); "); //TODO We generate unnecessary variables...
				printWriter.print("select ");
			}

			nb = 1;
			//Counter for generating synchro points
			int counter = 1;

			for (Collection<String> element : allCombi)
			{
				final int nbElem = element.size();
				int nb2 = 1;

				if (!isBalanced)
				{
					printWriter.println();
					printWriter.print(this.identifier);
					printWriter.print("_");
					printWriter.print(counter);
					printWriter.print("; ");
					counter++;
				}

				if (nbElem > 1)
				{
					counterVar = allCombi.size();

					if (isBalanced)
					{
						printWriter.print(" par ");
					}
					else
					{
						printWriter.println();
						printWriter.println("par");
					}

					for (String s : element)
					{
						printWriter.print(s);
						printWriter.print(" (?ident");
						printWriter.print(counterVar);
						printWriter.print(" of ID)");
						counterVar--;
						nb2++;

						if (nb2 <= nbElem)
						{
							if (isBalanced)
							{
								printWriter.print("||");
							}
							else
							{
								printWriter.println();
								printWriter.println("||");
							}
						}
					}

					if (isBalanced)
					{
						printWriter.print(" end par ");
					}
					else
					{
						printWriter.println();
						printWriter.print("end par");
					}
				}
				else
				{
					printWriter.print(element.iterator().next());
					printWriter.print(" (?ident of ID)");
				}

				if (isBalanced)
				{
					//Add synchronization points if there's a corresponding join
					if (!this.correspOrJoin.isEmpty())
					{
						printWriter.print(" ; ");
						printWriter.print(this.correspOrJoin);
						printWriter.print("_");
						printWriter.print(counter);
						counter++;
					}
				}

				nb++;

				if (nb <= nbt)
				{
					if (isBalanced)
					{
						printWriter.print(" [] ");
					}
					else
					{
						printWriter.println();
						printWriter.print("[] ");
					}
				}
			}

			if (isBalanced)
			{
				printWriter.println(" end select end loop end var end var");
				printWriter.println("end process");
			}
			else
			{
				printWriter.println();
				printWriter.println("end select ");
				printWriter.println("end loop ");
				printWriter.println("end var");
				printWriter.println("end var");
				printWriter.println("end process");
				printWriter.println();
			}
		}

		/**
		 * Generates process instantiation for main LNT process.
		 *
		 * @param printWriter
		 */
		void writeMainLnt(final PrintWriter printWriter)
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

				if (isBalanced)
				{
					//We dump the synchronisation points
					if (nbCombi > 0)
					{
						int counter = 1;

						for (ArrayList<String> combination : allCombinations) //TODO Bizarre...
						{
							printWriter.print(this.correspOrJoin);
							printWriter.print("_");
							printWriter.print(counter);
							counter++;

							if (counter <= nbCombi)
							{
								printWriter.print(",");
							}
						}

						printWriter.print(" -> ");
					}
				}

				//Process call + alphabet
				printWriter.print("orsplit_");
				printWriter.print(this.identifier);
				printWriter.print("[");
				printWriter.print(this.incomingFlows.get(0).identifier());
				printWriter.print("_finish,");
				int i = 0;

				while (i < nbOut)
				{
					printWriter.print(this.outgoingFlows.get(i).identifier());
					printWriter.print("_begin");
					i++;

					if (i < nbOut)
					{
						printWriter.print(",");
					}
				}

				if (nbCombi > 0)
				{
					printWriter.print(", ");
					int counter = 1;

					for (ArrayList<String> combination : allCombinations)
					{
						printWriter.print(isBalanced ? this.correspOrJoin : this.identifier);
						printWriter.print("_");
						printWriter.print(counter);
						counter++;

						if (counter <= nbCombi)
						{
							printWriter.print(",");
						}
					}
				}

				printWriter.print("]");
			}
			else
			{
				printWriter.print("orsplit_");
				printWriter.print(this.identifier);
				super.writeMainLnt(printWriter);
			}
		}

		/**
		 * For an or split, if not visited yet, recursive call on the target nodes of all outgoing flows.
		 * We increase the depth, to distinguish it from the split or being analyzed.
		 *
		 * @param visited
		 * @param depth
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

		public void dumpMaude(final PrintWriter printWriter)
		{
			if (isBalanced)
			{
				super.dumpMaude(printWriter, "inclusive");
			}
			else
			{
				printWriter.print("        split(");
				printWriter.print(this.identifier);
				printWriter.print(",inclusive,");
				printWriter.print(this.incomingFlows.get(0).identifier());
				printWriter.print(",(");
				int counter = this.outgoingFlows.size();

				for (Flow outFlow : this.outgoingFlows)
				{
					final Random random = new Random();
					final double proba = Math.round(random.nextDouble() * 100.0) / 100.0;
					counter--;
					printWriter.print("(");
					printWriter.print(outFlow.identifier());
					printWriter.print(",");
					printWriter.print(proba);
					printWriter.print(")");

					if (counter > 0) printWriter.print(" ");
				}

				printWriter.print("))");
			}
		}

		void schedulerLnt()
		{
			final String lnt = this.incomingFlows.get(0).identifier() + "_finish (?ident1 of ID);";
			final HashMap<String, String> idMap = new HashMap<>();
			int idCounter = 1;
			//TODO A revoir, le code Python semble très étrange....
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
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param printWriter
		 */
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbOut = this.outgoingFlows.size();
			printWriter.print("process xorsplit_");
			printWriter.print(this.identifier);
			printWriter.print(" [incf:any,");
			int nb = 1;

			while (nb <= nbOut)
			{
				printWriter.print("outf_");
				printWriter.print(nb);
				printWriter.print(":any");
				nb++;

				if (nb <= nbOut)
				{
					printWriter.print(",");
				}
			}

			printWriter.println(" ] is ");
			printWriter.println(" var ident: ID in loop incf (?ident of ID); ");
			printWriter.print(" select ");
			nb = 1;

			while (nb <= nbOut)
			{
				printWriter.print("outf_");
				printWriter.print(nb);
				printWriter.print("(?ident of ID)");
				nb++;

				if (nb <= nbOut)
				{
					printWriter.print("[]");
				}
			}

			printWriter.println(" end select end loop end var");
			printWriter.println("end process");
			printWriter.println();
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("xorsplit_");
			printWriter.print(this.identifier);
			super.writeMainLnt(printWriter);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final PrintWriter printWriter)
		{
			if (isBalanced)
			{
				super.dumpMaude(printWriter, "exclusive");
			}
			else
			{
				printWriter.print("        split(");
				printWriter.print(this.identifier);
				printWriter.print(",exclusive,");
				printWriter.print(this.incomingFlows.get(0).identifier());
				printWriter.print(",(");
				int counter = this.outgoingFlows.size();
				double proba = ((1d / (double) counter) * 1000.0) / 1000.0;

				for (Flow outFlow : this.outgoingFlows)
				{
					counter--;
					printWriter.print("(");
					printWriter.print(outFlow.identifier());
					printWriter.print(",");
					printWriter.print(proba);
					printWriter.print(")");

					if (counter > 0)
					{
						printWriter.print(" ");
					}
				}

				printWriter.print("))");
			}
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
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param printWriter
		 */
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbOut = this.outgoingFlows.size();
			printWriter.print("process andsplit_");
			printWriter.print(this.identifier);
			printWriter.print(" [incf:any,");
			int nb = 1;

			while (nb <= nbOut)
			{
				printWriter.print("outf_");
				printWriter.print(nb);
				printWriter.print(":any");
				nb++;

				if (nb <= nbOut)
				{
					printWriter.print(",");
				}
			}

			printWriter.println(" ] is ");
			int variablesCounter = nbOut;
			printWriter.print(" var ");

			while (variablesCounter > 0)
			{
				printWriter.print("ident");
				printWriter.print(variablesCounter);
				printWriter.print(":ID");
				variablesCounter--;

				if (variablesCounter > 0)
				{
					printWriter.print(",");
				}
			}

			printWriter.println(" in  var ident: ID in loop incf (?ident of ID); ");
			printWriter.print(" par ");
			nb = 1;
			variablesCounter = nbOut;

			while (nb <= nbOut)
			{
				printWriter.print("outf_");
				printWriter.print(nb);
				printWriter.print("(?ident");
				printWriter.print(variablesCounter);
				printWriter.print(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbOut)
				{
					printWriter.print("||");
				}
			}

			printWriter.println(" end par end loop end var end var");
			printWriter.println("end process");
			printWriter.println();
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("andsplit_");
			printWriter.print(this.identifier);
			super.writeMainLnt(printWriter);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final PrintWriter printWriter)
		{
			super.dumpMaude(printWriter, "parallel");
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
		 * @param printWriter
		 */
		void writeMainLnt(final PrintWriter printWriter)
		{
			//We assume one outgoing flow
			final int nbInc = this.incomingFlows.size();
			printWriter.print("[");
			int i = 0;

			while (i < nbInc)
			{
				printWriter.print(this.incomingFlows.get(i).identifier());
				printWriter.print("_finish");
				i++;
				printWriter.print(",");
			}

			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print("_begin]");
		}

		/**
		 * For a join (generic), if not visited yet, recursive call on the target node of the outgoing flow.
		 *
		 * @param visited
		 * @param depth
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

		/**
		 * Dumps a Maude line of code into the given file.
		 *
		 * @param printWriter
		 */
		void dumpMaude(final PrintWriter printWriter,
					   final String nameOp)
		{
			printWriter.print("        merge(");
			printWriter.print(this.identifier);
			printWriter.print(",");
			printWriter.print(nameOp);
			printWriter.print(",(");
			int nbInc = this.incomingFlows.size();

			for (Flow ofl : this.incomingFlows) //TODO : Bizarre "ofl" VS "incomingFlows"
			{
				nbInc--;
				printWriter.print(ofl.identifier());

				if (nbInc > 0)
				{
					printWriter.print(",");
				}
			}

			printWriter.print("),");
			printWriter.print(this.outgoingFlows.get(0).identifier());
			printWriter.print(")");
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
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		/**
		 * Generates the process for inclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param printWriter
		 */
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbInc = this.incomingFlows.size();

			if (isBalanced)
			{
				final ArrayList<String> alphaInc = new ArrayList<>();
				int nb = 1;

				while (nb <= nbInc)
				{
					alphaInc.add("incf_" + nb);
					nb++;
				}

				final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaInc);
				final int nbCombi = allCombinations.size();

				printWriter.print("process orjoin_");
				printWriter.print(this.identifier);
				printWriter.print(" [");
				nb = 1;

				while (nb <= nbInc)
				{
					printWriter.print("incf_");
					printWriter.print(nb);
					printWriter.print(":any,");
					nb++;
				}

				printWriter.print("outf:any ");

				//we add to the alphabet potential additional synchronization points
				if (nbCombi > 0
						&& !this.correspondingOrSplit.isEmpty())
				{
					int counter = 1;
					printWriter.print(",");

					for (ArrayList<String> combination : allCombinations)
					{
						printWriter.print(this.identifier);
						printWriter.print("_");
						printWriter.print(counter);
						printWriter.print(":any");
						counter++;

						if (counter <= nbCombi)
						{
							printWriter.print(",");
						}
					}
				}

				printWriter.println("] is ");
				printWriter.print(" var ");
				int variablesCounter = allCombinations.size();

				while (variablesCounter > 0) //TODO: we generate unnecessary variables
				{
					printWriter.print("ident");
					printWriter.print(variablesCounter);
					printWriter.print(":ID");
					variablesCounter--;

					if (variablesCounter > 0)
					{
						printWriter.print(",");
					}
				}

				printWriter.print(" in  var ident: ID in loop select ");
				nb = 1;
				int counter = 1;

				for (ArrayList<String> combination : allCombinations)
				{
					int nbElem = combination.size();
					int nb2 = 1;

					// add synchronization points if there's a corresponding split
					if (!this.correspondingOrSplit.isEmpty())
					{
						printWriter.print(this.identifier);
						printWriter.print("_");
						printWriter.print(counter);
						printWriter.print(";");
						counter++;
					}

					if (nbElem > 1)
					{
						variablesCounter = allCombinations.size();
						printWriter.print(" par ");

						for (String element : combination)
						{
							printWriter.print(element);
							printWriter.print(" (?ident");
							printWriter.print(variablesCounter);
							printWriter.print(" of ID)");
							variablesCounter--;
							nb2++;

							if (nb2 <= nbElem)
							{
								printWriter.print("||");
							}
						}

						printWriter.print(" end par ");
					}
					else
					{
						printWriter.print(combination.iterator().next());
						printWriter.print(" (?ident of ID)");
					}

					nb++;

					if (nb <= nbCombi)
					{
						printWriter.print(" [] ");
					}
				}

				printWriter.println(" end select ; outf (?ident of ID) end loop end var end var ");
				printWriter.println("end process");
			}
			else
			{
				printWriter.print("process orjoin_");
				printWriter.print(this.identifier);
				printWriter.print(" [");
				int nb = 1;

				while (nb <= nbInc)
				{
					printWriter.print("incf_");
					printWriter.print(nb);
					printWriter.print(":any,");
					nb++;
				}

				printWriter.println("outf:any, MoveOn:any] (mergeid: ID) is ");
				printWriter.println("var mergestatus:Bool, ident:ID in ");
				printWriter.println(" loop");
				printWriter.println("mergestatus := False;");
				printWriter.println("while mergestatus == False loop ");
				printWriter.println("select");

				nb = 1;

				while (nb <= nbInc)
				{
					printWriter.print("incf_");
					printWriter.print(nb);
					printWriter.print(" (?ident of ID)");
					nb++;

					if (nb <= nbInc)
					{
						printWriter.println();
						printWriter.print("[]");
					}
				}

				printWriter.println();
				printWriter.println("[] MoveOn(mergeid); mergestatus := True");
				printWriter.println("end select");
				printWriter.println("end loop;");
				printWriter.println("outf (?ident of ID)");
				printWriter.println("end loop");
				printWriter.println("end var");
				printWriter.println("end process");
				printWriter.println();
			}
		}

		//Generates process instantiation for main LNT process
		void writeMainLnt(final PrintWriter printWriter)
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

					//We dump synchronization points
					if (nbCombi > 0)
					{
						int counter = 1;

						for (ArrayList<String> combination : allCombinations)
						{
							printWriter.print(this.identifier);
							printWriter.print("_");
							printWriter.print(counter);
							counter++;

							if (counter <= nbCombi)
							{
								printWriter.print(",");
							}
						}

						printWriter.print(" -> ");
					}

					//Process call + alphabet
					printWriter.print("orjoin_");
					printWriter.print(this.identifier);
					printWriter.print("[");
					int i = 0;

					while (i < nbInc)
					{
						printWriter.print(this.incomingFlows.get(i).identifier());
						printWriter.print("_finish,");
						i++;
					}

					printWriter.print(this.outgoingFlows.get(0).identifier());
					printWriter.print("_begin");

					if (nbCombi > 0)
					{
						int counter = 1;
						printWriter.print(",");

						for (ArrayList<String> combination : allCombinations)
						{
							printWriter.print(this.identifier);
							printWriter.print("_");
							printWriter.print(counter);
							counter++;

							if (counter <= nbCombi)
							{
								printWriter.print(",");
							}
						}
					}

					printWriter.print("]");
				}
				else
				{
					printWriter.print("orjoin_");
					printWriter.print(this.identifier);
					super.writeMainLnt(printWriter);
				}
			}
			else
			{
				printWriter.print("orjoin_");
				printWriter.print(this.identifier);
				//We assume one outgoing flow
				final int nbInc = this.incomingFlows.size();
				printWriter.print("[");
				int i = 0;

				while (i < nbInc)
				{
					printWriter.print(this.incomingFlows.get(i).identifier());
					printWriter.print("_finish,");
					i++;
				}

				printWriter.print(this.outgoingFlows.get(0).identifier());
				printWriter.print("_begin, MoveOn] (");
				printWriter.print(this.identifier);
				printWriter.print(")");
			}
		}

		/**
		 * For an or join, if not visited yet, recursive call on the target node of the outgoing flow.
		 * We store the result and we decrease the depth.
		 *
		 * @param visited
		 * @param depth
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

		void dumpMaude(final PrintWriter printWriter)
		{
			super.dumpMaude(printWriter, "inclusive");
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
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param printWriter
		 */
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbInc = this.incomingFlows.size();
			printWriter.print("process xorjoin_");
			printWriter.print(this.identifier);
			printWriter.print(" [");
			int nb = 1;

			while (nb <= nbInc)
			{
				printWriter.print("incf_");
				printWriter.print(nb);
				printWriter.print(":any,");
				nb++;
			}

			printWriter.println("outf:any] is ");
			printWriter.print(" var ident: ID in loop select ");
			nb = 1;

			while (nb <= nbInc)
			{
				printWriter.print("incf_");
				printWriter.print(nb);
				printWriter.print(" (?ident of ID)");
				nb++;

				if (nb <= nbInc)
				{
					printWriter.print("[]");
				}
			}

			printWriter.println(" end select ; outf (?ident of ID) end loop end var ");
			printWriter.println("end process");
			printWriter.println();
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("xorjoin_");
			printWriter.print(this.identifier);
			super.writeMainLnt(printWriter);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
										 				 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final PrintWriter printWriter)
		{
			super.dumpMaude(printWriter, "exclusive");
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
		void processLnt(final PrintWriter printWriter)
		{
			this.writeLnt(printWriter); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param printWriter
		 */
		@Override
		void writeLnt(PrintWriter printWriter)
		{
			final int nbInc = this.incomingFlows.size();
			printWriter.print("process andjoin_");
			printWriter.print(this.identifier);
			printWriter.print(" [");
			int nb = 1;

			while (nb <= nbInc)
			{
				printWriter.print("incf_");
				printWriter.print(nb);
				printWriter.print(":any,");
				nb++;
			}

			printWriter.println("outf:any] is ");
			int variablesCounter = nbInc;
			printWriter.print(" var ");

			while (variablesCounter > 0)
			{
				printWriter.print("ident");
				printWriter.print(variablesCounter);
				printWriter.print(":ID");
				variablesCounter--;

				if (variablesCounter > 0)
				{
					printWriter.print(",");
				}
			}

			printWriter.print(" in  var ident:ID in loop par ");
			nb = 1;
			variablesCounter = nbInc;

			while (nb <= nbInc)
			{
				printWriter.print("incf_");
				printWriter.print(nb);
				printWriter.print(" (?ident");
				printWriter.print(variablesCounter);
				printWriter.print(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbInc)
				{
					printWriter.print("||");
				}
			}

			printWriter.println(" end par ; outf (?ident of ID) end loop end var end var ");
			printWriter.println("end process");
			printWriter.println();
		}

		void writeMainLnt(final PrintWriter printWriter)
		{
			printWriter.print("andjoin_");
			printWriter.print(this.identifier);
			super.writeMainLnt(printWriter);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final PrintWriter printWriter)
		{
			super.dumpMaude(printWriter, "parallel");
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

		Node initialNode()
		{
			return this.initial;
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
		 * @return
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
		 * @return
		 */
		ArrayList<String> computeAddSynchroPoints()
		{
			final ArrayList<String> res = new ArrayList<>();

			for (Node n : this.nodes)
			{
				if (n instanceof OrSplitGateway)
				{
					if (!((OrSplitGateway) n).getCorrespOrJoin().isEmpty())
					{
						final ArrayList<String> alphaOut = new ArrayList<>();
						int nb = 1;

						while (nb <= n.outgoingFlows().size())
						{
							alphaOut.add("outf_" + nb);
							nb++;
						}

						final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaOut);
						final int nbCombi = allCombinations.size();
						int counter = 1;

						for (ArrayList<String> combination : allCombinations)
						{
							res.add(((OrSplitGateway) n).getCorrespOrJoin() + "_" + counter);
							counter++;
						}
					}
				}
			}

			return res;
		}

		/**
		 * Computes the list with the additional synchronization points for corresponding or splits/joins.
		 *
		 * @return
		 */
		ArrayList<String> computeAddSynchroPoints(final boolean any)
		{
			final ArrayList<String> res = new ArrayList<>();

			for (Node n : this.nodes)
			{
				if (n instanceof OrSplitGateway)
				{
					final ArrayList<String> alphaOut = new ArrayList<>();
					int nb = 1;

					while (nb <= n.outgoingFlows().size())
					{
						alphaOut.add("outf_" + nb);
						nb++;
					}

					final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(alphaOut);
					final int nbCombi = allCombinations.size();
					int counter = 1;

					for (ArrayList<String> combination : allCombinations)
					{
						res.add(n.identifier() + "_" + counter + (any ? ":any" : ""));
						counter++;
					}
				}
			}

			return res;
		}

		//Dumps the alphabet for the scheduler process
		void dumpFlowsMsgs(final PrintWriter printWriter,
						   final boolean withAny)
		{
			final int nbFlows = this.flows.size();
			int counter = 1;

			for (Flow fl : this.flows)
			{
				printWriter.print(fl.identifier());
				printWriter.print("_begin");

				if (withAny)
				{
					printWriter.print(":any");
				}

				printWriter.print(", ");
				printWriter.print(fl.identifier());
				printWriter.print("_finish");

				if (withAny)
				{
					printWriter.print(":any");
				}

				counter++;

				if (counter <= nbFlows)
				{
					printWriter.print(", ");
				}
			}
		}

		String getFlowMsgs(final boolean withAny)
		{
			final int nbFlows = this.flows.size();
			final ArrayList<String> flowString = new ArrayList<>();
			int counter = 1;

			for (Flow fl : this.flows)
			{
				flowString.add(fl.identifier() + "_begin");

				if (withAny)
				{
					flowString.add(":any");
				}

				flowString.add("," + fl.identifier() + "_finish");

				if (withAny)
				{
					flowString.add(":any");
				}

				counter++;

				if (counter <= nbFlows)
				{
					flowString.add(", ");
				}
			}

			return PyToJavaUtils.join(flowString, "");
		}

		void processDump(final PrintWriter printWriter)
		{
			printWriter.println();
			printWriter.println("function p1(): BPROCESS is ");
			printWriter.println();
			printWriter.println(" return proc ( ");
			printWriter.print(this.name);
			printWriter.println(",");
			printWriter.println("{");
			printWriter.print("\ti ( ");
			this.initial.processLnt(printWriter);
			printWriter.println(" ),");

			//handle final
			boolean first = true;
			printWriter.print("\tf ( { ");

			for (Node fnode : this.finals)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				fnode.processLnt(printWriter);
			}

			printWriter.println(" } ),");

			//TODO: eliminate iterating twice / Separate printer class?
			//handle tasks
			printWriter.print("\tt ( { ");
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
						printWriter.print(",");
					}

					pNode.processLnt(printWriter);
				}
			}

			printWriter.println(" } ), ");

			//handle gateways
			printWriter.print("\tg ( { ");
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
						printWriter.print(",");
					}

					if (pNode instanceof XOrJoinGateway)
					{
						((XOrJoinGateway) pNode).processLnt(printWriter, "merge", "xor");
					}
					if (pNode instanceof XOrSplitGateway)
					{
						((XOrSplitGateway) pNode).processLnt(printWriter, "split", "xor");
					}
					if (pNode instanceof OrJoinGateway)
					{
						((OrJoinGateway) pNode).processLnt(printWriter, "merge", "or");
					}
					if (pNode instanceof OrSplitGateway)
					{
						((OrSplitGateway) pNode).processLnt(printWriter, "split", "or");
					}
					if (pNode instanceof AndJoinGateway)
					{
						((AndJoinGateway) pNode).processLnt(printWriter, "merge", "and");
					}
					if (pNode instanceof AndSplitGateway)
					{
						((AndSplitGateway) pNode).processLnt(printWriter, "split", "and");
					}
				}
			}

			printWriter.println(" } )");
			printWriter.println("},");
			//flows
			printWriter.println("{ ");
			first = true;

			for (Flow flow : this.flows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				flow.processLnt(printWriter);
			}

			printWriter.println();
			printWriter.println("}");
			printWriter.println(")");
			printWriter.println("end function");
			printWriter.println();
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

		void generateScheduler(final PrintWriter printWriter)
		{
			printWriter.println();
			printWriter.print("process scheduler [");
			printWriter.print(this.getFlowMsgs(true));
			//Add split synchro params
			final ArrayList<String> synchroParams = this.computeAddSynchroPoints(true);

			if (!synchroParams.isEmpty())
			{
				printWriter.print(",");

				String separator = "";

				for (String synchroParam : synchroParams)
				{
					printWriter.print(separator);
					printWriter.print(synchroParam);
					separator = ",";
				}
			}

			//This parameter stores the set of active flows/tokens
			printWriter.println(", MoveOn:any] (activeflows: IDS, bpmn: BPROCESS, syncstore: IDS, mergestore:IDS, " +
					"parstore:IDS) is");

			final ArrayList<String> identSet = new ArrayList<>();
			final ArrayList<String> flowSelectStrings = new ArrayList<>();
			final ArrayList<String> incJoinBeginList = new ArrayList<>();
			final ArrayList<String> parJoinBeginList = new ArrayList<>();

			for (Node node : this.nodes)
			{
				final ArrayList<String> flowString = new ArrayList<>();

				if (node instanceof Task)
				{
					flowString.add("\n(*----  Task with ID: " + node.identifier() + "------*)\n");
					flowString.add(node.incomingFlows().get(0).identifier() + "_finish (?ident1 of ID); " +
							node.outgoingFlows().get(0).identifier() + "_begin (?ident2 of ID);");
					flowString.add(this.getSchedulerString(
							"{ident1}",
							"{ident2}",
							SYNC_STORE,
							MERGE_STORE,
							PAR_STORE
					));
					identSet.add("ident1");
					identSet.add("ident2");
				}
				else if (node instanceof XOrSplitGateway)
				{
					flowString.add("\n(*----  XOrSplitGateway with ID: " + node.identifier() + "------*)\n");
					flowString.add(node.firstIncomingFlow().identifier() + "_finish (?ident1 of ID); ");
					identSet.add("ident1");
					boolean first = true;
					int counter = 2;
					flowString.add(" select\n");

					for (Flow flow : node.outgoingFlows())
					{
						if (first)
						{
							first = false;
						}
						else
						{
							flowString.add("[]\n");
						}

						flowString.add(flow.identifier() + "_begin (?ident" + counter + " of ID); ");
						identSet.add("ident" + counter);
						flowString.add(this.getSchedulerString(
								"{ident1}",
								"{ident" + counter + "}",
								SYNC_STORE,
								MERGE_STORE,
								PAR_STORE
						));
						counter++;
					}

					flowString.add("\nend select ");
				}
				else if (node instanceof XOrJoinGateway)
				{
					flowString.add("\n(*----  XOrJoinGateway with ID: " + node.identifier() + "------*)\n");
					boolean first = true;
					flowString.add(" select\n");

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
						}
						else
						{
							flowString.add("\n[]\n");
						}

						flowString.add(flow.identifier() + "_finish (?ident2 of ID) ");
					}

					flowString.add("\nend select; ");
					flowString.add(node.firstOutgoingFlow().identifier() + "_begin (?ident1 of ID); ");
					identSet.add("ident1");
					identSet.add("ident2");
					flowString.add(this.getSchedulerString(
							"{ident2}",
							"{ident1}",
							SYNC_STORE,
							MERGE_STORE,
							PAR_STORE
					));
				}
				else if (node instanceof AndSplitGateway)
				{
					flowString.add("\n(*----  AndSplitGateway with ID: " + node.identifier() + "------*)\n");
					flowString.add(node.firstIncomingFlow().identifier() + "_finish (?ident1 of ID);");
					boolean first = true;
					int counter = 2;
					final ArrayList<String> outIds = new ArrayList<>();
					flowString.add("par\n");

					for (Flow flow : node.outgoingFlows())
					{
						if (first)
						{
							first = false;
						}
						else
						{
							flowString.add("\n||");
						}

						flowString.add(flow.identifier() + "_begin (?ident" + counter + " of ID) ");
						identSet.add("ident" + counter);
						outIds.add("ident" + counter);
						counter++;
					}

					flowString.add("\nend par;");
					flowString.add(this.getSchedulerString(
							"{ident1}",
							"{" + PyToJavaUtils.join(outIds, ",") + "}",
							SYNC_STORE,
							MERGE_STORE,
							PAR_STORE
					));
				}
				else if (node instanceof AndJoinGateway)
				{
					flowString.add("\n(*----  AndJoinGateway with ID: " + node.identifier() + "------*)\n");
					boolean first = true;

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
							identSet.add("ident");
						}
						else
						{
							flowString.add("\n[]\n");
						}

						flowString.add(flow.identifier() + "_finish (?ident of ID); ");
						flowString.add(this.getSchedulerString(
								"{}",
								"{}",
								"insert(ident, syncstore)",
								MERGE_STORE,
								"insert(" + node.identifier() + ", parstore)"
						));
					}

					identSet.add("ident1");
					//Parallel merge join TODO: Clean up
					final ArrayList<String> parJoinString = new ArrayList<>();
					parJoinString.add(node.firstOutgoingFlow().identifier() + "_begin (?ident1 of ID);");
					parJoinString.add("scheduler [");
					parJoinString.add(this.getFlowMsgs(false));
					final ArrayList<String> res = this.computeAddSynchroPoints(false);

					if (!res.isEmpty())
					{
						parJoinString.add(",");
						parJoinString.add(PyToJavaUtils.join(res, ","));
					}

					parJoinString.add(", MoveOn]");
					parJoinString.add("(union({ident1}, remove_incf(bpmn, activeflows, mergeid)), bpmn, " +
							"remove_sync(bpmn, syncstore, mergeid), mergestore, remove(mergeid, parstore))\n");
					parJoinBeginList.add(PyToJavaUtils.join(parJoinString, ""));
				}
				else if (node instanceof OrSplitGateway)
				{
					flowString.add("\n(*----  OrSplitGateway with ID: " + node.identifier() + "------*)\n");
					flowString.add(node.firstIncomingFlow().identifier() + "_finish (?ident1 of ID); \n");
					identSet.add("ident1");

					final int nbOut = node.outgoingFlows().size();
					//We translate the inclusive split by enumerating all combinations in a select/par
					final ArrayList<String> flowAlpha = new ArrayList<>();
					int counter = 2;

					for (Flow flow : node.outgoingFlows())
					{
						flowAlpha.add(flow.identifier() + "_begin");
						identSet.add("ident" + counter);
						counter++;
					}

					final ArrayList<ArrayList<String>> allCombinations = computeAllCombinations(flowAlpha);
					final int nbCombinations = allCombinations.size();
					final ArrayList<String> outIds = new ArrayList<>();
					flowString.add("select ");
					int nb = 1;
					int cter = 1;

					for (ArrayList<String> combination : allCombinations)
					{
						final int nbElemCombi = combination.size();
						int nb2 = 1;
						flowString.add(" \n " + node.identifier() + "_" + cter + "; ");
						cter++;

						if (nbElemCombi > 1)
						{
							int combiCounter = nbCombinations;
							flowString.add("\npar\n");

							for (String element : combination)
							{
								flowString.add(element + "(?ident" + combiCounter + " of ID)");
								outIds.add("ident" + combiCounter);
								identSet.add("ident" + combiCounter);
								combiCounter--;
								nb2++;

								if (nb2 <= nbElemCombi)
								{
									flowString.add("\n||\n");
								}
							}

							flowString.add("\nend par");
							flowString.add(";" + this.getSchedulerString(
									"{ident1}",
									"{" + PyToJavaUtils.join(outIds, ",") + "}",
									SYNC_STORE,
									MERGE_STORE,
									PAR_STORE
							));
						}
						else
						{
							flowString.add(combination.iterator().next() + " (?ident of ID)");
							flowString.add(";" + this.getSchedulerString(
									"{ident1}",
									"{ident}",
									SYNC_STORE,
									MERGE_STORE,
									PAR_STORE
							));
							identSet.add("ident");
						}

						nb++;

						if (nb <= nbCombinations)
						{
							flowString.add("\n[] ");
						}
					}

					flowString.add("\nend select\n");
				}
				else if (node instanceof OrJoinGateway)
				{
					flowString.add("\n(*----  OrJoinGateway with ID: " + node.identifier() + "------*)\n");
					boolean first = true;

					for (Flow flow : node.incomingFlows())
					{
						if (first)
						{
							first = false;
							identSet.add("ident");
						}
						else
						{
							flowString.add("\n[]\n");
						}

						flowString.add(flow.identifier() + "_finish (?ident of ID); ");
						flowString.add(this.getSchedulerString(
								"{}",
								"{}",
								"insert(ident, syncstore)",
								"insert(" + node.identifier() + ", mergestore)",
								PAR_STORE
						));
					}

					identSet.add("ident1");
					//Inclusive merge join TODO: Clean up
					final ArrayList<String> incJoinString = new ArrayList<>();
					incJoinString.add(node.firstOutgoingFlow().identifier() + "_begin (?ident1 of ID);");
					incJoinString.add("scheduler [");
					incJoinString.add(this.getFlowMsgs(false));
					final ArrayList<String> res = this.computeAddSynchroPoints(false);

					if (!res.isEmpty())
					{
						incJoinString.add(",");
						incJoinString.add(PyToJavaUtils.join(res, ","));
					}

					incJoinString.add(", MoveOn]");
					incJoinString.add("(union({ident1}, remove_incf(bpmn, activeflows, mergeid)), bpmn, " +
							"remove_sync(bpmn, syncstore, mergeid), remove(mergeid, mergestore), parstore)\n");
					incJoinBeginList.add(PyToJavaUtils.join(incJoinString, ""));
				}
				else
				{
					//TODO Intéret ????
					flowString.add("\n(*----  ERROR - Unable to select ID: " + node.identifier() + "------*)\n");
					flowString.clear();
					flowString.add("");
				}

				final String separator = "";
				flowSelectStrings.add(PyToJavaUtils.join(flowString, separator));
			}

			//Generate var
			identSet.add("ident1"); //For initial/final
			printWriter.print("var ");
			boolean first = true;

			for (String ident : new HashSet<>(identSet)) //TODO Intéret ? Randomiser ?
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.print(",");
				}

				printWriter.print(ident);
				printWriter.print(": ID");
			}

			printWriter.println(", mergeid: ID in ");
			printWriter.println("select ");

			//Handle initial and final
			printWriter.println("(*---------- Initial node ---------------------*)");
			printWriter.print(this.initial.firstOutgoingFlow().identifier());
			printWriter.print("_begin (?ident1 of ID);");
			printWriter.println(this.getSchedulerString(
					"{}",
					"{ident1}",
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE
			));
			printWriter.println("[]");

			first = true;

			for (String flow : flowSelectStrings)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					printWriter.println();
					printWriter.print("[]");
				}

				printWriter.print(flow);
			}

			printWriter.println();
			printWriter.println("[]");
			printWriter.println();
			printWriter.println("(*----------------- Final node ----------------------*)");
			printWriter.print(this.finals.get(0).firstIncomingFlow().identifier());
			printWriter.print("_finish (?ident1 of ID);");
			printWriter.println(this.getSchedulerString(
					"{ident1}",
					"{}",
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE
			));
			printWriter.println("[]");
			printWriter.println(" mergeid := any ID where member(mergeid, mergestore);");
			printWriter.println("if (is_merge_possible_v2(bpmn,activeflows,mergeid) and is_sync_done(bpmn, " +
					"activeflows, syncstore, mergeid)) then");
			printWriter.print("MoveOn(mergeid);");

			if (!incJoinBeginList.isEmpty())
			{
				printWriter.println("select ");
				printWriter.print(PyToJavaUtils.join(incJoinBeginList, "[]\n"));
				printWriter.println("end select ");
			}
			else
			{
				printWriter.print(this.getSchedulerString(
						"{}",
						"{}",
						SYNC_STORE,
						MERGE_STORE,
						PAR_STORE
				));
			}

			printWriter.println("else ");
			printWriter.println();
			printWriter.print("scheduler [");
			printWriter.print(this.getFlowMsgs(false));
			ArrayList<String> res = this.computeAddSynchroPoints(false);

			if (!res.isEmpty())
			{
				printWriter.print(",");
				printWriter.print(PyToJavaUtils.join(res, ","));
			}

			printWriter.println(", MoveOn] (activeflows, bpmn, syncstore, mergestore, parstore)");
			printWriter.println("end if");

			//Outflow of parallel merge
			printWriter.println();
			printWriter.println("[]");
			printWriter.println(" mergeid := any ID where member(mergeid, parstore);");
			printWriter.println(("if (is_merge_possible_par(bpmn,syncstore,mergeid)) then "));

			if (!parJoinBeginList.isEmpty())
			{
				printWriter.println("select ");
				printWriter.print(PyToJavaUtils.join(parJoinBeginList, "[]\n"));
				printWriter.print("end select ");
			}
			else
			{
				printWriter.print(this.getSchedulerString(
						"{}",
						"{}",
						SYNC_STORE,
						MERGE_STORE,
						PAR_STORE
				));
			}

			printWriter.println("else ");
			printWriter.println();
			printWriter.print("scheduler [");
			printWriter.print(this.getFlowMsgs(false));
			res = this.computeAddSynchroPoints(false);

			if (!res.isEmpty())
			{
				printWriter.print(",");
				printWriter.print(PyToJavaUtils.join(res, ","));
			}

			printWriter.println(", MoveOn] (activeflows, bpmn, syncstore, mergestore, parstore)");
			printWriter.println("end if");
			printWriter.println("end select");
			printWriter.println("end var");
			printWriter.println("end process");
			printWriter.println();
		}

		String getSchedulerString(final String incIds,
								  final String outIds,
								  final String syncString,
								  final String mergeStoreString,
								  final String parStoreString)
		{
			final ArrayList<String> schedulerString = new ArrayList<>();
			schedulerString.add("scheduler [...]");
			schedulerString.add("(union(" + outIds + ", remove_ids_from_set(" + incIds + ", activeflows)), bpmn, " +
					syncString + ", " + mergeStoreString + ", " + parStoreString + ")\n");

			return PyToJavaUtils.join(schedulerString, "");
		}

		/**
		 * Generates file with process element ids
		 */
		void generateIdFile()
		{
			final String fileName = "id.lnt";
			final File file = new File(outputFolder + File.separator + fileName);
			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(file);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			//Generates an ID type for all identifiers
			printWriter.println("module id with get, <, == is");
			printWriter.println();
			printWriter.println("(* Data type for identifiers, useful for scheduling purposes *)");
			printWriter.println("type ID is");
			printWriter.print(this.name);

			for (Node node : this.nodes)
			{
				printWriter.println(",");
				printWriter.print(node.identifier());
			}

			printWriter.println(",");
			printWriter.print(this.initial.identifier());

			for (Node node : this.finals)
			{
				printWriter.println(",");
				printWriter.print(node.identifier());
			}

			for (Flow flow : this.flows)
			{
				printWriter.println(", ");
				printWriter.print(flow.identifier());
			}

			printWriter.println(", DummyId");
			printWriter.println("with ==, !=");
			printWriter.println("end type");
			printWriter.println();
			printWriter.println("end module");

			printWriter.flush();
			printWriter.close();
		}

		//Generates an LNT module and process for a BPMN 2.0 process
		void genLNT()
		{
			this.genLNT("");
		}

		//Generates an LNT module and process for a BPMN 2.0 process
		void genLNT(final String name)
		{
			if (!isBalanced)
			{
				this.generateIdFile();
			}

			final String fileName;

			if (name.isEmpty())
			{
				fileName = this.name + LNT_SUFFIX;
			}
			else
			{
				fileName = name + LNT_SUFFIX;
			}

			final File file = new File(outputFolder + File.separator + fileName);
			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(file);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printWriter.print("module ");
			printWriter.print(this.name);
			printWriter.print(isBalanced ? "" : "(bpmntypes)");
			printWriter.println(" with get, <, == is");
			printWriter.println();

			if (isBalanced)
			{
				//Generates an ID type for all flow identifiers
				printWriter.println("(* Data type for flow identifiers, useful for scheduling purposes *)");
				printWriter.println("type ID is");
				int counter = this.flows.size();

				for (Flow f : this.flows)
				{
					printWriter.print(f.identifier());
					counter--;

					if (counter > 0)
					{
						printWriter.print(",");
					}
				}

				printWriter.println();
				printWriter.println("with ==, !=");
				printWriter.println("end type");
				printWriter.println();
			}

			if (this.initial != null)
			{
				this.initial.writeLnt(printWriter);
			}

			//Generates one process for final events and events, this is enough because generic processes
			if (!this.finals.isEmpty())
			{
				this.finals.get(0).writeLnt(printWriter);
			}

			if (!this.flows.isEmpty())
			{
				this.flows.get(0).writeLnt(printWriter); //TODO: ConditionalFlow?
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
								n.writeLnt(printWriter);
							}
						}
						else
						{
							specialNodes.add(n.getClass().getName());
							n.writeLnt(printWriter);
						}
					}
				}
				else
				{
					n.writeLnt(printWriter);
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
				this.generateScheduler(printWriter);

				//Generate process
				this.processDump(printWriter);
			}

			printWriter.println();
			printWriter.print("process MAIN ");
			final ArrayList<String> alpha = this.alpha();
			dumpAlphabet(alpha, printWriter, true);
			printWriter.println(" is");
			printWriter.println();
			//Computes additional synchros for or splits/joins
			final ArrayList<String> synchroPoints = isBalanced ? this.computeAddSynchroPoints() : this.computeAddSynchroPoints(false);
			final int nbSync = synchroPoints.size();
			printWriter.print(" hide begin:any, finish:any");
			final int nbFlows = this.flows.size();

			if (isBalanced)
			{
				if (nbFlows > 0)
				{
					printWriter.print(", ");
					int cter = 1;

					for (Flow f : this.flows)
					{
						printWriter.print(f.identifier());
						printWriter.print("_begin:any, ");
						printWriter.print(f.identifier());
						printWriter.print("_finish:any");
						cter++;

						if (cter <= nbFlows)
						{
							printWriter.print(", ");
							//we hide additional synchros for or splits/joins as well
						}
					}

					int nb = 0;

					if (nbSync > 0)
					{
						printWriter.print(", ");

						for (String s : synchroPoints)
						{
							printWriter.print(s);
							printWriter.print(":any");
							nb++;

							if (nb < nbSync)
							{
								printWriter.print(", ");
							}
						}
					}
				}

				printWriter.println(" in");
				printWriter.print("par ");
				//Synchronizations on all begin/finish flows

				if (nbFlows > 0)
				{
					int cter = 1;

					for (Flow f : this.flows)
					{
						printWriter.print(f.identifier());
						printWriter.print("_begin, ");
						printWriter.print(f.identifier());
						printWriter.print("_finish");
						cter++;

						if (cter <= nbFlows)
						{
							printWriter.print(", ");
						}
					}
				}
			}
			else
			{
				if (nbFlows > 0)
				{
					printWriter.print(", ");
					printWriter.print(this.getFlowMsgs(true));
					//We hide additional synchros for or splits/joins as well
					int nb = 0;

					if (nbSync > 0)
					{
						printWriter.print(", ");

						for (String synchroPoint : synchroPoints)
						{
							printWriter.print(synchroPoint);
							printWriter.print(":any");
							nb++;

							if (nb < nbSync)
							{
								printWriter.print(", ");
							}
						}
					}
				}

				printWriter.print(", MoveOn:any ");
				printWriter.println(" in");

				//We start with the scheduler
				printWriter.print("par MoveOn, ");
				//Synchronization on all begin/finish flows

				if (nbFlows > 0)
				{
					printWriter.print(this.getFlowMsgs(false));
				}

				if (!synchroPoints.isEmpty())
				{
					printWriter.print(",");
					printWriter.print(PyToJavaUtils.join(synchroPoints, ","));
				}

				printWriter.println(" in");
				printWriter.println("  (* we first generate the scheduler, necessary for keeping track of tokens, and" +
						" triggering inclusive merge gateways *)");
				printWriter.println("    scheduler [...](nil, p1(), nil, nil, nil) ");
				printWriter.println("||");
				printWriter.print("par   ");
				printWriter.println(" (* synchronizations on all begin/finish flow messages *)");

				//Synchronizations on all begin/finish flows
				if (nbFlows > 0)
				{
					printWriter.print(this.getFlowMsgs(false));
				}
			}

			printWriter.println(" in");

			//Interleaving of all flow processes
			printWriter.println(" par  (* we then generate interleaving of all flow processes *)");
			int cter = 1;

			for (Flow f : this.flows)
			{
				//TODO: take conditional flows into account
				printWriter.print("flow [");
				printWriter.print(f.identifier());
				printWriter.print("_begin, ");
				printWriter.print(f.identifier());
				printWriter.print("_finish] (");
				printWriter.print(f.identifier());
				printWriter.print(")");
				cter++;

				if (cter <= nbFlows)
				{
					printWriter.print(" || ");
				}
			}

			printWriter.println();
			printWriter.println(" end par ");
			printWriter.println();
			printWriter.println("||");

			//Interleaving of all node processes
			printWriter.println(" par     (* we finally generate interleaving of all node processes *)");

			//Process instantiation for initial node
			printWriter.print("init [begin,");
			printWriter.print(this.initial.outgoingFlows().get(0).identifier());
			printWriter.print("_begin] || "); //We assume a single output flow
			final int nbFinals = this.finals.size();
			cter = 1;

			//Processes instantiations for final nodes
			for (Node n : this.finals)
			{
				printWriter.print("final [");
				printWriter.print(n.incomingFlows().get(0).identifier());
				printWriter.print("_finish, finish]"); //We assume a single incoming flow
				cter++;

				if (cter <= nbFlows)
				{
					printWriter.print(" || ");
				}
			}

			//Processes instantiations for all other nodes
			final int nbNodes = this.nodes.size();
			cter = 1;

			for (Node n : this.nodes)
			{
				n.writeMainLnt(printWriter);
				cter++;

				if (cter <= nbNodes)
				{
					printWriter.print(" || ");
				}
			}

			printWriter.println();
			printWriter.println(" end par ");
			printWriter.println(isBalanced ? "" : " end par");
			printWriter.println(" end par");
			printWriter.println(" end hide");
			printWriter.println();
			printWriter.println("end process");
			printWriter.println();
			printWriter.println("end module");

			printWriter.flush();
			printWriter.close();
		}

		/**
		 * Generates an SVL file
		 */
		void genSVL()
		{
			this.genSVL(true);
		}

		/**
		 * Generates an SVL file
		 */
		void genSVL(final boolean smartReduction)
		{
			final String fileName = this.name + ".svl";
			final File svlFile = new File(outputFolder + File.separator + fileName);

			//System.out.println("Absolute path: " + svlFile.getAbsolutePath());
			//System.out.println("Working Directory = " + System.getProperty("user.dir"));

			final PrintWriter printWriter;

			try
			{
				printWriter = new PrintWriter(svlFile);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printWriter.println("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"");
			printWriter.println(isBalanced ? "% CAESAR_OPTIONS=\"-more cat\"" : "% CAESAR_OPTIONS=\"-more cat -gc\"");
			printWriter.println();
			printWriter.print("% DEFAULT_PROCESS_FILE=");
			printWriter.print(this.name);
			printWriter.println(".lnt");
			printWriter.println();
			// generation of the raw bcg
			printWriter.print("\"");
			printWriter.print(this.name);
			printWriter.print("_raw.bcg\" = generation of \"MAIN");
			final ArrayList<String> alpha = this.alpha();
			dumpAlphabet(alpha, printWriter, false);
			printWriter.println("\";");
			printWriter.println();
			//reduction of the raw bcg
			printWriter.print("\"");
			printWriter.print(this.name);
			printWriter.print(".bcg\" = branching reduction of \"");
			printWriter.print(this.name);
			printWriter.println("_raw.bcg\";");
			printWriter.println();

			printWriter.flush();
			printWriter.close();

			if (!svlFile.setExecutable(true))
			{
				throw new IllegalStateException("Unable to make the SVL script executable. Please check your rights" +
						" on the current working directory.");
			}
		}

		/**
		 * This method takes as input a file.pif and generates a PIF Python object
		 *
		 * @param filename
		 */
		void buildProcessFromFile(final String filename)
		{
			this.buildProcessFromFile(filename, false);
		}

		/**
		 * This method takes as input a file.pif and generates a PIF Python object
		 *
		 * @param filename
		 * @param debug
		 */
		void buildProcessFromFile(final String filename,
								  final boolean debug)
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
	 * @return (Integer, String, Collection<String>), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName)
	{
		return this.generate(pifFileName, true, !isBalanced);
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection<String>), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName,
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
				return Triple.of(ReturnCodes.TERMINATION_ERROR, pifModelName, process.alpha());
			}
		}

		//Generate the LNT code for the model
		process.genLNT();
		//Compute the LTS from the LNT code using SVL, possibly with a smart reduction
		process.genSVL(smartReduction);

		try
		{
			final java.lang.Process svlCommand = Runtime.getRuntime().exec(
					"svl " + pifModelName, null, new File(outputFolder)
			);
			final int exitValue2 = svlCommand.waitFor();
		}
		catch (IOException | InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		return Triple.of(ReturnCodes.TERMINATION_OK, pifModelName, process.alpha()); //TODO use return value from SVL call
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection<String>), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> load(final String pifFileName)
	{
		return this.load(pifFileName, true, false);
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection<String>), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	@Override
	public Triple<Integer, String, Collection<String>> load(final String pifFileName,
															final boolean smartReduction,
															final boolean debug)
	{
		final Process process = new Process();
		process.buildProcessFromFile(pifFileName);
		final String pifModelName = process.name();
		final String ltsFileName = process.name() + LTS_SUFFIX;

		if (this.needsRebuild(pifFileName, ltsFileName))
		{
			return this.generate(pifFileName, smartReduction, debug);
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
