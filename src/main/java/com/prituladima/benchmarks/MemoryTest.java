package com.prituladima.benchmarks;

import androidx.collection.ArrayMap;
import com.prituladima.memory.InstrumentationAgent;

import java.util.HashMap;
import java.util.Map;

public class MemoryTest {
    public static void main(String[] args) {
        Map<Object, Object> o = new HashMap<>();

        System.out.printf("HashMap : %d\n", InstrumentationAgent.deepSizeOf(o));

        Map<Object, Object> o2 = new HashMap<>();
        o2.put("fdvsdfvsdvs", "sdfvsdfvsdfvs");
        o2.put("fdvsdfvsdvsa", "sdfvsdfvsdfvs");
        o2.put("fdvsdfvsdvss", "sdfvsdfvsdfvs");
        o2.put("fdvsdfvsdvsd", "sdfvsdfvsdfvs");
        o2.put("fdvsdfvsdvsf", "sdfvsdfvsdfvs");


        System.out.printf("HashMap : %d\n", InstrumentationAgent.deepSizeOf(o2));

        Map<Object, Object> a2 = new ArrayMap<>();
        a2.put("fdvsdfvsdvs", "sdfvsdfvsdfvs");
        a2.put("fdvsdfvsdvsa", "sdfvsdfvsdfvs");
        a2.put("fdvsdfvsdvss", "sdfvsdfvsdfvs");
        a2.put("fdvsdfvsdvsd", "sdfvsdfvsdfvs");
        a2.put("fdvsdfvsdvsf", "sdfvsdfvsdfvs");
        System.out.printf("ArrayMap : %d\n", InstrumentationAgent.deepSizeOf(a2));


        System.out.printf("int : %d\n", InstrumentationAgent.deepSizeOf(2));
        System.out.printf("boolean : %d\n", InstrumentationAgent.deepSizeOf(true));
        System.out.printf("char : %d\n", InstrumentationAgent.deepSizeOf('a'));
        System.out.printf("String : %d\n", InstrumentationAgent.deepSizeOf("Long string"));
        System.out.printf("String : %d\n", InstrumentationAgent.deepSizeOf("Long strinfhbdljfh" +
                "dfljhbdlhjbf lhdbf hbdf" +
                "sdfjl bdsklfbjkb jksdfbvjksdbfjkvbsdjfkg Long string Long string"));

        System.out.printf("long : %d\n", InstrumentationAgent.deepSizeOf(10L));


    }
}
