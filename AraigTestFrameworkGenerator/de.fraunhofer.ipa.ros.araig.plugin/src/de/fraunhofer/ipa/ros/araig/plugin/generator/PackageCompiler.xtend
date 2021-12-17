package de.fraunhofer.ipa.ros.araig.plugin.generator

class PackageCompiler {
	def cmakeList(String test_type_name, String robot_name, String code_type)'''
	cmake_minimum_required(VERSION 2.8.3)
	project(«test_type_name»_«robot_name»_«code_type»)
	find_package(catkin REQUIRED)
	
	'''
	
	def manifest(String test_type_name, String robot_name, String code_type)'''
	<?xml version="1.0"?>
	<package format="2">
	  <name>«test_type_name»_«robot_name»_«code_type»</name>
	  <version>0.0.0</version>
	  <description>The cob_tests package</description>
	
	  <maintainer email="ruichao.wu@ipa.fraunhofer.de">ruichao</maintainer>
	
	  <author email="ruichao.wu@ipa.fraunhofer.de">ruichao</author>
	  <author email="tejas.kumar.shastha@ipa.fraunhofer.de">tejas</author>
	
	  <license>Apache 2.0</license>
	
	  <buildtool_depend>catkin</buildtool_depend>
	
	  <export>
	  </export>
	</package>
	
	'''
}
