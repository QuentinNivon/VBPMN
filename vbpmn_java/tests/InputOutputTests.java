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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import models.base.*;
import models.process.pif.*;
import models.process.pif.generated.*;
import models.process.pif.generated.Process;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public class InputOutputTests {

    public static final String FILES_PATH = "out/test/vbpmn/pif/";
    public static final String OUTFILES_PATH = "out/test/vbpmn/pif/";
    public static final String SUFFIX = ".pif";
    public static final String GENPREFIX = "gen_";
    public static final String COPYSUFFIX = "_copy";
    public static final String EXCLUSIONS[] = {COPYSUFFIX};
    public static final String USELESS[] = {COPYSUFFIX, GENPREFIX};
    public static List<String> EXCLUSION_LIST;
    public static List<String> USELESS_LIST;

    /**
     * Sets up the file exclusion list (based on filenames)
     */
    @BeforeClass
    public void setUp() {
        EXCLUSION_LIST = Arrays.asList(EXCLUSIONS);
        USELESS_LIST = Arrays.asList(USELESS);
    }

    /**
     * Deletes files generated by the tests
     */
    @AfterClass
    public void tearDown() {
        try {
            Object[] files = Files.walk(Paths.get(FILES_PATH))
                    .filter(x -> isUseless(x.toFile().getName()))
                    .toArray();
            for (Object file : files) {
                Files.delete((Path) file);
            }
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Decides if a file should be excluded from tests
     *
     * @param filename name of the file
     * @return true if excluded, false else
     */
    public boolean isExcluded(String filename) {
        boolean rtr = false;
        for (String exclusion : EXCLUSION_LIST) {
            if (filename.contains(exclusion)) {
                rtr = true;
                break;
            }
        }
        return rtr;
    }

    /**
     * Decides if a file should be deleted after the tests (since created by them and useless)
     *
     * @param filename name of the file
     * @return true if useless, false else
     */
    public boolean isUseless(String filename) {
        boolean rtr = false;
        for (String exclusion : USELESS_LIST) {
            if (filename.contains(exclusion)) {
                rtr = true;
                break;
            }
        }
        return rtr;
    }

    /**
     * Provides data for tests based on the list of all .pif files in the example directory
     * in case it is used with the test_readWrite test, do not take copies (_copy.pif files) into account
     */
    @DataProvider(name = "directory_walker_provider")
    public Iterator<Object[]> directory_walker_provider() {
        List<Object[]> data = new ArrayList<>();
        try {
            Object[] files = Files.walk(Paths.get(FILES_PATH))
                    .filter(x -> x.toFile().getName().endsWith(SUFFIX) && !isExcluded(x.toFile().getName()))
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

    public final String getOutputFilenameFromInputFilename(String filename, String suffix1, String suffix2) {
        return String.format("%s%s", filename.substring(0, filename.length() - suffix1.length()), suffix2);
    }

    /**
     * Reads all examples (using PifPifReader) and dumps them in graphical format (using DotPifWriter)
     */
    @Test(dataProvider = "directory_walker_provider")
    public void test_read(Path filePath) {
        AbstractModelReader reader = new PifPifReader();
        AbstractModelWriter writer = new DotPifWriter();
        AbstractModelFactory factory = PifFactory.getInstance();
        AbstractModel model = factory.create();
        try {
            model.setResource(filePath.toFile());
            model.modelFromFile(reader);
            model.setResource(new File(getOutputFilenameFromInputFilename(filePath.toFile().getAbsolutePath(),
                    reader.getSuffix(),
                    writer.getSuffix())));
            model.modelToFile(writer);
        } catch (IllegalResourceException | IllegalModelException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_write() {
        ObjectFactory objectFactory = new ObjectFactory();
        AbstractModelWriter writer = new PifPifWriter();
        AbstractModelFactory mfactory = PifFactory.getInstance();
        PifModel model = (PifModel) mfactory.create();
        //
        Peer p1 = objectFactory.createPeer();
        p1.setId("peer1");
        Peer p2 = objectFactory.createPeer();
        p2.setId("peer2");
        //
        Message m1 = objectFactory.createMessage();
        m1.setId("message1");
        Message m2 = objectFactory.createMessage();
        m2.setId("message2");
        //
        InitialEvent start = objectFactory.createInitialEvent();
        start.setId("start");
        EndEvent stop = objectFactory.createEndEvent();
        stop.setId("stop");
        Task task = objectFactory.createTask();
        task.setId("task");
        Interaction exchange1 = objectFactory.createInteraction();
        exchange1.setId("x1");
        exchange1.setMessage(m1);
        exchange1.setInitiatingPeer(p1);
        exchange1.getReceivingPeers().add(objectFactory.createInteractionReceivingPeers(p2));
        //
        Workflow w = objectFactory.createWorkflow();
        w.getNodes().add(start);
        w.getNodes().add(stop);
        w.getNodes().add(task);
        w.getNodes().add(exchange1);
        w.setInitialNode(start);
        w.getFinalNodes().add(objectFactory.createWorkflowFinalNodes(stop));
        //
        SequenceFlow sf1 = objectFactory.createSequenceFlow();
        sf1.setId("sf1");
        sf1.setSource(start);
        sf1.setTarget(task);
        start.getOutgoingFlows().add(objectFactory.createWorkflowNodeOutgoingFlows(sf1));
        task.getIncomingFlows().add(objectFactory.createWorkflowNodeIncomingFlows(sf1));
        SequenceFlow sf2 = new SequenceFlow();
        sf2.setId("sf2");
        sf2.setSource(task);
        sf2.setTarget(exchange1);
        task.getOutgoingFlows().add(objectFactory.createWorkflowNodeOutgoingFlows(sf2));
        exchange1.getIncomingFlows().add(objectFactory.createWorkflowNodeIncomingFlows(sf2));
        SequenceFlow sf3 = new SequenceFlow();
        sf3.setId("sf3");
        sf3.setSource(exchange1);
        sf3.setTarget(stop);
        exchange1.getOutgoingFlows().add(objectFactory.createWorkflowNodeOutgoingFlows(sf3));
        stop.getIncomingFlows().add(objectFactory.createWorkflowNodeIncomingFlows(sf3));
        w.getSequenceFlows().add(sf1);
        w.getSequenceFlows().add(sf2);
        w.getSequenceFlows().add(sf3);
        //
        Process p = objectFactory.createProcess();
        p.setName("basic1");
        p.setDocumentation("test process basic 1");
        p.setBehaviour(w);
        p.getPeers().add(p1);
        p.getPeers().add(p2);
        p.getMessages().add(m1);
        p.getMessages().add(m2);
        model.setModel(p);
        try {
            model.setResource(new File(String.format("%s%s%s%s", OUTFILES_PATH, GENPREFIX, p.getName(), SUFFIX)));
            model.modelToFile(writer);
            assertEquals(true, true);
        } catch (IllegalResourceException | IllegalModelException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(dataProvider = "directory_walker_provider")
    public void test_read_write(Path filePath) {
        AbstractModelReader reader = new PifPifReader();
        AbstractModelWriter writer = new PifPifWriter();
        AbstractModel model = PifFactory.getInstance().create();
        try {
            model.setResource(filePath.toFile());
            model.modelFromFile(reader);
            model.setResource(new File(filePath.toString() + COPYSUFFIX + SUFFIX));
            model.modelToFile(writer);
            assertEquals(true, true);
        } catch (IOException | IllegalResourceException | IllegalModelException e) {
            e.printStackTrace();
            fail();
        }

    }

}