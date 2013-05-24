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
package org.apache.clerezza.jaxrs.sparql.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageBodyWirter for <code>ResultSet</code>. Resulting output is csv and
 * conforms to:
 * http://www.w3.org/TR/2013/REC-sparql11-results-csv-tsv-20130321/#csv
 * 
 * Also see: http://tools.ietf.org/html/rfc4180
 * 
 * @author misl
 */
@Component
@Service( Object.class )
@Property( name = "javax.ws.rs", boolValue = true )
@Produces( { "text/csv" } )
@Provider
public class ResultSetCsvMessageBodyWriter implements MessageBodyWriter<ResultSet> {

  private static final Logger logger = LoggerFactory
      .getLogger( ResultSetCsvMessageBodyWriter.class );
  private static final String UTF_8 = "UTF-8";
  private static byte[] separator;

  static {
    try {
      separator = ",".getBytes( UTF_8 );
    } catch( UnsupportedEncodingException e ) {
      logger.error( "Developer error", e );
    }

  }

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType ) {
    return ResultSet.class.isAssignableFrom( type );
  }

  @Override
  public long getSize( ResultSet t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType ) {
    return -1;
  }

  @Override
  public void writeTo( ResultSet resultSet, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream ) throws IOException, WebApplicationException {

    // According to spec header is mandatory.
    writeCsvHeader( entityStream, resultSet.getResultVars() );
    while( resultSet.hasNext() ) {
      writeCsvLine( entityStream, resultSet.getResultVars(), resultSet.next() );
    }
  }

  /**
   * Write resultset header to the given output stream.
   * 
   * @param outputStream
   *          stream to write to.
   * @param headers
   *          the headers to write.
   * @throws IOException
   */
  private void writeCsvHeader( OutputStream outputStream, List<String> headers ) throws IOException {
    boolean first = true;
    for( String header : headers ) {
      if( !first ) {
        outputStream.write( separator );
      }
      writeEscaped( outputStream, header );
      first = false;
    }
    outputStream.write( "\n".getBytes( UTF_8 ) );
  }

  /**
   * Write a single csv line using the given line data.
   * 
   * @param outputStream
   *          stream to write to.
   * @param headers
   *          the headers to write line data for.
   * @param lineData
   *          the line data to write.
   * @throws IOException
   */
  private void writeCsvLine( OutputStream outputStream, List<String> headers,
      SolutionMapping lineData ) throws IOException {
    boolean first = true;
    for( String header : headers ) {
      if( !first ) {
        outputStream.write( separator );
      }
      Resource resource = lineData.get( header );
      if( resource != null ) {
        writeEscaped( outputStream, getResourceValue( resource ) );
      }
      first = false;
    }
    outputStream.write( "\n".getBytes( UTF_8 ) );
  }

  /**
   * Helper to get the proper string representation for the given Resource.
   */
  private String getResourceValue( Resource resource ) {
    StringBuilder value = new StringBuilder();
    if( resource instanceof UriRef ) {
      value.append( ((UriRef) resource).getUnicodeString() );
    } else if( resource instanceof TypedLiteral ) {
      value.append( ((TypedLiteral) resource).getLexicalForm() );
    } else if( resource instanceof PlainLiteral ) {
      value.append( ((PlainLiteral) resource).getLexicalForm() );
    } else if( resource instanceof BNode ) {
      value.append( "/" );
    } else {
      value.append( resource.toString() );
    }
    return value.toString();
  }

  /**
   * Write the given string to the output stream and escape the output where
   * necessary.
   * 
   * @param outputStream
   *          stream to write to.
   * @param text
   *          the text to write.
   * @throws IOException
   */
  private void writeEscaped( OutputStream outputStream, String text ) throws IOException {
    String line = text;
    if( text.contains( "\r" ) || text.contains( "\n" ) || text.contains( "," )
        || text.contains( "\"" ) ) {
      StringBuilder builder = new StringBuilder();
      builder.append( '"' );
      builder.append( text.replaceAll( "\"", "\"\"" ) );
      builder.append( '"' );
      line = builder.toString();
    }
    outputStream.write( line.getBytes( UTF_8 ) );
  }
}
