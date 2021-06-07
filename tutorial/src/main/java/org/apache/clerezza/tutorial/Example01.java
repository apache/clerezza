package org.apache.clerezza.tutorial;

import org.apache.clerezza.BlankNode;
import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.Triple;
import org.apache.clerezza.implementation.literal.PlainLiteralImpl;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;

import java.util.Iterator;

public class Example01 {

    public static void main( String[] args ) {
        BlankNode subject = new BlankNode();

        IRI isA = new IRI( "http://clerezza.apache.org/2017/01/example#isA" );
        IRI clerezzaUser = new IRI( "http://clerezza.apache.org/2017/01/example#ClerezzaUser" );

        IRI hasFirstName = new IRI( "http://clerezza.apache.org/2017/01/example#hasFirstName" );
        PlainLiteralImpl firstName = new PlainLiteralImpl( "Hasan" );

        TripleImpl subjectType = new TripleImpl( subject, isA, clerezzaUser );
        TripleImpl subjectFirstName = new TripleImpl( subject, hasFirstName, firstName );

        Graph myGraph = new SimpleGraph();
        myGraph.add( subjectType );
        myGraph.add( subjectFirstName );

        Iterator<Triple> iterator = myGraph.filter( null, null, null );
        Triple triple;
        while ( iterator.hasNext() ) {
            triple = iterator.next();
            System.out.println( triple.getSubject().toString() );
            System.out.println( triple.getPredicate().toString() );
            System.out.println( triple.getObject().toString() );
        }
    }

}