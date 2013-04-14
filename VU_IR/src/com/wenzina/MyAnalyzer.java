package com.wenzina;

import java.io.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {

	private static final String [] stopWords =  {""};
	
	
	public TokenStream tokenStream(String fieldName, Reader reader) {
		Tokenizer tokenizer = new StandardTokenizer(reader);
		TokenFilter lowerCaseFilter = new LowerCaseFilter(tokenizer);
		TokenFilter stopFilter = new StopFilter(lowerCaseFilter, stopWords);
		TokenFilter stemFilter = new PorterStemFilter(stopFilter);
		return stemFilter;
	}
	
}
