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
package org.apache.clerezza.rdf.core.impl.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleImmutableGraph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.event.FilterTriple;
import org.apache.clerezza.commons.rdf.event.GraphListener;

/**
 * Calls the methods of the wrapped <code>Graph</code> as privileged
 * code, because they may need permissions like writing to disk or accessing       
 * network.
 *
 * @author mir
 */
public class PrivilegedGraphWrapper implements Graph {

    private Graph graph;

    public PrivilegedGraphWrapper(Graph Graph) {
        this.graph = Graph;
    }

    @Override
    public Iterator<Triple> filter(final BlankNodeOrIRI subject, final IRI predicate,
            final RDFTerm object) {
        return AccessController.doPrivileged(new PrivilegedAction<Iterator<Triple>>() {

            @Override
            public Iterator<Triple> run() {
                return graph.filter(subject, predicate, object);
            }
        });
    }

    @Override
    public int size() {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>() {

            @Override
            public Integer run() {
                return graph.size();
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.isEmpty();
            }
        });
    }

    @Override
    public boolean contains(final Object o) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.contains(o);
            }
        });
    }

    @Override
    public Iterator<Triple> iterator() {
        return AccessController.doPrivileged(new PrivilegedAction<Iterator<Triple>>() {

            @Override
            public Iterator<Triple> run() {
                return graph.iterator();
            }
        });
    }

    @Override
    public Object[] toArray() {
        return AccessController.doPrivileged(new PrivilegedAction<Object[]>() {

            @Override
            public Object[] run() {
                return graph.toArray();
            }
        });
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return AccessController.doPrivileged(new PrivilegedAction<T[]>() {

            @Override
            public T[] run() {
                return graph.toArray(a);
            }
        });
    }

    @Override
    public boolean add(final Triple triple) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.add(triple);
            }
        });
    }

    @Override
    public boolean remove(final Object o) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.remove(o);
            }
        });
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.containsAll(c);
            }
        });
    }

    @Override
    public boolean addAll(final Collection<? extends Triple> c) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.addAll(c);
            }
        });
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.removeAll(c);
            }
        });
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return graph.retainAll(c);
            }
        });
    }

    @Override
    public void clear() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                graph.clear();
                return null;
            }
        });
    }

    @Override
    public ReadWriteLock getLock() {
        return graph.getLock();
    }

    private static class PriviledgedTripleIterator implements Iterator<Triple> {

        private final Iterator<Triple> wrappedIterator;

        public PriviledgedTripleIterator(Iterator<Triple> wrappedIterator) {
            this.wrappedIterator = wrappedIterator;
        }

        @Override
        public boolean hasNext() {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

                @Override
                public Boolean run() {
                    return wrappedIterator.hasNext();
                }
            });
        }

        @Override
        public Triple next() {
            return AccessController.doPrivileged(new PrivilegedAction<Triple>() {

                @Override
                public Triple run() {
                    return wrappedIterator.next();
                }
            });
        }

        @Override
        public void remove() {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                @Override
                public Object run() {
                    wrappedIterator.remove();
                    return null;
                }
            });
        }
    }
    
    

    @Override
    public ImmutableGraph getImmutableGraph() {
        return new SimpleImmutableGraph(this);
    }
}
