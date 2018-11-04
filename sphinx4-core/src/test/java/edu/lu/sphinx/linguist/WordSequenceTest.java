package edu.lu.sphinx.linguist;

import static edu.lu.sphinx.linguist.WordSequence.asWordSequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.URL;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import edu.lu.sphinx.linguist.acoustic.UnitManager;
import edu.lu.sphinx.linguist.dictionary.Dictionary;
import edu.lu.sphinx.linguist.dictionary.TextDictionary;
import edu.lu.sphinx.linguist.dictionary.Word;

public class WordSequenceTest {

    private Dictionary dictionary;

    @BeforeClass
    public void setUp() throws IOException {
        URL dictUrl =
                getClass()
                        .getResource(
                                "/edu/lu/sphinx/models/en-us/cmudict-en-us.dict");
        URL noiseDictUrl =
                getClass().getResource(
                        "/edu/lu/sphinx/models/en-us/en-us/noisedict");

        dictionary =
                new TextDictionary(dictUrl, noiseDictUrl, null, null,
                                   new UnitManager());
        dictionary.allocate();
    }

    @Test
    public void equals() {
        WordSequence ws = asWordSequence(dictionary, "one", "two", "three");
        assertThat(ws.size(), is(3));
        assertThat(asWordSequence(dictionary, "one", "two", "three"),
                equalTo(ws));
    }

    @Test
    public void comparison() {
        assertThat(asWordSequence(dictionary, "one"),
                lessThan(asWordSequence(dictionary, "two")));
        assertThat(asWordSequence(dictionary, "one"),
                lessThan(asWordSequence(dictionary, "one", "two")));
        assertThat(asWordSequence(dictionary, "one", "two"),
                lessThanOrEqualTo(asWordSequence(dictionary, "one", "two")));
        assertThat(asWordSequence(dictionary, "one", "two", "one"),
                greaterThan(asWordSequence(dictionary, "one", "two")));
        assertThat(asWordSequence(dictionary, "one", "two", "one"),
                greaterThan(asWordSequence(dictionary, "one", "one", "one")));
    }

    @Test
    public void getOldest() {
        WordSequence ws = asWordSequence(dictionary, "zero", "six", "one");
        assertThat(asWordSequence(dictionary, "zero", "six"),
                equalTo(ws.getOldest()));
        assertThat(ws.getOldest().getOldest(),
                equalTo(new WordSequence(ws.getWord(0))));
    }

    @Test
    public void getNewest() {
        WordSequence ws = asWordSequence(dictionary, "one", "two", "three");
        assertThat(asWordSequence(dictionary, "two", "three"),
                equalTo(ws.getNewest()));
        assertThat(ws.getNewest().getOldest(), equalTo(ws.getOldest()
                .getNewest()));
    }

    @Test
    public void unknownWords() {
        assertThat(new WordSequence(Word.UNKNOWN), equalTo(new WordSequence(
                Word.UNKNOWN)));
        assertThat(new WordSequence(Word.UNKNOWN, Word.UNKNOWN, Word.UNKNOWN),
                not(equalTo(new WordSequence(Word.UNKNOWN, Word.UNKNOWN))));
    }
}
