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
package org.apache.clerezza.platform.dashboard.blackbox.osgi;

import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;

/**
 *
 * @author mir
 */
public class GlobalMenuItemsProviderA implements GlobalMenuItemsProvider{
	public static String groupALabel = "MyGroupA";
	public static String groupAPath = "test/pathA/";
	public static String groupAId = "idGroupA";
	public static String implicitGroupBLabel = "idGroupB";
	public static String groupCLabel = "MyGroupC";		
	public static String itemA1Label = "MyLabelA1";
	public static String itemA2Label = "MyLabelA2";
	public static String itemA2Path = "test/pathA2/";
	

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		items.add(new GlobalMenuItem(groupAPath, groupAId, groupALabel, 5, null));
		items.add(new GlobalMenuItem("test/pathC/", "idGroupC", groupCLabel, 5, null));
		items.add(new GlobalMenuItem("test/pathA1/", "idLabelA1", itemA1Label, 2, groupAId));
		items.add(new GlobalMenuItem(itemA2Path, "idLabelA2", itemA2Label, 7, groupAId));
		items.add(new GlobalMenuItem("test/pathB1/", "idLabelB1", "MyLabelB1", 1, "idGroupB"));
		items.add(new GlobalMenuItem("test/pathB2/", "idLabelB2", "MyLabelB2", 2, "idGroupB"));
		return items;
	}
}
