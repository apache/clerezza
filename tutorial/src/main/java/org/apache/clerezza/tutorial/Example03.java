package org.apache.clerezza.tutorial;

import org.apache.clerezza.Graph;
import org.apache.clerezza.representation.Parser;
import org.apache.clerezza.representation.Serializer;
import org.apache.clerezza.representation.SupportedFormat;
import org.apache.clerezza.representation.UnsupportedFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Example03 {

    private static final Logger logger = LoggerFactory.getLogger( Example03.class );

    public static void main( String[] args ) {
        InputStream inputStream = Example03.class.getResourceAsStream( "example03.ttl" );
        Parser parser = Parser.getInstance();

        Graph graph;
        try {
            graph = parser.parse( inputStream, SupportedFormat.TURTLE );
        } catch ( UnsupportedFormatException ex ) {
            logger.warn( String.format( "%s is not supported by the used parser", SupportedFormat.TURTLE ) );
            return;
        }

        Serializer serializer = Serializer.getInstance();
        try {
            FileOutputStream outputStream = new FileOutputStream( "/tmp/example03.rdf" );
            serializer.serialize( outputStream, graph, SupportedFormat.RDF_XML );
        } catch ( FileNotFoundException ex ) {
            logger.warn( ex.getMessage() );
        } catch ( UnsupportedFormatException ex ) {
            logger.warn( String.format( "%s is not supported by the used serializer", SupportedFormat.RDF_XML ) );
        }
    }

}