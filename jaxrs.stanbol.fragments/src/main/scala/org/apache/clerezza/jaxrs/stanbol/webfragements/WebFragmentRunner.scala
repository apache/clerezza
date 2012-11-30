/*
 * Copyright 2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.jaxrs.stanbol.webfragements

import org.apache.clerezza.osgi.services.ActivationHelper
import org.apache.felix.scr.annotations._
import org.apache.stanbol.commons.web.base.WebFragment
import org.osgi.framework.BundleContext
import org.osgi.service.component.ComponentContext


@Component
@Reference(name = "webFragment", 
           referenceInterface = classOf[WebFragment], 
           cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, 
           policy = ReferencePolicy.DYNAMIC)
class WebFragmentRunner {

  println("constructing");

  private var webFragments: List[WebFragment] = Nil
  private var bundleContext: BundleContext = _
  private var activator: Option[ActivationHelper] = None

  @Activate
  def activate(c: ComponentContext) {
    synchronized {
      println("activating with "+webFragments);
      bundleContext = c.getBundleContext
      registerFragments()
    }
  }
  
  @Deactivate
  def deactivate(c: ComponentContext) {
    synchronized {
      activator.foreach(_.stop(c.getBundleContext))
      activator = None
    }
  }

  private def registerFragments() {
    activator = Some(new ActivationHelper {
      
      for (f <- webFragments) {
        import scala.collection.JavaConverters._
        for (s <- f.getJaxrsResourceSingletons.asScala) {
          registerRootResource(s)
          println("Registered: "+s)
        }
        println(f)
      }
      start (bundleContext)
    })
  }

  protected def bindWebFragment(f: WebFragment) {
    synchronized {
      webFragments ::= f
      activator.foreach { a =>
        a.stop(bundleContext);
        registerFragments()
      }
    } 
  }

  protected def unbindWebFragment(f: WebFragment) {
    synchronized {
      webFragments = webFragments diff List(f)
      activator.foreach { a=>
        a.stop(bundleContext);
        registerFragments()
      }
    }
  }

}
