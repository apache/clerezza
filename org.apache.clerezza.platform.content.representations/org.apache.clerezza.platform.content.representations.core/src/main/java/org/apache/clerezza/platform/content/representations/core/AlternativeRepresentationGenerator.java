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
package org.apache.clerezza.platform.content.representations.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.content.DiscobitsHandler;
import org.apache.clerezza.platform.content.InfoDiscobit;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.utils.imageprocessing.ImageProcessor;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.felix.scr.annotations.Services;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class provides a method that generates a thumbnail for specified data
 * if the specified media type is supported. The supported media types are "image/*".
 * The generated thumbnail is added as a property to the specified
 * <code>GraphNode</code>. The property URI is
 * "http://discobits.org/ontology#thumbnail".
 *
 * @author mir
 */
@Component(metatype=true)
@Services({
	@Service(MetaDataGenerator.class),
	@Service(AlternativeRepresentationGenerator.class)
})

public class AlternativeRepresentationGenerator implements MetaDataGenerator {

	private static class Resolution {

		private int width;

		private int height;
		/**
		 * Takes a String, which contains a resoultion in the format
		 * [width]x[height].
		 *
		 * @param resoulutionString
		 */
		public Resolution(String resoulutionString) {
			String[] widthAndHeight = resoulutionString.split("x");
			width = new Integer(widthAndHeight[0]);
			height = new Integer(widthAndHeight[1]);
		}

		/**
		 * A Resolution with the specified width and height.
		 *
		 * @param width
		 * @param height
		 */
		public Resolution(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}
	}

	@Reference
	private ImageProcessor imageProcessor;
	@Property(value="100x100,200x200", description="Specifies the resolutions of alternative" +
			" representations in the format [width]x[height]. Multiple resolutions" +
			" are separated by comma (e.g. 100x100,30x30)")
	public static final String RESOLUTIONS = "resolutions";
	
	private volatile ServiceTracker discobitTracker;
	private Resolution[] resolutions;

	/**
	 * Indicates if data given to the AlternativeRepresentationGenerator is a
	 * alternative representation itself and therefore does not have to have a
	 * alternative representation generated for it.
	 */
	private ThreadLocal<Boolean> isAltRepresentation = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	protected void activate(ComponentContext context) {
		setupResolutionArray((String) context.getProperties().get(RESOLUTIONS));
		discobitTracker = new ServiceTracker(context.getBundleContext(),
				DiscobitsHandler.class.getName(), null);
		new Thread() {
			@Override
			public void run() {
				discobitTracker.open();
			}
		}.start();
	}

	private void setupResolutionArray(String resolutionsString) {
		String[] resoultionStrings = resolutionsString.split(",");
		resolutions = new Resolution[resoultionStrings.length];
		for (int i = 0; i < resoultionStrings.length; i++) {
			resolutions[i] = new Resolution(resoultionStrings[i].trim());
		}
	}

	protected void deactivate(ComponentContext context) {
		discobitTracker.close();
		discobitTracker = null;
	}

	@Override
	public void generate(GraphNode node, byte[] data, MediaType mediaType) {
		if (isAltRepresentation.get()) {
			return;
		}
		if (mediaType.getType().startsWith("image")) {
			generateAlternativeImages(data, mediaType, node);
		}
	}
	
	public UriRef generateAlternativeImage(GraphNode infoBitNode, int width, int height) {
		try {
			isAltRepresentation.set(Boolean.TRUE);
			InfoDiscobit infoBit = InfoDiscobit.createInstance(infoBitNode);
			BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(infoBit.getData()));
			return generateAlternativeImage(buffImage, new Resolution(width, height), 
					MediaType.valueOf(infoBit.getContentType()), infoBitNode);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			isAltRepresentation.set(Boolean.FALSE);
		}
	}

	private void generateAlternativeImages(byte[] data, MediaType mediaType,
			GraphNode node) throws RuntimeException {
		try {
			isAltRepresentation.set(Boolean.TRUE);
			BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(data));
			int imgWidth = buffImage.getWidth();
			int imgHeigth = buffImage.getHeight();
			for (Resolution resolution : resolutions) {
				if (imgWidth > resolution.getWidth() || imgHeigth > resolution.getHeight()) {
					generateAlternativeImage( buffImage, resolution, mediaType, node);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			isAltRepresentation.set(Boolean.FALSE);
		}
	}

	private UriRef generateAlternativeImage(BufferedImage buffImage, Resolution resolution,
			MediaType mediaType, GraphNode node) throws IOException {
		BufferedImage alternativeImage = imageProcessor.makeAThumbnail(buffImage,
				resolution.getWidth(), resolution.getHeight());
		byte[] alternativeImageBytes = bufferedImage2ByteArray(alternativeImage, mediaType);
		DiscobitsHandler contentHandler = (DiscobitsHandler) discobitTracker.getService();
		
		UriRef thumbnailUri = createThumbnailUri((UriRef) node.getNode(), alternativeImage);
		contentHandler.put(thumbnailUri, mediaType, alternativeImageBytes);
		Lock writeLock = node.writeLock();
		writeLock.lock();
		try {
			node.addProperty(DISCOBITS.thumbnail, thumbnailUri);
			return thumbnailUri;
		} finally {
			writeLock.unlock();
		}
	}

	private byte[] bufferedImage2ByteArray(BufferedImage image,
			MediaType mediaType) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, mediaType.getSubtype(), baos);
		byte[] bytes = baos.toByteArray();
		baos.flush();
		return bytes;
	}

	private UriRef createThumbnailUri(UriRef uriRef, BufferedImage img) {
		String resolution = "-" + img.getWidth() + "x" + img.getHeight();
		String oldUri = uriRef.getUnicodeString();
		String newUri;
		int lastIndexOfDot = oldUri.lastIndexOf(".");
		int lastIndexOfSlash = oldUri.lastIndexOf("/");
		// 6 characters to keep the extension at the end.
		if (lastIndexOfSlash < lastIndexOfDot &&
				lastIndexOfDot >= (oldUri.length() - 6)) {
			String firstPart = oldUri.substring(0, lastIndexOfDot);
			String lastPart = oldUri.substring(lastIndexOfDot, oldUri.length());
			newUri = firstPart + resolution + lastPart;
		} else {
			newUri = oldUri + resolution;
		}
		return new UriRef(newUri);
	}
}
