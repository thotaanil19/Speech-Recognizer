package edu.lu.sphinx.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import edu.lu.sphinx.api.Configuration;
import edu.lu.sphinx.api.LiveSpeechRecognizer;
import edu.lu.sphinx.api.SpeechResult;
import edu.lu.sphinx.api.StreamSpeechRecognizer;

public class AudioFileToSpeechConversion {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setAcousticModelPath("resource:/edu/lu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/lu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/lu/sphinx/models/en-us/en-us.lm.bin");

		audioFileSpeechRecognizer(configuration);

	}

	public static void audioFileSpeechRecognizer(Configuration configuration)
			throws Exception {

		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
				configuration);
		InputStream stream = new FileInputStream(
				new File(
						"D:\\Anil\\Grad Seminar\\project\\sphinx4-5prealpha-src\\sphinx4-5prealpha-src\\sphinx4-samples\\src\\main\\resources\\edu\\cmu\\sphinx\\demo\\aligner\\10001-90210-01803.wav"));

		recognizer.startRecognition(stream);

		SpeechResult result;
		while ((result = recognizer.getResult()) != null) {
			System.out.println("Hypothesis: " + result.getHypothesis());
		}
		recognizer.stopRecognition();
	}

}