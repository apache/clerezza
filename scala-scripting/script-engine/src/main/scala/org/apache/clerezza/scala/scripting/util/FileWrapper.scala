/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

import scala.tools.nsc.io.AbstractFile

class FileWrapper(val wrapped: AbstractFile, val childWrapper: (AbstractFile) => AbstractFile) extends AbstractFile with GenericFileWrapperTrait {

	/** overriding this hgere rather than in the trait as this is a var in VirtualDirectory
	*/
	def lastModified = {
		wrapped.lastModified
	}

	override protected def unsupported(msg: String) = {
		println("unsupported!")
		try {
			super.unsupported(msg)
		} catch {
			case e => e.printStackTrace(); throw e
		}
	}
}
