package com.wenzina;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class BM25Ranker {

	String prefix = "";
	int optSize;
	boolean optVerbose;
	LinkedList<TermDf> terms = new LinkedList<TermDf>();
	Hashtable<String, Integer> termHash = new Hashtable<String, Integer>();
	LinkedList<LinkedList<Item>> mat = new LinkedList<LinkedList<Item>>();
	List<Document> docs = new ArrayList<Document>();
	int Ndocuments;
	double Lave;
	

	public BM25Ranker(int oSize, boolean oVerbose) {
		optSize = oSize;
		optVerbose = oVerbose;
		if(oSize == 0) prefix = "small";
		if(oSize == 1) prefix = "medium";
		if(oSize == 2) prefix = "large";
		readARFF_File(prefix+"_search.arff");
		Ndocuments = mat.size();
		Lave = getLave();
		if(optVerbose) System.out.println("Average document length: "+ Lave + " of N documents: "+Ndocuments);
	}
	
	// get average document length
	double getLave() {
		long sum = 0;
		for(LinkedList<Item> ll: mat)
			for(Item it: ll) 
				sum += it.getItemCnt();
		return sum / Ndocuments;
	}
	
	int getDocumentLength(LinkedList<Item> ll){
		int sum = 0;
		for(Item i: ll)
			sum+= i.getItemCnt();
		return sum;
	}
	
	
	void rankDocument(String path, boolean optStemmer, boolean optHeadline, boolean optBody, double _k1, double _k3, double _b) {
		SourceDoc sd = new SourceDoc(path, optStemmer);
		LinkedList<Item> qryVector = createQryVector(sd.getTokens(optHeadline, optBody));
		double[] qryArray = mapLinkedList2Array(qryVector);
		// scoring
		for(int row = 0; row < mat.size(); row++) {
			double[] docArray = mapLinkedList2Array(mat.get(row));
			double RSV = 0;
			for(int col = 0; col < terms.size(); col++) {
				// only if term appears in both vectors
				if(qryArray[col] != 0 && docArray[col] != 0) {
					double f1 = Math.log10(Ndocuments/(double)terms.get(col).getDf());
					double f21 = ((_k1+1)*docArray[col]);
					double f22 = (double)((_k1*((1-_b)+_b*(getDocumentLength(mat.get(row))/Lave))+docArray[col]));
					double f3 = ((_k3+1)* qryArray[col])/(double)(_k3 + qryArray[col]);
					RSV += f1 * (f21/(double)f22) * f3; 
				}
			}
			docs.get(row).setScore(RSV);
		}
		
	}
	
	void writeTopTenList(int k, String group) {
		String nl = System.getProperty("line.separator");
		DecimalFormat f = new DecimalFormat("#0.00000");

		List<Document> docs2Sort = new ArrayList<Document>();
		for(Document d: docs) {
			docs2Sort.add(d);
		}
		Collections.sort(docs2Sort, new SortDocs());
		if(optVerbose) {
			for(int i = 0; i < 10; i++){
				System.out.println(docs2Sort.get(i));
			}
		}
		
		try {
			File file = new File(prefix+"_topic"+(k+1)+"_group"+group+".txt");
			FileWriter writer = new FileWriter(file, false);
			for(int i = 0; i < 10; i++){
				writer.write("topic"+(k+1)+" Q0 "+docs2Sort.get(i).getClasses()+"/"+docs2Sort.get(i).getName() +" "+(i+1)+" "+f.format(docs2Sort.get(i).getScore())+ " group"+group+"_"+prefix);
				if(i < 9)
					writer.write(nl);
				if(optVerbose) 
					System.out.println(docs2Sort.get(i));
			}
			writer.flush();
	        writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	double[] mapLinkedList2Array(LinkedList<Item> list) {
		double[] d = new double[terms.size()];
		for(int i = 0; i< terms.size(); i++)
			d[i] = 0;
		for(Item it: list) {
			d[it.getItemCol()] = it.getItemCnt();
		}
		return d;
	}
	
	
	LinkedList<Item> createQryVector(String[] words) {
		LinkedList<Item> q = new LinkedList<Item>();
		for(String word: words) {
			if(termHash.containsKey(word)) {
				int col = termHash.get(word);
					boolean found = false;
					for(Item it: q) {
						if(it.getItemCol() == col) {
							it.incrementItemCnt();
							found = true;
							break;
						}
					}
					if(!found)
						q.add(new Item(col, 1));
				}
		}
		
		return q;
	}
	
	
	
	void readARFF_File(String path) {
		String s;
		boolean inDataSection = false;
		int row = 0;
		int colIndex = 0;
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			while((s = br.readLine()) != null) {
				
				if(s.charAt(0) == '%')
					continue;
				if(!inDataSection) {
					String[] words = s.split(" ");
					if(words[0].equalsIgnoreCase("@attribute")) {
						if(words[1].charAt(0) != '_') {     // special attributes --> no terms
							terms.add(new TermDf(words[1]));
							termHash.put(words[1], colIndex);
							colIndex++;
						}
						continue;
					}
					if(words[0].equalsIgnoreCase("@data")) {
						inDataSection = true;
						continue;
					}
				}
				if(inDataSection) {
					if(optVerbose) System.out.println("DocumentData #: "+row+ " read!");
					
					String[] words = new String[terms.size()+2];
					int cnti = 0;
					String target = "";
					for(int i = 0; i < s.length(); i++) {
						if(s.charAt(i)== ',') {
							words[cnti] = target;
							target = "";
							cnti++;
						}
						else
							target += s.charAt(i);
					}
					words[cnti] = target;
					
					LinkedList<Item> itemList = new LinkedList<Item>();
					docs.add(new Document(words[words.length-2],words[words.length-1] ));

					mat.add(itemList);
					for(int col = 0; col < terms.size(); col++) {
						int cnt = Integer.parseInt(words[col]);
						if(cnt != 0) {
							itemList.add(new Item(col, cnt));
							terms.get(col).incrementDf();
						}
					}
					row++;
					words = null;
					System.gc();
					if(optVerbose) {
						long memoryLater = Runtime.getRuntime().freeMemory();
						System.out.println("Mem: "+memoryLater);
					}
				}
			}
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
