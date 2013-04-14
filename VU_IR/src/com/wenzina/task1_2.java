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

public class task1_2 {

	static LinkedList<TermDf> terms = new LinkedList<TermDf>();
	static LinkedList<LinkedList<Item>> mat = new LinkedList<LinkedList<Item>>();
	static List<Document> docs = new ArrayList<Document>();
	//static LinkedList<Document> docs = new LinkedList<Document>();
	static Hashtable<String, Integer> termHash = new Hashtable<String, Integer>();
	static boolean optSmall = false;
	static boolean optMedium = false;
	static boolean optLarge = false;
	static String group = "W1";
	
	public static void main(String[] args) {
		
		optLarge = true;
		
		String prefix = "";
		if(optSmall) prefix = "small";
		if(optMedium) prefix = "medium";
		if(optLarge) prefix = "large";
		String nl = System.getProperty("line.separator");
		DecimalFormat f = new DecimalFormat("#0.00000");
		
		
		readFromFile(prefix+"_search.arff");
		int N = mat.size();
		String ordner = "C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset";
		
		String[] rankDocs = new String []  {"misc.forsale/76057", "talk.religion.misc/83561", "talk.politics.mideast/75422",
				"sci.electronics/53720", "sci.crypt/15725", "misc.forsale/76165", "talk.politics.mideast/76261", "alt.atheism/53358",
				"sci.electronics/54340", "rec.motorcycles/104389", "talk.politics.guns/54328", "misc.forsale/76468", "sci.crypt/15469",
				"rec.sport.hockey/54171", "talk.religion.misc/84177", "rec.motorcycles/104727", "comp.sys.mac.hardware/52165",
				"sci.crypt/15379", "sci.space/60779", "sci.med/59456"};
		
		System.out.println("Docs: N: "+N);
		//output();
		for(TermDf t: terms) {
			if(t.getDf() > 0)
				System.out.println(t.toString());
		}
		
		// -------------------------------
		// calculate weights: euclidean normalization
		for(int row = 0; row < mat.size(); row++) {
			// length of document vector
			double sum = 0;
			for(Item it: mat.get(row)) {
				sum += it.getItemCnt()*it.getItemCnt();
				//System.out.print("-");
			}
			double length = Math.sqrt(sum);
			for(Item it: mat.get(row)) {
				String s = terms.get(it.getItemCol()).getTerm();
				it.weightNormalize(length);
				//it.weightItemIDF(N, terms.get(it.getItemCol()).getDf());
				//System.out.print("*");
			}
			//System.out.println("!");
		}		
		//output();

		
		for(int k = 0; k < rankDocs.length; k++) {
		//for(int k = 6; k < rankDocs.length; k++) {

			SourceDoc sd = new SourceDoc(ordner + "/" +rankDocs[k]);
			LinkedList<Item> qryVector = createQryVectorNew(sd.getTokens(false, true, true));
			// output test
			for(Item it: qryVector) {
				//System.out.println(terms.get(it.getItemCol())+ " - " + it.getItemCnt());
			}
			// weight queryVector: weight: IDF
			for(Item it: qryVector) {
				String s = terms.get(it.getItemCol()).getTerm();
				it.weightItemIDF(N, terms.get(it.getItemCol()).getDf());
			}
			for(Item it: qryVector) {
				//System.out.println(terms.get(it.getItemCol())+ " - " + it.getItemCnt());
			}
			double[] qryArray = mapLinkedList2Array(qryVector);
		
			// scoring
			for(int row = 0; row < mat.size(); row++) {
				double[] docArray = mapLinkedList2Array(mat.get(row));
				double score = 0;
				for(int col = 0; col < terms.size(); col++) {
					if(qryArray[col] != 0 && docArray[col] != 0) {
						//System.out.println("Yeah! "+ terms.get(col)+ " "+qryArray[col]+ " * "+docArray[col]);
						score += qryArray[col] * docArray[col];
					}
				}
				docs.get(row).setScore(score);
				//System.out.print(".");
			}
		
			for(int i = 0; i < docs.size(); i++){
				if(docs.get(i).getName().equalsIgnoreCase("53358"))
					System.out.println("iiiii"+docs.get(i));
			}
			
			List<Document> docs2Sort = new ArrayList<Document>();
			for(Document d: docs) {
				docs2Sort.add(d);
			}
			
			
			//Collections.sort(docs, new SortDocs());
			Collections.sort(docs2Sort, new SortDocs());
			System.out.println("!!!!!!!!!!!!!!!!!!");
			for(int i = 0; i < 10; i++){
				System.out.println(docs2Sort.get(i));
			}
			
			try {
				File file = new File(prefix+"_topic"+(k+1)+"_group"+group+".txt");
				FileWriter writer = new FileWriter(file, false);
				for(int i = 0; i < 10; i++){
					//writer.write("topic"+(k+1)+" Q0 "+docs.get(i).getClasses()+"/"+docs.get(i).getName() +" "+(i+1)+" "+f.format(docs.get(i).getScore())+ " group"+group+"_"+prefix);
					writer.write("topic"+(k+1)+" Q0 "+docs2Sort.get(i).getClasses()+"/"+docs2Sort.get(i).getName() +" "+(i+1)+" "+f.format(docs2Sort.get(i).getScore())+ " group"+group+"_"+prefix);
					if(i < 9)
						writer.write(nl);
					System.out.println(docs2Sort.get(i));
				}
				System.out.println("-----------------------------------");
				writer.flush();
		        writer.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	static double[] mapLinkedList2Array(LinkedList<Item> list) {
		double[] d = new double[terms.size()];
		for(int i = 0; i< terms.size(); i++)
			d[i] = 0;
		for(Item it: list) {
			d[it.getItemCol()] = it.getItemCnt();
		}
		return d;
	}

	static LinkedList<Item> createQryVectorNew(String[] words) {
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

	static LinkedList<Item> createQryVector(String s) {
		LinkedList<Item> q = new LinkedList<Item>();
		String[] words = s.split(" ");
		for(int col = 0; col < terms.size(); col++) {
			for(String word: words) {
				if(terms.get(col).getTerm().equalsIgnoreCase(word)) {
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
		}
		
		return q;
	}
	
	
	static void readFromFile(String path) {
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String s;
			boolean inDataSection = false;
			int row = 0;
			int colIndex = 0;
			while((s = br.readLine()) != null) {
				if(s.substring(0,1).equalsIgnoreCase("%"))
					continue;
				if(!inDataSection) {
					String[] words = s.split(" ");
					if(words[0].equalsIgnoreCase("@attribute")) {
						if(!words[1].substring(0,1).equalsIgnoreCase("_")) {
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
					// for debug reasons
					//if(row > 2800)
					//	break;
//System.out.println("Terms: "+terms.size());
System.out.println("Line: "+row);
					// V1
					// String[] words = s.split(",");
					// V2
					/*int pos = 0;
					int lastpos = -1;
					String[] words = new String[terms.size()+2];
					int cnti = 0;
					while((pos = s.indexOf(",", lastpos+1)) > -1) {
						words[cnti] = s.substring(lastpos+1, pos);
						lastpos = pos;
						cnti++;
					}
					words[cnti] = s.substring(lastpos+1); */
					
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
					/*int kk = words.length;
					String s1 = words[words.length-2];
					String s2 = words[words.length-1];*/
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
					long memoryLater = Runtime.getRuntime().freeMemory();
					System.out.println("Mem: "+memoryLater);
				}
			}
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void output() {
		// test output
		for(int row = 0; row < mat.size(); row++) {
			//String line = "";
			StringBuffer sb = new StringBuffer();
			for(int col = 0; col < terms.size(); col++) {
				double val = 0;
				for(Item it: mat.get(row)) {
					if(it.getItemCol() == col) {
						val = it.getItemCnt();
						break;
					}
				}
				//line +=","+val;
				sb.append(","+val);
			}
			//System.out.println(docs.get(row)+":" +line);
			System.out.println(docs.get(row)+":" +sb.toString());
		}
	}
	
}
