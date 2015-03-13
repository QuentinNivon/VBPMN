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
import models.process.pif.generated.*;
import models.process.pif.generated.Process;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class InputOutputTests {

    public static final String SCHEMA_PATH = "model/pif.xsd";
    public static final String FILES_PATH = "examples";
    public static final String SUFFIX = ".pif";

    /**
     * Provides data for tests based on the list of all .pif files in the example directory
     */
    @DataProvider(name = "directory_walker_provider")
    public Object[][] directory_walker_provider() {
        Object[][] o = null;
        try {
            Object[] paths = Files.walk(Paths.get(FILES_PATH))
                    .filter(x -> x.toFile().getName().endsWith(SUFFIX))
                    .toArray();
            int i = paths.length;
            o = new Object[i][];
            for (int j = 0; j < i; j++) {
                o[j] = new Object[1];
                o[j][0] = paths[j];
            }
        } catch (IOException e) {
            fail();
        }
        return o;
    }

    /**
     * Reads all examples
     */
    @Test(dataProvider = "directory_walker_provider")
    public void test_read_all_files(Path filePath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new File(SCHEMA_PATH)));
            if (filePath.toFile().getName().endsWith(SUFFIX)) {
                FileInputStream fis = new FileInputStream(filePath.toFile().getCanonicalPath());
                JAXBContext ctx = null;
                ctx = JAXBContext.newInstance(Process.class);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                unmarshaller.setSchema(schema);
                Process p = (Process) unmarshaller.unmarshal(fis);
                fis.close();
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            fail();
        } catch (IOException e1) {
            e1.printStackTrace();
            fail();
        } catch (SAXException e1) {
            e1.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_writeToFile() {
        ObjectFactory factory = new ObjectFactory();
        //
        WorkflowNode n1 = new InitialEvent();
        n1.setId("initial");
        WorkflowNode n2 = new EndEvent();
        n2.setId("final");
        SequenceFlow s1 = new SequenceFlow();
        s1.setId("s");
        //
        s1.setSource(n1);
        s1.setTarget(n2);
        n1.getOutgoingFlows().add(s1);
        n2.getIncomingFlows().add(s1);
        //
        Workflow w = new Workflow();
        w.getNodes().add(n1);
        w.getNodes().add(n2);
        w.setInitialNode((InitialEvent) n1);
        w.getFinalNodes().add((EndEvent) n2);
        //
        models.process.pif.generated.Process p = new Process();
        p.setName("t0000");
        p.setDocumentation("A simple process");
        p.setBehaviour(w);
        //
        final JAXBContext ctx;
        try {
            FileOutputStream fos = new FileOutputStream("tests/examples/gen_" + p.getName() + ".pif");
            ctx = JAXBContext.newInstance(Process.class);
            final Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(p, fos);
            fos.close();
            assertEquals(true, true);
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_readWrite() {
        Process p;
        FileInputStream fis;
        FileOutputStream fos;
        try {
            fis = new FileInputStream("tests/examples/p0001.pif");
            JAXBContext ctx = JAXBContext.newInstance(Process.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            p = (Process) unmarshaller.unmarshal(fis);
            fis.close();
            //
            fos = new FileOutputStream("tests/examples/gen_t0000.pif");
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(p, fos);
            fos.close();
            //
            fis = new FileInputStream("tests/examples/gen_t0000.pif");
            ctx = JAXBContext.newInstance(Process.class);
            unmarshaller = ctx.createUnmarshaller();
            p = (Process) unmarshaller.unmarshal(fis);
            fis.close();
            assertEquals(p.getName(), "p0000");
            assertEquals(p.getBehaviour().getNodes().size(), 8);
            //
            fos = new FileOutputStream("tests/examples/gen_r0000.pif");
            marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(p, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}