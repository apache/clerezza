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
package org.apache.clerezza.rdf.core.sparql.query;

import java.util.ArrayList;
import java.util.List;

/**
 * This expression is intended to be used as a filter expression to test whether a graph pattern matches 
 * the dataset or not, given the values of variables in the group graph pattern in which the filter occurs.
 * It does not generate any additional bindings.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-query/#neg-pattern">SPARQL 1.1 Query Language: 8.1 Filtering Using Graph Patterns</a>
 * 
 * @author hasan
 */
public class PatternExistenceCondition extends BuiltInCall {
    private boolean negated = false;
    private GroupGraphPattern pattern;

    public PatternExistenceCondition() {
        super("EXISTS", new ArrayList<Expression>());
    }

    public PatternExistenceCondition(String name, List<Expression> arguments) {
        super(name, new ArrayList<Expression>());
        if (!(name.equalsIgnoreCase("EXISTS") || name.equalsIgnoreCase("NOT EXISTS"))) {
            throw new RuntimeException("Unsupported name: " + name);
        } else {
            this.negated = name.equalsIgnoreCase("NOT EXISTS");
        }
    }

    public boolean isExistenceTest() {
        return !negated;
    }

    public GroupGraphPattern getPattern() {
        return pattern;
    }

    public void setExistenceTest(boolean existenceTest) {
        this.negated = !existenceTest;
        this.name = existenceTest ? "EXISTS" : "NOT EXISTS";
    }

    public void setPattern(GroupGraphPattern pattern) {
        this.pattern = pattern;
    }
}
