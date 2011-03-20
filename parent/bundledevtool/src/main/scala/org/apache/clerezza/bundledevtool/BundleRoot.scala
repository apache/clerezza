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

package org.apache.clerezza.bundledevtool


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
import scala.collection.mutable
import org.osgi.service.packageadmin.PackageAdmin
import tools.nsc.io.{AbstractFile, VirtualDirectory}
import java.io.{File, FileInputStream, ByteArrayInputStream}
import org.apache.clerezza.utils.osgi.BundlePathNode
import org.wymiwyg.commons.util.dirbrowser.PathNode

/**
 * Provides a service that allows to register directories containing a maven-style project
 * structure to be registered as SourceBundle which is added as OSGi Bundle and regenerated
 * whenever a file is changed.
 *
 * Currently only scala files are compiled.
 */
class BundleRoot {

	private var compilerService: CompilerService = null
	private var packageAdmin: PackageAdmin = null

	private var bundleContext: BundleContext = null

	private val skeletonsPath = "org/apache/clerezza/bundledevtool/skeletons"

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

	/**
	 * adds a SourceBundle for the sources in the specified dir
	 */
	def addSourceBundle(dir: File) = {
		val sourceBundle = new SourceBundle(dir)
		sourceBundle.start()
		sourceBundles += sourceBundle
		sourceBundle
	}

	@deprecated
	def createSourceBundle(dir: File) = addSourceBundle(dir)

	/**
	* list of the available skletons
	*/
	def availableSkeletons: Seq[Symbol] = {
		val skeletonsNode = new BundlePathNode(bundleContext.getBundle, skeletonsPath)
		for (name <- skeletonsNode.list) yield {
			Symbol(name.substring(1,name.length-1))
		}
	}

	/**
	 * Creates and adds a new SourceBundle from a skeleton, no existing file is
	 * replaced
	 */
	def createSourceBundle(skeleton: Symbol, dir: File) = {
		dir.mkdirs
		val skeletonsNode = new BundlePathNode(bundleContext.getBundle, skeletonsPath)
		val skeletonNode = skeletonsNode.getSubPath(skeleton.name)
		if (!skeletonNode.exists) {
			throw new UnavailableSkeletonException(skeleton, availableSkeletons)
		}
		def processFile(p: PathNode, f: File) {
			if (!f.exists) {
				val in = scala.io.Source.fromInputStream(p.getInputStream)
				val out = new java.io.PrintWriter(f)
				try { in.getLines().foreach(out.println(_)) }
				finally { out.close }
			}
		}
		def processDir(p: PathNode, f: File) {
			f.mkdir()
			for (subPathString <- p.list()) {
				val subPathNode: PathNode = p.getSubPath(subPathString)
				val subFile: File = new File(f, subPathString)
				if (subPathNode.isDirectory) {
					processDir(subPathNode, subFile)
				} else {
					processFile(subPathNode, subFile)
				}
			}
		}

		processDir(skeletonNode, dir)
		addSourceBundle(dir)
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

			def compileDir(sourceDir: File): Option[String] = {

				val charArrays = getFilesAsCharArrays(sourceDir)
				logger.debug("compiling "+charArrays.size+" files")

				val vdPathPrefix = "(memory)"
				val virtualDirectory = new VirtualDirectory(vdPathPrefix, None)
				//val wrappedDirectory = VirtualDirectoryWrapper.wrap(virtualDirectory, outputListener)

				val writtenClasses = compilerService.compileToDir(charArrays, virtualDirectory)
				logger.debug("virtualDirectory "+virtualDirectory.size)
				var potentialActivator: Option[String] = None
				for (writtenClass <- writtenClasses) {
					val fullPath = writtenClass.path
					val path = fullPath.substring(vdPathPrefix.length+1)
					if (path.endsWith("Activator.class")) {
						potentialActivator = Some(path.substring(0, path.lastIndexOf('.')).replace('/', '.'))
					}
					tinyBundle.add(path, new ByteArrayInputStream(writtenClass.toByteArray))
				}
		    potentialActivator
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

			val potentialActivator = if (scalaSourceDir.exists) {
				compileDir(scalaSourceDir)
			} else {
				logger.debug("No source dir "+scalaSourceDir)
				None
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
			potentialActivator match {
				case Some(s) =>  tinyBundle.set("Bundle-Activator", s)
				case _ => ;
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
				logger.debug("wathcing "+dir)
				val (triggered, newWatchState) =
					SourceModificationWatch.watch(sourcePath**(-HiddenFileFilter), 1, watchState)(stopped)
				if (!stopped) {
					try {
						updateBundle()
					} catch {
						case e => logger.warn("Exception compiling", e)
					}
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

	val sourceBundleUriPrefix = "bundledevtool:"

}
