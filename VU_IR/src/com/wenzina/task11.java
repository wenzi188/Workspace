package com.wenzina;

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


public class task11 {

	static List<Document> docList = new ArrayList<Document>();
	static boolean optSmall = false;
	static boolean optMedium = false;
	static boolean optLarge = false;
	static boolean optBody = true;
	static int optUpperBound = -1;
	static int optLowerBound = 0;
	static boolean optStem = true;
	
	static boolean optVerbose = false;
	static String optDirectory = "";  //C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset";	
	static boolean optHeadline = false;
	static boolean optHelp = false;
	static boolean optStemmer = false;
	
	
	
	public static void main(String[] args) {
		
		// handling of the options
		if(!optionHandling(args))
			return;
		
		int cnt = 0;
		File folder = new File(optDirectory);
		File[] listOfFiles = folder.listFiles();
		if(optVerbose) {
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
		}
		    
		Hashtable<String, LinkedList<Posting>> terms = createInvertedIndexFromFiles(optDirectory);
																					 
		List<String> liste = new ArrayList<String>(terms.keySet());
		Collections.sort(liste); 
		if(optVerbose) {
			for(String key: liste) {
				System.out.print("["+key+"]");
				LinkedList<Posting> poli = terms.get(key);
				for(Posting p: poli) {
					//System.out.print(" Doc: "+ p.getDocId()+"<"+p.getTF()+">");
					System.out.print(" Doc: "+ p.getDocId()+"<"+p.getTF()+">");
				}
				System.out.println();
			}
		}
		
		createARFF(terms);
	}

	
	static Hashtable<String, LinkedList<Posting>> createInvertedIndexFromFiles(String startPath) {
		
		Hashtable<String, LinkedList<Posting>> terms = new  Hashtable<String, LinkedList<Posting>>();
		
		int docIndex = 0;

		File folder = new File(startPath);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isDirectory()) {
				for (File file2 : file.listFiles()) {
				    if (file2.isFile()) {
				    	if(optVerbose)
				    		System.out.println(file2.getAbsolutePath());

				        SourceDoc sd = new SourceDoc(file2.getAbsolutePath(), optStemmer);
				        String[] words = sd.getTokens(true, true);
				        docList.add(new Document(sd.getName(), sd.getClasses()));
				        for(String word: words) {
				        	if(word.length() == 0)
				        		continue;
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
				        if(optVerbose)
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
				writer.write(key + ";"+poli.size()+nl);
			}
			writer.flush();
	        writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

    private static void printUsage() {
        System.err.println(
"Usage: task1_1 [{-v,--verbose}{-h,--headline}{-b,--body}{-?,--help}{-t,--stem}}] [{-s,--small}|{-m,--medium}|{-l,--large} {-L, --Lower #} {-U, --upper #}] -d directory\n");
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
    	CmdLineParser.Option lower = parser.addIntegerOption('L', "lower");
    	CmdLineParser.Option upper = parser.addIntegerOption('U', "upper");
    	
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
    	optLowerBound = (Integer)parser.getOptionValue(lower, 0);
    	optUpperBound = (Integer)parser.getOptionValue(upper, -1);
    	

    	if(optHelp) {
    		String nl = System.getProperty("line.separator");
    		String out = "This program creates an inverted index of the documents in the given directory and returns an ARFF file including the requested information (term frequencies per document)."+nl+nl;
    		out += "Input files: documents to be indexed located in the given directory"+nl;
    		out += "Output file: [small|medium|large]_search.arff - depending on th eoptions -s,-m or -l"+nl;
    		out += "Options: "+nl;
    		out += "   -s: creates an index file called small_search.arff"+nl;
    		out += "   -m: creates an index file called medium_search.arff"+nl;    		
    		out += "   -l: creates an index file called large_search.arff"+nl;
    		out += "    the three options above also determine the name of the output files, but only one is allowed"+nl+nl;
    		out += "   -d: directory where the files to be indexed are stored"+nl;
    		out += "   -h: includes the subject of the news "+nl;
    		out += "   -b: includes the body of the news"+nl;
    		out += "   -t: use stemmer"+nl;
    		out += "   -v: shows debug information during runtime"+nl;
    		out += "   -L #: lower bound for the term frequency"+nl;
    		out += "   -U #: upper bound for the term frequency"+nl;
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
	
	
}
