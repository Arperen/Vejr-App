import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// henter vejr data fra API - med denne backend henter vi seneste vejr data fra den eksterne API og give det tilbage.
// gui vil display dataen den som den modtager til brugeren.
public class WeatherApp {
    // henter vejr data fra givne lokation.
    public static JSONObject getWeatherData(String locationName){
        // skaffer lokations kordinater ved hjelp af geolocation API'en.
        JSONArray locationData = getLocationData(locationName);
        // henter latitude og longitude data (Hvad fanden er det danske ord for de mål?)
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // API anmodning URL med locations kordinater
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

        try{
            // caller api og får respons
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for respons status
            // hvis der står 200 - betyder det at forbindelsen er succesfuld
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Kunne ikke oprette forbbindelse til API");
                return null;
            }

            // opbevar resulterende json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                // læs og opbevar ind i string buildereren
                resultJson.append(scanner.nextLine());
            }

            // dis scanner
            scanner.close();

            // dis url connection
            conn.disconnect();

            // parser gennem dataen
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // hent data fra den seneste timelige opdatering
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // vi gerne have dataen for den nuværende time, så vi har brug for indexed af den nuværende time
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // henter temperatur
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // henter vejr kode (vejr kode er en kode der beskriver den overordnet status af vejret aka. regner, sner og overskyet)
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // henter fugtighed (jeg kunne ikke finde ud af at få regn mængde så tænkte fugtighed er en god erstatning)
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // henter vindhastighed
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // laver det vejr json data object som vi bruger til frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // henter givne lokation for byger man søger på
    public static JSONArray getLocationData(String locationName){
        // erstater "whitespace" i lokation så den indrætter til API'ens request format
        locationName = locationName.replaceAll(" ", "+");

        // laver API url med lokation parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // call api og få et respons
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check respons status
            // 200 betyder succefuld forbindelse ligesom da vi oprettede forbindelse til den anden API
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Kunne ikke oprette forbbindelse til API");
                return null;
            }else{
                // Stringbuild dataen
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // læs og opbevar json dataen ind i string builderen
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // luk scanner
                scanner.close();

                // luk url connection
                conn.disconnect();

                // parse JSON string ind i JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // skafer listen af location data fra API generered fra locations navnet
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        // Kunne ikke finde lokation
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // forsøgte at oprette forbindelse
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set anmodning metode til get
            conn.setRequestMethod("GET");

            // opretter forbindelse til vores API
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        // kunne ikke oprette forbindelse
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // iterer gennem tids listen og se hvilken en der matcher vores nuverende tid
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // return the index
                return i;
            }
        }

        return 0;
    }

    private static String getCurrentTime(){
        // Skaffer nuverende data og tid
        LocalDateTime currentDateTime = LocalDateTime.now();

        // formater dato til at være 2023-09-02T00:00 (dette er sådan det læses i API'en)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // formater og print den nuværende dato og tid
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // konvertere vejr koden så den kan aflæses nemmere/ kan aflæses
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            // skyfrit
            weatherCondition = "Clear";
        }else if(weathercode > 0L && weathercode <= 3L){
            // skyget
            weatherCondition = "Cloudy";
        }else if((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 99L)){
            // rejn
            weatherCondition = "Rain";
        }else if(weathercode >= 71L && weathercode <= 77L){
            // sne
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
