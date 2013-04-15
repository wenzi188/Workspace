package com.wenzina;

import java.io.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {

	private static final String [] stopWords =  {""};
	private boolean stemmer;
	
	public MyAnalyzer(boolean optStemmer) {
		stemmer = optStemmer;
	}
	
	public TokenStream tokenStream(String fieldName, Reader reader) {
		Tokenizer tokenizer = new StandardTokenizer(reader);
		TokenFilter lowerCaseFilter = new LowerCaseFilter(tokenizer);
		TokenFilter stopFilter = new StopFilter(lowerCaseFilter, stopWords);
		if(stemmer) {
			TokenFilter stemFilter = new PorterStemFilter(stopFilter);
			return stemFilter;
		}
		return stopFilter;
	}
	
}
