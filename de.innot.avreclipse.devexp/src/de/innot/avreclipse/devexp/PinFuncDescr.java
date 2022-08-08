package de.innot.avreclipse.devexp;

import java.util.Comparator;

public final class PinFuncDescr implements Comparator<PinFuncDescr> {
	public int nr; // numer pinu w ukladzie Avr (1..n)
	public int func_nr; // numer funkcji danego pinu
	public String name; // nazwa danego pinu
	public String descr; //opis danego pinu
	public String value; // wartosc pinu. Np. Dla pinow PAx-PEx "in","out" wejscie i wyjscie  
	
	public PinFuncDescr(int nr, int func_nr, String name, String descr, String value) {
		this.nr=nr;
		this.func_nr= func_nr;
		this.name = name;
		this.descr = descr;
		this.value = value;
	}

    public static Comparator<PinFuncDescr> ChipPinComparator = new Comparator<PinFuncDescr>() {

	public int compare(PinFuncDescr s1, PinFuncDescr s2) {
	   String PinFuncDescr1 = s1.name.toUpperCase();
	   String PinFuncDescr2 = s2.name.toUpperCase();

	   //ascending order
	   return PinFuncDescr1.compareTo(PinFuncDescr2);

	   //descending order
	   //return ChipPin2.compareTo(ChipPin1);
    }};

	@Override
	public int compare(PinFuncDescr arg0, PinFuncDescr arg1) {
		// TODO Auto-generated method stub
		return arg0.name.compareTo(arg1.name);
	}	
	
}
