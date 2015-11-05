/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * vbpmn
 * Copyright (C) 2015 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparisonTests {

    public static final String CMD = "python";
    public static final String WORKINGDIR = "out/test/vbpmn/pif/";
    public static final String PROGRAM_PATH = "../../../production/vbpmn/";
    public static final String PROGRAM = "vbpmn.py";
    public static final String TESTFILE = "tests.txt";
    public static final String REGEX_COMMENT = "^\\h*//.*$";
    public static final String REGEX_TEST = "^([+-])\\h(.*)\\h(.*)\\h(.*)$";
    public static final String REGEX_EMPTYLINE = "^\\h*$";
    public static final String OK = "+";
    public static final String NOK = "-";
    public static final int RETURN_NOT_AS_EXPECTED = 1;
    public static final int RETURN_AS_EXPECTED = 0;

    /**
     * Performs equivalence/pre-order checking following the tests described in a file
     * <p>
     * note : there are issues with Process/ProcessBuilder for some contexts of use
     * see http://docs.oracle.com/javase/8/docs/api/java/lang/Process.html, http://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html
     * hence, we use Apache Commons Exec https://commons.apache.org/proper/commons-exec/
     */
    @Test(dataProvider = "get_data_from_test_file")
    public void run_all_tests(String expected_result, String rest) {
        int exitValue;
        CommandLine cmd = CommandLine.parse(CMD + " -u " + PROGRAM_PATH + PROGRAM + " " + rest);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(WORKINGDIR));
        int expected_result_as_int = 2;
        if (expected_result.equalsIgnoreCase(OK)) expected_result_as_int = 0;
        else if (expected_result.equalsIgnoreCase(NOK)) expected_result_as_int = 1;
        else fail();
        executor.setExitValue(expected_result_as_int);
        try {
            exitValue = executor.execute(cmd);
//            if(expected_result.equals(OK))
//                assertEquals(RETURN_AS_EXPECTED, exitValue);
//            else if(expected_result.equals(NOK))
//                assertEquals(RETURN_NOT_AS_EXPECTED, exitValue);
//            else
//                fail();
        } catch (IOException e) {
            System.out.println(e.getCause());
            fail();
        }
    }


    /**
     * Data provider from a test file
     */
    @DataProvider(name = "get_data_from_test_file")
    public Iterator<Object[]> get_data_from_test_file() {
        List<Object[]> data = new ArrayList<>();
        String line;
        FileInputStream filetests = null;
        Pattern p_test = Pattern.compile(REGEX_TEST);
        try {
            filetests = new FileInputStream(WORKINGDIR + TESTFILE);
            BufferedReader fin = new BufferedReader(new InputStreamReader(filetests));
            line = fin.readLine();
            while (line != null) {
                if (line.matches(REGEX_EMPTYLINE)) {
                    // nothing
                } else if (line.matches(REGEX_COMMENT)) {
                    System.out.println(line);
                } else if (line.matches(REGEX_TEST)) {
                    Matcher m_test = p_test.matcher(line);
                    if (m_test.matches()) {
                        String expected_result = m_test.group(1);
                        String file1 = m_test.group(2);
                        String file2 = m_test.group(3);
                        String options = m_test.group(4);
                        Object[] line_elements = new Object[2];
                        line_elements[0] = expected_result;
                        line_elements[1] = file1+" "+file2+" "+options;
                        data.add(line_elements);
                    } else {
                        fail();
                    }
                } else {
                    System.out.println(String.format("cannot parse line: %s", line));
                    fail();
                }
                line = fin.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        return data.iterator();
    }

}
