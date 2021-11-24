package de.fraunhofer.ipa.ros.extravalidator

import org.eclipse.xtext.validation.Check

import ros.Node
import de.fraunhofer.ipa.ros.validation.AbstractRosValidator
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.XtextPackage
import ros.RosPackage

//public class RosvalidatorExtra extends AbstractDeclarativeValidator {

public class RosvalidatorExtra extends AbstractRosValidator {

  public static final String INVALID_NAME = "invalidName";
  
 
  /* CAPITAL LETTERS */
  @Check
  def void checkNameConventionSuffixNode (Node node) {
  	if (!node.name.endsWith("_node")) {
  		System.out.println("Custom rule:The name of the node has to end with the suffix '_node'")
  		error("The name of the node is '" + node.name+ "' it has to end with the suffix '_node'", RosPackage.Literals.NODE, null, INVALID_NAME);
  	}
 } 
}
