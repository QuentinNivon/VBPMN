package fr.inria.convecs.optimus.py_to_java;

public class InclusiveCycleDetectedException extends Exception
{
	public InclusiveCycleDetectedException()
	{
		super();
	}

	public InclusiveCycleDetectedException(final String message)
	{
		super(message);
	}

	public InclusiveCycleDetectedException(final Throwable throwable)
	{
		super(throwable);
	}

	public InclusiveCycleDetectedException(final String message,
										   final Throwable throwable)
	{
		super(message, throwable);
	}
}
