package fr.inria.convecs.optimus.bpmn.types.process;

import fr.inria.convecs.optimus.bpmn.types.process.events.Event;

import java.util.ArrayList;

public class BpmnProcessFactory
{
    private static ArrayList<String> objectIDs;

    private BpmnProcessFactory()
    {

    }

    public static void setObjectIDs(ArrayList<BpmnProcessObject> objects)
    {
        if (objectIDs == null)
        {
            objectIDs = new ArrayList<>();
        }

        if (!objectIDs.isEmpty())
        {
            return;
        }

        for (BpmnProcessObject object : objects)
        {
            objectIDs.add(object.id());
        }
    }

    public static void setObjectIDsString(ArrayList<String> objects)
    {
        if (objectIDs == null
            || objects == null)
        {
            objectIDs = new ArrayList<>();
        }

        if (!objectIDs.isEmpty())
        {
            return;
        }

        objectIDs.addAll(objects == null ? new ArrayList<>() : objects);
    }

    public static synchronized boolean validID(final String id)
    {
        //logger.debug("IDs already existing : {}", objectIDs);

        for (String idToCheck : objectIDs)
        {
            if (idToCheck.equalsIgnoreCase(id))
            {
                //logger.debug("ID {} already exists.", id);
                return false;
            }
        }

        //logger.debug("ID {} does not exist.", id);

        return true;
    }

    public static ArrayList<String> saveIDS()
    {
        return objectIDs;
    }

    public static void addID(String id)
    {
        if (!objectIDs.contains(id))
        {
            objectIDs.add(id);
        }
    }

    public static String generateLongID()
    {
        return generateID(8);
    }

    public static String generateVeryLongID()
    {
        return generateID(15);
    }

    public static String generateID()
    {
        return generateID(6);
    }

    public static synchronized String generateID(BpmnProcessObject object)
    {
        final String builder = generateID();
        final String id;

        if (object instanceof Task)
        {
            id = "Activity_" + builder;
        }
        else if (object instanceof Gateway)
        {
            id = "Gateway_" + builder;
        }
        else if (object instanceof Event)
        {
            if (object.type == BpmnProcessType.START_EVENT
                || object.type == BpmnProcessType.START_CONDITIONAL_EVENT
                || object.type == BpmnProcessType.START_MESSAGE_EVENT
                || object.type == BpmnProcessType.START_SIGNAL_EVENT
                || object.type == BpmnProcessType.START_TIMER_EVENT)
            {
                id = "StartEvent_" + builder;
            }
            else
            {
                id = "Event_" + builder;
            }
        }
        else if (object instanceof SequenceFlow)
        {
            id = "Flow_" + builder;
        }
        else
        {
            //logger.error("Object {} not managed", object.id());
            throw new UnsupportedOperationException(String.format("Object |%s| not managed.", object.id()));
        }

        if (validID(id))
        {
            objectIDs.add(id);
            return id;
        }
        else
        {
            return generateID(object);
        }
    }

    public static synchronized String generateID(BpmnProcessType type)
    {
        final String builder = generateID();
        final String id;

        if (type == BpmnProcessType.TASK
            || type == BpmnProcessType.TASK_MANUAL
            || type == BpmnProcessType.TASK_RECEIVE
            || type == BpmnProcessType.TASK_SCRIPT
            || type == BpmnProcessType.TASK_SEND
            || type == BpmnProcessType.TASK_BUSINESS_RULE
            || type == BpmnProcessType.TASK_SERVICE
            || type == BpmnProcessType.TASK_USER)
        {
            id = "Activity_" + builder;
        }
        else if (type == BpmnProcessType.COMPLEX_GATEWAY
                || type == BpmnProcessType.EXCLUSIVE_GATEWAY
                || type == BpmnProcessType.INCLUSIVE_GATEWAY
                || type == BpmnProcessType.PARALLEL_GATEWAY)
        {
            id = "Gateway_" + builder;
        }
        else if (type == BpmnProcessType.START_EVENT
                || type == BpmnProcessType.START_CONDITIONAL_EVENT
                || type == BpmnProcessType.START_MESSAGE_EVENT
                || type == BpmnProcessType.START_SIGNAL_EVENT
                || type == BpmnProcessType.START_TIMER_EVENT)
        {
            id = "StartEvent_" + builder;
        }
        else if (type.toString().toLowerCase().contains("event"))
        {
            id = "Event_" + builder;
        }
        else if (type == BpmnProcessType.SEQUENCE_FLOW)
        {
            id = "Flow_" + builder;
        }
        else
        {
            //logger.error("Object {} not managed", object.id());
            throw new UnsupportedOperationException(String.format("Type |%s| not managed.", type));
        }

        if (validID(id))
        {
            objectIDs.add(id);
            return id;
        }
        else
        {
            return generateID(type);
        }
    }

    public static BpmnProcessObject generateExclusiveGateway()
    {
        return BpmnProcessFactory.generateGateway(BpmnProcessType.EXCLUSIVE_GATEWAY);
    }

    public static BpmnProcessObject generateParallelGateway()
    {
        return generateParallelGateway(false);
    }

    public static BpmnProcessObject generateParallelGateway(boolean isMerge)
    {
        final BpmnProcessObject parallelGateway = BpmnProcessFactory.generateGateway(BpmnProcessType.PARALLEL_GATEWAY);

        if (isMerge)
        {
            ((Gateway) parallelGateway).markAsMergeGateway();
        }

        return parallelGateway;
    }

    public static BpmnProcessObject generateInclusiveGateway()
    {
        return BpmnProcessFactory.generateGateway(BpmnProcessType.INCLUSIVE_GATEWAY);
    }

    public static BpmnProcessObject generateComplexGateway()
    {
        return BpmnProcessFactory.generateGateway(BpmnProcessType.COMPLEX_GATEWAY);
    }

    public static BpmnProcessObject generateSequenceFlow(final String source,
                                                         final String destination)
    {
        return new SequenceFlow("Flow_" + generateID(), source, destination);
    }

    public static BpmnProcessObject generateSequenceFlow()
    {
        return new SequenceFlow("Flow_" + generateID(), "", "");
    }

    public static BpmnProcessObject generateEndEvent()
    {
        return new Event(BpmnProcessType.END_EVENT, "Event_" + generateID());
    }

    public static BpmnProcessObject generateStartEvent()
    {
        return new Event(BpmnProcessType.START_EVENT, "StartEvent_" + generateID());
    }

    public static Task generateTask(final String id,
                                    final String name,
                                    final int duration)
    {
        final Task task = new Task(id, BpmnProcessType.TASK, duration);
        task.setName(name);

        return task;
    }

    public static Task generateTask(final String id,
                                    final String name)
    {
        return BpmnProcessFactory.generateTask(id, name, -1);
    }

    public static Task generateTask(final String id,
                                    final int duration)
    {
        return BpmnProcessFactory.generateTask(id, id, duration);
    }

    public static Task generateTask(final String id)
    {
        return BpmnProcessFactory.generateTask(id, id);
    }

    public static Task generateTask()
    {
        return BpmnProcessFactory.generateTask(BpmnProcessFactory.generateID(BpmnProcessType.TASK));
    }

    //Private methods

    private static BpmnProcessObject generateGateway(BpmnProcessType type)
    {
        return new Gateway(type, "Gateway_" + generateID());
    }

    public static synchronized String generateID(int nbCharacters)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < nbCharacters; i++)
        {
            int randomInt = (int) (Math.random() * 62);

            if (randomInt < 10)
            {
                //integer
                builder.append((char) (randomInt + 48));
            }
            else if (randomInt < 36)
            {
                //caps
                builder.append((char) (randomInt + 55));
            }
            else
            {
                builder.append((char) (randomInt + 61));
            }
        }

        if (validID(builder.toString()))
        {
            objectIDs.add(builder.toString());
            return builder.toString();
        }
        else
        {
            return generateID(nbCharacters);
        }
    }
}
