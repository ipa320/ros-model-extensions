## Welcome to the RosTooling manual

The ROStooling toolchain comprises a series of tools, plugins and scripts developed to facilitate the use of ros-model, a family of DSLs for the formal description of components and systems developed for the ROS (Robot Operating System) framework. 

The following diagram shows the overview of the RosTooling blocks: 

![RosTooling blocks overview](images/RosToolingExtensionsDiagram.png)

The purpose of this document is to describe the extension options of RosTooling by developing plugins for each of its blocks.  

With this concept we aim to establish a federative development process to extend the tooling, which means that anyone can contribute by adding new functionalities to RosTooling but without modifying the core. 

The following sections that go through all the blocks one by one are organized as follows, a first sub-section defines how the main module is developed, the it continues with the developer's perspective and the knowledges required to develop an extension as well as an estimation of the difficulty. In the next sub-section we analyze the technical possibilities that exist for extensibility, finally each section ends with, if available, a list of existing extensions that can be used as examples or templates. 

### METAMODELS (ROS-MODEL) EXTENSIONS 

#### Core component

Ros-model is composed by a family of metamodels, described by Ecore files, and their derivated DSLs, written using the format Xtext.  

The metamodels that constitute ros-model are:

- [PrimitivesTypes.ecore](https://github.com/ipa320/ros-model/blob/master/plugins/de.fraunhofer.ipa.ros/model/PrimitivesTypes.ecore) 
- [Ros.ecore](https://github.com/ipa320/ros-model/blob/master/plugins/de.fraunhofer.ipa.ros/model/ros.ecore) 
- [ComponentInterface.ecore](https://github.com/ipa320/ros-model/blob/master/plugins/de.fraunhofer.ipa.componentInterface/model/componentInterface.ecore) - it will be deprecated - 
- [Rossystem.ecore](https://github.com/ipa320/ros-model/blob/master/plugins/de.fraunhofer.ipa.rossystem/model/rossystem.ecore) 
- [Urdf.ecore](https://github.com/ipa320/kinematics-model/blob/main/de.fraunhofer.ipa.kinematics/model/urdf.ecore) 
- [Xacro.ecore](https://github.com/ipa320/kinematics-model/blob/main/de.fraunhofer.ipa.kinematics/model/xacro.ecore) 

Each of them has an associated Xtext project, where the DSLs grammar, the validators rules and the compiler engines are implemented. 

#### Developer perspective and profile 

For the development of an extension plugin for the metamodels and languages, the developer requires a good knowledge of the Eclipse Modelling Framework (https://www.eclipse.org/modeling/emf/) and Xtext (https://www.eclipse.org/Xtext/). The level of difficulty of the development of this type of extensions is high.

#### Concept for extension 

Unfortunately, the traditional EMF projects implementation doesn’t support an easy, not even clean, way to extend the metamodels.  

##### Include and link a ros metamodel or DSL 

The only solution we can suggest is to include one of our metamodels while creating a new ecore file and link its attributes. For the Xtext implementation we recommend to set only references to the fields from our models. But unfortunately, this method is not a real language extension.  

<font color="green">… to be written …. </font>

##### Extend a ros metamodel or DSL  

<font color="red">NOT RECOMMENDED</font> - This is doable but complex (Ecore+Xtext+Xtend). The Xtext part will very likely duplicate a lot of code. 

#### Available examples and tutorials 

##### Include and link a ros metamodel or DSL 

<font color="green">… to be done, deployment model …. </font>

##### Extend a ros metamodel or DSL 

NO EXAMPLES AVAILABLE OR PLANNED


### EXTRACTION AND INTERPRETATION EXTENSIONS 

#### Core component
This section includes all those extensions that allow to create either automatically, semi-automatically or manually models that conform to the rules of the ros-model constituent DSLs.  

#### Developer perspective and profile 
Since the information that will be formalized using our models can come from many different sources, there is a wide variety of possible implementations. But we have created a set of common and reusable mechanisms. The most practical of these mechanisms is a Python API that allows to generate automatically the models. The level of difficulty for the integration of this API es relatively low for developer with Python experience. 

#### Concept for extension 

##### Python API

The ros_model_parser package (https://github.com/ipa320/ros_model_parser) is a Python module that connects the Xtext DSLs implementation to the Python programming language. The current version of the parser package allows the generation of components (.ros files) and system (.rossystem files) models. 

See the following simple example:

```
from ros_model_generator.rosmodel_generator import RosModelGenerator
from ros_metamodels.ros_metamodel_core import RosModel, Package, Node, Artifactimport rospy
def ros_model_generator_test():     ros_model = RosModelGenerator() 

    node = Node("test_node") 
    node.add_publisher("my_pub","std_msgs/Bool") 
    node.add_parameter("myIntParam", None, None, 25) 
    ros_model.create_model_from_node('my_ros_pkg',"test",node) 
    ros_model.generate_ros_model('/tmp/test.ros') 

if __name__ == '__main__': 

    try:    ros_model_generator_test() 
    except rospy.ROSInterruptException: 
      	  pass
```

Mainly the developer has to import the “RosModelGenerator” class and call its functions to create the attributes of the model. Once the model is completed, with the function generate_ros_model that takes as argument the full path to save the final model, the defined model will be automatically created. 

##### M2M techniques

EMF provides mechanisms that can be used to support migration  of data between different versions of a model (or between two different models, for that matter): 

- Use Ecore2Ecore and Ecore2XML to define a mapping between the different model(s) (versions) 
- Use Ecore2XMLExtendedMetaData with save/load options to handle unrecognized data 
- Use a resource handler to pre/post-process data that cannot be mapped automatically 

Ecore2Ecore Mappings: 

- Describe a mapping between two Ecore models 
- Can be created from an Ecore (*.ecore) model via the ‘Map To Ecore...’ context menu item in the Package Explorer or Resource Navigator 
- Typically used as a development-time artifact (only) 
- Can include one-to-one, one-to-many, many-to-one, many-to-many, and one-sided mappings 
- Only one-to-one and one-sided (one-to-none, none-to-one) mappings are useful for data migration 

Ecore2XML Mappings: 

- Describe a mapping between an Ecore model and its XML representation 
- Can be generated from an Ecore2Ecore (*.ecore2ecore) model via the ‘Generate Ecore to XML Mapping...’ context menu item in the Package Explorer or Resource Navigator 
- Often used as a run-time artifact (in conjunction with Ecore2XMLExtendedMetaData) 
- Can include one-to-one and many-to-one mappings, but only the former are useful for data migration 

Ecore2XMLExtendedMetaData: 

- Can be used with the OPTION_EXTENDED_META_DATA save/load option defined on XMLResource to affect how data are serialized/deserialized 
- ´Will consult registered Ecore2XML mappings to determine the XML representation of objects 

##### Leveraging existing parsers to ros-model 

The URDF file is parsed with urdf_parser_py (https://github.com/ros/urdf_parser_py), which parses the XML elements as xml.etree. ElementTree (https://docs.python.org/3/library/xml.etree.elementtree.html). Based on the XML tags, corresponding URDF objects are instantiated.  

The top-level URDF object is Robot which consists of child elements like links and joints. 

This Robot object is passed to the kinematics model generator, which iterates through the children and dumps the DSL grammar text for each element. 

<font color="green">… to be completed </font>


#### Available examples and tutorials 

- M2M from other MDE approaches (using EMF) 
- Static code analysis (using analyzers like HAROS) 
- Runtime interpreters 
- Parsers from other "models" (i.e., URDF) 

<font color="green"> extra??</font>
- URDF parser (python) links to generator (single library) 
- Import URDF parser which uses existing libraries with additional api and then call the kinematics_model_generator (python) to generate kinematics model file (model-to-model using python). 

### VALIDATION EXTENSIONS 

#### Core component

The validation part of our solution is handled by Xtext. All the ROSModel Xtext projects contain a Xtend file under the path ProjectName/validation where the compiler rules are implemented. 

#### Developer perspective and profile

To add new validation rules some knowledge about Xtend is preferred but not mandatory.	Tutorial: https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation  

#### Concept for extension

<font color="green">… to be completed </font>
- Best Practices influencers 
- Rules validators 
- Checkers for properties 

#### Available examples and tutorials

<font color="green">… to be completed </font>
Template: https://github.com/ipa320/ros-model-extensions/tree/master/templates/de.fraunhofer.ipa.ros.extravalidator 

### GENERATION EXTENSIONS 

<font color="green">… to be written …. </font>

#### Core component

- Generate code from the models 
- This is relatively easy to be supported by add-in plugins. For eclipse we have just to create the command (how the generator will be called, I.e., right click on the model, a button from the toolbar...) and then connect the handler of the command to the code generator written in Xtend. The RosTooling contains already a couple of examples. 
- This category can include M2M conversions 

#### Developer perspective and profile 
<font color="green">… to be written …. </font>

#### Concept for extension 

- Automatic from Model to model, source code, launch files.... 
- From design model, add a helper GUI and generate artifacts 

#### Available examples and tutorials 

- Ros1 and Ros2 cpp code generators 
- SeRoNet – ROS mixed ports 
- Deployment artifacts generator 
- Observers generators 
- (ARAIG test framework generator?) 
- Documentation generator 
- ROS1-ROS2 bridges generator 
