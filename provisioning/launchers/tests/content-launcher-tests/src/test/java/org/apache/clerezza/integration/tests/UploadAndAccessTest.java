/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.integration.tests;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.containsString;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class UploadAndAccessTest extends BaseTest {
    
    String smallGraphTurtle = null;
    ImmutableGraph smallGraph;
    
    protected String getRdfFormat() {
        return "text/turtle";
        
    }
    
    @Test
    public void uploadAndQuery() throws Exception {
        smallGraphTurtle = "<"+RestAssured.baseURI+"/test-resource> <http://www.w3.org/2000/01/rdf-schema#comment> \"A test resource\"^^<http://www.w3.org/2001/XMLSchema#string>.";
        smallGraphTurtle += "<"+RestAssured.baseURI+"/test-resource> <http://www.w3.org/2000/01/rdf-schema#comment> \"Another comment\"@en.";
        smallGraph = Parser.getInstance().parse(new ByteArrayInputStream(smallGraphTurtle.getBytes("utf-8")), "text/turtle");
        uploadRDF();
        sparqlAsk();
        dereferenceResource();
    }
    
    protected byte[] getSerializedRdfToUpload() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer.getInstance().serialize(baos, smallGraph, getRdfFormat());
        return baos.toByteArray();
    }
    
    protected void uploadRDF() { 
        RestAssured.given().header("Accept", "text/html")
                .auth().basic("admin", "admin")
                .formParam("name", Constants.CONTENT_GRAPH_URI_STRING)
                .multiPart("graph", "test.ttl", getSerializedRdfToUpload(), getRdfFormat())
                .formParam("append", "Append")
                .expect().statusCode(HttpStatus.SC_NO_CONTENT).when()
                .post("/graph");
    }
    

    protected void sparqlAsk() {
        final String sparqlQuery = "ASK {"+smallGraphTurtle+"}";
        Response response = RestAssured.given().header("Accept", "application/sparql-results+xml")
                .auth().basic("admin", "admin")
                .formParam("query", sparqlQuery)
                .formParam("default-graph-uri", Constants.CONTENT_GRAPH_URI_STRING)
                .expect()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml")
                .when()
                .post("/sparql");
        response.then().assertThat().body(containsString("true"));
    }
    
    protected void dereferenceResource() {
        Response response = RestAssured.given().header("Accept", getRdfFormat())
                .expect()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, getRdfFormat())
                .when()
                .get("/test-resource");
        ImmutableGraph returnedGraph = Parser.getInstance().parse(response.getBody().asInputStream(), getRdfFormat());
        Assert.assertEquals("Returned Graph has wrong size", 2, returnedGraph.size());
        Assert.assertEquals(smallGraph, returnedGraph);
    }
    
}
