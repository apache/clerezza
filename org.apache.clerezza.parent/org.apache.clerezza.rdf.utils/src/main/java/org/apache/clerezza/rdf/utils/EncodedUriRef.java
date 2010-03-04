/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.rdf.utils;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.utils.UriException;
import org.apache.clerezza.utils.UriUtil;

/**
 * Automatically escapes and encodes the uri string regarded as the path 
 * component of an URI with the default protocol charset.
 * @author mir
 */
public class EncodedUriRef extends UriRef{

	/**
	 * Creates an encoded UriRef.
	 *
	 * @param uriRefString unencoded or partly encoded uri string
	 * @throws UriException
	 */
	public EncodedUriRef(String uriRefString) throws UriException {
		super(UriUtil.encodePartlyEncodedPath(uriRefString, "UTF-8"));		
	}

	/**
	 * Creates an encoded UriRef.
	 *
	 * @param uriRefString unencoded or partly encoded uri string
     * @param charset the charset
	 * @throws UriException
	 */
	public EncodedUriRef(String uriRefString, String charset) throws UriException {
		super(UriUtil.encodePartlyEncodedPath(uriRefString, charset));
	}

	/**
	 * Creates an encoded UriRef.
	 *
	 * @param uriRef unencoded or partly encoded UriRef
	 * @throws UriException
	 */
	public EncodedUriRef(UriRef uriRef) throws UriException {
		super(UriUtil.encodePartlyEncodedPath(uriRef.getUnicodeString(), "UTF-8"));
	}

	/**
	 * Creates an encoded UriRef.
	 *
	 * @param uriRef unencoded or partly encoded UriRef
     * @param charset the charset
	 * @throws UriException
	 */
	public EncodedUriRef(UriRef uriRef, String charset) throws UriException {
		super(UriUtil.encodePartlyEncodedPath(uriRef.getUnicodeString(), charset));
	}

}
