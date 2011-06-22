/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.clerezza.rdf.scala.utils

import org.apache.clerezza.rdf.core.Language

/**
 * Language tags in html and rdf are explained in more detail
 * http://www.w3.org/International/questions/qa-choosing-language-tags
 *
 * @author hjs
 * @created: 21/06/2011
 */


/**
 * A Language Identifier can be a language and a number of Language variations
 * Would be nice if this could just implement the java
 */
class Lang(val id: String) extends Language(id: String) {
}

/**
 * Language and regions can be composed
 *    http://www.i18nguy.com/unicode/language-identifiers.html
 *
 */
/*class LangRegion(lang: Lang, region: Region) extends LangId(lang.toString+"_"+region.toString)
object LangRegion {
	import Lang._
	def apply(lang: Lang, region: Region) = new LangRegion(lang,region)
	val en_uk = LangRegion(en,uk)
	val en_dm = LangRegion(en,dm)
	val fr_ch = LangRegion(fr,ch)
	val de_ch = LangRegion(de,ch)
	val it_ch = LangRegion(it,ch)
}*/

/**
 * the simple two character language names
 */
object Lang {
	def apply(id: String) = new Lang(id)

	val en = Lang("en")
	val de = Lang("de")
	val fr = Lang("fr")
	val it = Lang("it")
	val rm = Lang("rm") //raeto-romance (swiss language)

}





/**
 * region
 */
class Region

case object uk extends Region
case object us extends Region
case object dm extends Region //dominican
case object ch extends Region //switzerland