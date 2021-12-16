package de.fraunhofer.ipa.ros.araig.plugin;

import java.util.Arrays;

public class TestCase {
	private String name = "";
	private String description = "";
	private Port[] subscribers;
	private Port[] publishers;
	private Port[] action_clients;
	
	public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
	
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Port[] getPublishers() {
        return publishers;
    }
    public Port[] getSubscribers() {
        return subscribers;
    }
    public Port[] getActionClients() {
        return action_clients;
    }
    @Override
    public String toString() {
        return name + ":{ \n publishers:" + Arrays.toString(publishers) 
        			+ "\n subscribers:" + Arrays.toString(subscribers) 
        			+ "\n action_clients:" + Arrays.toString(action_clients)
        			+ "\n}";
    }
}
