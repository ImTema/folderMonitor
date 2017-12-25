package com.javamonitor;

import java.util.HashMap;
import java.util.Map;

public class UserData {

    private String user;
    private long timestamp;
    Map<String, Long> sites = new HashMap<>();

    public UserData(String name, String url, String integer, long time) {
        user=name;
        timestamp=time;
        sites.put(url, Long.valueOf(integer));
    }

    public UserData(String name, String url, long diff, long time) {
        user=name;
        timestamp=time;
        sites.put(url, diff);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "sites=" + sites +
                "}\n";
    }
}
