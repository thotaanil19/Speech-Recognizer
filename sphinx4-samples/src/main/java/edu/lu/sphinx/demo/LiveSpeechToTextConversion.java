package edu.lu.sphinx.demo;

import edu.lu.sphinx.api.Configuration;
import edu.lu.sphinx.api.LiveSpeechRecognizer;
import edu.lu.sphinx.api.SpeechResult;

public class LiveSpeechToTextConversion {       

	public static void main(String[] args) throws Exception {
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/lu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/lu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/lu/sphinx/models/en-us/en-us.lm.bin");
		liveSpeechRecognizer(configuration);
	}

	public static void liveSpeechRecognizer(Configuration configuration) throws Exception {
		configuration.setGrammarPath("resource:/grammar");
		configuration.setGrammarName("Test");
		configuration.setUseGrammar(true);
		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		// Start recognition process pruning previously cached data.
		recognizer.startRecognition(true);
		SpeechResult result = recognizer.getResult();
		// Pause recognition process. It can be resumed then with startRecognition(false).
		while ((result = recognizer.getResult()) != null) {
			System.out.println("Hypothesis: "+ result.getHypothesis());
		}
		recognizer.stopRecognition();
	}


}