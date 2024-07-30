package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.bpmn.BpmnColor;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;
import fr.inria.convecs.optimus.util.Pair;

import java.util.*;

public class BPMNFolder
{
	private final Graph bpmnGraph;
	private final HashMap<Node, Node> brokenConnections;

	public BPMNFolder(final Graph originalGraph)
	{
		this.bpmnGraph = originalGraph.weakCopy();
		this.brokenConnections = new HashMap<>();
	}

	public Graph fold()
	{
		//Separate green, red and black parts
		final ArrayList<Graph> subGraphs = new ArrayList<>();
		this.isolateSubGraphs(this.bpmnGraph.initialNode(), subGraphs, new HashSet<>());
		int i = 0;
		final HashMap<Node, Node> dummyReplacements = new HashMap<>();

		for (Iterator<Node> iterator = this.brokenConnections.keySet().iterator(); iterator.hasNext();)
		{
			final Node key = iterator.next();
			final Node value = this.brokenConnections.get(key);
			final Node valueParentFlow = value.parentNodes().iterator().next();
			value.removeParent(valueParentFlow);
			key.removeChild(valueParentFlow);

			if (key.bpmnObject().type() != BpmnProcessType.TASK)
			{
				//This node may be removed by the folding process => add a dummy task
				final Node dummyTask = new Node(BpmnProcessFactory.generateTask("DUMMY_" + i++));
				final Node dummyFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
				key.addChild(dummyFlow);
				dummyFlow.addParent(key);
				dummyFlow.addChild(dummyTask);
				dummyTask.addParent(dummyFlow);
				iterator.remove();
				dummyReplacements.put(dummyTask, value);
			}
		}

		this.brokenConnections.putAll(dummyReplacements);

		//Manage each subgraph
		for (Graph subGraph : subGraphs)
		{
			this.foldSubGraph(subGraph, subGraph.getColor());
		}

		//Replug subgraphs
		for (Node key : this.brokenConnections.keySet())
		{
			final Node value = this.brokenConnections.get(key);

			if (key.bpmnObject().name().contains("DUMMY"))
			{
				//this is a dummy replacement => remove it
				final Node parentFlow = key.parentNodes().iterator().next();
				parentFlow.removeChild(key);
				key.removeParent(parentFlow);
				parentFlow.addChild(value);
				value.addParent(parentFlow);
			}
			else
			{
				final Node branchingFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
				key.addChild(branchingFlow);
				branchingFlow.addParent(key);
				branchingFlow.addChild(value);
				value.addParent(branchingFlow);
			}
		}

		return this.bpmnGraph;
	}

	public Graph getFoldedGraph()
	{
		return this.bpmnGraph;
	}

	//Private methods

	private void isolateSubGraphs(final Node currentNode,
								  final ArrayList<Graph> subGraphs,
								  final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject().type() == BpmnProcessType.TASK)
		{
			if (currentNode.bpmnObject().getBpmnColor() != null)
			{
				subGraphs.add(new Graph(currentNode));
				final Node parentFlow = currentNode.parentNodes().iterator().next();
				final Node parentFlowParent = parentFlow.parentNodes().iterator().next();
				this.brokenConnections.put(parentFlowParent, currentNode);
				return;
			}
		}

		for (Node child : currentNode.childNodes())
		{
			this.isolateSubGraphs(child, subGraphs, visitedNodes);
		}
	}

	private void foldSubGraph(final Graph graph,
							  final BpmnColor graphColor)
	{
		Graph originalGraph = graph.weakCopy();

		do
		{
			final HashSet<Node> exclusiveSplitGateways = new HashSet<>();
			final HashSet<Node> exclusiveMergeGateways = new HashSet<>();
			this.findAllGraphExclusiveGateways(graph.initialNode(), exclusiveSplitGateways, exclusiveMergeGateways, new HashSet<>());
			final HashMap<Node, Foldability> gatewaysInformation = new HashMap<>();

			for (Node exclusiveGateway : exclusiveSplitGateways)
			{
				final Foldability foldability = new Foldability(exclusiveGateway);
				foldability.setMaxDegree(exclusiveGateway.childNodes().size());
				gatewaysInformation.put(exclusiveGateway, foldability);
				final Node correspondingMerge = this.findCorrespondingMergeGateway(exclusiveGateway, exclusiveMergeGateways);
				foldability.setCorrespondingMerge(correspondingMerge);

				if (correspondingMerge != null
					&& correspondingMerge.parentNodes().size() == exclusiveGateway.childNodes().size())
				{
					foldability.setPerfectMatching();
				}
			}

			for (Node exclusiveSplitGateway : exclusiveSplitGateways)
			{
				this.finalizeFoldability(exclusiveSplitGateway, gatewaysInformation);
			}

			int maxFoldability = -1;

			for (Foldability foldability : gatewaysInformation.values())
			{
				if (foldability.getRealDegree() > maxFoldability)
				{
					maxFoldability = foldability.getRealDegree();
				}
			}

			if (maxFoldability < 2) return;

			for (Node gateway : gatewaysInformation.keySet())
			{
				if (!graph.hasNodeOfId(gateway.bpmnObject().id()))
				{
					//The current gateway has already been folded in a bigger gateway
					continue;
				}

				final Foldability currentFoldability = gatewaysInformation.get(gateway);

				if (currentFoldability.getRealDegree() == maxFoldability)
				{
					//Manage this gateway
					if (gateway.childNodes().size() == currentFoldability.getRealDegree())
					{
						//The gateway will be removed
						final Node correspondingMergeGateway = currentFoldability.getCorrespondingMerge();

						if (correspondingMergeGateway == null)
						{
							final Node firstOutOfScopeNode = currentFoldability.getOutOfScopeNodes().iterator().next();
							firstOutOfScopeNode.removeParents();
							final Node parentFlow = gateway.parentNodes().iterator().next();
							parentFlow.removeChildren();
							final Node splitParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway());
							parentFlow.addChild(splitParallelGateway);
							splitParallelGateway.addParent(parentFlow);
							final Node mergeParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway(true));
							final Node mergeToOutOfScopeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
							mergeParallelGateway.addChild(mergeToOutOfScopeFlow);
							mergeToOutOfScopeFlow.addParent(mergeParallelGateway);
							mergeToOutOfScopeFlow.addChild(firstOutOfScopeNode);
							firstOutOfScopeNode.addParent(mergeToOutOfScopeFlow);
							
							for (String parallelTask : currentFoldability.getFoldableTasksNames())
							{
								final Node task = new Node(BpmnProcessFactory.generateTask());
								task.bpmnObject().setName(parallelTask);
								task.bpmnObject().setBpmnColor(graphColor);
								final Node beforeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								final Node afterFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								splitParallelGateway.addChild(beforeFlow);
								beforeFlow.addParent(splitParallelGateway);
								beforeFlow.addChild(task);
								task.addParent(beforeFlow);
								task.addChild(afterFlow);
								afterFlow.addParent(task);
								afterFlow.addChild(mergeParallelGateway);
								mergeParallelGateway.addParent(afterFlow);
							}
						}
						else
						{
							boolean exactCorrespondence = true;

							for (Node parentFlow : correspondingMergeGateway.parentNodes())
							{
								if (!parentFlow.hasAncestor(gateway))
								{
									exactCorrespondence = false;
									break;
								}
							}

							if (exactCorrespondence)
							{
								//TODO VOIR SI CA FONCTIONNE BIEN
								//The gateway can be removed completely
								final Node mergeGatewayChildFlow = correspondingMergeGateway.childNodes().iterator().next();
								mergeGatewayChildFlow.removeParent(correspondingMergeGateway);
								final Node splitGatewayParentFlow = gateway.parentNodes().iterator().next();
								splitGatewayParentFlow.removeChild(gateway);
								final Node splitParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway());
								final Node mergeParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway(true));
								splitGatewayParentFlow.addChild(splitParallelGateway);
								splitParallelGateway.addParent(splitGatewayParentFlow);
								mergeParallelGateway.addChild(mergeGatewayChildFlow);
								mergeGatewayChildFlow.addParent(mergeParallelGateway);

								for (String parallelTask : currentFoldability.getFoldableTasksNames())
								{
									final Node task = new Node(BpmnProcessFactory.generateTask());
									task.bpmnObject().setName(parallelTask);
									task.bpmnObject().setBpmnColor(graphColor);
									final Node beforeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
									final Node afterFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
									splitParallelGateway.addChild(beforeFlow);
									beforeFlow.addParent(splitParallelGateway);
									beforeFlow.addChild(task);
									task.addParent(beforeFlow);
									task.addChild(afterFlow);
									afterFlow.addParent(task);
									afterFlow.addChild(mergeParallelGateway);
									mergeParallelGateway.addParent(afterFlow);
								}
							}
							else
							{
								//TODO VOIR SI CA FONCTIONNE BIEN
								/*
									The corresponding merge gateway corresponds only partially to the split,
									i.e., some of its parent flows do not start from the split gateway.
								 */
								correspondingMergeGateway.parentNodes().removeIf(node -> node.hasAncestor(gateway));
								final Node splitGatewayParentFlow = gateway.parentNodes().iterator().next();
								splitGatewayParentFlow.removeChild(gateway);
								final Node splitParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway());
								final Node mergeParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway(true));
								splitGatewayParentFlow.addChild(splitParallelGateway);
								splitParallelGateway.addParent(splitGatewayParentFlow);
								final Node mergeParallelChild = new Node(BpmnProcessFactory.generateSequenceFlow());
								mergeParallelGateway.addChild(mergeParallelChild);
								mergeParallelChild.addParent(mergeParallelGateway);
								mergeParallelChild.addChild(correspondingMergeGateway);
								correspondingMergeGateway.addParent(mergeParallelChild);

								for (String parallelTask : currentFoldability.getFoldableTasksNames())
								{
									final Node task = new Node(BpmnProcessFactory.generateTask());
									task.bpmnObject().setName(parallelTask);
									task.bpmnObject().setBpmnColor(graphColor);
									final Node beforeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
									final Node afterFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
									splitParallelGateway.addChild(beforeFlow);
									beforeFlow.addParent(splitParallelGateway);
									beforeFlow.addChild(task);
									task.addParent(beforeFlow);
									task.addChild(afterFlow);
									afterFlow.addParent(task);
									afterFlow.addChild(mergeParallelGateway);
									mergeParallelGateway.addParent(afterFlow);
								}
							}
						}
					}
					else
					{
						//Some paths of the gateway will be removed but not the whole gateway
						final Node correspondingMergeGateway = currentFoldability.getCorrespondingMerge();

						if (correspondingMergeGateway == null)
						{
							for (Iterator<Node> iterator = gateway.childNodes().iterator(); iterator.hasNext(); )
							{
								final Node childFlow = iterator.next();
								final Node task = childFlow.childNodes().iterator().next();

								if (currentFoldability.getFoldableTasksNames().contains(task.bpmnObject().name()))
								{
									iterator.remove();
								}
							}

							final Node firstOutOfScopeNode = currentFoldability.getOutOfScopeNodes().iterator().next();
							firstOutOfScopeNode.removeParents();
							final Node splitParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway());
							final Node splitOutFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
							gateway.addChild(splitOutFlow);
							splitOutFlow.addParent(gateway);
							splitOutFlow.addChild(splitParallelGateway);
							splitParallelGateway.addParent(splitOutFlow);
							final Node mergeParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway(true));
							final Node mergeToOutOfScopeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
							mergeParallelGateway.addChild(mergeToOutOfScopeFlow);
							mergeToOutOfScopeFlow.addParent(mergeParallelGateway);
							mergeToOutOfScopeFlow.addChild(firstOutOfScopeNode);
							firstOutOfScopeNode.addParent(mergeToOutOfScopeFlow);

							for (String parallelTask : currentFoldability.getFoldableTasksNames())
							{
								final Node task = new Node(BpmnProcessFactory.generateTask());
								task.bpmnObject().setName(parallelTask);
								task.bpmnObject().setBpmnColor(graphColor);
								final Node beforeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								final Node afterFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								splitParallelGateway.addChild(beforeFlow);
								beforeFlow.addParent(splitParallelGateway);
								beforeFlow.addChild(task);
								task.addParent(beforeFlow);
								task.addChild(afterFlow);
								afterFlow.addParent(task);
								afterFlow.addChild(mergeParallelGateway);
								mergeParallelGateway.addParent(afterFlow);
							}
						}
						else
						{
							final HashSet<Node> removedChildren = new HashSet<>();

							for (Iterator<Node> iterator = gateway.childNodes().iterator(); iterator.hasNext(); )
							{
								final Node childFlow = iterator.next();
								final Node task = childFlow.childNodes().iterator().next();

								if (currentFoldability.getFoldableTasksNames().contains(task.bpmnObject().name()))
								{
									removedChildren.add(childFlow);
									iterator.remove();
								}
							}

							for (Iterator<Node> iterator = correspondingMergeGateway.parentNodes().iterator(); iterator.hasNext(); )
							{
								boolean childFound = false;

								for (Node removedChild : removedChildren)
								{
									if (iterator.next().hasAncestor(removedChild))
									{
										childFound = true;
										break;
									}
								}

								if (childFound)
								{
									iterator.remove();
								}
							}

							final Node splitParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway());
							final Node splitOutFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
							gateway.addChild(splitOutFlow);
							splitOutFlow.addParent(gateway);
							splitOutFlow.addChild(splitParallelGateway);
							splitParallelGateway.addParent(splitOutFlow);
							final Node mergeParallelGateway = new Node(BpmnProcessFactory.generateParallelGateway(true));
							final Node mergeParallelChild = new Node(BpmnProcessFactory.generateSequenceFlow());
							mergeParallelGateway.addChild(mergeParallelChild);
							mergeParallelChild.addParent(mergeParallelGateway);
							mergeParallelChild.addChild(correspondingMergeGateway);
							correspondingMergeGateway.addParent(mergeParallelChild);

							for (String parallelTask : currentFoldability.getFoldableTasksNames())
							{
								final Node task = new Node(BpmnProcessFactory.generateTask());
								task.bpmnObject().setName(parallelTask);
								task.bpmnObject().setBpmnColor(graphColor);
								final Node beforeFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								final Node afterFlow = new Node(BpmnProcessFactory.generateSequenceFlow());
								splitParallelGateway.addChild(beforeFlow);
								beforeFlow.addParent(splitParallelGateway);
								beforeFlow.addChild(task);
								task.addParent(beforeFlow);
								task.addChild(afterFlow);
								afterFlow.addParent(task);
								afterFlow.addChild(mergeParallelGateway);
								mergeParallelGateway.addParent(afterFlow);
							}
						}
					}
				}
			}
		}
		while (!SyntacticAnalyzer.compare(originalGraph, graph));
	}

	private void finalizeFoldability(final Node gateway,
									 final HashMap<Node, Foldability> gatewaysInformation)
	{
		final Foldability foldability = gatewaysInformation.get(gateway);

		if (foldability.alreadyManaged())
		{
			//Foldability already managed => return
			return;
		}

		if (foldability.getMaxDegree() == 2)
		{
			final Iterator<Node> iterator = gateway.childNodes().iterator();
			final Node firstPathFirstNode = iterator.next().childNodes().iterator().next();
			final Node firstPathSecondNode = firstPathFirstNode.childNodes().iterator().next().childNodes().iterator().next();
			final Node secondPathFirstNode = iterator.next().childNodes().iterator().next();
			final Node secondPathSecondNode = secondPathFirstNode.childNodes().iterator().next().childNodes().iterator().next();

			if (firstPathFirstNode.bpmnObject().type() != BpmnProcessType.TASK
				|| firstPathSecondNode.bpmnObject().type() != BpmnProcessType.TASK
				|| secondPathFirstNode.bpmnObject().type() != BpmnProcessType.TASK
				|| secondPathSecondNode.bpmnObject().type() != BpmnProcessType.TASK)
			{
				foldability.setRealDegree(0);
			}
			else
			{
				//Diamond found => we look for the paths after the diamond
				final Graph firstSubGraph = new Graph(firstPathSecondNode.childNodes().iterator().next().childNodes().iterator().next());
				final Graph secondSubGraph = new Graph(secondPathSecondNode.childNodes().iterator().next().childNodes().iterator().next());

				if (SyntacticAnalyzer.compare(firstSubGraph, secondSubGraph))
				{
					//Both are equivalent => the gateway can be folded
					foldability.setRealDegree(2);
					foldability.addFoldableTaskName(firstPathFirstNode.bpmnObject().name());
					foldability.addFoldableTaskName(firstPathSecondNode.bpmnObject().name());
					foldability.addOutOfScopeNode(firstSubGraph.initialNode());
				}
				else
				{
					foldability.setRealDegree(0);
				}
			}
		}
		else
		{
			/*
				For a foldability degree greater than 2, we should check for paths
				of form "TASK --> <X> ...".
				These paths can possibly be folded.
			 */
			final HashSet<Pair<Node, Node>> eligibleGateways = new HashSet<>();
			final HashSet<Node> nonEligibleChildren = new HashSet<>();

			for (Node childFlow : gateway.childNodes())
			{
				final Node childNode = childFlow.childNodes().iterator().next();

				if (childNode.bpmnObject().type() == BpmnProcessType.TASK)
				{
					final Node childChildNode = childNode.childNodes().iterator().next().childNodes().iterator().next();

					if (childChildNode.bpmnObject().type() == BpmnProcessType.EXCLUSIVE_GATEWAY
						&& ((Gateway) childChildNode.bpmnObject()).isSplitGateway())
					{
						//We found an exclusive split gateway at the expected position
						this.finalizeFoldability(childChildNode, gatewaysInformation);
						final Foldability currentFoldability = gatewaysInformation.get(childChildNode);

						if (currentFoldability.foldable())
						{
							eligibleGateways.add(new Pair<>(childFlow, childChildNode));
						}
						else
						{
							nonEligibleChildren.add(childFlow);
						}
					}
					else
					{
						nonEligibleChildren.add(childFlow);
					}
				}
				else
				{
					nonEligibleChildren.add(childFlow);
				}
			}

			System.out.println("Eligible gateways: " + eligibleGateways);

			final HashMap<HashSet<String>, Pair<HashSet<String>, ArrayList<Foldability>>> infos = new HashMap<>();

			if (eligibleGateways.size() >= 3)
			{
				for (Pair<Node, Node> eligibleGateway : eligibleGateways)
				{
					final Node firstTask = eligibleGateway.getFirst().childNodes().iterator().next();
					final Foldability currentFoldability = gatewaysInformation.get(eligibleGateway.getSecond());
					System.out.println("Current foldability: " + currentFoldability);
					final HashSet<String> foldableTasks = new HashSet<>();
					foldableTasks.add(firstTask.bpmnObject().name());
					foldableTasks.addAll(currentFoldability.getFoldableTasksNames());

					if (infos.containsKey(foldableTasks))
					{
						final Pair<HashSet<String>, ArrayList<Foldability>> currentInfos = infos.get(foldableTasks);
						currentInfos.getFirst().add(firstTask.bpmnObject().name());
						currentInfos.getSecond().add(currentFoldability);
					}
					else
					{
						final HashSet<String> firstTasks = new HashSet<>();
						firstTasks.add(firstTask.bpmnObject().name());
						final ArrayList<Foldability> foldabilities = new ArrayList<>();
						foldabilities.add(currentFoldability);
						infos.put(foldableTasks, new Pair<>(firstTasks, foldabilities));
					}
				}
			}

			for (HashSet<String> tasks : infos.keySet())
			{
				final Pair<HashSet<String>, ArrayList<Foldability>> currentInfos = infos.get(tasks);
				System.out.println("Tasks: " + tasks);
				System.out.println("Current infos first: " + currentInfos.getFirst());

				if (currentInfos.getFirst().size() == tasks.size())
				{
					System.out.println("Good size");
					boolean identicalOutOfScopePaths = true;
					final Foldability firstFoldability = currentInfos.getSecond().remove(0);
					final Graph firstOutOfScopeGraph = new Graph(firstFoldability.getOutOfScopeNodes().iterator().next());
					
					for (Foldability currentFoldability : currentInfos.getSecond())
					{
						final Graph currentOutOfScopeGraph = new Graph(currentFoldability.getOutOfScopeNodes().iterator().next());
						
						if (!SyntacticAnalyzer.compare(firstOutOfScopeGraph, currentOutOfScopeGraph))
						{
							identicalOutOfScopePaths = false;
							break;
						}
					}
					
					if (identicalOutOfScopePaths)
					{
						//The current gateway is (at least partially foldable)
						foldability.setRealDegree(tasks.size());
						foldability.addFoldableTaskNames(tasks);
						foldability.addOutOfScopeNodes(firstFoldability.getOutOfScopeNodes());
						break; //Necessary to avoid overwriting data when gateway can be merged several times
					}
				}
			}

			System.out.println("Currently handled foldability: " + foldability);
		}
	}

	private void findAllGraphExclusiveGateways(final Node currentNode,
											   final HashSet<Node> exclusiveSplitGateways,
											   final HashSet<Node> exclusiveMergeGateways,
											   final HashSet<Node> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		if (currentNode.bpmnObject().type() == BpmnProcessType.EXCLUSIVE_GATEWAY)
		{
			if (((Gateway) currentNode.bpmnObject()).isSplitGateway())
			{
				exclusiveSplitGateways.add(currentNode);
			}
			else
			{
				exclusiveMergeGateways.add(currentNode);
			}
		}

		for (Node child : currentNode.childNodes())
		{
			this.findAllGraphExclusiveGateways(child, exclusiveSplitGateways, exclusiveMergeGateways, visitedNodes);
		}
	}

	private Node findCorrespondingMergeGateway(final Node exclusiveSplit,
											   final HashSet<Node> exclusiveMergeGateways)
	{
		Node correspondingMerge = null;

		for (Node exclusiveMergeGateway : exclusiveMergeGateways)
		{
			boolean eligible = true;

			for (Node exclusiveSplitChild : exclusiveSplit.childNodes())
			{
				if (!exclusiveMergeGateway.hasAncestor(exclusiveSplitChild))
				{
					eligible = false;
					break;
				}
			}

			if (eligible)
			{
				if (correspondingMerge == null)
				{
					correspondingMerge = exclusiveMergeGateway;
				}
				else
				{
					if (correspondingMerge.hasAncestor(exclusiveMergeGateway))
					{
						correspondingMerge = exclusiveMergeGateway;
					}
				}
			}
		}

		return correspondingMerge;
	}

	static class Foldability
	{
		private final Node split;
		private int maxDegree;
		private int realDegree;
		private Node correspondingMerge;
		private boolean perfectMatching;
		private final HashSet<Node> impureNodes;
		private final HashSet<Node> outOfScopeNodes;
		private final HashSet<String> foldableTaskNames;

		public Foldability(final Node split)
		{
			this.split = split;
			this.impureNodes = new HashSet<>();
			this.outOfScopeNodes = new HashSet<>();
			this.foldableTaskNames = new HashSet<>();
			this.maxDegree = -1;
			this.realDegree = -1;
			this.correspondingMerge = null;
			this.perfectMatching = false;
		}

		public void setMaxDegree(final int degree)
		{
			this.maxDegree = degree;
		}

		public int getMaxDegree()
		{
			return this.maxDegree;
		}

		public void setRealDegree(final int degree)
		{
			this.realDegree = degree;
		}

		public int getRealDegree()
		{
			return this.realDegree;
		}

		public void setCorrespondingMerge(final Node correspondingMerge)
		{
			this.correspondingMerge = correspondingMerge;
		}

		public Node getCorrespondingMerge()
		{
			return this.correspondingMerge;
		}

		public void setPerfectMatching()
		{
			this.perfectMatching = true;
		}

		public boolean isPerfectMatching()
		{
			return this.perfectMatching;
		}

		public void addImpurePath(final Node node)
		{
			this.impureNodes.add(node);
		}

		public void addImpureNodes(final Collection<Node> nodes)
		{
			this.impureNodes.addAll(nodes);
		}

		public HashSet<Node> getImpureNodes()
		{
			return this.impureNodes;
		}

		public void addOutOfScopeNode(final Node node)
		{
			this.outOfScopeNodes.add(node);
		}

		public void addOutOfScopeNodes(final Collection<Node> nodes)
		{
			this.outOfScopeNodes.addAll(nodes);
		}

		public HashSet<Node> getOutOfScopeNodes()
		{
			return this.outOfScopeNodes;
		}

		public void addFoldableTaskName(final String name)
		{
			this.foldableTaskNames.add(name);
		}

		public void addFoldableTaskNames(final Collection<String> names)
		{
			this.foldableTaskNames.addAll(names);
		}

		public HashSet<String> getFoldableTasksNames()
		{
			return this.foldableTaskNames;
		}

		public boolean foldable()
		{
			return this.realDegree >= 2;
		}

		public boolean alreadyManaged()
		{
			return this.realDegree != -1;
		}

		@Override
		public String toString()
		{
			return "Foldability of gateway \"" +
					this.split.bpmnObject().id() +
					"\" contains the following elements:\n" +
					"- Corresponding merge gateway: " +
					this.correspondingMerge +
					"\n- Maximum foldability degree: " +
					this.maxDegree +
					"\n- Real foldability degree: " +
					this.realDegree +
					"\n- Foldable task labels: " +
					this.foldableTaskNames +
					"\n- Out-of-scope nodes: " +
					this.outOfScopeNodes +
					"\n";
		}
	}
}
