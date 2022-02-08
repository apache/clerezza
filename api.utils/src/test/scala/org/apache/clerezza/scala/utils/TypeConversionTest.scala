/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.scala.utils

import org.apache.clerezza.implementation.TripleImpl
import org.apache.clerezza.implementation.literal.{LiteralFactory, PlainLiteralImpl, TypedLiteralImpl}
import org.apache.clerezza.{IRI, Language}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(classOf[JUnitPlatform])
class TypeConversionTest {

    private val literalFactory = LiteralFactory.getInstance()

    import Preamble._

    @Test
    def useStringAsObject {
        val t = new TripleImpl(new IRI(("http://example.org/subject")), new IRI(("http://example.org/predicate")), "a value")
        assertEquals(literalFactory.createTypedLiteral("a value"), t.getObject)
    }

    /*@Test
    def useStringWithLanguageTag {
      val t = new TripleImpl(new IRI(("http://example.org/subject")), new IRI(("http://example.org/predicate")), "a value"("en"))
      Assert.assertEquals(new PlainLiteralImpl("a value", new Language("en")), t.getObject)
    }*/

    @Test
    def useStringWithLanguageTag {
        val lit = new PlainLiteralImpl("a value", new Language("en"))
        val t = new TripleImpl(new IRI(("http://example.org/subject")), new IRI(("http://example.org/predicate")), "a value" lang "en")
        assertEquals(lit, t.getObject)
    }

    @Test
    def useStringWithType {
        val typeUri = new IRI("http://example.org/dt")
        val t = new TripleImpl(new IRI(("http://example.org/subject")), new IRI(("http://example.org/predicate")), "a value" ^^ typeUri)
        assertEquals(new TypedLiteralImpl("a value", typeUri), t.getObject)
    }

    @Test
    def literaToString {
        val lit = literalFactory.createTypedLiteral("a value")
        val s: String = lit
        assertEquals("a value", s)
    }

    @Test
    def dotUri {
        val t = new TripleImpl(new IRI(("http://example.org/subject")), new IRI(("http://example.org/predicate")), "http://example.org".iri)
        assertEquals(new IRI("http://example.org"), t.getObject)
    }

}
