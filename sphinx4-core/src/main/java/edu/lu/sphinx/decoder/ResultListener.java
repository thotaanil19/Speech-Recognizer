/*
* 
* Portions Copyright 2002 Sun Microsystems, Inc.
* Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
* All Rights Reserved.  Use is subject to license terms.
*
* See the file "license.terms" for information on usage and
* redistribution of this file, and for a DISCLAIMER OF ALL
* WARRANTIES.
*
*/

package edu.lu.sphinx.decoder;

import edu.lu.sphinx.util.props.Configurable;
import edu.lu.sphinx.result.Result;

import java.util.EventListener;

/** The listener interface for being informed when new results are generated. */
public interface ResultListener extends EventListener, Configurable {

    /**
     * Method called when a new result is generated
     *
     * @param result the new result
     */
    public void newResult(Result result);
}

