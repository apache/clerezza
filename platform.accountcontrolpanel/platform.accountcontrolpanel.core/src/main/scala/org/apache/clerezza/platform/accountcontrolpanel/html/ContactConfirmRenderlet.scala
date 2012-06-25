package org.apache.clerezza.platform.accountcontrolpanel.html

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
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._



/**
 * Metadata class for the person panel
 */
class ContactConfirmRenderlet extends SRenderlet {
	def getRdfType() = CONTROLPANEL.ContactConfirmPage


	override def renderedPage(arguments: XmlResult.Arguments) = new XmlPerson(arguments)

	/**
	 * Content class for the Person Panel
	 */
	class XmlPerson(args: XmlResult.Arguments) extends XmlResult(args) {

		import RenderingUtility._
		
		//
		// the content itself.
		// This is the piece that is closest to a pure ssp, though there is still too much code in it
		//

		override def content = {
			val primarySubject = res/FOAF.primaryTopic
			<div id="tx-content">
				{
				if (primarySubject.hasProperty(RDF.`type`, FOAF.Person)) {
					<form action="addContact" method="post">
						{render(primarySubject, "box-naked")}
						<input type="hidden" name="webId" value={primarySubject*} />
						<input type="submit" value="Add this contact" />
					</form>
				} else {
					<div>
					<span>The resource {primarySubject!} of type {primarySubject/RDF.`type`} is not known to be a Person</span>
					{
						import collection.JavaConversions._
						val otherPersons = (for (t <- primarySubject.getNodeContext.filter(null, RDF.`type`, FOAF.Person))
							yield t.getSubject).toList
						val personsWithUri: List[UriRef] = for (otherPerson <- otherPersons;
								if otherPerson.isInstanceOf[UriRef]) yield otherPerson.asInstanceOf[UriRef]
						if (personsWithUri.isEmpty) {
							<span>No person could be found</span>
						} else {
							<div>
								Maybe you want to add {
									if (personsWithUri.size > 1) {
										"one of the following "+personsWithUri.size+" persons:"
									} else {
										"the person"
									}
								}
								{
									for (otherPerson <- personsWithUri) yield {
										<form action="addContact" method="post">
											<span>{otherPerson}</span>
											{render(otherPerson, "box-naked")}
											<input type="hidden" name="webId" value={otherPerson.getUnicodeString} />
											<input type="submit" value="Add this contact" />
										</form>
									}
								}
							</div>
						}
					}
					{
						<form action="addContact" method="post">
						You can add {primarySubject} as contact even though it does not seem to be a person.
						<input type="hidden" name="webId" value={primarySubject*} />
						<input type="submit" value="Add anyway" />
					</form>
					} </div>
				}
			}
			<a href="../profile" onclick="history.go(-1)">Cancel</a>
			</div>
		}

	}
}
