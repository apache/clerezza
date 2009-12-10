/*
 * Copyright  2002-2004 WYMIWYG (www.wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.clerezza.jaxrs.utils.form;

/**
 * Represents a multipart/form-data message as specified by RFC 2388. It
 * currently supports only files and fields (text), as no implementation is
 * known to support file lists or external references
 * 
 * @author reto
 * 
 */
public interface MultiPartBody extends ParameterCollection {

	/**
	 * @return the file parameter names in the order they appear in the message
	 */
	public abstract String[] getFileParameterNames();

	/**
	 * @param name
	 *            the name of the parameter
	 * @return the values of the parameter
	 */
	public abstract FormFile[] getFormFileParameterValues(String name);

	/**
	 * @return all parameter names in the order they appear in the message
	 */
	public abstract String[] getParameterNames();

	/**
	 * @return the text parameter names in the order they appear in the message
	 */
	public abstract String[] getTextParameterNames();

	/**
	 * @param name
	 *            the name of the parameter
	 * @return the values of the parameter
	 */
	public abstract String[] getTextParameterValues(String name);

}