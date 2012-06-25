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
package org.apache.clerezza.platform.typerendering.scalaserverpages;

import java.util.logging.Level;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.platform.typerendering.TypeRenderlet;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.scala.scripting.CompilerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Map;

/**
 * A TypeRenderlet delegating the actual renderlet to a compiled ScalaServerPage. On every request the
 * ScalaServerPage is checked for changes and recompiled if needed.
 */
public class SspTypeRenderlet implements TypeRenderlet {


	private static final Logger logger = LoggerFactory.getLogger(ScalaServerPagesRenderlet.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final char[] headerChars
			= getChars(SspTypeRenderlet.class.getResource("typerenderlet-header.txt"));
	private static final String lineSeparator = System.getProperty("line.separator");
	private static final char[] footerChars = (";}" + lineSeparator + "}"
			+ lineSeparator + "}" + lineSeparator + "}" + lineSeparator).toCharArray();
	

	private UriRef rdfType;
	private String modePattern;
	private MediaType mediaType;
	private URL sspLocation;
	private char[] lastCompiledChars;
	private TypeRenderlet lastCompiledSsp = null;
	private CompilerService scalaCompilerService;

	SspTypeRenderlet(URL sspLocation, UriRef rdfType, String modePattern, MediaType mediaType,
					 CompilerService scalaCompilerService) {
		this.sspLocation = sspLocation;
		this.rdfType = rdfType;
		this.modePattern = modePattern;
		this.mediaType = mediaType;
		this.scalaCompilerService = scalaCompilerService;
	}

	@Override
	public UriRef getRdfType() {
		return rdfType;
	}

	@Override
	public String getModePattern() {
		return modePattern;
	}

	@Override
	public MediaType getMediaType() {
		return mediaType;
	}

	@Override
	public void render(GraphNode node, GraphNode context,
					   Map<String, Object> sharedRenderingValues, CallbackRenderer callbackRenderer,
					   RequestProperties requestProperties, OutputStream os) throws IOException {
		TypeRenderlet compiledSsp = getCompiledSsp();
		compiledSsp.render(node, context, sharedRenderingValues, callbackRenderer, requestProperties, os);
	}

	private synchronized TypeRenderlet getCompiledSsp()  {
		char[] scriptChars = getScriptChars(sspLocation);
		if (Arrays.equals(scriptChars, lastCompiledChars)) {
			return lastCompiledSsp;
		}
		final char[][] scripts = new char[][]{scriptChars};
		Class renderletClass;
		try {
			//doing as priviledged so that no CompilePermission is needed
			renderletClass = AccessController.doPrivileged(new PrivilegedExceptionAction<Class>() {
				@Override
				public Class run() {
					return scalaCompilerService.compile(scripts)[0];
				}
			});

		} catch (PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			/*if (cause instanceof CompileErrorsException) {
				throw (CompileErrorsException) cause;
			}*/
			throw new RuntimeException(e);
		}
		TypeRenderlet compiledSsp;
		try {
			compiledSsp = (TypeRenderlet) renderletClass.newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		lastCompiledSsp = compiledSsp;
		lastCompiledChars = scriptChars;
		return compiledSsp;
	}

	private static char[] getScriptChars(URL location) {
		try {
			final CharArrayWriter caos = new CharArrayWriter();
			//Add the scriptHeader to the beginning of the script
			caos.write(headerChars);
			//add the content
			caos.write(getChars(location));
			caos.write(footerChars);
			return caos.toCharArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static char[] getChars(URL location) {
		try {
			final Reader in = new InputStreamReader(location.openStream(), UTF8);
			final CharArrayWriter caos = new CharArrayWriter();
			
			final char[] buffer = new char[1024];
			int charsRead;
			while ((charsRead = in.read(buffer, 0, 1024)) != -1) {
				caos.write(buffer, 0, charsRead);
			}
			logger.debug("getting bytes for: {}", location);
			return caos.toCharArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "SspTypeRenderlet for: " + sspLocation;
	}


}
