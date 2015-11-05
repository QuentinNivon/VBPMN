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

import models.base.*;
import models.process.pif.*;
import models.process.pif.generated.*;
import models.process.pif.generated.Process;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class Pif2LntTests {

    public static final String CMD = "python";
    public static final String FILES_PATH = "out/test/vbpmn/pif/";
    public static final String PROGRAM_PATH = "out/production/vbpmn/";
    public static final String PROGRAM = "pif2lnt.py";
    public static final String OPTIONS = "--lazy"; // use --lazy not to recompute, empty string to recompute
    public static final String SUFFIX = ".pif";

    /**
     * Provides data for tests based on the list of all .pif files in the example directory
     */
    @DataProvider(name = "directory_walker_provider")
    public Iterator<Object[]> directory_walker_provider() {
        List<Object[]> data = new ArrayList<>();
        try {
            Object[] files = Files.walk(Paths.get(FILES_PATH))
                    .filter(x -> x.toFile().getName().endsWith(SUFFIX))
                    .toArray();
            for (Object file : files) {
                Object[] value = new Object[1];
                value[0] = file;
                data.add(value);
            }
        } catch (IOException e) {
            fail();
        }
        return data.iterator();
    }

    /**
     * Performs the Pif2Lnt transformation on all examples
     */
    @Test(dataProvider = "directory_walker_provider", groups = "reading")
    public void test_read_all_files_with_Reader(Path filePath) {
        int exitValue;
        Path python_dir = Paths.get(PROGRAM_PATH+PROGRAM);
        String python_script = python_dir.toAbsolutePath().toString();
        File file = filePath.toFile();
        String workingPath = file.getParent();
        String fileName = file.getName();
        CommandLine cmd = CommandLine.parse(String.format("%s -u %s %s %s", CMD, python_script, fileName, OPTIONS));
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workingPath));
        int expected_result_as_int = expected_result_as_int = 0;
        executor.setExitValue(expected_result_as_int);
        try {
            exitValue = executor.execute(cmd);
        } catch (IOException e) {
            System.out.println(e.getCause());
            fail();
        }
    }

}
