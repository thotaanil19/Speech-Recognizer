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

package edu.lu.sphinx.trainer;

import edu.lu.sphinx.linguist.acoustic.AcousticModel;
import edu.lu.sphinx.linguist.acoustic.UnitManager;

/** Defines the Transcript Graph */
public class TranscriptHMMGraph extends Graph implements TranscriptGraph {

    public TranscriptHMMGraph(String context, Transcript transcript,
                              AcousticModel acousticModel, UnitManager unitManager) {
        super();
        BuildTranscriptHMM builder =
                new BuildTranscriptHMM(context, transcript, acousticModel, unitManager);
        copyGraph(builder.getGraph());
    }

}
