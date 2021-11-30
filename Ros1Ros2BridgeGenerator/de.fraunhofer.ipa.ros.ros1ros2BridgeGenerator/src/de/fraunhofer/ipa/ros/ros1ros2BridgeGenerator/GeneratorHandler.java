package de.fraunhofer.ipa.ros.ros1ros2BridgeGenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
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

import ros.Artifact;
import ros.Node;
import ros.Package;
import ros.PackageSet;
import ros.Publisher;
import ros.Subscriber;
import ros.TopicSpec;
import ros.impl.SubscriberImpl;


public class GeneratorHandler extends AbstractHandler implements IHandler {
	 
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

	  @Override
	  public Object execute(ExecutionEvent event) throws ExecutionException {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof IFile) {
				IFile file = (IFile) firstElement;
				IProject project = file.getProject();

				URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
				ResourceSet rs = resourceSetProvider.get(project);
				Resource r = rs.getResource(uri, true);
					
				Display display = Display.getDefault();
				Shell shell = display.getActiveShell();

				PackageSet packageSet = (PackageSet)r.getContents().get(0);
				EList<Package> rosPackage = packageSet.getPackage();
				List<EObject> RosInterfaces = new ArrayList<EObject>();
				for (Package pkg:rosPackage) {
					for (Artifact art:pkg.getArtifact()) {
						for (EObject Interface: getInterfaces(art.getNode())) {
							RosInterfaces.add(Interface);
						}
					}
				}


				ElementListSelectionDialog dialogSelect = new ElementListSelectionDialog(shell, new LabelProvider());
			
				String[] ListofInterfaces = new String[RosInterfaces.size()];
				for (int i=0; i<RosInterfaces.size(); i++) {
					ListofInterfaces[i]=(getViewMenuInterfaceName(RosInterfaces.get(i)));
				}

				dialogSelect.setElements(ListofInterfaces);
				dialogSelect.setTitle("Select the ROS interface to bridge (only available for publishers)");
				dialogSelect.setMessage("!!! This features requires that the option: Project -> Build Automatilly is enable !!!");
				dialogSelect.setMultipleSelection(false);
				dialogSelect.open();

				Object[] results = dialogSelect.getResult();

				String RelativePathTogenerationFolder = "src-gen/bridges/";

				for (Object result_: results) {
					for (EObject ResultInterface: RosInterfaces) {
						if((getViewMenuInterfaceName(ResultInterface)).equals(result_.toString())) {
							EObject SelectedInterface = ResultInterface;
							if (SelectedInterface.getClass().toString().contains("ros.impl.PublisherImpl")) {
								Publisher resulted_pub = (Publisher) SelectedInterface;
								Subscriber sub = new SubscriberImpl();
								sub.setName(resulted_pub.getName());
								sub.setMessage((TopicSpec)resulted_pub.getMessage());

								String bridge_name = resulted_pub.getName().replace("/", "_")+"_bridge";
								String RelativePathTobridgeModel = RelativePathTogenerationFolder+bridge_name+".ros";
								IFile bridgeModelFile = project.getFile(RelativePathTobridgeModel);

								String ros_model =  
										"PackageSet { \n" + 
										"  CatkinPackage rosgraph_monitor {" + 
										"    Artifact "+bridge_name+" {\n" + 
										"      Node { name /"+bridge_name+"\n";
								ros_model+="        Subscribers { \n"+
										"          Subscriber { name '"+sub.getName()+"' message '"+sub.getMessage().getFullname().replace("/", ".")+"'}\n";								
								ros_model+="}}}}}";
								
								byte[] bytes = (ros_model).getBytes();

								// prepare the Xtext generation environment
								ros1ros2BridgeGenerator generator = new ros1ros2BridgeGenerator();
								final EclipseResourceFileSystemAccess2 fsa = fileAccessProvider.get();
								fsa.setProject(project);
								fsa.setOutputConfigurations(getOutputConfigurationsAsMap(new CustomOutputProvider()));
								fsa.setMonitor(new NullProgressMonitor());
								GeneratorContext generatorContext = new GeneratorContext();
								if (!project.getFolder(RelativePathTogenerationFolder).exists()) {
									generator.createXtextGenerationFolder(fsa, generatorContext);
								}
								InputStream source = new ByteArrayInputStream(bytes);
								try {
									if (!bridgeModelFile.exists()) {
										bridgeModelFile.create(source, IResource.NONE, null);
									} else{
										@SuppressWarnings("resource")
										OutputStream outputStream = new FileOutputStream(new File(project.getLocation().toString()+"/"+RelativePathTobridgeModel));
										outputStream.write(bytes);
									}

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								URI uribridgeFile = URI.createPlatformResourceURI(bridgeModelFile.getFullPath().toString(), true);
								
								// Call the python code generator
								ResourceSet rs2 = resourceSetProvider.get(project);
								Resource r2 = rs2.getResource(uribridgeFile, true);
								generator.doGenerate(r2, fsa, generatorContext);
							}
						}}}

			}}
			return null;
		}
	  
		private List<EObject> getInterfaces(Node node) {
			List<EObject> ROSInterfaces = new ArrayList<EObject>();
			for (Publisher RosPub: node.getPublisher()) {
				ROSInterfaces.add(RosPub);
			}
			/**for (RosSubscriber RosSub: componentInterface_model.getRossubscriber()) {
				ROSInterfaces.add(RosSub);
			}
			for (RosServiceClient RosSrvc: componentInterface_model.getRosserviceclient()) {
				ROSInterfaces.add(RosSrvc);
			}
			for (RosServiceServer RosSrvs: componentInterface_model.getRosserviceserver()) {
				ROSInterfaces.add(RosSrvs);
			}
			for (RosActionClient RosActc: componentInterface_model.getRosactionclient()) {
				ROSInterfaces.add(RosActc);
			}
			for (RosActionServer RosActs: componentInterface_model.getRosactionserver()) {
				ROSInterfaces.add(RosActs);
			}*/
			return ROSInterfaces;
		}
	 
		private String getInterfaceName(EObject RosInterface) {
			String name = RosInterface.toString().substring(RosInterface.toString().indexOf("name:")+6,RosInterface.toString().indexOf(")"));
			return name;
		}
	  
	  
		private String getViewMenuInterfaceName(EObject RosInterface) {
			String name = "["+RosInterface.toString().substring(RosInterface.toString().indexOf("ros.impl")+9,RosInterface.toString().indexOf("Impl@"))+"]  "+
					getInterfaceName(RosInterface);
			return name;
		}
	  
	  @Override
	  public boolean isEnabled() {
	    return true;
	  }
	}
