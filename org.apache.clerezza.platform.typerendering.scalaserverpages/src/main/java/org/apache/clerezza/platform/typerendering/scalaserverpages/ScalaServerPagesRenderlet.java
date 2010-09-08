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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.platform.typerendering.RenderingException;
import org.apache.clerezza.platform.typerendering.Renderlet;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.platform.typerendering.RenderingspecificationException;
import org.apache.clerezza.platform.typerendering.TypeRenderingException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import scala.collection.Seq;

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
public class ScalaServerPagesRenderlet implements Renderlet {


	@Reference(target="(javax.script.language=scala)")
	private ScriptEngineFactory scalaScriptEngineFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(ScalaServerPagesRenderlet.class);
	private int byteHeaderLines = 0;
	private Type multiStringObjectMapType;
	{
		try {
			multiStringObjectMapType = RequestProperties.class.getMethod("getHttpHeaders", new Class[0]).getReturnType();
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
	private final byte[] byteHeader;
	{
		final InputStream in = ScalaServerPagesRenderlet.class.getResourceAsStream("implicit-header.txt");
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for (int ch = in.read(); ch != -1; ch = in.read()) {
				baos.write(ch);
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
		byteHeader = baos.toByteArray();
	}

	private final byte[] byteCloser = (";}" + lineSeparator).getBytes();

	//TODO a map with SoftReferences as keys
	private Map<String, CompiledScript> compiledScripts = new HashMap<String, CompiledScript>();
	
	@Override
	public void render(GraphNode res, GraphNode context, Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer, URI renderingSpecification,
			String mode, MediaType mediaType,
			RequestProperties requestProperties, OutputStream os) throws IOException {
		try {
			logger.debug("ScalaServerPagesRenderlet rendering");
			final InputStream in = renderingSpecification.toURL().openStream();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//Add the scriptHeader to the beginning of the script
			baos.write(byteHeader);
			//add the content
			for (int b = in.read(); b != -1; b = in.read()) {
				baos.write(b);
			}
			//add the closing ";" 
			baos.write(byteCloser);
			String scriptName = extractFileName(renderingSpecification);
			logger.debug("getting CompiledScript for: {}", scriptName);
			final byte[] scriptBytes = baos.toByteArray();
			final CompiledScript cs = getCompiledScript(scriptBytes);
			
			final SimpleBindings values = new SimpleBindings();
			values.put("res", res);
			values.put("context", context);
			values.put("renderer", callbackRenderer);
			values.put("mode", mode);
			values.put("sharedRenderingValues", sharedRenderingValues);
			if (requestProperties != null) {
				values.put("uriInfo", requestProperties.getUriInfo());
				//values.put("httpHeaders", requestProperties.getHttpHeaders());
			}
			//The priviledged block is needed because of FELIX-2273
			Object execResult = null;
			try {
				execResult = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

					@Override
					public Object run() throws ScriptException {
						return cs.eval(values);
					}
				});
			} catch (PrivilegedActionException ex) {
				Exception cause = (Exception) ex.getCause();
				logger.debug("Exception executing ScalaServerPage Script", cause);
				if (cause instanceof ScriptException) {
					throw (ScriptException) cause;
				}
				throw new RuntimeException(cause);
 			} catch (RuntimeException ex) {
				logger.debug("RuntimeException executing ScalaServerPage Script", ex);
				throw ex;
			}
			if (execResult != null) {
				String sspResult = toString(execResult);
				logger.debug("executed ssp, result: {} (for {})", sspResult, scriptName);
				os.write(sspResult.getBytes("UTF-8"));
			}
			
			os.flush();
			logger.debug("flushed");
		} catch (MalformedURLException ex) {
			throw new WebApplicationException(ex);
		} catch (ScriptException ex) {
			logger.debug("ScriptException rendering ScalaServerPage: ", ex);
			Exception cause = (Exception) ex.getCause();
			if (cause != null) {
				if (cause instanceof TypeRenderingException) {
					throw (TypeRenderingException) cause;
				}
				throw new RenderingException(cause, renderingSpecification, res, context);
			}
			throw new RenderingspecificationException(ex.getMessage(), renderingSpecification,
					ex.getLineNumber(), ex.getColumnNumber(), res, context);
		}
	}

	private int getByteHeaderLines() {
		if (byteHeaderLines == 0) {
			int count = 0;
			LineNumberReader ln = new LineNumberReader(
					new InputStreamReader(new ByteArrayInputStream(byteHeader)));
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

	private static String toString(Object object) {
		if (object instanceof Seq) {
			return ((Seq)object).mkString();
		} else {
			return object.toString();
		}
	}

	private String extractFileName(URI renderingSpecification) {
		String path = renderingSpecification.getPath();
		return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
	}

	private CompiledScript getCompiledScript(byte[] scriptBytes) throws ScriptException {
		String scriptString;
		try {
			scriptString = new String(scriptBytes, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		CompiledScript cs = compiledScripts.get(scriptString);
		if (cs == null) {
			cs = ((Compilable)scalaScriptEngineFactory.getScriptEngine())
					.compile(scriptString);
			compiledScripts.put(scriptString, cs);
		}
		return cs;
	}



}
