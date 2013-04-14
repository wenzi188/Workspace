package com.wenzina;

public class Item {

	int col;
	double cnt;
	
	Item(int c1, int c2) {
		col = c1;
		cnt = c2;
	}
	
	int getItemCol() {
		return col;
	}
	double getItemCnt() {
		return cnt;
	}
	
	void incrementItemCnt() {
		cnt++;
	}
	
	void weightItemIDF(int N, int df) {
		cnt = Math.log10(N/Integer.valueOf(df).doubleValue());
	}
	
	void weightNormalize(double length) {
		cnt = cnt/length;
	}
	
	
}
