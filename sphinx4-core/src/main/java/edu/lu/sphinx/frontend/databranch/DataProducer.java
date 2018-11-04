package edu.lu.sphinx.frontend.databranch;

import edu.lu.sphinx.util.props.Configurable;

/** Some API-elements which are shared by components which can generate {@link edu.lu.sphinx.frontend.Data}s. */
public interface DataProducer extends Configurable {

    /** Registers a new listener for <code>Data</code>s.
     * @param l listener to add
     */
    void addDataListener(DataListener l);


    /** Unregisters a listener for <code>Data</code>s.
     * @param l listener to remove
     */
    void removeDataListener(DataListener l);
}
