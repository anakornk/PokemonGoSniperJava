import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import okhttp3.OkHttpClient;

class node{
	String name;
	String coord;
	String until;
	public node(String name,String coord,String until){
		this.name = name;
		this.coord = coord;
		this.until = until;
	}
	public String getName(){
		return name;
	}
	public String getCoord(){
		return coord;
	}
	public String getUntil(){
		return until;
	}

}



class nodeComparator implements Comparator<Object> {

    public int compare(Object obj1, Object obj2) {
        return ((node) obj1).getName().compareTo(((node) obj2).getName());
    }

}


public class PokemonSniper{
	
	private static Properties prop= new Properties();;
	
	private static String user;
	private static String pass;
	private static String googleorptc;
	private static String token;
	private static double latitude;
	private static double longitude;

	
	private static double catchLatitude;
	private static double catchLongitude;
	
	private static Scanner sc = new Scanner(System.in);
	
	private static PokemonGo go;
	
	private static List<node> nodeStorage = new ArrayList<node>();
	private static nodeComparator nc = new nodeComparator();

	
	public static void catchPokemonCoord(){
		
		
		System.out.println("Where to catch pokemon? Enter coordinates (e.g. 123.456,-151.123) ");
		String tempStr = sc.nextLine();
		int tempIndex = tempStr.indexOf(',');
        if(tempIndex == -1){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        	return;
        }
        
        try{
        	catchLatitude = Double.parseDouble(tempStr.substring(0, tempIndex-1));	        
	        catchLongitude = Double.parseDouble(tempStr.substring(tempIndex+1));
	        
	        go.setLocation(catchLatitude, catchLongitude,0);
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
					go.setLocation(latitude,longitude, 0);
					System.out.println("Warped back to: " +  go.getLatitude() + ","+ go.getLongitude());
					CatchResult result = cp.catchPokemon();
					System.out.println("Catch Status: " + result.getStatus());
					
					//output info
					System.out.println("Pokemon Information:\nPokemon:" + encResult.getPokemonData().getPokemonId().toString());
					System.out.println("CP:" + encResult.getPokemonData().getCp());
					System.out.println("Height:" + encResult.getPokemonData().getHeightM() +"m");
					System.out.println("Weight:" + encResult.getPokemonData().getWeightKg()+"kg");
					System.out.println("MOVE 1:" + encResult.getPokemonData().getMove1());
					System.out.println("MOVE 2:" + encResult.getPokemonData().getMove2());
					int ivAttack = encResult.getPokemonData().getIndividualAttack();
					int ivDefense = encResult.getPokemonData().getIndividualDefense();
					int ivStamina = encResult.getPokemonData().getIndividualStamina();		
					System.out.println("IV Perfection:" +(ivAttack + ivDefense + ivStamina)*100/45.0 + "%");
					
	
				}else{
					System.out.println("Fail to encounter " + cp.getPokemonId());
				}
	        }
	        catch(Exception e){
	        	System.out.println(e);
	        }

        }
        catch(NumberFormatException e){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        	return;
        }

       
	}
	
	public static void catchPokemonNode(int index){
		if(index <0 || index > nodeStorage.size()-1){
			System.out.println("Error - IndexOutofBounds");
			return;
		}
			
		String name  = nodeStorage.get(index).getName();
		String tempStr = nodeStorage.get(index).getCoord();
		int tempIndex = tempStr.indexOf(',');
        if(tempIndex == -1){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        	return;
        }
        try{
        	catchLatitude = Double.parseDouble(tempStr.substring(0, tempIndex-1));	        
	        catchLongitude = Double.parseDouble(tempStr.substring(tempIndex+1));
	        
	        go.setLocation(catchLatitude, catchLongitude,0);
	        System.out.println("Warped to: " +  go.getLatitude() + ","+ go.getLongitude());
	        
	        //get pokemon info
	        try{
	        	List<CatchablePokemon> catchablePokemon = go.getMap().getCatchablePokemon();
	
	        	
	        	boolean found = false;
	        	for(CatchablePokemon cp : catchablePokemon){
					if(cp.getPokemonId().toString().equals(name)){
						EncounterResult encResult = cp.encounterPokemon();
						if (encResult.wasSuccessful()) {
							System.out.println("Encountered:" + cp.getPokemonId());
							go.setLocation(latitude,longitude, 0);
							System.out.println("Warped back to: " +  go.getLatitude() + ","+ go.getLongitude());
							CatchResult result = cp.catchPokemon();
							System.out.println("Catch Status: " + result.getStatus());
							//output info
							System.out.println("Pokemon Information:\nPokemon:" + encResult.getPokemonData().getPokemonId().toString());
							System.out.println("CP:" + encResult.getPokemonData().getCp());
							System.out.println("Height:" + encResult.getPokemonData().getHeightM() +"m");
							System.out.println("Weight:" + encResult.getPokemonData().getWeightKg()+"kg");
							System.out.println("MOVE 1:" + encResult.getPokemonData().getMove1());
							System.out.println("MOVE 2:" + encResult.getPokemonData().getMove2());
							int ivAttack = encResult.getPokemonData().getIndividualAttack();
							int ivDefense = encResult.getPokemonData().getIndividualDefense();
							int ivStamina = encResult.getPokemonData().getIndividualStamina();		
							System.out.println("IV Perfection:" +(ivAttack + ivDefense + ivStamina)*100/45.0 + "%");
							
						}else{
							System.out.println("Fail to encounter " + cp.getPokemonId());
						}
						found = true;
						break;
					}
				}
	        	if(!found){
	        		System.out.println(name + " not found");
	        		go.setLocation(latitude,longitude, 0);
					System.out.println("Warped back to: " +  go.getLatitude() + ","+ go.getLongitude());
	        	}
	        	
	        	
	        	
	        }
	        catch(Exception e){
	        	System.out.println(e);
	        }

        }
        catch(Exception e){
        	System.out.println("Format error: Correct example - 123.456,-151.123");
        }

       
	}
	
	public static String getHTMLSource(String urlStr){
		String resultStr = "";
		try{
			System.setProperty("http.agent", "Chrome");
		 	URL url = new URL(urlStr);
		 	

	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(url.openStream()));

	        String inputLine;
	        while ((inputLine = in.readLine()) != null)
	            resultStr+=inputLine;
	        in.close();

		}catch(Exception e){
			System.out.println("Error while getting html");
   			resultStr = "";
   		}
   		return resultStr;
   		
	}
	
	public static void getPokeSnipers(){

    	try{
    		
             String str="["+ getHTMLSource("http://www.pokesnipers.com/api/v1/pokemon.json")+ "]";
             JSONArray json = new JSONArray(str);
             JSONArray json2 = ((JSONObject) json.get(0)).getJSONArray("results");
             int len = json2.length();
             for(int i=0;i<len;i++){
             	 JSONObject obj = (JSONObject)json2.get(i);
             	 String name = obj.get("name").toString().toUpperCase().replaceAll("('| )", "");
             	 String coords = obj.get("coords").toString();
             	 String until = obj.get("until").toString();
             	 System.out.println(i + ") "+ name +" Coords:" + coords + " Until:" + until );
             	 nodeStorage.add(new node(name,coords,until));
             }
    	}
    	catch(Exception e){
    		System.out.println(e);
    	}
   
	}
	
	public static void findPokeSnipers(String pokename){
		try{
    		String str = "["+ getHTMLSource("http://www.pokesnipers.com/api/v1/pokemon.json")+ "]";
             JSONArray json = new JSONArray(str);
             JSONArray json2 = ((JSONObject) json.get(0)).getJSONArray("results");
             //System.out.println(json2);
             int len = json2.length();      
             
             for(int i=0,j=0;i<len;i++){
            	 
             	 JSONObject obj = (JSONObject)json2.get(i);
             	 String name = obj.get("name").toString().toUpperCase().replaceAll("('| )", "");
             	 String coords = obj.get("coords").toString();
             	 String until = obj.get("until").toString();
             	 if(name.equals(pokename)){
             		System.out.println(j + ") "+ name +" Coords:" +coords + " Until:" + until);
             		nodeStorage.add(new node(name,coords,until));
             		j++;
             	 }
             	 
             }
    	}
    	catch(Exception e){
    		System.out.println(e);
    	}
   
	}
	
	public static void main(String[] arg){
		System.out.println("Locating config.properties");		
		try{
			
			prop.load(new FileInputStream("config.properties"));
			user = prop.getProperty("USERNAME");
			pass = prop.getProperty("PASSWORD");
			googleorptc = prop.getProperty("GOOGLEORPTC");
			token = prop.getProperty("TOKEN");
			latitude = Double.parseDouble(prop.getProperty("LATITUDE"));
			longitude = Double.parseDouble(prop.getProperty("LONGITUDE"));
			System.out.println("Loaded config.properties");			
		}
		catch(Exception e){
			System.out.println("Error: Cannot find config.properties\nExiting");
			return;
		}
		
		
		
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	sc.close();
            	System.out.println("-----------------Exiting:please wait for 5 minutes----------------");
            	try{
            		sleep(5000);
            	}
            	catch(InterruptedException e){
            		
            	}
            	
            	go.setLocation(latitude, longitude,0);
            	System.out.println("Warped back to:" +  go.getLatitude() + ","+ go.getLongitude());           	
            	System.out.println("Goodbye!!");
            }
        });
		
		
		OkHttpClient http = new OkHttpClient();
		new Thread(){
		public void run(){
			//Login
			try {
				if(googleorptc=="GOOGLE"){
					if(token==null){
						GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(http);

						// in this url, you will get a code for the google account that is logged
						System.out.println("Please go to: " + GoogleUserCredentialProvider.LOGIN_URL);
						System.out.println("Enter authorization code:");

						// Ask the user to enter the token in the standard input
						String authCode = sc.nextLine();

						// we should be able to login with this token
						provider.login(authCode);
						 go = new PokemonGo(provider, http);
						 System.out.println("Please edit the value of the 'TOKEN' field in config.properties");
						 System.out.println("Your token:" + provider.getRefreshToken());
						 
					}else{
						 go = new PokemonGo(new GoogleUserCredentialProvider(http, token), http);
					}


				}else{
					go = new PokemonGo(new PtcCredentialProvider(http,user,pass), http);
				}
				System.out.println("------------------------------------------------------------------");
				System.out.println("Hello "+go.getPlayerProfile().getPlayerData().getUsername() + ", Your level: "+ go.getPlayerProfile().getStats().getLevel());				 
				// Set to current location
				go.setLocation(latitude, longitude,0);
				System.out.println("Your current location: " +  go.getLatitude() + ","+ go.getLongitude());
				System.out.println("--------------------------Waiting: 5 sec--------------------------");
	        	sleep(5000);
			}
			catch(Exception e){
				System.out.println("Login error: "+e);
				return;
			}
			
			
			//Main Loop
			while(true){
				System.out.println("------------------------------------------------------------------");
				System.out.println("Enter command:");
				String input=sc.nextLine();
				String[] splitInput = input.split(" ");
				String cmd = splitInput[0];
				if(cmd.equals("catch")){
					if(splitInput.length ==1){
						catchPokemonCoord();
					}else{
						try{
							catchPokemonNode(Integer.parseInt(splitInput[1]));				
						}
						catch(NumberFormatException e){
							System.out.println("Error - Usage: catch number eg. catch 18");
						}

						
					}
					
				
				}else if(cmd.equals("help")){
					System.out.println("------------------------------------------------------------------");
					System.out.println("List of commands:");
					System.out.println("1) list - list rare pokemons");
					System.out.println("2) find pokemonname - e.g. 'find SNORLAX' ");
					System.out.println("3) catch - Warp to catch pokemon at a certain cooridnates ");
					System.out.println("4) catch number - Use after 'list' or 'find' commands to catch certain pokemon e.g. catch 7 ");
					System.out.println("5) arrange - arrange the list alphabetically");
					System.out.println("6) help - command information");
					System.out.println("7) exit - exit the program");
				}else if(cmd.equals("exit")){
					break;
				}else if(cmd.equals("list")){
					nodeStorage = new ArrayList<node>();
					getPokeSnipers();
				}else if(cmd.equals("find")){
					if(splitInput.length==2){
						nodeStorage = new ArrayList<node>();
						findPokeSnipers(splitInput[1].toUpperCase());
					}else{
						System.out.println("Error - Usage: find pokemonname eg. find SNORLAX");
					}
				}else if(cmd.equals("arrange")){	
					Collections.sort(nodeStorage,nc);
					int size = nodeStorage.size();
					for(int i=0;i<size;i++){
		             	node n = nodeStorage.get(i);
		             	System.out.println(i + ") "+ n.getName() +" Coords:" + n.getCoord() + " Until:" + n.getUntil() );
					}
				}else{
					System.out.println("Invalid command: Enter 'help' for command info");
				}
				
				
		        
			}
			
		}
			
		}.start();
	
        
        
        
        

		
	}



}