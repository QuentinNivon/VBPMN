package fr.inria.convecs.optimus.compatibility.unit_tests;

import fr.inria.convecs.optimus.compatibility.GenericTest;
import fr.inria.convecs.optimus.py_to_java.ShellColor;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.IOException;

public class Test32 extends GenericTest
{
	//Names of the PIF files to use (in ``vbpmn_transformation/src/main/resources/pif_examples'').
	//Files should be identical in the case of formula verification.
	private static final String PIF_FILE_1 = "Process_10a.pif";            //File 1: Balanced inclusive gateway
	private static final String PIF_FILE_2 = "Process_10a.pif";            //File 2: Balanced inclusive gateway
	//Processes comparison properties (null if formula checking mode)
	private static final String COMPARISON_MODE = null;                    //Comparison mode
	//Process formula checking properties (null if comparison mode)
	private static final String PROPERTY_MODE = "property-implied";        //Mode of property verification
	private static final String FORMULA_ARG = "--formula";                //--formula
	private static final String FORMULA = "true";                        //MCL formula to verify
	//Expected result of the test
	private static final boolean EXPECTED_RESULT = true;

	@Test
	public void test() throws IOException
	{
		//Get a clean temporary directory.
		final String tmpDir = this.getTempDir();

		//Copy the files of the test to the temporary directory.
		final Pair<String, String> filePaths = this.copyTestFiles(tmpDir, PIF_FILE_1, PIF_FILE_2);

		//Start VBPMN.
		final Vbpmn vbpmn = this.getVbpmnInstance(
				tmpDir,
				filePaths.getLeft(),
				filePaths.getRight(),
				COMPARISON_MODE,
				PROPERTY_MODE,
				FORMULA_ARG,
				FORMULA
		);
		final boolean result = vbpmn.execute();

		if (result != EXPECTED_RESULT)
		{
			final String errorMessage = "The result of this test should be \"" + EXPECTED_RESULT + "\" but is \"" + result + "\".";
			System.out.println(ShellColor.ANSI_RED + errorMessage + ShellColor.ANSI_RESET);
			throw new RuntimeException(errorMessage);
		}
	}
}
