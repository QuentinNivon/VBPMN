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
	public static final int TRANSLATION_TO_PIF_FAILED = 12;
	public static final int TRANSLATION_TO_LNT_FAILED = 13;
	public static final int PROPERTY_GENERATION_FAILED = 14;
	public static final int RETRIEVING_LABELS_FAILED = 17;
	public static final int READING_PROPERTY_FILE_FAILED = 18;
	public static final int WRITING_PROPERTY_NEGATION_FAILED = 19;
	public static final int TRANSLATING_PROPERTY_TO_BUCHI_AUTOMATA_FAILED = 20;
	public static final int WRITING_BUCHI_AUTOMATA_FILE_FAILED = 21;
	public static final int SVL_SCRIPT_GENERATION_FAILED = 22;
	public static final int SVL_SCRIPT_EXECUTION_FAILED = 23;
	public static final int WRITING_PROPERTY_EVALUATION_FILE_FAILED = 24;
	public static final int COUNTEREXAMPLE_DETERMINATION_FAILED = 25;
	public static final int COUNTEREXAMPLE_TO_AUT_FAILED = 26;
	public static final int AUT_TO_VIS_CONVERSION_FAILED = 27;
	public static final int WRITING_LTL_PROPERTY_FAILED = 28;
	public static final int SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS = 31;
	public static final int BUCHI_AUTOMATA_HAS_NO_LABELS = 32;
	public static final int PROPERTY_LABELS_CONTAIN_RESERVED_LTL_KEYWORD = 33;
	public static final int DIAGNOSTIC_FILE_MISSING = 34;
	public static final int SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD = 35;
	public static final int WARNING_FILE_WRITING_FAILED = 36;
	public static final int BCG_GENERATION_FAILED = 37;
}
