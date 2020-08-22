package com.prituladima.benchmarks;

import java.util.HashMap;

public class MemoryTEst {
    public static void main(String[] args) {
        HashMap<Object, Object> o = new HashMap<>();

        System.out.println(ObjectSizeFetcher.getObjectSize(o));
    }
}
