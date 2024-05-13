package fr.inria.convecs.optimus.bpmn.types.process.events;

import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;

public class SpecialEvent extends Event
{
    private final String subID;

    public SpecialEvent(BpmnProcessType type,
                        String id,
                        String subID)
    {
        super(type, id);
        this.subID = subID;
    }

    public String subID()
    {
        return this.subID;
    }

    @Override
    public BpmnProcessObject copy()
    {
        final SpecialEvent duplicate = new SpecialEvent(this.type, BpmnProcessFactory.generateID(this), this.subID + "_2");
        duplicate.setName(this.name());

        return duplicate;
    }
}
