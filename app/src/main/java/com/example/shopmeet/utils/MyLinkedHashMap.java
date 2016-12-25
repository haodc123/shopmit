package com.example.shopmeet.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"serial" })
public class MyLinkedHashMap<K,V> extends LinkedHashMap<K, V> {

	public V getValue(int i) {

       Entry<K, V>entry = this.getEntry(i);
       if(entry == null) return null;

       return entry.getValue();
    }
	public K getKey(int i) {

		Entry<K, V>entry = this.getEntry(i);
		if(entry == null) return null;

	    return entry.getKey();
	}
	public int getIndex(K k) {
		for (int i = 0; i < this.size(); i++) {
			if (this.getKey(i) == k)
				return i;
		}
		return -1;
	}
	public Entry<K, V> getEntry(int i) {
        // check if negetive index provided
        Set<Entry<K,V>>entries = entrySet();
        int j = 0;

        for(Entry<K, V>entry : entries)
            if(j++ == i)return entry;

        return null;
    }
}
