package de.innot.avreclipse.devexp.avrchip;

import java.util.BitSet;

public class DDRxPORTx {
	private char letter;
	private BitSet bs_ddr;
	private BitSet bs_port;
	private BitSet bs_bitsUsed;
	private String[] labels;
	
	public DDRxPORTx() {
		this.letter='A';
		this.bs_ddr = new BitSet(8);
		this.bs_ddr.clear();
		this.bs_port = new BitSet(8);
		this.bs_port.clear();
		this.bs_bitsUsed = new BitSet(8);
		this.bs_bitsUsed.clear(); // 0= not used  1= used
		labels = new String[8];
	}

	public void set_used_bit(int bitpos) {
		this.bs_bitsUsed.set(bitpos);
	}
	public void clear_used_bit(int bitpos) {
		this.bs_bitsUsed.clear(bitpos);
	}
	public boolean get_used_bit(int bitpos) {
		return this.bs_bitsUsed.get(bitpos);
	}
	public void set_used_bits_mask(int value) {
		int index=0;
		this.bs_bitsUsed.clear();
	    while (value != 0) {
	      if (value % 2 != 0) {
	        this.bs_bitsUsed.set(index);
	      }
	      ++index;
	      value = value >>> 1;
	    }		
	}
	public int get_used_bits_mask() {
		int value=0; 
	    for (int i = 0; i < 8; ++i) {
	    	value += this.bs_bitsUsed.get(i) ? (1 << i) : 0;
		}
	    return value;	
	}
	
	
	public void set_ddr_bit(int bitpos) {
		this.bs_ddr.set(bitpos);	  // set bit to '1'
	}
	public void clear_ddr_bit(int bitpos) {
		this.bs_ddr.clear(bitpos);	  // set bit to '1'
	}
	public boolean get_ddr_bit(int bitpos) {
		return this.bs_ddr.get(bitpos);
	}
	public int get_ddr() {
		int value=0; 
	    for (int i = 0; i < 8; ++i) {
	    	value += this.bs_ddr.get(i) ? (1 << i) : 0;
		}
	    return value;
	}	
	public void set_ddr(int value) {
		int index=0;
		this.bs_ddr.clear();
	    while (value != 0) {
	      if (value % 2 != 0) {
	        this.bs_ddr.set(index);
	      }
	      ++index;
	      value = value >>> 1;
	    }
	}
	
	public void set_port_bit(int bitpos) {
		this.bs_port.set(bitpos);	  // set bit to '1'
	}
	public void clear_port_bit(int bitpos) {
		this.bs_port.clear(bitpos);	  // set bit to '1'
	}
	public boolean get_port_bit(int bitpos) {
		return this.bs_port.get(bitpos);
	}
	public int get_port() {
		int value=0; 
	    for (int i = 0; i < 8; ++i) {
	    	value += this.bs_port.get(i) ? (1 << i) : 0;
		}
	    return value;
	}
	public void set_port(int value) {
		int index=0;
		this.bs_port.clear();
	    while (value != 0) {
	      if (value % 2 != 0) {
	        this.bs_port.set(index);
	      }
	      ++index;
	      value = value >>> 1;
	    }
	}
	
	public void set_bit_label(int bitNr, String label) {
		labels[bitNr] = label;
		this.bs_bitsUsed.set(bitNr); // set bit used to '1' (used)
	}
	public String get_bit_label(int bitNr) {
		return labels[bitNr];
	}
	
	public void set_port_letter(char letter) {
		this.letter = letter;
	}
	public char get_port_letter() {
		return this.letter;
	}
	
	public String get_pin_name(int bitNumber) {
		return "P" + String.valueOf(this.letter) + String.valueOf((int)bitNumber);
	}
	public String get_pin_label(int bitNumber) {
	
		return labels[bitNumber];
	}
	public String get_pin_alias_or_name(int bitNumber) { 
		if (labels[bitNumber].length()>0) 
			{ return labels[bitNumber]; }
		else
			{ return get_pin_label(bitNumber); }
	}	
	
}
