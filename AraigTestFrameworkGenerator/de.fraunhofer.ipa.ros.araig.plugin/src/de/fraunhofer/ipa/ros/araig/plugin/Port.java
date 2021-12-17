package de.fraunhofer.ipa.ros.araig.plugin;

public class Port {
	private String name = "";
	private String msg_type  = "";
	private String description  = "";
	
	public String getName() {
        return name;
    }
    public void setTopic(String name) {
        this.name = name;
    }
	public String getMsgType() {
        return msg_type;
    }
    public void setMsgType(String msg_type) {
        this.msg_type = msg_type;
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
	@Override
	public String toString() {
		return "{name: "+ name
				+ "\n msg type: " + msg_type 
				+ "\n description: " + description
				+"\n }";
	}
}
