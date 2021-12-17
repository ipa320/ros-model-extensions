package de.fraunhofer.ipa.ros.araig.plugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.gson.Gson;

import componentInterface.ComponentInterface;
import componentInterface.RosActionServer;
import componentInterface.RosPublisher;
import componentInterface.RosSubscriber;
import de.fraunhofer.ipa.ros.araig.plugin.generator.AMBSGenerator;
import de.fraunhofer.ipa.ros.araig.plugin.generator.CustomOutputProvider;
import rossystem.RosSystem;

public class GenerationHandler extends AbstractHandler implements IHandler {
	
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

	@SuppressWarnings("null")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IFile) {
			IFile file = (IFile) firstElement;
			IProject project = file.getProject();

	        final EclipseResourceFileSystemAccess2 fsa = fileAccessProvider.get();
	        fsa.setProject(project);
	        fsa.setOutputConfigurations(getOutputConfigurationsAsMap(new CustomOutputProvider()));
            fsa.setMonitor(new NullProgressMonitor());
            
			URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
			ResourceSet rs = resourceSetProvider.get(project);
			Resource r = rs.getResource(uri, true);
				
			Display display = Display.getDefault();
			Shell shell = display.getActiveShell();

			RosSystem rossystem = (RosSystem)r.getContents().get(0);
			EList<ComponentInterface> roscomponents = rossystem.getRosComponent();
			List<EObject> subscribers = getAllSubscribers(roscomponents);
			List<EObject> publishers = getAllPublishers(roscomponents);
			List<EObject> action_servers = getAllActServers(roscomponents);
			
			// 1: give robot a name
			String robot_name = setRobotName(shell, project);
			
			// 2: select test manifest
			String manifest_path = selectTestManifest(file.getFullPath().toString(), shell, project);
			TestCase[] test_manifest = parseManifest(manifest_path);
			
			// 3: select test case
			String test_name = select_test_model(test_manifest, shell, project);
			TestCase target_test = findTestCase(test_manifest, test_name);
			
			// 4: select ports from the robot system
			List<String> pub_ports = new ArrayList<String>();
			List<String> sub_ports = new ArrayList<String>();
			List<String> action_clients = new ArrayList<String>();
			if (target_test.getPublishers().length != 0) {
				pub_ports = mapPortsFromRobot(target_test, "Publishers", subscribers, shell, project);
				if (pub_ports.size() == 0) {
					return null;
				}
			}
			
			if (target_test.getActionClients() != null && target_test.getActionClients().length != 0 ) {
				action_clients = mapPortsFromRobot(target_test, "ActionClients", action_servers, shell, project);
				if (action_clients.size() == 0) {
					return null;
				}
			}
			
			if (target_test.getSubscribers().length != 0) {
				sub_ports = mapPortsFromRobot(target_test, "Subscribers", publishers, shell, project);
				if (sub_ports.size() == 0) {
					return null;
				}
			}
					
			// 5: ambs type, e.g. c++, python
			Map<String, Integer> code_type = select_code_type(shell, project);
			AMBSGenerator generator = new AMBSGenerator();
			generator.get_test_type_name(test_name);
			generator.get_robot_name(robot_name);
			generator.get_code_type(code_type.keySet().stream().findFirst().get());
			generator.get_pub_ports(pub_ports);
			generator.get_sub_ports(sub_ports);
			generator.get_action_clients(action_clients);

			generator.doGenerate(r, fsa, new GeneratorContext());
		}}
		return null;
	}

	private List<EObject> getPubInterfaces(ComponentInterface componentInterface_model) {
		List<EObject> ROSInterfaces = new ArrayList<EObject>();
		for (RosPublisher role: componentInterface_model.getRospublisher()) {
			ROSInterfaces.add(role);
		}
		return ROSInterfaces;
	}
	
	private List<EObject> getSubInterfaces(ComponentInterface componentInterface_model) {
		List<EObject> ROSInterfaces = new ArrayList<EObject>();
		for (RosSubscriber role: componentInterface_model.getRossubscriber()) {
			ROSInterfaces.add(role);
		}
		return ROSInterfaces;
	}
	
	private List<EObject> getActServerInterfaces(ComponentInterface componentInterface_model) {
		List<EObject> ROSInterfaces = new ArrayList<EObject>();
		for (RosActionServer role: componentInterface_model.getRosactionserver()) {
			ROSInterfaces.add(role);
		}
		return ROSInterfaces;
	}

	private List<EObject> getAllSubscribers(EList<ComponentInterface> roscomponents) {
		List<EObject> RosInterfaces = new ArrayList<EObject>();
		for (int i=0; i<roscomponents.size(); i++) {
			List<EObject>  NewRosInterfaces = getSubInterfaces(roscomponents.get(i));
			for (int j= 0;j<NewRosInterfaces.size();j++) {
				RosInterfaces.add(NewRosInterfaces.get(j));
			}
		}
		return RosInterfaces;
	}
	private List<EObject> getAllPublishers(EList<ComponentInterface> roscomponents) {
		List<EObject> RosInterfaces = new ArrayList<EObject>();
		for (int i=0; i<roscomponents.size(); i++) {
			List<EObject>  NewRosInterfaces = getPubInterfaces(roscomponents.get(i));
			for (int j= 0;j<NewRosInterfaces.size();j++) {
				RosInterfaces.add(NewRosInterfaces.get(j));
			}
		}
		return RosInterfaces;
	}
	
	private List<EObject> getAllActServers(EList<ComponentInterface> roscomponents) {
		List<EObject> RosInterfaces = new ArrayList<EObject>();
		for (int i=0; i<roscomponents.size(); i++) {
			List<EObject>  NewRosInterfaces = getActServerInterfaces(roscomponents.get(i));
			for (int j= 0;j<NewRosInterfaces.size();j++) {
				RosInterfaces.add(NewRosInterfaces.get(j));
			}
		}
		return RosInterfaces;
	}
	
	
	private static String getInterfaceName(EObject RosInterface) {
		String name = RosInterface.toString().substring(RosInterface.toString().indexOf("name:")+6,RosInterface.toString().indexOf(","));
		return name;
	}
	private static String getViewMenuInterfaceName(EObject RosInterface) {
		String name = "["+RosInterface.toString().substring(RosInterface.toString().indexOf("impl.Ros")+8,RosInterface.toString().indexOf("Impl@"))+"]  "+
				getInterfaceName(RosInterface);
		return name;
	}
	
	private String setRobotName(Shell shell, IProject project) {
		InputDialog dialogInput = new InputDialog(shell,"Give the robot a name", "Robot's name ", null, null);
		dialogInput.open();
		String name = dialogInput.getValue();
		return name;
	}
	private Map<String, Integer> select_code_type(Shell shell, IProject project) {
		Map<String, Integer> types = new HashMap<String, Integer>() {{
	        put("Python", 0);
	        put("C++", 1);
		}};

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
		dialog.setElements(types.keySet().toArray());
		dialog.setTitle("Select AMBS Code Type");
		dialog.setMultipleSelection(false);
		dialog.open();
		Map<String, Integer> result = new HashMap<String, Integer>() {{
			put(dialog.getResult()[0].toString(), types.get(dialog.getResult()[0]));
		}};
	  return result;
	}
	
	private String selectTestManifest(String current_path, Shell shell, IProject project) {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(new String [] {"*.json"});
		dialog.setFilterPath(null);
		String manifest_path = dialog.open();
		return manifest_path;
	}
	
	private TestCase findTestCase(TestCase[] manifest, String test_name) {
		for (TestCase test : manifest) {
			if (test.getName().equals(test_name)) {
				return test;
			}
		}
		return null;
	}

	private String select_test_model(TestCase[] manifest, Shell shell, IProject project) {
		List<String> msgs = new ArrayList<String>();
		for(TestCase testcase : manifest) {
			msgs.add(String.join(": ", testcase.getName(), testcase.getDescription()));
		}
	    ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
	    dialog.setElements(msgs.toArray());
	    dialog.setTitle("Select a test type");
	    dialog.setMultipleSelection(false);
	    dialog.open();
	    String res = dialog.getFirstResult().toString();
	    return res.split(":")[0];
	}
	
	private static TestCase[] parseManifest(String manifest_path) {
		TestCase[] manifest = null;
		Gson gson = new Gson(); 		
		try {
			FileReader reader;
			reader = new FileReader(manifest_path);
			
			manifest = gson.fromJson(reader, TestCase[].class);
		}  catch (FileNotFoundException e) {
			System.out.println("Cannot parse");
        }
        
        return manifest;
	}
	
	private static Object mapDialog(String type, List<String> interface_name_list, Port p, Shell shell, IProject project) {
		ElementListSelectionDialog dialogSelect = new ElementListSelectionDialog(shell, new LabelProvider());
		dialogSelect.setElements(interface_name_list.toArray());
		dialogSelect.setTitle("Select " + type + " interfaces");
		dialogSelect.setMessage("Choose the topic: " + p.getDescription());
		dialogSelect.setMultipleSelection(false);
		dialogSelect.open();
		Object selected_from_system = dialogSelect.getFirstResult();
		return selected_from_system;
	}
	
    private static List<String> mapPortsFromRobot(TestCase testcase, String type, List<EObject> RosInterfaces, Shell shell, IProject project) 
    {
    	List<String> topics = new ArrayList<String>();
    	try {
			Method gettype = testcase.getClass().getMethod("get"+type);
			try {
				Port[] ports = (Port[]) gettype.invoke(testcase, null);
				
				List<String> interface_name_list = new ArrayList<String>();
				List<EObject> potential_interfaces = new ArrayList<EObject>();				
				for (Port p: ports) {
					if (type.equals("Publishers")) {
						for (int i=0; i<RosInterfaces.size(); i++) {
								String msgtype = ((RosSubscriber) RosInterfaces.get(i)).getSubscriber().getMessage().getName();
								String msg = p.getMsgType().substring(p.getMsgType().lastIndexOf('/') + 1);
								if (msg.equals(msgtype)) {
									interface_name_list.add(getViewMenuInterfaceName(RosInterfaces.get(i)));
									potential_interfaces.add(RosInterfaces.get(i));
								}
							}
						if (interface_name_list.size() == 0) {
							String value = "Robot doesn't has mapped port. Stopped Generating " + testcase.getName() + "!";
							MessageDialog error_dialog = new MessageDialog(shell, "ERROR", null,
									  	value, MessageDialog.ERROR, new String[] { "Cancel" }, 0);
							error_dialog.open(); 
							}
							else {						
								Object selected_from_system = mapDialog(type, interface_name_list, p, shell, project);
								for (EObject po: potential_interfaces) {
									if((getViewMenuInterfaceName(po)).equals(selected_from_system.toString())) {
										EObject selected_interface = po;
										topics.add(((RosSubscriber) selected_interface).getName());
									}
								}
							}
					}
					if (type.equals("Subscribers")) {
						for (int i=0; i<RosInterfaces.size(); i++) {
								String msgtype = ((RosPublisher) RosInterfaces.get(i)).getPublisher().getMessage().getName();
								String msg = p.getMsgType().substring(p.getMsgType().lastIndexOf('/') + 1);
								if (msg.equals(msgtype)) {
									System.out.println("find it");
									System.out.println(RosInterfaces.get(i).toString());
									interface_name_list.add(getViewMenuInterfaceName(RosInterfaces.get(i)));
									potential_interfaces.add(RosInterfaces.get(i));
								}
							}
						if (interface_name_list.size() == 0) {
							String value = "Robot doesn't has mapped port. Stopped Generating " + testcase.getName() + "!";
							MessageDialog error_dialog = new MessageDialog(shell, "ERROR", null,
									  	value, MessageDialog.ERROR, new String[] { "Cancel" }, 0);
							error_dialog.open(); 
							}
							else {						
								Object selected_from_system = mapDialog(type, interface_name_list, p, shell, project);
								for (EObject po: potential_interfaces) {
									if((getViewMenuInterfaceName(po)).equals(selected_from_system.toString())) {
										EObject selected_interface = po;
										topics.add(((RosPublisher) selected_interface).getName());
									}
								}
							}
					}
					if (type.equals("ActionClients")) {
						for (int i=0; i<RosInterfaces.size(); i++) {
								String msgtype = ((RosActionServer) RosInterfaces.get(i)).getActserver().getAction().getName();
								interface_name_list.add(getViewMenuInterfaceName(RosInterfaces.get(i)));
								potential_interfaces.add(RosInterfaces.get(i));
							}
						if (interface_name_list.size() == 0) {
							String value = "Robot doesn't has mapped Action Server";
							MessageDialog error_dialog = new MessageDialog(shell, "ERROR", null,
									  	value, MessageDialog.ERROR, new String[] { "Cancel" }, 0);
							error_dialog.open();
							}
							else {						
								Object selected_from_system = mapDialog(type, interface_name_list, p, shell, project);
								for (EObject po: potential_interfaces) {
									if((getViewMenuInterfaceName(po)).equals(selected_from_system.toString())) {
										EObject selected_interface = po;
										topics.add(((RosActionServer) selected_interface).getActserver().getName());
									}
								}
							}
					}
				} 
			}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(topics);
    	return topics;
    }
     
	@Override
	  public boolean isEnabled() {
		return true;
	  }
	}
