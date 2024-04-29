package fr.inria.convecs.optimus.util;

public class Utils
{
	private Utils()
	{

	}

	public static boolean isAnInt(final char s)
	{
		try
		{
			Integer.parseInt(String.valueOf(s));
		}
		catch (NumberFormatException e)
		{
			return false;
		}

		return true;
	}

	public static boolean isAnInt(final String s)
	{
		try
		{
			Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return false;
		}

		return true;
	}

	/**
	 * Java 8 version of String.trim()
	 *
	 * @param s
	 * @return
	 */
	public static String trim(final String s)
	{
		final char[] chars = s.toCharArray();
		final StringBuilder builder = new StringBuilder();
		final StringBuilder buffer = new StringBuilder();

		for (char c : chars)
		{
			if (c == 0x20
				|| c == 0x09
				|| c == 0x0D
				|| c == 0x0A)
			{
				//Spaces (whitespace, tab, carriage return, line feed)
				if (!builder.toString().isEmpty())
				{
					/*
					 *	If the builder is not empty, add the spaces to the buffer.
					 *  Otherwise, do nothing because they are at the beginning of the
					 *  string and must thus be removed.
					 */
					buffer.append(c);
				}
			}
			else
			{
				//Non spaces: write the content of buffer and the character and clear the buffer
				builder.append(buffer)
						.append(c);
				buffer.setLength(0);
			}
		}

		return builder.toString();
	}

	public static String convertMonth(final int month)
	{
		switch (month)
		{
			case 0:
				return "January";

			case 1:
				return "February";

			case 2:
				return "March";

			case 3:
				return "April";

			case 4:
				return "May";

			case 5:
				return "June";

			case 6:
				return "July";

			case 7:
				return "August";

			case 8:
				return "September";

			case 9:
				return "October";

			case 10:
				return "November";

			case 11:
				return "December";

			default:
				throw new UnsupportedOperationException("Months indices should be between 0 and 11. Got " + month + " instead.");
		}
	}
}
