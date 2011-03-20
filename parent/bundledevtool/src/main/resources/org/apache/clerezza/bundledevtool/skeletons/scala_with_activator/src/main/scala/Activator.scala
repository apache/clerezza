package skeleton

import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions.asJavaDictionary
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends BundleActivator {

	var helloWorldRegistration: ServiceRegistration = null
	var renderletRegistration: ServiceRegistration = null
	/**
	 * called when the bundle is started, this method initializes the provided service
	 */
	def start(context: BundleContext) {
		//import org.apache.clerezza.shell.OsgiDsl
		//val osgiDsl = new OsgiDsl(context, System.out)
		//import osgiDsl._
		println("activating...")
		val args = scala.collection.mutable.Map("javax.ws.rs" -> true)
		helloWorldRegistration = context.registerService(classOf[Object].getName,
												  new HelloWorld(), args)
		val renderlet = new HelloWorldMessageRenderlet
		val serviceReference = context.getServiceReference(classOf[RenderletManager].getName)
		renderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  renderlet, null)
		println("enjoy it!")
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		helloWorldRegistration.unregister()
		renderletRegistration.unregister()
		println("bye")
	}

}
