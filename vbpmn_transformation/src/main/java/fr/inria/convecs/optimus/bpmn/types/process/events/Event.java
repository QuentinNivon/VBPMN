package fr.inria.convecs.optimus.bpmn.types.process.events;

import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;

public class Event extends BpmnProcessObject
{
    public Event(BpmnProcessType type,
                 String id)
    {
        super(type, id);
    }

    @Override
    public BpmnProcessObject copy()
    {
        final Event duplicate = new Event(this.type, BpmnProcessFactory.generateID(this));
        duplicate.setName(this.name());

        return duplicate;
    }
}
