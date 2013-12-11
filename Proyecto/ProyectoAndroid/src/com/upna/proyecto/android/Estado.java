package com.upna.proyecto.android;


public class Estado{
	/*La clase estado almacena el estado del switch que elige entre
	 * Horario y gps.*/
	
	private static Estado instance;
	
	private boolean status;
	
	private Estado(){};
	
	public void setEstado(boolean s){
		this.status = s;
	}
	
	public boolean getEstado(){
		return status;
	}
	
	public static synchronized Estado getInstance(){
	     if(instance==null){
	       instance=new Estado();
	     }
	     return instance;
	   }
	
}
