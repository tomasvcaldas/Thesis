package pt.up.fe.specs.contextwa;

import com.yahoo.labs.samoa.instances.Instance;

public class InstanceModified {

	private Instance instance;
	private String realClass;
	
	public InstanceModified(Instance instance, String realClass) {
		this.instance = instance;
		this.realClass = realClass;
	}
	
	public Instance getInstance() {
		return instance;
	}
	
	public void setInstance(Instance newInstance) {
		instance = newInstance;
	}
	
	public String getRealClass() {
		return realClass;
	}

}
