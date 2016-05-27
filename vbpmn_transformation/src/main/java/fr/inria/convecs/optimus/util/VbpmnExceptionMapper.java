/**
 * 
 */

package fr.inria.convecs.optimus.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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
    if (exception instanceof IllegalArgumentException) {
      logger.error("IllegalArgumentException ", exception);
      webApplicationException = new WebApplicationException(
          Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build());
    } else if (exception instanceof IllegalStateException) {
      logger.error("IllegalStateException ", exception);
      webApplicationException = new WebApplicationException(
          Response.status(Response.Status.CONFLICT).entity(exception.getMessage()).build());
    } else {
      logger.error("System exception ", exception);
      webApplicationException = new WebApplicationException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build());
    }
    return webApplicationException;
  }

}
