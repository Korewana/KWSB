/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal.entities;


import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class KWSBList<K, V> {

    private int SIZE;

    private final HashMap<Integer, K> keys = new HashMap<>();
    private final HashMap<Integer, V> values = new HashMap<>();

    public void add(K key, V value) {
        keys.put(SIZE, key);
        values.put(SIZE, value);
        SIZE++;
    }

    public V get(int i) {
        if(i >= SIZE) throw new IndexOutOfBoundsException();
        return values.get(i);
    }

    public V get(K key) {
        if(!keys.containsValue(key)) throw new NullPointerException("Key was not found");
        AtomicReference<Integer> needed_index = new AtomicReference<>();
        keys.forEach((index, target_key) -> {
            if(target_key == key) needed_index.set(index);
        });
        if(needed_index.get() == null) throw new NullPointerException("Key was not found");
        return values.get(needed_index.get());
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) throw new NullPointerException();
        if (size() > 0) {
           for(int i = 0; i < size(); i++) {
               action.accept(keys.get(i), values.get(i));
           }
        }
    }

    public int size() {
        return values.size();
    }

}
