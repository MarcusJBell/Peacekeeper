package com.gmail.sintinium.peacekeeper.utils;

import java.util.List;

public class ArrayHelper {

    // Custom array util to convert Integers to primitive since I had a lot of problems with Java's built in utils
    public static int[] convertIntegers(List<Integer> integers) {
        int[] result = new int[integers.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = integers.get(i);
        }
        return result;
    }

}
