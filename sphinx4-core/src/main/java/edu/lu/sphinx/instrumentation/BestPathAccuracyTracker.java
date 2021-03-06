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

import edu.lu.sphinx.decoder.search.Token;
import edu.lu.sphinx.result.Result;
import edu.lu.sphinx.util.props.*;
import edu.lu.sphinx.recognizer.Recognizer;

/** Tracks and reports recognition accuracy based upon the highest scoring path in a Result. */
public class BestPathAccuracyTracker extends AccuracyTracker {

    /** The property that define whether the full token path is displayed */
    @S4Boolean(defaultValue = false)
    public final static String PROP_SHOW_FULL_PATH = "showFullPath";

    private boolean showFullPath;
    
    public BestPathAccuracyTracker(Recognizer recognizer, boolean showSummary, boolean showDetails, boolean showResults, boolean showAlignedResults, boolean showRawResults, boolean showFullPath) {
        super(recognizer, showSummary, showDetails, showResults, showAlignedResults, showRawResults);
        this.showFullPath = showFullPath;
    }

    public BestPathAccuracyTracker() {

    }


    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.util.props.Configurable#newProperties(edu.lu.sphinx.util.props.PropertySheet)
    */
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        showFullPath = ps.getBoolean(PROP_SHOW_FULL_PATH);
    }


    /**
     * Dumps the best path
     *
     * @param result the result to dump
     */
    private void showFullPath(Result result) {
        if (showFullPath) {
            System.out.println();
            Token bestToken = result.getBestToken();
            if (bestToken != null) {
                bestToken.dumpTokenPath();
            } else {
                System.out.println("Null result");
            }
            System.out.println();
        }
    }


    /*
    * (non-Javadoc)
    *
    * @see edu.lu.sphinx.decoder.ResultListener#newResult(edu.lu.sphinx.result.Result)
    */
    @Override
    public void newResult(Result result) {
        String ref = result.getReferenceText();
        if (result.isFinal() && ref != null) {
            String hyp = result.getBestResultNoFiller();
            getAligner().align(ref, hyp);
            showFullPath(result);
            showDetails(result.toString());
        }
    }
}
