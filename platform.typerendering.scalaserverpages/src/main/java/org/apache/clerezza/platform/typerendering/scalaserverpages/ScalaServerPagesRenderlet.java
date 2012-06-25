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

import org.apache.clerezza.platform.typerendering.*;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.scala.scripting.CompileErrorsException;
import org.apache.clerezza.scala.scripting.CompilerService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Renderlet for Scala
 * 
 * @author rbn, pmg
 *  
 * 
 */
@Component
@Service(Renderlet.class)
@Deprecated
public class ScalaServerPagesRenderlet implements Renderlet {

	final Charset UTF8 = Charset.forName("UTF-8");

	@Reference
	private CompilerService scalaCompilerService;
	private static final Logger logger = LoggerFactory.getLogger(ScalaServerPagesRenderlet.class);
	private int byteHeaderLines = 0;
	private Type multiStringObjectMapType;

	{
		try {
			multiStringObjectMapType = RequestProperties.class.getMethod("getResponseHeaders", new Class[0]).getReturnType();
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Default constructor as for usage as OSGi service
	 */
	public ScalaServerPagesRenderlet() {
	}
	private final String lineSeparator = System.getProperty("line.separator");
	private final char[] headerChars;

	{
		final Reader in = new InputStreamReader(ScalaServerPagesRenderlet.class.getResourceAsStream("implicit-header.txt"),
				UTF8);
		final CharArrayWriter caos = new CharArrayWriter();
		try {
			for (int ch = in.read(); ch != -1; ch = in.read()) {
				caos.write(ch);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		headerChars = caos.toCharArray();
	}
	private final char[] footerChars = (";}" + lineSeparator + "}"
			+ lineSeparator + "}" + lineSeparator + "}" + lineSeparator).toCharArray();
	//TODO a map with SoftReferences as keys
	private Map<String, Renderlet> compiledRenderlet = Collections.synchronizedMap(
			new HashMap<String, Renderlet>());

	@Override
	public void render(GraphNode res, GraphNode context, Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer, URI renderingSpecification,
			String mode, MediaType mediaType,
			RequestProperties requestProperties, OutputStream os) throws IOException {
		try {
			logger.debug("ScalaServerPagesRenderlet rendering");
			final char[] scriptBytes = getScriptChars(renderingSpecification);

			final Renderlet cs = getCompiledRenderlet(scriptBytes);
			cs.render(res, context, sharedRenderingValues, callbackRenderer,
					renderingSpecification, mode, mediaType, requestProperties, os);
			//os.flush();
			//logger.debug("flushed");
		} catch (MalformedURLException ex) {
			throw new WebApplicationException(ex);
		} catch (CompileErrorsException ex) {
			logger.debug("ScriptException rendering ScalaServerPage: ", ex);
			Exception cause = (Exception) ex.getCause();
			if (cause != null) {
				if (cause instanceof TypeRenderingException) {
					throw (TypeRenderingException) cause;
				}
				throw new RenderingException(cause, renderingSpecification, res, context);
			}
			throw new RenderingspecificationException(ex.getMessage(), renderingSpecification,
					0,0, res, context);
		}
	}

	private int getByteHeaderLines() {
		if (byteHeaderLines == 0) {
			int count = 0;
			LineNumberReader ln = new LineNumberReader(
					new CharArrayReader(headerChars));
			try {
				while (ln.readLine() != null) {
					count++;
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			byteHeaderLines = count;
		}
		return byteHeaderLines;
	}


	private String extractFileName(URI renderingSpecification) {
		String path = renderingSpecification.getPath();
		return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
	}

	private Renderlet getCompiledRenderlet(char[] scriptChars) throws CompileErrorsException {
		String scriptString = new String(scriptChars);
		Renderlet renderlet = compiledRenderlet.get(scriptString);
		if (renderlet == null) {
			final char[][] scipts = new char[][]{scriptChars};
			Class renderletClass;
			try {
				//doing as priviledged so that no COmpilePermission is needed
				renderletClass = AccessController.doPrivileged(new PrivilegedExceptionAction<Class>() {
					@Override
					public Class run() {
						return scalaCompilerService.compile(scipts)[0];
					}
				});

			} catch (PrivilegedActionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RuntimeException) {
					throw (RuntimeException) cause;
				}
				if (cause instanceof CompileErrorsException) {
					throw (CompileErrorsException) cause;
				}
				throw new RuntimeException(e);
			}
			try {
				renderlet = (Renderlet) renderletClass.newInstance();
			} catch (InstantiationException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
			compiledRenderlet.put(scriptString, renderlet);
		}
		return renderlet;
	}

	private char[] getScriptChars(URI renderingSpecification) throws IOException {
		final Reader in = new InputStreamReader(renderingSpecification.toURL().openStream(), UTF8);
		final CharArrayWriter caos = new CharArrayWriter();
		//Add the scriptHeader to the beginning of the script
		caos.write(headerChars);
		//add the content
		final char[] buffer = new char[1024];


		int charsRead;
		while ((charsRead = in.read(buffer, 0, 1024)) != -1) {
			caos.write(buffer, 0, charsRead);


		}
		//add the closing ";"
		caos.write(footerChars);
		String scriptName = extractFileName(renderingSpecification);
		logger.debug("getting CompiledScript for: {}", scriptName);


		return caos.toCharArray();

	}
}
