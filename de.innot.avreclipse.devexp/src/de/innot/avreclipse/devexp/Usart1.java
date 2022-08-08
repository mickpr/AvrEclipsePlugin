package de.innot.avreclipse.devexp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Group;

public class Usart1 extends Composite {

	public Usart1(Composite parent, int style) {
		super(parent, SWT.BORDER);
		
		
//		Group group = new Group(this, SWT.FILL);
		
		GridLayout gl_compos = new GridLayout(4, false);
		gl_compos.marginHeight = 10;
		gl_compos.marginWidth = 10;
		this.setLayout(gl_compos);		
		
		Button btnCheckButton = new Button(this, SWT.CHECK);
		btnCheckButton.setToolTipText("Enable receiver");
		btnCheckButton.setText("Receiver enable (RXD)");
		
		Button btnCheckButton_2 = new Button(this, SWT.CHECK);
		btnCheckButton_2.setToolTipText("Generate receive IRQ");
		btnCheckButton_2.setText("Rx Interrupt");
		
		Label lblRxBuffer = new Label(this, SWT.NONE);
		lblRxBuffer.setText("RX Buffer (Bytes):");
		
		Spinner spinner = new Spinner(this, SWT.BORDER);
		spinner.setBackground(SWTResourceManager.getColor(255, 255, 153));
		GridData gd_spinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_spinner.widthHint = 50;
		spinner.setLayoutData(gd_spinner);
		spinner.setIncrement(8);
		
		Button btnCheckButton_1 = new Button(this, SWT.CHECK);
		btnCheckButton_1.setToolTipText("Enable transmitter");
		btnCheckButton_1.setText("Transmitter enable (TXD) ");
		
		Button btnCheckButton_3 = new Button(this, SWT.CHECK);
		btnCheckButton_3.setToolTipText("Generate tramsmitt IRQ");
		btnCheckButton_3.setText("Tx Interrupt");
		
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText("TX Buffer (Bytes) :");
		
		Spinner spinner_1 = new Spinner(this, SWT.BORDER);
		spinner_1.setBackground(SWTResourceManager.getColor(255, 255, 153));
		GridData gd_spinner_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_spinner_1.widthHint = 50;
		spinner_1.setLayoutData(gd_spinner_1);
		spinner_1.setIncrement(8);
		
		Label lblNewLabel_2 = new Label(this, SWT.NONE);
		lblNewLabel_2.setText("Speed (baud) : ");
		
		Combo combo_2 = new Combo(this, SWT.NONE);
		GridData gd_combo_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_combo_2.widthHint = 50;
		gd_combo_2.minimumWidth = 50;
		combo_2.setLayoutData(gd_combo_2);
		combo_2.setBackground(SWTResourceManager.getColor(255, 255, 153));
		combo_2.add("110");
		combo_2.add("300");
		combo_2.add("600");
		combo_2.add("1200");
		combo_2.add("2400");
		combo_2.add("4800");
		combo_2.add("9600");
		combo_2.add("14400");
		combo_2.add("19200");
		combo_2.add("28800");
		combo_2.add("31250");
		combo_2.add("38400");
		combo_2.add("56000");
		combo_2.add("57600");
		combo_2.add("115200");
		combo_2.select(6);
		
		Label lblBaudRateError = new Label(this, SWT.NONE);
		lblBaudRateError.setText("Baud rate error (%) :");
		
		Label lblNewLabel_3 = new Label(this, SWT.NONE);
		GridData gd_lblNewLabel_3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_3.widthHint = 50;
		lblNewLabel_3.setLayoutData(gd_lblNewLabel_3);
		lblNewLabel_3.setText("0,2");
		
		Button btnCheckButton_4 = new Button(this, SWT.CHECK);
		btnCheckButton_4.setToolTipText("Enable double speed UART mode");
		btnCheckButton_4.setText("U2X (Double Speed)");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(this, SWT.NONE);
		lblNewLabel_1.setText("Data (bits) :");
		
		Combo cmbDataBits= new Combo(this, SWT.NONE);
		cmbDataBits.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmbDataBits.setBackground(SWTResourceManager.getColor(255, 255, 153));
		cmbDataBits.add("5");
		cmbDataBits.add("6");
		cmbDataBits.add("7");
		cmbDataBits.add("8");
		cmbDataBits.add("9");
		cmbDataBits.select(3);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblParity = new Label(this, SWT.NONE);
		lblParity.setText("Parity :");
		
		Combo comParity = new Combo(this, SWT.NONE);
		comParity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		comParity.setBackground(SWTResourceManager.getColor(255, 255, 153));
		comParity.add("N - none (disabled)");
		comParity.add("E - even parity");
		comParity.add("O - odd parity");
		comParity.select(0); // select N parity
		
		Label lblStopbits = new Label(this, SWT.NONE);
		lblStopbits.setText("Stop (bits) :");
		
		Combo cmbStopBits = new Combo(this, SWT.NONE);
		cmbStopBits.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmbStopBits.setBackground(SWTResourceManager.getColor(255, 255, 153));
		cmbStopBits.add("1");
		cmbStopBits.add("2");
		cmbStopBits.select(0);
		
		Label lblMode = new Label(this, SWT.NONE);
		lblMode.setText("Mode :");
		
		Combo cbmAsyncSyncMode = new Combo(this, SWT.NONE);
		cbmAsyncSyncMode.setBackground(SWTResourceManager.getColor(255, 255, 153));
		GridData gd_combo_21 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_combo_21.widthHint = 150;
		cbmAsyncSyncMode.setLayoutData(gd_combo_21);
		cbmAsyncSyncMode.add("Asynchronus");
		cbmAsyncSyncMode.add("Synchronus, Master, Tx on rising XCK Edge, Rx on falling (UCPOL=0)");
		cbmAsyncSyncMode.add("Synchronus, Master, Tx on falling XCK Edge, Rx on rising (UCPOL=1)");
		cbmAsyncSyncMode.add("Synchronus, Slave, Tx on rising XCK Edge, Rx on falling (UCPOL=0)");
		cbmAsyncSyncMode.add("Synchronus, Slave, Tx on falling XCK Edge, Rx on rising (UCPOL=1)");
		cbmAsyncSyncMode.select(0);
		new Label(this, SWT.NONE);
		
		Group grpCodeGenerate = new Group(this, SWT.SHADOW_OUT);
		grpCodeGenerate.setText("Code generate");
		FillLayout fl_grpCodeGenerate = new FillLayout(SWT.HORIZONTAL);
		fl_grpCodeGenerate.spacing = 5;
		fl_grpCodeGenerate.marginWidth = 5;
		fl_grpCodeGenerate.marginHeight = 5;
		grpCodeGenerate.setLayout(fl_grpCodeGenerate);
		GridData gd_grpCodeGenerate = new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1);
		gd_grpCodeGenerate.widthHint = 388;
		gd_grpCodeGenerate.heightHint = 37;
		grpCodeGenerate.setLayoutData(gd_grpCodeGenerate);
		
		Button btnRadioButton = new Button(grpCodeGenerate, SWT.RADIO);
		btnRadioButton.setSelection(true);
		btnRadioButton.setText("Simple code");
		
		Button btnRadioButton_1 = new Button(grpCodeGenerate, SWT.RADIO);
		btnRadioButton_1.setText("printf code");
		
		Button btnGenerateCode = new Button(grpCodeGenerate, SWT.NONE);
		btnGenerateCode.setText("Generate code");
	}
}
