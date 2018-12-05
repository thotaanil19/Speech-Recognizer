package edu.lu.sphinx.french;

import edu.lu.sphinx.api.Configuration;
import edu.lu.sphinx.api.LiveSpeechRecognizer;
import edu.lu.sphinx.api.SpeechResult;

public class LiveSpeechToTextConversion {       

	public static void main(String[] args) throws Exception {
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/lu/sphinx/models/fr-fr/fr-fr");
		configuration.setDictionaryPath("resource:/edu/lu/sphinx/models/fr-fr/cmudict-fr-fr.dict");
		configuration.setLanguageModelPath("resource:/edu/lu/sphinx/models/fr-fr/fr-fr.lm.bin");
		liveSpeechRecognizer(configuration);
	}

	public static void liveSpeechRecognizer(Configuration configuration) throws Exception {
		configuration.setGrammarPath("resource:/grammar/fr-fr");
		configuration.setGrammarName("main");
		configuration.setUseGrammar(true);
		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		// Start recognition process pruning previously cached data.
		recognizer.startRecognition(true);
		SpeechResult result = recognizer.getResult();
		// Pause recognition process. It can be resumed then with startRecognition(false).
		while ((result = recognizer.getResult()) != null) {
			System.out.println(result.getHypothesis());
		}
		recognizer.stopRecognition();
	}


}