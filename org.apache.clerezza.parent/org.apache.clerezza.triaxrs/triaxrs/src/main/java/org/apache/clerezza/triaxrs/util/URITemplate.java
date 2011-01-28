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
package org.apache.clerezza.triaxrs.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.clerezza.utils.UriException;

public class URITemplate implements Comparable<URITemplate> {

	private class TemplateSection {
		public TemplateSection(String value, boolean variable) {
			this.variable = variable;
			if (variable) {
				int colonPos = value.indexOf(':');
				if (colonPos != -1) {
					String regexString = value.substring(colonPos + 1);
					value = value.substring(0, colonPos);
					pattern = Pattern.compile(regexString);
				}
			} else {
				literalCharacters += value.length();
			}
			this.value = value;
		}
		@Override
		public String toString() {
			if (variable) {
				return "{"+value+":"+pattern+"}";
			} else {
				return value;
			}
		}
		
		boolean variable;
		// for variables this is the name of the variable (without {)
		String value;
		Pattern pattern;
		
	}

	int literalCharacters = 0;
	int capturingGroups = 0;
	List<TemplateSection> templateSections = new ArrayList<TemplateSection>();
	private String templateString;

	public URITemplate(String rawTemplateString) {
		try {
			this.templateString = TemplateEncoder.encode(rawTemplateString, "UTF-8");
		} catch (UriException ex) {
			throw new RuntimeException(ex);
		}
		StringReader stringReader = new StringReader(templateString);
		boolean readingVariableName = false;
		StringWriter sectionWriter = new StringWriter();
		
		try {
			boolean ommitNextCharIfSlash = true;
			for (int ch = stringReader.read(); ch != -1; ch = stringReader
					.read()) {

				if (ch == '{') {
					if (readingVariableName) {
						throw new RuntimeException("{ in variable name");
					}
					String sectionString = sectionWriter.toString();
					if (sectionString.length() > 0) {
						if (!sectionString.equals("/")) {
							templateSections.add(new TemplateSection(
									sectionString, false));
						}
						sectionWriter = new StringWriter();
					}
					readingVariableName = true;
				} else {
					if (ch == '}') {
						if (!readingVariableName) {
							throw new RuntimeException("unbalanced }");
						}
						String sectionString = sectionWriter.toString();
						templateSections.add(new TemplateSection(sectionString,
								true));
						capturingGroups++;
						sectionWriter = new StringWriter();
						readingVariableName = false;
						ommitNextCharIfSlash = true;
					} else {
						if (ommitNextCharIfSlash) {
							ommitNextCharIfSlash = false;
							if (ch == '/') continue;
						}
						sectionWriter.write(ch);
					}

				}
			}
			if (readingVariableName) {
				throw new RuntimeException("unterminated variable name");
			}
			String lastSection = sectionWriter.toString();
			if (lastSection.length() > 0) {
				templateSections.add(new TemplateSection(lastSection, false));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @param uriPath
	 * @return a instance of PathMatching allowing access to params and
	 *         remaining path or null if uriPath didn't match
	 */
	public PathMatching match(final String uriPath) {
		Map<String, String> parameters = new HashMap<String, String>();
		// StringReader uriPathReader = new StringReader(uriPath);
		String remaingUriPath;
		if ((uriPath.length() > 0) && (uriPath.charAt(0) == '/')) {
			remaingUriPath = uriPath.substring(1);
		} else {
			remaingUriPath = uriPath;
		}
		for (TemplateSection templateSection : templateSections) {
			int usedTillPos = handleSection(templateSection, remaingUriPath,
					parameters);
			if (usedTillPos == -1) {
				return null;
			}
			if (remaingUriPath.length() > usedTillPos) {
				if (remaingUriPath.charAt(usedTillPos) == '/') {
					usedTillPos++;
				}
			}
			remaingUriPath = remaingUriPath.substring(usedTillPos);
		}
		return new PathMatching(parameters, remaingUriPath);
	}

	/**
	 * @return if the template isn't matched -1, otherwise the position till
	 *         witch subPath is matched
	 */
	private int handleSection(TemplateSection templateSection, String subPath,
			Map<String, String> result) {
		if (templateSection.variable) {
			if (templateSection.pattern == null) {
				StringWriter valueWriter = new StringWriter();
				int i;
				for (i = 0; i < subPath.length(); i++) {
					int ch = subPath.charAt(i);
					if (ch == '/') {
						break;
					}
					valueWriter.write(ch);
				}
				result.put(templateSection.value, valueWriter.toString());
				return i;
			} else {
				Matcher matcher = templateSection.pattern.matcher(subPath);
				if (!matcher.lookingAt()) {
					return -1;
				} else {
					int end = matcher.end();
					result.put(templateSection.value, subPath.substring(0, end));
					return end;
				}
				
			}

		} else {
			if (templateSection.value.length() > subPath.length()) {
				return -1;
			}
			byte[] templateBytes = templateSection.value.getBytes();
			int i;
			byte subPathOffSet = 0;
			for (i = 0; i < subPath.length(); i++) {
				if (i >= templateBytes.length) {
					break;
				}
				int uriChar = subPath.charAt(i + subPathOffSet);
				if (uriChar == templateBytes[i]) {
					continue;
				}
				if ((i == 0) && (templateBytes[0] == '/')) {
					uriChar = subPath.charAt(1);
					if (uriChar == templateBytes[i]) {
						subPathOffSet = 1;
						continue;
					}
				}
				return -1;
			}
			return i + subPathOffSet;
		}
	}

	@Override
	public int compareTo(URITemplate o) {
		if (literalCharacters > o.literalCharacters) {
			return -1;
		} else {
			if (literalCharacters < o.literalCharacters) {
				return 1;
			}
		}
		if (capturingGroups > o.capturingGroups) {
			return -1;
		} else {
			if (capturingGroups < o.capturingGroups) {
				return 1;
			}
		}
		return templateString.compareTo(o.templateString);
	}

	public String toString() {
		return templateString;
	}

	@Override
	public boolean equals(Object obj) {
		return templateString.equals(((URITemplate)obj).templateString);
	}

	@Override
	public int hashCode() {
		return templateString.hashCode();
	}
	
	

}