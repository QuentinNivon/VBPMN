package fr.inria.convecs.optimus.nl_to_mc;

import java.util.Arrays;
import java.util.List;

public class LTLKeywords
{
	public static final String UNTIL = "U";
	public static final String WEAK_UNTIL = "W";
	public static final String RELEASE = "R";
	public static final String STRONG_RELEASE = "M";
	public static final String FINALLY = "F";
	public static final String GLOBALLY = "G";
	public static final String NEXT = "X";

	public static final List<String> ALL_KEYWORDS = Arrays.asList(
			LTLKeywords.FINALLY,
			LTLKeywords.GLOBALLY,
			LTLKeywords.NEXT,
			LTLKeywords.UNTIL,
			LTLKeywords.WEAK_UNTIL,
			LTLKeywords.RELEASE,
			LTLKeywords.STRONG_RELEASE
	);

	private LTLKeywords()
	{

	}
}
