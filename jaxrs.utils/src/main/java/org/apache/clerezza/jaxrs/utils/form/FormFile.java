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
/*
 * Teken from org.wymiwyg.wrhapi.util.parameterparse, adapted for integration in
 * JAX-RS by clerezza
 */
package org.apache.clerezza.jaxrs.utils.form;

import javax.ws.rs.core.MediaType;

/**
 * Represents a file as contained in a multipart/form-data message
 * 
 * @author reto
 * 
 */
public interface FormFile extends ParameterValue {

    /**
     * @return the content of the file
     */
    public byte[] getContent();

    /**
     * @return the filename provided by the sender or null if not supplied
     */
    public String getFileName();

    /**
     * @return the mime-type of the file
     */
    public MediaType getMediaType();

}
