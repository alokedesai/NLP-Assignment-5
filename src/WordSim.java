import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;


public class WordSim {
	private Hashtable<String, Integer> frequency = new Hashtable<String, Integer>();
	private Hashtable<String, Integer> docFrequency = new Hashtable<String, Integer>();
	private HashSet<String> stopWords = new HashSet<String>();
	private Hashtable<String, Hashtable<String, Double>> occurrences = new Hashtable<String, Hashtable<String, Double>>();
	private Integer numDocuments = 0;
	private Integer wordCount = 0;

	public WordSim(String stopList, String sentences, String inputFile) {
		File file = new File(stopList);
		Scanner sc;

		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				stopWords.add(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File sentenceFile = new File(sentences);
		Scanner sentenceScanner;

		try {
			sentenceScanner = new Scanner(sentenceFile);
			
			while (sentenceScanner.hasNextLine()) {
				
				numDocuments += 1;
				String currentSentence = sentenceScanner.nextLine();
				String[] sentence = currentSentence.split(" ");
				ArrayList<String> prunedSentence = new ArrayList<String>();

				for (String word: sentence) {
					
					String currentWord = word.toLowerCase();
					if (!stopWords.contains(currentWord) && currentWord.matches("[a-z]+")) {
						wordCount += 1;
						prunedSentence.add(currentWord);
					}
				}
				HashSet<String> wordsSeen = new HashSet<String>();

				for (int i = 0; i < prunedSentence.size(); i++) {
					// lowercase word
					String currentWord = prunedSentence.get(i).toLowerCase();
					// update frequency
					if (frequency.containsKey(currentWord)) {
						frequency.put(currentWord, frequency.get(currentWord)+1);
					}  else {
						frequency.put(currentWord, 1);
					}
					
					
					//update document frequency
					if (!wordsSeen.contains(currentWord)) {
						
						if (docFrequency.containsKey(currentWord)) {
							docFrequency.put(currentWord, docFrequency.get(currentWord)+1);
						} else {
							docFrequency.put(currentWord, 1);
						}

						wordsSeen.add(currentWord);
					}

					// update co-occurrence stuff
					addToOccurrence(i, prunedSentence);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//print statistics
		System.out.println(frequency.size() + " unique words");
		System.out.println(wordCount + " word occurrences");
		System.out.println(numDocuments + " sentences/lines/documents");

		// calculate weights for each line in input file
//		File input = new File(inputFile);
//		Scanner inputScanner;
//
//		try {
//			inputScanner = new Scanner(input);
//			
//			while (inputScanner.hasNextLine()) {
//				String currentLine = inputScanner.nextLine();
//				String[] params = currentLine.split("\t");
//				System.out.println(params[2]);
//				System.out.println(calculateTopWeights(params[0], params[1], params[2]));
//			}
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void addToOccurrence(int i, ArrayList<String> sentences) {
		String word = sentences.get(i);
		// left
		if (i == 1) {
			
			addToHash(word, sentences.get(0));
		} else if (i > 1) {
			
			addToHash(word, sentences.get(i-1));
			addToHash(word, sentences.get(i-2));
		}
		
		if (i == sentences.size() - 2) {
			
			addToHash(word, sentences.get(i+1));
		} else if (i < sentences.size() - 2){
			
			addToHash(word,sentences.get(i+1));
			addToHash(word, sentences.get(i+2));
		}
	}
	private void addToHash(String word, String occurrence) {
		if (!occurrences.containsKey(word)) {
			occurrences.put(word, new Hashtable<String, Double>());
		}
		if (!occurrences.get(word).containsKey(occurrence)) {
			occurrences.get(word).put(occurrence, 1.0);
		} else {
			occurrences.get(word).put(occurrence, occurrences.get(word).get(occurrence) + 1.0);
		}
	}
	
	public Hashtable<String, Double> normalize(Hashtable<String, Double> h) {
		double norm = 0.0;
		for (String s : h.keySet()) {
			norm += Math.pow( h.get(s), 2);
		}
		norm = Math.sqrt(norm);
		
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		for (String s: h.keySet()) {
			output.put(s, (h.get(s)) / norm);
		}

		return output ;
		
	}
	
	public Hashtable<String, Double> tfWeight(Hashtable<String, Double> h) {
		return h;
	}
	
	public Hashtable<String, Double> tfIdfWeight(Hashtable<String, Double> h) {
		double n = (double) numDocuments;
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		for (String s : h.keySet()) {
			Double idf = Math.log10(n /docFrequency.get(s));
			output.put(s, h.get(s) * idf);
		}
		return output;
	}
	
	public Hashtable<String, Double> pmiWeight(Hashtable<String, Double> h, String word) {
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		double probWord = frequency.get(word) / (double) wordCount;
		for (String key : h.keySet()) {
			double numerator = occurrences.get(word).get(key) / (double) wordCount;
			double probKey = frequency.get(key) / (double) wordCount;
			double weight = Math.log10(numerator / (probKey * probWord));
			output.put(key, weight);
		}
		
		return output;
	}
	
	public Double l2Distance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;
		
		for (String word : vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += Math.pow((vector1.get(word) - vector2.get(word)), 2);
			} else {
				distance += Math.pow(vector1.get(word), 2);
			}
		}

		for (String word: vector2.keySet()) {
			if (!vector1.containsKey(word)) {
				distance += Math.pow(vector2.get(word),2);
			}
		}
		return Math.sqrt(distance);
	}
	
	public Double l1Distance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;
		
		for (String word : vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += Math.abs((vector1.get(word) - vector2.get(word)));
			} else {
				distance += vector1.get(word);
			}
		}

		for (String word: vector2.keySet()) {
			if (!vector1.containsKey(word)) {
				distance += vector2.get(word);
			}
		}
		return distance;
	}
	
	public Double cosineDistance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;

		for (String word: vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += vector1.get(word) * vector2.get(word);
			}
		}
		return distance;
	}
	
	public String calculateTopWeights(String word, String weighting, String simMeasure) {
		if (!occurrences.containsKey(word)) {
			return "ERROR";
		}
		Hashtable<String, Double> vector = occurrences.get(word);
		
		// weight vector
		if (weighting.equals("TF")) {
			vector = tfWeight(vector);
		} else if (weighting.equals("TFIDF")) {
			vector = tfIdfWeight(vector);
		} else if (weighting.equals("PMI")) {
			vector = pmiWeight(vector, word);
		} else {
			return "ERROR";
		}
		
		// normalize vector
		vector = normalize(vector);
		
		ArrayList<WordWeight> similarities = new ArrayList<WordWeight>();
		// compare this normalize vector to all other words
		for (String s : frequency.keySet()) {
			if (frequency.get(s) >= 3 && !s.equals(word)) {
				Hashtable<String, Double> currentVector = occurrences.get(s);

				// weight vector
				if (weighting.equals("TF")) {
					currentVector = tfWeight(currentVector);
				} else if (weighting.equals("TFIDF")) {
					currentVector = tfIdfWeight(currentVector);
				} else if (weighting.equals("PMI")) {
					// handle case
					currentVector = pmiWeight(currentVector, s);
				} else {
					return "ERROR";
				}
				
				// normalize vector
				currentVector = normalize(currentVector);
				
				Double distance = 0.0;
				// calculate distance 
				if (simMeasure.equals("L1")) {
					distance = l1Distance(currentVector, vector);
				} else if (simMeasure.equals("EUCLIDEAN")) {
					distance = l2Distance(currentVector, vector);
				} else if (simMeasure.equals("COSINE")) {
					distance = cosineDistance(currentVector, vector);
				} else {
					return "ERROR";
				}
				
				similarities.add(new WordWeight(s, distance));
			}
		}
		Collections.sort(similarities);

		StringBuffer output = new StringBuffer();
		if (simMeasure.equals("L1") || simMeasure.equals("EUCLIDEAN")) {
			for (int i = 0; i < 10; i++) {
				if (i >= similarities.size()) {
					break;
				}
				output.append(similarities.get(i) + " ");
			}
		} else {
			for (int i =  similarities.size() - 1; i >= similarities.size() - 10; i--) {
				if (i < 0) {
					break;
				}
				output.append(similarities.get(i) + " ");
			}
		}
		return output.toString();
	}
	public static void main(String[] args) {
		WordSim test = new WordSim("stoplist", "sentences","test");		
		System.out.println(test.calculateTopWeights("dog", "PMI", "COSINE"));
	}
}
