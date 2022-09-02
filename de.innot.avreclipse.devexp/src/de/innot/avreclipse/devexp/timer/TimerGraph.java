package de.innot.avreclipse.devexp.timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TimerGraph extends Canvas {
	public int width, height;
	GC g;
	
	public int textlegendsize=100;
	
	public TimerGraph(Composite parent, int style, int width) {
		super(parent, style);

		this.width=width;
		if (this.width<500) this.width=500;
		if (this.height<200) this.height=200;
		
        parent.setSize(this.width, this.height);

        
        final GridData data = new GridData();
		data.widthHint = this.width;
		data.heightHint = this.height;
		this.setLayoutData(data);

        // Create a paint handler for the canvas
        this.addPaintListener(new PaintListener() {
          public void paintControl(PaintEvent e) {
            // Do some drawing
            
        	//Rectangle rect = ((Canvas) e.widget).getBounds();
            e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
            e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
            e.gc.fillRectangle(0,0,e.width,e.height);

            drawSawWave(e.gc, "aa", 3, 25, 5);
          }
        });
	}
	
	
	public void drawSawWave(GC gc, String name, int wavecount, int waveheight, int offsetfromtop) {
		int w= (this.width-textlegendsize-5)/wavecount;
		int x=0, y=0;
		
		gc.drawString("0xFFFF", 2, offsetfromtop);
		gc.drawString("TCNT1=0xF239", 2, offsetfromtop+waveheight-4);
		
		x=x+textlegendsize;
		for (int i=0;i<wavecount;i++) {
			gc.drawLine(x, offsetfromtop+waveheight, x+w, offsetfromtop);
			gc.drawLine(x+w, offsetfromtop, x+w, offsetfromtop+waveheight);
			x=x+w;
		}
		
		// draw TIMER OVF
		y=offsetfromtop+waveheight+20;
		x=2;
		gc.drawString("TIMER_OVF", x,y-4);
		x=textlegendsize;
		y=y+4;
		gc.drawLine(x, y, x+this.width-textlegendsize-5,y);
	}
}
