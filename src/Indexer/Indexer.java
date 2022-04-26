package Indexer;

import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.File;
import mitos.stemmer.Stemmer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Indexer {

	public TreeMap<String, HashMap<String, HashMap<String,Integer>>> memoryStorage;
	public ArrayList<String> stopWords;

	public Indexer() {
		memoryStorage = new TreeMap<String, HashMap<String, HashMap<String,Integer>>>();
		stopWords = new ArrayList<String>();
		Stemmer.Initialize();
		loadStopWords();
	}

	public void makeTokens(String content, String tag,String filepath) {

		String delimiter = "\t\n\r\f \\ . / , ; \" \' ( ) [ ] & $ # * ** + - : ~ ";
		StringTokenizer tokenizer = new StringTokenizer(content, delimiter);
		boolean flag;
		while (tokenizer.hasMoreTokens()) {
			flag = false;
			String currentToken = tokenizer.nextToken();
			currentToken=Stemmer.Stem(currentToken);

			for (int i = 0; i < stopWords.size(); i++) {
				if (currentToken.equals(stopWords.get(i))) {
					flag = true;
					break;
				}
			}
			if (flag != true) {

				if (memoryStorage.get(currentToken) == null) {
					HashMap<String, HashMap<String,Integer>> temp = new HashMap<String, HashMap<String,Integer>>();
					HashMap<String,Integer> temp1 =new HashMap<String,Integer> ();
					temp1.put(tag,1);
					temp.put(filepath, temp1);
					memoryStorage.put(currentToken, temp);
				} else {
					HashMap<String,HashMap<String,Integer>> temp = memoryStorage.get(currentToken);
					if (temp.get(filepath) == null) {
						HashMap<String,Integer> temp1 =new HashMap<String,Integer> ();
						temp1.put(tag,1);
						memoryStorage.get(currentToken).put(tag, temp1);
					} else {
						if(temp.get(filepath).get(tag)==null) {
							memoryStorage.get(currentToken).get(filepath).put(tag, 1);
						}else {
							memoryStorage.get(currentToken).get(filepath).put(tag, (memoryStorage.get(currentToken).get(filepath).get(tag)) + 1);
						}
					}
				}
			}
		}

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
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				parseXML(fileEntry.getAbsolutePath());
			}
		}
	}

	public void createIndexer() {
	}

	public void parseXML(String filename) {
		if (filename.contains(".DS_Store")) {
			return;
		}
		File example = new File(filename);
		NXMLFileReader xmlFile = null;
		try {
			xmlFile = new NXMLFileReader(example);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String pmcid = xmlFile.getPMCID();
		makeTokens(pmcid, "pmcid",filename);
		String title = xmlFile.getTitle();
		makeTokens(title, "title",filename);
		String abstr = xmlFile.getAbstr();
		makeTokens(abstr, "abstr",filename);
		String body = xmlFile.getBody();
		makeTokens(body, "body",filename);
		String journal = xmlFile.getJournal();
		makeTokens(journal, "journal",filename);
		String publisher = xmlFile.getPublisher();
		makeTokens(publisher, "publisher",filename);
		ArrayList<String> authors = xmlFile.getAuthors();
		for (int i = 0; i < authors.size(); i++) {
			makeTokens(authors.get(i), "authors",filename);
		}
		HashSet<String> categories = xmlFile.getCategories();
		Iterator<String> it = categories.iterator();
		while (it.hasNext()) {
			makeTokens(it.next(), "categories",filename);
		}
	}

	public void writeCollectionIndex() throws FileNotFoundException, UnsupportedEncodingException {
		File theDir = new File("CollectionIndex");
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
		PrintWriter writer = new PrintWriter("CollectionIndex/VocabularyFile.txt", "UTF-8");
		memoryStorage.entrySet().forEach(entry -> {
			writer.println("\nWord: " + entry.getKey()+ " - "+entry.getValue().entrySet().size());
		});
		writer.close();
	}
	
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
		in.listFilesForFolder(new File("5_Resources_Corpus/MiniCollection/"));
		try {
			in.writeToFile();
			in.writeCollectionIndex();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
