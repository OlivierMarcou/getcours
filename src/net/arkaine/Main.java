package net.arkaine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main extends Application {
    private static String urlApiAllCoins ="https://min-api.cryptocompare.com/data/all/coinlist";
    public static JSONObject json = getJson(urlApiAllCoins);

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Courts crypto");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static JSONObject getJson(String urlApi){
        JSONParser parser = new JSONParser();

        try {
            URL url = new URL(urlApi); // URL to Parse
            URLConnection urlCon = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));

            String inputLine;
            StringBuilder sb = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            JSONObject json = (JSONObject) parser.parse(sb.toString());
            JSONObject jsonData;
            if(json.get("Data") != null && json.get("Data").getClass().equals(JSONObject.class))
                jsonData = (JSONObject) json.get("Data");
            else
                jsonData = json;
            in.close();
            return jsonData;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
