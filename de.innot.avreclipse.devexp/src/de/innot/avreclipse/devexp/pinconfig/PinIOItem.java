package de.innot.avreclipse.devexp.pinconfig;

public class PinIOItem {
	
	private int number;
    private String port;
    private String name;
    private boolean isInput;
    private boolean isPullUp;
    private String label;	
	
    public PinIOItem(int number, String port, String name, boolean isInput, boolean isPullUp, String label) {
    	this.number = number;
        this.port = port;
        this.name = name;
        this.isInput = isInput;
        this.isPullUp = isPullUp;
        this.label = label;	
    }
    
    public PinIOItem(String[] attributes) {
    	try {
        	this.number = Integer.parseInt(attributes[0]);
            this.port = attributes[1];
            this.name = attributes[2];
            this.isInput = (attributes[3].equals("true")?true:false);
            this.isPullUp = (attributes[4].equals("true")?true:false);
            this.label = attributes[5];	
    	} catch (Exception e) {
    		System.out.println("error while parsing attributes");
    	}
    }
    
    
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInput() {
		return isInput;
	}

	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	public boolean isPullUp() {
		return isPullUp;
	}

	public void setPullUp(boolean isPullUp) {
		this.isPullUp = isPullUp;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


    @Override
    public String toString() {
        return "PIN number=" + number+ ", port=" + port + ", name="
                + name+ ", " + (isInput?"Input":"Ouptut") +  " " + (isPullUp?"PullUp":"") + ", Label=" + label;
    }
}