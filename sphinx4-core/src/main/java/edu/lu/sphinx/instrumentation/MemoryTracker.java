/*
 * 
 * Copyright 1999-2004 Carnegie Mellon University.  
 * Portions Copyright 2004 Sun Microsystems, Inc.  
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package edu.lu.sphinx.instrumentation;

import edu.lu.sphinx.recognizer.Recognizer;
import edu.lu.sphinx.recognizer.Recognizer.State;
import edu.lu.sphinx.recognizer.StateListener;
import edu.lu.sphinx.result.Result;
import edu.lu.sphinx.decoder.ResultListener;
import edu.lu.sphinx.util.props.*;

import java.text.DecimalFormat;

/** Monitors a recognizer for memory usage */
public class MemoryTracker
        extends
        ConfigurableAdapter
        implements
        ResultListener,
        StateListener,
        Monitor {

    /** The property that defines which recognizer to monitor */
    @S4Component(type = Recognizer.class)
    public final static String PROP_RECOGNIZER = "recognizer";

    /** The property that defines whether summary accuracy information is displayed */
    @S4Boolean(defaultValue = true)
    public final static String PROP_SHOW_SUMMARY = "showSummary";

    /** The property that defines whether detailed accuracy information is displayed */
    @S4Boolean(defaultValue = true)
    public final static String PROP_SHOW_DETAILS = "showDetails";

    private static final DecimalFormat memFormat = new DecimalFormat("0.00 Mb");
    // ------------------------------
    // Configuration data
    // ------------------------------
    private String name;
    private Recognizer recognizer;
    private boolean showSummary;
    private boolean showDetails;
    private float maxMemoryUsed;
    private int numMemoryStats;
    private float avgMemoryUsed;

    public MemoryTracker(Recognizer recognizer, boolean showSummary, boolean showDetails) {
        initRecognizer(recognizer);
        initLogger();
        this.showSummary = showSummary;
        this.showDetails = showDetails;
    }

    public MemoryTracker() {
        
    }

    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.util.props.Configurable#newProperties(edu.lu.sphinx.util.props.PropertySheet)
    */
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        Recognizer newRecognizer = (Recognizer) ps.getComponent(
                PROP_RECOGNIZER);
        initRecognizer(newRecognizer);
        showSummary = ps.getBoolean(PROP_SHOW_SUMMARY);
        showDetails = ps.getBoolean(PROP_SHOW_DETAILS);
    }

    private void initRecognizer(Recognizer newRecognizer) {
        if (recognizer == null) {
            recognizer = newRecognizer;
            recognizer.addResultListener(this);
            recognizer.addStateListener(this);
        } else if (recognizer != newRecognizer) {
            recognizer.removeResultListener(this);
            recognizer.removeStateListener(this);
            recognizer = newRecognizer;
            recognizer.addResultListener(this);
            recognizer.addStateListener(this);
        }
    }


    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.util.props.Configurable#getName()
    */
    public String getName() {
        return name;
    }


    /** Shows memory usage
     * @param show*/
    private void calculateMemoryUsage(boolean show) {
        float totalMem = Runtime.getRuntime().totalMemory()
                / (1024.0f * 1024.0f);
        float freeMem = Runtime.getRuntime().freeMemory() / (1024.0f * 1024.0f);
        float usedMem = totalMem - freeMem;
        if (usedMem > maxMemoryUsed) {
            maxMemoryUsed = usedMem;
        }

        numMemoryStats++;
        avgMemoryUsed = ((avgMemoryUsed * (numMemoryStats - 1)) + usedMem)
                / numMemoryStats;

        if (show) {
            logger.debug("   Mem  Total: " + memFormat.format(totalMem)
                    + "  " + "Free: " + memFormat.format(freeMem));
            logger.debug("   Used: This: " + memFormat.format(usedMem) + "  "
                    + "Avg: " + memFormat.format(avgMemoryUsed) + "  " + "Max: "
                    + memFormat.format(maxMemoryUsed));
        }
    }


    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.decoder.ResultListener#newResult(edu.lu.sphinx.result.Result)
    */
    public void newResult(Result result) {
        if (result.isFinal()) {
            calculateMemoryUsage(showDetails);
        }
    }

    public void statusChanged(Recognizer.State status) {
        if (status == State.DEALLOCATED) {
            calculateMemoryUsage(showSummary);
        }
    }
}
