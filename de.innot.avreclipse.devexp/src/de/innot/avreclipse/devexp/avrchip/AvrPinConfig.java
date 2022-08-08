/******************************************************************************
 * Konfiguracja PINów, zapamietujaca wybrane piny sposrod dostepnych.   
 * 
 * 
 * 
 */

package de.innot.avreclipse.devexp.avrchip;

import java.util.ArrayList;


public class AvrPinConfig {
	private Integer pinNumber;
	private ArrayList<String> pinNames;
	private ArrayList<String> pinResources;
	private ArrayList<String> pinDescriptions;
	private boolean pinIsInput;
	private boolean pinIsPullUpOrHighState;
	private Integer selectedPinIndex; // wybrany pin (nazwa, zasob, oraz opis)

	public AvrPinConfig() {
		this.pinNumber=1;
		this.pinNames = new ArrayList<String>();
		this.pinResources = new ArrayList<String>();
		this.pinDescriptions = new ArrayList<String>();
		this.pinIsInput = true;
		this.pinIsPullUpOrHighState = false;
		this.selectedPinIndex =0; //domyslnie pierwszy wczytany jest (ArrayList jest zero indexed) 
	}
	
	public Integer getPinNumber() {
		return this.pinNumber;
	}

	public void setPinNumber(Integer pinNumber) {
		this.pinNumber = pinNumber;
	}
	
	public ArrayList<String> getPinNames() {
		return this.pinNames;
	}

	public void setPinNames(ArrayList<String> pinNames) {
		this.pinNames = pinNames;
	}

	public Integer getSelectedPinIndex() {
		return this.selectedPinIndex;
	}

	public void setSelectedPinIndex(Integer selectedPinIndex)  {
		if (selectedPinIndex>=0 && selectedPinIndex<pinNames.size()) {
			this.selectedPinIndex = selectedPinIndex;
		}
		else {
			System.out.println("ERROR ------------> setSelectedPinIndex value out of bounds"); 
		}
	}

	public void setSelectedPinIsInput(boolean isInput) {
		this.pinIsInput = isInput;
	}
	public void setSelectedPinIsPullUpOrHighState(boolean isPullUpOrHighState) {
		this.pinIsPullUpOrHighState= isPullUpOrHighState;
	}
	
	
	public String getSelectedPinName() {
		//System.out.println("selected PinIndex " + selectedPinIndex);
		return this.pinNames.get(selectedPinIndex);
		
	}
	
	public String getSelectedPinDescription() {
		return this.pinDescriptions.get(selectedPinIndex);
	}
	
	public String getSelectedPinResouce() {
		return pinResources.get(selectedPinIndex);
	}
	
	public boolean getSelectedPinIsInput() {
		return this.pinIsInput;
	}

	public boolean getSelectedPinIsPullUpOrHighState() {
		return this.pinIsPullUpOrHighState;
	}
	
	
	public void AddNewFunction(String pinName, String pinResource, String pinDescription) {
		this.pinNames.add(pinName);
		this.pinResources.add(pinResource);
		this.pinDescriptions.add(pinDescription);
		this.pinIsInput=true;	//default values input and no pullup
		this.pinIsPullUpOrHighState=false;
	}

	public void ClearAllFunctions() {
		this.pinNames.clear();
		this.pinResources.clear();
		this.pinDescriptions.clear();
		this.pinIsInput=true;  // default pin is input and state is low (or no pull up)
		this.pinIsPullUpOrHighState = false;
		this.selectedPinIndex=0; // first (default) selected
	}
	
//	public Integer SelectNextFunction(Integer PinNumer) {
//		if (this.selectedPinIndex >= this.pinNames.size()) { 
//			this.selectedPinIndex=0; }
//		else {
//			this.selectedPinIndex++;
//		}
//		return this.selectedPinIndex;
//	}
	
	public Integer getSelectedIndex() {
		return this.selectedPinIndex;
	}

	public boolean setSelectedIndex(Integer index) {
		if (index > pinNames.size()) {
			selectedPinIndex=0;
			return false; //error
		} 
		else {
			this.selectedPinIndex = index;
			return true; //ok
		}
	} 
	
} // end of class


