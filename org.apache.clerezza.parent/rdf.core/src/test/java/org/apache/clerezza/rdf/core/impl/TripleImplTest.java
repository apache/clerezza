/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.rdf.core.impl;

import org.junit.Test;
import junit.framework.Assert;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
/**
 *
 * @author reto
 *
 */

public class TripleImplTest {
    
	
	@Test public void tripleEquality() {
		NonLiteral subject = new UriRef("http://example.org/");
		UriRef predicate = new UriRef("http://example.org/property");
		Resource object = new PlainLiteralImpl("property value");
		Triple triple1 = new TripleImpl(subject, predicate, object);
		Triple triple2 = new TripleImpl(subject, predicate, object);
		Assert.assertEquals(triple1.hashCode(), triple2.hashCode());
		Assert.assertEquals(triple1, triple2);	
	}

}
