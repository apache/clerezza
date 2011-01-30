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
package org.apache.clerezza.utils.imageprocessing.metadataprocessing;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Unit tests for the MetaDataUtils
 * 
 * @author daniel
 *
 */
public class MetaDataConversionTest {
	
	@Test
	public void testIptcToXmp() {
		MetaData<IptcDataSet> metaData = new MetaData<IptcDataSet>();
		metaData.add(new IptcDataSet(IptcDataSet.KEYWORDS, "keyword1"));
		metaData.add(new IptcDataSet(IptcDataSet.KEYWORDS, "keyword2"));
		metaData.add(new IptcDataSet(IptcDataSet.KEYWORDS, "keyword3"));
		metaData.add(new IptcDataSet(IptcDataSet.CITY, "City"));
		
		TripleCollection tc = MetaDataUtils.convertIptcToXmp(metaData);

		Iterator<Triple> it = tc.filter(null, new UriRef(DC.subject.getURI()), null);
		it = tc.filter((NonLiteral) it.next().getObject(), null, null);
		while(it.hasNext()) {
			Triple triple = it.next();
			Assert.assertTrue(
					triple.getObject().toString().contains("keyword1") ||
					triple.getObject().toString().contains("keyword2") ||
					triple.getObject().toString().contains("keyword3") ||
					triple.getObject().toString().contains(RDF.Bag.getURI()));
		}

		Assert.assertTrue(tc.filter(null,
				new UriRef("http://ns.adobe.com/photoshop/1.0/City"), 
				new PlainLiteralImpl("City")).hasNext());
	}
	
	@Test
	public void testExifToXmp() {
		
		MetaData<ExifTagDataSet> metaData2 = new MetaData<ExifTagDataSet>();
		metaData2.add(new ExifTagDataSet(ExifTagDataSet.UserComment, "Bla Bla Bla"));
		metaData2.add(new ExifTagDataSet(ExifTagDataSet.Artist, "Hans Wurst"));
		
		TripleCollection tc = MetaDataUtils.convertExifToXmp(metaData2);
		
		Iterator<Triple> it = tc.filter(null, new UriRef("http://ns.adobe.com/exif/1.0/UserComment"), null);
		it = tc.filter((NonLiteral) it.next().getObject(), null, null);
		while(it.hasNext()) {
			Triple triple = it.next();
			Assert.assertTrue(
					triple.getObject().toString().contains("Bla Bla Bla") ||
					triple.getObject().toString().contains(RDF.Alt.getURI()));
		}
		
		Assert.assertTrue(tc.filter(null,
				new UriRef("http://ns.adobe.com/tiff/1.0/Artist"), 
				new PlainLiteralImpl("Hans Wurst")).hasNext());
		
	}
}
