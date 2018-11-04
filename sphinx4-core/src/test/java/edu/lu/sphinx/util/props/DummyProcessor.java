package edu.lu.sphinx.util.props;

import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;

/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class DummyProcessor implements DummyFrontEndProcessor {


    public void newProperties(PropertySheet ps) throws PropertyException {
    }


    public String getName() {
        return this.getClass().getName();
    }
}
