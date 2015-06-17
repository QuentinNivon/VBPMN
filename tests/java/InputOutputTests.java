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

import models.base.*;
import models.process.pif.*;
import models.process.pif.generated.Process;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import models.process.pif.generated.*;

import javax.xml.bind.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public class InputOutputTests {

    public static final String FILES_PATH = "out/test/vbpmn/pif/";
    public static final String OUTFILES_PATH = "out/test/vbpmn/pif/";
    public static final String SUFFIX = ".pif";
    public static final String GENPREFIX = "gen_";
    public static final String SCHEMA_PATH = "model/pif.xsd";

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
     * Reads all examples (using PifPifReader) and dumps them in graphical format (using DotPifWriter)
     */
    @Test(dataProvider = "directory_walker_provider", groups = "file_reading")
    public void test_read_all_files_with_Reader(Path filePath) {
        AbstractModelReader reader = new PifPifReader();
        AbstractModelWriter writer = new DotPifWriter();
        AbstractModelFactory factory = PifFactory.getInstance();
        AbstractModel model = factory.create();
        try {
            model.setResource(filePath.toFile());
            model.modelFromFile(reader);
            model.setResource(new File(filePath.getFileName()+".dot"));
            model.modelToFile(writer);
        } catch (IllegalResourceException e) {
            e.printStackTrace();
            fail();
        } catch (IllegalModelException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(groups = "file_writing")
    public void test_writeToFile() {
        //
        ObjectFactory factory = new ObjectFactory();
        //
        Workflow w = factory.createWorkflow();
        InitialEvent n1 = factory.createInitialEvent();
        n1.setId("n1");
        EndEvent n2 = factory.createEndEvent();
        n2.setId("n2");
        Task t = factory.createTask();
        t.setId("t");
        //
        SequenceFlow sf1 = factory.createSequenceFlow();
        sf1.setId("sf1");
        SequenceFlow sf2 = factory.createSequenceFlow();
        sf2.setId("sf2");
        sf1.setSource(n1);
        sf1.setTarget(t);
        sf2.setSource(t);
        sf2.setTarget(n2);
        n1.getOutgoingFlows().add(sf1);
        t.getIncomingFlows().add(sf1);
        t.getOutgoingFlows().add(sf2);
        n2.getIncomingFlows().add(sf2);
        //
        w.getNodes().add(n1);
        w.setInitialNode(n1);
        w.getNodes().add(n2);
        w.getFinalNodes().add(n2);
        w.getNodes().add(t);
        w.getSequenceFlows().add(sf1);
        w.getSequenceFlows().add(sf2);
        //
        Peer p1 = factory.createPeer();
        p1.setId("p1");
        Message m1 = factory.createMessage();
        m1.setId("m1");
        //
        models.process.pif.generated.Process p = factory.createProcess();
        p.setName("t0000");
        p.setDocumentation("A simple process");
        p.setBehaviour(w);
        p.getPeers().add(p1);
        //
        AbstractModelWriter writer = new PifPifWriter();
        AbstractModelFactory mfactory = PifFactory.getInstance();
        PifModel model = (PifModel) mfactory.create();
        model.setModel(p);
        try {
            model.setResource(new File(String.format("%s%s%s%s", OUTFILES_PATH, GENPREFIX, p.getName(), SUFFIX)));
            model.modelToFile(writer);
            final JAXBContext ctx;
            assertEquals(true, true);
        } catch (IllegalResourceException e) {
            e.printStackTrace();
            fail();
        } catch (IllegalModelException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(dependsOnGroups = {"file_reading", "file_writing"})
    public void test_readWrite() {
        AbstractModelReader reader = new PifPifReader();
        AbstractModelWriter writer = new PifPifWriter();
        AbstractModel model = PifFactory.getInstance().create();
        String file1 = String.format("%s%s%s%s", OUTFILES_PATH, GENPREFIX, "t0000", SUFFIX);
        String file2 = String.format("%s%s%s%s", OUTFILES_PATH, GENPREFIX, "t0000_copy", SUFFIX);
        try {
            model.setResource(new File(file1));
            model.modelFromFile(reader);
            model.setResource(new File(file2));
            model.modelToFile(writer);
            assertEquals(true,true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } catch (IllegalResourceException e) {
            e.printStackTrace();
            fail();
        } catch (IllegalModelException e) {
            e.printStackTrace();
            fail();
        }

    }

}