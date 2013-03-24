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
package org.apache.clerezza.platform.xhtml2html;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Sets the accept-header to text/html, application/xhtml+xml;q=.9,*\/*;q=.1
 *
 * @author rbn
 */
class WrappedRequest extends HttpServletRequestWrapper {
    public static final String NEW_ACCCEPT = "text/html, application/xhtml+xml;q=.9, */*;q=.1";

    public WrappedRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Enumeration<String> getHeaders(String headerName) {
        if ("Accept".equalsIgnoreCase(headerName)) {
            Vector<String> newList = new Vector<String>();
            newList.add(NEW_ACCCEPT);
            return newList.elements();
        } else {
            return super.getHeaders(headerName);
        }
    }
    @Override
    public String getHeader(String headerName) {
        if ("Accept".equalsIgnoreCase(headerName)) {
            List<String> newList = new ArrayList();
            return NEW_ACCCEPT;
        } else {
            return super.getHeader(headerName);
        }
    }


}
