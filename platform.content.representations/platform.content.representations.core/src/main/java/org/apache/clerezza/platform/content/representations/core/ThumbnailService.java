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

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.EXIF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This JAX-RS resource provides a method to retrieve the uri to
 * the thumbnail or a other small representation of a InfoDiscoBit.
 *
 * @author mir
 */
@Component
@Services({
	@Service(Object.class),
	@Service(ThumbnailService.class)
})

@Property(name = "javax.ws.rs", boolValue = true)
@Path("thumbnail-service")
public class ThumbnailService implements BundleListener {

	@Reference
	ContentGraphProvider cgProvider;
	@Reference
	PlatformConfig config;
	@Reference
	AlternativeRepresentationGenerator altRepGen;
	private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);
	private BundleContext bundleContext;
	private String STATICWEB_PATH = "/CLEREZZA-INF/web-resources/style/";
	private String MEDIA_TYPE_BASE_PATH = STATICWEB_PATH + "images/icons/mediatype/";
	private Bundle cachedStyleBundle = null;
	private Map<MediaType, String> mediaTypeIconUriCache =
			Collections.synchronizedMap(new HashMap<MediaType, String>());

	protected void activate(ComponentContext context) {
		bundleContext = context.getBundleContext();
		bundleContext.addBundleListener(this);
	}

	protected void deactivate(ComponentContext context) {
		bundleContext.removeBundleListener(this);
		bundleContext = null;
		mediaTypeIconUriCache.clear();
	}

	/**
	 * Returns the thumbnail uri for a InfoDiscoBit which is located at the uri
	 * specified over the query parameter "uri". The thumbnails
	 * maximum width and height can optionally be specified over the query parameters
	 * "width" and "height". Futhermore there is the optional "exact" parameter,
	 * which specifies if the thumbnail must have the exact width and height as
	 * specified or not (default is "false"). If more than one acceptable thumbnail
	 * is available then the thumbnail uri of the thumbnail with the highest resolution
	 * (width * height) is returned. If no thumbnail is available and the logged
	 * in user has the write permission for the content graph, then an attempt is
	 * made to create the thumbnail on the fly. If this fails or the write permission
	 * is missing, then the uri of the icon representing the media type is returned.
	 * If also no media type icon is available the uri to default icon is returned.
	 *
	 * @param infoBitUri the uri of the infoDiscoBit of which the thumbnail uri should
	 *		be returned
	 * @param height the maximum height that the thumbnail has
	 * @param width the maximum width that the thumbnail has
	 * @param exact boolean that specifies if the return thumbnail should have
	 *		the exact width and height.
	 * @return
	 */
	@GET
	public Response getThumbnailUri(@QueryParam("uri") UriRef infoBitUri,
			@QueryParam("width") Integer width, @QueryParam("height") Integer height,
			@DefaultValue("false") @QueryParam("exact") Boolean exact, @Context UriInfo uriInfo) {
		return RedirectUtil.createSeeOtherResponse(
				getThumbnailUri(infoBitUri, width, height, exact).getUnicodeString(), uriInfo);
	}


	/**
	 * Returns the thumbnail uri for a InfoDiscoBit which is located at the uri
	 * specified over the query parameter "uri". The thumbnails
	 * maximum width and height can optionally be specified over the query parameters
	 * "width" and "height". If more than one acceptable thumbnail is available
	 * then the thumbnail uri of the thumbnail with the highest resolution
	 * (width * height) is returned. If no thumbnail is available and the logged
	 * in user has the write permission for the content graph, then an attempt is
	 * made to create the thumbnail on the fly. If this fails or the write permission
	 * is missing, then the uri of the icon representing the media type is returned.
	 * If also no media type icon is available the uri to default icon is returned.
	 *
	 * @param infoBitUri the uri of the infoDiscoBit of which the thumbnail uri should
	 *		be returned
	 * @param height the maximum height that the thumbnail has
	 * @param width the maximum width that the thumbnail has
	 * @return
	 */
	public UriRef getThumbnailUri(UriRef infoBitUri, Integer width,  Integer height) {
		return getThumbnailUri(infoBitUri, width, height, false);
	}

	/**
	 * Returns the thumbnail uri for a InfoDiscoBit which is located at the uri
	 * specified over the query parameter "uri". The thumbnails
	 * maximum width and height can optionally be specified over the query parameters
	 * "width" and "height". Futhermore there is the optional "exact" parameter,
	 * which specifies if the thumbnail must have the exact width and height as
	 * specified or not. If more than one acceptable thumbnail is available then
	 * the thumbnail uri of the thumbnail with the highest resolution (width * height)
	 * is returned. If no thumbnail is available and the logged in user has the write
	 * permission for the content graph, then an attempt is made to create the
	 * thumbnail on the fly. If this fails or the write permission is missing, then
	 * the uri of the icon representing the media type is returned. If also no
	 * media type icon is available the uri to default icon is returned.
	 *
	 * @param infoBitUri the uri of the infoDiscoBit of which the thumbnail uri should
	 *		be returned
	 * @param height the maximum height that the thumbnail has
	 * @param width the maximum width that the thumbnail has
	 * @param exact boolean that specifies if the return thumbnail should have
	 *		the exact width and height.
	 * @return
	 */
	public UriRef getThumbnailUri(UriRef infoBitUri, Integer width,  Integer height,
			boolean exact) {
		if ((width == null) && (height == null)) {
			throw new IllegalArgumentException("height and/or width must be specified");
		}
		if (width == null) {
			width = Integer.MAX_VALUE;
		}
		if (height == null) {
			height = Integer.MAX_VALUE;
		}
		GraphNode infoBitNode = new GraphNode(infoBitUri, cgProvider.getContentGraph());
		UriRef thumbnailUri = getGeneratedThumbnailUri(infoBitNode, width, height, exact);
		if (thumbnailUri == null) {
			Literal mediaTypeLiteral = null;
			Lock readLock = infoBitNode.readLock();
			readLock.lock();
			try {
				Iterator<Resource> mediaTypes = infoBitNode.getObjects(DISCOBITS.mediaType);
				if (mediaTypes.hasNext()) {
					mediaTypeLiteral = (Literal) mediaTypes.next();
				}
			} finally {
				readLock.unlock();
			}
			if (mediaTypeLiteral != null) {
				MediaType mediaType = MediaType.valueOf(mediaTypeLiteral.getLexicalForm());
				// if the infoBit is an image, create a thumbnail on the fly.
				if (mediaType.getType().startsWith("image")) {
					try {
						thumbnailUri = altRepGen.generateAlternativeImage(infoBitNode, width,
								height, exact);
					} catch (Exception ex) {
						ex.printStackTrace();
						// Was worth a try. eLets go on
					}
				}
				if (thumbnailUri == null) {
					String iconUri = mediaTypeIconUriCache.get(mediaType);
					if (iconUri == null) {
						iconUri = getMediaTypeIconUri(mediaType);
						mediaTypeIconUriCache.put(mediaType, iconUri);
					}
					thumbnailUri = new UriRef(iconUri);
				}
			}
		}
		if (thumbnailUri == null) {
			thumbnailUri = new UriRef(getDefaultIconUrl(getStyleBundle()));
		}
		return thumbnailUri;
	}

	private String getMediaTypeIconUri(MediaType mediaType) {
		Bundle styleBundle = getStyleBundle();
		if (styleBundle == null) {
			throw new RuntimeException("no style bundle found");
		}
		String path = MEDIA_TYPE_BASE_PATH + mediaType.getType() + "/";
		Enumeration entries = styleBundle.findEntries(path,
				mediaType.getSubtype() + ".*", false);
		String iconUri = createIconUri(entries);
		if (iconUri != null) {
			return iconUri;
		}
		entries = styleBundle.findEntries(path, "any.*", false);
		iconUri = createIconUri(entries);
		if (iconUri != null) {
			return iconUri;
		}
		return getDefaultIconUrl(styleBundle);
	}

	private String getDefaultIconUrl(Bundle bundle) {
		Enumeration entries = bundle.findEntries(MEDIA_TYPE_BASE_PATH, "any.*", false);
		String iconUri = createIconUri(entries);
		if (iconUri != null) {
			return iconUri;
		} else {
			throw new RuntimeException("No default icon found");
		}
	}

	private String createIconUri(Enumeration entries) {
		if (entries != null && entries.hasMoreElements()) {
			URL iconUrl = (URL) entries.nextElement();
			return iconUrl.getPath().replace(STATICWEB_PATH, "style/");
		}
		return null;
	}

	private UriRef getGeneratedThumbnailUri(GraphNode infoBitNode,
			Integer width, Integer height, boolean exact) {
		if (isFittingImage(infoBitNode, width, height, exact)) {
			return (UriRef) infoBitNode.getNode();
		}
		UriRef resultThumbnailUri = null;
		int pixels = 0;
		Lock readLock = infoBitNode.readLock();
		readLock.lock();
		try {
			Iterator<Resource> thumbnails = infoBitNode.getObjects(DISCOBITS.thumbnail);
			while (thumbnails.hasNext()) {
				UriRef thumbnailUri = (UriRef) thumbnails.next();
				GraphNode thumbnailNode = new GraphNode(thumbnailUri,
						cgProvider.getContentGraph());
				int thumbnailPixels = getSurfaceSizeIfFitting(thumbnailNode, width, height, exact);
				if (thumbnailPixels > pixels) {
					if (exact) {
						return thumbnailUri;
					}
					resultThumbnailUri = thumbnailUri;
					pixels = thumbnailPixels;
				}
			}
		} finally {
			readLock.unlock();
		}
		return resultThumbnailUri;
	}

	/**
	 * returns the surface in pixel if the image fits withing width and height,
	 * or -1 if it doesn't fit
	 */
	private int getSurfaceSizeIfFitting(GraphNode infoBitNode, Integer width, Integer height,
			boolean exact) {
		Resource imageRes = infoBitNode.getNode();
		if (imageRes instanceof UriRef) {
			String imageUri = ((UriRef)imageRes).getUnicodeString();
			if (!exact && imageUri.contains(AlternativeRepresentationGenerator.EXACT_APPENDIX)) {
				return -1;
			}
		}		
		Iterator<Resource> exifWidths = infoBitNode.getObjects(EXIF.width);
		Iterator<Resource> exifHeights = infoBitNode.getObjects(EXIF.height);
		if (!exifWidths.hasNext() || !exifHeights.hasNext()) {
			log.warn(infoBitNode.getNode() + " doesn't have exif:width and exif:height");
			return -1;
		}
		int thumbnailWidth = LiteralFactory.getInstance().createObject(
				Integer.class, (TypedLiteral) exifWidths.next());
		int thumbnailHeight = LiteralFactory.getInstance().createObject(
				Integer.class, (TypedLiteral) exifHeights.next());
		if (exact) {
			if (thumbnailHeight == height && thumbnailWidth == width) {
				return 1;
			}
		} else {
			if (thumbnailHeight <= height && thumbnailWidth <= width) {
				return thumbnailWidth * thumbnailHeight;
			}
		}
		return -1;
	}

	/**
	 * returns true if infoBitNode is an image and fits
	 */
	private boolean isFittingImage(GraphNode infoBitNode, Integer width, Integer height,
			boolean exact) {
		Lock readLock = infoBitNode.readLock();
		readLock.lock();
		try {
			final Iterator<Literal> mediaTypesIter = infoBitNode.getLiterals(DISCOBITS.mediaType);
			if (!mediaTypesIter.hasNext()) {
				return false;
			}
			if (mediaTypesIter.next().getLexicalForm().startsWith("image")) {
				return getSurfaceSizeIfFitting(infoBitNode, width, height, exact) > -1;
			} else {
				return false;
			}
		} finally {
			readLock.unlock();
		}
	}

	private synchronized Bundle getStyleBundle() {
		if (cachedStyleBundle != null) {
			return cachedStyleBundle;
		}
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			URL staticWebPathURL = bundle.getEntry(STATICWEB_PATH);
			if (staticWebPathURL != null) {
				cachedStyleBundle = bundle;
				return bundle;
			}
		}
		return null;
	}

	@Override
	public synchronized void bundleChanged(BundleEvent be) {
		if (be.getType() == BundleEvent.UNINSTALLED
				&& be.getBundle().equals(cachedStyleBundle)) {
			cachedStyleBundle = null;
			cachedStyleBundle = getStyleBundle();
			mediaTypeIconUriCache.clear();
		}
	}
}
