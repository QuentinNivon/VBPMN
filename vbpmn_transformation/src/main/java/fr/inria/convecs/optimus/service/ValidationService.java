/**
 * 
 */
package fr.inria.convecs.optimus.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.convecs.optimus.config.AppProperty;
import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.VbpmnExceptionMapper;
import fr.inria.convecs.optimus.util.XmlUtil;
import fr.inria.convecs.optimus.validator.ModelValidator;
import fr.inria.convecs.optimus.validator.VbpmnValidator;


/**
 * @author silverquick
 * TODO: dirty implementation - add a resource interface to invoke service, implement JSON data
 *
 */
@Path("/validate")
public class ValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

	//private static final String UPLOAD_PATH = "C:\\Users\\ajayk\\Documents\\temp\\";
	private static final String UPLOAD_PATH = AppProperty.getInstance().getValue("UPLOAD_PATH");

	private static final String TRANSFORM_LOCATION = AppProperty.getInstance().getValue("TRANSFORM_LOCATION");

	private static final String PIF_SCHEMA = AppProperty.getInstance().getValue("PIF_SCHEMA");

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/plain")
	@Path("/bpmn")
	public Response validateVbpmn(@FormDataParam("file1") InputStream fileStream1,
			@FormDataParam("file1") FormDataContentDisposition fileInfo1,
			@FormDataParam("file2") InputStream fileStream2,
			@FormDataParam("file2") FormDataContentDisposition fileInfo2,
			FormDataMultiPart formData) 
	{

		Response httpResponse = null;
		try 
		{

			File file1 = new File(UPLOAD_PATH+fileInfo1.getFileName());
			File file2 = new File(UPLOAD_PATH+fileInfo2.getFileName());

			Files.copy(fileStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(fileStream2, file2.toPath(), StandardCopyOption.REPLACE_EXISTING);

			List<File> fileList = new ArrayList<File>();

			fileList.add(file1);
			fileList.add(file2);

			httpResponse = processRequest(fileList, formData);


		}catch(Exception e)
		{
			logger.error("Error processing request: ", e);
			throw VbpmnExceptionMapper.createWebAppException(e);
		}
		return httpResponse;

	}

	//TODO:Cleanup
	private Response processRequest(final List<File> fileList, final FormDataMultiPart formData) {

		Response httpResponse;
		try 
		{
			String mode = formData.getField("mode").getValue();
			String option = formData.getField("option").getValue();
			String inputValue = formData.getField("inputValue").getValue();

			List<String> operationMode = new ArrayList<String>();
			operationMode.add(mode);

			if(mode.equals("property-and") || mode.equals("property-implied"))
			{
				String formula = formData.getField("formula").getValue();
				operationMode.add("--formula");
				operationMode.add(formula);
			}
			else
			{
				if(option.equals("hiding"))
				{
					operationMode.add("--hiding");
					operationMode.add(inputValue);
					if(null != formData.getField("exposeMode"))
					{
						operationMode.add("--exposemode");
					}
				}
				else if(option.equals("renaming"))
				{
					String renameOption = formData.getField("renameOption").getValue();
					operationMode.add("--renaming");
					operationMode.add(inputValue);
					operationMode.add("--renamed");
					if(!(renameOption.equals("none")))
					{
						operationMode.add("first");
					}
				}
			}

			if(fileList.size() > 2 || fileList.size() <= 0)
			{
				httpResponse = Response.status(Status.BAD_REQUEST).entity("You can only specify 1 or 2 files").build();
			}
			else 
			{
				ModelValidator validator = new VbpmnValidator();
				String result;

				if(fileList.size() == 2)
				{
					File input1 = parseAndTransform(fileList.get(0));
					File input2 = parseAndTransform(fileList.get(1));
					validator.validate(input1, input2, operationMode);
				}
				else
				{
					File input1 = parseAndTransform(fileList.get(0));
					validator.validate(input1, operationMode);
				}
				result = validator.getResult();
				httpResponse = Response.status(Status.OK).entity(result).build();
			}
		}catch(Exception e)
		{
			logger.error("Exception while invoking VBPMN", e);
			throw new RuntimeException(e);
		}
		return httpResponse;
	}

	private File parseAndTransform(File input) {
		try {
			ContentHandler baseHandler = new BaseContentHandler(input);
			baseHandler.handle();
			Process processOutput =(Process)baseHandler.getOutput();

			String outputFileName = new StringBuilder()
					.append(TRANSFORM_LOCATION)
					.append(processOutput.getId())
					.append(".pif").toString();
			File outputFile = new File(outputFileName);

			ContentTransformer baseTransformer = new BaseContentTransformer(processOutput, outputFile);
			baseTransformer.transform();
			if(XmlUtil.isDocumentValid(outputFile, new File(PIF_SCHEMA)))
				return outputFile;
			else
				throw new RuntimeException("Unable to transform the file <Schema Validation Error>: "+input.getName());
		} catch(Exception e)
		{
			logger.error("Unable to parse and transform the file ", e);
			throw new RuntimeException(e);
		}
	}
}
