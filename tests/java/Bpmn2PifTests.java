import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.fail;

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

public class Bpmn2PifTests {

    public static final String FILES_PATH = "out/test/vbpmn";
    public static final String SUFFIX1 = ".bpmn";
    public static final String SUFFIX2 = ".pif";

    /**
     * Provides data for tests based on the list of all .pif files in the example directory
     */
    @DataProvider(name = "directory_walker_provider")
    public Iterator<Object[]> directory_walker_provider() {
        List<Object[]> data = new ArrayList<>();
        try {
            Object[] files = Files.walk(Paths.get(FILES_PATH))
                    .map(x -> x.toFile().getPath())
                    .filter(x -> x.endsWith(SUFFIX1))
                    .toArray();
            for(Object filename1 : files) {
                Object[] value = new Object[2];
                value[0] = filename1;
                String filename2 = ((String)filename1).substring(0,((String)filename1).length()-SUFFIX1.length())+SUFFIX2;
                value[1] = filename2;
                data.add(value);
            }
        } catch (IOException e) {
            fail();
        }
        return data.iterator();
    }

    /**
     * Transforms BPMN into PIF
     */
    @Test(dataProvider = "directory_walker_provider")
    public void test_bpmn2pif(String fin, String fout) {

    }

}
