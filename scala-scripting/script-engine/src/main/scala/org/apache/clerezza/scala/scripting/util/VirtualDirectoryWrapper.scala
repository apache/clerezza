/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory

class VirtualDirectoryWrapper(val wrapped: AbstractFile,
							  val childWrapper: (AbstractFile) => AbstractFile) extends VirtualDirectory(null, None)
							with GenericFileWrapperTrait {
	lastModified =wrapped.lastModified

	override def output = {
		wrapped.asInstanceOf[VirtualDirectory].output
	}
	override def input = {
		wrapped.asInstanceOf[VirtualDirectory].input
	}
	override def file = {
		wrapped.asInstanceOf[VirtualDirectory].file
	}
	override def container = {
		wrapped.asInstanceOf[VirtualDirectory].container
	}
	override def absolute = {
		wrapped.asInstanceOf[VirtualDirectory].absolute
	}
	override val name = {
		wrapped.name
	}
	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}

}
