package com.netply.zero.service.base;

public class Pair<X, Y> {
    private X left;
    private Y right;


    public Pair(X left, Y right) {
        this.left = left;
        this.right = right;
    }

    public X getLeft() {
        return left;
    }

    public Y getRight() {
        return right;
    }
}
