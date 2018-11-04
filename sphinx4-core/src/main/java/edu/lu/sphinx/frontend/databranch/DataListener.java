package edu.lu.sphinx.frontend.databranch;

import edu.lu.sphinx.frontend.Data;


/** 
 * Defines some API-elements for Data-observer classes. 
 */
public interface DataListener {

    /** This method is invoked when a new {@link Data} object becomes available.
     * @param data feature frame
     */
    public void processDataFrame(Data data);

}
