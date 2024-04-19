package fr.inria.convecs.optimus.py_to_java;

public class ReturnCodes
{
	private ReturnCodes()
	{

	}

	public static final int TERMINATION_OK = 0;
	public static final int TERMINATION_ERROR = 1;
	public static final int TERMINATION_PROBLEM = 2;
	public static final int TERMINATION_UNBALANCED_INCLUSIVE_CYCLE = 3;
}
