package de.fraunhofer.ipa.ros.araig.plugin.generator

import java.util.List

class LaunchFileCompiler {
	
	def braking_test_python(List<String> pubs, List<String> subs, List<String> action_servers, String test_type_name, String robot_name) '''
	<launch>
	    <arg name="disable_marvel" default="false"/>
	
	    <rosparam command="load" file="$(find «test_type_name»_«robot_name»_python)/config/«test_type_name».yaml" />
	
	    <arg name="src1" default ="/dev/video2" />
	    <arg name="src2" default ="/dev/video4" />
	    <arg name="port" default="/dev/ttyACM0" />
	<!-- sensors -->
	    <include file="$(find ambs_test_common)/launch/common.launch">
	        <args name="src1" value="$(arg cam1)" />
	        <args name="src2" value="$(arg cam2)" />
	        <args name="port" value="$(arg port)" />
	    </include>
	
	<!-- test runner -->
	    <node pkg="araig_test_runners" type="test_1_braking" name="test_1_runner_node" output="screen">
	    </node>
	
	<!-- interpreters -->
	    <node pkg="araig_interpreters" type="velocity_interpreter" name="velocity_interpreter_node" output="screen">
	        <remap from="/stop" to="/signal/runner/stop_robot"/>
	        <remap from="/start" to="/signal/runner/start_robot"/>
	        <remap from="/velocity" to="«pubs.get(0)»"/>    
	    </node>
	
	<!-- calculators -->
	    <!-- get speed from /odom -->
	    <node name="focus_odom_x" pkg="topic_tools" type="transform" args="«subs.get(0)» /data/robot/odom/twist/linear_x std_msgs/Float64 'm.twist.twist.linear.x'" />
	    
	
        <!-- X Y Z Y P R transform of marvel origin frame wrt map frame. Needs to be physically measured. -->
        <arg name="origin_pos" default="1.52 -2.37 0 0.55 0 0"/>

        <!-- Publish static TF of the origin anchor to map -->
        <node pkg="tf" type="static_transform_publisher" name="marvel_tf" args=" $(arg origin_pos)  /map /marvel_origin 1"/>

        <node pkg="araig_interpreters" type="marvel_location_interpreter" name="marvel_interpreter" output="screen">
            <remap from="/marvel_interpreter/beacon11" to="/data/interpreter/location/robot"/>
        </node>
	
	    <!-- robot_reached_max_vel_node -->
	    <node pkg="araig_calculators" type="comp_param_node" name="robot_reached_max_vel_node" output="screen">
	        <remap from="/in_float" to="/data/robot/odom/twist/linear_x"/>
	        <remap from="/out_bool" to="/signal/calc/robot_has_max_vel"/>
	    </node>
	
	    <!-- robot_has_stopped_node -->
	    <node pkg="araig_calculators" type="comp_param_node" name="robot_has_stopped_node" output="screen">
	        <remap from="/in_float" to="/data/robot/odom/twist/linear_x"/>
	        <remap from="/out_bool" to="/signal/calc/robot_has_stopped"/>
	    </node>
	
	    <!-- braking_distance_node -->
	    <node pkg="araig_calculators" type="diff_pose_temporal_node" name="braking_distance_node" output="screen">
	        <remap from="/in_start" to="/signal/runner/stop_robot"/>
	        <remap from="/in_stop" to="/signal/calc/robot_has_stopped"/>
	        <remap from="/in_pose" to="/data/interpreter/location/robot"/>
	        <remap from="/out_disp_position" to="/data/calc/braking_distance"/>
	    </node>
	
	    <!-- braking_time_node -->
	    <node pkg="araig_calculators" type="diff_time_node" name="braking_time_node" output="screen">
	        <remap from="/in_start" to="/signal/runner/stop_robot"/>
	        <remap from="/in_stop" to="/signal/calc/robot_has_stopped"/>
	        <remap from="/out_duration" to="/data/calc/braking_time"/>
	    </node>
	
	    <!-- logging -->
	    <!-- rosbagger_node -->
	    <node pkg="araig_calculators" type="rosbagger_node" name="folder_bagger_node" output="screen">
	        <remap from="/start" to="/signal/ui/start_test"/>
	        <remap from="/stop" to="/signal/runner/test_completed"/>
	        <remap from="/test_failed" to="/signal/runner/test_failed"/>
	        <remap from="/test_succeeded" to="/signal/runner/test_succeeded"/>
	    </node>
	
	    <!-- param_logger_node -->
	    <node pkg="araig_calculators" type="rosparam_logger_node" name="param_logger_node" output="log">
	        <remap from="/start" to="/signal/ui/start_test"/>
	        <remap from="/stop" to="/signal/runner/test_completed"/>
	    </node>
	
	    <!-- results_logger_node -->
	    <node pkg="araig_calculators" type="results_logger_node" name="results_logger_node" output="log">
	        <remap from="/start" to="/signal/ui/start_test"/>
	        <remap from="/stop" to="/signal/runner/test_completed"/>
	        <remap from="/test_failed" to="/signal/runner/test_failed"/>
	        <remap from="/test_succeeded" to="/signal/runner/test_succeeded"/>
	    </node>
	    
	    <!-- speed plotter -->
	    <node name="plotter" type="rqt_multiplot" pkg="rqt_multiplot"
	        args="--multiplot-config $(find ambs_test_common)/config/plotter/speed_position.xml --multiplot-run-all">
	    </node>
	
	</launch>

	'''
	
	def braking_test_cplusplus(List<String> pubs, List<String> subs, List<String> action_servers, String test_type_name, String robot_name) '''
	TODO
	'''
}