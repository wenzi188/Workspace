package com.wenzina;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/* tbd
A)
erl 1. File lesen und Text extrahieren
erl 2. Text tokenizen --> http://stackoverflow.com/questions/5836148/how-to-use-opennlp-with-java oder http://www.programcreek.com/2012/05/opennlp-tutorial/ 
erl 3. uebergeordnete Schleife fuer alle Dateien aus Verzeichnis
erl 4. RUN --> check ob ARFF Datei noch lesbar bzw. MemoryOverflow 

B)
erl 1.) task1_2: Referenz doc verarbeiten/tokenizen (siehe Klasse A1) und qryVector erzeugen
erl 2.) scoring ( ist fertig?)
erl 3.) Ausgabe im trec Format

C)
1. Eingabeoptionen programmieren (http://args4j.kohsuke.org/) oder http://jargs.sourceforge.net/
   -m -s -l -S -B -L# -U# -t
erl 2. Stemmer verwenden (in beiden Fällen)
erl 3. unterschiedliche Dateien erzeugen (small, medium, large)
erl 4. 60 Dateien erzeugen

D) 
1. jar-File mit Startskript
2. README
erl 3. Zugang zu github
4. Upload
*/

public class task11 {

	/**
	 * @param args
	 */
	static List<Document> docList = new ArrayList<Document>();
	static boolean optSmall = false;
	static boolean optMedium = false;
	static boolean optLarge = false;
	static boolean optSubject = true;
	static boolean optBody = true;
	static int optUpperBound = -1;
	static int optLowerBound = 0;
	static boolean optStem = true;
	static String path = "C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset";	
	
	public static void main(String[] args) {

		
		//optLowerBound = 2;
		optLarge = true;
		
		// preparation
		// SourceDoc sd = new SourceDoc("C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset/alt.atheism/51120");
		// String[] tok = sd.getTokens(false, true, true);

		int cnt = 0;
		//File folder = new File("C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset");
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isDirectory()) {
				for (File file2 : file.listFiles()) {
				    if (file2.isFile()) {
				        System.out.println(file2.getAbsolutePath());
				        cnt++;
				    }
				}
		    }
		}
		System.out.println("Anzahl: "+cnt);
		    
		// ---------------------------
		// Hashtable<String, LinkedList<Posting>> terms = createInvertedIndex();
		
		Hashtable<String, LinkedList<Posting>> terms = createInvertedIndexFromFiles(path);
																					 
		
		// writeStatistic(terms);
		// System.exit(0);
		
		// test output
		List<String> liste = new ArrayList<String>(terms.keySet());
		Collections.sort(liste); 
		for(String key: liste) {
			System.out.print("["+key+"]");
			LinkedList<Posting> poli = terms.get(key);
			for(Posting p: poli) {
				//System.out.print(" Doc: "+ p.getDocId()+"<"+p.getTF()+">");
				System.out.print(" Doc: "+ p.getDocId()+"<"+p.getTF()+">");
			}
			System.out.println();
		}
		
		createARFF(terms);
	}

	
	static Hashtable<String, LinkedList<Posting>> createInvertedIndexFromFiles(String startPath) {
		
		Hashtable<String, LinkedList<Posting>> terms = new  Hashtable<String, LinkedList<Posting>>();
		
		int docIndex = 0;
// --> loop over folders		for(String s: docs){

		File folder = new File(startPath);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isDirectory()) {
				for (File file2 : file.listFiles()) {
				    if (file2.isFile()) {
				        System.out.println(file2.getAbsolutePath());

		
			//SourceDoc sd = new SourceDoc("C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset/alt.atheism/51120");
		    SourceDoc sd = new SourceDoc(file2.getAbsolutePath());
			String[] words = sd.getTokens(false, true, true);
			docList.add(new Document(sd.getName(), sd.getClasses()));
			for(String word: words) {
				if(word.length() == 0)
					continue;
				// test fuer alle a woerter
				//if(word.indexOf("a")!=0)
				//	continue;
				boolean inserted = false;
				if(!terms.containsKey(word)) {
					terms.put(word, new LinkedList<Posting>());
				}
				LinkedList<Posting> poli = terms.get(word);
				if(poli.size() > 0) {
					if(poli.get(poli.size()-1).getDocId() == docIndex) {
						poli.get(poli.size()-1).incrementTF();
						inserted = true;
					}
				}
				if(!inserted)
					poli.add(new Posting(docIndex));
			}
			docIndex++;
			System.out.println("Terms: "+ terms.size());
				    }
					}
			    }
			}
		return terms;
	}
	

	static void writeStatistic(Hashtable<String, LinkedList<Posting>> terms) {
		File file = new File("dfStatistic.txt");
		String nl = System.getProperty("line.separator");

		List<String> liste = new ArrayList<String>(terms.keySet());
		Collections.sort(liste); 
		
		try {
			FileWriter writer = new FileWriter(file ,true);
			for(String key: liste) {
				LinkedList<Posting> poli = terms.get(key);
				writer.write(key + ";"+poli.size()+"\n");
			}
			writer.flush();
	        writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	static Hashtable<String, LinkedList<Posting>> createInvertedIndex() {
		String[] docs = new String[3];
		docs[0] = "The best insurance for the car";
		docs[1] = "Auto Insurance at its best";
		docs[2] = "The best car I have ever seen";
		
		Hashtable<String, LinkedList<Posting>> terms = new  Hashtable<String, LinkedList<Posting>>();
		
		int docIndex = 0;
		for(String s: docs){
			s = s.toLowerCase();
			String[] words = s.split(" ");
			for(String word: words) {
				boolean inserted = false;
				if(!terms.containsKey(word)) {
					terms.put(word, new LinkedList<Posting>());
				}
				LinkedList<Posting> poli = terms.get(word);
				if(poli.size() > 0) {
					if(poli.get(poli.size()-1).getDocId() == docIndex) {
						poli.get(poli.size()-1).incrementTF();
						inserted = true;
					}
				}
				if(!inserted)
					poli.add(new Posting(docIndex));
			}
			docIndex++;
		}
		return terms;
	}
	
	static void createARFF(Hashtable<String, LinkedList<Posting>> terms) {
		String prefix = "";
		if(optSmall) prefix = "small";
		if(optMedium) prefix = "medium";
		if(optLarge) prefix = "large";
		if(optUpperBound == -1)
			optUpperBound = terms.size();

		List<String> lis = new ArrayList<String>(terms.keySet());
		for(String key: lis) {
			LinkedList<Posting> poli = terms.get(key);
			if(poli.size() < optLowerBound || poli.size() > optUpperBound)
				terms.remove(key);
		}
		
		
		File file = new File(prefix+"_search.arff");
		String nl = System.getProperty("line.separator");
		List<String> liste = new ArrayList<String>(terms.keySet());
		Collections.sort(liste); 
		String headerStr = createARFF_Header(docList.size(), terms.size()+2, prefix);
		String attributeStr = createARFF_Attributes(terms, liste);
		StringBuffer sb;

		try {
			FileWriter writer = new FileWriter(file ,false);
			writer.write(headerStr);
			writer.write(attributeStr);
			writer.write("@data\n");
			for(int i = 0; i < docList.size(); i++) {
				sb = new StringBuffer();
				for(String key: liste) {
					LinkedList<Posting> poli = terms.get(key);
					if(sb.length() > 0) sb.append(",");
					if(poli.size() > 0) {
						if(poli.get(0).getDocId() == i) {
							sb.append(poli.get(0).getTF());
							poli.remove(0);
						}
						else
							sb.append("0");
					} else 
						sb.append("0");
				}
				writer.write(sb.toString()+","+docList.get(i).getName()+","+docList.get(i).getClasses()+nl);
			}
			writer.flush();
	        writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String createARFF_Header(int numberOfInstances, int numberOfAttributes, String prefix) {
		String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("% 1. Title: VU 188.412 - IR: Exercise 1"+nl);
		sb.append("%"+nl);
		sb.append("% 2. Sources:"+nl+"%      20_newsgroups_subset"+nl);
		sb.append("%"+nl);
		sb.append("% 3. Past Usage:"+nl+"%      Task 1.2"+nl);
		sb.append("%"+nl);
		sb.append("% 4. Relevant Information:"+nl+"%      Please ask Wenzina Reinhardt (8225629) for further information"+nl);
		sb.append("%          file created with optLowerBound: "+optLowerBound + " and optUpperBound: "+optUpperBound+nl);
		sb.append("%          files prefix: "+prefix+nl);
		sb.append("%"+nl);
		sb.append("% 5. Number of Instances: "+numberOfInstances+nl);
		sb.append("%"+nl);
		sb.append("% 6. Number of Attributes: "+numberOfAttributes+" (including the class and name attribute)"+nl);
		sb.append("%"+nl);
		sb.append("% 7. Attribute Information:"+nl+"%      (multiple)term frequencies + doc + class"+nl);
		sb.append("%"+nl);
		sb.append("% 8. Missing Attributes: none"+nl);
		sb.append("%"+nl);
		return sb.toString();
	}
	static String createARFF_Attributes(Hashtable<String, LinkedList<Posting>> terms, List<String> liste) {
		String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("@relation searchIndex"+nl);
		for(String s: liste) {
			sb.append("@attribute "+s+" integer"+nl); 
		}
		sb.append("@attribute _Doc String"+nl);  // setze alle document namen
		sb.append("@attribute _Class String"+nl);  // setze alle document Klassen
		return sb.toString();
	}

}
