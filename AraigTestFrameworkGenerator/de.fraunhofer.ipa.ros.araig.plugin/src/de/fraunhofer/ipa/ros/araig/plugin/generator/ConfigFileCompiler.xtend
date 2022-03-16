package de.fraunhofer.ipa.ros.araig.plugin.generator

import java.util.List

class ConfigFileCompiler {
	def braking_test_python(List<String> pubs, List<String> subs, List<String> action_servers, String test_type_name, String robot_name) '''
		runner:
		    timeout_after_max_speed: 1.0
		    timeout_after_complete: 3
		    
		interpreters:
		  velocity_interpreter_node: 
		    max_vel: TODO
		    rate: 50
		
		calculators:
		  dest_dir: "TODO"
		  robot_type: "«robot_name»"
		  test_type: "«test_type_name»"
		
		  robot_reached_max_vel_node: 
		    param: 0.3
		    tolerance: 0.1
		  
		  robot_has_stopped_node: 
		    param: 0.0
		    tolerance: 0.01
		
		  braking_time_node:
		    if_log: True
		
		  param_logger_node:
		    start_offset: 0.5
		    stop_offset: 0.5
		    namespaces: 
		      - "runner"
		      - "interpreters"
		      - "calculators"
		  
		  results_logger_node:
		    start_offset: 0.5
		    stop_offset: 0.1
		    logginng_topics: 
		      - /data/calc/braking_time
		      - /data/calc/braking_distance
		
		  folder_bagger_node:
		    start_offset: 1 # sleep after creating folder
		    stop_offset: 2 # sleep before killing
		    blacklist:
		      - compressed
		      - torso
		      - mimic
		      - bms
		      - collision_velocity_filter
		      - arm
		      - light 
		      - docker
		      - dashboard_agg
		      - gripper
		      - sound
		      - base
		      - script_server
		      - map
		      - scan
		      - sensorring
		      - image_view

	'''
	def braking_test_cplusplus(List<String> pubs, List<String> subs, List<String> action_servers, String test_type_name, String robot_name) '''
	TODO
	'''
}