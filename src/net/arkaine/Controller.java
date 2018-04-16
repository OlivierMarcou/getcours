package net.arkaine;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private ComboBox listsCoins;
    @FXML private TabPane tabSelectedCoins;
    @FXML private Label priceCoin;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ArrayList<String> listCoins = new ArrayList<>();
        for(Object name:Main.json.keySet()){
            if(name.getClass().equals(String.class)){
                listCoins.add((String) name);
            }
        }
        Collections.sort(listCoins);
        ObservableList<String> options =
                FXCollections.observableArrayList(listCoins);
        listsCoins.getItems().clear();
        listsCoins.setItems(options);
        listsCoins.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String coinNameOld, String coinName) {
           //     System.out.println(ov);
                Controller.this.addPrice(coinName);
            }
        });
    }

    public static final String urlApiPrice = "https://min-api.cryptocompare.com/data/price?tsyms=BTC,USD,EUR&fsym=";
    protected void addPrice(String coinName){
     //   System.out.println(coinName);
        JSONObject json = Main.getJson(urlApiPrice+coinName);System.out.println(urlApiPrice+coinName);
        for(Object key:json.keySet()){
            if(key.getClass().equals(String.class) && key != null)
            {System.out.println(key);
                if( key.equals("USD"))
                    priceCoin.setText( json.get(key) + " $" );
                if( key.equals("EUR"))
                    priceCoin.setText(priceCoin.getText() +" " + json.get(key) + " ?" );
            }
        }
    }
}
