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
package org.apache.clerezza.rdf.core.sparql.update.impl;

/**
 * The ADD operation is a shortcut for inserting all data from an input graph into a destination graph. 
 * Data from the input graph is not affected, and initial data from the destination graph, if any, is kept intact.
 * @see <a href="http://www.w3.org/TR/2013/REC-sparql11-update-20130321/#add">SPARQL 1.1 Update: 3.2.5 ADD</a>
 * 
 * @author hasan
 */
public class AddOperation extends SimpleUpdateOperation {
}
