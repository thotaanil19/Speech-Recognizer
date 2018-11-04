package edu.lu.sphinx.frontend.databranch;

import edu.lu.sphinx.frontend.BaseDataProcessor;
import edu.lu.sphinx.frontend.Data;
import edu.lu.sphinx.frontend.DataProcessingException;
import edu.lu.sphinx.util.props.Configurable;
import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;
import edu.lu.sphinx.util.props.S4ComponentList;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates push-branches out of a Frontend. This might be used for for push-decoding or to create new pull-streams
 *
 * @see edu.lu.sphinx.decoder.FrameDecoder
 * @see edu.lu.sphinx.frontend.databranch.DataBufferProcessor
 */
public class FrontEndSplitter extends BaseDataProcessor implements DataProducer {


    @S4ComponentList(type = Configurable.class, beTolerant = true)
    public static final String PROP_DATA_LISTENERS = "dataListeners";
    private List<DataListener> listeners = new ArrayList<DataListener>();

    public FrontEndSplitter() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);

        listeners = ps.getComponentList(PROP_DATA_LISTENERS, DataListener.class);
    }


    /**
     * Reads and returns the next Data frame or return <code>null</code> if no data is available.
     *
     * @return the next Data or <code>null</code> if none is available
     * @throws edu.lu.sphinx.frontend.DataProcessingException
     *          if there is a data processing error
     */
    @Override
    public Data getData() throws DataProcessingException {
        Data input = getPredecessor().getData();

        for (DataListener l : listeners)
            l.processDataFrame(input);

        return input;
    }


    public void addDataListener(DataListener l) {
        if (l == null) {
            return;
        }
        listeners.add(l);
    }


    public void removeDataListener(DataListener l) {
        if (l == null) {
            return;
        }
        listeners.remove(l);
    }
}

