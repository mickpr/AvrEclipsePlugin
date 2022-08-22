package de.innot.avreclipse.devexp;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Canvas;

public class MyProgressBar extends Composite {

	private int percent;
	private Canvas canvas;
	public MyProgressBar(Composite parent, int style) {
		super(parent, SWT.BORDER);
		
		canvas = new Canvas(this, SWT.NONE);
		canvas.setBounds(0, 0, 50, 19);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				PaintPackage(e.gc);	
			}
		});
	}
	
	protected void PaintPackage(GC gc) {
		
		//oblicz pozycje procentow¹
		int width=canvas.getSize().x;
		int height=canvas.getSize().y; 
		
		float used = (this.percent*(width-2))/100;
		if (percent<=70)
			gc.setBackground(SWTResourceManager.getColor(220,255,220));
		if (percent>70) 
			gc.setBackground(SWTResourceManager.getColor(255,250,180)); 
		if (percent>90) 
			gc.setBackground(SWTResourceManager.getColor(250,220,220));
		
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.fillRectangle(1, 1, Math.round(used), height-2);

		
		gc.setFont(SWTResourceManager.getFont("Microsoft Sans Serif", 8, SWT.NORMAL));
		gc.setBackground(SWTResourceManager.getColor(255,255,255));
		gc.drawText(String.valueOf(percent) + "%", (width/2)-8, 3);


		//gc.fillRectangle(used+1, 1, free-2, height-2);
	}

	public void setPercent(int percent) {
		if (percent>100) this.percent =100;
		if (percent<0) this.percent =0;
		this.percent=percent;
		canvas.redraw();
	}
	public int getPercent() {
		return this.percent;
	}
}
