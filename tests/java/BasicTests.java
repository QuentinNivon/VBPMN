/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * vbpmn
 * Copyright (C) 2015 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicTests {

    public static final String FILES_PATH = "examples/pif.basic";
    public static final String TESTFILE_PATH = "tests/examples/pif.basic/tests.txt";
    public static final String REGEX_COMMENT = "^\\h*//.*$";
    public static final String REGEX_TEST = "^(\\w*)\\h([=<>])\\h(\\w*)\\h([+-])$";
    public static final String REGEX_EMPTYLINE = "^\\h*$";
    public static final String SUFFIX = ".pif";

    /**
     * Performs equivalence/preoder checking following the tests described in a file
     */
    @Test
    public void run_all_tests() {
        String line;
        FileInputStream filetests = null;
        Pattern p_test = Pattern.compile(REGEX_TEST);
        try {
            filetests = new FileInputStream(TESTFILE_PATH);
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
                        String process1 = FILES_PATH + m_test.group(1) + SUFFIX;
                        String process2 = FILES_PATH + m_test.group(3) + SUFFIX;
                        String operator = m_test.group(2);
                        String expected_result = m_test.group(4);
                        System.out.println(process1 + operator + process2 + expected_result);
                        // TODO : call python, get result, and compare to the expected one
                    } else {
                        fail();
                    }
                } else {
                    System.out.println(String.format("cannot parse line: ", line));
                    fail();
                }
                line = fin.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
