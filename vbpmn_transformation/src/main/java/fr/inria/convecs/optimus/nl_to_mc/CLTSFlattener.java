package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutState;
import fr.inria.convecs.optimus.aut.AutWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

import static fr.inria.convecs.optimus.nl_to_mc.Main.LOCAL_SITE;

public class CLTSFlattener
{
	private static final boolean VERIFY_FLATTENING = true;
	private final AutGraph clts;

	public CLTSFlattener(final AutGraph clts)
	{
		this.clts = clts;
	}

	/**
	 * The flattening consists in duplicating each state of the CLTS
	 * that has several incoming transitions, except if it is a loop
	 * entry state.
	 * By doing so, the corresponding BPMN process will be flat, which
	 * will ease the matching computations.
	 * To perform this flattening step, the CLTS is first made acyclic,
	 * then flattened, and finally made cyclic again.
	 */
	public void flatten()
	{
		//Make the CLTS acyclic
		final HashSet<AutEdge> edgesToReplace = new HashSet<>();
		final HashMap<AutState, HashSet<AutState>> stateCorrespondences = new HashMap<>();

		for (AutEdge outgoingEdge : this.clts.startNode().outgoingEdges())
		{
			final HashSet<AutState> visitedNodes = new HashSet<>();
			visitedNodes.add(this.clts.startNode());
			this.makeAcyclic(outgoingEdge, edgesToReplace, visitedNodes);
		}

		int nextStateLabel = this.clts.getMaxStateLabel() + 1;

		for (AutEdge autEdge : edgesToReplace)
		{
			autEdge.sourceNode().removeOutgoingEdge(autEdge);
			autEdge.targetNode().removeIncomingEdge(autEdge);
			final AutState newState = new AutState(nextStateLabel++);
			final AutEdge newEdge = new AutEdge(autEdge.sourceNode(), autEdge.label(), newState);
			final HashSet<AutState> correspondences = stateCorrespondences.computeIfAbsent(autEdge.targetNode(), n -> new HashSet<>());
			correspondences.add(newState);
			autEdge.sourceNode().addOutgoingEdge(newEdge);
			autEdge.targetNode().addIncomingEdge(newEdge);
		}

		//Debug
		if (LOCAL_SITE)
		{
			final AutWriter autWriter = new AutWriter(this.clts, new File("/home/quentin/Documents/test.autx"), true);
			try
			{
				autWriter.write();
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		//Flatten CLTS
		boolean changed = true;

		while (changed)
		{
			changed = false;

			for (AutState autState : this.clts.nodesAndEdges().getFirst())
			{
				if (autState.incomingEdges().size() > 1)
				{
					final HashSet<AutEdge> incomingTransitions = new HashSet<>();
					boolean started = false;

					for (AutEdge incomingTransition : autState.incomingEdges())
					{
						if (started)
						{
							incomingTransitions.add(incomingTransition);
						}

						started = true;
					}

					for (AutEdge incomingTransition : incomingTransitions)
					{
						incomingTransition.sourceNode().removeOutgoingEdge(incomingTransition);
						autState.removeIncomingEdge(incomingTransition);

						final HashMap<AutState, AutState> correspondences = new HashMap<>();
						final AutGraph copy = new AutGraph(autState).copyAndShift(nextStateLabel, correspondences);
						nextStateLabel = copy.getMaxStateLabel() + 1;
						final AutEdge newEdge = new AutEdge(incomingTransition.sourceNode(), incomingTransition.label(), copy.startNode(), incomingTransition.getColor());
						incomingTransition.sourceNode().addOutgoingEdge(newEdge);
						copy.startNode().addIncomingEdge(newEdge);

						for (AutState loopState : stateCorrespondences.keySet())
						{
							final AutState copiedLoopState = correspondences.get(loopState);

							if (copiedLoopState != null)
							{
								final HashSet<AutState> loopCorrespondences = stateCorrespondences.computeIfAbsent(copiedLoopState, n -> new HashSet<>());

								for (AutState dummyLoopNode : stateCorrespondences.get(loopState))
								{
									final AutState copiedDummy = correspondences.get(dummyLoopNode);
									if (copiedDummy == null) throw new IllegalStateException();
									loopCorrespondences.add(copiedDummy);
								}
							}
						}
					}

					changed = true;
					break;
				}
			}
		}

		//Verify flattening
		if (VERIFY_FLATTENING)
		{
			for (AutState autState : this.clts.nodesAndEdges().getFirst())
			{
				if (autState.incomingEdges().size() > 1) throw new IllegalStateException();
			}
		}

		//Make the CLTS cyclic again
		for (AutState realLoopState : stateCorrespondences.keySet())
		{
			final HashSet<AutState> dummyLoopStates = stateCorrespondences.get(realLoopState);

			for (AutState dummyLoopState : dummyLoopStates)
			{
				final AutEdge incomingTransition = dummyLoopState.incomingEdges().iterator().next();
				final AutState sourceState = incomingTransition.sourceNode();
				sourceState.removeOutgoingEdge(incomingTransition);
				final AutEdge realEdge = new AutEdge(sourceState, incomingTransition.label(), realLoopState);
				sourceState.addOutgoingEdge(realEdge);
				realLoopState.addIncomingEdge(realEdge);
			}
		}
	}

	//Private methods

	private void makeAcyclic(final AutEdge currentEdge,
							 final HashSet<AutEdge> edgesToReplace,
							 final HashSet<AutState> visitedNodes)
	{
		if (visitedNodes.contains(currentEdge.targetNode()))
		{
			//Cycle found
			edgesToReplace.add(currentEdge);
			return;
		}

		visitedNodes.add(currentEdge.targetNode());

		for (AutEdge outgoingEdge : currentEdge.targetNode().outgoingEdges())
		{
			this.makeAcyclic(outgoingEdge, edgesToReplace, new HashSet<>(visitedNodes));
		}
	}
}
