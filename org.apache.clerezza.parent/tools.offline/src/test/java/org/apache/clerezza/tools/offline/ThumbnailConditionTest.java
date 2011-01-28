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

package org.apache.clerezza.tools.offline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.clerezza.platform.content.representations.core.ThumbnailService;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.tools.offline.utils.ConditionalOutputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mir
 */
public class ThumbnailConditionTest {

	private UriRef uri = new UriRef ("http://localhost:8080/html_export/digital-assets/2010/08/30/770a7f14-74a7-4036-8341-f9e50e944e06");

	private static final byte[] input = "<img href=\"/thumbnail-service?uri=http://localhost:8080/html_export/digital-assets/2010/08/30/770a7f14-74a7-4036-8341-f9e50e944e06&amp;width=700&height=300&exact=true\" />".getBytes();
	
	@Test
	public void thumbnailConditionTest() throws IOException {
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		OutputStream out = new ConditionalOutputStream(bous,
				new ThumbnailCondition(new ThumbnailService() {

			@Override
			public UriRef getThumbnailUri(UriRef infoBitUri, Integer width, Integer height, boolean exact) {
				Assert.assertEquals(uri, infoBitUri);
				Assert.assertEquals(Integer.valueOf(700), width);
				Assert.assertEquals(Integer.valueOf(300), height);
				Assert.assertEquals(true, exact);
				return new UriRef("http://example.com/test");
			}				
		}));
		
		out.write(input);
		Assert.assertEquals("<img href=\"http://example.com/test\" />", bous.toString());
	}
}
