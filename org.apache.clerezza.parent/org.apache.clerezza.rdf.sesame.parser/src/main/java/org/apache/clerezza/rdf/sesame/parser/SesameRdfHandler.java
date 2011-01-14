/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.clerezza.rdf.sesame.parser;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author hasan
 */
public class SesameRdfHandler implements RDFHandler {

	final TripleCollection tc;
	private SesameScbConverter sesameScbConverter;

	public SesameRdfHandler(TripleCollection tc) {
		this.tc = tc;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		sesameScbConverter = new SesameScbConverter();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		sesameScbConverter = null;
	}

	@Override
	public void handleNamespace(String string, String string1) throws RDFHandlerException {
	}

	@Override
	public void handleStatement(Statement stmnt) throws RDFHandlerException {
		tc.add(sesameScbConverter.createTriple(stmnt));
	}

	@Override
	public void handleComment(String string) throws RDFHandlerException {
	}
}
