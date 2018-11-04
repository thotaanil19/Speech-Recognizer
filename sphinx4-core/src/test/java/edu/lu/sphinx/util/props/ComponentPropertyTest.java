package edu.lu.sphinx.util.props;

import org.testng.Assert;
import org.testng.annotations.Test;

import edu.lu.sphinx.util.props.Configurable;
import edu.lu.sphinx.util.props.ConfigurationManager;
import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;
import edu.lu.sphinx.util.props.S4Component;

/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ComponentPropertyTest implements Configurable {

    @S4Component(type = DummyProcessor.class, defaultClass = AnotherDummyProcessor.class)
    public static final String PROP_DATA_PROC = "dataProc";
    private DummyProcessor dataProc;


    public void newProperties(PropertySheet ps) throws PropertyException {
        dataProc = (DummyProcessor) ps.getComponent(PROP_DATA_PROC);
    }


    public String getName() {
        return this.getClass().getName();
    }


    @Test
    public void testDefInstance() throws PropertyException, InstantiationException {
        ComponentPropertyTest cpt = ConfigurationManager.getInstance(ComponentPropertyTest.class);

        Assert.assertTrue(cpt != null);
        Assert.assertTrue(cpt.dataProc instanceof AnotherDummyProcessor);
    }
}
