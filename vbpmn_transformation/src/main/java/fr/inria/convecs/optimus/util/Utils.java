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
}
