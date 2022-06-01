package QueryTester;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import mitos.stemmer.Stemmer;

public class IRQualityEvaluator extends JFrame {

	public TreeMap<String, HashMap<Integer, Integer>> vocabularyMap;
	public TreeMap<Integer, HashMap<String, String>> topicsMap;
	public TreeMap<Integer, HashMap<String, Integer>> qrelsMap;
	public ArrayList<String> stopWords;
	public TreeMap<String, TreeMap<Double, Double>> documentScore;
	public TreeMap<String, TreeMap<String, Double>> vectorDocument;
	public static TreeMap<String, Double> queryScores;
	public int numberOfFiles;
	public JTextField queryInput;
	public JTextField typeInput;
	public JButton searchButton;
	public JButton automaticEval;
	public JPanel panel;
	public JTextArea log;

	public IRQualityEvaluator() {
		vocabularyMap = new TreeMap<String, HashMap<Integer, Integer>>();
		qrelsMap = new TreeMap<Integer, HashMap<String, Integer>>();
		topicsMap = new TreeMap<Integer, HashMap<String, String>>();
		stopWords = new ArrayList<String>();
		queryScores = new TreeMap<String, Double>();
		documentScore = new TreeMap<String, TreeMap<Double, Double>>();
		vectorDocument = new TreeMap<String, TreeMap<String, Double>>();

		loadGraphics();

		try {
			RandomAccessFile readerDocument = new RandomAccessFile("CollectionIndex/DocumentsFile.txt", "r");
			String line;
			while ((line = readerDocument.readLine()) != null) {
				numberOfFiles++;
			}
			readerDocument.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Stemmer.Initialize();
		loadStopWords();
		readTopics();
		readQRels();
	}

	public void loadGraphics() {
		this.setBounds(100, 100, 600, 400);
		this.setTitle("IRQualityEvaluator");
		this.setLayout(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		JLabel queryText = new JLabel("Query: ");
		queryText.setBounds(20, 20, 100, 30);
		this.add(queryText);

		JLabel typeText = new JLabel("Type: ");
		typeText.setBounds(255, 20, 100, 30);
		this.add(typeText);

		this.queryInput = new JTextField();
		this.queryInput.setBounds(70, 20, 170, 35);
		this.typeInput = new JTextField();
		this.typeInput.setBounds(300, 20, 120, 35);

		this.searchButton = new JButton("Search");
		this.searchButton.setBounds(450, 20, 100, 35);
		this.searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (queryInput.getText() != "" && typeInput.getText() != "") {

					String query = queryInput.getText();
					String type = typeInput.getText();

					resetQuery();
					queryResults(query, type, makeTokens(query, 0));

					String text = "";
					int i = 1;
					boolean flag = false;

					try {

						FileWriter fos = new FileWriter("collectionOutput/results.txt");
						PrintWriter dos = new PrintWriter(fos);

						for (Map.Entry<String, Double> entry : entriesSortedByValues(queryScores)) {
							text = text + i + ") " + entry.getKey() + "\n" + entry.getValue() + "\n\n";

							flag = false;

							for (Map.Entry<Integer, HashMap<String, String>> entry1 : topicsMap.entrySet()) {

								for (Map.Entry<String, String> temp : entry1.getValue().entrySet()) {
									String topicType = temp.getKey().toLowerCase();
									String topicDesc = temp.getValue();

									if (query.equals(topicDesc)) {
										dos.print(entry1.getKey() + "\t");
										flag = true;
										break;
									}
								}
								if (flag == true) {
									break;
								}
							}

							dos.print(0 + "\t");
							dos.print(entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1) + "\t");
							dos.print(i + "\t");
							dos.print(entry.getValue() + "\t");
							dos.print(null + "\t");
							dos.println();
							i++;
						}
						log.setText(text);
						dos.close();

						fos.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}

		});

		this.automaticEval = new JButton("Automatic Evaluation");
		this.automaticEval.setBounds(220, 320, 150, 35);
		this.automaticEval.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				double bpref = 0.0;
				int counter = 0;
				boolean flag = false;
				int topic_no = 0;
				int total_relevant = 0;

				String query = queryInput.getText();

				for (Map.Entry<Integer, HashMap<String, String>> entry1 : topicsMap.entrySet()) {

					for (Map.Entry<String, String> temp : entry1.getValue().entrySet()) {
						String topicType = temp.getKey().toLowerCase();
						String topicDesc = temp.getValue();

						if (query.equals(topicDesc)) {
							topic_no = entry1.getKey();
							flag = true;
							break;
						}
					}
					if (flag == true) {
						break;
					}
				}

				HashMap<String, Integer> temp = qrelsMap.get(topic_no);

				for (Map.Entry<String, Integer> entry1 : temp.entrySet()) {
					if (entry1.getValue() != 0) {
						total_relevant++;
					}
				}
				DecimalFormat df = new DecimalFormat("#.####");
				df.setRoundingMode(RoundingMode.CEILING);

				for (Map.Entry<String, Integer> entry1 : temp.entrySet()) {
					for (Map.Entry<String, Double> entry : queryScores.entrySet()) {
						// System.out.println(entry.getKey().substring((entry.getKey().lastIndexOf("/")
						// + 1),(entry.getKey().lastIndexOf("."))));
						String filename = entry.getKey().substring((entry.getKey().lastIndexOf("/") + 1),
								(entry.getKey().lastIndexOf(".")));
						if (entry1.getKey().equals(filename)) {
							if (entry1.getValue() != 0) {
								bpref = bpref + (1 - (counter / (double) total_relevant));
								counter++;
							}
						}
					}
				}
				bpref = bpref / total_relevant;

				try {

					FileWriter fos = new FileWriter("collectionOutput/eval_results.txt");
					PrintWriter dos = new PrintWriter(fos);
					dos.print(topic_no + "\t");
					dos.print(df.format(bpref) + "\t");
					dos.print(0.0 + "\t");
					dos.print(0.0 + "\t");
					dos.println();

					log.setText(log.getText() + "\n\nBpref: " + df.format(bpref));
					dos.close();

					fos.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		});

		this.panel = new JPanel();
		this.panel.setLayout(new BorderLayout());
		this.panel.setBounds(10, 100, 570, 200);

		this.log = new JTextArea("");
		this.log.setBounds(10, 100, 570, 200);
		this.log.setLineWrap(true);
		this.log.setWrapStyleWord(true);
		JScrollPane sp = new JScrollPane(log);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.panel.add(sp);

		this.add(panel);
		this.add(automaticEval);
		this.add(searchButton);
		this.add(queryInput);
		this.add(typeInput);

		this.setVisible(true);
	}

	public void resetQuery() {
		documentScore.clear();
		vectorDocument.clear();
		queryScores.clear();
	}

	public void loadStopWords() {

		try {
			File english = new File("3_Resources_Stoplists/stopwordsEn.txt");
			File greek = new File("3_Resources_Stoplists/stopwordsGr.txt");
			Scanner myReaderEn = new Scanner(english);
			Scanner myReaderGr = new Scanner(greek);
			while (myReaderEn.hasNextLine()) {
				String data = myReaderEn.nextLine();
				stopWords.add(data);
			}
			myReaderEn.close();
			while (myReaderGr.hasNextLine()) {
				String data = myReaderGr.nextLine();
				stopWords.add(data);
			}
			myReaderGr.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	public ArrayList<String> makeTokens(String content, int tokenIndex) {

		ArrayList<String> temp = new ArrayList<>();
		String delimiter = "\t\n\r\f \\ . / , ; \" \' ( ) [ ] & $ # * ** + - : ~ ?";
		StringTokenizer tokenizer = new StringTokenizer(content, delimiter);
		boolean flag;
		while (tokenizer.hasMoreTokens()) {
			flag = false;
			String currentToken = tokenizer.nextToken();
			currentToken = Stemmer.Stem(currentToken);

			for (int i = 0; i < stopWords.size(); i++) {
				if (currentToken.equals(stopWords.get(i))) {
					flag = true;
					break;
				}
			}
			if (flag != true) {
				System.out.println(currentToken);
			}
			temp.add(currentToken);
		}
		return temp;
	}

	public void readTopics() {
		ArrayList<Topic> topics = null;
		try {
			topics = TopicsReader.readTopics("5_Resources_Corpus/topics.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Topic topic : topics) {

			HashMap<String, String> temp = new HashMap<>();
			temp.put(topic.getType().toString(), topic.getDescription());
			topicsMap.put(topic.getNumber(), temp);

			/*
			 * System.out.println(topic.getNumber()); System.out.println(topic.getType());
			 * System.out.println(topic.getSummary());
			 * System.out.println(topic.getDescription()); System.out.println("---------");
			 */
		}
	}

	public void readQRels() {
		File qrels = new File("5_Resources_Corpus/qrels.txt");
		try (BufferedReader TSVReader = new BufferedReader(new FileReader(qrels))) {
			String line = null;
			while ((line = TSVReader.readLine()) != null) {
				String[] lineItems = line.split("\t"); // splitting the line and adding its items in String[]

				if (qrelsMap.get(Integer.parseInt(lineItems[0])) == null) {
					HashMap<String, Integer> temp = new HashMap<>();
					temp.put(lineItems[2], Integer.parseInt(lineItems[3]));
					qrelsMap.put(Integer.parseInt(lineItems[0]), temp);
				} else {
					qrelsMap.get(Integer.parseInt(lineItems[0])).put(lineItems[2], Integer.parseInt(lineItems[3]));
				}
			}
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}

	public void specificWord(ArrayList<String> query, String type) {

		for (int i = 0; i < query.size(); i++) {
			if (vocabularyMap.containsKey(query.get(i))) {
				for (Map.Entry<String, HashMap<Integer, Integer>> entry : vocabularyMap.entrySet()) {
					if (query.get(i).equals(entry.getKey())) {
						for (Map.Entry<Integer, Integer> temp : entry.getValue().entrySet()) {
							int pointer = temp.getValue();
							int tf = temp.getKey();

							try {
								RandomAccessFile readerPosting = new RandomAccessFile("CollectionIndex/PostingFile.txt",
										"r");
								readerPosting.seek(pointer);
								RandomAccessFile readerDocument = new RandomAccessFile(
										"CollectionIndex/DocumentsFile.txt", "r");

								int files = 0;

								while (files < tf) {

									String line = readerPosting.readLine();
									String tfd = line.substring(line.indexOf("[") + 1, line.indexOf("]") + 2);
									double wordTF = Double.parseDouble(line.substring(line.indexOf("-") + 2,
											line.indexOf("-", line.indexOf("-") + 1) - 1));
									String[] positions = tfd.split(",");
									ArrayList<String> sl = new ArrayList(Arrays.asList(positions));
									String documentNumber = line.substring(line.indexOf("documentPointer:") + 16);

									readerDocument.seek(Integer.parseInt(documentNumber));
									String documentLine = readerDocument.readLine();
									String document = documentLine.substring(documentLine.indexOf("-") + 2,
											documentLine.indexOf("-", documentLine.indexOf("-") + 1) - 1);

									if (document.contains(type)) {
										int index = documentLine.indexOf("-", documentLine.indexOf("-") + 1);
										Double df = Double.parseDouble(documentLine.substring(index + 2));

										if (vectorDocument.get(document) == null) {
											TreeMap<String, Double> entry1 = new TreeMap<>();
											entry1.put(query.get(i), wordTF);
											vectorDocument.put(document, entry1);

										} else {

											vectorDocument.get(document).put(query.get(i), wordTF);
										}

										if (documentScore.get(document) == null) {
											TreeMap<Double, Double> entry1 = new TreeMap<>();
											entry1.put(df, -1.0);
											documentScore.put(document, entry1);
										}
									}

									files++;
								}

								readerPosting.close();
								readerDocument.close();

							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
			}
		}
	}

	public void queryResults(String sentence, String type, ArrayList<String> query) {

		Map.Entry<String, String> tempEntry = null;

		for (Map.Entry<Integer, HashMap<String, String>> entry : topicsMap.entrySet()) {
			for (Map.Entry<String, String> temp : entry.getValue().entrySet()) {
				String topicType = temp.getKey().toLowerCase();
				String topicDesc = temp.getValue();
				if (topicDesc.equals(sentence) && topicType.equals(type)) {
					specificWord(query, type);
					tempEntry = temp;
				}

			}

		}
		if (tempEntry.getValue().equals(sentence)) {

			calculateScoreQuery(calculateQueryStats(query, sentence));
		}

	}

	public TreeMap<String, Double> calculateQueryStats(ArrayList<String> query, String sentence) {

		TreeMap<String, Integer> words = new TreeMap<>();
		TreeMap<String, Double> vectorQuery = new TreeMap<String, Double>();
		int max = 1;
		int counter = 1;
		double sum = 0.0;

		for (int i = 0; i < query.size(); i++) {
			for (int j = i + 1; j < query.size(); j++) {
				if (query.get(j).equals(query.get(i))) {
					counter++;
				}
			}

			if (max < counter) {
				max = counter;
			}
			words.put(query.get(i), counter);
			counter = 1;
		}

		for (Map.Entry<String, Integer> entry : words.entrySet()) {
			for (Map.Entry<String, HashMap<Integer, Integer>> word : vocabularyMap.entrySet()) {
				if (entry.getKey().equals(word.getKey())) {
					for (Map.Entry<Integer, Integer> wordDetails : word.getValue().entrySet()) {
						vectorQuery.put(entry.getKey(), (entry.getValue() / (double) max)
								* (Math.log(numberOfFiles / Math.log(2)) / (double) wordDetails.getKey()));
					}

				}
			}

		}
		return vectorQuery;
	}

	public double calculateDocumentTF(String Document, TreeMap<String, Double> wQ) {
		TreeMap<String, Double> temp = vectorDocument.get(Document);

		int max = -1;
		double sum = 0.0;

		if (temp != null) {

			for (Map.Entry<String, Double> entry : temp.entrySet()) {
				for (Map.Entry<String, HashMap<Integer, Integer>> word : vocabularyMap.entrySet()) {
					if (entry.getKey().equals(word.getKey())) {

						for (Map.Entry<Integer, Integer> wordDetails : word.getValue().entrySet()) {
							for (Map.Entry<String, Double> wordQ : wQ.entrySet()) {
								if (wordQ.getKey().equals(word.getKey())) {
									sum = sum + (wordQ.getValue() * (entry.getValue()
											* (Math.log(numberOfFiles / Math.log(2)) / (double) wordDetails.getKey())));
								}
							}

						}
					}

				}

			}
		}
		return sum;

	}

	public double calculateDocumentTFDouble(String Document, TreeMap<String, Double> wQ) {
		TreeMap<String, Double> temp = vectorDocument.get(Document);

		int max = -1;
		double sum = 0.0;
		double sum2 = 0.0;

		if (temp != null) {

			for (Map.Entry<String, Double> entry : temp.entrySet()) {
				for (Map.Entry<String, HashMap<Integer, Integer>> word : vocabularyMap.entrySet()) {
					if (entry.getKey().equals(word.getKey())) {

						for (Map.Entry<Integer, Integer> wordDetails : word.getValue().entrySet()) {
							for (Map.Entry<String, Double> wordQ : wQ.entrySet()) {
								if (wordQ.getKey().equals(word.getKey())) {

									sum = sum + Math.pow(entry.getValue()
											* (Math.log(numberOfFiles / Math.log(2)) / (double) wordDetails.getKey()),
											2);
									sum2 = sum2 + Math.pow(wordQ.getValue(), 2);

								}
							}

						}
					}

				}

			}
		}
		return Math.sqrt(sum * sum2);

	}

	public void calculateScoreQuery(TreeMap<String, Double> wQ) {

		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		for (Map.Entry<String, TreeMap<Double, Double>> entry : documentScore.entrySet()) {
			for (Map.Entry<Double, Double> temp : entry.getValue().entrySet()) {
				// System.out.println(entry.getKey() + " , " + temp.getKey() + " , " +
				// temp.getValue());
				double score = calculateDocumentTF(entry.getKey(), wQ)
						/ (calculateDocumentTFDouble(entry.getKey(), wQ));
				temp.setValue(score);
				// System.out.println(temp.getValue() +" "+ entry.getKey());
				queryScores.put(entry.getKey(), Double.parseDouble(df.format(temp.getValue())));
			}

		}
	}

	public void readVocabulary() {

		try {
			File vocabulary = new File("CollectionIndex/VocabularyFile.txt");
			Scanner myReader = new Scanner(vocabulary);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (data.indexOf("Word: ") != -1) {
					String word = data.substring(data.indexOf(": ") + 2, data.indexOf(" -"));
					String tf = data.substring(data.indexOf("-") + 2, data.indexOf("-", data.indexOf("-") + 1) - 1);
					// System.out.println(tf);
					int index = data.indexOf("-", data.indexOf("-") + 1);
					int pointer = Integer.parseInt(data.substring(index + 2));
					// System.out.println(pointer);
					HashMap<Integer, Integer> temp = new HashMap<>();
					temp.put(Integer.parseInt(tf), pointer);
					vocabularyMap.put(word, temp);
				}

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		IRQualityEvaluator qr = new IRQualityEvaluator();
		qr.readVocabulary();

		String s = in.nextLine();
		/*
		 * while (!s.equals("exit")) { // System.out.println(s); qr.queryResults(s,
		 * qr.makeTokens(s, 0)); s = in.nextLine(); qr.resetQuery(); }
		 */
	}

}
