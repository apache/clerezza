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
package org.apache.clerezza.rdf.core.sparql.query.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.sparql.query.InlineData;
import org.apache.clerezza.rdf.core.sparql.query.Variable;

/**
 *
 * @author hasan
 */
public class SimpleInlineData implements InlineData {

    private List<Variable> variables = new ArrayList<Variable>();
    private List<List<Resource>> values = new ArrayList<List<Resource>>();

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public List<List<Resource>> getValues() {
        return values;
    }

    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    public void addValues(List<Resource> values) {
        this.values.add(values);
    }
}
