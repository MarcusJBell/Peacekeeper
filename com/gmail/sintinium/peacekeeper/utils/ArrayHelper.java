package com.gmail.sintinium.peacekeeper.utils;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    public static char[] covertCharArray(Character[] characters) {
        char[] chars = new char[characters.length];
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == null) continue;
            chars[i] = characters[i];
        }
        return chars;
    }

    public static Character[] covertCharArray(char[] characters) {
        Character[] chars = new Character[characters.length];
        for (int i = 0; i < characters.length; i++) {
            chars[i] = characters[i];
        }
        return chars;
    }

    public static <T> List<T> removeAllIndices(List<T> list, List<Integer> indices) {
        List<Integer> sorted = new ArrayList<>(new HashSet<>(indices));
        Collections.sort(sorted);
        sorted = Lists.reverse(sorted);
        Bukkit.getConsoleSender().sendMessage(sorted.toString());
        for (int i : sorted) {
            list.remove(i);
        }
        return list;
    }

}
