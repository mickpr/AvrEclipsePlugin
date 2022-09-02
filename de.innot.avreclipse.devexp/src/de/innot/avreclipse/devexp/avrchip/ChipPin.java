package de.innot.avreclipse.devexp.avrchip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.wb.swt.SWTResourceManager;

import de.innot.avreclipse.devexp.DeviceExplorerCore;
import de.innot.avreclipse.devexp.utils.PinLocation;

// klasa do rysowania PIN-u
public final class ChipPin {
	public int number;
	public String name; // name=function short np. DDRA
	public String descr; // description of function 
	public PinLocation orient;
	public int dx;
	public int dy;
	public Color color;  //color of background
	 
	public ChipPin(int Number, String Name, int X, int Y, PinLocation orient) {
		this.dx= X;
		this.dy= Y;
		this.number=Number;
		this.name = Name;
		this.orient=orient;
		this.color = SWTResourceManager.getColor(SWT.COLOR_GRAY);
	}
	
/*	
	// lokacja pinu: w zaleznosci od lokacji tak beda dukowane napisy
	public void CreatePin(GC gc,int pin_number, String name, PinLocation pin_location, int x,int y,Color  color) {
		gc.setFont(SWTResourceManager.getFont("Microsoft Sans Serif", 8, SWT.NORMAL));
		gc.setBackground(color);
		if (pin_location==PinLocation.LEFT || pin_location==PinLocation.RIGHT) {
			gc.fillRectangle(x, y, 20, 15);
			gc.drawRectangle(x, y, 20, 15);
			gc.drawText(Integer.toString(pin_number),x+8-(pin_number>9?3:0), y+1);
			if (pin_location==PinLocation.LEFT) gc.drawText(name,x-10-gc.stringExtent(name).x,y+1,true);
			if (pin_location==PinLocation.RIGHT) gc.drawText(name,x+30,y+1,true);
		} else {
			gc.fillRectangle(x, y, 15,20);
			gc.drawRectangle(x, y, 15,20);

			Transform oldTransform = new Transform(gc.getDevice());  
	        gc.getTransform(oldTransform);
			Transform tr = new Transform(gc.getDevice());
			tr.translate(x, y);
	        tr.rotate(-90);
	        tr.translate(-x,-y);
	        
	        gc.setTransform(tr);
			if (pin_location==PinLocation.BOTTOM) gc.drawText(name,x-30-gc.stringExtent(name).x,y+1,true);
			if (pin_location==PinLocation.TOP) gc.drawText(name,x+10,y+1,true);
	        gc.setTransform(oldTransform);

			tr.dispose();
			oldTransform.dispose();
			gc.drawText(Integer.toString(pin_number),x+4-(pin_number>9?3:0), y+1);
		}
		
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.setFont(SWTResourceManager.getFont("Microsoft Sans Serif", 8, SWT.NORMAL));
	}
*/
	// lokacja pinu: w zaleznosci od lokacji tak beda dukowane napisy
	public void CreatePinWithWideDescription(DeviceExplorerCore core, GC gc,int pin_number, PinLocation pin_location, int x,int y,Color  color) {
		Color old_color;
		
		// ktory ciag uzupelniamy 0 - normal1, 1-bold 2-normal2
		int ktoryciag=0;
		
		// GPIO1/PIN2/BOLD/PIN4/PIN5
		// 0000  1111 ---- 2222 

		String normal1="";
		String bold="";
		String normal2="";
		
		int test_selected_index=0;
		int sizeOfAll;
		
		// musimy miec poukladana liste funkcji w tych 3 ciagach.
		// srodkowy (bold) pogrubiony reprezentuje domysln¹ funkcje pinu
		for(AvrPinConfig apc : core.selectedChip.avrPinsConfig) {
			if (apc.getPinNumber()==pin_number) 
			{
				test_selected_index=0;				
				for (String sss: apc.getPinNames()) {
					
					if (test_selected_index == apc.getSelectedIndex()) {
						bold = sss;
						ktoryciag=2;
						test_selected_index++;
						continue;
					}
					if (ktoryciag==0) {
						normal1=sss + "/";
						ktoryciag=1;
						test_selected_index++;
						continue;
					}
					if (ktoryciag==1) {
						normal1=normal1+ sss+"/";
						test_selected_index++;
						continue;
					}
					if (ktoryciag==2) {
						normal2=normal2 + "/"+sss;
						test_selected_index++;
						continue;
					}
					
				}
			}
		}		
		
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		sizeOfAll = gc.stringExtent(normal1+bold+normal2).x; //calculateStringSize(gc, normal1, bold, normal2);
		
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));		
		
		if (pin_location==PinLocation.LEFT || pin_location==PinLocation.RIGHT) {
			gc.setBackground(color);
			gc.fillRectangle(x, y, 20, 15);
			gc.drawRectangle(x, y, 20, 15);
			
			
			//gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			gc.setFont(SWTResourceManager.getFont("Courier New", 8, SWT.NORMAL));

			gc.drawText(Integer.toString(pin_number),x+8-(pin_number>9?3:0), y+1);
			if (pin_location==PinLocation.LEFT) { 
				drawNormalBoldNormalString(gc, normal1, bold, normal2, x-10-sizeOfAll, y+1, pin_location, color); //(name,x-10-gc.stringExtent(name).x,y+1,true);
				if (core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinResouce().contains("PORT") 
					&& core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinIsPullUpOrHighState()) {
					old_color=gc.getBackground();
					gc.setBackground(SWTResourceManager.getColor(255,100,100));
					gc.fillOval(x-5, y+4 , 8, 8);
					gc.setBackground(old_color);
				}			
				
			}
			if (pin_location==PinLocation.RIGHT){ 
				drawNormalBoldNormalString(gc, normal1, bold, normal2, x+30, y+1, pin_location, color); // gc.drawText(name,x+30,y+1,true);
				if (core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinResouce().contains("PORT") 
					&& core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinIsPullUpOrHighState()) {
					old_color=gc.getBackground();
					gc.setBackground(SWTResourceManager.getColor(255,100,100));
					gc.fillOval(x+18, y+4 , 8, 8);
					gc.setBackground(old_color);
				}			
			}
			//gc.setBackground(color);
		} else {
			
			// top and bottom pins are rotated
			gc.setBackground(color);
			gc.fillRectangle(x, y, 15,20);
			gc.drawRectangle(x, y, 15,20);
				Transform oldTransform = new Transform(gc.getDevice());  
		        gc.getTransform(oldTransform);
				Transform tr = new Transform(gc.getDevice());
				tr.translate(x, y);
		        tr.rotate(-90); //bylo -90, ale -92 ³adniej wygl¹da po transformacji.
		        tr.translate(-x,-y);
			        gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
			        gc.setTransform(tr);
					if (pin_location==PinLocation.BOTTOM) drawNormalBoldNormalString(gc, normal1, bold, normal2, x-30-sizeOfAll, y+1, pin_location, color); //gc.drawText(name,x-30-gc.stringExtent(name).x,y+1,true);
					if (pin_location==PinLocation.TOP)  drawNormalBoldNormalString(gc, normal1, bold, normal2, x+10,y+1, pin_location,color); // gc.drawText(name,x+10,y+1,true);
				gc.setTransform(oldTransform);
				tr.dispose();
				oldTransform.dispose();
			
			gc.setBackground(color);
			gc.setFont(SWTResourceManager.getFont("Courier New", 8, SWT.NORMAL));
			gc.drawText(Integer.toString(pin_number),x+4-(pin_number>9?3:0), y+1);
			
			if ((pin_location==PinLocation.TOP) && 
					(core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinResouce().contains("PORT") && 
					 core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinIsPullUpOrHighState())) {
						old_color=gc.getBackground();
						gc.setBackground(SWTResourceManager.getColor(255,100,100));
						gc.fillOval(x+4, y-5 , 8, 8);
						gc.setBackground(old_color);
				}			
			if ((pin_location==PinLocation.BOTTOM) && 
				(core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinResouce().contains("PORT") && 
				 core.selectedChip.avrPinsConfig.get(pin_number-1).getSelectedPinIsPullUpOrHighState())) {
					old_color=gc.getBackground();
					gc.setBackground(SWTResourceManager.getColor(255,100,100));
					gc.fillOval(x+4, y+20 , 8, 8);
					gc.setBackground(old_color);
			}			
		}
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
	}

	
	// wywo³ujemy z 3 ciagami (np. "PA1/PWM1", "TWI", "ICP1").. a ci¹g zostanie uzupelniony o brakujace elementy "/" i wydrukowany 
	// z pogrubieniem drugiego elementu. Drugi element musi istniec wiec jesli ciag jest jedna pozycja (VCC/GND/AREF/..), to zawsze powinien byc
	// wpisany jako druga z pominieciem ("") pierwszej i ("") ostatniej.
	// x i y to pozycja ciagu. Lokalizacja wskazuje do pino z ktorej strony bedzie ciag wyrownany. Do pinu z lewej - ma byc ciag wyrownany do prawej
	/// do pinu z prawej - ma byc rysowany od x,y.  Do pinu z gory i dolu - dodatkowo obrocony. 
	public void drawNormalBoldNormalString(GC gc, String normal1,String bold, String normal2, int x, int y, PinLocation pin_location, Color color){
		// oblicz pozycje tekstu
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		int len1= gc.stringExtent(normal1).x;
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.BOLD));
		int len2=gc.stringExtent(bold).x;
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		//int len3=gc.stringExtent(bold).x;

		// drukuj tekst normalny (*jesli jest)
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		gc.drawString(normal1, x, y);
		//gc.setForeground(new Color(gc.getDevice(), 250,110,110));
		// teraz bold
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.BOLD));		
		gc.drawString(bold, x+len1, y);
		// i znow szare (normalny) (*jesli jest)
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		gc.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		gc.drawString(normal2, x+len1+len2, y);
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}		
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Pin:");
		sb.append(Integer.toString(this.number));
		sb.append(" Name:");
		sb.append(this.name);
		return sb.toString();
	}
}
