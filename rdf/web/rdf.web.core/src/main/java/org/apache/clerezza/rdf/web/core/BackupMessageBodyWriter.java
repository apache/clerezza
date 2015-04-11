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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.commons.rdf.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;

/**
 * This does the actual work of producing a backup
 * 
 * @author hasan, reto
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Produces("application/zip")
@Provider
public class BackupMessageBodyWriter implements MessageBodyWriter<Backup> {

    @Reference
    TcManager tcManager;

    @Reference
    Serializer serializer;

    final Logger logger = LoggerFactory.getLogger(BackupMessageBodyWriter.class);

    private final String folder = "graphs/";


    byte[] createBackup() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        writeBackup(result);
        return result.toByteArray();
    }

    private void archive(ZipOutputStream compressedTcs, 
            Graph tripleCollection,
            String fileName) throws IOException, UnsupportedFormatException {
        Lock readLock = null;
        compressedTcs.putNextEntry(new ZipEntry(fileName));
        if (tripleCollection instanceof Graph) {
            readLock = ((Graph) tripleCollection).getLock().readLock();
            readLock.lock();
        }
        try {
            serializer.serialize(compressedTcs, tripleCollection,
                    SupportedFormat.N_TRIPLE);
        } finally {
            if (readLock != null) {
                readLock.unlock();
            }
        }
    }

    private String getTcFileName(IRI tcUri, String extension,
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

    private void writeBackup(OutputStream result) {
        Map<String, Integer> fileNameCount = new HashMap<String, Integer>();
        Graph backupContents = new SimpleGraph();
        try {
            ZipOutputStream compressedTcs = new ZipOutputStream(result);

            compressedTcs.putNextEntry(new ZipEntry(folder));

            Set<IRI> tripleCollections = tcManager.listGraphs();
            Iterator<IRI> tcIRIs = tripleCollections.iterator();
            while (tcIRIs.hasNext()) {
                IRI tcUri = tcIRIs.next();
                String fileName = folder + getTcFileName(tcUri, ".nt",
                        fileNameCount);
                Graph tripleCollection = tcManager.getGraph(tcUri);
                archive(compressedTcs, tripleCollection, fileName);
                if (tripleCollection instanceof Graph) {
                    backupContents.add(new TripleImpl(tcUri, RDF.type,
                            BACKUP.Graph));
                } else {
                    backupContents.add(new TripleImpl(tcUri, RDF.type,
                            BACKUP.Graph));
                }
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
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return Backup.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Backup t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Backup t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        writeBackup(entityStream);
    }
}
