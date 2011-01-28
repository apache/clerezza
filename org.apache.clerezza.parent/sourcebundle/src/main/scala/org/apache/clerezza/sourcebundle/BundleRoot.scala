/*
 *  Copyright 2011 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.sourcebundle

import java.io._
import scala.actors.DaemonActor
import scala.io._
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import sbt._
import scala.actors.Actor
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles._
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundle
import org.apache.clerezza.scala.scripting.CompilerService
import org.osgi.framework.Constants
import org.osgi.service.component.ComponentContext
import org.slf4j.LoggerFactory
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import scala.collection.mutable
import org.osgi.service.packageadmin.PackageAdmin

class BundleRoot {

	var compilerService: CompilerService = null
	var packageAdmin: PackageAdmin = null

	var bundleContext: BundleContext = null

	val sourceBundles = mutable.ListBuffer[SourceBundle]()

	protected def activate(c: ComponentContext) {
		this.bundleContext = c.getBundleContext
		sourceBundles.clear()
		for (bundle <- bundleContext.getBundles) {
			val location = bundle.getLocation
			if (location.startsWith(BundleRoot.sourceBundleUriPrefix)) {
				val dir = new File(location.substring(
						BundleRoot.sourceBundleUriPrefix.length))
				val sourceBundle = new SourceBundle(dir, bundle)
				sourceBundle.start()
				sourceBundles += sourceBundle
			}
		}
	}

	protected def deactivate(c: ComponentContext) {
		for (sb <- sourceBundles) sb.stop()
	}

	def createSourceBundle(dir: File) = {
		val sourceBundle = new SourceBundle(dir)
		sourceBundle.start()
		sourceBundles += sourceBundle
		sourceBundle
	}

	def bindCompilerService(cs: CompilerService) {
		compilerService = cs;
	}

	def unbindCompilerService(cs: CompilerService) {
		compilerService = null;
	}

	def bindPackageAdmin(pa: PackageAdmin) {
		packageAdmin = pa
	}

	def unbindPackageAdmin(pa: PackageAdmin) {
		packageAdmin = null
	}

	class SourceBundle(dir: File, existingBundle: Bundle) extends DaemonActor {

		def this(dir: File) {
			this(dir, null)
		}

		var stopped = false
		var logger = LoggerFactory.getLogger(classOf[SourceBundle])

		val sourcePath = Path.fromFile(dir)
		var watchState = WatchState.empty
		var bundle: Bundle = existingBundle

		def getFilesAsCharArrays(file: File): List[Array[Char]] = {
			logger.debug("getting sources in "+file)
			var result: List[Array[Char]] = Nil
			if (file.isDirectory) {
				val children = file.listFiles
				import scala.collection.JavaConversions._
				for(child <- children) {
					if (!child.getName.startsWith(".")) {
						result = getFilesAsCharArrays(child) ::: result
					}
				}
			} else {
				if (file.getName.endsWith(".scala")) {
					val in = Source.fromFile(file, "utf-8")
					val stream = in.toStream
					result = stream.toArray :: result
				} 
			}
			result
		}

		private[this] def updateBundle() {
			logger.info("updating source bundle with root "+dir)

			val tinyBundle: TinyBundle = newBundle()

			def compileDir(sourceDir: File) {
				
				val charArrays = getFilesAsCharArrays(sourceDir)
				logger.debug("compiling "+charArrays.size+" files")
				
				val vdPathPrefix = "(memory)"
				val virtualDirectory = new VirtualDirectory(vdPathPrefix, None)
				//val wrappedDirectory = VirtualDirectoryWrapper.wrap(virtualDirectory, outputListener)

				val writtenClasses = compilerService.compileToDir(charArrays, virtualDirectory)
				logger.debug("virtualDirectory "+virtualDirectory.size)
				for (writtenClass <- writtenClasses) {
					val fullPath = writtenClass.path
					val path = fullPath.substring(vdPathPrefix.length+1)
					tinyBundle.add(path, new ByteArrayInputStream(writtenClass.toByteArray))
				}
			}

			def copyResource(resourcesDir: File) {
				def copyResource(resourcesDir: File, prefix: String) {
					val children = resourcesDir.listFiles
					import scala.collection.JavaConversions._
					for(child <- children) {
						val childName = child.getName
						if (!childName.startsWith(".")) {
							if (child.isDirectory) {
								copyResource(child, prefix+childName+"/")
							} else {
								tinyBundle.add(prefix+childName, new FileInputStream(child))
							}
						}
					}
				}
				copyResource(resourcesDir, "")
			}

			val symName = dir.getPath.substring(1).replace(File.separatorChar, '.')

			tinyBundle.set("Bundle-SymbolicName", symName)

			val scalaSourceDir = new File(dir, "src/main/scala")
			if (scalaSourceDir.exists) {
				compileDir(scalaSourceDir)
			} else {
				logger.debug("No source dir "+scalaSourceDir)
			}
			val resourcesDir = new File(dir, "src/main/resources")
			if (resourcesDir.exists) {
				copyResource(resourcesDir)
			} else {
				logger.debug("No resources dir "+resourcesDir)
			}
			val serviceComponentsFile = new File(resourcesDir, "OSGI-INF/serviceComponents.xml")
			if (serviceComponentsFile.exists) {
				tinyBundle.set("Service-Component", "OSGI-INF/serviceComponents.xml")
				tinyBundle.set(Constants.EXPORT_PACKAGE, "!OSGI-INF, *" )
			}
			tinyBundle.set(Constants.IMPORT_PACKAGE, "*" );
			val in = tinyBundle.build(
					withBnd()
				)


			if (bundle == null) {
				bundle = bundleContext.installBundle(BundleRoot.sourceBundleUriPrefix+dir.toString, in)
				bundle.start()
			} else {
				bundle.update(in)
			}
			
		}

		def act() {
			while (!stopped) {
				val (triggered, newWatchState) =
					SourceModificationWatch.watch(sourcePath**(-HiddenFileFilter), 1, watchState)(stopped)
				if (!stopped) {
					updateBundle()
					watchState = newWatchState
				}
			}
		}

		def stop() {
			stopped = true
		}
	}
}


object BundleRoot {

	val sourceBundleUriPrefix = "sourcebundle:"

}
