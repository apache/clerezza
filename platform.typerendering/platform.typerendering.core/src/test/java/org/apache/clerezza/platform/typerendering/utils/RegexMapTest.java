package org.apache.clerezza.platform.typerendering.utils;

import org.junit.Assert;
import org.junit.Test;


public class RegexMapTest {

	@Test
	public void orderingTest() {
		RegexMap<Object> map = new RegexMap<Object>();
		map.addEntry("(naked|.*-naked)", "v2");
		map.addEntry("concept-find-create-naked", "v1");
		Assert.assertEquals("v1", map.getMatching("concept-find-create-naked").next());
	}
}
