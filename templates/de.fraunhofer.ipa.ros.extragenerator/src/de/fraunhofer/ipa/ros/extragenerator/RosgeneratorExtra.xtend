package de.fraunhofer.ipa.ros.extragenerator

import ros.Node
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.XtextPackage
import ros.RosPackage
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.OutputConfiguration
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import java.util.Set

//public class RosvalidatorExtra extends AbstractDeclarativeValidator {

class CustomOutputProvider implements IOutputConfigurationProvider {
	public final static String EXTRA_GENERATOR_OUTPUT = "EXTRA_GENERATOR_OUTPUT"
	
	override Set<OutputConfiguration> getOutputConfigurations() {
		var OutputConfiguration observer_config = new OutputConfiguration(EXTRA_GENERATOR_OUTPUT)
		observer_config.setDescription("EXTRA_GENERATOR_OUTPUT");
		observer_config.setOutputDirectory("./src-gen/reports/");
		observer_config.setOverrideExistingResources(true);
		observer_config.setCreateOutputDirectory(true);
		observer_config.setCleanUpDerivedResources(true);
		observer_config.setSetDerivedProperty(true);
		return newHashSet(observer_config)
	}
}

class RosgeneratorExtra extends AbstractGenerator {
	
	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
			for (node : resource.allContents.toIterable.filter(Node)){
				fsa.generateFile(node.getName()+".txt",CustomOutputProvider::EXTRA_GENERATOR_OUTPUT,node.compile)
				}
			}

def compile(Node node) {
'''
Node: «node.name»
#########
  Publishers:
  
	«FOR pub: node.publisher»
	  «pub.name» : «pub.message.fullname»
	«ENDFOR»
#########
  Subscribers:
  
	«FOR sub: node.subscriber»
	  «sub.name» : «sub.message.fullname»
	«ENDFOR»
#########
  Service servers:
  
	«FOR srv: node.serviceserver»
	  «srv.name» : «srv.service.fullname»
	«ENDFOR»
#########
  Service clients:
  
	«FOR srv: node.serviceclient»
	  «srv.name» : «srv.service.fullname»
	«ENDFOR»
#########
  Action servers:
  
	«FOR act: node.actionserver»
	  «act.name» : «act.action.fullname»
	«ENDFOR»
#########
  Actions clients:
  
	«FOR act: node.actionclient»
	  «act.name» : «act.action.fullname»
	«ENDFOR»
#########
  Parameters:
  
	«FOR param: node.parameter»
	  «param.name» : «param.value»
	«ENDFOR»
#########

'''
}

}
