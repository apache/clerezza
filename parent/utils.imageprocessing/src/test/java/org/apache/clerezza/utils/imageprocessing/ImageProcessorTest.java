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
package org.apache.clerezza.utils.imageprocessing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author tio
 *
 */
public class ImageProcessorTest {

	private ImageProcessor processor;
	
	private BufferedImage bimg;
	@Before
	public void setUp() throws IOException {
		processor = new JavaGraphicsProvider();		
		bimg = ImageIO.read(new File(getClass().getResource("testimage.png").getPath()));
	}

	/**
	 * Tests if a copy of an image which was rotated 360 degrees is the same 
	 * as the original image
	 */
	@Test
	public void testRotateImage360() throws IOException {
		BufferedImage tempImg = processor.rotate(bimg, 360);

		Assert.assertTrue(bimg.getWidth() == tempImg.getWidth());
		Assert.assertTrue(bimg.getHeight() == tempImg.getHeight());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
		ImageIO.write(bimg, "png", baos);
		ImageIO.write(tempImg, "png", tempBaos);
		byte[] bytesOut = baos.toByteArray();
		baos.flush();
		byte[] tempBytesOut = tempBaos.toByteArray();
		tempBaos.flush();
		Assert.assertArrayEquals(bytesOut, tempBytesOut);
	}

	/**
	 * Tests if a a copy of an image which was rotated 180 degrees keeps
	 * the width and the height
	 */
	@Test
	public void testRotateImage180() throws IOException {
		BufferedImage tempImg = processor.rotate(bimg, 180);

		Assert.assertTrue(bimg.getWidth() == tempImg.getWidth());
		Assert.assertTrue(bimg.getHeight() == tempImg.getHeight());
	}

	/**
	 * Tests if the resulted thumbnail has the specified size.
	 * @throws java.io.IOException
	 */
	@Test
	public void testMakeAThumbnail() throws IOException {
		BufferedImage tempImg = processor.makeAThumbnail(bimg, 50, 70);
		Assert.assertTrue(50 == tempImg.getWidth()||70 == tempImg.getHeight());
		Assert.assertTrue(tempImg.getWidth() <= 50 && tempImg.getHeight() <= 70);
		bimg = ImageIO.read(new File(getClass().getResource("testimage1.jpg").getPath()));
		tempImg = processor.makeAThumbnail(bimg, 50, 70);
		Assert.assertTrue(50 == tempImg.getWidth()||70 == tempImg.getHeight());
		Assert.assertTrue(tempImg.getWidth() <= 50 && tempImg.getHeight() <= 70);
		bimg = ImageIO.read(new File(getClass().getResource("testimage2.jpg").getPath()));
		tempImg = processor.makeAThumbnail(bimg, 50, 70);
		Assert.assertTrue(50 == tempImg.getWidth()||70 == tempImg.getHeight());
		Assert.assertTrue(tempImg.getWidth() <= 50 && tempImg.getHeight() <= 70);
	}
}
