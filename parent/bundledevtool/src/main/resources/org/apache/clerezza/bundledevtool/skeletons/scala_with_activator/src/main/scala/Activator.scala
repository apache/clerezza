package skeleton

import org.apache.clerezza.osgi.services.ActivationHelper

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends ActivationHelper {
	registerRootResource(new HelloWorld(context))
	registerRenderlet(new HelloWorldMessageRenderlet)
}
