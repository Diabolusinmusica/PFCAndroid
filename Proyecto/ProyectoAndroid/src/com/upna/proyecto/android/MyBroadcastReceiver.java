package com.upna.proyecto.android;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.upna.proyecto.android.DbAdapter;

public class MyBroadcastReceiver extends BroadcastReceiver {
	
	private WifiManager wifiManager;	
	private AudioManager audioManager;
	
	@Override
	public void onReceive(Context context, Intent intent){
		
		Bundle bundle = intent.getExtras();
		int rowId = bundle.getInt("ID");
		
		Log.i("BC Reveiver","Ha llegado a iniciar el BCReceiver");
		
		DbAdapter db = new DbAdapter(context);
		
		db.open();
		Cursor cursor = db.getEntrada(rowId);
		db.close();
		
		if (cursor.equals(null)){
			Log.e("BroadcastReceiver","DATA NOT FOUND");
		} else {
			
			String msge = "BROADCASTRECEIVER ON, ESTOY VIVOOO!!";
			String tag = "BCReceiver";
			Log.i(tag,msge);
			
			Calendar cal = Calendar.getInstance();
			int dia_semana_sistema = cal.get(Calendar.DAY_OF_WEEK);
			
				String semana_entrada = cursor.getString(11);
				boolean eshoy = comprobarDia(semana_entrada,dia_semana_sistema);
				if (!eshoy){
					Log.i("BCReceiver","No Es Hoy");
				}
				// Comprobamos si el dia la semana de hoy está marcado en la entrada.
				// Aquí se programa todo lo que se hace a la hora indicada.
				if (eshoy) {
					Log.i("BCReceiver","Es Hoy");
					
					String nombre = cursor.getString(2);
					String notificacion = cursor.getString(6);
					crearNotificacion(context, nombre, notificacion);
					
					Log.i("BCReveiver","3G: " + cursor.getInt(3) + "Wifi: " + cursor.getInt(4) + "Bluetooth: " + cursor.getInt(5));
					
					//Activar/Desactivar Wifi.
					wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						if(cursor.getInt(4)==1){
							wifiManager.setWifiEnabled(true);
						}else{
							wifiManager.setWifiEnabled(false);
						}
						
					//Configurar el sonido.
					audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
					String modo = cursor.getString(9);
					
					if (modo.equals("Con Sonido")){
						Log.i("BCReveiver","Con sonido");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); 
					}
					
					if (modo.equals("Silencioso")){
						Log.i("BCReveiver","Silencioso");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); 
					}
					
					if (modo.equals("Vibración")){
						Log.i("BCReveiver","Vibración");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); 
					}
					
					//Configurar la conexión de datos.
					if (cursor.getInt(3)==1){
						setMobileDataEnabled(context,true);
					}else{
						setMobileDataEnabled(context,false);
					}
							

					
				}
			
		}
	}

	//Comprueba el día de la semana de la entrada.
	private boolean comprobarDia(String semana, int dia_semana_sistema) {
		boolean eshoy = false;
		
		int[] diasemanaArray = new int[semana.length()];
		for(int i = 0 ; i < semana.length(); i++){
			diasemanaArray[i] = Character.digit(semana.charAt(i), 10);
		}
		
		//Lunes
		if ((diasemanaArray[0]==1)&(dia_semana_sistema==2)){
			eshoy = true;
		}
		//Martes
		if ((diasemanaArray[1]==1)&(dia_semana_sistema==3)){
			eshoy = true;
		}
		//Miércoles
		if ((diasemanaArray[2]==1)&(dia_semana_sistema==4)){
			eshoy = true;
		}
		//Jueves
		if ((diasemanaArray[3]==1)&(dia_semana_sistema==5)){
			eshoy = true;
		}
		//Viernes
		if ((diasemanaArray[4]==1)&(dia_semana_sistema==6)){
			eshoy = true;
		}
		//Sábado
		if ((diasemanaArray[5]==1)&(dia_semana_sistema==7)){
			eshoy = true;
		}
		//Domingo
		if ((diasemanaArray[6]==1)&(dia_semana_sistema==1)){
			eshoy = true;
		}
		
		return eshoy;
	}
	
	//Crea y lanza la notificación.
	public void crearNotificacion(Context context, String nombre, String notificacion){
		// Prepare intent which is triggered if the
	     // notification is selected

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
	            new Intent(context, ConfigHorario.class), 0);

	     // Build notification
	     // Actions are just fake
	     NotificationCompat.Builder mBuilder =
	             new NotificationCompat.Builder(context)
	             .setSmallIcon(R.drawable.ic_launcher)
	             .setContentTitle(nombre)
	             .setContentText(notificacion);
	     mBuilder.setContentIntent(contentIntent);
	     mBuilder.setDefaults(Notification.DEFAULT_SOUND);
	     mBuilder.setAutoCancel(true);
	     NotificationManager mNotificationManager =
	         (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	     mNotificationManager.notify(1, mBuilder.build()); 
	}
	
	//Configura la conexión de Datos (Encendido/Apagado).
	private void setMobileDataEnabled(Context context, boolean enabled) {
	    try {
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    final Class conmanClass = Class.forName(conman.getClass().getName());
	    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	    iConnectivityManagerField.setAccessible(true);
	    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
	    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    setMobileDataEnabledMethod.setAccessible(true);

	    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	    }catch (Exception e){
			Log.e("BCReveiver","error turning on/off data");
		}
	}
	

}
