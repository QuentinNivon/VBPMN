package fr.inria.convecs.optimus.bpmn.types.process.events;

import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessFactory;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessType;

public class ConditionalEvent extends SpecialEvent
{
    private final Condition condition;

    public ConditionalEvent(BpmnProcessType type,
                            String id,
                            String subID,
                            Condition condition)
    {
        super(type, id, subID);
        this.condition = condition;
    }

    public Condition condition()
    {
        return this.condition;
    }

    @Override
    public BpmnProcessObject copy()
    {
        final ConditionalEvent duplicate = new ConditionalEvent(this.type, BpmnProcessFactory.generateID(this), this.subID() + "_2", this.condition);
        duplicate.setName(this.name());

        return duplicate;
    }
}
