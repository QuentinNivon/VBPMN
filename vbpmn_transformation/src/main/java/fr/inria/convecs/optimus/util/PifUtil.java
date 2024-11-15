/**
 * 
 */
package fr.inria.convecs.optimus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.convecs.optimus.pif.Gateway;
import fr.inria.convecs.optimus.pif.OrJoinGateway;
import fr.inria.convecs.optimus.pif.OrSplitGateway;
import fr.inria.convecs.optimus.pif.Process;
import fr.inria.convecs.optimus.pif.SequenceFlow;
import fr.inria.convecs.optimus.pif.WorkflowNode;

/**
 * @author ajayk
 *
 */
public class PifUtil {

	static final Logger logger = LoggerFactory.getLogger(PifUtil.class);

	public static Boolean isPifBalanced(File pifFile)
	{
		Boolean result = false;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Process process = (Process) jaxbUnmarshaller.unmarshal(pifFile);

			result = isProcessBalanced(process);
		} catch(JAXBException e)
		{
			logger.warn("Unable to check if the pif file is balanced", e);
			throw new IllegalStateException("Unable to check if the pif file is balanced.");
		}
		return result;
	}

	public static Boolean isProcessBalanced(Process process)
	{
		Stack<Gateway> gatewayStack = new Stack<Gateway>();

		Boolean result = false;

		try {
			List<WorkflowNode> gateways = process.getBehaviour().getNodes().stream()
					.filter(wfNode -> (wfNode instanceof Gateway))
					.collect(Collectors.toList());

			WorkflowNode initial = process.getBehaviour().getInitialNode();

			List<WorkflowNode> visited = new ArrayList<>();

			result = traverseNodes(initial, visited, gatewayStack);
		}
		catch(Exception e)
		{
			logger.warn("Unable to check if the process is balanced", e);
			throw new IllegalStateException("Unable to check if the pif file is balanced.");
		}
		return result;

	}

	private static Boolean traverseNodes(WorkflowNode initial, 
			List<WorkflowNode> visited, Stack<Gateway> gatewayStack) throws Exception {

		if(initial == null)
		{
			return true;
		}

		if(!visited.contains(initial))
		{
			visited.add(initial);
			//logger.debug("Processing workflownode: {}", initial.getId());
			List<JAXBElement<Object>> seqFlows = initial.getOutgoingFlows();

			if (seqFlows.isEmpty())
			{
				if (gatewayStack.isEmpty())
					return true;
				else 
					return false;
			}

			for (JAXBElement<Object> flowElement: seqFlows)
			{
				SequenceFlow flow = (SequenceFlow) flowElement.getValue();
				WorkflowNode targetNode = flow.getTarget();

				if (targetNode instanceof OrSplitGateway)
				{
					//logger.debug("Push SplitGateway: {}", targetNode.getId());
					gatewayStack.push((Gateway) targetNode);
				}

				if(targetNode instanceof OrJoinGateway)
				{
					//logger.debug("Found JoinGateway: {}", targetNode.getId());
					Gateway sourceSplit = gatewayStack.pop();

					if(sourceSplit instanceof OrSplitGateway)
					{
						if(targetNode.getIncomingFlows().size() != sourceSplit.getOutgoingFlows().size())
						{
							return false;
						}
					}
				}
				return traverseNodes(targetNode, visited, gatewayStack);
			}
		}
		else
		{
			//Loop inclusivegateway
			//logger.warn("Found an inclusive gateway with loop!");
			return false;
		}
		return true;
	}
}
