package com.upna.proyecto.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class ConfigGPS extends Activity {
	
	Button nuevaEntrada, editarEntradas;
	TextView entradas;
	String result="";
	
	int rowId;
	int fRowId;
	int ArrayRowId[] = new int[200];
	
	
	private ListView lista;
	private ArrayAdapter<String> listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_horario);
		
		entradas = (TextView)findViewById(R.id.entradas);
		
		
		try {
			String destPath = "/data/data" + getPackageName() + "/databases/GPSDB";
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
		
		
		//Encontrar el ListView
		lista = (ListView) findViewById(R.id.listaEntradas);
		
		listAdapter = new ArrayAdapter<String>(this, R.layout.fila_entradas);
		
		final DbAdapterGPS db = new DbAdapterGPS(this);
		
		db.open();
		final Cursor c = db.getTodasEntradas();
		int i = 0;
		if (c.moveToFirst()){
			do {
				result = "Lugar: " + c.getString(1) + "\n";
				listAdapter.add(result);
				rowId = c.getInt(0); 
				ArrayRowId[i]=rowId;
				i++;
				
			} while (c.moveToNext());
		}
		c.close();
		db.close();
		
		lista.setAdapter( listAdapter );
	
		lista.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view,
	              int position, long id) {

	              // Launching new Activity on selecting single List Item
	              Intent intent = new Intent(getApplicationContext(), MapaEdit.class);
	              // sending data to new activity
	              // Obtener el indice del vector de IDs y pasar la ID del elemento escogido.
	              fRowId = ArrayRowId[position];
	           
	              intent.putExtra("ID", fRowId );
	              
	              startActivity(intent);
	             
	          }
	        });
		
		lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                    int index, long arg3) {
            	
            	 final int posicion = index;
               
                 String str=lista.getItemAtPosition(index).toString();
                 
                 LayoutInflater layoutInflater 
                 = (LayoutInflater)getBaseContext()
                  .getSystemService(LAYOUT_INFLATER_SERVICE);  
                View popupView = layoutInflater.inflate(R.layout.popup, null);  
                         final PopupWindow popupWindow = new PopupWindow(
                           popupView, 
                           LayoutParams.WRAP_CONTENT,  
                                 LayoutParams.WRAP_CONTENT);
                         
                         //Comportamiento del boton cancelar.
                         Button btnCancelar = (Button)popupView.findViewById(R.id.cancelar);
                         btnCancelar.setOnClickListener(new Button.OnClickListener(){

				                 @Override
				                 public void onClick(View v) {
				                	 popupWindow.dismiss();
				                 }
				         });
                           
                         
                    
                       //Comportamiento del boton aceptar.
                         Button btnAceptar = (Button)popupView.findViewById(R.id.aceptar);
                         btnAceptar.setOnClickListener(new Button.OnClickListener(){

				                 @Override
				                 public void onClick(View v) {
				                	 
				                	 // Launching new Activity on selecting single List Item
				    	             Intent intent = new Intent(getApplicationContext(), ConfigGPS.class);
				    	             // sending data to new activity
				    	             // Obtener el indice del vector de IDs y pasar la ID del elemento escogido.
				    	             fRowId = ArrayRowId[posicion];
				    	             db.open();
				    	             db.borrarEntrada(fRowId);
				    	             db.close();
				    	              
				    	             startActivity(intent);
				                	 
				                 }
				         });
                 
                       popupWindow. showAtLocation (v, 0, 20, -30);
                 
                 return true;
            }
}); 
		
		addOnClickListenerNuevaEntrada();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config_horario, menu);
		return true;
	}

	  public void addOnClickListenerNuevaEntrada() {
			 
			final Context context = this;
			nuevaEntrada = (Button) findViewById(R.id.nuevaEntrada);
			nuevaEntrada.setOnClickListener(new OnClickListener() {
	 
				@Override
				public void onClick(View arg0) {
				    Intent intent = new Intent(context,Mapa.class);
				    //Introducimos por defecto las coordenadas de Pamplona
				    intent.putExtra("ID", -1);
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
		

	//Añade un 0 a la hora o el minuto si son menores que 10
	private static String pad(int c){
		if (c >= 10){
			return String.valueOf(c);
		}else{
			return "0" + String.valueOf(c);
		}
	}
	
	@Override
	public void onBackPressed() {
	   Intent setIntent = new Intent(getApplicationContext(), Main.class);
	   startActivity(setIntent);
	}
	
}
