package com.wenzina;

import jargs.gnu.CmdLineParser;

import java.io.File;

public class Exercise2 {

	static int optSize = -1;  // 0.. small; 1..medium; 2..large
	//static boolean optSmall = false;
	static boolean optMedium = false;
	static boolean optLarge = false;
	static boolean optVerbose = false;
	static String optDirectory = ""; 	
	static boolean optHeadline = false;
	static boolean optBody = false;
	static boolean optHelp = false;
	static boolean optStemmer = false;
	static final String group = "W1";
	static final String[] rankDocs = new String []  {"misc.forsale/76057", "talk.religion.misc/83561", "talk.politics.mideast/75422",
			"sci.electronics/53720", "sci.crypt/15725", "misc.forsale/76165", "talk.politics.mideast/76261", "alt.atheism/53358",
			"sci.electronics/54340", "rec.motorcycles/104389", "talk.politics.guns/54328", "misc.forsale/76468", "sci.crypt/15469",
			"rec.sport.hockey/54171", "talk.religion.misc/84177", "rec.motorcycles/104727", "comp.sys.mac.hardware/52165",
			"sci.crypt/15379", "sci.space/60779", "sci.med/59456"};

	// static final String[] rankDocs = new String []  {"alt.atheism/51120"};

	
	public static void main(String[] args) {
		// handling of the options
		//if(!optionHandling(args))
		//	return;
		
		// only for testing!
		optSize = 0;  // small
		optStemmer = true;
		optHeadline = true;
		optBody = true;
		optVerbose = true;
		optDirectory = "C:/projects/reinhardt/PhD/ECTS/InformationRetrieval/Ex1/20_newsgroups_subset"; 
		
		BM25Ranker b25 = new BM25Ranker(optSize, optVerbose);
		
		for(int i = 0; i < rankDocs.length; i++) {
			b25.rankDocument(optDirectory+"/"+rankDocs[i], optStemmer, optHeadline, optBody);
			b25.writeTopTenList(i, group);
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
    	//optSmall = (Boolean)parser.getOptionValue(small, Boolean.FALSE);
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
    	//if(optSmall) cnt++;
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
