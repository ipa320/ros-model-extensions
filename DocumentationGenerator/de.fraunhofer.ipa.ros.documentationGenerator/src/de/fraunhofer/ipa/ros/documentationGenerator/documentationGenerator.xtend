package de.fraunhofer.ipa.ros.documentationGenerator

import ros.Node
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.XtextPackage
import ros.Package
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
		observer_config.setOutputDirectory("./src-gen/");
		observer_config.setOverrideExistingResources(true);
		observer_config.setCreateOutputDirectory(true);
		observer_config.setCleanUpDerivedResources(true);
		observer_config.setSetDerivedProperty(true);
		return newHashSet(observer_config)
	}
}

class documentationGenerator extends AbstractGenerator {
	
	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
			for (package : resource.allContents.toIterable.filter(Package)){
				fsa.generateFile(package.getName()+"_README.md",CustomOutputProvider::EXTRA_GENERATOR_OUTPUT,package.compile)
				}
			}

def compile(Package pkg) {
'''

# «pkg.name»

 * [Introduction](#introduction)
 * [Installation and setup](#installation-and-setup)
 * [Execute the node](#execute-the-nodes)
 * [API](#api)

## Introduction
TBC

## Installation and setup

«IF pkg.class.toString.contains("CatkinPackage")»«IF pkg.fromGitRepo!==null»
```
mkdir -p ~/catkin_ws/src && cd ~/catkin_ws/src
catkin_init_workspace
git clone «pkg.fromGitRepo»
source /opt/ros/$ROS_DISTRO/setup.bash
cd ~/catkin_ws
rosdep update && rosdep install --from-paths ./src -y -i -r
catkin build
source devel/setup.bash
```
«ENDIF»«ENDIF»
«IF pkg.class.toString.contains("AmentPackage")»«IF pkg.fromGitRepo!==null»
```
mkdir -p ~/ros2_ws/src && cd ~/ros2_ws/src
git clone «pkg.fromGitRepo»
source /opt/ros/$ROS_DISTRO/setup.bash
cd ~/ros2_ws
colcon build
source ~/ros2_ws/install/setup.bash
```
«ENDIF»«ENDIF»

## Execute the nodes
«IF pkg.class.toString.contains("CatkinPackage")»«FOR art:pkg.artifact»
### «art.name»
```
rosrun «pkg.name» «art.name»
```
«ENDFOR»«ENDIF»
«IF pkg.class.toString.contains("AmentPackage")»«FOR art:pkg.artifact»
### «art.name»
```
ros2 run «pkg.name» «art.name»
```
«ENDFOR»«ENDIF»

## API

«FOR art:pkg.artifact»
#### Node: «art.node.name»

#####  Publishers:
  
	«FOR pub: art.node.publisher»
	  «pub.name» : «pub.message.fullname»
	«ENDFOR»

#####  Subscribers:
  
	«FOR sub: art.node.subscriber»
	  «sub.name» : «sub.message.fullname»
	«ENDFOR»

#####  Service servers:
  
	«FOR srv: art.node.serviceserver»
	  «srv.name» : «srv.service.fullname»
	«ENDFOR»

#####  Service clients:
  
	«FOR srv: art.node.serviceclient»
	  «srv.name» : «srv.service.fullname»
	«ENDFOR»

#####  Action servers:
  
	«FOR act: art.node.actionserver»
	  «act.name» : «act.action.fullname»
	«ENDFOR»

#####  Actions clients:
  
	«FOR act: art.node.actionclient»
	  «act.name» : «act.action.fullname»
	«ENDFOR»

#####  Parameters:
  
	«FOR param: art.node.parameter»
	  «param.name» : «param.value»
	«ENDFOR»

«ENDFOR»
'''
}

}
