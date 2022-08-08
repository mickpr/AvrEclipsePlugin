package de.innot.avreclipse.devexp;

import java.util.ArrayList;

public class AvailableMCUsListEntity {
	public String Name;  // name of chip. eg. ATmega32
	public String Type;  // name of type eg. ATmega,ATXmega,ATtiny
	public String FLASH; // amount of flash memory in bytes
	public String RAM;   // amount of memory RAM
	public String EEPROM; // amount of EEPROM
	public String Freq;   // frequency max
	public String Vcc;  // Vcc allowed
	public ArrayList<String> Packages; // list of available package names - np. TQFP44, DIP40... etc.
	
	
	public AvailableMCUsListEntity() {
		this.Name = "";
		this.Type = "";
		this.FLASH= "";
		this.RAM = "";
		this.EEPROM="";
		this.Freq="";
		this.Vcc="";
		this.Packages = new ArrayList<String>();
	}
}
