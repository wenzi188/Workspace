package com.wenzina;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//import opennlp.tools.tokenize.*;
//import opennlp.maxent.*;
//import opennlp.tools.util.*;


import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.*;


public class SourceDoc {
	String name;
	String classes;
	String subject;
	String body; 
	String path;
	
	// for OpenNLP
	//static InputStream is;
	//static TokenizerModel model;
	//static Tokenizer tokenizer;

	static Analyzer analyzer;
	
	
	SourceDoc (String p) {
		path = p;
		File file = new File(p);
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
		name = absolutePath.substring(absolutePath.lastIndexOf(File.separator)+1);
		classes = filePath.substring(filePath.lastIndexOf(File.separator)+1);
		body = "";
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String s;
			boolean inBody = false;
			while((s = br.readLine()) != null) {
				if(s.indexOf("Subject") == 0) {
					subject = s.substring(s.indexOf("Subject")+9);
				}
				if(s.indexOf("Lines") == 0) {
					inBody = true;
					// to skip empty line
					br.readLine();
					continue;
				}
				if(inBody) {
					body += s;
				}
			}
			fr.close();

			// Version 1: OpenNLP
			// is = new FileInputStream("en-token.bin");
			// model = new TokenizerModel(is);
			// tokenizer = new TokenizerME(model);

			// Version 2: Lucene
			analyzer = new MyAnalyzer();
			
			
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	String getName () {
		return name;
	}
	String getClasses () {
		return classes;
	}
	
	
	String[] getTokens(boolean stemming, boolean inclSubject, boolean inclBody) {
		if(!inclSubject && !inclBody)
			return new String[] {""};
		String myText = "";
		if(inclSubject)
			myText += subject;
		if(inclBody)
			myText += " "+body;
		try {
			// version1: opnenlp
			//InputStream is = new FileInputStream("en-token.bin");
			//TokenizerModel model = new TokenizerModel(is);
			//Tokenizer tokenizer = new TokenizerME(model);
			//String tokens[] = tokenizer.tokenize(myText);
			//is.close();
			// for(int i = 0; i < tokens.length; i++) {
			//	tokens[i] = tokens[i].replace(".", "");
			//	if(!tokens[i].matches ("([A-Za-z])*"))
			//		tokens[i] = "";
			//	else
			//		tokens[i] = tokens[i].toLowerCase();
			//}

			// version2 lucene
			Reader reader = new StringReader(myText);
			TokenStream ts = analyzer.tokenStream(null, reader);
			Token token = ts.next();
			int counter = 0;
			List<String> tokLi = new ArrayList<String>();
			while(token != null) {
				// System.out.println(token.termText());
				if(token.termText().matches ("([A-Za-z])*")) {
					tokLi.add(token.termText());
					counter++;
				}
				token = ts.next();
			}
			
			String[] tokens = new String[counter];
			int counter2 = 0;
			for(String s : tokLi) {
				tokens[counter2] = s;
				counter2++;
			}
				
			
			
			/*for(int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].replace(".", "");
				if(!tokens[i].matches ("([A-Za-z])*"))
					tokens[i] = "";
				else
					tokens[i] = tokens[i].toLowerCase();
			} */
			return tokens;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new String[] {""};
		}
		

	}
	
	
}
