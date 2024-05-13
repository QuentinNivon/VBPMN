package fr.inria.convecs.optimus.bpmn.graph;

import fr.inria.convecs.optimus.bpmn.constants.FlowDirection;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.SequenceFlow;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExceptionStatus;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;

public class GraphToList
{
	private final HashSet<BpmnProcessObject> objects;
	private final Graph graph;

	public GraphToList(final Graph graph)
	{
		this.objects = new HashSet<>();
		this.graph = graph;
	}

	public HashSet<BpmnProcessObject> convert() throws ExpectedException
	{
		this.convertRec(this.graph.initialNode(), new HashSet<>());
		return this.objects;
	}

	public HashSet<BpmnProcessObject> objects()
	{
		if (this.objects.isEmpty())
		{
			throw new IllegalStateException("This method should always follow a call to the \"convert()\" method.");
		}

		return this.objects;
	}

	public ArrayList<BpmnProcessObject> objectsList()
	{
		if (this.objects.isEmpty())
		{
			throw new IllegalStateException("This method should always follow a call to the \"convert()\" method.");
		}

		return new ArrayList<>(this.objects);
	}

	//Private methods

	private void convertRec(final Node currentNode,
							final HashSet<Node> visitedNodes) throws ExpectedException
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);
		this.objects.add(currentNode.bpmnObject());

		if (currentNode.bpmnObject() instanceof SequenceFlow)
		{
			if (currentNode.childNodes().size() != 1
				|| currentNode.parentNodes().size() != 1)
			{
				throw new ExpectedException("Flow |" + currentNode.bpmnObject().id() + "| should have " +
						"exactly one parent node (got " + currentNode.parentNodes().size() + ") and one child node" +
						" (got " + currentNode.childNodes().size() + ").", ExceptionStatus.BADLY_FORMED_BPMN_PROCESS);
			}

			//We set the proper source and target
			final Node parent = currentNode.parentNodes().iterator().next();
			final Node child = currentNode.childNodes().iterator().next();

			((SequenceFlow) currentNode.bpmnObject()).setSourceRef(parent.bpmnObject().id());
			((SequenceFlow) currentNode.bpmnObject()).setTargetRef(child.bpmnObject().id());
		}
		else
		{
			currentNode.bpmnObject().flows().clear();

			//We set the proper flows
			for (Node child : currentNode.childNodes())
			{
				if (!(child.bpmnObject() instanceof SequenceFlow))
				{
					throw new ExpectedException("Node |" + currentNode.bpmnObject().id() + "| has (at least) one " +
							"child that is not a sequence flow (" + child.bpmnObject().type() + ")", ExceptionStatus.BADLY_FORMED_BPMN_PROCESS);
				}

				currentNode.bpmnObject().addFlow((SequenceFlow) child.bpmnObject(), FlowDirection.OUTGOING);
			}

			for (Node parent : currentNode.parentNodes())
			{
				if (!(parent.bpmnObject() instanceof SequenceFlow))
				{
					throw new ExpectedException("Node |" + currentNode.bpmnObject().id() + "| has (at least) one " +
							"parent that is not a sequence flow (" + parent.bpmnObject().type() + ")", ExceptionStatus.BADLY_FORMED_BPMN_PROCESS);
				}

				currentNode.bpmnObject().addFlow((SequenceFlow) parent.bpmnObject(), FlowDirection.INCOMING);
			}
		}

		for (Node child : currentNode.childNodes())
		{
			this.convertRec(child, visitedNodes);
		}
	}
}
