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
 
if (String.prototype.decodeId == null) {
	
	String.prototype.decodeId = function() {
		
		var newstring = this;
		var match = this.match(/_[0-9]+_/g);

		if(match) {
			for(var i = 0; i < match.length; i++) {
				regexp = new RegExp(match[i], "g");
				newstring = newstring.replace(regexp,String.fromCharCode(match[i].replace(/_/g,'')));
			}
		}
		return newstring;
	}
}

if (String.prototype.encodeId == null) {
	
	String.prototype.encodeId = function(type) {
		var newstring = '';
		
		for(var i = 0; i < this.length; i++) {
			if(this.charAt(i).search(/[-A-Za-z0-9]/) == -1) {
				newstring += "_" + this.charCodeAt(i) + "_";
			 } else {
				newstring += this.charAt(i);
			}
		}
		return newstring;
	}		
}