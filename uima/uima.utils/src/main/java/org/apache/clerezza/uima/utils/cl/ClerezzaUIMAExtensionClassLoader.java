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
package org.apache.clerezza.uima.utils.cl;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * An extension {@link ClassLoader} to be used as UIMA extension CL within a
 * {@link org.apache.uima.resource.ResourceManager}. This delegates class loading to
 * multiple (underlying) classloaders
 */
public class ClerezzaUIMAExtensionClassLoader extends ClassLoader {

  private static final String CLASS = ".class";
  private static final char POINT = '.';
  private static final char SLASH = '/';

  private final Collection<ClassLoader> delegateClassLoaders;
  private final ResourceByteReader byteReader;

  public ClerezzaUIMAExtensionClassLoader(ClassLoader parent, Collection<ClassLoader> delegateClassLoaders) {
    super(parent);
    this.delegateClassLoaders = delegateClassLoaders;
    this.byteReader = new ResourceByteReader();
  }

  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String path = new StringBuilder(name.replace(POINT, SLASH)).append(CLASS).toString();
    URL url = findResource(path);
    if (url == null) {
      throw new ClassNotFoundException(name);
    }
    ByteBuffer byteCode;
    try {
      byteCode = byteReader.loadResource(url);
    } catch (IOException e) {
      throw new ClassNotFoundException(name, e);
    }
    return defineClass(name, byteCode, null);
  }

  protected URL findResource(String name) {
    URL resource = null;
    for (ClassLoader delegate : delegateClassLoaders) {
      resource = delegate.getResource(name);
      if (resource != null)
        break;
    }
    return resource;
  }

  protected Enumeration<URL> findResources(String name) throws IOException {
    Vector<URL> vector = new Vector<URL>();
    for (ClassLoader delegate : delegateClassLoaders) {
      Enumeration<URL> enumeration = delegate.getResources(name);
      while (enumeration.hasMoreElements()) {
        vector.add(enumeration.nextElement());
      }
    }
    return vector.elements();
  }

}