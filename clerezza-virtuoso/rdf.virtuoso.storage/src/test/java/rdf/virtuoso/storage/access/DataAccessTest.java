package rdf.virtuoso.storage.access;

import java.sql.SQLException;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rdf.virtuoso.storage.TestUtils;

public class DataAccessTest {

	private static DataAccess da = null;

	static Logger log = LoggerFactory.getLogger(DataAccessTest.class);
	
	@Before
	public void before() throws ClassNotFoundException, SQLException {
		da = TestUtils.getProvider().createDataAccess();
		da.clearGraph( "urn:x-test:DataAccessTest" );
	}

	@After
	public void after() {
		da.clearGraph( "urn:x-test:DataAccessTest" );
		da.close();
		da = null;
	}

	private void testTriple(Triple t){
		String g = "urn:x-test:DataAccessTest";
		da.insertQuad(g, t);
		
		Assert.assertTrue(da.filter(g, null, null, null).hasNext());

		Assert.assertTrue(da.filter(g, t.getSubject(), null, null).hasNext());
		Assert.assertTrue(da.filter(g, null, t.getPredicate(), null).hasNext());
		Assert.assertTrue(da.filter(g, null, null, t.getObject()).hasNext());
		
		Assert.assertTrue(da.filter(g, null, t.getPredicate(), t.getObject()).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), null, t.getObject()).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), t.getPredicate(), null).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), null, t.getObject()).hasNext());

		Assert.assertTrue(da.filter(g, t.getSubject(), t.getPredicate(), t.getObject()).hasNext());

		Assert.assertTrue(da.size(g) == 1);
		
		da.deleteQuad(g, t);
		
		Assert.assertTrue(da.size(g) == 0);
	}

	@Test
	public void test_Uri_Uri_Uri(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		testTriple(t);
	}

	@Test
	public void test_Uri_Uri_PlainLiteral(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new PlainLiteralImpl("Lorem Ipsum"));
		testTriple(t);
	}
	
	@Test
	public void test_Uri_Uri_BNode(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new BNode());
		testTriple(t);
	}
	
	@Test
	public void testRenew(){
		int i = 100;
		while(i>0){
			test_Uri_Uri_Uri();
			test_Uri_Uri_PlainLiteral();
			i--;
		}
		da.renew();
		i = 100;
		while(i>0){
			test_Uri_Uri_Uri();
			test_Uri_Uri_PlainLiteral();
			i--;
		}
		da.renew();
		i = 100;
		while(i>0){
			test_Uri_Uri_Uri();
			test_Uri_Uri_PlainLiteral();
			i--;
		}
	}

}
