package fr.inria.convecs.optimus.py_to_java;

public class ReturnCode
{
	private ReturnCode()
	{

	}

	public static final int TERMINATION_OK = 0;
	public static final int TERMINATION_ERROR = 1;
	public static final int TERMINATION_PROBLEM = 2;
	public static final int TERMINATION_UNBALANCED_INCLUSIVE_CYCLE = 3;
	public static final int RETRIEVING_SPEC_LABELS_FAILED = 17;
	public static final int SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS = 31;
	public static final int SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD = 35;
	public static final int PRODUCT_WEAKTRACE_FAILED = 38;
	public static final int BCG_TO_AUT_FAILED = 39;
	public static final int BCG_SPEC_TO_AUT_FAILED = 40;
	public static final int WEAKTRACE_SPEC_FAILED = 41;
	public static final int CLTS_AUT_TO_BCG_FAILED = 42;
	public static final int CLTS_WEAKTRACE_FAILED = 43;
	public static final int CLTS_BCG_TO_AUT_FAILED = 44;
	public static final int PRODUCT_BRANCHING_REDUCTION_FAILED = 45;
	public static final int PRODUCT_STRONG_REDUCTION_FAILED = 46;
	public static final int CLTS_BRANCHING_REDUCTION_FAILED = 47;
	public static final int CLTS_STRONG_REDUCTION_FAILED = 48;
	public static final int SPEC_BRANCHING_REDUCTION_FAILED = 49;
	public static final int SPEC_STRONG_REDUCTION_FAILED = 50;
}
