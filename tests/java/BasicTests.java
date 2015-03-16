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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicTests {

    public static final String FILES_PATH = "out/test/vbpmn/";
    public static final String TESTFILE = "tests.txt";
    public static final String REGEX_COMMENT = "^\\h*//.*$";
    public static final String REGEX_TEST = "^([\\w/.]*)\\h([=<>])\\h([\\w/.]*)\\h([+-])$";
    public static final String REGEX_EMPTYLINE = "^\\h*$";

    /**
     * Performs equivalence/preoder checking following the tests described in a file
     * TODO
     */
    @Test(dataProvider = "get_data_from_test_file")
    public void run_all_tests(String filepath1, String filepath2, String operator, String expected_result) {
        // call python
        // get result
        // compare wrt. expected result
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
            filetests = new FileInputStream(FILES_PATH + TESTFILE);
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
                        String process1 = FILES_PATH + m_test.group(1);
                        String process2 = FILES_PATH + m_test.group(3);
                        String operator = m_test.group(2);
                        String expected_result = m_test.group(4);
                        Object[] line_elements = new Object[4];
                        line_elements[0] = process1;
                        line_elements[1] = process2;
                        line_elements[2] = operator;
                        line_elements[3] = expected_result;
                        data.add(line_elements);
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
        return data.iterator();
    }

}
