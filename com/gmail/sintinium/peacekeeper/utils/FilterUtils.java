package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.data.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FilterUtils {

    private static char[] symbols = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '-', '=', '[', ']', '{', '}', '\\', '|', ':', ';', '"', '\'', '?', '/', '>', '.', '<', ',', '~', '`'};

    public static boolean isSpecial(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                for (char sc : symbols) {
                    if (c == sc) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static String filterSpecials(String s) {
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                for (char sc : symbols) {
                    if (c == sc) {
                        result.append(c);
                        break;
                    }
                }
            }
        }
        return result.toString();
    }

    public static int upperCaseCount(String s) {
        int capCount = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) capCount++;
        }
        return capCount;
    }

    public static int lowerCaseCount(String s) {
        int lowerCount = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) lowerCount++;
        }
        return lowerCount;
    }

    public static String filterCaps(String s) {
        s = s.toLowerCase();
        return properCaptilize(s);
    }

    public static String replaceCharAt(String s, int index, char newChar) {
        char[] chars = s.toCharArray();
        chars[index] = newChar;
        return String.valueOf(chars);
    }

    private static String properCaptilize(String s) {
        StringBuilder result = new StringBuilder();
        char[] chars = s.toCharArray();
        boolean capitalize = true;
        for (char c : chars) {
            if (c == '.') {
                capitalize = true;
            } else if (capitalize && Character.isAlphabetic(c)) {
                c = Character.toUpperCase(c);
                capitalize = false;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static ArrayList<MutablePair<Character, Integer>> duplicateCharatersCount(String s, boolean ignoreNumbers) {
        ArrayList<MutablePair<Character, Integer>> result = new ArrayList<>();
        MutablePair<Character, Integer> current = null;
        boolean streak = false;
        char last = ' ';
        boolean init = false;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c) && ignoreNumbers) continue;
            if (!init) {
                init = true;
                last = c;
                continue;
            }
            if (last == c) {
                if (streak) {
                    current.value++;
                } else {
                    current = new MutablePair<>(c, 2);
                    streak = true;
                }
            } else if (streak) {
                streak = false;
                result.add(current);
            }

            last = c;
        }
        if (current != null && !result.contains(current)) result.add(current);
        return result;
    }

    @Nullable
    public static Pair<Character, Integer> highestRepeatedCharacterCount(String s, boolean ignoreNumbers) {
        s = s.toLowerCase();
        Character last = null;
        Pair<Character, Integer> highest = new org.apache.commons.lang3.tuple.MutablePair<>(null, 0);
        int streakCount = 1;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c) && ignoreNumbers) continue;
            if (last == null) {
                last = c;
                continue;
            }
            if (c == last) {
                if (streakCount > highest.getValue()) {
                    highest = new org.apache.commons.lang3.tuple.MutablePair<>(c, streakCount);
                }
                streakCount++;
            } else {
                streakCount = 1;
            }
            last = c;
        }
        if (highest.getValue() == 0) return null;
        return highest;
    }

    public static String charCountToString(char c, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    public static List<Integer> getLastSpaceIndices(List<Character> chars, List<Integer> indices) {
        List<Integer> result = new ArrayList<>();
        for (int index : indices) {
            for (int i = index - 1; i > 0; i--) {
                if (chars.get(i) == ' ') {
                    result.add(i);
                    break;
                }
            }
        }
        return result;
    }

    public static List<Integer> getNextSpaceIndices(List<Character> chars, List<Integer> indices) {
        List<Integer> result = new ArrayList<>();
        for (int index : indices) {
            for (int i = index + 1; i < chars.size(); i++) {
                if (chars.get(i) == ' ') {
                    result.add(i);
                    break;
                }
            }
        }
        return result;
    }

//    public static List<Character> removeNextSpace(List<> chars, int startingIndex) {
//        List<Character> result = new ArrayList<>(chars);
//        for (int i = startingIndex + 1; i < chars.size(); i++) {
//            if (chars.get(i) == ' ') {
//                result.remove(i);
//                break;
//            }
//        }
//        return chars;
//    }

}
