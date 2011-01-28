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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PackedColorModel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <code>ImageProcessor</code> implementation based on the Java Abstract
 * Windowing Toolkit.
 * 
 * @author tio, hasan
 */
@Component
@Service(ImageProcessor.class)
public class JavaGraphicsProvider extends ImageProcessor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public BufferedImage makeImageTranslucent(BufferedImage image,
			float translucency) {
		BufferedImage img = new BufferedImage(image.getWidth(), 
				image.getHeight(), BufferedImage.TRANSLUCENT);
		Graphics2D g2D = img.createGraphics();
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				translucency));
		g2D.drawImage(image, null, 0, 0);
		g2D.dispose();
		return img;
	}

	@Override
	public BufferedImage makeColorTransparent(BufferedImage image, Color color) {
		BufferedImage img = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		g2D.setComposite(AlphaComposite.Src);
		g2D.drawImage(image, null, 0, 0);
		g2D.dispose();
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				if (img.getRGB(j, i) == color.getRGB()) {
					img.setRGB(j, i, 0x8F1C1C);
				}
			}
		}
		return img;
	}

	@Override
	public BufferedImage flip(BufferedImage image, int direction) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage img = new BufferedImage(width, height, getType(image));
		Graphics2D g2D = img.createGraphics();
		if (direction == 0) {
			g2D.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
		} else {
			g2D.drawImage(image, 0, 0, width, height, 0, height, width, 0, null);
		}
		g2D.dispose();
		return img;
	}

	private int getType(BufferedImage image) {
		if (image.getType() != BufferedImage.TYPE_CUSTOM) {
			return image.getType();
		}
		ColorModel imageColorModel = image.getColorModel();
		Class<?> imageColorModelClass = imageColorModel.getClass();
		int imageColorSpaceType = imageColorModel.getColorSpace().getType();
		switch (image.getSampleModel().getDataType()) {
			case DataBuffer.TYPE_BYTE:
				switch (imageColorModel.getPixelSize()) {
					case 1:
					case 2:
					case 4:
						if (IndexColorModel.class.
								isAssignableFrom(imageColorModelClass)) {
							return BufferedImage.TYPE_BYTE_BINARY;
						}
						logger.debug("Unsupported 1-, 2-, or 4-bit type for data type TYPE_BYTE");
						return BufferedImage.TYPE_CUSTOM;
					case 8:
						if (ComponentColorModel.class.
								isAssignableFrom(imageColorModelClass)) {
							if (imageColorSpaceType == ColorSpace.TYPE_GRAY) {
								return BufferedImage.TYPE_BYTE_GRAY;
							}
						}
						if (IndexColorModel.class.
								isAssignableFrom(imageColorModelClass)) {
							return BufferedImage.TYPE_BYTE_INDEXED;
						}
						logger.debug("Unsupported 1-byte type for data type TYPE_BYTE");
						return BufferedImage.TYPE_CUSTOM;
					case 24:
						if (ComponentColorModel.class.
								isAssignableFrom(imageColorModelClass)) {
							if (imageColorSpaceType == ColorSpace.TYPE_RGB 
									&& !imageColorModel.hasAlpha()) {
								return BufferedImage.TYPE_3BYTE_BGR;
							}
						}
						logger.debug("Unsupported 3-byte type for data type TYPE_BYTE");
						return BufferedImage.TYPE_CUSTOM;
					case 32:
						if (ComponentColorModel.class.
								isAssignableFrom(imageColorModelClass) 
								&& (imageColorSpaceType == ColorSpace.TYPE_RGB) 
								&& imageColorModel.hasAlpha()) {
							if (imageColorModel.isAlphaPremultiplied()) {
								return BufferedImage.TYPE_4BYTE_ABGR_PRE;
							} else {
								return BufferedImage.TYPE_4BYTE_ABGR;
							}
						}
						logger.debug("Unsupported 4-byte type for data type TYPE_BYTE");
						return BufferedImage.TYPE_CUSTOM;
					default:
						logger.debug("Unsupported pixel size for data type TYPE_BYTE");
						return BufferedImage.TYPE_CUSTOM;
				}
			case DataBuffer.TYPE_USHORT:
				switch (imageColorModel.getPixelSize()) {
					case 16:
						if (ComponentColorModel.class.
								isAssignableFrom(imageColorModelClass) 
								&& (imageColorSpaceType == ColorSpace.TYPE_GRAY)) {
							return BufferedImage.TYPE_USHORT_GRAY;
						}
						logger.debug("Unsupported 2-byte type for data type TYPE_USHORT");
						return BufferedImage.TYPE_CUSTOM;
					case 32:
						if (DirectColorModel.class.
								isAssignableFrom(imageColorModelClass)
								&& (imageColorSpaceType == ColorSpace.TYPE_RGB) 
								&& !imageColorModel.hasAlpha()) {
							int[] componentSize = imageColorModel.
									getComponentSize();
							if (componentSize[1] == 5) {
								return BufferedImage.TYPE_USHORT_555_RGB;
							}
							if (componentSize[1] == 6) {
								return BufferedImage.TYPE_USHORT_565_RGB;
							}
						}
						logger.debug("Unsupported 4-byte type for data type TYPE_USHORT");
						return BufferedImage.TYPE_CUSTOM;
					default:
						logger.debug("Unsupported pixel size for data type TYPE_USHORT");
						return BufferedImage.TYPE_CUSTOM;
				}
			case DataBuffer.TYPE_INT:
				if (DirectColorModel.class.isAssignableFrom(imageColorModelClass)
						&& imageColorModel.hasAlpha()) {
					if (image.getColorModel().isAlphaPremultiplied()) {
						return BufferedImage.TYPE_INT_ARGB_PRE;
					} else {
						return BufferedImage.TYPE_INT_ARGB;
					}
				}
				if (PackedColorModel.class.isAssignableFrom(imageColorModelClass) 
						&& !imageColorModel.hasAlpha()) {
					PackedColorModel pcm = (PackedColorModel) imageColorModel;
					int[] masks = pcm.getMasks();
					if (masks[0] == 255) {
						return BufferedImage.TYPE_INT_BGR;
					} else {
						return BufferedImage.TYPE_INT_RGB;
					}
				}
				logger.debug("Unsupported type for data type TYPE_INT");
				return BufferedImage.TYPE_CUSTOM;
			default:
				logger.debug("Unsupported data type of sample model");
				return BufferedImage.TYPE_CUSTOM;
		}
	}

	@Override
	public BufferedImage rotate(BufferedImage image, int angle) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (Math.abs(angle) == 90 || Math.abs(angle) == 270) {
			int tempH = height;
			height = width;
			width = tempH;
		}
		BufferedImage img = new BufferedImage(width, height, getType(image));
		Graphics2D g2D = img.createGraphics();
		g2D.rotate(Math.toRadians(angle), width >>> 1, height >>> 1);
		g2D.drawImage(image, null, (width >>> 1) - (image.getWidth() >>> 1),
				(height >>> 1) - (image.getHeight() >>> 1));
		g2D.dispose();
		return img;
	}

	@Override
	public BufferedImage resize(BufferedImage image, int newWidth,
			int newHeight) {
		return resizeBufferedImage(image, newWidth, newHeight,
				image.getWidth(), image.getHeight());
	}

	private BufferedImage resizeBufferedImage(BufferedImage image,
			int newWidth, int newHeight, int width, int height) {
		BufferedImage img = 
				new BufferedImage(newWidth, newHeight, getType(image));
		Graphics2D g2D = img.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2D.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, width,
				height, null);
		return img;
	}

	@Override
	public BufferedImage resizeProportional(BufferedImage image, int newWidth,
			int newHeight) {
		if ((newWidth == 0) && (newHeight == 0)) {
			return image;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (newWidth != 0) {
			newHeight = (int) (height * (float) newWidth / width);
		} else {
			newWidth = (int) (width * (float) newHeight / height);
		}
		return resizeBufferedImage(image, newWidth, newHeight, width, height);
	}

	@Override
	public BufferedImage resizeRelative(BufferedImage image,
			float resizeFactorWidth, float resizeFactorHeight) {
		int width = image.getWidth();
		int height = image.getHeight();
		int newWidth = (int) (width * resizeFactorWidth);
		int newHeight = (int) (height * resizeFactorHeight);
		return resizeBufferedImage(image, newWidth, newHeight, width, height);
	}

	@Override
	public BufferedImage resizeRelativeProportional(BufferedImage image,
			float resizeFactor) {
		int width = image.getWidth();
		int height = image.getHeight();
		int newWidth = (int) (width * resizeFactor);
		int newHeight = (int) (height * resizeFactor);
		return resizeBufferedImage(image, newWidth, newHeight, width, height);
	}
}
