package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExceptionStatus;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPTManager
{
	private static final String SYSTEM_BASE = "You are an helpful Linear Temporal Logic (LTL) assistant. The user" +
			" provides you a sentence written in natural language that you have to translate into the corresponding LTL" +
			" formula. Your output should contain only the LTL formula, without any surrounding text to" +
			" explain it.";
	private static final String BASE_MODEL = "gpt-4-turbo";
	private static final String FINE_TUNED_MODEL_1 = "ft:gpt-3.5-turbo-0125:personal:inria-nivon-salaun:9Loa9FXv";
	private static final String URL = "https://api.openai.com/v1/chat/completions";
	private static final String REQUEST_METHOD = "POST";
	private static final String USER_ROLE = "user";
	private static final String SYSTEM_ROLE = "system";
	private static final double TEMPERATURE = 0; //Set to 0 so that the model behaves deterministically
	private static final double TOP_P = 0;
	private static final double FREQUENCE_PENALTY = 0;
	private static final double PRESENCE_PENALTY = 0;
	private static final int MAX_TOKENS = 12540;

	private ChatGPTManager()
	{

	}

	public static String generateAnswer(final String question,
										final String apiKey) throws ExpectedException
	{
		try
		{
			final URL url = new URL(URL);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(REQUEST_METHOD);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + apiKey);
			connection.setDoOutput(true);

			final String body = "{\"model\": \"" + FINE_TUNED_MODEL_1 + "\"," +
					"\"messages\": [" +
					"{\"role\": \"" + SYSTEM_ROLE + "\", \"content\": \"" + SYSTEM_BASE + "\"}" + ", " +
					"{\"role\": \"" + USER_ROLE + "\", \"content\": \"" + question + "\"}" +
					"]," +
					"\"temperature\": " + TEMPERATURE + ", " +
					"\"top_p\": " + TOP_P +
					"}";

			MyOwnLogger.append(body);

			final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final StringBuilder response = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null)
			{
				response.append(line);
			}

			reader.close();

			//System.out.println("Real answer: " + response);

			final JSONParser jsonParser = new JSONParser();
			final JSONObject jsonAnswer = (JSONObject) jsonParser.parse(response.toString());
			//System.out.println("JSON answer: " + jsonAnswer);
			final JSONArray dataArray = (JSONArray) jsonAnswer.get("choices");
			//System.out.println("Data array: " + dataArray);
			final JSONObject singleChoice = (JSONObject) dataArray.get(0);
			//System.out.println("Single choice: " + singleChoice);
			final JSONObject message = (JSONObject) singleChoice.get("message");
			//System.out.println("message : " + message);
			final String answer = (String) message.get("content");
			//System.out.println("JSON answer: " + answer);

			return answer;
		}
		catch (IOException | ParseException e)
		{
			throw new ExpectedException(e, ExceptionStatus.CHATGPT_IO);
		}
	}
}
