package com.wenzina;

import jargs.gnu.CmdLineParser;

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
	static Hashtable<String, Integer> termHash = new Hashtable<String, Integer>();
	static boolean optSmall = false;
	static boolean optMedium = false;
	static boolean optLarge = false;
	static boolean optVerbose = false;
	static String optDirectory = "";  //C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset";	
	static boolean optHeadline = false;
	static boolean optBody = false;
	static boolean optHelp = false;
	static boolean optStemmer = false;
	static String group = "W1";
	
	public static void main(String[] args) {

		// handling of the options
		if(!optionHandling(args))
			return;

		// preparations
		String prefix = "";
		if(optSmall) prefix = "small";
		if(optMedium) prefix = "medium";
		if(optLarge) prefix = "large";
		String nl = System.getProperty("line.separator");
		DecimalFormat f = new DecimalFormat("#0.00000");
		String ordner = optDirectory;
		String[] rankDocs = new String []  {"misc.forsale/76057", "talk.religion.misc/83561", "talk.politics.mideast/75422",
				"sci.electronics/53720", "sci.crypt/15725", "misc.forsale/76165", "talk.politics.mideast/76261", "alt.atheism/53358",
				"sci.electronics/54340", "rec.motorcycles/104389", "talk.politics.guns/54328", "misc.forsale/76468", "sci.crypt/15469",
				"rec.sport.hockey/54171", "talk.religion.misc/84177", "rec.motorcycles/104727", "comp.sys.mac.hardware/52165",
				"sci.crypt/15379", "sci.space/60779", "sci.med/59456"};

		// read the arff file
		readFromFile(prefix+"_search.arff");
		int N = mat.size();
		
		if(optVerbose) {
			System.out.println("Docs: N: "+N);
			for(TermDf t: terms) {
				if(t.getDf() > 0)
					System.out.println(t.toString());
			}
		}
		
		// calculate weights of the documents: euclidean normalization
		for(int row = 0; row < mat.size(); row++) {
			double sum = 0;
			for(Item it: mat.get(row)) {
				sum += it.getItemCnt()*it.getItemCnt();
			}
			double length = Math.sqrt(sum);
			for(Item it: mat.get(row)) {
				//String s = terms.get(it.getItemCol()).getTerm();
				it.weightNormalize(length);
			}
		}		
		// rank the documents
		for(int k = 0; k < rankDocs.length; k++) {
			SourceDoc sd = new SourceDoc(ordner + "/" +rankDocs[k], optStemmer);
			LinkedList<Item> qryVector = createQryVectorNew(sd.getTokens(optHeadline, optBody));
			if(optVerbose) {
				for(Item it: qryVector) {
					System.out.println(terms.get(it.getItemCol())+ " - " + it.getItemCnt());
				}
			}
			// weight queryVector: weight: IDF
			for(Item it: qryVector) {
				//String s = terms.get(it.getItemCol()).getTerm();
				it.weightItemIDF(N, terms.get(it.getItemCol()).getDf());
			}
			if(optVerbose) {
				for(Item it: qryVector) {
					System.out.println(terms.get(it.getItemCol())+ " - " + it.getItemCnt());
				}
			}
			double[] qryArray = mapLinkedList2Array(qryVector);
		
			// scoring
			for(int row = 0; row < mat.size(); row++) {
				double[] docArray = mapLinkedList2Array(mat.get(row));
				double score = 0;
				for(int col = 0; col < terms.size(); col++) {
					if(qryArray[col] != 0 && docArray[col] != 0) {
						score += qryArray[col] * docArray[col];
					}
				}
				docs.get(row).setScore(score);
				if (optVerbose) System.out.print(".");
			}
		
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
	}

    private static void printUsage() {
        System.err.println(
"Usage: task1_2 [{-v,--verbose}{-h,--headline}{-b,--body}{-?,--help}{-t,--stem}}] [{-s,--small}|{-m,--medium}|{-l,--large}] -d directory\n");
        System.err.println("use -? option for help");
    }

    private static boolean optionHandling(String[] args) {
    	boolean ok = true;
    	
    	CmdLineParser parser = new CmdLineParser();
       	CmdLineParser.Option small = parser.addBooleanOption('s', "small");
    	CmdLineParser.Option medium = parser.addBooleanOption('m', "medium");
    	CmdLineParser.Option large = parser.addBooleanOption('l', "large");
    	CmdLineParser.Option verbose = parser.addBooleanOption('v', "verbose");
    	CmdLineParser.Option path = parser.addStringOption('d', "directory");
    	CmdLineParser.Option body = parser.addBooleanOption('b', "body");
    	CmdLineParser.Option stemmer = parser.addBooleanOption('t', "stemmer");
    	CmdLineParser.Option headline = parser.addBooleanOption('h', "headline");
    	CmdLineParser.Option help = parser.addBooleanOption('?', "help");
    	
    	try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }
        optDirectory = (String)parser.getOptionValue(path, "");
    	optSmall = (Boolean)parser.getOptionValue(small, Boolean.FALSE);
    	optMedium = (Boolean)parser.getOptionValue(medium, Boolean.FALSE);
    	optLarge = (Boolean)parser.getOptionValue(large, Boolean.FALSE);
    	optVerbose = (Boolean)parser.getOptionValue(verbose, Boolean.FALSE);
    	optHeadline = (Boolean)parser.getOptionValue(headline, Boolean.FALSE);
    	optBody = (Boolean)parser.getOptionValue(body, Boolean.FALSE);
    	optStemmer = (Boolean)parser.getOptionValue(stemmer, Boolean.FALSE);
    	optHelp = (Boolean)parser.getOptionValue(help, Boolean.FALSE);

    	if(optHelp) {
    		String nl = System.getProperty("line.separator");
    		String out = "This program compares 20 different news in a given index file and returns the ranking of the top ten documents from the search index."+nl+nl;
    		out += "Input file: [small|medium|large]_search.arff"+nl;
    		out += "Output files: [small|medium|large]_topic?_groupW1.txt - one for every of the 20 search topics containing information in trec style"+nl;
    		out += "Options: "+nl;
    		out += "   -s: searches for an index file called small_search.arff"+nl;
    		out += "   -m: searches for an index file called medium_search.arff"+nl;    		
    		out += "   -l: searches for an index file called large_search.arff"+nl;
    		out += "    the three options above also determine the name of the output files, but only one is allowed"+nl+nl;
    		out += "   -d: directory where the query files are stored"+nl;
    		out += "   -h: includes the subject of the news of the query document"+nl;
    		out += "   -b: includes the body of the news of the query document"+nl;
    		out += "   -t: use stemmer"+nl;
    		out += "   -v: shows debug information during runtime"+nl;
    		out += "   -?: shows this message"+nl;
    		System.out.println(out);
    		return false;
    	}
    	
    	int cnt = 0;
    	if(optSmall) cnt++;
    	if(optMedium) cnt++;
    	if(optLarge) cnt++;
    	if(cnt == 0)  {
    		System.err.println("you have to set one option for -s small or -m medium or -l large!");
    		ok = false;
    	}
    	if(cnt > 1)  {
    		System.err.println("only one of the options -s small or -m medium or -l large is allowed!");
    		ok = false;
    	}
    	if(optDirectory.length() == 0) {
    		System.err.println("the path to the directory for the documents is not set - see option - d directory!");
    		ok = false;
		} else {
			File f = new File(optDirectory);
			if(!f.exists()) {
				System.err.println("path to the directory does not exist - see option - d directory!");
				ok = false;
			}
		}
    	if(!optHeadline && !optBody) {
    		System.err.println("at least one of the options -b or -h must be set!");
    		ok = false;
    	}
    	
    	if(!ok)
			System.err.println("program TERMINATED!");
    	return ok;
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
					if(optVerbose) System.out.println("DocumentData #: "+row+ "read!");
					
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

	static void output() {
		// test output
		for(int row = 0; row < mat.size(); row++) {
			StringBuffer sb = new StringBuffer();
			for(int col = 0; col < terms.size(); col++) {
				double val = 0;
				for(Item it: mat.get(row)) {
					if(it.getItemCol() == col) {
						val = it.getItemCnt();
						break;
					}
				}
				sb.append(","+val);
			}
			System.out.println(docs.get(row)+":" +sb.toString());
		}
	}
	
}
