package com.netply.zero.service.base;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtil {
    public static <T> List<T> stringToArray(String json, Class<T[]> clazz) {
        try {
            return Arrays.asList(new Gson().fromJson(json, clazz));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return Collections.emptyList();
    }
}
