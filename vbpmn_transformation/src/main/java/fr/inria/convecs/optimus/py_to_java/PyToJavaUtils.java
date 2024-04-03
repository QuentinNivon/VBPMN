package fr.inria.convecs.optimus.py_to_java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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

	/**
	 * In this method, we want to compute all the combinations of elements of a set.
	 * For example, getCombinationsOf([1,2,3]) returns [[1], [2], [3], [1,2], [1,3],
	 * [2,3], [1,2,3]].
	 *
	 * @param elements the elements to combine
	 * @return the list of all possible combinations of the elements
	 * @param <T> any type
	 */
	public static <T> Collection<Collection<T>> getCombinationsOf(Collection<T> elements)
	{
		final Collection<Collection<T>> combinations = new ArrayList<>();
		final ArrayList<T> sortedElements = new ArrayList<>(elements);

		PyToJavaUtils.getCombinationsOf(combinations, new ArrayList<>(), sortedElements);

		return combinations;
	}

	private static <T> void getCombinationsOf(Collection<Collection<T>> allCombinations,
											  Collection<T> currentCombination,
											  List<T> remainingElements)
	{
		if (remainingElements.isEmpty())
		{
			return;
		}

		for (int i = 0; i < remainingElements.size(); i++)
		{
			final List<T> newRemainingElements = new ArrayList<>(remainingElements);

			int toRemove = 0;

			//Avoid duplicates
			while (toRemove < i)
			{
				newRemainingElements.remove(0);
				toRemove++;
			}

			final List<T> currentCombinationCopy = new ArrayList<>(currentCombination);
			currentCombinationCopy.add(newRemainingElements.remove(0));
			allCombinations.add(currentCombinationCopy);

			PyToJavaUtils.getCombinationsOf(allCombinations, currentCombinationCopy, newRemainingElements);
		}
	}
}
