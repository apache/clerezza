/*
*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common Development
* and Distribution License("CDDL") (collectively, the "License"). You
* may not use this file except in compliance with the License. You can obtain
* a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
* or jersey/legal/LICENSE.txt. See the License for the specific
* language governing permissions and limitations under the License.
*
* When distributing the software, include this License Header Notice in each
* file and include the License file at jersey/legal/LICENSE.txt.
* Sun designates this particular file as subject to the "Classpath" exception
* as provided by Sun in the GPL Version 2 section of the License file that
* accompanied this code. If applicable, add the following below the License
* Header, with the fields enclosed by brackets [] replaced by your own
* identifying information: "Portions Copyrighted [year]
* [name of copyright owner]"
*
* Contributor(s):
*
* If you wish your version of this file to be governed by only the CDDL or
* only the GPL Version 2, indicate your decision by adding "[Contributor]
* elects to include this software in this distribution under the [CDDL or GPL
* Version 2] license." If you don't indicate a single choice of license, a
* recipient has the option to distribute your version of this file under
* either the CDDL, the GPL Version 2 or to extend the choice of license to
* its licensees as provided above. However, if you add GPL Version 2 code
* and therefore, elected the GPL Version 2 license, then the option applies
* only if the new code is made subject to such option by the copyright
* holder.
* 
* trialox.org (trialox AG, Switzerland) elects to include this software in this
* distribution under the CDDL license.
*/ 
package org.apache.clerezza.utils.imageprocessing.metadataprocessing;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
*
* @author Paul.Sandoz@Sun.Com
*/

public class MultivaluedMapImpl<K, V> extends HashMap<K, List<V>>
       implements MultivaluedMap<K, V> {

   // MultivaluedMap
	@Override
   public final void putSingle(K key, V value) {
       List<V> l = getList(key);

       l.clear();
       if (value != null) {
           l.add(value);
       } else {
           throw new NullPointerException("Adding nulls not (yet) supported");
       }
   }

	@Override
   public final void add(K key, V value) {
       List<V> l = getList(key);

       if (value != null) {
           l.add(value);
       } else {
           throw new NullPointerException("Adding nulls not (yet) supported");
       }
   }

	@Override
   public final V getFirst(K key) {
       List<V> values = get(key);
       if (values != null && values.size() > 0) {
           return values.get(0);
       } else {
           return null;
       }
   }
   // 
   public final void addFirst(K key, V value) {
       List<V> l = getList(key);

       if (value != null) {
           l.add(0, value);
       } else {
           throw new NullPointerException("Adding nulls not (yet) supported");
       }
   }

   public final <A> List<A> get(K key, Class<A> type) {
       Constructor<A> c = null;
       try {
           c = type.getConstructor(String.class);
       } catch (Exception ex) {
           throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
       }

       ArrayList<A> l = null;
       List<V> values = get(key);
       if (values != null) {
           l = new ArrayList<A>();
           for (V value : values) {
               try {
                   l.add(c.newInstance(value));
               } catch (Exception ex) {
                   l.add(null);
               }
           }
       }
       return l;
   }


   private final List<V> getList(K key) {
       List<V> l = get(key);
       if (l == null) {
           l = new LinkedList<V>();
           put(key, l);
       }
       return l;
   }

   public final <A> A getFirst(K key, Class<A> type) {
       V value = getFirst(key);
       if (value == null) {
           return null;
       }
       Constructor<A> c = null;
       try {
           c = type.getConstructor(String.class);
       } catch (Exception ex) {
           throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
       }
       A retVal = null;
       try {
           retVal = c.newInstance(value);
       } catch (Exception ex) {
       }
       return retVal;
   }

   @SuppressWarnings("unchecked")
   public final <A> A getFirst(K key, A defaultValue) {
       V value = getFirst(key);
       if (value == null) {
           return defaultValue;
       }
       Class<A> type = (Class<A>) defaultValue.getClass();

       Constructor<A> c = null;
       try {
           c = type.getConstructor(String.class);
       } catch (Exception ex) {
           throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
       }
       A retVal = defaultValue;
       try {
           retVal = c.newInstance(value);
       } catch (Exception ex) {
       }
       return retVal;
   }
}// $Log: $