package com.upna.proyecto.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;
import com.upna.proyecto.android.DbAdapter;

public class EditarEntradas extends Activity {

	Button cambiarHora,aceptarHorario;
	EditText nombre,notificacion;
	private CheckBox tresge,wifi;
	private ToggleButton lunes,martes,miercoles,jueves,viernes,sabado,domingo;
	private Spinner spinnerModos;
	int modoHorario,modoSeleccionado,mHora,mMinuto;
	TextView mostrarHora;
	static final int TIME_DIALOG_ID=0;
	String horaStr;
	
	//Variables a almacenar.
	DbAdapter db = new DbAdapter(this);
	String nombreDB, notificacionDB,modoDB,entrada;
	int horaDB,minutoDB,horaInd;
	int tresgeDB=0,wifiDB=0,bluetoothDB=0;
	
	int[] diasemanaArray = {0,0,0,0,0,0,0};
	String diasemanaDB;
	
	int rowId;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editar_entradas);
		
		cambiarHora = (Button)findViewById(R.id.cambiarHora);
		mostrarHora = (TextView)findViewById(R.id.mostrarHora);
		spinnerModos = (Spinner)findViewById(R.id.ModosHorario);
		nombre = (EditText)findViewById(R.id.NombreHorario);
		notificacion = (EditText)findViewById(R.id.NotificacionHorario);
		tresge = (CheckBox)findViewById(R.id.checkBox3g);
		wifi  = (CheckBox)findViewById(R.id.checkBoxWifi);
		aceptarHorario = (Button) findViewById(R.id.aceptarHorario);
		
		//Switches dias de la semana;
		lunes = (ToggleButton)findViewById(R.id.toggleLunes);
		martes = (ToggleButton)findViewById(R.id.toggleMartes);
		miercoles = (ToggleButton)findViewById(R.id.toggleMiercoles);
		jueves = (ToggleButton)findViewById(R.id.toggleJueves);
		viernes = (ToggleButton)findViewById(R.id.toggleViernes);
		sabado = (ToggleButton)findViewById(R.id.toggleSabado);
		domingo = (ToggleButton)findViewById(R.id.toggleDomingo);
		
		Bundle bundle = getIntent().getExtras();
		rowId = bundle.getInt("ID");
		
		try {
			String destPath = "/data/data" + getPackageName() + "/databases/HorarioDB";
			File f = new File(destPath);
			if (!f.exists()){
				CopyDB(getBaseContext().getAssets().open("MyDatabase"),
						new FileOutputStream(destPath));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		/* Sacamos la entrada adecuada de la base de datos 
		e inicializamos el formulario con los valores de la entrada */
		DbAdapter db = new DbAdapter(this);
		
		db.open();
		Cursor c = db.getEntrada(rowId);
		db.close();

		tresgeDB = c.getInt(3);
		wifiDB = c.getInt(4);
		bluetoothDB = c.getInt(5);
		
		diasemanaDB = c.getString(11);
		
		int[] diasemanaArray = new int[diasemanaDB.length()];
		for(int i = 0 ; i < diasemanaDB.length(); i++){
			diasemanaArray[i] = Character.digit(diasemanaDB.charAt(i), 10);
		}
		
		if (diasemanaArray[0]==1){
			lunes.setChecked(true);
		}
		
		if (diasemanaArray[1]==1){
			martes.setChecked(true);
		}
		
		if (diasemanaArray[2]==1){
			miercoles.setChecked(true);
		}
		
		if (diasemanaArray[3]==1){
			jueves.setChecked(true);
		}
		
		if (diasemanaArray[4]==1){
			viernes.setChecked(true);
		}
		
		if (diasemanaArray[5]==1){
			sabado.setChecked(true);
		}
		
		if (diasemanaArray[6]==1){
			domingo.setChecked(true);
		}
		
		nombre.setText(c.getString(2));
		notificacion.setText(c.getString(6));
		
		if (tresgeDB==1) {
			tresge.setChecked(true);
		}
		
		if (wifiDB==1) {
			wifi.setChecked(true);
		}
		
		
		cambiarHora.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		
		
		mHora = c.getInt(7);
		mMinuto = c.getInt(8);
	
		updateDisplay();
		
		addItemsOnSpinner();
		
		addListenerOnAceptar();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.editar_entradas, menu);
		return true;
	}
	
	
	//Actualiza el tiempo que se muestra en el textview "mostrarHora"
	private void updateDisplay(){
		mostrarHora.setText(
				new StringBuilder()
				.append(pad(mHora)).append(":")
				.append(pad(mMinuto)));
	}
		
	//Añade un 0 a la hora o el minuto si son menores que 10
	private static String pad(int c){
		if (c >= 10){
			return String.valueOf(c);
		}else{
			return "0" + String.valueOf(c);
		}
	}
	
	//Asignar la hora y minuto elegidos a las variables mHora y mMinuto
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = 
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int horadelDia, int minutodelDia){
			mHora = horadelDia;
			mMinuto = minutodelDia;
			updateDisplay();
		}
	};
	
	
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
			case TIME_DIALOG_ID:
				return new TimePickerDialog(this,
					mTimeSetListener, mHora, mMinuto, false);
		}
		return null;
	}
	
	//Añadir elementos al menu desplegable
	public void addItemsOnSpinner(){
		spinnerModos= (Spinner) findViewById(R.id.ModosHorario);
		
		String []listaModos = {"Con Sonido","Silencioso","Vibración"}; 
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listaModos);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerModos.setAdapter(adapter);
	}
		
	//Escucha al botón de aceptar
	public void addListenerOnAceptar(){
		
		final Context context = this;
				
		aceptarHorario.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View v) {
				  
				//Asignar variables
				nombreDB = nombre.getText().toString();	
				if (tresge.isChecked()){
					tresgeDB = 1;
				}
				if( wifi.isChecked()){
					wifiDB = 1;
				} 
				
				notificacionDB = notificacion.getText().toString();	
				horaDB = mHora;
				minutoDB = mMinuto;
				modoDB = spinnerModos.getSelectedItem().toString();
				horaInd = (horaDB*100) + minutoDB; 
				
				//Dias de la semana.
				if( lunes.isChecked()){
					diasemanaArray[0] = 1;
				}
				if( martes.isChecked()){
					diasemanaArray[1] = 1;
				}
				if( miercoles.isChecked()){
					diasemanaArray[2] = 1;
				}
				if( jueves.isChecked()){
					diasemanaArray[3] = 1;
				}
				if( viernes.isChecked()){
					diasemanaArray[4] = 1;
				}
				if( sabado.isChecked()){
					diasemanaArray[5] = 1;
				}
				if( domingo.isChecked()){
					diasemanaArray[6] = 1;
				}
				
				StringBuffer strBuff = new StringBuffer();
				
				strBuff.append(diasemanaArray[0]);
				for (int i = 1; i < diasemanaArray.length; i++){
					strBuff.append(diasemanaArray[i]);
				}
				
				diasemanaDB = strBuff.toString();
				
				//Almacenar en la BBDD.
				db.open();
				boolean id = db.actualizarEntrada(rowId,horaInd,nombreDB,tresgeDB,wifiDB,bluetoothDB,notificacionDB,horaDB,minutoDB,modoDB,diasemanaDB);
				db.close();
				
//				//Cambiar de actividad.
//				Intent sendIntent = new Intent(context, MyBroadcastReceiver.class);
//				sendIntent.putExtra("ID",rowId);
//				
//				PendingIntent sender = PendingIntent.getBroadcast(context, rowId, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//				
//				Calendar cal = Calendar.getInstance();
//				cal.set(Calendar.HOUR, horaDB);
//				cal.set(Calendar.MINUTE, minutoDB);
//				
//				if (horaDB > 12){
//					cal.set(Calendar.HOUR, horaDB - 12);
//					cal.set(Calendar.AM_PM, Calendar.PM);
//				}
//				
//				long diaMillisecs = 86400000;
//
//				//Alarm manager
//				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//				am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() ,diaMillisecs , sender);
				
				Intent intent = new Intent(context, ConfigHorario.class);
				startActivity(intent); 
				
			}

		});
	
	}
	
	

	public void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException {
	//---copy 1K bytes at a time---
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
		}
		inputStream.close();
		outputStream.close();
	}
	
	@Override
	public void onBackPressed() {
	   Intent setIntent = new Intent(getApplicationContext(), ConfigHorario.class);
	   startActivity(setIntent);
	}
}



