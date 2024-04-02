package fr.inria.convecs.optimus.py_to_java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PyToJavaUtils
{
	private PyToJavaUtils()
	{

	}

	/**
	 * User-transparent method used to fill a parametrized string
	 * with its parameters.
	 * It is transparent in the sense that one can simply replace
	 * the ``MessageFormat.format()'' method with an equivalent
	 * one if desired, without any impact on the user.
	 *
	 * @param mainString the parametrized string
	 * @param parameters the parameters to fill the string with
	 * @return the original string filled with its parameters
	 */
	public static String parametrize(final String mainString,
									 final String... parameters)
	{
		return MessageFormat.format(mainString, (Object[]) parameters);
	}

	/**
	 * User-transparent method corresponding to the String.join(List<T>)
	 * Python method.
	 *
	 * @param elements the list of elements to concatenate
	 * @return a string corresponding to the concatenation of @elements
	 */
	public static String join(final List<?> elements)
	{
		final StringBuilder builder = new StringBuilder();
		String separator = "";

		for (Object element : elements)
		{
			builder.append(separator)
					.append(element.toString());

			separator = ",";
		}

		return builder.toString();
	}
}
