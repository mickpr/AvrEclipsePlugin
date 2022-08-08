package de.innot.avreclipse.devexp.avrchip;

import java.util.ArrayList;

import de.innot.avreclipse.devexp.PinFuncDescr;

// 1 - zasob (np pin) dostêpny do wyboru
// 2 - zasob wybrany (dostêpny do odznaczenia)
// 3 - zasob niedostêpny (np. wybranie funkcji deaktywuje mozliwoœc wybrania danego pinu jako I/O).
enum AvrResourceUsage{ FREE, USED, LOCKED }; 

public class AvrResource implements Comparable<AvrResource> {
	public String ResourceName;
	//public String FunctionName;
	//public String FunctionDescription;
	public AvrResourceUsage   FunctionUsage; // used (selected), free (possible to selection) or disabled (not selected, and not possible to selection) 
//	public ArrayList<Integer> Pins;  // lista pinow i numer funkcji podleg³ych danemu zasobowi
//	public ArrayList<String> PinFunctionalName; // eg. PB0, or ADC2, or RX, TX
//	public ArrayList<Integer> PinFunctionNumber; // numer funkcyjny piny 0..n (pod ktorym numerem jest dana funkcja pinu)  
	public ArrayList<PinFuncDescr> Pins;
	
	public AvrResource() {
		ResourceName="";
		//FunctionName="";
		//FunctionDescription="";
		FunctionUsage=AvrResourceUsage.USED;
		Pins = new ArrayList<PinFuncDescr>(); // initialize ArrayList
	}

	public AvrResource(String Name) {
		ResourceName=Name;
		//FunctionName="";
		//FunctionDescription="";
		FunctionUsage=AvrResourceUsage.USED;
		Pins = new ArrayList<PinFuncDescr>(); // initialize ArrayList
	}	
		
	public void addPinNumber(PinFuncDescr pin) {
		Pins.add(pin);
	}

	public int compareTo(AvrResource argument) {
		return argument.ResourceName.compareToIgnoreCase(this.ResourceName);
	}
}

