/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.clerezza.commons.rdf.impl.utils;

import org.apache.clerezza.commons.rdf.Literal;

/**
 *
 * @author developer
 */
public abstract class AbstractLiteral implements Literal {

    @Override
    public int hashCode() {
        int result = 0;
        if (getLanguage() != null) {
            result = getLanguage().hashCode();
        }
        result += getLexicalForm().hashCode();
        result += getDataType().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Literal) {
            Literal other = (Literal) obj;
            
            if (getLanguage() == null) {
                if (other.getLanguage() != null) {
                    return false;
                }
            } else {
                if (!getLanguage().equals(other.getLanguage())) {
                    return false;
                }
            }
            boolean res = getDataType().equals(other.getDataType()) && getLexicalForm().equals(other.getLexicalForm());
            return res;
        } else {
            return false;
        }
    }
    
}
