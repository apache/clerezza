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
 * @author reto
 * 
 */
public interface ParameterCollection extends
        java.util.Collection<KeyValuePair<ParameterValue>> {

    /**
     * @return the parameter names in the order they first appear in the request
     */
    public String[] getParameterNames();

    /**
     * 
     * @param parameterName
     *            the name of the parameter
     * @return an array with the values of the parameter or null if the
     *         parameter is not present
     */
    public ParameterValue[] getParameteValues(String parameterName);

}
