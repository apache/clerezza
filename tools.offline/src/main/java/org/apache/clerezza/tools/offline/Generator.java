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
package org.apache.clerezza.tools.offline;

import java.util.logging.Level;
import org.apache.clerezza.tools.offline.utils.ConditionalOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.content.representations.core.ThumbnailService;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RendererFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.utils.ReplacingOutputStream;
import org.apache.clerezza.web.fileserver.util.MediaTypeGuesser;
import org.wymiwyg.commons.util.dirbrowser.MultiPathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * This JAX-RS resource provides a method to retrieve a zip file containing
 * an offline version of a site served by the clerezza instance
 *
 * @author reto
 */
@Component(metatype=true)
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/admin/offline")
public class Generator {

	@Reference
	private ContentGraphProvider cgp;

	@Reference
	private Serializer serializer;

	@Reference
	private RendererFactory rendererFactory;

	@Reference
	private ThumbnailService thumbnailService;

	private MediaTypeGuesser mediaTypeGuesser = MediaTypeGuesser.getInstance();

	final Logger logger = LoggerFactory.getLogger(Generator.class);

	final private Charset UTF8 = Charset.forName("utf-8");

	/**
	 *
	 * The service takes the following argumens:
	 * - 1 base-uri: for all resources with a URI starting with this Uri
	 * creation of the specified target fomats is attempted
	 * - 1 target-uri: where base-uri appears in the representations it is
	 * replaces with target-uri
	 * - 0 or 1 root link prefix: prefix to be prepended to links with Uri
	 * reference starting with a slash
	 * - 1 - n target formats: the file- extensions represing the different
	 * formats to be produced, the extensions are added to the generated files
	 * where the file would not otherwise end with the extension.
	 *
	 * The service generates a zip containing a directory structure matching the
	 * subpaths from base-uri, the directories contains files for the individual
	 * representations, URIs ending with / are matched to a file called
	 * "index"+format extension within the respective directory.
	 *
	 * @return
	 *		a zipped file
	 */
	@GET
	@Path("download")
	@Produces("application/zip")
	public Response download(@QueryParam("baseUri") String baseUri,
			@QueryParam("targetUri") String targetUri,
			@QueryParam("rootLinkPrefix") String rootLinkPrefix,
			@QueryParam("formatExtension") List<String> formatExtensions) throws IOException {

		if (baseUri == null) {
			throw new WebApplicationException(Response.
					status(Status.BAD_REQUEST).entity(
					"Parameter baseUri missing").build());
		}
		if (targetUri == null) {
			targetUri = baseUri;
		}
		if (rootLinkPrefix == null) {
			rootLinkPrefix = "";
		}
		if (formatExtensions == null) {
			throw new WebApplicationException(Response.
					status(Status.BAD_REQUEST).entity(
					"Parameter formatExtension missing, at least one required").build());
		}
		byte[] byteArray = createOfflineSite(baseUri, targetUri, rootLinkPrefix,
				formatExtensions);
		ResponseBuilder responseBuilder = Response.status(Status.OK).
				entity(byteArray);
		responseBuilder.header("Content-Disposition",
				"attachment; filename=site" + getCurrentDate() + ".zip");
		return responseBuilder.build();
	}

	private byte[] createOfflineSite(String baseUri, String targetUri,
			String rootLinkPrefix, List<String> formatExtensions) throws IOException {
		PathNode baseNode = createFileHierarchy(baseUri, baseUri, targetUri, rootLinkPrefix,
				formatExtensions);
		PathNode allHostsNode = createFileHierarchy(Constants.ALL_HOSTS_URI_PREFIX+"/",
				baseUri,targetUri, rootLinkPrefix, formatExtensions);
		PathNode rootNode = new MultiPathNode(allHostsNode, baseNode);
		try {
			return ZipCreationUtil.createZip(rootNode);
		} catch (IOException ex) {
			throw new WebApplicationException(ex);
		}
	}

	private PathNode createFileHierarchy(String baseUri, String retrievalBaseUri, String targetUri,
			String rootLinkPrefix, List<String> formatExtensions) throws IOException {
		Hierarchy result = new Hierarchy("");
		MGraph contentGraph = cgp.getContentGraph();
		Set<UriRef> matchingUri = new HashSet<UriRef>();
		for (Triple triple : contentGraph) {
			final NonLiteral subject = triple.getSubject();
			if ((subject instanceof UriRef) &&
					((UriRef)subject).getUnicodeString().startsWith(baseUri)) {
				matchingUri.add((UriRef)subject);
			}
		}
		for (UriRef uriRef : matchingUri) {
			if (matchingUri.contains(new UriRef(uriRef.getUnicodeString()+"index"))) {
				continue;
			}
			if (matchingUri.contains(new UriRef(uriRef.getUnicodeString()+"index.html"))) {
				continue;
			}
			generateFilesForResource(baseUri, retrievalBaseUri, targetUri,
					rootLinkPrefix, uriRef, contentGraph, formatExtensions,
					result);
		}
		return result;
	}

	/**
	 * Currently not using graph, but in future this might be used for special
	 * handling of infodicscobits
	 */
	private void generateFilesForResource(String baseUri, String retrievalBaseUri,
			String targetBaseUri, String rootLinkPrefix, UriRef resourceUriRef, TripleCollection graph,
			List<String> formatExtensions, Hierarchy hierarchy) throws IOException {
		final String path = getPathForUriRef(resourceUriRef, baseUri);
		UriRef retreivalUriRef = new UriRef(retrievalBaseUri+path);
		for (String formatExtension : formatExtensions) {
			MediaType mediaType = mediaTypeGuesser.getTypeForExtension(formatExtension);
			try {
				final byte[] variant = getVariant(retreivalUriRef, mediaType);
				if (mediaType.getSubtype().equals("png"))
					logger.info("Got variant of length : {}",variant.length);
				final byte[] addedThumbnailUris = applyThumbnailService(variant);
				final byte[] dataPrefixApplied = applyRootLinkPrefix(addedThumbnailUris,
						rootLinkPrefix, mediaType);
				final String filePath = resourceUriRef.getUnicodeString().endsWith("/") ? path+"index" : path;
				final String dottedExtension = "."+formatExtension;
				final String extendedPath = filePath.endsWith(dottedExtension) ?
					filePath : filePath + dottedExtension;
				if (mediaType.getSubtype().equals("png"))
					logger.info("Processed length : {}",dataPrefixApplied.length);
				hierarchy.addChild(extendedPath, 
						changeBaseUri(dataPrefixApplied, baseUri, targetBaseUri));
			} catch (VariantUnavailableException ex) {
				logger.debug("{} not available as {}", resourceUriRef, mediaType);
			}
		}	
	}

	private byte[] changeBaseUri(byte[] variant, String baseUri,
			String targetBaseUri) {
		try {
			//here we should locate some mediaType specific handlers
			//a quick hack
			final ByteArrayOutputStream resultWriter = new ByteArrayOutputStream(variant.length + 1000);
			final OutputStream out = new ReplacingOutputStream(resultWriter,
						baseUri.getBytes(UTF8),
						targetBaseUri.getBytes(UTF8));
			out.write(variant);
			out.close();
			return resultWriter.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] getVariant(UriRef uriRef, MediaType mediaType) throws 
			IOException, VariantUnavailableException {
		logger.info("requested uri " + uriRef.getUnicodeString() + ",mediatype " + mediaType.toString());
		try{
			final URL url = new URL(uriRef.getUnicodeString());

			final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", mediaType.toString());
			urlConnection.connect();
			final int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				throw new VariantUnavailableException("response code: "+responseCode);
			}
			final String responseContentType = urlConnection.getContentType();
			if (!responseContentType.startsWith(mediaType.toString())) {
				throw new VariantUnavailableException("Got " + responseContentType + " and not " + mediaType);
			}
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final InputStream in = urlConnection.getInputStream();
			try {
				for (int ch = in.read(); ch != -1; ch = in.read()) {
					baos.write(ch);
				}
			} finally {
				in.close();
			}
			return baos.toByteArray();
		} catch(SocketException ex) {
			try {
				logger.info("SocketException thrown");
				Thread.sleep(5000);		
			} catch (InterruptedException ex1) {
				new RuntimeException(ex1);
			}
			return getVariant(uriRef, mediaType);
		}
	}

	private String getPathForUriRef(UriRef uriRef, String baseUri) {
		if (!uriRef.getUnicodeString().startsWith(baseUri)) {
			throw new RuntimeException(uriRef+" doesn't start with "+baseUri);
		}
		return uriRef.getUnicodeString().substring(baseUri.length());
	}

	private String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}


	private Set<String> rootLinkIndicators = new HashSet<String>();
	{
		rootLinkIndicators.add("src=\"");
		rootLinkIndicators.add("href=\"");
		rootLinkIndicators.add("url\\(");
	}

	private byte[] applyRootLinkPrefix(byte[] variant, String rootLinkPrefix,
			MediaType mediaType) {
		try {
			//here we should locate some mediaType specific handlers
			//a quick hack
			final ByteArrayOutputStream resultWriter = new ByteArrayOutputStream(variant.length + 1000);
			OutputStream out = resultWriter;
			for (String rootLinkIndicator : rootLinkIndicators) {
				out = new ReplacingOutputStream(out, 
						(rootLinkIndicator + "/").getBytes(UTF8),
						(rootLinkIndicator + rootLinkPrefix + "/").getBytes(UTF8));
			}
			out.write(variant);
			out.close();
			return resultWriter.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] applyThumbnailService(byte[] variant) {
		try {			
			final ByteArrayOutputStream resultWriter = new ByteArrayOutputStream(variant.length);
			OutputStream thumbnailCorrectingStream = new ConditionalOutputStream(resultWriter,
					new ThumbnailCondition(thumbnailService));
			thumbnailCorrectingStream.write(variant);
			thumbnailCorrectingStream.close();
			return resultWriter.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}



	private static class VariantUnavailableException extends Exception {

		VariantUnavailableException(String message) {
			super(message);
		}

	}
}
