/**
 * 
 */
package fr.inria.convecs.optimus.util;

import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

/**
 * @author ajayk
 *
 */
public class BpmnBuilder {

	public StartEvent createStartEvent(String id)
	{
		StartEvent startEvent = new StartEvent();
		startEvent.setId(id);
		return startEvent;
	}

	public EndEvent createEndEvent(String id)
	{
		EndEvent endEvent = new EndEvent();
		endEvent.setId(id);
		return endEvent;
	} 

	public UserTask createUserTask(String id, String name)
	{
		UserTask userTask = new UserTask();
		userTask.setId(id);
		userTask.setName(name);
		return userTask;
	}


	public SequenceFlow createSequenceFlow(String from, String to)
	{
		SequenceFlow flow = new SequenceFlow();
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		return flow;
	}
	
	public ExclusiveGateway createExclusiveGateway(String id, String name, List<SequenceFlow> outgoingFlows)
	{
		ExclusiveGateway p = new ExclusiveGateway();
		p.setId(id);
		p.setName(name);
		//p.setOutgoingFlows(outgoingFlows);
		return p;

	}
	
	public InclusiveGateway createInclusiveGateway(String id, String name, List<SequenceFlow> outgoingFlows)
	{
		InclusiveGateway p = new InclusiveGateway();
		p.setId(id);
		p.setName(name);
		//p.setOutgoingFlows(outgoingFlows);
		return p;

	}

	public ParallelGateway createParallelGateway(String id, String name, List<SequenceFlow> outgoingFlows)
	{
		ParallelGateway p = new ParallelGateway();
		p.setId(id);
		p.setName(name);
		//p.setOutgoingFlows(outgoingFlows);
		return p;
	}
}
