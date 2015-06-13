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
import com.jayway.restassured.response.Response;
import org.apache.clerezza.platform.Constants;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class AdminGraphsTest extends BaseTest {
    
    
    @Test
    public void htmlVersion() {
        Response response = RestAssured.given().header("Accept", "text/html")
                .auth().basic("admin", "admin")
                .expect().statusCode(HttpStatus.SC_OK).when()
                .get("/admin/graphs");
        response.then().assertThat().body(containsString(Constants.CONTENT_GRAPH_URI_STRING));
    }
    
   
    
}
