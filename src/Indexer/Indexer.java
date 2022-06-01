package Indexer;

import gr.uoc.csd.hy463.NXMLFileReader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.File;
import mitos.stemmer.Stemmer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;

public class Indexer extends JFrame {

	public TreeMap<String, HashMap<String, HashMap<String, Integer>>> memoryStorage;
	public ArrayList<String> stopWords;
	public static int documentIndex;
	public TreeMap<String, Integer> documentsList;
	public TreeMap<String, TreeMap<String, ArrayList<Integer>>> positions;
	public static final DecimalFormat df = new DecimalFormat("0.000");
	public String documentsName, outputName;

	public JFileChooser jfc;
	public JMenuBar jb;
	public JTextField documentFolder, outputFolder;
	public JButton document, output, startIndexing;
	public JTextArea log;
	public JPanel panel;

	public Indexer() {
		memoryStorage = new TreeMap<String, HashMap<String, HashMap<String, Integer>>>();
		documentsList = new TreeMap<String, Integer>();
		positions = new TreeMap<String, TreeMap<String, ArrayList<Integer>>>();
		documentIndex = 0;
		stopWords = new ArrayList<String>();
		Stemmer.Initialize();
		loadStopWords();

		this.setTitle("Indexer");
		this.setLayout(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(200, 200, 530, 500);
		this.loadGraphics();
		this.setVisible(true);
	}

	public int makeTokens(String content, String tag, String filepath, int tokenIndex) {

		String delimiter = "\t\n\r\f \\ . / , ; \" \' ( ) [ ] & $ # * ** + - : ~ ";
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

				if (memoryStorage.get(currentToken) == null) {
					HashMap<String, HashMap<String, Integer>> temp = new HashMap<String, HashMap<String, Integer>>();
					HashMap<String, Integer> temp1 = new HashMap<String, Integer>();
					temp1.put(tag, 1);
					temp.put(filepath, temp1);
					memoryStorage.put(currentToken, temp);
				} else {
					HashMap<String, HashMap<String, Integer>> temp = memoryStorage.get(currentToken);
					if (temp.get(filepath) == null) {
						HashMap<String, Integer> temp1 = new HashMap<String, Integer>();
						temp1.put(tag, 1);
						memoryStorage.get(currentToken).put(filepath, temp1);
					} else {
						if (temp.get(filepath).get(tag) == null) {
							memoryStorage.get(currentToken).get(filepath).put(tag, 1);
						} else {
							memoryStorage.get(currentToken).get(filepath).put(tag,
									(memoryStorage.get(currentToken).get(filepath).get(tag)) + 1);
						}
					}
				}
				if (memoryStorage.get(currentToken) != null) {
					if (memoryStorage.get(currentToken).get(filepath) != null) {
						if (positions.get(currentToken) != null && positions.get(currentToken).get(filepath) != null) {
							positions.get(currentToken).get(filepath).add(tokenIndex);
						} else {
							if (positions.get(currentToken) == null) {

								TreeMap<String, ArrayList<Integer>> temp = new TreeMap<String, ArrayList<Integer>>();
								ArrayList<Integer> temp_array = new ArrayList<Integer>();
								temp_array.add(tokenIndex);
								temp.put(filepath, temp_array);
								positions.put(currentToken, temp);
							} else {
								ArrayList<Integer> temp_array = new ArrayList<Integer>();
								temp_array.add(tokenIndex);
								positions.get(currentToken).put(filepath, temp_array);
							}

						}

					}
				}

			}
			tokenIndex++;
		}
		return tokenIndex;
	}

	public void loadGraphics() {

		this.jb = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem j1 = new JMenuItem("file1");
		file.add(j1);
		JMenu help = new JMenu("Help");
		JMenuItem j2 = new JMenuItem("help1");
		help.add(j2);

		this.jb.add(file);
		this.jb.add(help);

		this.documentFolder = new JTextField();
		this.documentFolder.setBounds(10, 50, 180, 30);
		this.outputFolder = new JTextField();
		this.outputFolder.setBounds(10, 100, 180, 30);

		this.document = new JButton("Select");
		this.document.setBounds(200, 50, 80, 35);
		this.document.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				File workingDirectory = new File(System.getProperty("user.dir"));
				chooser.setCurrentDirectory(workingDirectory);
				chooser.setDialogTitle("Select Document folder");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int returnValue = chooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
					String directoryName = chooser.getSelectedFile().getAbsolutePath()
							.replace(new java.io.File("").getAbsolutePath(), "");
					documentFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					documentsName = directoryName.substring(directoryName.indexOf("/") + 1);
				} else {
					System.out.println("No Selection ");
				}

			}

		});
		this.output = new JButton("Select");
		this.output.setBounds(200, 100, 80, 35);
		this.output.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				File workingDirectory = new File(System.getProperty("user.dir"));
				chooser.setCurrentDirectory(workingDirectory);
				chooser.setDialogTitle("Select Document folder");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int returnValue = chooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
					String directoryName = chooser.getSelectedFile().getAbsolutePath()
							.replace(new java.io.File("").getAbsolutePath(), "");
					outputFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					outputName = directoryName.substring(directoryName.indexOf("/") + 1);
					log.setText("Initialising Stemmer\n\nCreating Stopwords list\n\n");
				} else {
					System.out.println("No Selection ");
				}

			}

		});

		JLabel documentInfo = new JLabel("Select the Document Folder");
		documentInfo.setBounds(300, 50, 200, 35);
		JLabel outputInfo = new JLabel("Select the Output Folder");
		outputInfo.setBounds(300, 100, 200, 35);

		this.startIndexing = new JButton("Start Indexing");
		this.startIndexing.setBounds(200, 160, 120, 35);
		this.startIndexing.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				log.setText("Starting Indexing...\n");
				long startTime = System.currentTimeMillis();

				listFilesForFolder(new File(documentsName));
				try {
					writeToFile();
					calculateNorm();
					writeCollectionIndex();

					registerPositions();
				} catch (FileNotFoundException | UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long estimatedTime = System.currentTimeMillis() - startTime;
				log.setText(
						log.getText() + "\n\nIndexing completed! Time elapsed: " + (estimatedTime / 1000) + " seconds");
			}

		});

		this.panel = new JPanel();
		this.panel.setLayout(new BorderLayout());
		this.panel.setBounds(40, 220, 450, 200);

		this.log = new JTextArea("");
		this.log.setBounds(40, 220, 450, 200);
		this.log.setLineWrap(true);
		this.log.setWrapStyleWord(true);
		JScrollPane sp = new JScrollPane(log);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.panel.add(sp);

		this.add(documentFolder);
		this.add(outputFolder);
		this.add(document);
		this.add(output);
		this.add(documentInfo);
		this.add(outputInfo);
		// this.add(log);
		this.add(startIndexing);
		this.add(panel);
		this.setJMenuBar(jb);
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

	public void listFilesForFolder(File folder) {
		this.log.setText(this.log.getText() + "\nDirectory: " + folder.getAbsolutePath());
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else if (!fileEntry.getAbsolutePath().contains(".DS_Store")) {
				// writeDocumentsFile(fileEntry.getAbsolutePath());
				documentsList.put(fileEntry.getAbsolutePath(), -1);
				parseXML(fileEntry.getAbsolutePath());
			}
		}
	}

	public void createIndexer() {
	}

	public void registerPositions() {

		/*
		 * positions.entrySet().forEach(entry -> { System.out.println("\nWord:" +
		 * entry.getKey()); entry.getValue().entrySet().forEach(document -> {
		 * System.out.println("\nDocument:" + document.getKey());
		 * System.out.println("\nPositions:"); for (int i = 0; i <
		 * document.getValue().size(); i++) {
		 * System.out.print(document.getValue().get(i) + ","); } }); });
		 * 
		 */
	}

	public void calculateNorm() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outputName + "/DocumentsFile.txt", "UTF-8");

		documentsList.entrySet().forEach(document -> {
			memoryStorage.entrySet().forEach(entry -> {
				if (entry.getValue().get(document.getKey()) != null) {
					int[] max = { 0 };
					entry.getValue().get(document.getKey()).entrySet().forEach(tag -> {
						max[0] = max[0] + tag.getValue();
					});
					if (documentsList.get(document.getKey()) < max[0]) {
						documentsList.replace(document.getKey(), max[0]);
					}

				}
			});

		});

		int index[] = { 0 };
		documentsList.entrySet().forEach(document -> {
			double[] norm = { 0 };
			memoryStorage.entrySet().forEach(entry -> {
				if (entry.getValue().get(document.getKey()) != null) {
					int[] tf_i = { 0 };
					double[] tf = { 0 };
					double[] idf = { 0 };
					entry.getValue().get(document.getKey()).entrySet().forEach(tag -> {
						tf_i[0] = tf_i[0] + tag.getValue();
					});
					tf[0] = (double) tf_i[0] / documentsList.get(document.getKey());
					idf[0] = (Math.log(documentsList.entrySet().size()) / Math.log(2)) / entry.getValue().size();
					norm[0] = norm[0] + Math.pow((tf[0] * idf[0]), 2);
				}
			});
			writer.println(index[0] + " - " + document.getKey() + " - " + df.format(Math.sqrt(norm[0])));
			index[0]++;
		});
		writer.close();

	}

	public void countPositions(int tokenIndex, String filename) {
		int token_in[] = { tokenIndex };
		memoryStorage.entrySet().forEach(entry -> {

			if (positions.get(entry.getKey()) != null && positions.get(entry.getKey()).get(filename) != null) {
				positions.get(entry.getKey()).get(filename).add(token_in[0]);
			} else {
				TreeMap<String, ArrayList<Integer>> temp = new TreeMap<String, ArrayList<Integer>>();
				ArrayList<Integer> temp_array = new ArrayList<Integer>();
				temp_array.add(token_in[0]);
				temp.put(filename, temp_array);
				positions.put(entry.getKey(), temp);
			}
		});
	}

	public void parseXML(String filename) {
		if (filename.contains(".DS_Store")) {
			return;
		}
		File example = new File(filename);
		int tokenIndex = 0;
		NXMLFileReader xmlFile = null;
		try {
			xmlFile = new NXMLFileReader(example);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String pmcid = xmlFile.getPMCID();
		tokenIndex = makeTokens(pmcid, "pmcid", filename, tokenIndex);
		String title = xmlFile.getTitle();
		tokenIndex = makeTokens(title, "title", filename, tokenIndex);
		String abstr = xmlFile.getAbstr();
		tokenIndex = makeTokens(abstr, "abstr", filename, tokenIndex);
		String body = xmlFile.getBody();
		tokenIndex = makeTokens(body, "body", filename, tokenIndex);
		String journal = xmlFile.getJournal();
		tokenIndex = makeTokens(journal, "journal", filename, tokenIndex);
		String publisher = xmlFile.getPublisher();
		tokenIndex = makeTokens(publisher, "publisher", filename, tokenIndex);
		ArrayList<String> authors = xmlFile.getAuthors();
		for (int i = 0; i < authors.size(); i++) {
			tokenIndex = makeTokens(authors.get(i), "authors", filename, tokenIndex);
			// countPositions(tokenIndex,filename);
		}
		HashSet<String> categories = xmlFile.getCategories();
		Iterator<String> it = categories.iterator();
		while (it.hasNext()) {
			tokenIndex = makeTokens(it.next(), "categories", filename, tokenIndex);
		}
	}

	/*
	 * Grafoume sto VocabularyFile
	 */

	public void writeCollectionIndex() throws FileNotFoundException, UnsupportedEncodingException {
		File theDir = new File("CollectionIndex");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		String[] fileArray = null;

		RandomAccessFile file = new RandomAccessFile(outputName + "/PostingFile.txt", "rw");
		RandomAccessFile documentFile = new RandomAccessFile(outputName + "/DocumentsFile.txt", "rw");
		try {
			file.seek(0);
			documentFile.seek(0);
			byte[] array = new byte[(int) documentFile.length()];
			documentFile.read(array);

			String fileString = new String(array);

			fileArray = fileString.split("\\r?\\n");

			for (int i = 0; i < fileArray.length; i++) {
				// System.out.println(fileArray[i]);
			}

			documentFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(
				documentsList.entrySet());

		PrintWriter writer = new PrintWriter(outputName + "/VocabularyFile.txt", "UTF-8");
		DecimalFormat df = new DecimalFormat("0.000");
		final String temp_array[] = fileArray;
		memoryStorage.entrySet().forEach(entry -> {

			try {
				writer.println("\nWord: " + entry.getKey() + " - " + entry.getValue().entrySet().size() + " - "
						+ file.getFilePointer());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			entry.getValue().entrySet().forEach(document -> {

				String line = "";
				int index[] = { 0 };
				for (int i = 0; i < entryList.size(); i++) {
					entryList.get(i).getKey();
					if (entryList.get(i).getKey().equals(document.getKey())) {
						index[0] = i;
						break;
					}

				}
				ArrayList temp = positions.get(entry.getKey()).get(document.getKey());
				double tf = temp.size() / (double) entryList.get(index[0]).getValue();
				try {
					file.writeBytes(index[0] + " - " + df.format(tf) + " - [");

					for (int i = 0; i < temp.size(); i++) {
						file.writeBytes(temp.get(i) + ",");
					}

					int bytes_counter[] = { 0 };
					for (int i = 0; i < temp_array.length; i++) {
						if (temp_array[i].substring(0, temp_array[i].indexOf(" -")).equals(String.valueOf(index[0]))) {
							// System.out.println(temp_array[i].substring(0,temp_array[i].indexOf(" -")));
							for (int j = 0; j < i; j++) {
								System.out.println(temp_array[j]+" "+bytes_counter[0]);
								bytes_counter[0] = bytes_counter[0] + temp_array[j].length()+1;
							}
						}
					}

					file.writeBytes("] - documentPointer:" + bytes_counter[0] + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});

		});
		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();
	}

	/*
	 * Grafoume sto index.txt
	 */
	public void writeToFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("index.txt", "UTF-8");
		memoryStorage.entrySet().forEach(entry -> {
			writer.println("\nWord: " + entry.getKey());
			entry.getValue().entrySet().forEach(file -> {
				writer.println("File:\n" + file.getKey());
				file.getValue().entrySet().forEach(tag -> {
					writer.println("Tag: " + tag.getKey() + " - " + tag.getValue());
				});
			});
		});
		writer.close();
	}

	public static void main(String[] args) {
		Indexer in = new Indexer();
	}

}
