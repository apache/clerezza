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
package org.apache.clerezza.platform.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.content.DiscobitsHandler;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.ontologies.SCRIPT;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This class executes scripts, binds {@code ScriptEngineFactory}ies
 * of installed {@code ScriptEngine}s.
 *
 * It provides a facade to the installed {@code ScriptEngine}s
 * that implement {@code javax.script.ScriptEngine}.
 *
 * The services {@code ContentGraphProvider}, {@code DiscobitsHandler}, and
 * {@code TcManager} are provided to the ScriptEngine's eval method through
 * the parameter {@code Bindings}. In a script, they are accessible under the
 * names contentGraphProvider, contentHandler, and tcManager respectively.
 *
 * @scr.component
 * @scr.service
 *		interface="org.apache.clerezza.platform.scripting.ScriptExecution"
 * @scr.reference name="scriptEngineFactory" cardinality="0..n"
 *		interface="javax.script.ScriptEngineFactory"
 *
 * @author hasan, daniel
 *
 * @see javax.script.ScriptEngineManager
 * @see javax.script.ScriptEngine
 */
public class ScriptExecution {

	/**
	 * @scr.reference
	 */
	private ContentGraphProvider cgProvider;
	
	/**
	 * @scr.reference
	 */
	private DiscobitsHandler contentHandler;

	/**
	 * @scr.reference
	 */
	private TcManager tcManager;
	
	/**
	 * @scr.reference
	 */
	private Parser parser;

	private Map<ScriptLanguageDescription, List<ScriptEngineFactory>>
			languageToFactoryMap =
			new HashMap<ScriptLanguageDescription, List<ScriptEngineFactory>>();

	private static final Logger logger = LoggerFactory.getLogger(ScriptExecution.class);

	/**
	 * @see #execute(org.apache.clerezza.rdf.core.NonLiteral, javax.script.Bindings)
	 */
	public Object execute(NonLiteral scriptResource)
			throws ScriptException, NoEngineException {
		return execute(scriptResource, null);
	}

	/**
	 * Executes the specified {@code scriptResource}.
	 *
	 * The output is either a GraphNode or a response with
	 * the media type specified by the producedType property
	 * of the associated script.
	 *
	 * GraphNodes can be rendered by Renderlets registered for the type
	 * of the GraphNode.
	 *
	 * @param scriptResource
	 *				The resource (URI).
	 * @param bindings
	 *				Name/Value pairs of Java {@code Object}s made accessible
	 *				to script
	 * @return
	 *				The value returned from the execution of the script.
	 *				Either a GraphNode or a Response.
	 * @throws ScriptException
	 *				If an error occurrs in the script.
	 * @throws NoEngineException
	 *				If no engine can be found
	 *
	 * @see org.apache.clerezza.rdf.ontologies.SCRIPT#Script
	 * @see org.apache.clerezza.rdf.ontologies.SCRIPT#producedType
	 * @see org.apache.clerezza.platform.typerendering.Renderlet
	 */
	public Object execute(NonLiteral scriptResource, Bindings bindings)
			throws ScriptException, NoEngineException {

		MGraph contentGraph = cgProvider.getContentGraph();

		String scriptString = null;
		String scriptLanguage = null;
		String scriptLanguageVersion = null;
		String scriptProducedType = "text/plain";

		GraphNode scriptNode = new GraphNode(scriptResource, contentGraph);
		Iterator<Resource> it = scriptNode.getObjects(SCRIPT.scriptLanguage);
		scriptLanguage = LiteralFactory.getInstance().createObject(
						String.class, (TypedLiteral) it.next());

		it = scriptNode.getObjects(SCRIPT.scriptLanguageVersion);
		scriptLanguageVersion = LiteralFactory.getInstance().createObject(
						String.class, (TypedLiteral) it.next());

		it = scriptNode.getObjects(SCRIPT.producedType);
		if(it.hasNext()) {
			scriptProducedType = LiteralFactory.getInstance().createObject(
							String.class, (TypedLiteral) it.next());
		}

		scriptString = new String(
				contentHandler.getData((UriRef) scriptResource));

		Object result = execute(
				scriptString, bindings, scriptLanguage, scriptLanguageVersion);
		if (result instanceof GraphNode) {
			return result;
		}
		return Response.ok(result, MediaType.valueOf(scriptProducedType)).build();
	}

	/**
	 * Executes the specified {@code script} with the {@code ScriptEngine}
	 * that is registered for {@code language} and {@code languageVersion}.
	 * <p>
	 *	The default {@link ScriptContext} for the {@link ScriptEngine} is used.
	 *  It uses {@code bindings} as the {@link ScriptContext#ENGINE_SCOPE}
	 *	Bindings of the ScriptEngine during the script execution.
	 *  Three new attributes are added to the bindings: contentGraphProvider,
	 *  contentHandler, and tcManager.
	 *	The Reader, Writer and non-ENGINE_SCOPE Bindings of the default
	 *	ScriptContext are used. The ENGINE_SCOPE Bindings of the ScriptEngine
	 *	is not changed, and its mappings are unaltered by the script execution.
	 * </p>
	 *
	 * @param script
	 *				The script language source to be executed.
	 * @param bindings
	 *				 The Bindings of attributes to be used for script execution.
	 * @param language
	 *				The script language.
	 *				It is used to determine the ScriptEngine.
	 * @param languageVersion
	 *				The version of the script language.
	 *				It is used to determine the ScriptEngine.
	 * @return
	 *				The value returned from the execution of {@code script}.
	 * @throws ScriptException
	 *				If an error occurrs in the script.
	 * @throws NoEngineException
	 *				If no engine can be found for the specified language
	 *
	 * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.Bindings)
	 */
	public Object execute(String script, Bindings bindings,
			String language, String languageVersion)
			throws ScriptException, NoEngineException {
		ScriptEngine engine = getScriptEngine(language, languageVersion);
		if (engine == null) {
			logger.warn("Cannot execute script: " +
					"No engine available for language {}({})",
					language, languageVersion);
			throw new NoEngineException(new ScriptLanguageDescription(language,
					languageVersion));
		}
		if (bindings == null) {
			bindings = engine.createBindings();
		}
		addBindings(bindings);
		return engine.eval(script, bindings);
	}

	private void addBindings(Bindings bindings) {
		bindings.put("contentGraphProvider", cgProvider);
		bindings.put("contentHandler", contentHandler);
		bindings.put("tcManager", tcManager);
		bindings.put("parser", parser);
	}

	private ScriptEngine getScriptEngine(String language, String languageVersion) {
		List<ScriptEngineFactory> factoryList = null;

		try {
			factoryList = languageToFactoryMap.get(
					new ScriptLanguageDescription(language, languageVersion));
		} catch (IllegalArgumentException ex) {
			return null;
		}

		if (factoryList == null || factoryList.isEmpty()) {
			return null;
		}
		return factoryList.get(factoryList.size() - 1).getScriptEngine();
	}

	/**
	 * Returns all installed script language descriptions.
	 *
	 * @return
	 *				All script language descriptions
	 *				of the currently installed script engines.
	 *
	 * @see ScriptLanguageDescription
	 */
	public Iterator<ScriptLanguageDescription> getInstalledScriptLanguages() {
		return languageToFactoryMap.keySet().iterator();
	}

	/**
	 * Binds a ScriptEngineFactory.
	 * If script engine factories for the same language and/or language version
	 * already exist, they are shadowed.
	 *
	 * @param factory
	 *		the script engine factory to bind
	 */
	protected void bindScriptEngineFactory(ScriptEngineFactory factory) {
		String language = factory.getLanguageName();
		String languageVersion = factory.getLanguageVersion();

		addScriptEngineFactory(
				new ScriptLanguageDescription(language, languageVersion),
				factory);
	}

	private void addScriptEngineFactory(ScriptLanguageDescription language, 
			ScriptEngineFactory factory) {

		List<ScriptEngineFactory> factoryList = languageToFactoryMap.get(language);
		if (factoryList == null) {
			factoryList = new ArrayList<ScriptEngineFactory>();
		} else {
			if (factoryList.contains(factory)) {
				return;
			}
		}
		factoryList.add(factory);
		languageToFactoryMap.put(language, factoryList);
	}

	/**
	 * Unbinds a ScriptEngineFactory.
	 *
	 * @param factory
	 *		the script engine factory to unbind
	 */
	protected void unbindScriptEngineFactory(ScriptEngineFactory factory) {
		String language = factory.getLanguageName();
		String languageVersion = factory.getLanguageVersion();

		removeScriptEngineFactory(
				new ScriptLanguageDescription(language, languageVersion),
				factory);
	}

	private void removeScriptEngineFactory(ScriptLanguageDescription language, 
			ScriptEngineFactory factory) {

		List<ScriptEngineFactory> factoryList = languageToFactoryMap.get(language);
		if (factoryList != null) {
			factoryList.remove(factory);
			if (factoryList.isEmpty()) {
				languageToFactoryMap.remove(language);
			}
		}
	}
}
