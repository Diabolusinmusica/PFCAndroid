package com.upna.proyecto.android;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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


public class MyBroadcastReceiverGPS extends BroadcastReceiver {
	
	private WifiManager wifiManager;	
	private AudioManager audioManager;
	
	@Override
	public void onReceive(Context context, Intent intent){
		
		Bundle bundle = intent.getExtras();
		int rowId = bundle.getInt("ID");
		
		Log.i("BC ReveiverGPS","Ha llegado a iniciar el BCReceiver");
		
		DbAdapterGPS db = new DbAdapterGPS(context);
		
		db.open();
		Cursor cursor = db.getEntrada(rowId);
		db.close();
		
		if (cursor.equals(null)){
			Log.e("BroadcastReceiver","DATA NOT FOUND");
		} else {
			
			String msge = "BROADCASTRECEIVER ON, ESTOY VIVOOO!!";
			String tag = "BCReceiverGPS";
			Log.i(tag,msge);
			
			
					String nombre = cursor.getString(1);
					String notificacion = cursor.getString(4);
					crearNotificacion(context, nombre, notificacion);
					
					Log.i("BCReveiverGPS","3G: " + cursor.getInt(2) + "Wifi: " + cursor.getInt(3));
					
					//Activar/Desactivar Wifi.
					wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						if(cursor.getInt(3)==1){
							wifiManager.setWifiEnabled(true);
						}else{
							wifiManager.setWifiEnabled(false);
						}
						
					//Configurar el sonido.
					audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
					String modo = cursor.getString(7);
					
					if (modo.equals("Con Sonido")){
						Log.i("BCReveiverGPS","Con sonido");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); 
					}
					
					if (modo.equals("Silencioso")){
						Log.i("BCReveiverGPS","Silencioso");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); 
					}
					
					if (modo.equals("Vibración")){
						Log.i("BCReveiverGPS","Vibración");
						audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); 
					}
					
					//Configurar la conexión de datos.
					if (cursor.getInt(2)==1){
						setMobileDataEnabled(context,true);
					}else{
						setMobileDataEnabled(context,false);
					}
							

					
				
			
		}
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
