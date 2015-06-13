/*
 * Copyright 2015 The Apache Software Foundation.
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

package org.apache.clerezza.integration.tests;

import com.jayway.restassured.RestAssured;
import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.stanbol.commons.testing.jarexec.JarExecutor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reto
 */

public abstract class BaseTest {

    public static final String TEST_SERVER_URL_PROP = "test.server.url";
    public static final String SERVER_READY_TIMEOUT_PROP = "server.ready.timeout.seconds";
    public static final String SERVER_READY_PROP_PREFIX = "server.ready.path";
    public static final String KEEP_JAR_RUNNING_PROP = "keepJarRunning";

    protected static String serverBaseUrl;

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    protected boolean serverReady = false;
    //protected RequestBuilder builder;
    protected DefaultHttpClient httpClient = new DefaultHttpClient();
    //protected RequestExecutor executor = new RequestExecutor(httpClient);

    @BeforeClass
    public static synchronized void startRunnableJar() throws Exception {
        if (serverBaseUrl != null) {
            // concurrent initialization by loading subclasses
            return;
        }
        final String configuredUrl = System.getProperty(TEST_SERVER_URL_PROP);
        if ((configuredUrl != null) && !configuredUrl.isEmpty()) {
            serverBaseUrl = configuredUrl;
            log.info(TEST_SERVER_URL_PROP + " is set: not starting server jar (" + serverBaseUrl + ")");
        } else {
            final File workingDir = new File(System.getProperty(JarExecutor.PROP_WORKING_DIRECTORY));
            if (workingDir.exists()) {
               delete(workingDir);
            }
            final JarExecutor j = JarExecutor.getInstance(System.getProperties());
            j.start();
            serverBaseUrl = "http://localhost:" + j.getServerPort();
            log.info("Forked subprocess server listening to: " + serverBaseUrl);

            
        }
        RestAssured.baseURI = serverBaseUrl;
    }
    
    @AfterClass
    public static void afterClass() {
        // Optionally block here so that the runnable jar stays up - we can
        // then run tests against it from another VM
        if ("true".equals(System.getProperty(KEEP_JAR_RUNNING_PROP))) {
            log.info(KEEP_JAR_RUNNING_PROP + " set to true - entering infinite loop"
                    + " so that runnable jar stays up. Kill this process to exit.");
            while (true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Before
    public void waitForServerReady() throws Exception {
        log.debug("> before {}#waitForServerReady()",getClass().getSimpleName());
        if (serverReady) {
            log.debug(" ... server already marked as ready!");
            return;
        }

        // Timeout for readiness test
        final String sec = System.getProperty(SERVER_READY_TIMEOUT_PROP);
        final int timeoutSec = sec == null ? 60 : Integer.valueOf(sec);
        log.info("Will wait up to " + timeoutSec + " seconds for server to become ready");
        final long endTime = System.currentTimeMillis() + timeoutSec * 1000L;

        // Get the list of paths to test and expected content regexps
        final List<String> testPaths = new ArrayList<String>();
        final TreeSet<Object> propertyNames = new TreeSet<Object>();
        propertyNames.addAll(System.getProperties().keySet());
        for (Object o : propertyNames) {
            final String key = (String) o;
            if (key.startsWith(SERVER_READY_PROP_PREFIX)) {
                testPaths.add(System.getProperty(key));
            }
        }

        // Consider the server ready if it responds to a GET on each of 
        // our configured request paths with a 200 result and content
        // that matches the regexp supplied with the path
        long sleepTime = 100;
        readyLoop:
        while (!serverReady && System.currentTimeMillis() < endTime) {
            // Wait a bit between checks, to let the server come up
            Thread.sleep(sleepTime);
            sleepTime = Math.min(5000L, sleepTime * 2);

            // A test path is in the form path:substring or just path, in which case
            // we don't check that the content contains the substring 
            log.debug(" - check serverReady Paths");
            for (String p : testPaths) {
                log.debug("    > path: {}", p);
                final String[] s = p.split(":");
                final String path = s[0];
                final String substring = (s.length > 0 ? s[1] : null);
                final String url = serverBaseUrl + path;
                log.debug("    > url: {}", url);
                log.debug("    > content: {}", substring);
                final HttpGet get = new HttpGet(url);
                //authenticate as admin with password admin
                get.setHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
                for(int i = 2; i+1<s.length;i=i+2){
                    log.debug("    > header: {}:{}", s[i], s[i+1]);
                    if(s[i] != null && !s[i].isEmpty() &&
                            s[i+1] != null && !s[i+1].isEmpty()){
                        get.setHeader(s[i], s[i+1]);
                    }
                }
                HttpEntity entity = null;
                try {
                    log.debug("    > execute: {}", get);
                    HttpResponse response = httpClient.execute(get);
                    log.debug("    > response: {}", response);
                    entity = response.getEntity();
                    final int status = response.getStatusLine().getStatusCode();
                    if (status != 200) {
                        log.info("Got {} at {} - will retry", status, url);
                        continue readyLoop;
                    } else {
                        log.debug("Got {} at {} - will retry", status, url);
                    }

                    if (substring != null) {
                        if (entity == null) {
                            log.info("No entity returned for {} - will retry", url);
                            continue readyLoop;
                        }
                        final String content = EntityUtils.toString(entity);
                        final boolean checkAbsence = substring.startsWith("!");
                        final String notPresentString = substring.substring(1);
                        if ((!checkAbsence && content.contains(substring)) || 
                                (checkAbsence && content.contains(notPresentString))) {
                            log.debug("Returned content for {}  contains {} - ready", 
                                url, substring);
                        } else {
                            log.info("Returned content for {}  does not contain " 
                                    + "{} - will retry", url, substring);
                            continue readyLoop;
                            
                        }
                    }
                } catch (ConnectException e) {
                    log.info("Got {} at {} - will retry", e.getClass().getSimpleName(), url);
                    continue readyLoop;
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }
            log.info("Got expected content for all configured requests, server is ready");
            //Some additional wait time, as not everything can be tested with the paths
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            serverReady = true;
        }

        if (!serverReady) {
            throw new Exception("Server not ready after " + timeoutSec + " seconds");
        }
    }
   
}
