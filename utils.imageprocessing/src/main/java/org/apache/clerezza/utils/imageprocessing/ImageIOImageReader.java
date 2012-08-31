/*
 * Copyright 2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.utils.imageprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;

/**
 * A service using Java Image IO to read images.
 *
 * @author daniel
 */
@Component(label="Java Image IO Image Reader", 
        description="Reads images using default Java Image IO.",
        metatype=true)
@Service(ImageReader.class)
@Property(name=Constants.SERVICE_RANKING, intValue=0, propertyPrivate=false)
public class ImageIOImageReader implements ImageReader {
    
    @Override
    public BufferedImage getBufferedImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    @Override
    public BufferedImage getBufferedImage(InputStream in) throws IOException {
        if(in instanceof ImageInputStream) {
            return ImageIO.read((ImageInputStream) in);
        }
        return ImageIO.read(in);
    }

    @Override
    public BufferedImage getBufferedImage(URL url) throws IOException {
        return ImageIO.read(url);
    }
}
