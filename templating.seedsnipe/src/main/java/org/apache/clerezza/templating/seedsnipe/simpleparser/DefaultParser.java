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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;

/**
 * Default parser for parsing text with 
 * FreeMarker (http://freemarker.sourceforge.net/) style tags.
 * 
 * <p>
 *  This parser takes text input from a <code>java.io.Reader</code> 
 *	and writes text output to a <code>java.io.Writer</code>.
 * </p>
 * 
 * @author reto
 * @author daniel
 */
public class DefaultParser {

	private final static char[] beginCodeSection = {'$', '{'};
	private final static char[] endCodeSection = {'}'};
	private final static Hashtable<String, KeywordResolver> keywords = new Hashtable<String, KeywordResolver>();
	private final static String errorBegin = "<!--";
	private final static String errorEnd = "-->";
	private Reader in;
	private Writer out;


	/**
	 * Creates and initializes a new Parser.
	 * 
	 * @param in  Reader from which the parser takes its input.
	 * @param out  Writer to which the parser writes its output.
	 * @param dataFieldResolver  data model of the input.
	 */
	public DefaultParser(Reader in, Writer out) {

		this.in = new MultiMarkLineNumberReader(in);
		this.out = out;

		// add keywords and resolvers
		DefaultParser.keywords.put("loop", new LoopKeywordResolver());
		DefaultParser.keywords.put("if", new IfKeywordResolver());
	}

	/**
	 * Starts the parsing procedure.
	 * 
	 * @see DefaultParser#perform(Writer, String[], int[])
	 */
	public void perform(DataFieldResolver dataFieldResolver) throws IOException {
		perform(out, new String[0], new int[0], dataFieldResolver);

	}

	/**
	 * Parses the input string.
	 * 
	 * <p>
	 * DataFields are resolved using this parser's dataFieldResolver, keyword 
	 * resolution is delegated to a <code>KeywordResolver</code>. The input that
	 * is not withing a tag (keyword or datafield) is written directly to 
	 * <code>out</code> unless it is consumed by a <code>KeywordResolver</code>.
	 * </p>
	 * <p>
	 * 	<b>NOTE:</b> For the initial start of the parsing use <code>perform()</code>
	 * </p>
	 * <p>
	 * 	The arrayPositioner holds the postions to be used for resolving
	 * multi-dimensional fields. E.g. in a loop construct
	 * <code>arrayPositioner[0]</code> will hold the iteration (starting from 0)
	 * of the outer most loop, <code>arrayPositioner[1]</code> holds the iteration
	 * of the first inner loop.
	 * </p>
	 * 
	 * @param out  the current output reader.
	 * @param endMarkers  the currently expected endMarkers.
	 * @param arrayPositioner  the current arrayPositioner.
	 * @return the reached end-marker if one has been reached, null otherwise
	 *         (i.e. when the end of in has been reached)
	 * 
	 * @see #resolve(String, Reader, Writer, DataFieldResolver, String[], int[])
	 */
	public String perform(Writer out, String[] endMarkers, int[] arrayPositioner,
			DataFieldResolver dataFieldResolver) throws IOException {


		//The position of the char in beginCodeSection[] or endCodeSection[] we
		//look for when parsing for keywords.
		int evaluatingSectionChangePosition = 0;

		// is the next character escaped?
		boolean escapedChar = false;
		// is the following input part of a keyword or its parameters?
		boolean writingCode = false;

		StringWriter codeSection = new StringWriter(); // saves the parsed
		// keyword and
		// parameters.

		try {

			MAIN:
			while (true) {
				if (!writingCode) { // parsing normal text
					int c = in.read();

					if (c == -1) {
						return null; // end of stream
					}

					if (escapedChar) { // the current character has been
						// escaped.
						if (c == beginCodeSection[0]) {
							if (out != null) {
								out.write(beginCodeSection[0]);
							}
							escapedChar = false;
							continue MAIN;
						}
						if (c == '\\') { // backslash
							if (out != null) {
								out.write('\\');
							}
							escapedChar = false;
							continue MAIN;
						}
						// Not a known escape sequence
						escapedChar = false;
						if (out != null) {
							// write backslash to output so
							// it remains escaped for the next interpreter.
							// (e.g. \n or \t)
							out.write('\\');
						}
					}
					// possible introduction of a keyword
					if (c == beginCodeSection[evaluatingSectionChangePosition]) {
						evaluatingSectionChangePosition++;
						if (evaluatingSectionChangePosition == beginCodeSection.length) {
							// verified that the next characters
							// are to be interpreted as a keyword
							writingCode = true;
							evaluatingSectionChangePosition = 0;
						}
						continue MAIN;
					} else { // not a keyword introducing sequence
						if (evaluatingSectionChangePosition > 0) {
							// there was part of a keyword introducing sequence
							// just before.
							if (out != null) {
								out.write(beginCodeSection, 0,
										evaluatingSectionChangePosition);
							}
							evaluatingSectionChangePosition = 0;
						}
					}
					// if the read character is a backslash the following
					// character is not to be interpreted as normal input.
					if (c == '\\') {
						escapedChar = true;
						continue MAIN;
					}

					// not escaped, not a keyword introduction, just plain
					// text
					if (out != null) {
						out.write(c);
					}
				} else { // writingCode - parsing for a keyword and
					// parameters
					int c;
					c = in.read();

					if (c == -1) { // nothing could be read
						return null;
					}

					if (c == endCodeSection[evaluatingSectionChangePosition]) {
						evaluatingSectionChangePosition++;
						if (evaluatingSectionChangePosition == endCodeSection.length) {
							try {
								String codeSectionString = codeSection.toString();
								codeSection = new StringWriter();

								//resolve the parsed keyword or datafield
								final String reachedEndMarker = resolve(codeSectionString, in, out,
										dataFieldResolver, endMarkers,
										arrayPositioner);
								if (reachedEndMarker != null) {
									return reachedEndMarker;
								}

							} catch (IOException ex) {
								throw new RuntimeException(ex.toString());
							}
							writingCode = false;
							evaluatingSectionChangePosition = 0;
						}
						continue MAIN;
					} else {
						if (evaluatingSectionChangePosition > 0) { //interpret keyword as
							codeSection.write(endCodeSection, 0, //normal text and write
									evaluatingSectionChangePosition);
							evaluatingSectionChangePosition = 0;
						}
					}
					codeSection.write(c);
				}

			}
		} finally {
			try {
				if (out != null) {
					out.flush();
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex.toString());
			}
		}
	}

	/**
	 * Handles parsed keywords, end markers and fields.
	 * 
	 * @param code  the parsed keyword, end marker or field with parameters.
	 * @param in  the current reader.
	 * @param out  the current writer.
	 * @param dataFieldResolver  the data model.
	 * @param endMarkers  the current end markers.
	 * @param arrayPositioner  the current loop variables.
	 * @return the reached end-marker if <code>code</code> starts with one, null otherwise
	 * 
	 * @throws IOException  Thrown if there is an problem with the Writer.
	 */
	private String resolve(String code, Reader in, Writer out,
			DataFieldResolver dataFieldResolver, String[] endMarkers,
			int[] arrayPositioner) throws IOException {
		StringTokenizer stringTokens = new StringTokenizer(code, " \t()-\n\r");
		String firstWord;
		try {
			firstWord = stringTokens.nextToken();
		} catch (java.util.NoSuchElementException ex) {
			throw new RuntimeException("Syntax error");
		}
		for (int i = 0; i < endMarkers.length; i++) {
			if (endMarkers[i].equals(firstWord)) {
				return firstWord;
			}
		}

		if (keywords.containsKey(firstWord)) { //firstWord is a keyword
			int beginParameters = code.indexOf(firstWord) + firstWord.length();
			String parameters;
			if (beginParameters < code.length()) {
				parameters = code.substring(beginParameters); //extract parameters
			} else {
				parameters = "";
			}
			try {
				//resolve the keyword with the mapped keyword resolver.
				keywords.get(firstWord).resolve(this, parameters, in, out, dataFieldResolver,
						arrayPositioner);
			} catch (Exception ex) {
				ex.printStackTrace();
				out.write("<--! exception in keywordResolver: " + ex.toString() + "-->");
			}
		} else {
			if (out != null) {
				try {
					Object resolvedField = dataFieldResolver.resolve(code,
							arrayPositioner); //resolve a field
					if (!(resolvedField instanceof Class)) {
						out.write(resolvedField.toString());
					}

				} catch (FieldDoesNotHaveDimensionException ex) {
					out.write(ex.getSolutionObtainedReducingDimensions().toString());
				} catch (FieldIndexOutOfBoundsException ex) {
					out.write(errorBegin + " could not resolve: " + code + " (" + ex + ") " + errorEnd);
				} catch (InvalidElementException ex) {
					out.write(errorBegin + " could not resolve: " + code + " (" + ex + ") " + errorEnd);
				}
			}
		}
		return null;

	}

}