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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotate {@link ParsingProvider}s to indicate
 * the format(s) they support.
 *
 * @author reto
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedFormat {

    /**
     * used as a key for the OSGi service property
     */
    public static final String supportedFormat = "supportedFormat";
    public static final String RDF_XML = "application/rdf+xml";
    public static final String TURTLE = "text/turtle";
    public static final String X_TURTLE = "application/x-turtle";
    public static final String N_TRIPLE = "application/n-triples";
    /**
     * @deprecated The mime-type for N Triples is "application/n-triples": http://www.w3.org/TR/n-triples/#sec-mediaReg-n-triples
     */
    @Deprecated
    public static final String TEXT_RDF_NT = "text/rdf+nt";
    public static final String N3 = "text/rdf+n3";
    public static final String RDF_JSON = "application/rdf+json";
    //both html and xhtml can be rdf formats with RDFa
    public static final String XHTML = "application/xhtml+xml";
    public static final String HTML = "text/html";

    /**
     * A list of format Identifiers (typically MIME-types) types without
     * parameter (without ';'-character).
     * E.g. {"application/rdf+xml","application/turtle"}
     */
    String[] value();
}
