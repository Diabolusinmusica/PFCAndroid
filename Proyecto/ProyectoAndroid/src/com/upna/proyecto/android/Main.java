package com.upna.proyecto.android;

import java.util.Calendar;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.upna.proyecto.android.DbAdapter;
import com.upna.proyecto.android.DbAdapterGPS;
import com.upna.proyecto.android.Estado;

public class Main extends Activity implements LocationListener  {
	
	Estado est = Estado.getInstance();
	boolean status;
	
	Button btnConfigHorario, btnConfigGPS,actualizar;
	ToggleButton horarioGPS;
	
	DbAdapter dbHorario = new DbAdapter(this);
	DbAdapterGPS dbGPS = new DbAdapterGPS(this);
	Calendar cal = Calendar.getInstance();
	int horaActual, horaNotif;
	
	long lat;
	long lng;

	final Context context = this;
	
	//Para el GPS:  
	private LocationManager locationManager;
	private String provider;
    int rowId=0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		btnConfigHorario = (Button) findViewById(R.id.configHorario);
 		actualizar = (Button) findViewById(R.id.actualizar);
		btnConfigGPS = (Button) findViewById(R.id.configGPS);
        
        //Elegir la funcionalidad por Horario o por Geolocalizacion.
        horarioGPS = (ToggleButton)findViewById(R.id.horarioGPS);
        
        //Estado del botón toggle por defecto: false
        status = est.getEstado();

        if (status){
        	Log.i("MAIN","ESTADO ON");
        	horarioGPS.setChecked(true);
        }else{
        	Log.i("MAIN","ESTADO OFF");
        	horarioGPS.setChecked(false);
        }
        
        
        
        
        addOnClickListenerHorario();
        addOnClickListenerGPS();
        addOnClickListenerActualizar();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
   
    public void addOnClickListenerHorario() {
		 
		final Context context = this;
		btnConfigHorario.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				
				if( horarioGPS.isChecked()){
					est.setEstado(true);
				    Intent intent = new Intent(context, ConfigHorario.class);
	                startActivity(intent);   
				}else{
					est.setEstado(false);
		        	Intent intent = new Intent(context, ConfigHorario.class);
	                startActivity(intent);   
				}
			}
		});
	}
    
    public void addOnClickListenerGPS() {
		 
		final Context context = this;
		btnConfigGPS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if( horarioGPS.isChecked()){
					est.setEstado(true);
				    Intent intent = new Intent(context, ConfigGPS.class);
	                startActivity(intent);   
				}else{
					est.setEstado(false);
		        	Intent intent = new Intent(context, ConfigGPS.class);
	                startActivity(intent);   
				} 
			}
		});
	}
    
    public void addOnClickListenerActualizar() {
		 
 		final Context context = this;
 		actualizar.setOnClickListener(new OnClickListener() {
  
 			@Override
 			public void onClick(View arg0) {
 				/*si horarioGPS.isChecked() es true, está activado el Horario,
 		        si es false, la parte del GPS*/
 		        
 				int cont=1;
 				
 				//Con status=true, está activa la opción del horario, con false, la del GPS.
 				
 		        if( status ){
 		        	//Código para el horario.
 		        	
 		        	dbHorario.open();
 		        	Cursor cHorario = dbHorario.getTodasEntradas();
 			        cHorario.moveToFirst();
 		        	dbHorario.close();
 			        
 			        do{
 			        	
 						//Ejecuta el BCReceiver y crea las alarmas para todas las entradas de la BBDD.
 						Intent sendIntent = new Intent(context, MyBroadcastReceiver.class);
 						sendIntent.putExtra("ID",cHorario.getInt(0));
 						
 						PendingIntent sender = PendingIntent.getBroadcast(context, cont, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 						
 						Calendar cal = Calendar.getInstance();
 						cal.set(Calendar.HOUR, cHorario.getInt(7));
 						cal.set(Calendar.MINUTE, cHorario.getInt(8));
 						
 						if (cHorario.getInt(7) > 12){
 							cal.set(Calendar.HOUR, cHorario.getInt(7) - 12);
 							cal.set(Calendar.AM_PM, Calendar.PM);
 						}
 						
 						long diaMillisecs = 86400000;
 			
 						//Alarm manager			
 						Log.i("Main","Justo antes del AlManager");
 						AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 						am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() ,diaMillisecs , sender);
 						Log.i("Main","Justo despues del AlManager");
 						Log.i("Main","Hora marcada: " + cHorario.getInt(7) + ":" + cHorario.getInt(8) );
 						
 						cont++;
 			        }while (cHorario.moveToNext());
 			        cHorario.close();
					Context context = getApplicationContext();
					CharSequence text = "Entradas Actualizadas / Nº de entradas: " + (cont-1);
					int duration = Toast.LENGTH_SHORT;
					
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
 			        	
 				}else{
 					//Código para cambiar parámetros en el lugar elegido por GPS.
 					dbGPS.open();
 		        	Cursor cGPS = dbGPS.getTodasEntradas();
 		        	cGPS.moveToFirst();
 			        dbGPS.close();
 			        
 			        Long Latitudes[] = new Long[200];
 			        Long Longitudes[] = new Long[200];
 			        int IDs[] = new int[200];
 			        int i=0;
 			        
 			        do{
 			    	   Latitudes[i]=cGPS.getLong(5);
 			    	   Longitudes[i]=cGPS.getLong(6);
 			    	   IDs[i]=cGPS.getInt(0);
 			    	   i++;
 			        }while(cGPS.moveToNext());
 					
 					// Establecer el location manager
 					locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 				    Criteria criteria = new Criteria();
 				    provider = locationManager.getBestProvider(criteria, false);
 				    Location location = locationManager.getLastKnownLocation(provider);
 				    boolean esaqui = false;
 				    // Initialize the location fields
 				    if (location != null) {
 				    	Log.i("MAIN","Provider " + provider + " has been selected.");
 				    	onLocationChanged(location);
 				    	esaqui = comprobarLugar(lat,lng,Latitudes,Longitudes,IDs);
 				      
 				    } else {
 				    	Log.i("MAIN","Location vacía.");
 				    }
 				    
 				    if (esaqui) {
 				    	
 				    	Calendar cal = Calendar.getInstance();
 				    	Log.i("Main","Se ejecuta el Alarm Manager del GPS a las: " + cal);
 				    	Log.i("MAIN","RowId vale: " + rowId);
 				    	
 				    	Intent sendIntent = new Intent(context, MyBroadcastReceiverGPS.class);
 						sendIntent.putExtra("ID",rowId);
 						
 						PendingIntent sender = PendingIntent.getBroadcast(context, cont, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 						
 						AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 						am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() ,0 , sender);
 				    }
 				}
 				
 			}

			
 		});
 	}
    
    private boolean comprobarLugar(long lat, long lng, Long[] Latitudes, Long[] Longitudes, int[] IDs) {
		
    	boolean esaqui = false;
    	for (int j = 0; j<IDs.length;j++){
    		if ((lat == Latitudes[j])&(lng == Longitudes[j])){
    			esaqui = true;
    			rowId = IDs[j];
    			break;
    			
    		}
			
		}
    	return esaqui;
    	
	}
    
//    /* Request updates at startup */
//    @Override
//    protected void onResume() {
//      super.onResume();
//      locationManager.requestLocationUpdates(provider, 400, 1, (LocationListener) this);
//    }

//    /* Remove the locationlistener updates when Activity is paused */
//    @Override
//    protected void onPause() {
//      super.onPause();
//      locationManager.removeUpdates((LocationListener) this);
//    }

    @Override
    public void onLocationChanged(Location location) {
      lat = (int) (location.getLatitude());
      lng = (int) (location.getLongitude());
     
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      
    }

    @Override
    public void onProviderEnabled(String provider) {
      Toast.makeText(this, "Enabled new provider " + provider,
          Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
      Toast.makeText(this, "Disabled provider " + provider,
          Toast.LENGTH_SHORT).show();
    }
    
    @Override
	public void onBackPressed() {
    	this.finish();
    	return;
	}



    
    
}
