package Indexer;

import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Indexer {

    public HashMap<String, HashMap<String, Integer>> memoryStorage;
    public ArrayList<String> stopWords;

    public Indexer() {
        memoryStorage = new HashMap<String, HashMap<String, Integer>>();
        stopWords = new ArrayList<String>();
        loadStopWords();
        for(int i =0; i<stopWords.size(); i++) {
        	System.out.println(stopWords.get(i));
        }
    }

    public void makeTokens(String content, String tag) {
       
    	String delimiter = "\t\n\r\f \\ . / , ; # & [ ] ( ) !";
    	StringTokenizer tokenizer = new StringTokenizer(content, delimiter);
    	while(tokenizer.hasMoreTokens() ) {
    		String currentToken = tokenizer.nextToken(); 
    		//System.out.println(currentToken);
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

    public void createIndexer() {}

    public void parseXML(String filename) {
        File example = new File(filename);
        NXMLFileReader xmlFile = null;
        try {
            xmlFile = new NXMLFileReader(example);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pmcid = xmlFile.getPMCID();
        makeTokens(pmcid, "pmcid");
        String title = xmlFile.getTitle();
        makeTokens(title, "title");
        String abstr = xmlFile.getAbstr();
        makeTokens(abstr, "abstr");
        String body = xmlFile.getBody();
        makeTokens(body, "body");
        String journal = xmlFile.getJournal();
        makeTokens(journal, "journal");
        String publisher = xmlFile.getPublisher();
        makeTokens(publisher, "publisher");
        ArrayList<String> authors = xmlFile.getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            makeTokens(authors.get(i), "authors");
        }
        HashSet<String> categories = xmlFile.getCategories();
        Iterator<String> it = categories.iterator();
        while (it.hasNext()) {
            makeTokens(it.next(), "categories");
        }
    }

    public static void main(String[] args) {
        Indexer in = new Indexer();
        in.listFilesForFolder(new File("5_Resources_Corpus/MiniCollection/"));
    }


}



