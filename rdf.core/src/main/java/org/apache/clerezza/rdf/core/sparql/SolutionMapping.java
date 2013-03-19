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
package org.apache.clerezza.rdf.core.sparql;

import org.apache.clerezza.rdf.core.sparql.query.Variable;
import java.util.Map;
import org.apache.clerezza.rdf.core.Resource;

/**
 * A set of mapping from variable names to solutions.
 *
 * a variable name has the form: ( PN_CHARS_U | [0-9] ) ( PN_CHARS_U | [0-9]
 * | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040] )*
 * where PN_CHARS_U =      [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6]
 * | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D]
 * | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF]
 * | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF] | '_'
 *
 *
 * @author rbn
 */
public interface SolutionMapping extends Map<Variable, Resource> {

    /**
     * Should be the equivalent to this:
     * public Resource get(String name) {
     *    return get(new Variable(name));
     * }
     *
     * @param name
     * @return
     */
    public Resource get(String name);
}
