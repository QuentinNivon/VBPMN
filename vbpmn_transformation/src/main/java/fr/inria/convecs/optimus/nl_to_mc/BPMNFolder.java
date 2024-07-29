package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.bpmn.BpmnColor;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.Node;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;
import fr.inria.convecs.optimus.bpmn.types.process.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class BPMNFolder
{
	private Graph bpmnGraph;
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
			key.removeChildren(valueParentFlow);

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
			this.foldSubGraph(subGraph);
		}

		//Replug subgraphs
		for (Node key : this.brokenConnections.keySet())
		{
			final Node value = this.brokenConnections.get(key);

			if (key.bpmnObject().name().contains("DUMMY"))
			{
				//this is a dummy replacement => remove it
				final Node parentFlow = key.parentNodes().iterator().next();
				parentFlow.removeChildren(key);
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

	private void foldSubGraph(final Graph graph)
	{
		final HashSet<Node> exclusiveSplitGateways = new HashSet<>();
		final HashSet<Node> exclusiveMergeGateways = new HashSet<>();
		this.findAllGraphExclusiveGateways(graph.initialNode(), exclusiveSplitGateways, exclusiveMergeGateways, new HashSet<>());
		final HashMap<Node, Foldability> gatewaysInformation = new HashMap<>();

		for (Node exclusiveGateway : exclusiveSplitGateways)
		{
			final Foldability foldability = new Foldability();
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
	}

	private void finalizeFoldability(final Node gateway,
									 final HashMap<Node, Foldability> gatewaysInformation)
	{
		final Foldability foldability = gatewaysInformation.get(gateway);

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
		}
		else
		{

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
		private int maxDegree;
		private int realDegree;
		private Node correspondingMerge;
		private boolean perfectMatching;
		private final ArrayList<Path<Node>> impurePaths;
		private final ArrayList<Path<Node>> afterPaths;
		private final HashSet<String> foldableTaskNames;

		public Foldability()
		{
			this.impurePaths = new ArrayList<>();
			this.afterPaths = new ArrayList<>();
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

		public void addImpurePath(final Path<Node> path)
		{
			this.impurePaths.add(path);
		}

		public ArrayList<Path<Node>> getImpurePaths()
		{
			return this.impurePaths;
		}

		public boolean foldable()
		{
			return this.realDegree >= 2;
		}
	}
}
