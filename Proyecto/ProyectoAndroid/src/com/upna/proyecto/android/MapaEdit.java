package com.upna.proyecto.android;


import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Geocoder;
import android.location.Address;

 
public class MapaEdit extends android.support.v4.app.FragmentActivity {
	
	DbAdapterGPS dbGPS = new DbAdapterGPS(this);
	Button buscar,aceptar; 
	EditText direccion;
	double latitudFinal=0,longitudFinal=0,latitudInic,longitudInic;
	LatLng seleccionado;
	String busqueda="";
	int rowId;
	
	GoogleMap gMap;
	LatLng posicionInicial;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa_edit); 
        
        aceptar = (Button) findViewById(R.id.aceptar);
		buscar = (Button) findViewById(R.id.buscar);
        
        Bundle bundle = getIntent().getExtras();
        rowId = bundle.getInt("ID");
        
        dbGPS.open();
        Cursor cGPS = dbGPS.getEntrada(rowId);
        dbGPS.close();
        
        direccion = (EditText)findViewById(R.id.direccion);
        posicionInicial = new LatLng(cGPS.getDouble(5),cGPS.getDouble(6));
        
        
        //Obtenemos un objeto GoogleMap a partir del SupportMapFragment.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        gMap = mapFragment.getMap();
        
        //Centrar camara en pamplona con un zoom de 12
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionInicial, 12));
        
        addListenerOnBuscar();
        addListenerOnAceptar();

       
    }

	private void addListenerOnAceptar() {
		final Context context = this;
		
		aceptar.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {

				    Intent intentNueva = new Intent(context,EditarEntradasGPS.class);
				    intentNueva.putExtra("Latitud", latitudFinal);
				    intentNueva.putExtra("Longitud", longitudFinal);
				    intentNueva.putExtra("ID", rowId);
	                startActivity(intentNueva);   

			}
		});
	}

	private void addListenerOnBuscar() {
		final Context context = this;
		buscar.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
			    //Traducir la dirección buscada a coordenadas
				
			try {
				Geocoder geoCoder = new Geocoder(context);
				busqueda = direccion.getText().toString();
				List<Address> addresses = geoCoder.getFromLocationName(busqueda,5);
				
				if (addresses.size() > 0){
					
					latitudInic = (double) (addresses.get(0).getLatitude());
					longitudInic = (double) (addresses.get(0).getLongitude());
					LatLng dirBusqueda = new LatLng(latitudInic,longitudInic);
					Marker marcador = gMap.addMarker(new MarkerOptions().position(dirBusqueda));
					gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
						 @Override 
						 public boolean onMarkerClick(Marker marker) {
							 	LatLng seleccionado = new LatLng(latitudInic,longitudInic);
						        String mostrar = "Asignada la posición: " + seleccionado.toString();
						        Toast.makeText(context, mostrar, Toast.LENGTH_SHORT).show();
						        return true;
						    }
					
					});
					
					latitudFinal = latitudInic;
					longitudFinal = longitudInic;
					LatLng camara = new LatLng((double) (addresses.get(0).getLatitude()),
							(double) (addresses.get(0).getLongitude()));
					gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(camara, 12));
				}else{
                        Log.i("MAPA","NO SE RECONOCE LA DIRECCION");
                }
			} catch (IOException e) {
				e.printStackTrace();
			}	
				
			}
		});
	}
}