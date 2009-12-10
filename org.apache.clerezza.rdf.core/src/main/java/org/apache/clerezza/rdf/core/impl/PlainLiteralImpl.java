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
package org.apache.clerezza.rdf.core.impl;

import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.PlainLiteral;

/**
 *
 * @author reto
 */
public class PlainLiteralImpl implements PlainLiteral {

	private String lexicalForm;
	private Language language = null;

	public PlainLiteralImpl(String value) {
		if (value == null) {
			throw new IllegalArgumentException("The literal string cannot be null");
		}
		this.lexicalForm = value;
	}

	public PlainLiteralImpl(String value, Language language) {
		if (value == null) {
			throw new IllegalArgumentException("The literal string cannot be null");
		}
		this.lexicalForm = value;
		this.language = language;
	}

	@Override
	public String getLexicalForm() {
		return lexicalForm;
	}

	@Override
	public boolean equals(Object otherObj) {
		if (!(otherObj instanceof PlainLiteral)) {
			return false;
		}
		PlainLiteral other = (PlainLiteral) otherObj;
		if (!lexicalForm.equals(other.getLexicalForm())) {
			return false;
		}
		if (language != null) {
			return language.equals(other.getLanguage());
		}
		if (other.getLanguage() != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = lexicalForm.hashCode();
		if (language != null) {
			hash += language.hashCode();
		}
		return hash;
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append('\"').append(lexicalForm).append('\"');
		if (language != null) {
			result.append("@").append(language.toString());
		}
		return result.toString();
	}
}
