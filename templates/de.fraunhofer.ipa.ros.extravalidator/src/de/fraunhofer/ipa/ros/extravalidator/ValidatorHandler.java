package de.fraunhofer.ipa.ros.extravalidator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import de.fraunhofer.ipa.ros.extravalidator.RosvalidatorExtra;
import ros.PackageSet;
import ros.Artifact;
import ros.Node;
import ros.Package;

public class ValidatorHandler extends AbstractHandler implements IHandler {
	 
	  @Inject
	  private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;
	 
	  @Inject
	  IResourceDescriptions resourceDescriptions;
	 
	  @Inject
	  IResourceSetProvider resourceSetProvider;

	static Map<String, OutputConfiguration> getOutputConfigurationsAsMap(IOutputConfigurationProvider provider) {
		Map<String, OutputConfiguration> outputs = new HashMap<String, OutputConfiguration>();
		for(OutputConfiguration c: provider.getOutputConfigurations()) {
			outputs.put(c.getName(), c);
		}
		return outputs;
	}

	  @Override
	  public Object execute(ExecutionEvent event) throws ExecutionException {
	 
	    ISelection selection = HandlerUtil.getCurrentSelection(event);
	    if (selection instanceof IStructuredSelection) {
	      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	      Object firstElement = structuredSelection.getFirstElement();
	      if (firstElement instanceof IFile) {
	        IFile file = (IFile) firstElement;
	        IProject project = file.getProject();
            
	        URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	        ResourceSet rs = resourceSetProvider.get(project);
	        Resource r = rs.getResource(uri, true);
	        PackageSet pS = (PackageSet) r.getContents().get(0);
	        Package p = pS.getPackage().get(0);
	        Artifact a = p.getArtifact().get(0);
	        Node n = a.getNode();
	        
	        RosvalidatorExtra val = new RosvalidatorExtra();
	        
	        //Injector injector = Activator.getInstance().getInjector(Activator.DE_FRAUNHOFER_IPA_ROS_ROS);
	        //Injector injector = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("dummy.mydsl")).get(Injector.class);
	        //injector.injectMembers(val);
	        
	        val.checkNameConventionSuffixNode(n);
	 
	      }
	    }
	    return null;
	  }
	 
	  @Override
	  public boolean isEnabled() {
	    return true;
	  }
	}
