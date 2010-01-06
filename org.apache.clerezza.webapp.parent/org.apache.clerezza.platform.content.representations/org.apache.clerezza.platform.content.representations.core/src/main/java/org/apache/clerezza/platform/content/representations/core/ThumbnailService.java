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

import java.net.URI;
import java.util.Iterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.content.representations.ontologies.REPRESENTATIONS;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.EXIF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * This JAX-RS resource provides a method to retrieve the uri to
 * the thumbnail or a other small representation of a InfoDiscoBit.
 * 
 * @author mir
 */
@Component
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("thumbnail-service")
public class ThumbnailService {

	@Reference
	ContentGraphProvider cgProvider;

	@Reference
	PlatformConfig config;

	private UriRef defaultIconUri;

	protected void activate(ComponentContext context) {
		String baseUri = config.getDefaultBaseUri().getUnicodeString();
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/text/plain.png"), MediaType.TEXT_PLAIN);
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/msword.png"), "application/msword");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/x-amf.png"), "application/x-shockwave-flash");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/pdf.png"), "application/pdf");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/vnd.ms-powerpoint.png"), "application/vnd.ms-powerpoint");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/x-tar.png"), "application/x-tar");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/x-amf.png"), "application/x-amf");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/vnd.ms-excel.png"), "application/vnd.ms-excel");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/application/octet-stream.png"), "application/octet-stream");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/audio/any.png"), "audio/x-wav");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/audio/any.png"), "audio/mpeg");
		addMediaTypeIcon(createUriRef(baseUri, "style/images/icons/mediatype/audio/any.png"), "audio/mid");
		defaultIconUri = createUriRef(baseUri, "style/images/icons/mediatype/any.png");
	}

	private UriRef createUriRef(String baseUri, String relativeUri) {
		return new UriRef(baseUri + relativeUri);
	}

	private void addMediaTypeIcon(UriRef icon, String type) {
		cgProvider.getContentGraph().add(
				new TripleImpl(icon, REPRESENTATIONS.isIconFor ,
				LiteralFactory.getInstance().createTypedLiteral(type)));
	}

	/**
	 * Returns the thumbnail uri for a InfoDiscoBit which is located at the uri
	 * specified over the query parameter "uri". The thumbnails
	 * maximum width and height can optionally be specified over the query parameters
	 * "width" and "height". If more than one acceptable thumbnail is available
	 * then the thumbnail uri of the thumbnail with the highest resolution
	 * (width * height) is returned. If no thumbnail is available then the uri of
	 * the icon representing the media type is returned. If also no media type
	 * icon is available the uri to default icon is returned.
	 * @param infoBitUri the uri of the infoDiscoBit of which the thumbnail uri should be returned
	 * @param height the maximum height that the thumbnail has
	 * @param width the maximum width that the thumbnail has
	 * @return
	 */
	@GET
	public Response getThumbnailUri(@QueryParam("uri") UriRef infoBitUri,
			@QueryParam("width") Integer width,
			@QueryParam("height") Integer height) {		
		if (width == null) {
			width = Integer.MAX_VALUE;
		}
		if (height == null) {
			height = Integer.MAX_VALUE;
		}
		GraphNode infoBitNode = new GraphNode(infoBitUri, cgProvider.getContentGraph());
		UriRef thumbnailUri = getThumbnailUri(infoBitNode, height, width);
		if (thumbnailUri != null) {
			return Response.seeOther(
					URI.create((thumbnailUri).getUnicodeString())).build();
		}		
		Iterator<Resource> mediaTypes = infoBitNode.getObjects(DISCOBITS.mediaType);
		if (mediaTypes.hasNext()) {
			GraphNode mediaType = new GraphNode(mediaTypes.next(), 
					cgProvider.getContentGraph());
			
			Iterator<NonLiteral> icons = mediaType.getSubjects(REPRESENTATIONS.isIconFor);
			if (icons.hasNext()) {
				return Response.seeOther(
					URI.create(((UriRef)icons.next()).getUnicodeString())).build();
			}
		}
		return Response.seeOther(URI.create(defaultIconUri.getUnicodeString())).build();
	}

	private UriRef getThumbnailUri(GraphNode infoBitNode, Integer height,
			Integer width) {
		UriRef resultThumbnailUri = null;
		int pixels = 0;
		Iterator<Resource> thumbnails = infoBitNode.getObjects(DISCOBITS.thumbnail);
		while (thumbnails.hasNext()) {
			UriRef thumbnailUri = (UriRef) thumbnails.next();
			GraphNode thumbnailNode = new GraphNode(thumbnailUri, 
					cgProvider.getContentGraph());
			Iterator<Resource> exifWidths = thumbnailNode.getObjects(EXIF.width);
			Iterator<Resource> exifHeights = thumbnailNode.getObjects(EXIF.height);
			if (!exifWidths.hasNext() || !exifHeights.hasNext()) {
				continue;
			}
			Integer thumbnailWidth = LiteralFactory.getInstance().createObject(
					Integer.class, (TypedLiteral) exifWidths.next());
			Integer thumbnailHeight = LiteralFactory.getInstance().createObject(
					Integer.class, (TypedLiteral) exifHeights.next());
			int thumbnailPixels = thumbnailWidth * thumbnailHeight;
			if (thumbnailHeight <= height && thumbnailWidth <= width && thumbnailPixels > pixels) {
				resultThumbnailUri = thumbnailUri;
				pixels = thumbnailPixels;
			}
		}
		return resultThumbnailUri;
	}
}