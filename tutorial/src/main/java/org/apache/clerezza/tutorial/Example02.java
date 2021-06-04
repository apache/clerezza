package org.apache.clerezza.tutorial;

import org.apache.clerezza.Graph;
import org.apache.clerezza.Triple;
import org.apache.clerezza.rdf.jena.parser.JenaParserProvider;
import org.apache.clerezza.representation.Parser;
import org.apache.clerezza.representation.SupportedFormat;
import org.apache.clerezza.representation.UnsupportedFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

public class Example02 {

    private static final Logger logger = LoggerFactory.getLogger( Example02.class );

    public static void main( String[] args ) {

        InputStream inputStream = Example02.class.getResourceAsStream( "example02.ttl" );
        Parser parser = Parser.getInstance();

        parser.bindParsingProvider( new JenaParserProvider() );
        try {
            Graph graph = parser.parse( inputStream, SupportedFormat.TURTLE );

            Iterator<Triple> iterator = graph.filter( null, null, null );
            Triple triple;

            while ( iterator.hasNext() ) {
                triple = iterator.next();
                logger.info( String.format( "%s %s %s",
                        triple.getSubject().toString(),
                        triple.getPredicate().toString(),
                        triple.getObject().toString()
                ) );
            }
        } catch ( UnsupportedFormatException ex ) {
            logger.warn( String.format( "%s is not supported by the used parser", SupportedFormat.TURTLE ) );
        }
    }
}