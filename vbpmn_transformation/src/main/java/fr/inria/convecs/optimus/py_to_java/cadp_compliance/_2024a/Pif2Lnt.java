package fr.inria.convecs.optimus.py_to_java.cadp_compliance._2024a;

import fr.inria.convecs.optimus.pif.Peer;
import fr.inria.convecs.optimus.pif.SequenceFlow;
import fr.inria.convecs.optimus.pif.WorkflowNode;
import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.Pif2LntGeneric;
import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.util.CommandManager;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
	 * @param stringBuilder is the stringBuilder representing the file to where the alphabet should be dumped
	 * @param addAny is a boolean indicating whether to add "any" or not
	 */
	public void dumpAlphabet(final ArrayList<String> alphabet,
							 final StringBuilder stringBuilder,
							 final boolean addAny)
	{
		final int nbElem = alphabet.size();

		if (nbElem > 0)
		{
			stringBuilder.append("[");
			int counter = 1;

			for (String element : alphabet)
			{
				stringBuilder.append(element);

				if (addAny)
				{
					stringBuilder.append(":any");
				}

				counter++;

				if (counter <= nbElem)
				{
					stringBuilder.append(", ");
				}
			}

			stringBuilder.append("]");
			//stringBuilder.close();
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

		abstract void writeMainLnt(final StringBuilder stringBuilder);
		abstract void processLnt(final StringBuilder stringBuilder);
		abstract void writeLnt(final StringBuilder stringBuilder);
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
			stringBuilder.append("process flow [begin:any, finish:any] (ident:ID) is\n");
			stringBuilder.append(" loop begin (ident) ; finish (ident) end loop\n");
			stringBuilder.append("end process\n\n");
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

		void processLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("flow(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			stringBuilder.append(this.source.identifier());
			stringBuilder.append(",");
			stringBuilder.append(this.target.identifier());
			stringBuilder.append(")");
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
		void writeLnt(final StringBuilder stringBuilder)
		{
			//TODO: Translate the condition too
			stringBuilder.append("process conditionalflow [begin:any, finish:any] (ident: ID) is\n");
			stringBuilder.append(" loop begin (ident) ; finish (ident) end loop\n");
			stringBuilder.append("end process\n");
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
		void writeMainLnt(final StringBuilder stringBuilder)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on InitialEvent!");
		}

		//Generates the (generic) process for the initial event, only once
		@Override
		void writeLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("process init [begin:any, outf:any] is\n")
					.append(" var ident: ID in begin ; outf (?ident of ID) end var \n")
					.append("end process\n\n");
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

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("initial(")
					.append(this.identifier)
					.append(",")
					.append(this.outgoingFlows.get(0).identifier())
					.append(")");
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
		void writeMainLnt(final StringBuilder stringBuilder)
		{
			throw new NotImplementedException("Method \"writeMainLnt()\" should not be used on EndEvent!");
		}

		//Generates the (generic) process for final events, only once
		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			stringBuilder.append("process final [incf:any, finish:any] is\n");

			if (isBalanced)
			{
				stringBuilder.append(" var ident: ID in incf (?ident of ID); finish end var\n");
			}
			else
			{
				stringBuilder.append("var ident: ID in \n")
						.append("loop \n")
						.append("incf (?ident of ID); finish \n")
						.append("end loop\n")
						.append("end var\n");
			}

			stringBuilder.append("end process\n\n");
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

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("final(")
					.append(this.identifier)
					.append(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("})");
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
		void writeLnt(StringBuilder stringBuilder)
		{
			stringBuilder.append("process interaction [incf:any, inter:any, outf:any] is\n")
					.append(" var ident: ID in loop incf (?ident of ID); inter; outf (?ident of ID) end loop end var \n")
					.append("end process\n");
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			//TODO Vérifier
			this.writeLnt(stringBuilder);
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
		void writeMainLnt(final StringBuilder stringBuilder)
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
		void writeLnt(StringBuilder stringBuilder)
		{
			stringBuilder.append("process messagesending [incf:any, msg:any, outf:any] is\n");
			stringBuilder.append(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var \n");
			stringBuilder.append("end process\n");
		}

		@Override
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder); //TODO vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> res = new ArrayList<>();
			res.add(this.message + "_EM");
			return res;
		}

		void writeMainLnt(final StringBuilder stringBuilder)
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
		void writeLnt(StringBuilder stringBuilder)
		{
			stringBuilder.append("process messagereception [incf:any, msg:any, outf:any] is\n");
			stringBuilder.append(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var\n");
			stringBuilder.append("end process\n");
		}

		@Override
		void processLnt(StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> result = new ArrayList<>();
			result.add(this.message + "_REC");
			return result;
		}

		void writeMainLnt(final StringBuilder stringBuilder)
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
		void processLnt(StringBuilder stringBuilder)
		{
			stringBuilder.append("task(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("},");
			first = true;
			stringBuilder.append("{");

			for (Flow outFlow : this.outgoingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}
				stringBuilder.append(outFlow.identifier());
			}
			stringBuilder.append("})");
		}

		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();

			stringBuilder.append("process task_");
			stringBuilder.append(nbInc);
			stringBuilder.append("_");
			stringBuilder.append(nbOut);
			stringBuilder.append(" [");

			if (nbInc == 1)
			{
				stringBuilder.append("incf:any,");
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					stringBuilder.append("incf");
					stringBuilder.append(incCounter);
					stringBuilder.append(":any,");
					incCounter++;
				}
			}

			stringBuilder.append("task:any,");

			if (nbOut == 1)
			{
				stringBuilder.append("outf:any");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					stringBuilder.append("outf");
					stringBuilder.append(outCounter);
					stringBuilder.append(":any");
					outCounter++;

					if (outCounter < nbOut)
					{
						stringBuilder.append(",");
					}
				}
			}

			stringBuilder.append("] is\n");
			stringBuilder.append(" var ident: ID in loop ");

			if (nbInc == 1)
			{
				stringBuilder.append(" incf (?ident of ID); ");
			}
			else
			{
				int incCounter = 0;
				stringBuilder.append(" select ");

				while (incCounter < nbInc)
				{
					stringBuilder.append("incf");
					stringBuilder.append(incCounter);
					stringBuilder.append(" (?ident of ID)");
					incCounter++;

					if (incCounter < nbInc)
					{
						stringBuilder.append(" [] ");
					}
				}

				stringBuilder.append(" end select ; \n");
			}

			stringBuilder.append("task ; ");

			if (nbOut == 1)
			{
				stringBuilder.append(" outf (?ident of ID)");
			}
			else
			{
				int outCounter = 0;
				stringBuilder.append(" select ");

				while (outCounter < nbOut)
				{
					stringBuilder.append("outf");
					stringBuilder.append(outCounter);
					stringBuilder.append(" (?ident of ID)");
					outCounter++;

					if (outCounter < nbOut)
					{
						stringBuilder.append(" [] ");
					}
				}

				stringBuilder.append(" end select \n");
			}

			stringBuilder.append(" end loop end var\n");
			stringBuilder.append("end process\n\n");
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
		void writeMainLnt(final StringBuilder stringBuilder)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();

			stringBuilder.append(" task_");
			stringBuilder.append(nbInc);
			stringBuilder.append("_");
			stringBuilder.append(nbOut);
			stringBuilder.append(" [");

			if (nbInc == 1)
			{
				stringBuilder.append(this.incomingFlows.get(0).identifier());
				stringBuilder.append("_finish,");
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					stringBuilder.append(this.incomingFlows.get(incCounter).identifier());
					stringBuilder.append("_finish,");
					incCounter++;
				}
			}

			stringBuilder.append(this.identifier);
			stringBuilder.append(",");

			if (nbOut == 1)
			{
				//TODO VOIR SI ON A VRAIMENT BESOIN DE DIFFÉRENCIER CES CAS
				stringBuilder.append(this.outgoingFlows.get(0).identifier());
				stringBuilder.append("_begin");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					stringBuilder.append(this.outgoingFlows.get(outCounter).identifier());
					stringBuilder.append("_begin");
					outCounter++;

					if (outCounter < nbOut)
					{
						stringBuilder.append(",");
					}
				}
			}

			stringBuilder.append("] ");
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			final Random random = new Random();
			final int randomInt = random.nextInt(51);
			stringBuilder.append("        task(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",\"");
			stringBuilder.append(this.identifier);
			stringBuilder.append("\",");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append(",");
			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append(",");
			stringBuilder.append(randomInt);
			stringBuilder.append(")");
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
			stringBuilder.append("gateway(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			stringBuilder.append(pattern);
			stringBuilder.append(",");
			stringBuilder.append(type);
			stringBuilder.append(",{");
			boolean first = true;

			for (Flow inFlow : this.incomingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				stringBuilder.append(inFlow.identifier());
			}

			stringBuilder.append("},{");
			first = true;

			for (Flow outFlow : this.outgoingFlows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}
				stringBuilder.append(outFlow.identifier());
			}

			stringBuilder.append("})");
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
		 * @param stringBuilder
		 */
		void writeMainLnt(final StringBuilder stringBuilder)
		{
			final int nbOut = this.outgoingFlows.size();
			int i = 0;

			stringBuilder.append("[");
			stringBuilder.append(this.incomingFlows.get(0).identifier());
			stringBuilder.append("_finish,");

			while (i < nbOut)
			{
				stringBuilder.append(this.outgoingFlows.get(i).identifier());
				stringBuilder.append("_begin");
				i++;

				if (i < nbOut)
				{
					stringBuilder.append(",");
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

		void dumpMaude(final StringBuilder stringBuilder,
					   final String nameOp)
		{
			stringBuilder.append("        split(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			stringBuilder.append(nameOp);
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
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		@Override
		void writeLnt(StringBuilder stringBuilder)
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

			stringBuilder.append("process orsplit_");
			stringBuilder.append(this.identifier);
			stringBuilder.append(" [incf:any,");

			//We dump the process alphabet (flows + synchronization points if necessary)
			int nbg = 1;

			while (nbg <= nbOut)
			{
				stringBuilder.append("outf_");
				stringBuilder.append(nbg);
				stringBuilder.append(":any");
				nbg++;

				if (nbg <= nbOut)
				{
					stringBuilder.append(",");
				}
			}

			if (nbt > 0
					&& (!isBalanced || !this.correspOrJoin.isEmpty()))
			{
				stringBuilder.append(", ");
				int counter = 1;

				for (Collection<String> combi : allCombi) //TODO Bizarre ....
				{
					stringBuilder.append(isBalanced ? this.correspOrJoin : this.identifier);
					stringBuilder.append("_");
					stringBuilder.append(counter);
					stringBuilder.append(":any");
					counter++;

					if (counter <= nbt)
					{
						stringBuilder.append(",");
					}
				}
			}

			stringBuilder.append(" ] is \n");
			int counterVar = allCombi.size();
			stringBuilder.append(" var ");

			while (counterVar > 0)
			{
				stringBuilder.append("ident");
				stringBuilder.append(counterVar);
				stringBuilder.append(":ID");
				counterVar--;

				if (counterVar > 0)
				{
					stringBuilder.append(",");
				}
			}

			if (isBalanced)
			{
				stringBuilder.append(" in  var ident: ID in loop incf (?ident of ID); \n"); //TODO We generate unnecessary variables...
				stringBuilder.append(" select ");
			}
			else
			{
				stringBuilder.append(" in \n");
				stringBuilder.append("var ident: ID in loop \n");
				stringBuilder.append("incf (?ident of ID); \n"); //TODO We generate unnecessary variables...
				stringBuilder.append("select ");
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
					stringBuilder.append("\n");
					stringBuilder.append(this.identifier);
					stringBuilder.append("_");
					stringBuilder.append(counter);
					stringBuilder.append("; ");
					counter++;
				}

				if (nbElem > 1)
				{
					counterVar = allCombi.size();

					if (isBalanced)
					{
						stringBuilder.append(" par ");
					}
					else
					{
						stringBuilder.append("\npar\n");
					}

					for (String s : element)
					{
						stringBuilder.append(s);
						stringBuilder.append(" (?ident");
						stringBuilder.append(counterVar);
						stringBuilder.append(" of ID)");
						counterVar--;
						nb2++;

						if (nb2 <= nbElem)
						{
							if (isBalanced)
							{
								stringBuilder.append("||");
							}
							else
							{
								stringBuilder.append("\n||\n");
							}
						}
					}

					if (isBalanced)
					{
						stringBuilder.append(" end par ");
					}
					else
					{
						stringBuilder.append("\nend par");
					}
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
						stringBuilder.append(" ; ");
						stringBuilder.append(this.correspOrJoin);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						counter++;
					}
				}

				nb++;

				if (nb <= nbt)
				{
					if (isBalanced)
					{
						stringBuilder.append(" [] ");
					}
					else
					{
						stringBuilder.append("\n[] ");
					}
				}
			}

			if (isBalanced)
			{
				stringBuilder.append(" end select end loop end var end var\n");
				stringBuilder.append("end process\n");
			}
			else
			{
				stringBuilder.append("\nend select \n");
				stringBuilder.append("end loop \n");
				stringBuilder.append("end var\n");
				stringBuilder.append("end var\n");
				stringBuilder.append("end process\n\n");
			}
		}

		/**
		 * Generates process instantiation for main LNT process.
		 *
		 * @param stringBuilder
		 */
		void writeMainLnt(final StringBuilder stringBuilder)
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
							stringBuilder.append(this.correspOrJoin);
							stringBuilder.append("_");
							stringBuilder.append(counter);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(",");
							}
						}

						stringBuilder.append(" -> ");
					}
				}

				//Process call + alphabet
				stringBuilder.append("orsplit_");
				stringBuilder.append(this.identifier);
				stringBuilder.append("[");
				stringBuilder.append(this.incomingFlows.get(0).identifier());
				stringBuilder.append("_finish,");
				int i = 0;

				while (i < nbOut)
				{
					stringBuilder.append(this.outgoingFlows.get(i).identifier());
					stringBuilder.append("_begin");
					i++;

					if (i < nbOut)
					{
						stringBuilder.append(",");
					}
				}

				if (nbCombi > 0)
				{
					stringBuilder.append(", ");
					int counter = 1;

					for (ArrayList<String> combination : allCombinations)
					{
						stringBuilder.append(isBalanced ? this.correspOrJoin : this.identifier);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						counter++;

						if (counter <= nbCombi)
						{
							stringBuilder.append(",");
						}
					}
				}

				stringBuilder.append("]");
			}
			else
			{
				stringBuilder.append("orsplit_");
				stringBuilder.append(this.identifier);
				super.writeMainLnt(stringBuilder);
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

		public void dumpMaude(final StringBuilder stringBuilder)
		{
			if (isBalanced)
			{
				super.dumpMaude(stringBuilder, "inclusive");
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
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param stringBuilder
		 */
		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			final int nbOut = this.outgoingFlows.size();
			stringBuilder.append("process xorsplit_");
			stringBuilder.append(this.identifier);
			stringBuilder.append(" [incf:any,");
			int nb = 1;

			while (nb <= nbOut)
			{
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append(":any");
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append(",");
				}
			}

			stringBuilder.append(" ] is \n");
			stringBuilder.append(" var ident: ID in loop incf (?ident of ID); \n");
			stringBuilder.append(" select ");
			nb = 1;

			while (nb <= nbOut)
			{
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append("(?ident of ID)");
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append("[]");
				}
			}

			stringBuilder.append(" end select end loop end var\n");
			stringBuilder.append("end process\n\n");
		}

		void writeMainLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("xorsplit_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			if (isBalanced)
			{
				super.dumpMaude(stringBuilder, "exclusive");
			}
			else
			{
				stringBuilder.append("        split(");
				stringBuilder.append(this.identifier);
				stringBuilder.append(",exclusive,");
				stringBuilder.append(this.incomingFlows.get(0).identifier());
				stringBuilder.append(",(");
				int counter = this.outgoingFlows.size();
				double proba = ((1d / (double) counter) * 1000.0) / 1000.0;

				for (Flow outFlow : this.outgoingFlows)
				{
					counter--;
					stringBuilder.append("(");
					stringBuilder.append(outFlow.identifier());
					stringBuilder.append(",");
					stringBuilder.append(proba);
					stringBuilder.append(")");

					if (counter > 0)
					{
						stringBuilder.append(" ");
					}
				}

				stringBuilder.append("))");
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
		void processLnt(final StringBuilder stringBuilder)
		{
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel split gateway.
		 * Takes as input the number of outgoing flows.
		 *
		 * @param stringBuilder
		 */
		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			final int nbOut = this.outgoingFlows.size();
			stringBuilder.append("process andsplit_");
			stringBuilder.append(this.identifier);
			stringBuilder.append(" [incf:any,");
			int nb = 1;

			while (nb <= nbOut)
			{
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append(":any");
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append(",");
				}
			}

			stringBuilder.append(" ] is \n");
			int variablesCounter = nbOut;
			stringBuilder.append(" var ");

			while (variablesCounter > 0)
			{
				stringBuilder.append("ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(":ID");
				variablesCounter--;

				if (variablesCounter > 0)
				{
					stringBuilder.append(",");
				}
			}

			stringBuilder.append(" in  var ident: ID in loop incf (?ident of ID); \n");
			stringBuilder.append(" par ");
			nb = 1;
			variablesCounter = nbOut;

			while (nb <= nbOut)
			{
				stringBuilder.append("outf_");
				stringBuilder.append(nb);
				stringBuilder.append("(?ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbOut)
				{
					stringBuilder.append("||");
				}
			}

			stringBuilder.append(" end par end loop end var end var\n");
			stringBuilder.append("end process\n\n");
		}

		void writeMainLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("andsplit_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			super.dumpMaude(stringBuilder, "parallel");
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
		 * @param stringBuilder
		 */
		void writeMainLnt(final StringBuilder stringBuilder)
		{
			//We assume one outgoing flow
			final int nbInc = this.incomingFlows.size();
			stringBuilder.append("[");
			int i = 0;

			while (i < nbInc)
			{
				stringBuilder.append(this.incomingFlows.get(i).identifier());
				stringBuilder.append("_finish");
				i++;
				stringBuilder.append(",");
			}

			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append("_begin]");
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
		 * @param stringBuilder
		 */
		void dumpMaude(final StringBuilder stringBuilder,
					   final String nameOp)
		{
			stringBuilder.append("        merge(");
			stringBuilder.append(this.identifier);
			stringBuilder.append(",");
			stringBuilder.append(nameOp);
			stringBuilder.append(",(");
			int nbInc = this.incomingFlows.size();

			for (Flow ofl : this.incomingFlows) //TODO : Bizarre "ofl" VS "incomingFlows"
			{
				nbInc--;
				stringBuilder.append(ofl.identifier());

				if (nbInc > 0)
				{
					stringBuilder.append(",");
				}
			}

			stringBuilder.append("),");
			stringBuilder.append(this.outgoingFlows.get(0).identifier());
			stringBuilder.append(")");
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
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		/**
		 * Generates the process for inclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder
		 */
		@Override
		void writeLnt(StringBuilder stringBuilder)
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

				stringBuilder.append("process orjoin_");
				stringBuilder.append(this.identifier);
				stringBuilder.append(" [");
				nb = 1;

				while (nb <= nbInc)
				{
					stringBuilder.append("incf_");
					stringBuilder.append(nb);
					stringBuilder.append(":any,");
					nb++;
				}

				stringBuilder.append("outf:any ");

				//we add to the alphabet potential additional synchronization points
				if (nbCombi > 0
						&& !this.correspondingOrSplit.isEmpty())
				{
					int counter = 1;
					stringBuilder.append(",");

					for (ArrayList<String> combination : allCombinations)
					{
						stringBuilder.append(this.identifier);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						stringBuilder.append(":any");
						counter++;

						if (counter <= nbCombi)
						{
							stringBuilder.append(",");
						}
					}
				}

				stringBuilder.append("] is \n");
				stringBuilder.append(" var ");
				int variablesCounter = allCombinations.size();

				while (variablesCounter > 0) //TODO: we generate unnecessary variables
				{
					stringBuilder.append("ident");
					stringBuilder.append(variablesCounter);
					stringBuilder.append(":ID");
					variablesCounter--;

					if (variablesCounter > 0)
					{
						stringBuilder.append(",");
					}
				}

				stringBuilder.append(" in  var ident: ID in loop select ");
				nb = 1;
				int counter = 1;

				for (ArrayList<String> combination : allCombinations)
				{
					int nbElem = combination.size();
					int nb2 = 1;

					// add synchronization points if there's a corresponding split
					if (!this.correspondingOrSplit.isEmpty())
					{
						stringBuilder.append(this.identifier);
						stringBuilder.append("_");
						stringBuilder.append(counter);
						stringBuilder.append(";");
						counter++;
					}

					if (nbElem > 1)
					{
						variablesCounter = allCombinations.size();
						stringBuilder.append(" par ");

						for (String element : combination)
						{
							stringBuilder.append(element);
							stringBuilder.append(" (?ident");
							stringBuilder.append(variablesCounter);
							stringBuilder.append(" of ID)");
							variablesCounter--;
							nb2++;

							if (nb2 <= nbElem)
							{
								stringBuilder.append("||");
							}
						}

						stringBuilder.append(" end par ");
					}
					else
					{
						stringBuilder.append(combination.iterator().next());
						stringBuilder.append(" (?ident of ID)");
					}

					nb++;

					if (nb <= nbCombi)
					{
						stringBuilder.append(" [] ");
					}
				}

				stringBuilder.append(" end select ; outf (?ident of ID) end loop end var end var \n");
				stringBuilder.append("end process\n");
			}
			else
			{
				stringBuilder.append("process orjoin_");
				stringBuilder.append(this.identifier);
				stringBuilder.append(" [");
				int nb = 1;

				while (nb <= nbInc)
				{
					stringBuilder.append("incf_");
					stringBuilder.append(nb);
					stringBuilder.append(":any,");
					nb++;
				}

				stringBuilder.append("outf:any, MoveOn:any] (mergeid: ID) is \n");
				stringBuilder.append("var mergestatus:Bool, ident:ID in \n");
				stringBuilder.append(" loop\n");
				stringBuilder.append("mergestatus := False;\n");
				stringBuilder.append("while mergestatus == False loop \n");
				stringBuilder.append("select\n");

				nb = 1;

				while (nb <= nbInc)
				{
					stringBuilder.append("incf_");
					stringBuilder.append(nb);
					stringBuilder.append(" (?ident of ID)");
					nb++;

					if (nb <= nbInc)
					{
						stringBuilder.append("\n[]");
					}
				}

				stringBuilder.append("\n[] MoveOn(mergeid); mergestatus := True\n");
				stringBuilder.append("end select\n");
				stringBuilder.append("end loop;\n");
				stringBuilder.append("outf (?ident of ID)\n");
				stringBuilder.append("end loop\n");
				stringBuilder.append("end var\n");
				stringBuilder.append("end process\n\n");
			}
		}

		//Generates process instantiation for main LNT process
		void writeMainLnt(final StringBuilder stringBuilder)
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
							stringBuilder.append(this.identifier);
							stringBuilder.append("_");
							stringBuilder.append(counter);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(",");
							}
						}

						stringBuilder.append(" -> ");
					}

					//Process call + alphabet
					stringBuilder.append("orjoin_");
					stringBuilder.append(this.identifier);
					stringBuilder.append("[");
					int i = 0;

					while (i < nbInc)
					{
						stringBuilder.append(this.incomingFlows.get(i).identifier());
						stringBuilder.append("_finish,");
						i++;
					}

					stringBuilder.append(this.outgoingFlows.get(0).identifier());
					stringBuilder.append("_begin");

					if (nbCombi > 0)
					{
						int counter = 1;
						stringBuilder.append(",");

						for (ArrayList<String> combination : allCombinations)
						{
							stringBuilder.append(this.identifier);
							stringBuilder.append("_");
							stringBuilder.append(counter);
							counter++;

							if (counter <= nbCombi)
							{
								stringBuilder.append(",");
							}
						}
					}

					stringBuilder.append("]");
				}
				else
				{
					stringBuilder.append("orjoin_");
					stringBuilder.append(this.identifier);
					super.writeMainLnt(stringBuilder);
				}
			}
			else
			{
				stringBuilder.append("orjoin_");
				stringBuilder.append(this.identifier);
				//We assume one outgoing flow
				final int nbInc = this.incomingFlows.size();
				stringBuilder.append("[");
				int i = 0;

				while (i < nbInc)
				{
					stringBuilder.append(this.incomingFlows.get(i).identifier());
					stringBuilder.append("_finish,");
					i++;
				}

				stringBuilder.append(this.outgoingFlows.get(0).identifier());
				stringBuilder.append("_begin, MoveOn] (");
				stringBuilder.append(this.identifier);
				stringBuilder.append(")");
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

		void dumpMaude(final StringBuilder stringBuilder)
		{
			super.dumpMaude(stringBuilder, "inclusive");
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
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		/**
		 * Generates the process for exclusive join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder
		 */
		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			final int nbInc = this.incomingFlows.size();
			stringBuilder.append("process xorjoin_");
			stringBuilder.append(this.identifier);
			stringBuilder.append(" [");
			int nb = 1;

			while (nb <= nbInc)
			{
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(":any,");
				nb++;
			}

			stringBuilder.append("outf:any] is \n");
			stringBuilder.append(" var ident: ID in loop select ");
			nb = 1;

			while (nb <= nbInc)
			{
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident of ID)");
				nb++;

				if (nb <= nbInc)
				{
					stringBuilder.append("[]");
				}
			}

			stringBuilder.append(" end select ; outf (?ident of ID) end loop end var \n");
			stringBuilder.append("end process\n\n");
		}

		void writeMainLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("xorjoin_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			super.dumpMaude(stringBuilder, "exclusive");
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
			this.writeLnt(stringBuilder); //TODO Vérifier
		}

		/**
		 * Generates the process for parallel join gateway.
		 * Takes as input the number of incoming flows.
		 *
		 * @param stringBuilder
		 */
		@Override
		void writeLnt(StringBuilder stringBuilder)
		{
			final int nbInc = this.incomingFlows.size();
			stringBuilder.append("process andjoin_");
			stringBuilder.append(this.identifier);
			stringBuilder.append(" [");
			int nb = 1;

			while (nb <= nbInc)
			{
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(":any,");
				nb++;
			}

			stringBuilder.append("outf:any] is \n");
			int variablesCounter = nbInc;
			stringBuilder.append(" var ");

			while (variablesCounter > 0)
			{
				stringBuilder.append("ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(":ID");
				variablesCounter--;

				if (variablesCounter > 0)
				{
					stringBuilder.append(",");
				}
			}

			stringBuilder.append(" in  var ident:ID in loop par ");
			nb = 1;
			variablesCounter = nbInc;

			while (nb <= nbInc)
			{
				stringBuilder.append("incf_");
				stringBuilder.append(nb);
				stringBuilder.append(" (?ident");
				stringBuilder.append(variablesCounter);
				stringBuilder.append(" of ID)");
				variablesCounter--;
				nb++;

				if (nb <= nbInc)
				{
					stringBuilder.append("||");
				}
			}

			stringBuilder.append(" end par ; outf (?ident of ID) end loop end var end var \n");
			stringBuilder.append("end process\n\n");
		}

		void writeMainLnt(final StringBuilder stringBuilder)
		{
			stringBuilder.append("andjoin_");
			stringBuilder.append(this.identifier);
			super.writeMainLnt(stringBuilder);
		}

		ArrayList<Pair<String, Integer>> reachableOrJoin(final ArrayList<Pair<String, Integer>> visited,
														 final int depth)
		{
			return super.reachableOrJoin(visited, depth);
		}

		void dumpMaude(final StringBuilder stringBuilder)
		{
			super.dumpMaude(stringBuilder, "parallel");
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
		void dumpFlowsMsgs(final StringBuilder stringBuilder,
						   final boolean withAny)
		{
			final int nbFlows = this.flows.size();
			int counter = 1;

			for (Flow fl : this.flows)
			{
				stringBuilder.append(fl.identifier());
				stringBuilder.append("_begin");

				if (withAny)
				{
					stringBuilder.append(":any");
				}

				stringBuilder.append(", ");
				stringBuilder.append(fl.identifier());
				stringBuilder.append("_finish");

				if (withAny)
				{
					stringBuilder.append(":any");
				}

				counter++;

				if (counter <= nbFlows)
				{
					stringBuilder.append(", ");
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

		void processDump(final StringBuilder stringBuilder)
		{
			stringBuilder.append("\nfunction p1(): BPROCESS is \n\n");
			stringBuilder.append(" return proc ( \n");
			stringBuilder.append(this.name);
			stringBuilder.append(",\n");
			stringBuilder.append("{\n");
			stringBuilder.append("\ti ( ");
			this.initial.processLnt(stringBuilder);
			stringBuilder.append(" ),\n");

			//handle final
			boolean first = true;
			stringBuilder.append("\tf ( { ");

			for (Node fnode : this.finals)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				fnode.processLnt(stringBuilder);
			}

			stringBuilder.append(" } ),\n");

			//TODO: eliminate iterating twice / Separate printer class?
			//handle tasks
			stringBuilder.append("\tt ( { ");
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
						stringBuilder.append(",");
					}

					pNode.processLnt(stringBuilder);
				}
			}

			stringBuilder.append(" } ), \n");

			//handle gateways
			stringBuilder.append("\tg ( { ");
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
						stringBuilder.append(",");
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

			stringBuilder.append(" } )\n");
			stringBuilder.append("},\n");
			//flows
			stringBuilder.append("{ \n");
			first = true;

			for (Flow flow : this.flows)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				flow.processLnt(stringBuilder);
			}

			stringBuilder.append("\n}\n");
			stringBuilder.append(")\n");
			stringBuilder.append("end function\n\n");
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
			stringBuilder.append("\nprocess scheduler [");
			stringBuilder.append(this.getFlowMsgs(true));
			//Add split synchro params
			final ArrayList<String> synchroParams = this.computeAddSynchroPoints(true);

			if (!synchroParams.isEmpty())
			{
				stringBuilder.append(",");

				String separator = "";

				for (String synchroParam : synchroParams)
				{
					stringBuilder.append(separator);
					stringBuilder.append(synchroParam);
					separator = ",";
				}
			}

			//This parameter stores the set of active flows/tokens
			stringBuilder.append(", MoveOn:any] (activeflows: IDS, bpmn: BPROCESS, syncstore: IDS, mergestore:IDS, " +
					"parstore:IDS) is\n");

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
			stringBuilder.append("var ");
			boolean first = true;

			for (String ident : new HashSet<>(identSet)) //TODO Intéret ? Randomiser ?
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(",");
				}

				stringBuilder.append(ident);
				stringBuilder.append(": ID");
			}

			stringBuilder.append(", mergeid: ID in \n");
			stringBuilder.append("select \n");

			//Handle initial and final
			stringBuilder.append("(*---------- Initial node ---------------------*)\n");
			stringBuilder.append(this.initial.firstOutgoingFlow().identifier());
			stringBuilder.append("_begin (?ident1 of ID);");
			stringBuilder.append(this.getSchedulerString(
					"{}",
					"{ident1}",
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE
			));
			stringBuilder.append("\n[]\n");

			first = true;

			for (String flow : flowSelectStrings)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append("\n[]");
				}

				stringBuilder.append(flow);
			}

			stringBuilder.append("\n[]\n\n");
			stringBuilder.append("(*----------------- Final node ----------------------*)\n");
			stringBuilder.append(this.finals.get(0).firstIncomingFlow().identifier());
			stringBuilder.append("_finish (?ident1 of ID);");
			stringBuilder.append(this.getSchedulerString(
					"{ident1}",
					"{}",
					SYNC_STORE,
					MERGE_STORE,
					PAR_STORE
			));
			stringBuilder.append("\n[]\n");
			stringBuilder.append(" mergeid := any ID where member(mergeid, mergestore);\n");
			stringBuilder.append("if (is_merge_possible_v2(bpmn,activeflows,mergeid) and is_sync_done(bpmn, " +
					"activeflows, syncstore, mergeid)) then\n");
			stringBuilder.append("MoveOn(mergeid);");

			if (!incJoinBeginList.isEmpty())
			{
				stringBuilder.append("select \n");
				stringBuilder.append(PyToJavaUtils.join(incJoinBeginList, "[]\n"));
				stringBuilder.append("end select \n");
			}
			else
			{
				stringBuilder.append(this.getSchedulerString(
						"{}",
						"{}",
						SYNC_STORE,
						MERGE_STORE,
						PAR_STORE
				));
			}

			stringBuilder.append("else \n\n");
			stringBuilder.append("scheduler [");
			stringBuilder.append(this.getFlowMsgs(false));
			ArrayList<String> res = this.computeAddSynchroPoints(false);

			if (!res.isEmpty())
			{
				stringBuilder.append(",");
				stringBuilder.append(PyToJavaUtils.join(res, ","));
			}

			stringBuilder.append(", MoveOn] (activeflows, bpmn, syncstore, mergestore, parstore)\n");
			stringBuilder.append("end if\n");

			//Outflow of parallel merge
			stringBuilder.append("\n[]\n");
			stringBuilder.append(" mergeid := any ID where member(mergeid, parstore);\n");
			stringBuilder.append(("if (is_merge_possible_par(bpmn,syncstore,mergeid)) then \n"));

			if (!parJoinBeginList.isEmpty())
			{
				stringBuilder.append("select \n");
				stringBuilder.append(PyToJavaUtils.join(parJoinBeginList, "[]\n"));
				stringBuilder.append("end select ");
			}
			else
			{
				stringBuilder.append(this.getSchedulerString(
						"{}",
						"{}",
						SYNC_STORE,
						MERGE_STORE,
						PAR_STORE
				));
			}

			stringBuilder.append("else \n\n");
			stringBuilder.append("scheduler [");
			stringBuilder.append(this.getFlowMsgs(false));
			res = this.computeAddSynchroPoints(false);

			if (!res.isEmpty())
			{
				stringBuilder.append(",");
				stringBuilder.append(PyToJavaUtils.join(res, ","));
			}

			stringBuilder.append(", MoveOn] (activeflows, bpmn, syncstore, mergestore, parstore)\n");
			stringBuilder.append("end if\n");
			stringBuilder.append("end select\n");
			stringBuilder.append("end var\n");
			stringBuilder.append("end process\n\n");
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
			final StringBuilder lntBuilder = new StringBuilder();

			lntBuilder.append("module ")
					.append(this.name)
					.append(isBalanced ? "" : "(bpmntypes)")
					.append(" with get, <, == is\n\n");

			if (isBalanced)
			{
				//Generates an ID type for all flow identifiers
				lntBuilder.append("(* Data type for flow identifiers, useful for scheduling purposes *)\n")
						.append("type ID is\n");

				int counter = this.flows.size();

				for (Flow f : this.flows)
				{
					lntBuilder.append(f.identifier());
					counter--;

					if (counter > 0)
					{
						lntBuilder.append(",");
					}
				}

				lntBuilder.append("\nwith ==, !=\n")
						.append("end type\n\n");
			}

			if (this.initial != null)
			{
				this.initial.writeLnt(lntBuilder);
			}

			//Generates one process for final events and events, this is enough because generic processes
			if (!this.finals.isEmpty())
			{
				this.finals.get(0).writeLnt(lntBuilder);
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
								n.writeLnt(lntBuilder);
							}
						}
						else
						{
							specialNodes.add(n.getClass().getName());
							n.writeLnt(lntBuilder);
						}
					}
				}
				else
				{
					n.writeLnt(lntBuilder);
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

			lntBuilder.append("\nprocess MAIN ");
			final ArrayList<String> alpha = this.alpha();
			dumpAlphabet(alpha, lntBuilder, true);
			lntBuilder.append(" is\n\n");

			//Computes additional synchros for or splits/joins
			final ArrayList<String> synchroPoints = isBalanced ? this.computeAddSynchroPoints() : this.computeAddSynchroPoints(false);
			final int nbSync = synchroPoints.size();
			lntBuilder.append(" hide begin:any, finish:any");
			final int nbFlows = this.flows.size();

			if (isBalanced)
			{
				if (nbFlows > 0)
				{
					lntBuilder.append(", ");
					int cter = 1;

					for (Flow f : this.flows)
					{
						lntBuilder.append(f.identifier())
								.append("_begin:any, ")
								.append(f.identifier())
								.append("_finish:any");
						cter++;

						if (cter <= nbFlows)
						{
							lntBuilder.append(", ");
							//we hide additional synchros for or splits/joins as well
						}
					}

					int nb = 0;

					if (nbSync > 0)
					{
						lntBuilder.append(", ");

						for (String s : synchroPoints)
						{
							lntBuilder.append(s)
									.append(":any");
							nb++;

							if (nb < nbSync)
							{
								lntBuilder.append(", ");
							}
						}
					}
				}

				lntBuilder.append(" in\n")
						.append("par ");
				//Synchronizations on all begin/finish flows

				if (nbFlows > 0)
				{
					int cter = 1;

					for (Flow f : this.flows)
					{
						lntBuilder.append(f.identifier())
								.append("_begin, ")
								.append(f.identifier())
								.append("_finish");
						cter++;

						if (cter <= nbFlows)
						{
							lntBuilder.append(", ");
						}
					}
				}
			}
			else
			{
				if (nbFlows > 0)
				{
					lntBuilder.append(", ")
							.append(this.getFlowMsgs(true));
					//We hide additional synchros for or splits/joins as well
					int nb = 0;

					if (nbSync > 0)
					{
						lntBuilder.append(", ");

						for (String synchroPoint : synchroPoints)
						{
							lntBuilder.append(synchroPoint)
									.append(":any");
							nb++;

							if (nb < nbSync)
							{
								lntBuilder.append(", ");
							}
						}
					}
				}

				lntBuilder.append(", MoveOn:any in\n");

				//We start with the scheduler
				lntBuilder.append("par MoveOn, ");

				//Synchronization on all begin/finish flows

				if (nbFlows > 0)
				{
					lntBuilder.append(this.getFlowMsgs(false));
				}

				if (!synchroPoints.isEmpty())
				{
					lntBuilder.append(",")
							.append(PyToJavaUtils.join(synchroPoints, ","));
				}

				lntBuilder.append(" in\n")
						.append(" (* we first generate the scheduler, necessary for keeping track of tokens, and triggering inclusive merge gateways *)\n")
						.append("    scheduler [...](nil, p1(), nil, nil, nil) \n")
						.append("||\n")
						.append("par   ")
						.append(" (* synchronizations on all begin/finish flow messages *)\n");

				//Synchronizations on all begin/finish flows
				if (nbFlows > 0)
				{
					lntBuilder.append(this.getFlowMsgs(false));
				}
			}

			lntBuilder.append(" in\n");

			//Interleaving of all flow processes
			lntBuilder.append(" par (* we then generate interleaving of all flow processes *)\n");
			int cter = 1;

			for (Flow f : this.flows)
			{
				//TODO: take conditional flows into account
				lntBuilder.append("flow [")
						.append(f.identifier())
						.append("_begin, ")
						.append(f.identifier())
						.append("_finish] (")
						.append(f.identifier())
						.append(")");
				cter++;

				if (cter <= nbFlows)
				{
					lntBuilder.append(" || ");
				}
			}

			lntBuilder.append("\n end par \n\n||\n");

			//Interleaving of all node processes
			lntBuilder.append(" par     (* we finally generate interleaving of all node processes *)\n");

			//Process instantiation for initial node
			lntBuilder.append("init [begin,")
					.append(this.initial.outgoingFlows().get(0).identifier())
					.append("_begin] || ");   //We assume a single output flow
			final int nbFinals = this.finals.size();
			cter = 1;

			//Processes instantiations for final nodes
			for (Node n : this.finals)
			{
				lntBuilder.append("final [")
						.append(n.incomingFlows().get(0).identifier())
						.append("_finish, finish]");   //We assume a single incoming flow
				cter++;

				if (cter <= nbFlows)
				{
					lntBuilder.append(" || ");
				}
			}

			//Processes instantiations for all other nodes
			final int nbNodes = this.nodes.size();
			cter = 1;

			for (Node n : this.nodes)
			{
				n.writeMainLnt(lntBuilder);
				cter++;

				if (cter <= nbNodes)
				{
					lntBuilder.append(" || ");
				}
			}

			lntBuilder.append("\n end par \n")
					.append(isBalanced ? "\n" : " end par\n")
					.append(" end par\n")
					.append("end hide\n\n")
					.append("end process\n\n")
					.append("end module\n");

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
			this.genSVL(true);
		}

		/**
		 * Generates an SVL file
		 */
		void genSVL(final boolean smartReduction)
		{
			final String fileName = this.name + ".svl";
			final StringBuilder svlCommandBuilder = new StringBuilder();

			svlCommandBuilder.append("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n")
					.append(isBalanced ? "% CAESAR_OPTIONS=\"-more cat\"\n\n" : "% CAESAR_OPTIONS=\"-more cat -gc\"\n\n")
					.append("% DEFAULT_PROCESS_FILE")
					.append(this.name)
					.append(".lnt\n\n")
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
		return this.generate(pifFileName, true,true, !isBalanced);
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName is the name of the PIF file
	 * @return (Integer, String, Collection<String>), return code, name of the model
	 * (can be different from the filename) and its alphabet
	 */
	@Override
	public Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																final boolean generateLTS)
	{
		return this.generate(pifFileName, generateLTS,true, !isBalanced);
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
		if (generateLTS)
		{
			//Compute the LTS from the LNT code using SVL, possibly with a smart reduction
			process.genSVL(smartReduction);

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
		return this.load(pifFileName, generateLTS,true, false);
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
