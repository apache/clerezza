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
package org.apache.clerezza.shell;



import org.osgi.framework.BundleContext
import org.osgi.service.component.ComponentContext;
import java.nio.channels.Channels
import org.osgi.framework.Bundle
import java.io.FileDescriptor
import java.io.FileInputStream
import java.net._
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.util._
import scala.actors.Actor
import scala.actors.Actor._

class ConsoleShell()  {



	var factory: ShellFactory = null
	var bundleContext: BundleContext = null
	var stoppedBundle: Option[Bundle] = None
	var shellOption: Option[Shell] = None
	var interruptibleIn: InterruptibleInputStream = null

	def activate(componentContext: ComponentContext)= {
		bundleContext = componentContext.getBundleContext
		if (("true" != bundleContext.getProperty("clerezza.shell.disable")) &&
		    (System.console != null)) {
			for (bundle <- bundleContext.getBundles;
					if (bundle.getSymbolicName == "org.apache.felix.shell.tui");
					if (bundle.getState == Bundle.ACTIVE)) {
				println("stopping "+bundle);
				bundle.stop()
				stoppedBundle = Some(bundle)
			}
			//this call sets the console terminal to the right settings
			//and it must not be invoked when there is no console input, or the system will stop
			val terminalOption = Some(scala.tools.jline.TerminalFactory.create())
			val in =  Channels.newInputStream(
				(new FileInputStream(FileDescriptor.in)).getChannel());
			interruptibleIn = new InterruptibleInputStream(in)
			val shell = factory.createShell(interruptibleIn, System.out, terminalOption)
			shell.start()
			shellOption = Some(shell)
		}
	}


	def deactivate(componentContext: ComponentContext) = {
		bundleContext = componentContext.getBundleContext
		stoppedBundle match {
			case Some(bundle) => bundle.start()
			case _ =>
		}
		shellOption match {
			case Some(shell) => shell.stop()
			case _ =>
		}
		if (interruptibleIn != null) {
			interruptibleIn.terminate()
		}
	}

	def bindShellFactory(f: ShellFactory) = {
		factory = f
	}

	def unbindShellFactory(f: ShellFactory) = {
		factory = null
	}
	
}