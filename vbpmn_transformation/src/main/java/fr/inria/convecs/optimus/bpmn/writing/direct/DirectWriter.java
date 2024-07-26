package fr.inria.convecs.optimus.bpmn.writing.direct;

import fr.inria.convecs.optimus.bpmn.BpmnCategories;
import fr.inria.convecs.optimus.bpmn.BpmnDiagram;
import fr.inria.convecs.optimus.bpmn.BpmnHeader;
import fr.inria.convecs.optimus.bpmn.BpmnProcess;
import fr.inria.convecs.optimus.bpmn.constants.Constants;
import fr.inria.convecs.optimus.bpmn.constants.FlowDirection;
import fr.inria.convecs.optimus.bpmn.resources.Resource;
import fr.inria.convecs.optimus.bpmn.types.diagram.*;
import fr.inria.convecs.optimus.bpmn.types.process.*;
import fr.inria.convecs.optimus.bpmn.types.process.events.ConditionalEvent;
import fr.inria.convecs.optimus.bpmn.types.process.events.Event;
import fr.inria.convecs.optimus.bpmn.types.process.events.SpecialEvent;
import fr.inria.convecs.optimus.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

public class DirectWriter
{
    private final PrintWriter writer;
    private final BpmnHeader bpmnHeader;
    private final BpmnProcess bpmnProcess;
    private final BpmnDiagram bpmnDiagram;
    private final BpmnCategories bpmnCategories;
    private final String documentation;

    public DirectWriter(String path,
                        BpmnHeader header,
                        BpmnProcess process,
                        BpmnDiagram diagram,
                        BpmnCategories categories,
                        String documentation) throws IOException
    {
        this.writer = new PrintWriter(path, StandardCharsets.UTF_8);
        this.bpmnHeader = header;
        this.bpmnProcess = process;
        this.bpmnDiagram = diagram;
        this.bpmnCategories = categories;
        this.documentation = documentation;
    }

    public DirectWriter(final File workingDirectory,
                        final ArrayList<BpmnProcessObject> objects,
                        final String dumpValue) throws FileNotFoundException
    {
        this.writer = new PrintWriter(Path.of(workingDirectory.getPath(), dumpValue).toString());
        this.bpmnHeader = new BpmnHeader();
        this.bpmnProcess = new BpmnProcess("Process_" + BpmnProcessFactory.generateID(), false);
        this.bpmnProcess.setObjects(objects);
        this.bpmnCategories = new BpmnCategories();
        this.bpmnDiagram = null;
        this.documentation = null;
    }

    public void writeInitialBpmnFile()
    {
        //Write header
        this.writer.println(bpmnHeader.xmlHeader());
        this.writer.print("<bpmn:definitions");

        for (String key : bpmnHeader.metadata().keySet())
        {
            String value = bpmnHeader.metadata().get(key);

            this.writer.print(Constants.SPACE);
            this.writer.print(key);
            this.writer.print(Constants.EQUALS);
            this.writer.print(Utils.quote(value));
        }

        if (!bpmnHeader.metadata().containsKey("xmlns:bpmn"))
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("xmlns:bpmn");
            this.writer.print(Constants.EQUALS);
            this.writer.print(Utils.quote("http://www.omg.org/spec/BPMN/20100524/MODEL"));
        }
        if (!bpmnHeader.metadata().containsKey("xmlns:bpmndi"))
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("xmlns:bpmndi");
            this.writer.print(Constants.EQUALS);
            this.writer.print(Utils.quote("http://www.omg.org/spec/BPMN/20100524/DI"));
        }

        this.writer.print(Constants.GREATER_THAN);
        this.writer.println();

        //Write process

        this.writer.print(Constants.TWO_SPACES);
        this.writer.print("<bpmn:process id=");
        this.writer.print(Utils.quote(this.bpmnProcess.id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("isExecutable=");
        this.writer.print(this.bpmnProcess.isExecutable() ? Utils.quote(Constants.TRUE) : Utils.quote(Constants.FALSE));
        this.writer.print(Constants.GREATER_THAN);
        this.writer.println();

        if (this.documentation != null)
        {
            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("<documentation>");
            this.writer.print(this.documentation);
            this.writer.println("</documentation>");
        }

        for (BpmnProcessObject object : this.bpmnProcess.objects())
        {
            if (object instanceof Event)
            {
                writeEvent((Event) object);
            }
            else if (object instanceof Task)
            {
                writeTask((Task) object);
            }
            else if (object.type() == BpmnProcessType.SEQUENCE_FLOW)
            {
                writeSequenceFlow((SequenceFlow) object);
            }
            else if (object.type() == BpmnProcessType.EXCLUSIVE_GATEWAY
                    || object.type() == BpmnProcessType.PARALLEL_GATEWAY
                    || object.type() == BpmnProcessType.INCLUSIVE_GATEWAY
                    || object.type() == BpmnProcessType.COMPLEX_GATEWAY)
            {
                writeGateway((Gateway) object);
            }
            else if (object.type() == BpmnProcessType.GROUP)
            {
                writeGroup((Group) object);
            }
            else if (object.type() == BpmnProcessType.ASSOCIATION)
            {
                writeAssociation((Association) object);
            }
            else if (object.type() == BpmnProcessType.TEXT_ANNOTATION)
            {
                writeTextAnnotation((TextAnnotation) object);
            }
            else
            {
                System.out.printf("BPMN type |%s| is not managed yet!%n", object.type());
            }
        }

        this.writer.print(Constants.TWO_SPACES);
        this.writer.println("</bpmn:process>");

        //Write categories

        for (Category category : this.bpmnCategories.categories())
        {
            writeCategory(category);
        }

        //Write diagram

        if (this.bpmnDiagram != null)
        {
            this.writer.print(Constants.TWO_SPACES);
            this.writer.print("<bpmndi:BPMNDiagram id=");
            this.writer.print(Utils.quote(this.bpmnDiagram.diagramID()));
            this.writer.println(Constants.GREATER_THAN);
            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("<bpmndi:BPMNPlane id=");
            this.writer.print(Utils.quote(this.bpmnDiagram.planeID()));
            this.writer.print(Constants.SPACE);
            this.writer.print("bpmnElement=");
            this.writer.print(Utils.quote(this.bpmnDiagram.bpmnElement()));
            this.writer.println(Constants.GREATER_THAN);

            for (BpmnDiagramObject object : this.bpmnDiagram.objects())
            {
                if (object.type() == BpmnDiagramType.EDGE)
                {
                    writeEdge((Edge) object);
                }
                else if (object.type() == BpmnDiagramType.SHAPE)
                {
                    writeShape((Shape) object);
                }
                else
                {
                    System.out.printf("Objet of type |%s| is waiting for its implementation!%n", object.type());
                }
            }

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.println("</bpmndi:BPMNPlane>");
            this.writer.print(Constants.TWO_SPACES);
            this.writer.println("</bpmndi:BPMNDiagram>");
        }

        this.writer.print("</bpmn:definitions>");
        this.writer.close();
    }

    //Private methods

    //Process objects

    private void writeEvent(Event event)
    {
        if (event instanceof ConditionalEvent)
        {
            ConditionalEvent conditionalEvent = (ConditionalEvent) event;

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print(Constants.LOWER_THAN);
            this.writer.print(conditionalEvent.type().generalType());
            this.writer.print(Constants.SPACE);
            this.writer.print("id=");
            this.writer.print(Utils.quote(conditionalEvent.id()));

            if (event.hasName())
            {
                this.writer.print(Constants.SPACE);
                this.writer.print("name=");
                this.writer.print(Utils.quote(event.name()));
            }

            this.writer.println(Constants.GREATER_THAN);

            this.writer.print(Constants.SIX_SPACES);
            this.writer.print("<bpmn:conditionalEventDefinition id=");
            this.writer.print(Utils.quote(conditionalEvent.subID()));
            this.writer.println(Constants.GREATER_THAN);

            this.writer.print(Constants.EIGHT_SPACES);
            this.writer.print("<bpmn:condition xsi:type=");
            this.writer.print(Utils.quote(conditionalEvent.condition().type()));
            this.writer.print(Constants.SPACE);
            this.writer.println("/>");

            this.writer.print(Constants.SIX_SPACES);
            this.writer.println("</bpmn:conditionalEventDefinition>");

            for (SequenceFlow flow : event.flows().keySet())
            {
                FlowDirection direction = event.flows().get(flow);

                this.writer.print(Constants.SIX_SPACES);
                this.writer.print(direction == FlowDirection.INCOMING ? "<bpmn:incoming>" : "<bpmn:outgoing>");
                this.writer.print(flow.id());
                this.writer.println(direction == FlowDirection.INCOMING ? "</bpmn:incoming>" : "</bpmn:outgoing>");
            }

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("</");
            this.writer.print(conditionalEvent.type().generalType());
            this.writer.println(Constants.GREATER_THAN);
        }
        else if (event instanceof SpecialEvent)
        {
            SpecialEvent specialEvent = (SpecialEvent) event;

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print(Constants.LOWER_THAN);
            this.writer.print(specialEvent.type().generalType());
            this.writer.print(Constants.SPACE);
            this.writer.print("id=");
            this.writer.print(Utils.quote(specialEvent.id()));

            if (event.hasName())
            {
                this.writer.print(Constants.SPACE);
                this.writer.print("name=");
                this.writer.print(Utils.quote(event.name()));
            }

            this.writer.println(Constants.GREATER_THAN);

            this.writer.print(Constants.SIX_SPACES);
            this.writer.print(Constants.LOWER_THAN);
            this.writer.print(specialEvent.type().toString());
            this.writer.print(Constants.SPACE);
            this.writer.print("id=");
            this.writer.print(Utils.quote(specialEvent.subID()));
            this.writer.println("/>");

            for (SequenceFlow flow : event.flows().keySet())
            {
                FlowDirection direction = event.flows().get(flow);

                this.writer.print(Constants.SIX_SPACES);
                this.writer.print(direction == FlowDirection.INCOMING ? "<bpmn:incoming>" : "<bpmn:outgoing>");
                this.writer.print(flow.id());
                this.writer.println(direction == FlowDirection.INCOMING ? "</bpmn:incoming>" : "</bpmn:outgoing>");
            }

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("</");
            this.writer.print(specialEvent.type().generalType());
            this.writer.println(Constants.GREATER_THAN);
        }
        else
        {
            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("<");
            this.writer.print(event.type().toString());
            this.writer.print(Constants.SPACE);
            this.writer.print("id=");
            this.writer.print(Utils.quote(event.id()));

            if (event.hasName())
            {
                this.writer.print(Constants.SPACE);
                this.writer.print("name=");
                this.writer.print(Utils.quote(event.name()));
            }

            this.writer.println(Constants.GREATER_THAN);

            for (SequenceFlow flow : event.flows().keySet())
            {
                FlowDirection direction = event.flows().get(flow);

                this.writer.print(Constants.SIX_SPACES);
                this.writer.print(direction == FlowDirection.INCOMING ? "<bpmn:incoming>" : "<bpmn:outgoing>");
                this.writer.print(flow.id());
                this.writer.println(direction == FlowDirection.INCOMING ? "</bpmn:incoming>" : "</bpmn:outgoing>");
            }

            this.writer.print(Constants.FOUR_SPACES);
            this.writer.print("</");
            this.writer.print(event.type().toString());
            this.writer.println(Constants.GREATER_THAN);
        }
    }

    private void writeSequenceFlow(SequenceFlow sequenceFlow)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<bpmn:sequenceFlow id=");
        this.writer.print(Utils.quote(sequenceFlow.id()));

        if (sequenceFlow.hasName())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("name=");
            this.writer.print(Utils.quote(sequenceFlow.name()));
        }
        else
        {
            final StringBuilder nameBuilder = new StringBuilder();

            if (sequenceFlow.probability() < 1d)
            {
                nameBuilder.append(sequenceFlow.probability());
            }

            this.writer.print(Constants.SPACE);
            this.writer.print("name=");
            this.writer.print(Utils.quote(nameBuilder.toString()));
        }

        this.writer.print(Constants.SPACE);
        this.writer.print("sourceRef=");
        this.writer.print(Utils.quote(sequenceFlow.sourceRef()));
        this.writer.print(Constants.SPACE);
        this.writer.print("targetRef=");
        this.writer.print(Utils.quote(sequenceFlow.targetRef()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");
    }

    private void writeTask(Task task)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<");
        this.writer.print(task.type().toString());
        this.writer.print(Constants.SPACE);
        this.writer.print("id=");
        this.writer.print(Utils.quote(task.id()));

        if (task.hasName())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("name=\"");
            this.writer.print(task.name());
        }

        if (task.hasDuration())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("(");
            this.writer.print(task.duration());
            this.writer.print(")");
        }

        if (!task.resourceUsage().isEmpty())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("&#60;");
            String separator = "";

            for (Resource resource : task.resourceUsage().resources())
            {
                final int usage = task.resourceUsage().getUsageOf(resource);
                this.writer.print(separator);
                this.writer.print(usage);
                this.writer.print(Constants.SPACE);
                this.writer.print(resource.name());

                separator = ", ";
            }

            this.writer.print("&#62;");
        }

        this.writer.print("\"");

        if (task.autoStoreVariables() != null)
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("activiti:autoStoreVariables=");
            this.writer.print(Utils.quote(task.autoStoreVariables() ? "true" : "false"));
        }

        this.writer.println(Constants.GREATER_THAN);

        for (SequenceFlow incomingFlow : task.flows().keySet())
        {
            FlowDirection direction = task.flows().get(incomingFlow);

            this.writer.print(Constants.SIX_SPACES);
            this.writer.print("<bpmn:");
            this.writer.print(direction.toString());
            this.writer.print(Constants.GREATER_THAN);
            this.writer.print(incomingFlow.id());
            this.writer.print("</bpmn:");
            this.writer.print(direction);
            this.writer.println(Constants.GREATER_THAN);
        }

        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("</");
        this.writer.print(task.type().toString());
        this.writer.println(Constants.GREATER_THAN);
    }

    private void writeGateway(final Gateway gateway)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<");
        this.writer.print(gateway.type().toString());
        this.writer.print(Constants.SPACE);
        this.writer.print("id=");
        this.writer.print(Utils.quote(gateway.id()));

        if (gateway.hasName())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("name=");
            this.writer.print(Utils.quote(gateway.name()));
        }

        this.writer.println(Constants.GREATER_THAN);

        for (SequenceFlow incomingFlow : gateway.flows().keySet())
        {
            FlowDirection direction = gateway.flows().get(incomingFlow);

            this.writer.print(Constants.SIX_SPACES);
            this.writer.print("<bpmn:");
            this.writer.print(direction.toString());
            this.writer.print(Constants.GREATER_THAN);
            this.writer.print(incomingFlow.id());
            this.writer.print("</bpmn:");
            this.writer.print(direction);
            this.writer.println(Constants.GREATER_THAN);
        }


        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("</");
        this.writer.print(gateway.type().toString());
        this.writer.println(Constants.GREATER_THAN);
    }

    private void writeGroup(Group group)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<bpmn:group id=");
        this.writer.print(Utils.quote(group.id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("categoryValueRef=");
        this.writer.print(Utils.quote(group.categoryValueRef()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");
    }

    private void writeAssociation(Association association)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<bpmn:association id=");
        this.writer.print(Utils.quote(association.id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("sourceRef=");
        this.writer.print(Utils.quote(association.source().id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("targetRef=");
        this.writer.print(Utils.quote(association.target().id()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");
    }

    private void writeTextAnnotation(TextAnnotation textAnnotation)
    {
        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<bpmn:textAnnotation id=");
        this.writer.print(Utils.quote(textAnnotation.id()));
        this.writer.println(Constants.GREATER_THAN);

        this.writer.print(Constants.SIX_SPACES);
        this.writer.print("<bpmn:text>");
        this.writer.print(textAnnotation.text() == null ? "" : Utils.protectString(textAnnotation.text()));
        this.writer.println("</bpmn:text>");

        this.writer.print(Constants.FOUR_SPACES);
        this.writer.println("</bpmn:textAnnotation>");
    }

    private void writeCategory(Category category)
    {
        this.writer.print(Constants.TWO_SPACES);
        this.writer.print("<bpmn:category id=");
        this.writer.print(Utils.quote(category.id()));
        this.writer.println(Constants.GREATER_THAN);

        this.writer.print(Constants.FOUR_SPACES);
        this.writer.print("<bpmn:categoryValue id=");
        this.writer.print(Utils.quote(category.categoryValueID()));
        this.writer.print(Constants.SPACE);
        this.writer.print("value=");
        this.writer.print(Utils.quote(category.value()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");

        this.writer.print(Constants.TWO_SPACES);
        this.writer.println("</bpmn:category>");
    }

    //Diagram objects

    private void writeEdge(final Edge edge)
    {
        this.writer.print(Constants.SIX_SPACES);
        this.writer.print("<bpmndi:BPMNEdge id=");
        this.writer.print(Utils.quote(edge.id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("bpmnElement=");
        this.writer.print(Utils.quote(edge.bpmnElement().id()));

        this.writer.println(Constants.GREATER_THAN);

        for (Waypoint waypoint : edge.waypoints())
        {
            writeWaypoint(waypoint);
        }

        this.writer.print(Constants.SIX_SPACES);
        this.writer.println("</bpmndi:BPMNEdge>");
    }

    private void writeWaypoint(final Waypoint waypoint)
    {
        this.writer.print(Constants.EIGHT_SPACES);
        this.writer.print("<omgdi:waypoint x=");
        this.writer.print(Utils.quote(waypoint.x()));
        this.writer.print(Constants.SPACE);
        this.writer.print("y=");
        this.writer.print(Utils.quote(waypoint.y()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");
    }

    private void writeShape(final Shape shape)
    {
        this.writer.print(Constants.SIX_SPACES);
        this.writer.print("<bpmndi:BPMNShape id=");
        this.writer.print(Utils.quote(shape.id()));
        this.writer.print(Constants.SPACE);
        this.writer.print("bpmnElement=");
        this.writer.print(Utils.quote(shape.bpmnElement().id()));

        //Color if needed
        final BpmnProcessObject object = this.getObjectFromId(shape.bpmnElement().id());
        if (object == null) throw new IllegalStateException();

        if (object.getBpmnColor() != null)
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("bioc:stroke=");
            this.writer.print(Utils.quote(object.getBpmnColor().getStrokeColor()));

            this.writer.print(Constants.SPACE);
            this.writer.print("bioc:fill=");
            this.writer.print(Utils.quote(object.getBpmnColor().getFillColor()));
        }

        if (shape.hasMarkerVisibleTag())
        {
            this.writer.print(Constants.SPACE);
            this.writer.print("isMarkerVisible=");
            this.writer.print(shape.isMarkerVisible() ? Utils.quote(Constants.TRUE) : Utils.quote(Constants.FALSE));
        }

        this.writer.println(Constants.GREATER_THAN);

        this.writeBounds(shape.bounds());

        if (shape.hasBpmnLabelTag())
        {
            this.writer.print(Constants.EIGHT_SPACES);
            this.writer.println("<bpmndi:BPMNLabel />");
        }

        this.writer.print(Constants.SIX_SPACES);
        this.writer.println("</bpmndi:BPMNShape>");
    }

    private void writeBounds(final Bounds bounds)
    {
        this.writer.print(Constants.EIGHT_SPACES);
        this.writer.print("<omgdc:Bounds x=");
        this.writer.print(Utils.quote(bounds.x()));
        this.writer.print(Constants.SPACE);
        this.writer.print("y=");
        this.writer.print(Utils.quote(bounds.y()));
        this.writer.print(Constants.SPACE);
        this.writer.print("width=");
        this.writer.print(Utils.quote(bounds.width()));
        this.writer.print(Constants.SPACE);
        this.writer.print("height=");
        this.writer.print(Utils.quote(bounds.height()));
        this.writer.print(Constants.SPACE);
        this.writer.println("/>");
    }

    private BpmnProcessObject getObjectFromId(final String id)
    {
        for (BpmnProcessObject object : this.bpmnProcess.objects())
        {
            if (object.id().equals(id))
            {
                return object;
            }
        }

        return null;
    }
}
