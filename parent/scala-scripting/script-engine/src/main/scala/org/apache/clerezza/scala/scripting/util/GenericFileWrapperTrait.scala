/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

import scala.tools.nsc.io.AbstractFile

trait GenericFileWrapperTrait extends AbstractFile with Wrapper[AbstractFile] {
	override def lookupNameUnchecked(name: String,directory: Boolean) = {
		childWrapper(wrapped.lookupNameUnchecked(name, directory))
	}
	override def lookupName(name: String,directory: Boolean) = {
		wrapped.lookupName(name, directory)
	}
	override def iterator = {
		//TODO wrap
		wrapped.iterator
	}
	override def output = {
		wrapped.output
	}
	override def input = {
		wrapped.input
	}
	
	override def isDirectory = {
		wrapped.isDirectory
	}
	override def delete = {
		wrapped.delete
	}
	override def create = {
		wrapped.create
	}
	override def file = {
		wrapped.file
	}
	override def container = {
		childWrapper(wrapped.container)
	}
	override def absolute = {
		childWrapper(wrapped.absolute)
	}
	override def path = {
		wrapped.path
	}
	override def name = {
		wrapped.name
	}

	override def sizeOption = {
		wrapped.sizeOption
	}

	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}
	override def fileNamed(name: String): AbstractFile = {
		childWrapper(wrapped.fileNamed(name))
	}

	override def subdirectoryNamed(name: String): AbstractFile = {
		childWrapper(wrapped.subdirectoryNamed(name))
	}
}
