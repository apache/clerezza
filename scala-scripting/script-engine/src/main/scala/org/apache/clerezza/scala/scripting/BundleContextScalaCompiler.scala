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
package org.apache.clerezza.scala.scripting

import org.osgi.framework.BundleContext
import org.osgi.framework.Bundle
/*import java.io.File
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.{AbstractFile, PlainFile}
import scala.tools.nsc.util._
import java.net._
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver*/
import org.fusesource.scalate._
import osgi.{BundleHeaders, BundleClassPathBuilder, BundleClassLoader}
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ReplGlobal
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.backend.JavaPlatform
import tools.nsc.reporters.{Reporter, ConsoleReporter}
import scala.tools.nsc.util.{ClassPath, MergedClassPath}
import scala.reflect.internal.util.{Position, NoPosition, FakePos}
import scala.runtime.ByteRef
import scala.util.parsing.input.OffsetPosition
import collection.mutable.ListBuffer
import org.osgi.framework.Bundle
import java.io.{PrintWriter, StringWriter, File}
import org.slf4j.scala._

/*
 * unfortunately there seems to be no way to change the classpath, so this doesn't
 * listen to BundleEvents
 * TODO: check if this is still true with Scala 2.20
 */
class BundleContextScalaCompiler(bundleContext : BundleContext,
		settings: Settings, reporter: Reporter)
		extends Global(settings, reporter) with ReplGlobal with Logging { self =>


  override lazy val platform: ThisPlatform = {
    new { val global: self.type = self } with JavaPlatform {
      override lazy val classPath = {
        createClassPath[AbstractFile](super.classPath)
      }
    }
  }

  override def classPath = platform.classPath

  def createClassPath[T](original: ClassPath[T]) = {
    
    var result = ListBuffer(original)
    for (bundle <- bundleContext.getBundles; if bundle.getResource("/") != null) {
      try {
        val files = BundleClassPathBuilder.fromBundle(bundle) 
        files.foreach(file => {
          //debug("Adding bundle " + file + " to the Scala compiler classpath")
          result += original.context.newClassPath(file)
        })
      } catch {
        case e: Exception => logger.debug(e.toString)
      }
      
    }
    new MergedClassPath(result.toList.reverse, original.context)
  }
	/*override lazy val classPath: ClassPath[AbstractFile] = {

		val classPathOrig: ClassPath[AbstractFile]  = new PathResolver(settings).result
		var bundles: Array[Bundle] = bundleContext.getBundles
		val classPathAbstractFiles = for (bundle <- bundles;
										  val url = bundle.getResource("/");
										  if url != null) yield {
			if ("file".equals(url.getProtocol())) {
				new PlainFile(new File(url.toURI()))
			}
			else {
				BundleFS.create(bundle);
			}
		}
		val classPaths: List[ClassPath[AbstractFile]] = (for (abstractFile <- classPathAbstractFiles)
			yield {
					new DirectoryClassPath(abstractFile, classPathOrig.context)
				}) toList

		new MergedClassPath[AbstractFile](classPathOrig :: classPaths,
			   classPathOrig.context)

	}*/

	/*override def rootLoader: LazyType = {
		new loaders.JavaPackageLoader(classPath)
	}*/
}


