package de.fraunhofer.ipa.ros.extravalidator;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

public class RosValidatorExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	  @Override
	    protected Bundle getBundle() {
	        return Activator.getInstance().getBundle();
	    }
	     
	  @Override
	    protected Injector getInjector() {
	        RosvalidatorExtra val = new RosvalidatorExtra();
	        Injector injector =  Activator.getInstance().getInjector(Activator.DE_FRAUNHOFER_IPA_ROS_ROS);
	        //injector.injectMembers(val);
	        return injector;
	    }
 }
