package org.apache.clerezza.rdf.core.access;
/*
 *
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
 *
*/


import java.util.Comparator;

/**
 * Compares the WeightedTcManagementProviders, descending for weight and
 * ascending by name
 */
public class WeightedProviderComparator implements Comparator<WeightedTcProvider> {

    @Override
    public int compare(WeightedTcProvider o1, WeightedTcProvider o2) {
        int o1Weight = o1.getWeight();
        int o2Weight = o2.getWeight();
        if (o1Weight != o2Weight) {
            return o2Weight - o1Weight;
        }
        return o1.getClass().toString().compareTo(o2.getClass().toString());
    }
}
