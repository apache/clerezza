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

/**
 * A service that reads image data and provides {@link BufferedImage}s.
 * 
 * By providing a custom service it is easy to extend the platform's image 
 * processing capabilities with new image formats or use alternate processing 
 * libraries for reading image data.
 *
 * @author daniel
 */
public interface ImageReader {
    
    /**
     * Creates a {@link BufferedImage} from an {@link File}.
     *
     * @param file the image file.
     * @return a {@link BufferedImage} containing the data of the specified
     * image.
     */
    public BufferedImage getBufferedImage(File file) throws IOException;

    /**
     * Creates a {@link BufferedImage} from an {@link InputStream}.
     *
     * @param in a stream from which the image data can be read.
     * @return a {@link BufferedImage} containing the data of the specified
     * image.
     */
    public BufferedImage getBufferedImage(InputStream in) throws IOException;

    /**
     * Creates a {@link BufferedImage} from a {@link URL}.
     *
     * @param url the location of the image described as a URL.
     * @return a {@link BufferedImage} containing the data of the specified
     * image.
     */
    public BufferedImage getBufferedImage(URL url) throws IOException;
}
