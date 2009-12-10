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
package org.apache.clerezza.platform.documentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Replaces the <code>BASE_URI_PLACEHOLDER</code> in the <code>UriRef</code>s 
 * as well as in the XML Literal of the <code>Triple</code>s provided by the
 * specified <code>Iterator</code>s with the specified base URI.
 *
 * @author mir, hasan
 */
public class UriMutatorIterator implements Iterator<Triple> {

	public static final Logger logger = LoggerFactory.getLogger(UriMutatorIterator.class);

	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public static final UriRef XML_LITERAL =
			new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

	public static final String BASE_URI_PLACEHOLDER = "bundle://";
	private Iterator<Triple> wrapped;
	private String baseUri;
	/*
	 * The symbolic name of the bundle from which the documentation origins.
	 */
	private String originBundleSymbName;

	UriMutatorIterator(Iterator<Triple> wrapped, String baseUri,
			String bundleSymbolicName) {
		this.wrapped = wrapped;
		this.baseUri = baseUri;
		this.originBundleSymbName = bundleSymbolicName;
	}

	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	@Override
	public Triple next() {
		Triple triple = wrapped.next();

		NonLiteral subject = triple.getSubject();
		if (subject instanceof UriRef) {
			subject = replacePlaceHolder((UriRef) subject);
		}
		UriRef predicate = replacePlaceHolder(triple.getPredicate());

		Resource object = triple.getObject();
		if (object instanceof UriRef) {
			object = replacePlaceHolder((UriRef) object);
		} else if (object instanceof TypedLiteral) {
			TypedLiteral literal = (TypedLiteral) object;
			if (literal.getDataType().equals(XML_LITERAL)) {
				object = replacePlaceHolderInUrl(literal);
			}
		}
		return new TripleImpl(subject, predicate, object);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported.");
	}

	private UriRef replacePlaceHolder(UriRef uriRef) {
		String orig = uriRef.getUnicodeString();
		if (orig.startsWith(BASE_URI_PLACEHOLDER)) {
			int nextSlash = orig.indexOf("/", BASE_URI_PLACEHOLDER.length());
			String bundleSymbolicName;
			if (nextSlash != BASE_URI_PLACEHOLDER.length()) {
				bundleSymbolicName = orig.subSequence(
						BASE_URI_PLACEHOLDER.length(), nextSlash).toString();
			} else {
				bundleSymbolicName = originBundleSymbName;
			}
			return new UriRef(baseUri + "bundle-doc/" + bundleSymbolicName +
					orig.substring(nextSlash));
		}
		return uriRef;
	}

	private TypedLiteral replacePlaceHolderInUrl(TypedLiteral xmlLiteral) {
		final String tagName = "infoBit";
		final String openingTag = "<" + tagName + ">";
		final String closingTag = "</" + tagName + ">";
		String text = openingTag + xmlLiteral.getLexicalForm() + closingTag;

		InputStream styleSheet = getClass().getResourceAsStream("baseUriTransformation.xsl");
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		StreamSource styleSource = new StreamSource(styleSheet);
		try {
			Transformer transformer = transformerFactory.newTransformer(styleSource);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
			Source source = new DOMSource(document);

			ByteArrayOutputStream baos = new ByteArrayOutputStream(text.length());
			Result result = new StreamResult(baos);

			transformer.setParameter("baseUri", baseUri);
			transformer.setParameter("originBundleSymbolicName", originBundleSymbName);
			transformer.transform(source, result);

			String resultedText = baos.toString("UTF-8");
			int startIdx = resultedText.indexOf(openingTag) + openingTag.length();
			int endIdx = resultedText.lastIndexOf(closingTag);
			return new TypedLiteralImpl(resultedText.substring(startIdx, endIdx),
					XML_LITERAL);
		} catch (SAXException se) {
			logger.warn("SAXException {} while transforming xml literal: {}",
					se.getMessage(), text);
		} catch (ParserConfigurationException pce) {
			logger.warn("ParserConfigurationException {} while transforming xml literal: {}",
					pce.getMessage(), text);
		} catch (TransformerException te) {
			logger.warn("TransformerException {} while transforming xml literal: {}",
					te.getMessage(), text);
		} catch (UnsupportedEncodingException uee) {
			logger.warn("UnsupportedEncodingException {} while transforming xml literal: {}",
					uee.getMessage(), text);
		} catch (IOException ioe) {
			logger.warn("IOException {} while transforming xml literal: {}",
					ioe.getMessage(), text);
		}
		return xmlLiteral;
	}
}
