package com.synload.mtg.backend.database.models;

import com.sun.source.tree.BinaryTree;

import java.util.Date;
import java.util.TreeMap;

public class MTGSet extends Set {

    private String code;
    private String type;
    private int count;
    private Date release;
    private Card[] cards;

    public MTGSet(String name, String code, String type, int count, Date release) {
        super(name);
        this.code = code;
        this.type = type;
        this.count = count;
        this.release = release;
    }
}
