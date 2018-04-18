package net.arkaine;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

public class Controller implements Initializable {
    @FXML private CheckBox showEuro;
    @FXML private CheckBox showDollar;
    @FXML private CheckBox showBTC;
    @FXML private ComboBox listsCoins;
    @FXML private TabPane tabSelectedCoins;
    @FXML private Label priceCoin;
    @FXML private Button deleteBtn;
    @FXML private Button saveBtn;
    @FXML private TextField autoCompletion;

    private HashMap<String, Tab> savedMoney = new HashMap<>();
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for(String money: Main.properties.getProperty("listMonnaies").split(","))
            savedMoney.put(money, addTab(money));
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

        listsCoins.valueProperty().addListener(( ov,  coinNameOld,  coinName) ->{
            priceCoin.setText(Controller.this.addPrice((String)coinName));
        });
        autoCompletion.setOnKeyReleased(ke -> {
                    System.out.println("key"+ke);
            Optional<String> result = listsCoins.getItems().stream().filter(name -> autoCompletion.getText()!= null
                    && !autoCompletion.getText().trim().isEmpty()
                    && ((String)name).toLowerCase().startsWith(autoCompletion.getText().toLowerCase())).findFirst();
            result.ifPresent(s ->{
                    listsCoins.getSelectionModel().select(s);
                    priceCoin.setText(addPrice(s));
                    System.out.println("listsCoins.setValue("+s+")"); });
        });
        showEuro.setOnAction(eventEuro -> {refreshTab();});
        showDollar.setOnAction(eventDollar -> {refreshTab();});
        showBTC.setOnAction(eventBTC ->{refreshTab();});
        deleteBtn.setOnAction( eventDelete -> {
                savedMoney.clear();
                tabSelectedCoins.getTabs().clear();
                try {
                    Files.delete(Paths.get("liste.properties"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });
        saveBtn.setOnAction(eventSave-> {
                String money = (String) listsCoins.getSelectionModel().getSelectedItem();
                if(money != null && !money.isEmpty() && !savedMoney.containsKey(money)) {
                    savedMoney.put(money, addTab(money));
                    try {
                        String content =  ("listMonnaies=");
                        for(String token: savedMoney.keySet())
                            content += token+",";
                        Files.write(Paths.get("liste.properties"), content.getBytes(), StandardOpenOption.CREATE);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }
    private boolean isNotRefresh = true;
    private Tab addTab(String money) {
        if(!money.trim().isEmpty()) {
            Tab tab = new Tab(money + " " + addPrice(money));

            WebView browser = new WebView();
            WebEngine webEngine = browser.getEngine();
            String url = "https://www.cryptocompare.com/coins/"+money.toLowerCase()+"/overview/USD";
            tab.setOnSelectionChanged(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if(tab.isSelected())
                        webEngine.load(url);
                }
            });
            tab.setContent(browser);
            tabSelectedCoins.getTabs().add(tab);
            return tab;
        }
        return null;
    }

    private void refreshTab() {
        isNotRefresh = false;
        try {
            Main.initProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        savedMoney = new HashMap<>();
        tabSelectedCoins.getTabs().clear();
        for(String money: Main.properties.getProperty("listMonnaies").split(","))
            savedMoney.put(money, addTab(money));
    }


    public static final String urlApiPrice = "https://min-api.cryptocompare.com/data/price?tsyms=BTC,USD,EUR&fsym=";

    protected String addPrice(String coinName){
        JSONObject json = Main.getJson(urlApiPrice+coinName);
        StringBuilder result = new StringBuilder();
        Consumer<String> consumerCoins = key -> {
            if(key.getClass().equals(String.class) && key != null)
            {
                if( key.equals("USD") && showDollar.isSelected())
                    result.append(json.get(key) + " $ ");
                if( key.equals("EUR") && showEuro.isSelected())
                    result.append(json.get(key) + " E ");
                if( key.equals("BTC") && showBTC.isSelected())
                    result.append(json.get(key) + " B ");
            }};
        json.keySet().forEach(consumerCoins);
        return result.toString();
    }
}
