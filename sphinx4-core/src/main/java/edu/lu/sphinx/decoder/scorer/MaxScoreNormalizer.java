package edu.lu.sphinx.decoder.scorer;

import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;

import java.util.List;

/**
 * Performs a simple normalization of all token-scores by
 *
 * @author Holger Brandl
 */
public class MaxScoreNormalizer implements ScoreNormalizer {


    public void newProperties(PropertySheet ps) throws PropertyException {
    }

    public MaxScoreNormalizer() {
    }


    public Scoreable normalize(List<? extends Scoreable> scoreableList, Scoreable bestToken) {
        for (Scoreable scoreable : scoreableList) {
            scoreable.normalizeScore(bestToken.getScore());
        }

        return bestToken;
    }
}
