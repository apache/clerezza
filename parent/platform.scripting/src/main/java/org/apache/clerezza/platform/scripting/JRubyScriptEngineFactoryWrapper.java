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

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 *
 * @author hasan
 */
public class JRubyScriptEngineFactoryWrapper implements ScriptEngineFactory {

	private ScriptEngineFactory wrappedFactory;

	public JRubyScriptEngineFactoryWrapper(ScriptEngineFactory wrappedFactory) {
		this.wrappedFactory = wrappedFactory;
	}

	@Override
	public String getEngineName() {
		return wrappedFactory.getEngineName();
	}

	@Override
	public String getEngineVersion() {
		return wrappedFactory.getEngineVersion();
	}

	@Override
	public List<String> getExtensions() {
		return wrappedFactory.getExtensions();
	}

	@Override
	public List<String> getMimeTypes() {
		return wrappedFactory.getMimeTypes();
	}

	@Override
	public List<String> getNames() {
		return wrappedFactory.getNames();
	}

	@Override
	public String getLanguageName() {
		return wrappedFactory.getLanguageName();
	}

	@Override
	public String getLanguageVersion() {
		return wrappedFactory.getLanguageVersion();
	}

	@Override
	public Object getParameter(String key) {
		return wrappedFactory.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return wrappedFactory.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return wrappedFactory.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(String... statements) {
		return wrappedFactory.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);
		ScriptEngine engine = wrappedFactory.getScriptEngine();
		Thread.currentThread().setContextClassLoader(oldClassLoader);
		return engine;
	}
}
