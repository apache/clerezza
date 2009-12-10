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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;

/**
 * Provides a resolve method that is used by {@link DefaultParser} to resolve
 * a keyword.
 * 
 * @author reto
 * 
 */
public interface KeywordResolver {

	/**
	 * Resolves a keyword using the passed <code>parameters</code>.
	 *
	 * It may use template code following the keyword, typically ending at a
	 * closingt tag. Any output it generates is written to <code>out</code>.
	 *
	 * When the method returns the parser will continue parsing at the next char
	 * in <code>in</code>
	 * 
	 * @param parser
	 *            the parser instance that might be used for callbacks, i.e. for
	 *            parsing nested template sections.
	 * @param parameters
	 *            the parameters for this keyword.
	 * @param in
	 *            the template input <code>Reader</code> at the position
	 *            immediately after the keyword.
	 * @param out
	 *            may be null, this means the Keyword is within a section that
	 *            will not be part of the result, the resolver may just skip
	 *            till the endmarker
	 * @param dataFieldResolver  the data structure used for fields.
	 * @param arrayPositioner
	 *            the current loop variables.
	 * 
	 * 
	 * @throws IOException
	 */
	public abstract void resolve(DefaultParser parser, String parameters,
			Reader in, Writer out, DataFieldResolver dataFieldResolver,
			int[] arrayPositioner) throws IOException;
}