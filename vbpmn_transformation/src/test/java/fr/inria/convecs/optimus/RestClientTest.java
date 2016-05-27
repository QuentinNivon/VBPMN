package fr.inria.convecs.optimus;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

public class RestClientTest {

  @Test
  public void restApiTest() {
    String vbpmnMode = "conservative";
    String url = "http://localhost:8080/transformation";
    String inputFile1 = "data/input/ExpenseWorkflow.bpmn";

    Client client = ClientBuilder.newClient();
    ;
    Response response;

    MultiPart multiPartEntity = new MultiPart()
        .bodyPart(new FileDataBodyPart("file", new File(inputFile1)),
            MediaType.APPLICATION_OCTET_STREAM_TYPE)
        .bodyPart(new BodyPart("none", MediaType.TEXT_PLAIN_TYPE));

    WebTarget webTarget = client.target(url + "/vbpmn/validate/bpmn");
    response = webTarget.request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPartEntity, MediaType.MULTIPART_FORM_DATA_TYPE));

    int status = response.getStatus();

    Assert.assertTrue(Response.Status.OK.getStatusCode() == status);
  }

}
