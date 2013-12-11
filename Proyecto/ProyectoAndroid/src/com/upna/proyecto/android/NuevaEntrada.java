package com.upna.proyecto.android;
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
import com.upna.proyecto.android.MyBroadcastReceiver;

public class NuevaEntrada extends Activity {

	Button cambiarHora,aceptarHorario;
	EditText nombre,notificacion;
	private CheckBox tresge,wifi,bluetooth;
	private ToggleButton lunes,martes,miercoles,jueves,viernes,sabado,domingo;
	private Spinner spinnerModos;
	int modoHorario,modoSeleccionado,mHora,mMinuto;
	TextView mostrarHora;
	static final int TIME_DIALOG_ID=0;
	String horaStr;
	
	//Variables a almacenar en BBDD.
	DbAdapter db = new DbAdapter(this);
	String nombreDB, notificacionDB,modoDB,entrada;
	int horaDB,minutoDB,horaInd;
	int tresgeDB=0,wifiDB=0,bluetoothDB=0;
	
	//Cada posicion representa un día de la semana.
	int[] diasemanaArray = {0,0,0,0,0,0,0};
	String diasemanaDB;
	
	//Variables para el AlarmManager
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nueva_entrada);
		
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
		
		
		cambiarHora.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		final Calendar c = Calendar.getInstance();
		mHora = c.get(Calendar.HOUR_OF_DAY);
		mMinuto = c.get(Calendar.MINUTE);
	
		updateDisplay();
		
		addItemsOnSpinner();
		
		addListenerOnAceptar();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nueva_entrada, menu);
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
				long id = db.crearEntrada(horaInd,nombreDB,tresgeDB,wifiDB,bluetoothDB,notificacionDB,horaDB,minutoDB,modoDB,diasemanaDB);
				db.close();
				
				Intent intent = new Intent(context, Main.class);
				startActivity(intent);  
				
			    
			}

		});
	
	}
	
	@Override
	public void onBackPressed() {
	   Intent setIntent = new Intent(getApplicationContext(), ConfigHorario.class);
	   startActivity(setIntent);
	}
	
}



