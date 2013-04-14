package com.wenzina;

public class Posting {
	int docId;
	int termFrequency;
	
	Posting(int d) {
		docId = d;
		termFrequency = 1;
	}
	
	void incrementTF() {
		termFrequency++;
	}
	
	int getDocId() {
		return docId;
	}
	int getTF() {
		return termFrequency;
	}
	
}
