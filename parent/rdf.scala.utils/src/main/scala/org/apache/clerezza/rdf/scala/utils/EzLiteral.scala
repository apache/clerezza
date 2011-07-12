/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.clerezza.rdf.scala.utils

import org.apache.clerezza.rdf.core.impl.{TypedLiteralImpl, PlainLiteralImpl}
import org.apache.clerezza.rdf.ontologies.XSD
import org.apache.clerezza.rdf.core.{TypedLiteral, Language, PlainLiteral, UriRef}

object EzLiteral extends EzLiteralImplicits

trait EzLiteralImplicits {

	implicit def string2lit(str: String) = new EzLiteral(str)

}

/**
 * An EzLiteral is a typed string literal - ie, just a String literal - that comes with N3/turtle like methods
 * to map to other types of literals.
 *
 * This makes it useful when combined with the EzLiteralImplicit for writing out literals
 *
 * <code>
 *    "ABCDEFGHIJKLMN"                   -- a plain string converted to a EzLiteral
 *    "some text in english".lang(en)    -- an english literal
 *    "1234"^^XSD.int                    -- a number
 * </code>
 *
 * @author bblfish
 */
class EzLiteral(string: String) extends TypedLiteralImpl(string,XSD.string) {

	/**
	 * @return a plain literal with language specified by lang
	 */
	def lang(lng: String): PlainLiteral = lang(new Language(lng))

	/**
	 * @return a plain literal with language specified by lang
	 */
	def lang(lng: Language): PlainLiteral = new PlainLiteralImpl(string, lng)

	/**
	 * Map to a Typed Literal of given type
	 */
	def ^^(typ: UriRef): TypedLiteral = new TypedLiteralImpl(string, typ)

	/**
	 * Map to a URI of given lexical form
	 */
	def uri = new UriRef(string)

}
