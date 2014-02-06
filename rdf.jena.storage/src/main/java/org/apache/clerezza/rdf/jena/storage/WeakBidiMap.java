/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.rdf.jena.storage;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.wymiwyg.commons.util.collections.BidiMap;

/**
 * Like a WeakHashMap but Bidirectional. Only weak references are kept to the
 * key
 *
 * @author reto
 *
 */
public class WeakBidiMap<K, V> implements BidiMap<K, V> {

    private final Map<K, V> forward = new WeakHashMap<K, V>();
    private final Map<V, WeakReference<K>> backward = new HashMap<V, WeakReference<K>>();

    public WeakBidiMap() {
        super();
    }

    @Override
    public K getKey(V value) {
        WeakReference<K> ref = backward.get(value);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public V get(Object key) {
        return forward.get(key);
    }

    @Override
    public V put(K key, V value) {
        //remove possible existing with same value

        WeakReference<K> ref = backward.get(value);
        if (ref != null) {
            K oldKey = ref.get();
            if (oldKey != null) {
                forward.remove(oldKey);
            }
        }
        if (forward.containsKey(key)) {
            V oldValue = forward.get(key);
            backward.remove(oldValue);
        }
        backward.put(value, new WeakReference<K>(key));
        return forward.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V value = forward.remove(key);
        backward.remove(value);
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();

    }

    @Override
    public Set<K> keySet() {
        return forward.keySet();
    }

    @Override
    public Collection<V> values() {
        return backward.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return forward.entrySet();
    }

    public BidiMap<V, K> inverse() {
        return new BidiMap<V,K>() {

            @Override
            public V getKey(K v) {
                return WeakBidiMap.this.get(v);
            }

            @Override
            public BidiMap<K, V> inverse() {
                return WeakBidiMap.this;
            }

            @Override
            public int size() {
                return WeakBidiMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return WeakBidiMap.this.isEmpty();
            }

            @Override
            public boolean containsKey(Object o) {
                return WeakBidiMap.this.containsValue(o);
            }

            @Override
            public boolean containsValue(Object o) {
                return WeakBidiMap.this.containsKey(o);
            }

            @Override
            public K get(Object o) {
                try {
                    return WeakBidiMap.this.getKey((V)o);
                } catch (ClassCastException e) {
                    return null;
                }
            }

            @Override
            public K put(V k, K v) {
                K origValue = get(k);
                WeakBidiMap.this.put(v, k);
                return origValue;
            }

            @Override
            public K remove(Object o) {
                K key = get(o);
                if (key != null) {
                    WeakBidiMap.this.remove(key);
                }
                return key;
            }

            @Override
            public void putAll(Map<? extends V, ? extends K> map) {
                for (Entry<? extends V, ? extends K> entry : map.entrySet()) {
                    put(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public void clear() {
                WeakBidiMap.this.clear();
            }

            @Override
            public Set<V> keySet() {
                return (Set<V>) WeakBidiMap.this.values();
            }

            @Override
            public Collection<K> values() {
                return WeakBidiMap.this.keySet();
            }

            @Override
            public Set<Entry<V, K>> entrySet() {
                Set<Entry<V, K>> result = new HashSet<Entry<V, K>>();
                for (final Entry<K, V> e: WeakBidiMap.this.entrySet()) {
                    result.add(new Entry<V, K>() {

                        @Override
                        public V getKey() {
                            return e.getValue();
                        }

                        @Override
                        public K getValue() {
                            return e.getKey();
                        }

                        @Override
                        public K setValue(K v) {
                            throw new UnsupportedOperationException("Not supported.");
                        }
                    });
                }
                return result;
            }
        };
    }
}