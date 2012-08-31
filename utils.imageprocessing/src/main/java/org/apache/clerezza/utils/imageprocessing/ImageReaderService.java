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
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * This service dynamically provides the registered {@link ImageReader} with the 
 * highest service ranking.
 *
 * @author daniel
 */
@Component(label="Image Reader Service.", 
        description="Provides the registered ImageReader with the highest service ranking.")
@Service(ImageReaderService.class)
@Reference(name = "imageReaders", 
        referenceInterface = ImageReader.class, 
        cardinality = ReferenceCardinality.MANDATORY_MULTIPLE, 
        policy = ReferencePolicy.STATIC, 
        strategy = ReferenceStrategy.LOOKUP)
public class ImageReaderService implements ImageReader {
    
    private ComponentContext componentContext;
    
    /**
     * Returns the {@link ImageReader} with the highest service ranking. 
     * 
     * In case of a tie, the service with the lower service id (in general the 
     * service that got registered first) is returned.
     * 
     * @return an {@link ImageReader} service instance
     */
    public ImageReader getImageReader() {
        if(componentContext != null) {
            ImageReader ret = (ImageReader) componentContext.locateService("imageReaders");
            return ret;
        } else {
            throw new RuntimeException(
                    String.format("%s is not initialized correctly.", 
                    ImageReaderService.class.getSimpleName()));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This method delegates to the {@link ImageReader} service with the 
     * highest service ranking. In case of a tie the service with the lowest 
     * service id is used.
     * </p>
     */
    @Override
    public BufferedImage getBufferedImage(File file) throws IOException {
        return this.getImageReader().getBufferedImage(file);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This method delegates to the {@link ImageReader} service with the 
     * highest service ranking. In case of a tie the service with the lowest 
     * service id is used.
     * </p>
     */
    @Override
    public BufferedImage getBufferedImage(InputStream in) throws IOException {
        return this.getImageReader().getBufferedImage(in);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This method delegates to the {@link ImageReader} service with the 
     * highest service ranking. In case of a tie the service with the lowest 
     * service id is used.
     * </p>
     */
    @Override
    public BufferedImage getBufferedImage(URL url) throws IOException {
        return this.getImageReader().getBufferedImage(url);
    }
    
    /**
     * Called when this component is enabled (when this services references are 
     * satisfied and this service is referenced by some other service).
     * 
     * @param context the component context.
     */
    @Activate
    protected void activate(ComponentContext context) {
        this.componentContext = context;
    }
    
    /**
     * Called when this component is disabled (when this services references are 
     * not satisfied or this service is not referenced by some other service).
     * 
     * @param context the component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        this.componentContext = null;
    }
}
