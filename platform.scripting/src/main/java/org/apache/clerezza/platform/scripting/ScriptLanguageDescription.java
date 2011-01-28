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

/**
 * This class describes a script language in terms of language name and 
 * language version.
 *
 * @author daniel
 */
public class ScriptLanguageDescription {
	private final String language;
	private final String version;

	/**
	 * Creates an new script language description.
	 *
	 * @param language  the script language.
	 * @param version  the script version.
	 * @throws IllegalArgumentException  if <code>language</code> is null.
	 */
	public ScriptLanguageDescription(String language, String version)
			throws IllegalArgumentException {
		if(language == null) {
			throw new IllegalArgumentException("Script Language can not be null");
		}
		if(version == null) {
			version = "";
		}
		this.language = language;
		this.version = version;
	}

	/**
	 * Returns the script language (e.g. "Scala").
	 *
	 * @return  the script language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Returns the script version (e.g. "1.8.6" if the described
	 * script language is "Ruby 1.8.6".
	 *
	 * @return  the script version
	 */
	public String getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		return (language + version).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass().getName().equals(this.getClass().getName())) {
			ScriptLanguageDescription sld = (ScriptLanguageDescription) obj;
			return (this.language.equals(sld.language) &&
					this.version.equals(sld.version));
		}
		return false;
	}

	@Override
	public String toString() {
		return language+" "+version;
	}


}
