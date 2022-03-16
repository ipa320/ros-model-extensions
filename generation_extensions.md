## GENERATION EXTENSIONS

- [Core component](#core-component)
- [Developer perspective and profile](#developer-perspective-and-profile)
- [Concept for extension](#concept-for-extension)
- [Available examples and tutorials](#available-examples-and-tutorials)

### Core component
 
Ros-model is based on Xtext and uses its infrastructure to provide code generation after model compilation. Therefore, ros-model allows to generate code through its two main models:
- Component models (ros models, file extension .ros)
- System models (rossystem model, file extension .rossystem)

This is relatively easy to be supported by add-in plugins. For eclipse we have just to create the command (how the generator will be called, i.e., right click on the model, a button from the toolbar...) and then connect the handler of the command to the code generator written in Xtend. The RosTooling contains already a couple of examples.
Apart from generating templates, this method can also be used for model-to-model(M2M) transformation.

### Developer perspective and profile

For the development of a new code generator extension plugin, the developer requires a some experience with the [Eclipse Modelling Framework](https://www.eclipse.org/modeling/emf/) and [Xtext](https://www.eclipse.org/Xtext/). However, using as template one of our examples it is relatively easy to get a propotype working with little effort.

### Concept for extension

#### Automatic from one of the models and using a template

The main class to build a generator for a Xtext based DSL is an extension of the [org.eclipse.xtext.generator.AbstractGenerator](https://archive.eclipse.org/modeling/tmf/xtext/javadoc/2.9/org/eclipse/xtext/generator/AbstractGenerator.html). An override method of the pre-defined doGenerate allows the developer to define the workflow of the generator. See the following example (from our [Documentation generator](https://github.com/ipa320/ros-model-extensions/tree/master/DocumentationGenerator/de.fraunhofer.ipa.ros.documentationGenerator)):

```
package de.fraunhofer.ipa.ros.documentationGenerator
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.XtextPackage
import ros.Package
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.OutputConfiguration
import org.eclipse.xtext.generator.IOutputConfigurationProvider

class documentationGenerator extends AbstractGenerator {
  override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
    for (package : resource.allContents.toIterable.filter(Package)){
      fsa.generateFile(package.getName()+"_README.md", package.compile)
      }
    }
```

The line 'resource.allContents.toIterable.filter(Package)' takes all the content of the input resource (the model, it could for the RosTooling the .ros or the .rossystem model) and filter the attribute that will be used as base of the generation, for this example [ros.Package](https://github.com/ipa320/ros-model/blob/master/plugins/de.fraunhofer.ipa.ros.xtext/src/de/fraunhofer/ipa/ros/Ros.xtext#L31). The next line 'fsa.generateFile(package.getName()+"_README.md", package.compile)' calls the generateFile method from the file system access(fsa). The first argument this method takes is the name of the file that will be generated, optionally a second argument can define a new configuration for the generation file(see the interface [IOutputConfigurationProvider](https://archive.eclipse.org/modeling/tmf/xtext/javadoc/2.5/org/eclipse/xtext/generator/IOutputConfigurationProvider.html) for further details) and the thrid argument is the return in form of string of the function compile. A compile funtion developed for Xtend looks like:

```
def compile(Package pkg) {
'''
 This is the name of the input package: «pkg.name»
 The package contains the following artifacts: pkg.artifact
'''
}
```

The oficial documentation of the Xtend framework explains in a 15-minutes tutorial how to use and program the code https://www.eclipse.org/Xtext/documentation/103_domainmodelnextsteps.html.

The following two lines can be used from any Java code to call the code generator (based on our [Documentation generator](https://github.com/ipa320/ros-model-extensions/tree/master/DocumentationGenerator/de.fraunhofer.ipa.ros.documentationGenerator):

```
URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
ResourceSet rs = resourceSetProvider.get(project);
Resource r = rs.getResource(uri, true);

documentationGenerator generator = new documentationGenerator();
generator.doGenerate(r, fsa, new GeneratorContext());
```

Where 'file' is the .ros or .rossystem input file. The example we provide use the typical 'right-click' button trigger to call the generator through a Handler. The plugin definition is availabel under: [plugin.xml](https://github.com/ipa320/ros-model-extensions/blob/master/DocumentationGenerator/de.fraunhofer.ipa.ros.documentationGenerator/plugin.xml) while the handler [GeneratorHandler.java](https://github.com/ipa320/ros-model-extensions/blob/master/DocumentationGenerator/de.fraunhofer.ipa.ros.documentationGenerator/src/de/fraunhofer/ipa/ros/documentationGenerator/GeneratorHandler.java).

### Available examples and tutorials

- [Documentation generator](https://github.com/ipa320/ros-model-extensions/tree/master/DocumentationGenerator/de.fraunhofer.ipa.ros.documentationGenerator)
- [ROS1-ROS2 bridges generator](https://github.com/ipa320/ros-model-extensions/tree/master/Ros1Ros2BridgeGenerator/de.fraunhofer.ipa.ros.ros1ros2BridgeGenerator)
- [ROS1 and ROS2 cpp code generators](https://github.com/ipa320/ros-model/tree/master/plugins/de.fraunhofer.ipa.roscode.generator)
- [SeRoNet – ROS mixed ports](https://github.com/seronet-project/SeRoNet-Tooling-ROS-Mixed-Port/tree/master/de.seronet_projekt.ros.componentGateway.generator)
- [Deployment artifacts generator](https://github.com/ipa320/ros-model/tree/master/plugins/de.fraunhofer.ipa.rossystem.deployment)
- [Observers generators](https://github.com/ipa320/ros-model/tree/master/plugins/de.fraunhofer.ipa.ros.observer.generator)
- [ARAIG test framework generator](https://github.com/ipa320/ros-model-extensions/tree/master/AraigTestFrameworkGenerator/de.fraunhofer.ipa.ros.araig.plugin)

