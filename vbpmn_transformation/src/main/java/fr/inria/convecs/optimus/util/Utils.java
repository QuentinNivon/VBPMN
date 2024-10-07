package fr.inria.convecs.optimus.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class Utils
{
	private static final long MICROSECONDS_THRESHOLD = 1000;
	private static final long MILLISECONDS_THRESHOLD = 1000000;
	private static final long SECONDS_THRESHOLD = 1000000000;
	private static final long MINUTES_THRESHOLD = 60000000000L;
	private static final long HOURS_THRESHOLD = 3600000000000L;
	private static final long DAYS_THRESHOLD = 86400000000000L;
	private static final String LNT_SINGLE_INDENT = "   ";

	private Utils()
	{

	}

	public static String indent(int size)
	{
		final StringBuilder indentBuilder = new StringBuilder();

		for (int i = 0; i < size; i++)
		{
			indentBuilder.append(" ");
		}

		return indentBuilder.toString();
	}

	public static String indentLNT(int nbIndent)
	{
		final StringBuilder indentBuilder = new StringBuilder();

		for (int i = 0; i < nbIndent; i++)
		{
			indentBuilder.append(LNT_SINGLE_INDENT);
		}

		return indentBuilder.toString();
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

	public static String nanoSecToReadable(final long nanoseconds)
	{
		final DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);

		if (nanoseconds > DAYS_THRESHOLD)
		{
			return df.format((double) nanoseconds / (double) DAYS_THRESHOLD) + " days";
		}
		else if (nanoseconds > HOURS_THRESHOLD)
		{
			return df.format((double) nanoseconds / (double) HOURS_THRESHOLD) + "h";
		}
		else if (nanoseconds > MINUTES_THRESHOLD)
		{
			return df.format((double) nanoseconds / (double) MINUTES_THRESHOLD) + "m";
		}
		else if (nanoseconds > SECONDS_THRESHOLD)
		{
			//More than 1sec
			return df.format((double) nanoseconds / (double) SECONDS_THRESHOLD) + "s";
		}
		else if (nanoseconds > MILLISECONDS_THRESHOLD)
		{
			//More than 1ms
			return df.format((double) nanoseconds / (double) MILLISECONDS_THRESHOLD) + "ms";
		}
		else if (nanoseconds > MICROSECONDS_THRESHOLD)
		{
			//More than 1µs
			return df.format((double) nanoseconds / (double) MICROSECONDS_THRESHOLD) + "µs";
		}
		else
		{
			//Value in nanoseconds
			return df.format((double) nanoseconds) + "ns";
		}
	}

	public static String getStackTrace(final Throwable throwable)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	public static String generateRandomIdentifier()
	{
		return Utils.generateRandomIdentifier(30);
	}

	public static String generateRandomIdentifier(final int length)
	{
		final StringBuilder builder = new StringBuilder();
		final Random random = new Random();

		for (int i = 0; i < length; i++)
		{
			final char c;

			//CAPS
			if (random.nextBoolean())
			{
				c = (char) (random.nextInt(25) + 65 + 1); //Exclusive upper bound
			}
			//NON CAPS
			else
			{
				c = (char) (random.nextInt(25) + 97 + 1); //Exclusive upper bound
			}

			builder.append(c);
		}

		return builder.toString();
	}

	public static String multiTab(final int nbTab)
	{
		final StringBuilder tabBuilder = new StringBuilder();
		int i = 0;

		while (i < nbTab)
		{
			tabBuilder.append("		");
			i++;
		}

		return tabBuilder.toString();
	}
}
