/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
package org.apache.clerezza.triaxrs.providers.provided;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.clerezza.triaxrs.util.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Produces("*/*")
@Consumes("*/*")
public class FileProvider implements MessageBodyWriter<File>, MessageBodyReader<File> {

	private static final Logger logger = LoggerFactory.getLogger(FileProvider.class);
	private String prefix = "FP_PRE";                                   //$NON-NLS-1$
	private String uploadDir = null;
	private String suffix = "FP_SUF";                                   //$NON-NLS-1$

	/********************** Writer **************************************/
	@Override
	public long getSize(File t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return t.length();
	}

	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return File.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(File t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		if (!t.canRead() || t.isDirectory()) {
			if (logger.isWarnEnabled()) {
				logger.warn(Messages.getMessage("cannotUseFileAsResponse", t.getAbsoluteFile()));
			}
			throw new WebApplicationException();
		} else {
			FileInputStream fis = new FileInputStream(t);
			try {
				pipe(fis, entityStream);
			} finally {
				fis.close();
			}
		}

	}

	/********************** Reader **************************************/
	@Override
	public boolean isReadable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return type.isAssignableFrom(File.class);
	}

	@Override
	public File readFrom(Class<File> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		File dir = null;
		if (uploadDir != null) {
			dir = new File(uploadDir);
			if (!dir.exists() || !dir.isDirectory()) {
				dir = null;
				if (logger.isWarnEnabled()) {
					logger.warn(Messages.getMessage("uploadDirDoesNotExist", uploadDir)); //$NON-NLS-1$
				}
				throw new WebApplicationException();

			}
		}
		File f = File.createTempFile(prefix, suffix, dir);

		FileOutputStream fos = new FileOutputStream(f);
		try {
			pipe(entityStream, fos);
		} finally {
			fos.close();
		}
		return f;
	}

	/********************** Help methods ************************************/
	private void pipe(InputStream is, OutputStream os) throws IOException {
		byte[] ba = new byte[1024];
		int i = is.read(ba);
		while (i != -1) {
			os.write(ba, 0, i);
			i = is.read(ba);
		}
	}

	/********************** getters & setters *********************************/
	public String getPrefix() {
		return prefix;
	}

	/**
	 * set the prefix of the uploaded files
	 *
	 * @return
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	/**
	 * set the directory where this provider save the upload files
	 *
	 * @param uploadDir
	 */
	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getSuffix() {
		return suffix;
	}

	/**
	 * set the suffix of the uploaded files
	 *
	 * @return
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}
