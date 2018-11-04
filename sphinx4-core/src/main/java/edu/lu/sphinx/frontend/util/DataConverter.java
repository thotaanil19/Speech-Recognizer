package edu.lu.sphinx.frontend.util;

import edu.lu.sphinx.frontend.*;
import edu.lu.sphinx.util.MatrixUtils;
import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;
import edu.lu.sphinx.util.props.S4String;

/**
 * A simple converter which converts <code>DoubleData</code> to <code>FloatData</code> and vv (depending on its
 * configuration). All remaining <code>Data</code>s will pass this processor unchanged.
 *
 * @author Holger Brandl
 */

public class DataConverter extends BaseDataProcessor {

    public static final String CONVERT_D2F = "d2f";
    public static final String CONVERT_F2D = "f2d";

    @S4String(defaultValue = "d2f", range = {CONVERT_D2F, CONVERT_F2D})
    public static final String PROP_CONVERSION_MODE = "conversionMode";
    private String convMode;

    public DataConverter(String convMode) throws PropertyException {
        initLogger();
        this.convMode = convMode;
    }

    public DataConverter() {

    }

    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.util.props.Configurable#newProperties(edu.lu.sphinx.util.props.PropertySheet)
    */
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);

        convMode = ps.getString(PROP_CONVERSION_MODE);
    }


    @Override
    public Data getData() throws DataProcessingException {
        Data d = getPredecessor().getData();

        if (d instanceof DoubleData && convMode.equals(CONVERT_D2F)) {
            DoubleData dd = (DoubleData) d;
            d = new FloatData(MatrixUtils.double2float(dd.getValues()), dd.getSampleRate(),
                    dd.getFirstSampleNumber());
        } else if (d instanceof FloatData && convMode.equals(CONVERT_F2D)) {
            FloatData fd = (FloatData) d;
            d = new DoubleData(MatrixUtils.float2double(fd.getValues()), fd.getSampleRate(),
                    fd.getFirstSampleNumber());
        }

        return d;
    }
}
