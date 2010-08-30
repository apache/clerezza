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

package org.apache.clerezza.tools.offline.utils;

/**
 *
 * @author mir
 */
public interface StreamCondition {

	/**
	 * This method is called by the ConditionalOutputStream. The conditional
	 * output stream feeds the condition with bytes as long as the feed()-method
	 * returns true. If false is returned, then the condition is either satified
	 * or unsatisfied. This can be determined by calling isSatisfied() of this
	 * condition.
	 * After returning false, the condition can be fed again.
	 *
	 * @param b
	 * @return
	 */
	public boolean feed(int b);

	/**
	 * Returns true if the condition is satisfied, false otherwise.
	 * @return
	 */
	public boolean isSatisfied();

	/**
	 * The ConditionOutputStream will call this method if the condition is
	 * satisfied. The returned bytes will be written into its underlying outputstream
	 * instead of the bytes that were fed to the condition.
	 * @return
	 */
	public byte[] getBytes();
}
