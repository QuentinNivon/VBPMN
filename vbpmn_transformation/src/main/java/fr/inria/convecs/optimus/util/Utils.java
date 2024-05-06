package fr.inria.convecs.optimus.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils
{
	private static final long MICROSECONDS_THRESHOLD = 1000;
	private static final long MILLISECONDS_THRESHOLD = 1000000;
	private static final long SECONDS_THRESHOLD = 1000000000;
	private static final long MINUTES_THRESHOLD = 60000000000L;
	private static final long HOURS_THRESHOLD = 3600000000000L;
	private static final long DAYS_THRESHOLD = 86400000000000L;

	private Utils()
	{

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
}
