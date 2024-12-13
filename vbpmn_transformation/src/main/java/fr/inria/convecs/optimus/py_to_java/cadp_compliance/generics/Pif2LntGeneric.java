package fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics;

import fr.inria.convecs.optimus.util.Triple;

import java.io.File;
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

	public Triple<Integer, String, Collection<String>> load(final File pifFileName)
	{
		return this.load(pifFileName, true, true, false);
	}

	public Triple<Integer, String, Collection<String>> load(final File pifFileName,
																	 final boolean generateLTS)
	{
		return this.load(pifFileName, generateLTS, true, false);
	}

	/**
	 * Gets the name and the alphabet of the LTS for the PIF model.
	 *
	 * @param pifFileName    is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug          is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	public abstract Triple<Integer, String, Collection<String>> load(final File pifFileName,
																	 final boolean generateLTS,
																	 final boolean smartReduction,
																	 final boolean debug);

	//------------------------------------------------------------------------------------------------------------------

	public Triple<Integer, String, Collection<String>> generate(final File pifFileName,
																final boolean generateLTS)
	{
		return this.generate(pifFileName, generateLTS, true, !isBalanced);
	}

	public Triple<Integer, String, Collection<String>> generate(final File pifFileName)
	{
		return this.generate(pifFileName, true, true, !isBalanced);
	}

	/**
	 * Computes the LTS model (BCG file) for a PIF model.
	 *
	 * @param pifFileName    is the name of the PIF file.
	 * @param smartReduction is true if a smart reduction is done on the LTS when loading it, false otherwise.
	 * @param debug          is true if debug information are displayed, false otherwise.
	 * @return (Integer, String, Collection < String >), return code, name of the model
	 * (can be different from the filename) and its alphabet.
	 */
	public abstract Triple<Integer, String, Collection<String>> generate(final File pifFileName,
																		 final boolean generateLTS,
																		 final boolean smartReduction,
																		 final boolean debug);

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Decides if the LTS of the PIF workflow has to be recomputed.
	 *
	 * @param pifFile     is the PIF file
	 * @param ltsFileName is the name of the LTS file
	 * @return true if the LTS must be rebuilt from the PIF file, false otherwise
	 */
	public boolean needsRebuild(final File pifFile,
								final String ltsFileName)
	{
		final File ltsFile = new File(ltsFileName);

		//If the LTS file does not exist -> rebuild
		if (!ltsFile.exists())
		{
			return true;
		}

		//If the timestamp of the LTS file is older than the timestamp of the PIF file -> rebuild
		return ltsFile.lastModified() < pifFile.lastModified();
	}
}
