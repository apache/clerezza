/*
 * Copyright  2002-2006 WYMIWYG (http://wymiwyg.org)
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
 * Represents a parameter-value. A textual parameter-value is guaranteed to have
 * the text as its toSTring-representation
 * 
 * @author reto
 * 
 */
public interface ParameterValue {

	/**
	 * @return the String-value of the paramter, for text-values this is the
	 *         text
	 */
	public String toString();
}
