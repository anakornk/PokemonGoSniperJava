import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.auth.PtcCredentialProvider;


import okhttp3.OkHttpClient;

public class PokemonSniper{
	
	private static Properties prop= new Properties();;
	
	private static String user;
	private static String pass;
	private static String googleorptc;

	private static double latitude;
	private static double longtitude;

	
	private static double catchLatitude;
	private static double catchLongtitude;
	
	private static Scanner sc = new Scanner(System.in);
	
	private static PokemonGo go;
	
	public static void catchpokemon(){
		
		System.out.println("------------------------------------------------------------------");
		System.out.println("Where to catch pokemon? Enter coordinate (e.g. 123.456,-151.123) ");
		String tempStr = sc.nextLine();
		int tempIndex = tempStr.indexOf(',');
        if(tempIndex == -1){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        	return;
        }
        try{
        	catchLatitude = Double.parseDouble(tempStr.substring(0, tempIndex-1));	        
	        catchLongtitude = Double.parseDouble(tempStr.substring(tempIndex+1));
	        
	        go.setLocation(catchLatitude, catchLongtitude,0);
	        System.out.println("Warped to: " +  go.getLatitude() + ","+ go.getLongitude());
	        
	        //get pokemon info
	        try{
	        	List<CatchablePokemon> catchablePokemon = go.getMap().getCatchablePokemon();
	        	System.out.println("Number of Pokemons in area:" + catchablePokemon.size());
	        	
	        	int i=0;
	        	for(CatchablePokemon cp : catchablePokemon){
					System.out.println(i + ") " + cp.getPokemonId() +" found at "+ cp.getLatitude() +" "+cp.getLongitude());
					i++;
				}
	        	System.out.println("Please enter 0-"+(catchablePokemon.size()-1)+":");
	        	int index = Integer.parseInt(sc.nextLine());
	        	CatchablePokemon cp = catchablePokemon.get(index);
	        	EncounterResult encResult = cp.encounterPokemon();
				if (encResult.wasSuccessful()) {
					System.out.println("Encountered:" + cp.getPokemonId());
					go.setLocation(latitude,longtitude, 0);
					System.out.println("Warped back to: " +  go.getLatitude() + ","+ go.getLongitude());
					CatchResult result = cp.catchPokemon();
					System.out.println("Catch Status: " + result.getStatus());
					//System.out.println("Caught Pokemon information\n" + go.getInventories().getPokebank().getPokemonById(result.getCapturedPokemonId()).getNickname() );
	
				}else{
					System.out.println("Fail to encounter " + cp.getPokemonId());
				}
	        }
	        catch(Exception e){
	        	System.out.println(e);
	        }
	        
	       
			
			//System.out.println("--------Wait for five seconds---------");
        }
        catch(NumberFormatException e){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        	return;
        }
	}
	
	public static void main(String[] arg){
		System.out.println("Locating config.properties");		
		try{
			
			prop.load(new FileInputStream("config.properties"));
			System.out.println("Loaded config.properties");			
		}
		catch(Exception e){
			System.out.println("Error: Cannot find config.properties\nExiting");
			return;
		}
		
		user = prop.getProperty("USERNAME");
		pass = prop.getProperty("PASSWORD");
		googleorptc = prop.getProperty("GOOGLEORPTC");
		latitude = Double.parseDouble(prop.getProperty("LATITUDE"));
		longtitude = Double.parseDouble(prop.getProperty("LONGTITUDE"));
		
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	sc.close();
            	System.out.println("------------Exiting:please wait for 5 minutes---------------");
            	try{
            		sleep(5000);
            	}
            	catch(InterruptedException e){
            		
            	}
            	
            	go.setLocation(latitude, longtitude,0);
            	System.out.println("Warped back to:" +  go.getLatitude() + ","+ go.getLongitude());           	
            	System.out.println("Exited");
            }
        });
		
		
		OkHttpClient http = new OkHttpClient();
		new Thread(){
		public void run(){
			//Login
			try {
				if(googleorptc=="GOOGLE"){
					System.out.println("Google login not supported");
					return;
				}else{
					go = new PokemonGo(new PtcCredentialProvider(http,user,pass), http);
				}
				System.out.println("Hello "+go.getPlayerProfile().getPlayerData().getUsername() + ", Your level: "+ go.getPlayerProfile().getStats().getLevel());				 
				// Set to current location
				go.setLocation(latitude, longtitude,0);
				System.out.println("Your current location: " +  go.getLatitude() + ","+ go.getLongitude());
				System.out.println("----------------Wait for five seconds----------------");
	        	sleep(5000);
			}
			catch(Exception e){
				System.out.println("Error"+e);
				return;
			}
			
			
			//Main Loop
			while(true){
				
				System.out.println("Enter command: (type help for more info)");
				String cmd = sc.nextLine();
				if(cmd.equals("catch")){
					catchpokemon();
				
				}else if(cmd.equals("help")){
					System.out.println("List of commands:");
					System.out.println("catch - catch pokemon according to the given latitude and longtitude");
					System.out.println("exit - exit the program");
				}else if(cmd.equals("exit")){
					break;
				}
				
				
		       
		        
			}
			
		}
			
		}.start();
	
        
        
        
        

		
	}



}