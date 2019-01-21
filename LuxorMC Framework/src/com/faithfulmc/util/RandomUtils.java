package com.faithfulmc.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomUtils {
    private static final String[] numeric;
    private static final String[] alpha;

    public static String randomAlphaNumeric(final Integer maxChar) {
        final ArrayList<String> alphaText = new ArrayList<String>();
        for (final String rand : RandomUtils.alpha) {
            alphaText.add(rand);
        }
        for (final String rand : RandomUtils.numeric) {
            alphaText.add(rand);
        }
        String var6 = null;
        for (int i = 0; i < maxChar; ++i) {
            final Random var7 = new Random();
            final Integer var8 = var7.nextInt(alphaText.size());
            if (var6 == null) {
                var6 = alphaText.get(var8);
            }
            var6 += alphaText.get(var8);
        }
        return var6;
    }

    public static Integer randomNumeric(final Integer maxChar) {
        final ArrayList<String> alphaText = new ArrayList<String>();
        for (final String rand : RandomUtils.numeric) {
            alphaText.add(rand);
        }
        String var6 = null;
        for (int i = 0; i < maxChar; ++i) {
            final Random var7 = new Random();
            final Integer var8 = var7.nextInt(alphaText.size());
            if (var6 == null) {
                var6 = alphaText.get(var8);
            }
            var6 += alphaText.get(var8);
        }
        return Integer.parseInt(var6);
    }

    public static String randomString(final Integer maxChar) {
        final ArrayList<String> alphaText = new ArrayList<>();
        for (final String rand : RandomUtils.alpha) {
            alphaText.add(rand);
        }
        String var6 = null;
        for (int i = 0; i < maxChar; ++i) {
            final Random var7 = new Random();
            final Integer var8 = var7.nextInt(alphaText.size());
            if (var6 == null) {
                var6 = alphaText.get(var8);
            }
            var6 += alphaText.get(var8);
        }
        return var6;
    }

    static {
        numeric = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        alpha = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    }
}
