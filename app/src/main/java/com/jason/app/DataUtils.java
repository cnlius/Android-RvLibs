package com.jason.app;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by liusong on 2017/12/5.
 */

public class DataUtils {

    /**
     * 生成一个固定长度字符列list
     *
     * @param size list的大小
     * @return
     */
    public static List<String> createStringList(int start,int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(start+i));
        }
        return list;
    }

    public static List<String> createStringList(int size) {
        return createStringList(0,size);
    }

}
