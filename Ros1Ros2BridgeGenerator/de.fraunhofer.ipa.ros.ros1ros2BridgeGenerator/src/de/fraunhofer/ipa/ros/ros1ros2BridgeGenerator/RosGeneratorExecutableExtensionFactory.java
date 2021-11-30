package de.fraunhofer.ipa.ros.ros1ros2BridgeGenerator;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

public class RosGeneratorExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	  @Override
	    protected Bundle getBundle() {
	        return Activator.getInstance().getBundle();
	    }
	     
	  @Override
	    protected Injector getInjector() {
	        Injector injector =  Activator.getInstance().getInjector(Activator.DE_FRAUNHOFER_IPA_ROS_ROS);
	        return injector;
	    }
 }



