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

package org.apache.clerezza.representation;

import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.InputStream;

/**
 *
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class ParserTest {

    private static boolean providerAInvoked;
    private static boolean providerBInvoked;
    private ParsingProvider parsingProviderA = new ParsingProviderA();
    private ParsingProvider parsingProviderB = new ParsingProviderB();

    @Test
    public void registerOneProvider() {
        Parser parser = new Parser(null);
        parser.bindParsingProvider(parsingProviderA);
        providerAInvoked = false;
        parser.parse(null, "application/x-fantasy2+rdf");
        Assertions.assertTrue(providerAInvoked);
    }
    
    @Test
    public void registerAndUnregisterSecond() {
        Parser parser = new Parser(null);
        parser.bindParsingProvider(parsingProviderA);
        parser.bindParsingProvider(parsingProviderB);
        providerAInvoked = false;
        providerBInvoked = false;
        parser.parse(null, "application/x-fantasy2+rdf");
        Assertions.assertFalse(providerAInvoked);
        Assertions.assertTrue(providerBInvoked);
        providerAInvoked = false;
        providerBInvoked = false;
        parser.parse(null, "application/x-fantasy1+rdf");
        Assertions.assertTrue(providerAInvoked);
        Assertions.assertFalse(providerBInvoked);
        parser.unbindParsingProvider(parsingProviderB);
        providerAInvoked = false;
        providerBInvoked = false;
        parser.parse(null, "application/x-fantasy2+rdf");
        Assertions.assertTrue(providerAInvoked);
        Assertions.assertFalse(providerBInvoked);
        
    }

    @SupportedFormat({"application/x-fantasy1+rdf", "application/x-fantasy2+rdf"})
    static class ParsingProviderA implements ParsingProvider {

        @Override
        public void parse(Graph target, InputStream serializedGraph, String formatIdentifier, IRI baseUri) {
            providerAInvoked = true;
        }
    };
    @SupportedFormat("application/x-fantasy2+rdf")
    static class ParsingProviderB implements ParsingProvider {

        @Override
        public void parse(Graph target, InputStream serializedGraph, String formatIdentifier, IRI baseUri) {
            providerBInvoked = true;
        }
    };
}
