package com.wenzina;

import java.util.Comparator;

public class SortDocs implements Comparator<Document> {
	@Override
    public int compare(Document d1, Document d2) {
        return d2.getScore().compareTo(d1.getScore());
    }
}
