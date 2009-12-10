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
package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;

/**
 * This JAX-RS resource provides a method to retrieve a zip file containing
 * all triple collections that the use may access. The triple collection are
 * serialized in N-Triples format. The URI path of this resource is
 * "/admin/backup".
 * 
 * @author hasan
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/backup")
public class Backup {

	@Reference
	TcManager tcManager;

	@Reference
	Serializer serializer;

	final Logger logger = LoggerFactory.getLogger(Backup.class);

	private final String folder = "graphs/";

	/**
	 * Get a zipped file containing all triple collections which the
	 * user may access. The resource is accessible through the URI path
	 * "/admin/backup/download".
	 * The triple collections are serialized in N-Triples format before being
	 * archived in a single zipped file.
	 * A mapping of the names of the files in the archive to triple collection
	 * names is available as well in the archive as a text file named
	 * triplecollections.nt.
	 *
	 * @return
	 *		a zipped file
	 */
	@GET
	@Path("download")
	@Produces("application/zip")
	public Response download() {

		byte[] byteArray =  createBackup();
		ResponseBuilder responseBuilder = Response.status(Status.OK).
					entity(byteArray);
		responseBuilder.header("Content-Disposition",
				"attachment; filename=backup" + getCurrentDate() +".zip");
		return responseBuilder.build();
	}

	byte[] createBackup() {
		Map<String, Integer> fileNameCount = new HashMap<String, Integer>();

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		MGraph backupContents = new SimpleMGraph();
		try {
			ZipOutputStream compressedTcs = new ZipOutputStream(result);

			compressedTcs.putNextEntry(new ZipEntry(folder));

			Set<UriRef> tripleCollections = tcManager.listTripleCollections();
			Iterator<UriRef> tcUriRefs = tripleCollections.iterator();
			while (tcUriRefs.hasNext()) {
				UriRef tcUri = tcUriRefs.next();
				String fileName = folder + getTcFileName(tcUri, ".nt",
						fileNameCount);
				archive(compressedTcs, tcManager.getTriples(tcUri), fileName);
				backupContents.add(new TripleImpl(tcUri, RDF.type,
						BACKUP.Graph));
				backupContents.add(new TripleImpl(tcUri, BACKUP.file,
						LiteralFactory.getInstance().createTypedLiteral(
						fileName)));
			}
			archive(compressedTcs, backupContents, "triplecollections.nt");
			compressedTcs.close();

		} catch (UnsupportedFormatException ufe) {
			throw new WebApplicationException(Response.status(
					Status.NOT_ACCEPTABLE).entity(ufe.getMessage()).build());
		} catch (IOException ex) {
			throw new WebApplicationException(ex);
		}
		return result.toByteArray();
	}

	private void archive(ZipOutputStream compressedTcs, 
			TripleCollection tripleCollection,
			String fileName) throws IOException, UnsupportedFormatException {
		Lock readLock = null;
		final int BUF_SIZE = 2048;
		byte buffer[] = new byte[BUF_SIZE];
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();

		if (tripleCollection instanceof LockableMGraph) {
			readLock = ((LockableMGraph) tripleCollection).getLock().readLock();
			readLock.lock();
		}
		try {
			serializer.serialize(serializedGraph, tripleCollection,
					SupportedFormat.N_TRIPLE);
		} finally {
			if (readLock != null) {
				readLock.unlock();
			}
		}
		ByteArrayInputStream graphToCompress = new ByteArrayInputStream(
				serializedGraph.toByteArray());

		compressedTcs.putNextEntry(new ZipEntry(fileName));
		int count;
		while ((count = graphToCompress.read(buffer, 0, BUF_SIZE)) != -1) {
			compressedTcs.write(buffer, 0, count);
		}
		graphToCompress.close();
	}

	private String getTcFileName(UriRef tcUri, String extension,
			Map<String, Integer> fileNameCount) {
		String fileName = tcUri.getUnicodeString();
		fileName = fileName.substring(fileName.lastIndexOf("/")+1);
		Integer count = fileNameCount.get(fileName);
		if (count == null) {
			fileNameCount.put(fileName, 0);
		} else {
			count++;
			fileNameCount.put(fileName, count);
			fileName = fileName.concat("_" + count);
		}
		return  fileName.concat(extension);
	}

	private String getCurrentDate(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
	}
}
