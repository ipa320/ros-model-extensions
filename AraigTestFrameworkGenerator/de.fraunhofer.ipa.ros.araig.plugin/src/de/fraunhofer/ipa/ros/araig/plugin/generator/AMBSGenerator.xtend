package de.fraunhofer.ipa.ros.araig.plugin.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import org.eclipse.xtext.generator.OutputConfiguration
import java.util.Set
import java.lang.reflect.Method
import java.util.List
import java.util.stream.Collectors

class CustomOutputProvider implements IOutputConfigurationProvider {
	public final static String DEFAULT_OUTPUT = "DEFAULT_OUTPUT"
	
	override Set<OutputConfiguration> getOutputConfigurations() {
		var OutputConfiguration observer_config = new OutputConfiguration(DEFAULT_OUTPUT)
		observer_config.setDescription("DEFAULT_OUTPUT");
		observer_config.setOutputDirectory("./src-gen/ambs/");
		observer_config.setOverrideExistingResources(true);
		observer_config.setCreateOutputDirectory(true);
		observer_config.setCleanUpDerivedResources(true);
		observer_config.setSetDerivedProperty(true);
		return newHashSet(observer_config)
	}
}

class AMBSGenerator extends AbstractGenerator {
	
	String test_type_name
	String robot_name
	String code_type
	List<String> pub_ports 
	List<String> sub_ports
	List<String> action_clients 
	
	LaunchFileCompiler launch_compiler= new LaunchFileCompiler()
	ConfigFileCompiler config_compiler= new ConfigFileCompiler()
	PackageCompiler pkg_compiler = new PackageCompiler()
	
	def get_test_type_name(String name){
		return test_type_name = name
	}
	
	def get_robot_name(String name){
		return robot_name = name
	}
	
	def get_code_type(String name){
		code_type = name.substring(0, 1).toLowerCase() + name.substring(1)
		if(code_type.equals("c++")){
			code_type = "cplusplus"
		}
	}
	
	def get_pub_ports(List<String> ports){
		pub_ports = ports.stream().collect(Collectors.toList());
	}
	def get_sub_ports(List<String> ports){
		sub_ports = ports.stream().collect(Collectors.toList());
	}
	def get_action_clients(List<String> ports){
		action_clients = ports.stream().collect(Collectors.toList());
	}
	
	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		try {	
			var Method fun = launch_compiler.getClass().getDeclaredMethod(String.join("_", test_type_name, code_type), List, List, List, String, String)
			var CharSequence cq = fun.invoke(launch_compiler, pub_ports,sub_ports, action_clients, test_type_name, robot_name) as CharSequence
			fsa.generateFile(String.join("_", test_type_name, robot_name, code_type) +"/launch/" + test_type_name + ".launch", cq)		
		}catch (NoSuchMethodException e) {
			System.out.println("Please define " + String.join("_", test_type_name, code_type + "in LaunchFileCompiler Class "));
		}
		
		try {	
			var Method fun = config_compiler.getClass().getDeclaredMethod(String.join("_", test_type_name, code_type), List, List, List, String, String)
			var CharSequence cq = fun.invoke(config_compiler, pub_ports,sub_ports, action_clients, test_type_name, robot_name) as CharSequence
			fsa.generateFile(String.join("_", test_type_name, robot_name, code_type) +"/config/" + test_type_name + ".yaml", cq)		
		}catch (NoSuchMethodException e) {
			System.out.println("Please define " + String.join("_", test_type_name, code_type + "in ConfigFileCompiler Class "));
		}
		
		fsa.generateFile(String.join("_", test_type_name, robot_name, code_type) + "/package.xml", pkg_compiler.manifest(test_type_name, robot_name,code_type))		
		fsa.generateFile(String.join("_", test_type_name, robot_name, code_type) +"/CMakeLists.txt", pkg_compiler.cmakeList(test_type_name, robot_name,code_type))		
		
	}
		
}