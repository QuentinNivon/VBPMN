/**
 * 
 */
package fr.inria.convecs.optimus.parser;

import fr.inria.convecs.optimus.model.Node;
import fr.inria.convecs.optimus.model.Node.NodeType;
import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.model.Sequence;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ajayk
 *
 */
public class BaseContentHandler implements ContentHandler {

	static final Logger logger = LoggerFactory.getLogger(BaseContentHandler.class);

	private File input;
	private Process output;


	private static final String START_EVENT = "startEvent";
	private static final String END_EVENT = "endEvent";
	private static final String SEQUENCE_FLOW = "sequenceFlow";
	private static final String ID = "id";
	private static final String OUTGOING = "outgoing";
	private static final String INCOMING = "incoming";
	private static final String PROCESS = "process";
	private static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
	private static final String INCLUSIVE_GATEWAY = "inclusiveGateway";
	private static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
	private static final String PARALLEL_GATEWAY ="parallelGateway";
	//private static final String USER_TASK = "userTask";
	private static final String TASK = "Task";

	private static final Set<String> NODE_VALUES = new HashSet<>(Arrays.asList
			(START_EVENT, END_EVENT,TASK, EXCLUSIVE_GATEWAY, INCLUSIVE_GATEWAY, PARALLEL_GATEWAY, EVENT_BASED_GATEWAY));

	public BaseContentHandler(File input) {
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see fr.inria.convecs.optimus.parser.ContentHandler#handle()
	 */
	@Override
	public void handle() {

		try {

			XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
			XMLStreamReader2 streamReader = inputFactory.createXMLStreamReader(input);

			Process process = null;
			String processId = null;
			List<Node> nodeList = new ArrayList<Node>();
			List<Sequence> sequenceList = new ArrayList<Sequence>();
			while (streamReader.hasNext()) {
				int eventType = streamReader.next(); //eventReader.next();
				if(streamReader.getEventType() == XMLStreamReader.END_ELEMENT)
				{
					String elementName = streamReader.getLocalName();
					if(elementName.equals(PROCESS))
					{
						//quick fix: dot is not handled at backend
						processId = processId.replace(".", "_");
						process = new Process(processId, nodeList, sequenceList);
						break;
					}

				}
				else if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT) 
				{
					String elementName = streamReader.getLocalName();
					if(elementName.equals(PROCESS))
					{
						processId = streamReader.getAttributeValue(null, ID);
						logger.debug("Parsing Process id: {} ", processId);
					}
					else if(NODE_VALUES.contains(elementName)) 
					{
						Node node = handleNode(streamReader, elementName);
						logger.debug("Node info: {}", node.toString());
						nodeList.add(node);
					}
					else if(elementName.endsWith(TASK) || elementName.equals("task")) 
					{
						Node task = handleNode(streamReader, elementName);
						logger.debug("Task info: {}", task.toString());
						nodeList.add(task);
					}
					else if(elementName.equals(SEQUENCE_FLOW)) {
						String id = streamReader.getAttributeValue(null, ID);
						String source = streamReader.getAttributeValue(null, "sourceRef");
						String target = streamReader.getAttributeValue(null, "targetRef");
						logger.debug("Parsing Sequence id: {} ", id);
						Sequence sequence = new Sequence(id, source, target);
						logger.debug("Sequence info: {}", sequence.toString());
						sequenceList.add(sequence);
					}
				}
			}
			this.output = process;

		} catch (XMLStreamException e) {
			logger.error("Error parsing the XML", e);
			e.printStackTrace();
		}
	}

	private Node handleNode(final XMLStreamReader2 streamReader, String eventName) throws XMLStreamException
	{
		Node node = new Node();
		List<String> incomingFlows = new ArrayList<String>();
		List<String> outgoingFlows = new ArrayList<String>();


		while(streamReader.hasNext())
		{
			if(streamReader.getEventType() == XMLStreamReader.END_ELEMENT)
			{
				String elementName = streamReader.getLocalName();
				if(elementName.equals(eventName))
				{
					node.setIncomingFlows(incomingFlows);
					node.setOutgoingFlows(outgoingFlows);
					break;
				}
			} 
			else if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT)
			{
				String elementName = streamReader.getLocalName();
				String id;
				if(elementName.equals(eventName)) 
				{
					id = streamReader.getAttributeValue(null, ID);		
					logger.debug("Parsing {} id: {} ", elementName, id);
					node.setId(id);
				}
				else if(elementName.equals(INCOMING))
				{
					String flowId = readCharacters(streamReader);
					incomingFlows.add(flowId);
				}
				else if(elementName.equals(OUTGOING))
				{
					String flowId = readCharacters(streamReader);
					outgoingFlows.add(flowId);
				}
			}

			int eventType = streamReader.next();

		}

		if(eventName.equals(START_EVENT))
		{
			node.setType(NodeType.INITIAL_EVENT);
			node.setIncomingFlows(null);
		}
		else if (eventName.equals(END_EVENT)) 
		{
			node.setType(NodeType.END_EVENT);
			node.setOutgoingFlows(null);
		}
		else if(eventName.equals(EXCLUSIVE_GATEWAY))
		{
			if(incomingFlows.size() < outgoingFlows.size())
				node.setType(NodeType.XOR_SPLIT_GATEWAY);
			else
				node.setType(NodeType.XOR_JOIN_GATEWAY);
		}
		else if(eventName.equals(EVENT_BASED_GATEWAY))
		{
			if(incomingFlows.size() < outgoingFlows.size())
				node.setType(NodeType.XOR_SPLIT_GATEWAY);
			else
				node.setType(NodeType.XOR_JOIN_GATEWAY);
		}
		else if(eventName.equals(INCLUSIVE_GATEWAY))
		{
			if(incomingFlows.size() < outgoingFlows.size())
				node.setType(NodeType.OR_SPLIT_GATEWAY);
			else
				node.setType(NodeType.OR_JOIN_GATEWAY);
		}
		else if(eventName.equals(PARALLEL_GATEWAY))
		{
			if(incomingFlows.size() < outgoingFlows.size())
				node.setType(NodeType.AND_SPLIT_GATEWAY);
			else
				node.setType(NodeType.AND_JOIN_GATEWAY);
		}
		else if(eventName.endsWith(TASK) || eventName.equals("task"))
		{
			node.setType(NodeType.TASK);
		}
		else
		{
			logger.error("Unhandled event! : {}", eventName);
		}

		return node;
	}

	private String readCharacters(final XMLStreamReader2 streamReader) throws XMLStreamException {
		StringBuilder result = new StringBuilder();
		while (streamReader.hasNext()) {
			int eventType = streamReader.next();
			if(eventType == XMLStreamReader.CHARACTERS){
				result.append(streamReader.getText());
			}
			else if(eventType == XMLStreamReader.END_ELEMENT){
				return result.toString();
			}
		}
		logger.error("Error parsing the file @ {}", streamReader.getLocation().toString());
		throw new XMLStreamException("Error parsing the file");
	}

	/* (non-Javadoc)
	 * @see fr.inria.convecs.optimus.parser.ContentHandler#getOutput()
	 */
	@Override
	public Object getOutput() {

		return this.output;
	}

}
