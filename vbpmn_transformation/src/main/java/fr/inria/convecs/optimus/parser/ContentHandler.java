/**
 * 
 */
package fr.inria.convecs.optimus.parser;

/**
 * @author ajayk
 *
 */
public interface ContentHandler {
	
	/***
	 * 
	 */
	public void handle();
	
	/**
	 * @return *
	 * 
	 */
	public Object getOutput();
}
