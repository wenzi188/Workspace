package com.wenzina;

public class Document {

	String name;
	String classes;
	Double score;
	
	Document(String n, String c) {
		name = n;
		classes = c;
	}
	void setScore(Double s) {
		score = s;
	}
	
	Double getScore() {
		return score;
	}
	
	String getName() {
		return name;
	}
	String getClasses() {
		return classes;
	}
	
	@Override
	public String toString() {
		return name + "/"+classes+" - " +score;
	}
	
}
