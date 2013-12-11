package com.upna.proyecto.android;

import android.os.Bundle;
import android.app.Activity;
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
import android.view.View.OnClickListener;
import com.upna.proyecto.android.DbAdapterGPS;

public class EditarEntradasGPS extends Activity {
	
	double latitud=2,longitud=2;
	String busqueda;
	EditText nombre,notificacion;
	CheckBox wifi,tresge;
	Spinner spinnerModos;
	Button aceptar,volver;
	TextView lugar;
	
	//Variables para la BBDD
	String nombreDB="",notificacionDB="",modoDB="";
	int wifiDB=0,tresgeDB=0,rowId;
	double latitudDB=2,longitudDB=2;
	DbAdapterGPS dbGPS = new DbAdapterGPS(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nueva_entrada_gps);
		
		nombre = (EditText)findViewById(R.id.nombreGPS);
		lugar = (TextView)findViewById(R.id.mostrarLugar);
		wifi  = (CheckBox)findViewById(R.id.checkBoxWifi);
		tresge = (CheckBox)findViewById(R.id.checkBox3g);
		spinnerModos = (Spinner)findViewById(R.id.ModosHorario);
		notificacion = (EditText)findViewById(R.id.notificacionGPS);
		
		aceptar = (Button)findViewById(R.id.aceptarGPS);
		volver = (Button)findViewById(R.id.volverBtn);
		
		Bundle bundle = getIntent().getExtras();
		rowId = bundle.getInt("ID");
		latitud = bundle.getDouble("Latitud");
		longitud = bundle.getDouble("Longitud");
		
		dbGPS.open();
		Cursor cGPS = dbGPS.getEntrada(rowId);
		dbGPS.close();
		
		Log.i("Editar Entradas GPS","ID: "+rowId);
		
		//Inicializar los campos del formulario.
		nombre.setText(cGPS.getString(1));
		
		String mostrar ="Dirección: " + cGPS.getInt(1) + "\nLat: " + latitud + " | Long: " + longitud;
		lugar.setText(mostrar);

		tresgeDB = cGPS.getInt(2);
		wifiDB = cGPS.getInt(3);
		if (tresgeDB==1) {
			tresge.setChecked(true);
		}
		
		if (wifiDB==1) {
			wifi.setChecked(true);
		}
		
		
		notificacion.setText(cGPS.getString(4));
		
		addItemsOnSpinner();
		addListenerOnAceptar();
		addListenerOnVolver();
		
	}

	private void addListenerOnAceptar() {
		final Context context = this;
		
		aceptar.setOnClickListener(new OnClickListener() {
			 
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
				latitudDB = latitud;
				longitudDB = longitud;
				modoDB = spinnerModos.getSelectedItem().toString();
				
				
				//Almacenar en la BBDD.
				dbGPS.open();
				boolean id = dbGPS.actualizarEntrada(rowId,nombreDB,tresgeDB,wifiDB,notificacionDB,latitudDB,longitudDB,modoDB);
				dbGPS.close();
				
				Intent intent = new Intent(context, ConfigGPS.class);
				startActivity(intent);  
				
			    
			}

		});
	
	}

	private void addListenerOnVolver() {
		Intent setIntent = new Intent(getApplicationContext(), Mapa.class);
	    setIntent.putExtra("ID", rowId);
	    startActivity(setIntent);
	}

	//Añadir elementos al menu desplegable
	public void addItemsOnSpinner(){
		spinnerModos= (Spinner) findViewById(R.id.ModosHorario);
		
		String []listaModos = {"Con Sonido","Silencioso","Vibración"}; 
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listaModos);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerModos.setAdapter(adapter);
	}
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nueva_entrada, menu);
		return true;
	}
	
	
	@Override
	public void onBackPressed() {
		Intent setIntent = new Intent(getApplicationContext(), Mapa.class);
	    setIntent.putExtra("ID", rowId);
	    startActivity(setIntent);
	}
	
	
	
	
}



