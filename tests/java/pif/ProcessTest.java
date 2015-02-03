package pif;

import org.testng.annotations.Test;
import models.process.pif.generated.*;
import models.process.pif.generated.Process;

import javax.xml.bind.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.testng.Assert.*;

public class ProcessTest {

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
        w.setInitialNode((InitialEvent)n1);
        w.getFinalNodes().add((EndEvent)n2);
        //
        models.process.pif.generated.Process p = new Process();
        p.setName("t0000");
        p.setDocumentation("A simple process");
        p.setBehaviour(w);
        //
        final JAXBContext ctx;
        try {
            FileOutputStream fos = new FileOutputStream("tests/examples/gen_"+p.getName()+".pif");
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
    public void test_readFromFile() {
        Process p;
        FileInputStream fis;
        try {
            fis = new FileInputStream("tests/examples/p0000.pif");
            JAXBContext ctx = JAXBContext.newInstance(Process.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            p = (Process) unmarshaller.unmarshal(fis);
            fis.close();
            assertEquals(p.getName(),"p0000");
            assertEquals(p.getBehaviour().getNodes().size(),8);
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

    @Test
    public void test_readWrite() {
        Process p;
        FileInputStream fis;
        FileOutputStream fos;
        try {
            fis = new FileInputStream("tests/examples/p0000.pif");
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
            assertEquals(p.getName(),"p0000");
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