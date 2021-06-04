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
package org.apache.clerezza.implementation.literal;

import org.apache.clerezza.Literal;
import org.apache.clerezza.IRI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class TypedLiteralImplTest {

    @Test
    public void typedLiteralEquality() {
        String stringValue = "some text";
        IRI uriRef = new IRI("http://example.org/datatypes/magic");
        Literal literal1 = new TypedLiteralImpl(stringValue, uriRef);
        Literal literal2 = new TypedLiteralImpl(stringValue, uriRef);
        Assertions.assertEquals(literal1, literal2);
        Assertions.assertEquals(literal1.hashCode(), literal2.hashCode());
        Literal literal3 = new TypedLiteralImpl("something else", uriRef);
        Assertions.assertFalse(literal1.equals(literal3));
        IRI uriRef2 = new IRI("http://example.org/datatypes/other");
        Literal literal4 = new TypedLiteralImpl(stringValue, uriRef2);
        Assertions.assertFalse(literal1.equals(literal4));
    }

    /**
     * The hascode is equals to the hascode of the lexical form plus the hashcode of the dataTyp
     */
    @Test
    public void checkHashCode() {
        String stringValue = "some text";
        IRI uriRef = new IRI("http://example.org/datatypes/magic");
        Literal literal = new TypedLiteralImpl(stringValue, uriRef);
        Assertions.assertEquals(stringValue.hashCode() + uriRef.hashCode(), literal.hashCode());
    }

}
