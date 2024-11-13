package fr.inria.convecs.optimus.nl_to_mc;

import java.io.File;

import static fr.inria.convecs.optimus.nl_to_mc.Main.OLD_WEBSITE;

public class Filename
{
	public static final String COUNTEREXAMPLE_FILE = "diag";
	public static final String TEMPORARY_COUNTEREXAMPLE = COUNTEREXAMPLE_FILE + ".tmp";
	public static final String VBPMN_COUNTEREXAMPLE_FILE = "evaluator.bcg";
	public static final String BUCHI_AUTOMATA = "buchi.hoa";
	public static final String SVL_SCRIPT_NAME = "task.svl";
	public static final String TRUE_RESULT_FILE_NAME = "res_true.txt";
	public static final String FALSE_RESULT_FILE_NAME = "res_false.txt";
	public static final String WARNING_FILE_NAME = "warning.txt";
	public static final String BCG_SPEC_FILE_NAME = "problem.bcg";
	public static final String LTL_PROPERTY = "property.ltl";
	public static final String ID_FILE = "id.lnt";
	public static final String BPMN_TYPES_FILE = "bpmntypes.lnt";
	public static final String PIF_SCHEMA = "pif.xsd";
	public static final String REMOTE_PIF_FILE_LOCATION = OLD_WEBSITE ? "/home/quentin_nivon/nl_to_mc/public" : "/home/convecs/nivonq/nl_to_mc/public";
	public static final String LOCAL_PIF_FILE_LOCATION = "/home/quentin/Documents/VBPMN/vbpmn_transformation/src/main/resources";
	public static final String REMOTE_PIF_FILE = REMOTE_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA;
	public static final String LOCAL_PIF_FILE = LOCAL_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA;
}
