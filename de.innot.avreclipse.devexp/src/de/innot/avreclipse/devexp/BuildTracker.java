package de.innot.avreclipse.devexp;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.BadLocationException;

public class BuildTracker implements IConsoleLineTrackerExtension {
	private IConsole m_console;
    
    public void dispose() {        
        
    }

    public void init(IConsole console) {        
        m_console = console;        
    }

	@Override
	public void lineAppended(org.eclipse.jface.text.IRegion line) {
		// TODO Auto-generated method stub
        try {
            String line1 = m_console.getDocument().get(line.getOffset(), line.getLength());
            System.out.println("MyConsoleOutput: " + line1);   
             // DO SOMETHING WITH THAT LINE
        } catch (org.eclipse.jface.text.BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	@Override 
	public void consoleClosed() {
		try {
			String allText = m_console.getDocument().get(0, m_console.getDocument().getLength());
			m_console.getDocument().set( allText + "\nxxxx");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		
	}
}


