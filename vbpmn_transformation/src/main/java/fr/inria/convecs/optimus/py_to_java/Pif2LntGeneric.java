package fr.inria.convecs.optimus.py_to_java;

import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;

public abstract class Pif2LntGeneric
{
	protected boolean isBalanced;
	protected String outputFolder;

	public Pif2LntGeneric(final boolean isBalanced)
	{
		this.isBalanced = isBalanced;
	}
	public Pif2LntGeneric()
	{

	}

	public void setBalance(final boolean balance)
	{
		this.isBalanced = balance;
	}
	public void setOutputFolder(final String outputFolder)
	{
		this.outputFolder = outputFolder;
	}

	public abstract Triple<Integer, String, Collection<String>> load(final String pifFileName);
	public abstract Triple<Integer, String, Collection<String>> load(final String pifFileName,
																	 final boolean smartReduction,
																	 final boolean debug);
	public abstract Triple<Integer, String, Collection<String>> generate(final String pifFileName);
	public abstract Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																		 final boolean smartReduction,
																		 final boolean debug);
}
