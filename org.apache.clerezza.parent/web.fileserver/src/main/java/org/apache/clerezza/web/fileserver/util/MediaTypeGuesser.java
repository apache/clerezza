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
package org.apache.clerezza.web.fileserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This uses a built in mime.types file.<br/>
 * Currently it doesn't implement java.net.FileNameMap, not does it uses the
 * java specific mime.type mechanism as the URLConnection.getFileNameMap()
 * method.
 *
 * @author reto
 */
public class MediaTypeGuesser {

	static MediaTypeGuesser instance;
	private final Logger logger = LoggerFactory.getLogger(MediaTypeGuesser.class);


	/**
	 * @return The singleton instance
	 */
	public static MediaTypeGuesser getInstance() {
		if (instance == null) {
			synchronized (MediaTypeGuesser.class) {
				if (instance == null) {
					instance = new MediaTypeGuesser();
				}
			}
		}
		return instance;
	}
	
	private Map<String, MediaType> extendsion2MediaTypeMap = new HashMap();
	
	private MediaTypeGuesser() {
		try {
			Reader in = new InputStreamReader(MediaTypeGuesser.class.getResourceAsStream("mime.types"));
			BufferedReader reader = new BufferedReader(in);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == '#') {
					continue;
				}
				StringTokenizer tokens = new StringTokenizer(line);
				String mimeTypeString = tokens.nextToken();
				MediaType mediaType = MediaType.valueOf(mimeTypeString);
				while (tokens.hasMoreTokens()) {
					String extension = tokens.nextToken();
					extendsion2MediaTypeMap.put(extension, mediaType);
				}
			}
		} catch (IOException ex) {
			logger.error("IOException thrown {}", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 *
	 * @param extension
	 * @return the mediaType for the specified extension or null
	 */
	public MediaType getTypeForExtension(String extension) {
		return extendsion2MediaTypeMap.get(extension);
	}

	/**
	 *
	 * @param fileName
	 * @return the guessed mediaType or null
	 */
	public MediaType guessTypeForName(String fileName) {
		int dotPos = fileName.lastIndexOf('.');
		if (dotPos == -1) {
			return null;
		}
		return getTypeForExtension(fileName.substring(dotPos+1).toLowerCase());
	}
}
