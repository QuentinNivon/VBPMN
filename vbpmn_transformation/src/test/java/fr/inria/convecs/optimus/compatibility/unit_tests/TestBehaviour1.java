package fr.inria.convecs.optimus.compatibility.unit_tests;

import fr.inria.convecs.optimus.compatibility.GenericTest;
import fr.inria.convecs.optimus.py_to_java.ShellColor;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TestBehaviour1 extends GenericTest
{
	//Names of the PIF files to use (in ``vbpmn_transformation/src/main/resources/pif_examples'').
	//Files should be identical in the case of formula verification.
	private static final String FILE_1 = "inclusive_A_B_C.pif";
	private static final String FILE_2 = "inclusive_A_B_C.bcg";
	//Processes comparison properties (null if formula checking mode)
	private static final String COMPARISON_MODE = "conservative";                    //Comparison mode
	//Process formula checking properties (null if comparison mode)
	private static final String PROPERTY_MODE = null;        //Mode of property verification
	private static final String FORMULA_ARG = null;          //--formula
	private static final String FORMULA = null;              //MCL formula to verify
	//Expected result of the test
	private static final boolean EXPECTED_RESULT = true;

	@Test
	public void test() throws IOException
	{
		//Get a clean temporary directory.
		final String tmpDir = this.getTempDir();

		//Copy the files of the test to the temporary directory.
		final Pair<String, String> filePaths = this.copyTestFiles(tmpDir, FILE_1, FILE_2);

		//Start VBPMN.
		final Vbpmn vbpmn = this.getVbpmnInstance(
				tmpDir,
				null,
				Arrays.asList("A", "B", "C"),		//Alphabet of the LTS, necessary when passing a BCG file directly
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
