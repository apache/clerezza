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
package org.apache.clerezza.utils.imagemagick;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.ExifTagDataSet;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.IptcDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hasan, daniel
 */
public class ImageMagickUtilsTest {

	private static boolean correctlyInstalled = true;
	private final static Logger logger = LoggerFactory.getLogger(ImageMagickUtilsTest.class);

	@BeforeClass
	public static void checkIfImageMagickInstalled() {
		try {
			new ImageMagickProvider().checkImageMagickInstallation();
		} catch (RuntimeException ex) {
			logger.warn("No valid imagemagick installation found, skipping tests.");
			correctlyInstalled = false;
		}
	}

	@Test
	public void TestFlip() throws IOException {
		Assume.assumeTrue(correctlyInstalled);
		ImageMagickProvider ip = new ImageMagickProvider();

		InputStream in = getClass().getResourceAsStream("test.png");
		BufferedImage bimg = ip.flip(ImageIO.read(in), 0);
		assert(bimg != null);
	}
	
	@Test
	public void extractMetaDataTest() throws IOException {
		Assume.assumeTrue(correctlyInstalled);

		ImageMagickProvider ip = new ImageMagickProvider();

		InputStream in = getClass().getResourceAsStream("metadata.jpg");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int ch;
		while((ch = in.read()) > -1) {
			baos.write(ch);
		}

		byte[] img = baos.toByteArray();

		Assert.assertTrue(ip.extractIPTC(img).get("2:80").
				contains(new IptcDataSet(2, 80, ".Joe O'Shaughnessy")));
		Assert.assertTrue(ip.extractEXIF(img).get(String.valueOf(ExifTagDataSet.Make)).
				contains(new ExifTagDataSet(ExifTagDataSet.Make, "Canon")));
		TripleCollection tc = ip.extractXMP(img);
		Iterator<Triple> it = tc.filter(null, DC.creator, null);
		Assert.assertTrue(tc.contains(
				new TripleImpl(
						(NonLiteral) it.next().getObject(),
						new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1"),
						new PlainLiteralImpl(".Joe O'Shaughnessy"))));
	}

	@Test
	public void writeXMPTest() throws IOException {
		Assume.assumeTrue(correctlyInstalled);

		ImageMagickProvider ip = new ImageMagickProvider();

		InputStream in = getClass().getResourceAsStream("metadata.jpg");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int ch;
		while((ch = in.read()) > -1) {
			baos.write(ch);
		}

		byte[] img = baos.toByteArray();

		TripleCollection tc = ip.extractXMP(img);

		InputStream in2 = getClass().getResourceAsStream("no-metadata.jpg");
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

		while((ch = in2.read()) > -1) {
			baos2.write(ch);
		}

		byte[] img2 = baos2.toByteArray();

		byte[] fileWithMetaData = ip.writeXMP(img2, tc);

		TripleCollection tc2 = ip.extractXMP(fileWithMetaData);
		Iterator<Triple> it = tc2.filter(null, DC.creator, null);
		Assert.assertTrue(tc2.contains(
				new TripleImpl(
						(NonLiteral) it.next().getObject(),
						new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1"),
						new PlainLiteralImpl(".Joe O'Shaughnessy"))));
	}
}
