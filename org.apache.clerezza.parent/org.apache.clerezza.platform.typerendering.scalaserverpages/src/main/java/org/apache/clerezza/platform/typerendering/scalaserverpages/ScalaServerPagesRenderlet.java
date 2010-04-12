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
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import javax.script.ScriptException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.platform.typerendering.RenderingException;
import org.apache.clerezza.platform.typerendering.Renderlet;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.scala.service.CompiledScript;
import org.apache.clerezza.scala.service.ScalaService;
import org.apache.clerezza.platform.typerendering.RenderingspecificationException;
import org.apache.clerezza.platform.typerendering.TypeRenderingException;
import scala.Seq;

/**
 * 
 * Renderlet for Scala
 * 
 * @author rbn, pmg
 *  
 * @scr.component
 * @scr.service interface="org.apache.clerezza.platform.typerendering.Renderlet"
 * 
 */
public class ScalaServerPagesRenderlet implements Renderlet {

	/**
	 * @scr.reference
	 */
	private ScalaService scalaService;
	private static final Logger logger = LoggerFactory.getLogger(ScalaServerPagesRenderlet.class);
	private int byteHeaderLines = 0;

	/**
	 * Default constructor as for usage as OSGi service
	 */
	public ScalaServerPagesRenderlet() {
	}

	/**
	 * constructor used by tests
	 * 
	 * @param scalaService
	 */
	ScalaServerPagesRenderlet(ScalaService scalaService) {
		this.scalaService = scalaService;
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

	private final byte[] byteCloser = (';' + lineSeparator).getBytes();

	@Override
	public void render(GraphNode res, GraphNode context,
			CallbackRenderer callbackRenderer, URI renderingSpecification,
			String mode, MediaType mediaType, OutputStream os) throws IOException {
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
			final Map<String, Type> map = new HashMap<String, Type>();
			map.put("res", GraphNode.class);
			map.put("context", GraphNode.class);
			map.put("renderer", CallbackRenderer.class);
			map.put("mode", String.class);			
			String scriptName = extractFileName(renderingSpecification);
			logger.debug("compiling script: " + scriptName);
			final CompiledScript cs = scalaService.interpretScalaScript(
					new String(baos.toByteArray(), "UTF-8"), map, scriptName, getByteHeaderLines());			
			logger.debug("compiled");
			final Map<String, Object> values = new HashMap<String, Object>();
			values.put("res", res);
			values.put("context", context);
			values.put("renderer", callbackRenderer);
			values.put("mode", mode);
			//The priviledged block is needed because of FELIX-2273
			Object execResult = AccessController.doPrivileged(new PrivilegedAction<Object>() {

				@Override
				public Object run() {
					return cs.execute(values);
				}
			});
			os.write(toString(execResult).getBytes("UTF-8"));
			logger.debug("executed");
			os.flush();
			logger.debug("flushed");
		} catch (MalformedURLException ex) {
			throw new WebApplicationException(ex);
		} catch (ScriptException ex) {
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
}
