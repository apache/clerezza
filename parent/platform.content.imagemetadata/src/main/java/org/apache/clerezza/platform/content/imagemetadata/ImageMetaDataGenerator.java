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
package org.apache.clerezza.platform.content.imagemetadata;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.ontologies.EXIF;

/**
 * This class generates metadata about image data.
 *
 * @author mir, hasan
 */
@Component(metatype=true)
@Service(MetaDataGenerator.class)
public class ImageMetaDataGenerator implements MetaDataGenerator {

	@Override
	public void generate(GraphNode node, byte[] data, MediaType mediaType) {

		if (mediaType.getType().startsWith("image")) {
			try {
				BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(data));
				node.deleteProperties(EXIF.width);
				node.deleteProperties(EXIF.height);
				node.addProperty(EXIF.width, LiteralFactory.getInstance().
						createTypedLiteral(Integer.valueOf(buffImage.getWidth())));
				node.addProperty(EXIF.height, LiteralFactory.getInstance().
						createTypedLiteral(Integer.valueOf(buffImage.getHeight())));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
