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
package org.apache.clerezza.integrationtest.web.framework;

/**
 * An object of this class stores the minimum, maximum, sum, and size of
 * a set of test values.
 * Each time a new test value is reported it determines the new minimum, 
 * maximum, sum, and size.
 * The test values themselves are however not stored.
 *
 * @author hasan
 * 
 * @since version 0.1
 */

class TestStatistics {

	private long min;
	private long max;
	private long sum = 0;
	private long size = 0;

	long getMax() {
		return max;
	}

	long getMin() {
		return min;
	}

	long getSize() {
		return size;
	}

	long getSum() {
		return sum;
	}

	void newTestValue(long value) {
		if (size == 0) {
			min = value;
			max = value;
		} else {
			if (max < value) {
				max = value;
			}
			if (min > value) {
				min = value;
			}
		}
		sum += value;
		size++;
	}
}
