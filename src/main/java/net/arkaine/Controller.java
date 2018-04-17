package net.arkaine;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Controller implements Initializable {

    @FXML private ComboBox listsCoins;
    @FXML private TabPane tabSelectedCoins;
    @FXML private Label priceCoin;
    @FXML private Button deleteBtn;
    @FXML private Button saveBtn;
    private List<String> savedMoney = new ArrayList<String>();
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for(String money: Main.properties.getProperty("listMonnaies").split(","))
            savedMoney.add(money);
        ArrayList<String> listCoins = new ArrayList<>();
        for(Object name:Main.json.keySet()){
            if(name.getClass().equals(String.class)){
                listCoins.add((String) name);
            }
        }
        Collections.sort(listCoins);
        ObservableList<String> options =
                FXCollections.observableArrayList(listCoins);
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                savedMoney.clear();
                tabSelectedCoins.getTabs().clear();
                try {
                    Files.delete(Paths.get("liste.properties"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                String money = (String) listsCoins.getSelectionModel().getSelectedItem();
                if(money != null && !money.isEmpty() && !savedMoney.contains(money)) {
                    savedMoney.add(money);
                    try {
                        String content =  ("listMonnaies=");
                        for(String token: savedMoney)
                            content += token+",";
                        Files.write(Paths.get("liste.properties"), content.getBytes(), StandardOpenOption.CREATE);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    addTab(money);

                }
            }

        });
        listsCoins.getItems().clear();
        listsCoins.setItems(options);
        listsCoins.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String coinNameOld, String coinName) {
                priceCoin.setText(Controller.this.addPrice(coinName));
            }
        });
        for(String money:savedMoney){
            addTab(money);
        }
    }

    private void addTab(String money) {
        if(!money.trim().isEmpty()) {
            Tab tab = new Tab(money + " " + addPrice(money));
            HBox hbox = new HBox();
            hbox.getChildren().add(new Label(money));
            hbox.setAlignment(Pos.CENTER);
            tab.setContent(hbox);
            tabSelectedCoins.getTabs().add(tab);
        }
    }

    public static final String urlApiPrice = "https://min-api.cryptocompare.com/data/price?tsyms=BTC,USD,EUR&fsym=";

    protected String addPrice(String coinName){
        JSONObject json = Main.getJson(urlApiPrice+coinName);

        StringBuilder result = new StringBuilder();
        for(Object key:json.keySet()){
            if(key.getClass().equals(String.class) && key != null)
            {
                System.out.println(key);
                if( key.equals("USD"))
                    result.append(json.get(key) + " $ ");
                if( key.equals("EUR"))
                    result.append(json.get(key) + " E ");
            }
        }
        return result.toString();
    }
}
