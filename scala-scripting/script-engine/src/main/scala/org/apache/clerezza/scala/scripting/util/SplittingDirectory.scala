/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory

class SplittingDirectory()
		extends VirtualDirectory(null, None) with GenericFileWrapperTrait {

	var currentTarget: VirtualDirectory = null

	protected def wrapped: VirtualDirectory = {
		if (currentTarget == null) {
			throw new RuntimeException("No current Target set, SplittingDirectory not usable")
		}
		currentTarget
	}

	private def wrap(f: AbstractFile): AbstractFile =  {
		f match {
			case d: VirtualDirectory => new VirtualDirectoryWrapper(d, wrap) {
					override def output = d.output
				}
			case o => new FileWrapper(o, wrap)
		}
	}
	val childWrapper: (AbstractFile) => AbstractFile = wrap

	//lastModified = wrapped.lastModified

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
	override val name = "(splitting)"
	
	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}

}

