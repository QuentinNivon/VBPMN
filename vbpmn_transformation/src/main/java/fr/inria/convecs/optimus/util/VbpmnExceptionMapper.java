/**
 * 
 */

package fr.inria.convecs.optimus.util;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author silverquick
 *
 */
public class VbpmnExceptionMapper {

  private static final Logger logger = LoggerFactory.getLogger(VbpmnExceptionMapper.class);

  /**
   * default constructor
   */
  protected VbpmnExceptionMapper() {

  }

  /**
   * Custom exception handler.
   * TODO: Cleanup exception handling across project
   * @param exception
   *          app exception
   * @return WebApplicationException
   */

  public static WebApplicationException createWebAppException(final Exception exception) {
    WebApplicationException webApplicationException = null;
    String exceptionMessage = new StringBuilder("Error processing the request. Please contact the team")
    		.append("\n \n")
    		.append("The exception message is: ")
    		.append("\n")
    		.append(exception.getMessage()).toString();
    if (exception instanceof IllegalArgumentException) {
      logger.error("IllegalArgumentException ", exception);
      webApplicationException = new WebApplicationException(
          Response.status(Response.Status.BAD_REQUEST).entity(exceptionMessage).build());
    } else if (exception instanceof IllegalStateException) {
      logger.error("IllegalStateException ", exception);
      webApplicationException = new WebApplicationException(
          Response.status(Response.Status.CONFLICT).entity(exceptionMessage).build());
    } else {
      logger.error("Internal server errror", exception);
  
      webApplicationException = new WebApplicationException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionMessage).build());
    }
    return webApplicationException;
  }

}
