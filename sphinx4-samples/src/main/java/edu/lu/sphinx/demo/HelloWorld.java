package edu.lu.sphinx.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import edu.lu.sphinx.api.Configuration;
import edu.lu.sphinx.api.LiveSpeechRecognizer;
import edu.lu.sphinx.api.SpeechResult;
import edu.lu.sphinx.api.StreamSpeechRecognizer;

public class HelloWorld {       

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		/*
		configuration.setAcousticModelPath("resource:/edu/lu/SpeechRecognition/models/en-us");
		configuration.setDictionaryPath("resource:/edu/lu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/lu/sphinx/models/en-us/en-us.lm.bin");
		 */
		configuration.setAcousticModelPath("resource:/edu/lu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/lu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/lu/sphinx/models/en-us/en-us.lm.bin");
		
		//LiveSpeechRecognizer liveSpeechRecognizer = new LiveSpeechRecognizer(configuration);

		audioFileSpeechRecognizer(configuration); 
		
		//liveSpeechRecognizer(configuration);

	}

	public static void liveSpeechRecognizer(Configuration configuration) throws Exception {
		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		configuration.setGrammarPath("resource:/grammars2");
		configuration.setGrammarName("grammar");
		configuration.setUseGrammar(true);
		// Start recognition process pruning previously cached data.
		recognizer.startRecognition(true);
		SpeechResult result = recognizer.getResult();
		// Pause recognition process. It can be resumed then with startRecognition(false).
		while ((result = recognizer.getResult()) != null) {
			System.out.println("Hypothesis: "+ result.getHypothesis());
		}
		recognizer.stopRecognition();
	}

	public static void audioFileSpeechRecognizer (Configuration configuration) throws Exception {

		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
		//InputStream stream = new FileInputStream(new File("D:\\Anil\\Grad Seminar\\project\\sphinx4-5prealpha-src\\sphinx4-5prealpha-src\\sphinx4-core\\src\\test\\resources\\edu\\cmu\\sphinx\\frontend\\test-feat.wav"));
		InputStream stream = new FileInputStream(new File("D:\\Anil\\Grad Seminar\\project\\sphinx4-5prealpha-src\\sphinx4-5prealpha-src\\sphinx4-samples\\src\\main\\resources\\edu\\cmu\\sphinx\\demo\\aligner\\10001-90210-01803.wav"));

		recognizer.startRecognition(stream);

		SpeechResult result;
		while ((result = recognizer.getResult()) != null) {
			System.out.println("Hypothesis: " + result.getHypothesis());
		}
		recognizer.stopRecognition();
	}


}