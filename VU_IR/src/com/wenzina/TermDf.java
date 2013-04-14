package com.wenzina;

public class TermDf {

		String term;
		int df;
		
		TermDf(String t) {
			term = t;
			df = 0;
		}
		
		void incrementDf(){
			df++;
		}
		
		int getDf() {
			return df; 
		}
		
		public String getTerm() {
			return term;
		}
		
		public String toString() {
			return term+": df:"+df;
		}
		
}
