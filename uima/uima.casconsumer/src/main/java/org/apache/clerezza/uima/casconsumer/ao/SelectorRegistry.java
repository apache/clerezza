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
package org.apache.clerezza.uima.casconsumer.ao;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.uima.ontologies.annotationontology.AO;

import java.util.HashMap;
import java.util.Iterator;

public class SelectorRegistry {
  public class Selector {
    public final UriRef uri;
    public final int start;
    public final int end;

    public Selector(UriRef uri, int start, int end) {
      this.uri = uri;
      this.start = start;
      this.end = end;
    }

  }

  private HashMap<String, Selector> registry;
  private int count;

  public SelectorRegistry() {
    registry = new HashMap<String, Selector>();
    count = 1;
  }

  public UriRef get(int start, int end) {
    String key = start + ":" + end;
    Selector sel = registry.get(key);
    if (sel == null) {
      UriRef uri = new UriRef(new StringBuilder(AO.Selector.getUnicodeString()).
        append("/").append(count++).toString());

      sel = new Selector(uri, start, end);
      registry.put(key, sel);
    }
    return sel.uri;
  }

  public Iterator<Selector> iterator() {
    return registry.values().iterator();
  }

  public int getSize() {
    return registry.size();
  }
}
