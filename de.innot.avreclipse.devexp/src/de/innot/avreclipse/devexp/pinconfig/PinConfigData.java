package de.innot.avreclipse.devexp.pinconfig;

public class PinConfigData {

	public static enum InOutValue {
	    Input, Output;
	  }
	public static enum PullUpValue {
		Yes, No;
	}
	
	private String pinNr;
	private String port;
	private String pinName;
	private boolean isInput;
	private boolean isPullUp;
	private String label;
	  
	public PinConfigData(String pinNr, String port, String pinName, boolean isInput, boolean isPullUp, String label) {
	    this.setPinNr(pinNr);
	    this.port = port;
		this.pinName = pinName;
		this.isInput = isInput;
		this.isPullUp = isPullUp;
		this.label = label;
	}

	public String getPinNr() { return pinNr; }
	public void setPinNr(String pinNr) { this.pinNr = pinNr; }	  
	
	public String getPort() { return port; }
	public void setPort(String port) { this.port = port; }

	public String getPinName() { return pinName; }
	public void setPinName(String pinName) { this.pinName = pinName; }
	
	public boolean getIsInput() { return isInput; }
	public void setIsInput(boolean isInput) { this.isInput = isInput; }
		     
	public boolean getIsPullUp() { return isPullUp; } 
	public void setIsPullUp(boolean isPullUp) { this.isPullUp= isPullUp; }
				     
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
}
