package fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics;

import fr.inria.convecs.optimus.util.Triple;

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
																	 final boolean generateLTS);
	public abstract Triple<Integer, String, Collection<String>> load(final String pifFileName,
																	 final boolean generateLTS,
																	 final boolean smartReduction,
																	 final boolean debug);
	public abstract Triple<Integer, String, Collection<String>> generate(final String pifFileName);
	public abstract Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																		 final boolean generateLTS);
	public abstract Triple<Integer, String, Collection<String>> generate(final String pifFileName,
																		 final boolean generateLTS,
																		 final boolean smartReduction,
																		 final boolean debug);
}
