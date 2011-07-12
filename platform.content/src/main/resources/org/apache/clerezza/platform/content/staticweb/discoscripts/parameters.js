/*
 *
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
 *
*/

function Parameters() {

	this.parameterArray = new Array(0);
    var url   = window.location.search;
    if (url != "")
     {
      url = url.substring(1,url.length);                      
      splittedURL = url.split("&"); 
      for (i=0;i < splittedURL.length;i++)
       {
        temp = splittedURL[i].split("=");
        currentField = new field();
				currentField.name = temp[0];
				currentField.value = Parameters.readField(temp[1]);
				this.parameterArray.push(currentField);
       }
     }
     
}

Parameters.prototype.getField = function(name) {

	for (i = 0; i < this.parameterArray.length; i++) {
		if (this.parameterArray[i].name == name) {
			return this.parameterArray[i].value;
		}
	}
	return null;
}

Parameters.readField = function(raw) {
	if (raw == null) {
		return null;
	}
	var result = raw.replace(/\+/g," ");
	return unescape(result);
}

	 
function field() {
 	var name;
	var value;
}
	
	
	
	


   
	 	
