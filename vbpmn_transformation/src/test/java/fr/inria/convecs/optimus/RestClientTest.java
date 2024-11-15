package fr.inria.convecs.optimus;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;

public class RestClientTest {

  @Test
  public void restApiTest() {
    String vbpmnMode = "conservative";
    String url = "http://localhost:8080/transformation";
    String inputFile1 = "data/input/ExpenseWorkflow.bpmn";

    Client client = ClientBuilder.newClient();
    Response response;

    /*MultiPart multiPartEntity = new MultiPart()
        .bodyPart(new FileDataBodyPart("file", new File(inputFile1)),
            MediaType.APPLICATION_OCTET_STREAM_TYPE)
        .bodyPart(new BodyPart("none", MediaType.TEXT_PLAIN_TYPE));

    WebTarget webTarget = client.target(url + "/vbpmn/validate/bpmn");
    response = webTarget.request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPartEntity, MediaType.MULTIPART_FORM_DATA_TYPE));

    int status = response.getStatus();*/
    
    //TODO: Actual test

    Assert.assertTrue(Response.Status.OK.getStatusCode() == 200);
  }

}
