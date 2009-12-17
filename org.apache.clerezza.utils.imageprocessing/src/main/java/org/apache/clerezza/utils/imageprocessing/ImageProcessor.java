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

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Provides methods to manipulate image data via 
 * <code>java.awt.image.BufferedImage</code>. BufferedImage is an accessible
 * buffer of image data (pixels and their RGB colors).
 * 
 * @author tio, hasan, mir
 */
public abstract class ImageProcessor {

	/**
	 * Makes an image translucent. The parameter translucency has to be in the 
	 * range 0.0 - 1.0
	 * 
	 * @param image
	 * @param translucency
	 */
	public abstract BufferedImage makeImageTranslucent(BufferedImage image,
			float translucency);

	/**
	 * Makes a <code>Color</code> of an image transparent
	 * 
	 * @param image
	 * @param color
	 */
	public abstract BufferedImage makeColorTransparent(BufferedImage image, Color color);

	/**
	 * Flips an image
	 * 
	 * @param image
	 * @param direction
	 *		0 means horizontal
	 *		1 means vertical
	 */
	public abstract BufferedImage flip(BufferedImage image, int direction);

	/**
	 * Rotates an image by an angle defined in degrees.
	 * If the angle is negative, the image will be rotated counter-clockwise.
	 * Otherwise, it will be rotated clockwise.
	 * 
	 * @param image
	 * @param angle
	 */
	public abstract BufferedImage rotate(BufferedImage image, int angle);

	/**
	 * Resizes an image
	 * 
	 * @param image
	 * @param newWidth
	 * @param newHeight
	 */
	public abstract BufferedImage resize(BufferedImage image, int newWidth, int newHeight);

	/**
	 * Resizes an image proportionally. 
	 * The proportion is determined by the newWidth if it is not 0, irrespective
	 * of the newHeight.
	 * If the newWidth is 0, the proportion is determined by the newHeight if
	 * it is not 0.
	 * If both newWidth and newHeight are 0, no resize takes place.
	 * 
	 * @param image
	 * @param newWidth
	 * @param newHeight
	 */
	public abstract BufferedImage resizeProportional(BufferedImage image, int newWidth,
			int newHeight);

	/**
	 * Resizes an image relatively to the original size. 
	 * Resize factors take the value in the range [0.0 .. 1.0]
	 * 
	 * @param image
	 * @param resizeFactorWidth
	 * @param resizeFactorHeight
	 */
	public abstract BufferedImage resizeRelative(BufferedImage image,
			float resizeFactorWidth, float resizeFactorHeight);

	/**
	 * Resizes an image proportionally and relative to the original size.
	 * Resize factor takes the value in the range [0.0 .. 1.0]
	 * 
	 * @param image
	 * @param resizeFactor
	 */
	public abstract BufferedImage resizeRelativeProportional(BufferedImage image,
			float resizeFactor);

	/**
	 * Create a thumbnail of the image. The image is scaled down so that height
	 * and width are smaller or equals to <code>height</code> and
	 * <code>width</code> respectively. If image is already smaller or equals
	 * <code>width</code> x <code>height</code> image is returned without
	 * transformation.
	 * 
	 * @param image The image from which the thumbnails is derived.
	 * @param width The maximum width of the thumbnail
	 * @param height The maximum height of the thumbnail
	 */
	public BufferedImage makeAThumbnail(BufferedImage image, int width, int height) {
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		if (imgWidth <= width && imgHeight <= height) {
			return image;
		}
		float propWidth = (float) width / imgWidth;
		float propHeight = (float) height / imgHeight;
		float factor = (propWidth < propHeight) ? propWidth : propHeight;
		BufferedImage img = resizeRelativeProportional(image, factor);
		return img;
	}
}
