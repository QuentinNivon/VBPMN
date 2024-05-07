package fr.inria.convecs.optimus.nl_to_mc;

import java.util.Arrays;
import java.util.List;

public class LTLKeyword
{
	public static final String UNTIL = "U";
	public static final String WEAK_UNTIL = "W";
	public static final String RELEASE = "R";
	public static final String STRONG_RELEASE = "M";
	public static final String FINALLY = "F";
	public static final String GLOBALLY = "G";
	public static final String NEXT = "X";

	public static final List<String> ALL_KEYWORDS = Arrays.asList(
			LTLKeyword.FINALLY,
			LTLKeyword.GLOBALLY,
			LTLKeyword.NEXT,
			LTLKeyword.UNTIL,
			LTLKeyword.WEAK_UNTIL,
			LTLKeyword.RELEASE,
			LTLKeyword.STRONG_RELEASE
	);

	private LTLKeyword()
	{

	}
}
