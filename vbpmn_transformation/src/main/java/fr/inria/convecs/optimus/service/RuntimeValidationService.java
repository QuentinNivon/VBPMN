/**
 * 
 */

package fr.inria.convecs.optimus.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import fr.inria.convecs.optimus.validator.RuntimeValidator;

import static fr.inria.convecs.optimus.nl_to_mc.Main.*;

/**
 * @author silverquick TODO: dirty implementation - add a resource interface to invoke service,
 *         implement JSON data.
 *
 */
@Path("/runtime")
public class RuntimeValidationService {

	private static final Logger logger = LoggerFactory.getLogger(RuntimeValidationService.class);
	private static final String REMOTE_PIF_FILE_LOCATION = OLD_WEBSITE ? "/home/quentin_nivon/nl_to_mc/public" : "/home/convecs/nivonq/nl_to_mc/public";
	private static final String LOCAL_PIF_FILE_LOCATION = "/home/quentin/Documents/VBPMN/vbpmn_transformation/src/main/resources";
	private static final String OUTPUT_PATH = AppProperty.getInstance().getFolder("OUTPUT_PATH");
	private static final String SCRIPTS_PATH = "/WEB-INF/classes";
	private static final String PIF_SCHEMA = "pif.xsd";

	@Context
	ServletContext servletContext;

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/plain")
	@Path("/verify")
	public Response verifyProperties(@FormDataParam("file1") InputStream fileStream1,
			@FormDataParam("file1") FormDataContentDisposition fileInfo1, 
			FormDataMultiPart formData) {

		Response httpResponse = null;
		try {      

			String outputDir = Files.createTempDirectory(Paths.get(OUTPUT_PATH), "vbpmn_")
					.toAbsolutePath().toString();

			File file1 = new File(outputDir + File.separator + fileInfo1.getFileName());

			Files.copy(fileStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);

			List<File> fileList = new ArrayList<File>();

			fileList.add(file1);

			httpResponse = processRequest(fileList, formData, outputDir);

		} catch (Exception e) {
			logger.error("Error processing request: ", e);
			throw VbpmnExceptionMapper.createWebAppException(e);
		}
		return httpResponse;

	}

	// TODO:Cleanup
	private Response processRequest(final List<File> fileList, final FormDataMultiPart formData,
			final String outputDir) {

		String scriptsPath = servletContext.getRealPath(SCRIPTS_PATH);

		Response httpResponse;
		try {
			if (fileList.size() > 2 || fileList.size() <= 0) {
				httpResponse = Response.status(Response.Status.BAD_REQUEST)
						.entity("You can only specify 1 or 2 files").build();
			} else {
				ModelValidator validator = new RuntimeValidator(scriptsPath, outputDir);
				String result;
				List<String> operationMode = new ArrayList<String>();
				if (fileList.size() == 2) {
					String mode = formData.getField("mode").getValue();
					String option = formData.getField("option").getValue();

					
					operationMode.add(mode);

					if (mode.equals("property-and") || mode.equals("property-implied")) {
						String formula = formData.getField("formula").getValue();
						operationMode.add("--formula");
						operationMode.add(formula);
					} else if (option.equals("hiding")) {
						String hidingValue = formData.getField("hidingVal").getValue();
						if(hidingValue != null && !hidingValue.isEmpty()) {
							operationMode.add("--hiding");
							operationMode.add(hidingValue);
							if (null != formData.getField("exposeMode")) {
								operationMode.add("--exposemode");
							}
						}
						String renameValue = formData.getField("renameVal").getValue();
						if(renameValue != null && !renameValue.isEmpty())
						{
							String renameOption = formData.getField("renameOption").getValue();
							operationMode.add("--renaming");
							operationMode.add(renameValue);
							if (!(renameOption.equals("none"))) {
								operationMode.add("--renamed");
								operationMode.add(renameOption);
							}
						}
					}
					File input1 = parseAndTransform(fileList.get(0));
					File input2 = parseAndTransform(fileList.get(1));
					validator.validateV2(input1, input2, operationMode);
				} 
				else 
				{
					operationMode.add("property-implied");
					String formula = formData.getField("formula").getValue();
					operationMode.add("--formula");
					operationMode.add(formula);
					
					File input1 = parseAndTransform(fileList.get(0));
					validator.validateV2(input1, operationMode);
				}
				result = validator.getResult();
				httpResponse = Response.status(Response.Status.OK)
						   .header("Access-Control-Allow-Origin", "*")
				            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				            .entity(result).build();
			}
		} catch (Exception e) {
			logger.error("Exception while invoking VBPMN", e);
			throw new RuntimeException(e);
		}
		return httpResponse;
	}

	public File parseAndTransform(File input) {
		try {
			String pifSchema = LOCAL_SITE ? LOCAL_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA : LOCAL_TESTING ?
					LOCAL_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA :
					REMOTE_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA;

			ContentHandler baseHandler = new BaseContentHandler(input);
			baseHandler.handle();
			Process processOutput = (Process) baseHandler.getOutput();

			String outputFileName = new StringBuilder().append(input.getParentFile().getAbsolutePath())
					.append(File.separator).append(processOutput.getId()).append(".pif").toString();
			File outputFile = new File(outputFileName);

			ContentTransformer baseTransformer = new BaseContentTransformer(processOutput, outputFile);
			baseTransformer.transform();
			if (XmlUtil.isDocumentValid(outputFile, new File(pifSchema))) {
				return outputFile;
			} else {
				throw new RuntimeException(
						"Unable to transform the file <Schema Validation Error>: " + input.getName());
			}
		} catch (Exception e) {
			logger.error("Unable to parse and transform the file ", e);
			throw new RuntimeException(e);
		}
	}
}
