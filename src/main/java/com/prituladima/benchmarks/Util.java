package com.prituladima.benchmarks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Util {
    public static String getGridRange(int a, int b) {
        return String.format("A1:%s%s", getColumn(a), b);
    }

    protected static String getColumn(int columnNumber) {

        StringBuilder columnName = new StringBuilder();
        while (columnNumber > 0) {
            int rem = columnNumber % 26;
            if (rem == 0) {
                columnName.append("Z");
                columnNumber = (columnNumber / 26) - 1;
            } else {
                columnName.append((char) ((rem - 1) + 'A'));
                columnNumber = columnNumber / 26;
            }
        }
        return columnName.reverse().toString();
    }

    private static int[] shuffled(int from, int upTo) {
        int[] ans = new int[upTo - from];
        for (int i = from; i < upTo; i++) {
            ans[i - from] = i;
        }
        shuffle(ans);
        return ans;
    }

    public static void shuffle(int[] array) {
        Random random = new Random();
        for (int i = 0, j; i < array.length; i++) {
            j = i + random.nextInt(array.length - i);
            int buf = array[j];
            array[j] = array[i];
            array[i] = buf;
        }
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    protected static char getMostRepeatingCharacter(char[] results) {
        Map<Character, Integer> multiSet = new HashMap<>();
        for (char result : results) {
            multiSet.merge(result, 1, Integer::sum);
        }
        int max = 0;
        char maxChar = '\0';
        for (char ch : multiSet.keySet()) {
            int val = multiSet.get(ch);
            if (val > max) {
                max = val;
                maxChar = ch;
            }
        }
        if (multiSet.getOrDefault('H', 0).equals(multiSet.getOrDefault('A', 0))) {
            return '\0';
        }
        return maxChar;
    }

}
