/**
 * 
 */

package fr.inria.convecs.optimus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author silverquick
 *
 */
public class RuntimeValidatorTest {

	//private static final String OUTPUT_PATH = AppProperty.getInstance().getFolder("OUTPUT_PATH");

	//private static final String SCRIPTS_PATH = AppProperty.getInstance().getFolder("SCRIPTS_PATH");

	@Test
	public void testProbOutput() {
		String probOutput = "Output\n0.6\nTRUE";
		List<String> lines = Arrays.asList(probOutput.split("\n"));
	    List<String> txt = new ArrayList<String>(lines.subList(Math.max(0, lines.size() - 2), lines.size()));
	    System.out.println(txt);

	}
}
