package fr.inria.convecs.optimus.py_to_java._2023k;

import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class Pif2LntV1
{
	private static final String LTS_SUFFIX = ".bcg";
	private static final String LNT_SUFFIX = ".lnt";

	public Pif2LntV1()
	{

	}

	/**
	 * Dumps alphabet (list of strings) in the given file.
	 *
	 * @param alphabet is the alphabet to dump
	 * @param file is the file in which the alphabet should be dumped
	 * @param addAny is a boolean indicating whether to add "any" or not
	 */
	public void dumpAlphabet(final ArrayList<String> alphabet,
							 final File file,
							 final boolean addAny)
	{
		final int nbElem = alphabet.size();

		if (nbElem > 0)
		{
			final PrintStream printStream;

			try
			{
				printStream = new PrintStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printStream.print("[");
			int counter = 1;

			for (String element : alphabet)
			{
				printStream.print(element);

				if (addAny)
				{
					printStream.print(":any");
				}

				counter++;

				if (counter <= nbElem)
				{
					printStream.print(", ");
				}
			}

			printStream.print("]");
			printStream.close();
		}
	}

	/**
	 * Computes all combinations, in sorted order, any possible number from 1 to size(list)
	 *
	 * @param list the list on which combinations should be computed
	 */
	public static Collection<Collection<String>> computeAllCombinations(final ArrayList<String> list)
	{
		return PyToJavaUtils.getCombinationsOf(list);
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

		Node(final String identifier,
			 final ArrayList<Flow> incomingFlows,
			 final ArrayList<Flow> outgoingFlows)
		{
			this.identifier = identifier;
			this.incomingFlows = incomingFlows;
			this.outgoingFlows = outgoingFlows;
		}

		abstract void writeLnt(final PrintStream printStream);
		abstract ArrayList<String> reachableOrJoin(final ArrayList<String> visited,
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
		void writeLnt(final PrintStream printStream)
		{
			printStream.println("process flow [begin:any, finish:any] (ident:ID) is");
			printStream.println(" loop begin (ident) ; finish (ident) end loop");
			printStream.println("end process");
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
		void writeLnt(final PrintStream printStream)
		{
			//TODO: Translate the condition too
			printStream.println("process conditionalflow [begin:any, finish:any] (ident: ID) is");
			printStream.println(" loop begin (ident) ; finish (ident) end loop");
			printStream.println("end process");
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

		//Generates the (generic) process for the initial event, only once
		@Override
		void writeLnt(final PrintStream printStream)
		{
			printStream.println("process init [begin:any, outf:any] is");
			printStream.println(" var ident: ID in begin ; outf (?ident of ID) end var ");
			printStream.println("end process");
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
		ArrayList<String> reachableOrJoin(ArrayList<String> visited,
										  int depth)
		{
			final ArrayList<String> newVisited = new ArrayList<>(visited);
			newVisited.add(this.identifier);

			return this.outgoingFlows.get(0).getTarget().reachableOrJoin(newVisited, depth);
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

		//Generates the (generic) process for final events, only once
		@Override
		void writeLnt(PrintStream printStream)
		{
			printStream.println("process final [incf:any, finish:any] is");
			printStream.println(" var ident: ID in incf (?ident of ID); finish end var");
			printStream.println("end process");
		}

		/**
		 * Seeks an or join, for an initial event, just a recursive call on the target node of the outgoing flow
		 *
		 * @param visited
		 * @param depth
		 * @return
		 */
		@Override
		ArrayList<String> reachableOrJoin(ArrayList<String> visited, int depth)
		{
			return new ArrayList<>();
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
		ArrayList<String> reachableOrJoin(ArrayList<String> visited, int depth)
		{
			if (visited.contains(this.identifier))
			{
				return new ArrayList<>();
			}
			else
			{
				final ArrayList<String> newVisited = new ArrayList<>(visited);
				newVisited.add(this.identifier);

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
		void writeLnt(PrintStream printStream)
		{
			printStream.println("process interaction [incf:any, inter:any, outf:any] is");
			printStream.println(" var ident: ID in loop incf (?ident of ID); inter; outf (?ident of ID) end loop end var ");
			printStream.println("end process");
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
		void writeMainLnt(final PrintStream printStream)
		{
			//We assume one incoming flow and one outgoing flow
			printStream.print("interaction [");
			printStream.print(this.incomingFlows.get(0).identifier());
			printStream.print("_finish,");
			printStream.print(this.sender);
			printStream.print("_");

			for (String e : this.receivers)
			{
				printStream.print(e);
				printStream.print("_");
			}

			printStream.print(this.message);
			printStream.print(",");
			printStream.print(this.outgoingFlows.get(0).identifier());
			printStream.print("_begin]");
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
		void writeLnt(PrintStream printStream)
		{
			printStream.println("process messagesending [incf:any, msg:any, outf:any] is");
			printStream.println(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var ");
			printStream.println("end process");
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> res = new ArrayList<>();
			res.add(this.message + "_EM");
			return res;
		}

		void writeMainLnt(final PrintStream printStream)
		{
			printStream.print("messagesending [");
			printStream.print(this.incomingFlows.get(0).identifier());
			printStream.print("_finish,");
			printStream.print(this.message);
			printStream.print("_EM,");
			printStream.print(this.outgoingFlows.get(0).identifier());
			printStream.print("_begin]");
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
		void writeLnt(PrintStream printStream)
		{
			printStream.println("process messagereception [incf:any, msg:any, outf:any] is");
			printStream.println(" var ident: ID in loop incf (?ident of ID); msg; outf (?ident of ID) end loop end var");
			printStream.println("end process");
		}

		ArrayList<String> alpha()
		{
			final ArrayList<String> result = new ArrayList<>();
			result.add(this.message + "_REC");
			return result;
		}

		void writeMainLnt(final PrintStream printStream)
		{
			printStream.print("messagereception [");
			printStream.print(this.incomingFlows.get(0).identifier());
			printStream.print("_finish");
			printStream.print(this.message);
			printStream.print("_REC");
			printStream.print(this.outgoingFlows.get(0).identifier());
			printStream.print("_begin]");
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
		void writeLnt(PrintStream printStream)
		{
			final int nbInc = this.incomingFlows.size();
			final int nbOut = this.outgoingFlows.size();

			printStream.print("process task_");
			printStream.print(nbInc);
			printStream.print("_");
			printStream.print(nbOut);
			printStream.print(" [");

			if (nbInc == 1)
			{
				printStream.print("incf:any,");
			}
			else
			{
				int incCounter = 0;

				while (incCounter < nbInc)
				{
					printStream.print("incf");
					printStream.print(incCounter);
					printStream.print(":any,");
					incCounter++;
				}
			}

			printStream.print("task:any,");

			if (nbOut == 1)
			{
				printStream.print("outf:any");
			}
			else
			{
				int outCounter = 0;

				while (outCounter < nbOut)
				{
					printStream.print("outf");
					printStream.print(outCounter);
					printStream.print(":any");
					outCounter++;

					if (outCounter < nbOut)
					{
						printStream.print(",");
					}
				}
			}

			printStream.println("] is");
			printStream.print(" var ident: ID in loop ");

			if (nbInc == 1)
			{
				printStream.print(" incf (?ident of ID); ");
			}
			else
			{
				int incCounter = 0;
				printStream.print(" select ");

				while (incCounter < nbInc)
				{
					printStream.print("incf");
					printStream.print(incCounter);
					printStream.print(" (?ident of ID)");
					incCounter++;

					if (incCounter < nbInc)
					{
						printStream.print(" [] ");
					}
				}

				printStream.println(" end select ; ");
			}

			printStream.print("task ; ");

			if (nbOut == 1)
			{
				printStream.print(" outf (?ident of ID)");
			}
			else
			{
				int outCounter = 0;
				printStream.print(" select ");

				while (outCounter < nbOut)
				{
					printStream.print("outf");
					printStream.print(outCounter);
					printStream.print(" (?ident of ID)");
					outCounter++;

					if (outCounter < nbOut)
					{
						printStream.print(" [] ");
					}
				}

				printStream.println(" end select ");
			}

			printStream.println(" end loop end var");
			printStream.println("end process");
		}

		@Override
		ArrayList<String> reachableOrJoin(ArrayList<String> visited,
										  int depth)
		{
			if (visited.contains(this.identifier))
			{
				return new ArrayList<>();
			}
			else
			{
				if (this.outgoingFlows.size() == 1)
				{
					final ArrayList<String> newVisited = new ArrayList<>(visited);
					newVisited.add(this.identifier);
					return this.outgoingFlows.get(0).getTarget().reachableOrJoin(newVisited, depth);
				}
				else
				{
					final ArrayList<String> res = new ArrayList<>();

					for (Flow f : this.outgoingFlows)
					{
						final ArrayList<String> newVisited = new ArrayList<>(visited);
						newVisited.add(this.identifier);
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
			final int randomInt = random.nextInt(5);
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
		 * @param printStream
		 */
		void writeMainLnt(final PrintStream printStream)
		{
			final int nbOut = this.outgoingFlows.size();
			int i = 0;

			printStream.print("[");
			printStream.print(this.incomingFlows.get(0).identifier());
			printStream.print("_finish,");

			while (i < nbOut)
			{
				printStream.print(this.outgoingFlows.get(i).identifier());
				printStream.print("_begin");
				i++;

				if (i < nbOut)
				{
					printStream.print(",");
				}
			}

			printStream.print("]");
		}

		/**
		 * For a split (generic), if not visited yet, recursive call on the target nodes of all outgoing flows.
		 * Returns the list of reachable or joins.
 		 */
		ArrayList<String> reachableOrJoin(final ArrayList<String> visited,
										  final int depth)
		{
			if (visited.contains(this.identifier))
			{
				return new ArrayList<>();
			}

			final ArrayList<String> res = new ArrayList<>();

			for (Flow f : this.outgoingFlows)
			{
				final ArrayList<String> temp = new ArrayList<>(visited);
				temp.add(this.identifier);
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
		void writeLnt(PrintStream printStream)
		{
			final int nbOut = this.outgoingFlows.size();
			final boolean existsDefault = this.existDefaultFlow();
			//TODO: update the translation to consider properly the default semantics (if there is such a branch)

			//We translate the inclusive split by enumerating all combinations in a select / par
			final ArrayList<String> alphaOut = new ArrayList<>();
			int nb = 1;

			while (nb < nbOut)
			{
				alphaOut.add("outf_" + nb);
				nb++;
			}

			final Collection<Collection<String>> allCombi = Pif2LntV1.computeAllCombinations(alphaOut);
			final int nbt = allCombi.size();

			printStream.print("process orsplit_");
			printStream.print(this.identifier);
			printStream.print(" [incf:any,");

			//We dump the process alphabet (flows + synchronization points if necessary)
			int nbg = 1;

			while (nbg < nbOut)
			{
				printStream.print("outf_");
				printStream.print(nbg);
				printStream.print(":any");
				nbg++;

				if (nbg < nbOut)
				{
					printStream.print(",");
				}
			}

			if (nbt > 0
				&& !this.correspOrJoin.isEmpty())
			{
				printStream.print(", ");
				int counter = 1;

				for (Collection<String> combi : allCombi) //TODO Bizarre ....
				{
					printStream.print(this.correspOrJoin);
					printStream.print("_");
					printStream.print(counter);
					printStream.print(":any");
					counter++;

					if (counter < nbt)
					{
						printStream.print(",");
					}
				}
			}

			printStream.println(" ] is ");
			int counterVar = allCombi.size();
			printStream.print(" var ");

			while (counterVar > 0)
			{
				printStream.print("ident");
				printStream.print(counterVar);
				printStream.print(":ID");
				counterVar--;

				if (counterVar > 0)
				{
					printStream.print(",");
				}
			}

			printStream.println(" in var ident: ID in loop incf (?ident of ID); "); //TODO We generate unnecessary variables...
			printStream.print(" select ");

			nb = 1;
			//Counter for generating synchro points
			int counter = 1;

			for (Collection<String> element : allCombi)
			{
				final int nbElem = element.size();
				int nb2 = 1;

				if (nbElem > 1)
				{
					counterVar = allCombi.size();
					printStream.print(" par ");

					for (String s : element)
					{
						printStream.print(s);
						printStream.print(" (?ident");
						printStream.print(counterVar);
						printStream.print(" of ID)");
						counterVar--;
						nb2++;

						if (nb2 <= nbElem)
						{
							printStream.print("||");
						}
					}

					printStream.print(" end par ");
				}
				else
				{
					printStream.print(element.iterator().next());
					printStream.print(" (?ident of ID)");
				}

				//Add synchronization points if there's a corresponding join
				if (!this.correspOrJoin.isEmpty())
				{
					printStream.print(" ; ");
					printStream.print(this.correspOrJoin);
					printStream.print("_");
					printStream.print(counter);
					counter++;
				}

				nb++;

				if (nb <= nbt)
				{
					printStream.print(" [] ");
				}
			}

			printStream.println(" end select end loop end var end var");
			printStream.println("end process");
		}

		/**
		 * Generates process instantiation for main LNT process.
		 *
		 * @param printWriter
		 */
		void writeMainLnt(final PrintWriter printWriter)
		{
			if (this.correspOrJoin.isEmpty())
			{

			}
		}

		@Override
		ArrayList<String> reachableOrJoin(final ArrayList<String> visited,
										  final int depth)
		{

		}
	}
}
