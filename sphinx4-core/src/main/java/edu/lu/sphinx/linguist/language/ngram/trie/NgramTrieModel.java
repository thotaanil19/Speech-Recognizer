package edu.lu.sphinx.linguist.language.ngram.trie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;

import edu.lu.sphinx.linguist.WordSequence;
import edu.lu.sphinx.linguist.dictionary.Dictionary;
import edu.lu.sphinx.linguist.dictionary.Word;
import edu.lu.sphinx.linguist.language.ngram.LanguageModel;
import edu.lu.sphinx.linguist.util.LRUCache;
import edu.lu.sphinx.util.LogMath;
import edu.lu.sphinx.util.TimerPool;
import edu.lu.sphinx.util.props.ConfigurationManagerUtils;
import edu.lu.sphinx.util.props.PropertyException;
import edu.lu.sphinx.util.props.PropertySheet;
import edu.lu.sphinx.util.props.S4Boolean;
import edu.lu.sphinx.util.props.S4Double;
import edu.lu.sphinx.util.props.S4Integer;
import edu.lu.sphinx.util.props.S4String;

/**
 * Language model that uses a binary NGram language model file ("binary trie file")
 * generated by the SphinxBase sphinx_lm_convert.
 */

public class NgramTrieModel implements LanguageModel {

    /**
     * The property for the name of the file that logs all the queried N-grams.
     * If this property is set to null, it means that the queried N-grams are
     * not logged.
     */
    @S4String(mandatory = false)
    public static final String PROP_QUERY_LOG_FILE = "queryLogFile";

    /** The property that defines that maximum number of ngrams to be cached */
    @S4Integer(defaultValue = 100000)
    public static final String PROP_NGRAM_CACHE_SIZE = "ngramCacheSize";

    /**
     * The property that controls whether the ngram caches are cleared after
     * every utterance
     */
    @S4Boolean(defaultValue = false)
    public static final String PROP_CLEAR_CACHES_AFTER_UTTERANCE = "clearCachesAfterUtterance";

    /** The property that defines the language weight for the search */
    @S4Double(defaultValue = 1.0f)
    public final static String PROP_LANGUAGE_WEIGHT = "languageWeight";

    /**
     * The property that controls whether or not the language model will apply
     * the language weight and word insertion probability
     */
    @S4Boolean(defaultValue = false)
    public final static String PROP_APPLY_LANGUAGE_WEIGHT_AND_WIP = "applyLanguageWeightAndWip";

    /** Word insertion probability property */
    @S4Double(defaultValue = 1.0f)
    public final static String PROP_WORD_INSERTION_PROBABILITY = "wordInsertionProbability";

    // ------------------------------
    // Configuration data
    // ------------------------------
    URL location;
    protected Logger logger;
    protected LogMath logMath;
    protected int maxDepth;
    protected int curDepth;
    protected int[] counts;

    protected int ngramCacheSize;
    protected boolean clearCacheAfterUtterance;

    protected Dictionary dictionary;
    protected String format;
    protected boolean applyLanguageWeightAndWip;
    protected float languageWeight;
    protected float unigramWeight;
    protected float logWip;

    // -------------------------------
    // Statistics
    // -------------------------------
    protected String ngramLogFile;
    private int ngramMisses;
    private int ngramHits;

    // -------------------------------
    // subcomponents
    // --------------------------------
    private PrintWriter logFile;

    //-----------------------------
    // Trie structure
    //-----------------------------
    protected TrieUnigram[] unigrams;
    protected String[] words;
    protected NgramTrieQuant quant;
    protected NgramTrie trie;

    //-----------------------------
    // Working data
    //-----------------------------
    protected Map<Word, Integer> unigramIDMap;
    private LRUCache<WordSequence, Float> ngramProbCache;
    
    public NgramTrieModel(String format, URL location, String ngramLogFile,
            int maxNGramCacheSize, boolean clearCacheAfterUtterance,
            int maxDepth, Dictionary dictionary,
            boolean applyLanguageWeightAndWip, float languageWeight,
            double wip, float unigramWeight) {
        logger = Logger.getLogger(getClass().getName());
        this.format = format;
        this.location = location;
        this.ngramLogFile = ngramLogFile;
        this.ngramCacheSize = maxNGramCacheSize;
        this.clearCacheAfterUtterance = clearCacheAfterUtterance;
        this.maxDepth = maxDepth;
        logMath = LogMath.getLogMath();
        this.dictionary = dictionary;
        this.applyLanguageWeightAndWip = applyLanguageWeightAndWip;
        this.languageWeight = languageWeight;
        this.logWip = logMath.linearToLog(wip);
        this.unigramWeight = unigramWeight;
    }

    public NgramTrieModel() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.lu.sphinx.util.props.Configurable#newProperties(edu.lu.sphinx.
     * util.props.PropertySheet)
     */
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        logMath = LogMath.getLogMath();
        location = ConfigurationManagerUtils.getResource(PROP_LOCATION, ps);
        ngramLogFile = ps.getString(PROP_QUERY_LOG_FILE);
        maxDepth = ps.getInt(LanguageModel.PROP_MAX_DEPTH);
        ngramCacheSize = ps.getInt(PROP_NGRAM_CACHE_SIZE);
        clearCacheAfterUtterance = ps
                .getBoolean(PROP_CLEAR_CACHES_AFTER_UTTERANCE);
        dictionary = (Dictionary) ps.getComponent(PROP_DICTIONARY);
        applyLanguageWeightAndWip = ps
                .getBoolean(PROP_APPLY_LANGUAGE_WEIGHT_AND_WIP);
        languageWeight = ps.getFloat(PROP_LANGUAGE_WEIGHT);
        logWip = logMath.linearToLog(ps.getDouble(PROP_WORD_INSERTION_PROBABILITY));
        unigramWeight = ps.getFloat(PROP_UNIGRAM_WEIGHT);
    }

    /**
     * Builds the map from unigram to unigramID. Also finds the startWordID and
     * endWordID.
     * 
     * @param dictionary
     * */
    private void buildUnigramIDMap() {
        int missingWords = 0;
        if (unigramIDMap == null)
            unigramIDMap = new HashMap<Word, Integer>();
        for (int i = 0; i < words.length; i++) {
            Word word = dictionary.getWord(words[i]);
            if (word == null) {
                logger.warn("The dictionary is missing a phonetic transcription for the word '"
                        + words[i] + "'");
                missingWords++;
            }

            unigramIDMap.put(word, i);

            if (org.apache.log4j.Level.DEBUG .equals(logger.getLevel()))
                logger.warn("Word: " + word);
        }

        if (missingWords > 0)
            logger.warn("Dictionary is missing " + missingWords
                    + " words that are contained in the language model.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.lu.sphinx.linguist.language.ngram.LanguageModel#allocate()
     */
    //@SuppressWarnings("unchecked")
    public void allocate() throws IOException {
        TimerPool.getTimer(this, "Load LM").start();

        logger.debug("Loading n-gram language model from: " + location);

        // create the log file if specified
        if (ngramLogFile != null)
            logFile = new PrintWriter(new FileOutputStream(ngramLogFile));
        BinaryLoader loader;
        if (location.getProtocol() == null
                || location.getProtocol().equals("file")) {
            try {
                loader = new BinaryLoader(new File(location.toURI()));
            } catch (Exception ex) {
                loader = new BinaryLoader(new File(location.getPath()));
            }
        } else {
            loader = new BinaryLoader(location);
        }
        loader.verifyHeader();
        counts = loader.readCounts();
        if (maxDepth <= 0 || maxDepth > counts.length)
            maxDepth = counts.length;
        if (maxDepth > 1) {
            quant = loader.readQuant(maxDepth);
        }
        unigrams = loader.readUnigrams(counts[0]);
        if (maxDepth > 1) {
            trie = new NgramTrie(counts, quant.getProbBoSize(), quant.getProbSize());
            loader.readTrieByteArr(trie.getMem());
        }
        //string words can be read here
        words = loader.readWords(counts[0]);
        buildUnigramIDMap();
        ngramProbCache = new LRUCache<WordSequence, Float>(ngramCacheSize);
        loader.close();
        TimerPool.getTimer(this, "Load LM").stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.lu.sphinx.linguist.language.ngram.LanguageModel#deallocate()
     */
    @Override
    public void deallocate() throws IOException {
        if (logFile != null) {
            logFile.flush();
        }
    }

    /**
     * Selects ngram of highest order available for specified word sequence
     * and extracts probability for it
     * @param wordSequence - word sequence to score
     * @param range - range to look bigram in
     * @param prob - probability of unigram
     * @return probability of of highest order ngram available
     */
    private float getAvailableProb(WordSequence wordSequence, TrieRange range, float prob) {
        if (!range.isSearchable()) return prob;
        for (int reverseOrderMinusTwo = wordSequence.size() - 2; reverseOrderMinusTwo >= 0; reverseOrderMinusTwo--) {
            int orderMinusTwo = wordSequence.size() - 2 - reverseOrderMinusTwo;
            if (orderMinusTwo + 1 == maxDepth) break;
            int wordId = unigramIDMap.get(wordSequence.getWord(reverseOrderMinusTwo));
            float updatedProb = trie.readNgramProb(wordId, orderMinusTwo, range, quant);
            if (!range.getFound()) break;
            prob = updatedProb;
            curDepth++;
            if (!range.isSearchable()) break;
        }
        return prob;
    }

    /**
     * Selects backoffs for part of word sequence 
     * unused in {@link #getAvailableProb(WordSequence, TrieRange, float) getAvailableProb}
     * Amount of unused words is specified by local variable curDepth
     * @param wordSequence - full word sequence that is scored
     * @return backoff
     */
    private float getAvailableBackoff(WordSequence wordSequence) {
        float backoff = 0.0f;
        int wordsNum = wordSequence.size();
        int wordId = unigramIDMap.get(wordSequence.getWord(wordsNum - 2));
        TrieRange range = new TrieRange(unigrams[wordId].next, unigrams[wordId + 1].next);
        if (curDepth == 1) {
            backoff += unigrams[wordId].backoff;
        }
        int sequenceIdx, orderMinusTwo;
        for (sequenceIdx = wordsNum - 3, orderMinusTwo = 0; sequenceIdx >= 0; sequenceIdx--, orderMinusTwo++) {
            int tmpWordId = unigramIDMap.get(wordSequence.getWord(sequenceIdx));
            float tmpBackoff = trie.readNgramBackoff(tmpWordId, orderMinusTwo, range, quant);
            if (!range.getFound()) break;
            backoff += tmpBackoff;
            if (!range.isSearchable()) break;
        }
        return backoff;
    }

    /**
     * extracts raw word sequence probability without using caching, 
     * making fresh LM trie traversing
     * @param wordSequence - sequence of words to get probability for
     * @return probability of specialized sequence of words
     */
    private float getProbabilityRaw(WordSequence wordSequence) {
        int wordsNum = wordSequence.size();
        int wordId = unigramIDMap.get(wordSequence.getWord(wordsNum - 1));
        TrieRange range = new TrieRange(unigrams[wordId].next, unigrams[wordId + 1].next);
        float prob = unigrams[wordId].prob;
        curDepth = 1;
        if (wordsNum == 1)
            return prob;
        //find prob of ngrams of higher order if any
        prob = getAvailableProb(wordSequence, range, prob);
        if (curDepth < wordsNum) {
            //use backoff for rest of ngram
            prob += getAvailableBackoff(wordSequence);
        }
        return prob;
    }

    /**
     * Applies weights to scores produced by language model
     * @param score - raw score
     * @return weighted score
     */
    private float applyWeights(float score) {
        //TODO ignores unigram weight. Apply or remove from properties
        if (applyLanguageWeightAndWip)
            return score * languageWeight + logWip;
        return score;
    }

    /**
     * Gets the ngram probability of the word sequence represented by the word
     * list
     * 
     * @param wordSequence - the word sequence
     * @return the probability of the word sequence. 
     *         Probability is in logMath log base
     */
    @Override
    public float getProbability(WordSequence wordSequence) {
        int numberWords = wordSequence.size();
        if (numberWords > maxDepth) {
            throw new Error("Unsupported NGram: " + wordSequence.size());
        }

        if (numberWords == maxDepth) {
            Float probability = ngramProbCache.get(wordSequence);

            if (probability != null) {
                ngramHits++;
                return probability;
            }
            ngramMisses++;
        }
        float probability = applyWeights(getProbabilityRaw(wordSequence));
        if (numberWords == maxDepth)
            ngramProbCache.put(wordSequence, probability);
        if (logFile != null)
            logFile.println(wordSequence.toString().replace("][", " ") + " : "
                    + Float.toString(probability));
        return probability;
    }

    /**
     * Gets the smear term for the given wordSequence
     * 
     * @param wordSequence - the word sequence
     * @return the smear term associated with this word sequence
     */
    @Override
    public float getSmear(WordSequence wordSequence) {
        //TODO not implemented
        return 0;
    }

    /**
     * Returns the set of words in the language model. The set is unmodifiable.
     * 
     * @return the unmodifiable set of words
     */
    @Override
    public Set<String> getVocabulary() {
        Set<String> vocabulary = new HashSet<String>(Arrays.asList(words));
        return Collections.unmodifiableSet(vocabulary);
    }

    /**
     * Returns the number of times when a NGram is queried, but there is no such
     * NGram in the LM (in which case it uses the backoff probabilities).
     * 
     * @return the number of NGram misses
     */
    public int getNGramMisses() {
        return ngramMisses;
    }

    /**
     * Returns the number of NGram hits.
     * 
     * @return the number of NGram hits
     */
    public int getNGramHits() {
        return ngramHits;
    }

    /**
     * Returns the maximum depth of the language model
     * 
     * @return the maximum depth of the language model
     */
    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    /** Clears the various N-gram caches. */
    private void clearCache() {
        logger.debug("LM Cache Size: " + ngramProbCache.size() + " Hits: "
                + ngramHits + " Misses: " + ngramMisses);
        if (clearCacheAfterUtterance) {
            ngramProbCache = new LRUCache<WordSequence, Float>(ngramCacheSize);
        }
    }

    /**
     *  Called by lexicon after recognition.
     *  Used to clear caches
     */
    public void onUtteranceEnd() {
        clearCache();

        if (logFile != null) {
            logFile.println("<END_UTT>");
            logFile.flush();
        }
    }    

    /**
     * Structure that keeps unigram instance data in trie.
     * Language model contains sorted array of TrieUnigram,
     * where index in array is wordId
     */
    public static class TrieUnigram {
        public float prob;
        public float backoff;
        public int next;
    }

    /**
     * Structure to keep ngram indexes range for trie traversal
     */
    public static class TrieRange {
        int begin;
        int end;
        boolean found;
        TrieRange(int begin, int end) {
            this.begin = begin;
            this.end = end;
            found = true;
        }

        int getWidth() {
            return end - begin;
        }

        void setFound(boolean found) {
            this.found = found;
        }

        boolean getFound() {
            return found;
        }
        
        boolean isSearchable() {
            return getWidth() > 0;
        }
    }

}
