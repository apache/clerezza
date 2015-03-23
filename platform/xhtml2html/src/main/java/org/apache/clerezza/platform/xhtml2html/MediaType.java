/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.platform.xhtml2html;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MediaType {
    static MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application", "xhtml+xml", null);
    static final MediaType WILDCARD_TYPE = new MediaType("*", "*", null);
    static final String MEDIA_TYPE_WILDCARD = "*";
    static MediaType TEXT_HTML_TYPE = new MediaType("text", "html", null);

    static MediaType valueOf(String string) {
        String[] parts = string.split(";");
        MediaType result = valueOfParamLess(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            parseParam(parts[i], result);
        }
        return result;
    }
    
    static MediaType valueOfParamLess(String string) {
        String[] parts = string.split("/");
        Map<String, String> parameters = new HashMap<String, String>();
        MediaType result = new MediaType(parts[0], parts[1], parameters);
        return result;
    }

    private static void parseParam(String string, MediaType result) {
        String[] parts = string.split("=");
        result.parameters.put(parts[0], parts[1]);
    }
    
    private final String type;
    private final String subtype;
    private Map<String, String> parameters;

    MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    Map<String,String> getParameters() {
        return parameters == null? Collections.EMPTY_MAP : parameters;
    }

    String getType() {
        return type;
    }

    String getSubtype() {
        return subtype;
    }

    /**
     * for now parameters are ignored
     */
	public boolean isCompatible(MediaType other) {
		if (other == null) {
			return false;
		}
		if (isWildcardType() || other.isWildcardType()) {
			return true;
		}
        if (!type.equals(other.getType())) {
            return false;
        }
        if (isWildcardSubtype() || other.isWildcardSubtype()) {
            return true;
        }
        return subtype.equals(other.subtype);
	}

    private boolean isWildcardSubtype() {
        return subtype.equals("*");
    }
    private boolean isWildcardType() {
        return type.equals("*");
    }
    
}
