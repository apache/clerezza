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
import org.apache.clerezza.rdf.core.sparql.query.OrderCondition;
import org.apache.clerezza.rdf.core.sparql.query.QueryWithSolutionModifier;

/**
 *
 * @author hasan
 */
public abstract class SimpleQueryWithSolutionModifier extends SimpleQuery
        implements QueryWithSolutionModifier {

    private List<OrderCondition> orderConditions = new ArrayList<OrderCondition>();

    /**
     * Result offset. 0 means no offset.
     */
    private int offset = 0;

    /**
     * Result limit. -1 means no limit.
     */
    private int limit = -1;

    @Override
    public List<OrderCondition> getOrderConditions() {
        return orderConditions;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    public void addOrderCondition(OrderCondition orderCondition) {
        orderConditions.add(orderCondition);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
