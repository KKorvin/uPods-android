package com.chickenkiller.upods2.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Use it for transfering data across the app.
 * Created by alonzilberman on 11/21/15.
 */
public class DataHolder {
    private static DataHolder dataHolder;
    private Map<String, WeakReference<Object>> data;

    private DataHolder() {
        data = new HashMap<String, WeakReference<Object>>();
    }

    public static DataHolder getInstance() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public void save(String id, Object object) {
        data.put(id, new WeakReference<Object>(object));
    }

    public boolean contains(String id) {
        return data.containsKey(id);
    }

    public Object retrieve(String id) {
        if (data.containsKey(id)) {
            WeakReference<Object> objectWeakReference = data.get(id);
            return objectWeakReference.get();
        } else {
            return null;
        }
    }

    public void remove(String id) {
        if (data.containsKey(id)) {
            data.remove(id);
        }
    }
}
