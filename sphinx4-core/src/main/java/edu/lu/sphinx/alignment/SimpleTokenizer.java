/*
 * Copyright 2014 Alpha Cephei Inc.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package edu.lu.sphinx.alignment;

import java.util.Arrays;
import java.util.List;

public class SimpleTokenizer implements TextTokenizer {
    public List<String> expand(String text) {

        text = text.replace("â€™", "\"");
        text = text.replace("â€˜", " "); 
        text = text.replace("â€�", " ");
        text = text.replace("â€œ", " ");
        text = text.replace("\"", " ");
        text = text.replace("Â»", " ");
        text = text.replace("Â«", " ");
        text = text.replace("â€“", "-");
        text = text.replace("â€”", " ");
        text = text.replace("â€¦", " ");

        text = text.replace(" - ", " ");
        text = text.replaceAll("[/_*%]", " ");
        text = text.toLowerCase();

        String[] tokens = text.split("[.,?:!;()]");
        return Arrays.asList(tokens);
    }
}
