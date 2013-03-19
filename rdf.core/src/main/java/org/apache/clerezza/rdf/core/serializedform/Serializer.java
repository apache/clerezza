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
package org.apache.clerezza.rdf.core.serializedform;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.clerezza.rdf.core.TripleCollection;

/**
 * This singleton class provides a method <code>serialize</code> to transform a
 * {@link Graph} into serialized RDF forms.
 * 
 * Functionality is delegated to registered {@link SerializingProvider}s. Such
 * <code>SerializingProvider</code>s can be registered and unregistered, later
 * registered <code>SerializingProvider</code>s shadow previously registered
 * providers for the same format.
 * 
 * Note on synchronization: <code>SerializingProvider</code>s must be able to
 * handle concurrent requests.
 * 
 * @author mir
 * 
 * @scr.component
 * @scr.service interface="org.apache.clerezza.rdf.core.serializedform.Serializer"
 * @scr.reference 
 *                name="serializingProvider"
 *                cardinality="0..n" policy="dynamic"
 *                interface="org.apache.clerezza.rdf.core.serializedform.SerializingProvider"
 */
public class Serializer {

    /**
     * The list of providers in the order of registration
     */
    private List<SerializingProvider> providerList = new ArrayList<SerializingProvider>();

    /**
     * A map to quickly locate a provider
     */
    private volatile Map<String, SerializingProvider> providerMap = new HashMap<String, SerializingProvider>();

    /**
     * The singleton instance
     */
    private volatile static Serializer instance;

    /**
     * the constructor sets the singleton instance to allow instantiation
     * by OSGi-DS. This constructor should not be called except by OSGi-DS,
     * otherwise the static <code>getInstance</code> method should be used.
     */
    public Serializer() {
        Serializer.instance = this;
    }

    /**
     * A constructor for tests, which doesn't set the singleton instance
     * 
     * @param dummy
     *            an ignored argument to distinguish this from the other
     *            constructor
     */
    Serializer(Object dummy) {
    }

    /**
     * This returns the singleton instance, if an instance has been previously
     * created (e.g. by OSGi declarative services) this instance is returned,
     * otherwise a new instance is created and providers are injected using the
     * service provider interface (META-INF/services/)
     * 
     * @return the singleton Serializer instance
     */
    public static Serializer getInstance() {
        if (instance == null) {
            synchronized (Serializer.class) {
                if (instance == null) {
                    new Serializer();
                    Iterator<SerializingProvider> SerializingProviders = ServiceLoader
                            .load(SerializingProvider.class).iterator();
                    while (SerializingProviders.hasNext()) {
                        SerializingProvider SerializingProvider = SerializingProviders
                                .next();
                        instance.bindSerializingProvider(SerializingProvider);
                    }
                }
            }
        }
        return instance;

    }

    /**
     * Serializes a Graph into an OutputStream. This delegates the
     * processing to the provider registered for the specified format, if
     * the formatIdentifier contains a ';'-character only the section before
     * that character is used for choosing the provider.
     * 
     * @param serializedGraph
     *            an outputStream into which the Graph will be serialized
     * @param tc  the <code>TripleCollection</code> to be serialized
     * @param formatIdentifier
     *            a string specifying the serialization format (usually the
     *            MIME-type)
     * @throws UnsupportedFormatException
     */
    public void serialize(OutputStream serializedGraph, TripleCollection tc,
            String formatIdentifier) throws UnsupportedFormatException {
        String deParameterizedIdentifier;
        int semicolonPos = formatIdentifier.indexOf(';');
        if (semicolonPos > -1) {
            deParameterizedIdentifier = formatIdentifier.substring(0, semicolonPos);
        } else {
            deParameterizedIdentifier = formatIdentifier;
        }
        SerializingProvider provider = providerMap.get(deParameterizedIdentifier);
        if (provider == null) {
            throw new UnsupportedSerializationFormatException(formatIdentifier);
        }
        provider.serialize(serializedGraph, tc, formatIdentifier);
    }

    /**
     * Registers a Serializing provider
     * 
     * @param provider
     *            the provider to be registered
     */
    public void bindSerializingProvider(SerializingProvider provider) {
        providerList.add(provider);
        refreshProviderMap();

    }

    /**
     * Unregister a Serializing provider
     * 
     * @param provider
     *            the provider to be unregistered
     */
    public void unbindSerializingProvider(SerializingProvider provider) {
        providerList.remove(provider);
        refreshProviderMap();

    }

    private void refreshProviderMap() {
        final Map<String, SerializingProvider> newProviderMap = new HashMap<String, SerializingProvider>();
        //we want more generic providers first so they get overridden by more specific ones
        Collections.sort(providerList, new Comparator<SerializingProvider>() {

            @Override
            public int compare(SerializingProvider s1, SerializingProvider s2) {
                return getFormatIdentifiers(s2).length - getFormatIdentifiers(s1).length;
            }

        });
        for (SerializingProvider provider : providerList) {
            String[] formatIdentifiers = getFormatIdentifiers(provider);
            for (String formatIdentifier : formatIdentifiers) {
                newProviderMap.put(formatIdentifier, provider);
            }
        }
        providerMap = newProviderMap;
    }

    private String[] getFormatIdentifiers(
            SerializingProvider SerializingProvider) {
        Class<? extends SerializingProvider> clazz = SerializingProvider
                .getClass();
        SupportedFormat supportedFormatAnnotation = clazz
                .getAnnotation(SupportedFormat.class);
        String[] formatIdentifiers = supportedFormatAnnotation.value();
        return formatIdentifiers;
    }
}
