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
package org.apache.clerezza.commons.rdf.impl.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.rdf.core.serializedform.Parser;

/**
 *
 * @author developer
 */
public class SparqlClient {

    final String endpoint;

    public SparqlClient(final String endpoint) {
        this.endpoint = endpoint;
    }

    public List<Map<String, RDFTerm>> queryResultSet(final String query) throws IOException {
        return (List<Map<String, RDFTerm>>) queryResult(query);
    }
    
    public Object queryResult(final String query) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("query", query));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response2 = httpclient.execute(httpPost);
        HttpEntity entity2 = response2.getEntity();
        try {
            InputStream in = entity2.getContent();
            final String mediaType = entity2.getContentType().getValue();
            if (mediaType.startsWith("application/sparql-results+xml")) {
                return SparqlResultParser.parse(in);
            } else {
                //assuming RDF response
                //FIXME clerezza-core-rdf to clerezza dependency
                Parser parser = Parser.getInstance();
                return parser.parse(in, mediaType);
            }
        }  finally {
            EntityUtils.consume(entity2);
            response2.close();
        }

    }

    

}
