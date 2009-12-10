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
package org.apache.clerezza.rdf.file.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;

import java.util.Iterator;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

/**
 *
 * @author mir
 */
public class FileMGraph extends SimpleMGraph {

	private Serializer serializer;
	private File file;
	private String fileType;

	FileMGraph(UriRef uri, Parser parser,
			Serializer serializer) {
		file = new File(URI.create(uri.getUnicodeString()));
		String fileEnding = extractFileEnding(uri);
		fileType = getMediaTypeForFileEnding(fileEnding);
		this.serializer = serializer;
		try {
			if (file.exists() && file.length() != 0) {
				InputStream fio = new FileInputStream(file);
				Graph graph = parser.parse(fio, fileType);
				addAllNoFileAccess(graph);
			} else {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean add(Triple e) {
		boolean result = super.add(e);
		writeToFile();
		return result;
	}
	
	@Override
	public boolean addAll(Collection<? extends Triple> c) {
		boolean modified = addAllNoFileAccess(c);
		writeToFile();
		return modified;
	}

	@Override
	public boolean remove(Object o) {
		Iterator<Triple> e = super.filter(null, null, null);
		while (e.hasNext()) {
			if (o.equals(e.next())) {
				e.remove();
				writeToFile();
				return true;
			}
		}		
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<Triple> e = super.filter(null, null, null);
		while (e.hasNext()) {
			if (c.contains(e.next())) {
				e.remove();
				modified = true;
			}			
		}
		writeToFile();
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = super.retainAll(c);
		writeToFile();
		return result;
	}
	
	@Override
	public Iterator<Triple> filter(final NonLiteral subject,
			final UriRef predicate,
			final Resource object) {
		final Iterator<Triple> baseIter = super.filter(subject, predicate, object);
		Iterator<Triple> iterator = new Iterator<Triple>() {

			@Override
			public boolean hasNext() {
				return baseIter.hasNext();
			}

			@Override
			public Triple next() {
				return baseIter.next();
			}

			@Override
			public void remove() {
				baseIter.remove();
				writeToFile();
			}

		};
		return iterator;
	}

	private boolean addAllNoFileAccess(Collection<? extends Triple> c) {
		boolean modified = false;
		Iterator<? extends Triple> e = c.iterator();
		while (e.hasNext()) {
			if (super.add(e.next())) {
				modified = true;
			}
		}
		return modified;
	}

	private String extractFileEnding(UriRef uri) {
		String uriString = uri.getUnicodeString();
		String fileEnding = uriString.substring(uriString.lastIndexOf(".") + 1, uriString.length());
		return fileEnding;
	}

	private String getMediaTypeForFileEnding(String fileEnding) {
		if (fileEnding.equals("rdf")) {
			return SupportedFormat.RDF_XML;
		}
		if (fileEnding.equals("nt")) {
			return SupportedFormat.N_TRIPLE;
		}
		if (fileEnding.equals("ttl")) {
			return SupportedFormat.TURTLE;
		}
		if (fileEnding.equals("n3")) {
			return SupportedFormat.N3;
		}
		return null;
	}

	private void writeToFile() {
		synchronized(this) {
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				serializer.serialize(out, this, fileType);
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			} finally {
				try {
					out.close();
				} catch (IOException ex) {
					new RuntimeException(ex);
				}
			}
		}
	}

	public void delete() {
		file.delete();
	}
}
