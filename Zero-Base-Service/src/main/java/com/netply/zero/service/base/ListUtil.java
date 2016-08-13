package com.netply.zero.service.base;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class ListUtil {
    public static <T> List<T> stringToArray(String s, Class<T[]> clazz) {
        return Arrays.asList(new Gson().fromJson(s, clazz));
    }
}
