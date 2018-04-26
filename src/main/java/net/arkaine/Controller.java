package net.arkaine;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
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
    @FXML private Button priceCoinRefresh;
    @FXML private  TextArea volumeCoin;
    @FXML private Label valeur;

    ArrayList<String> activeCoins = new ArrayList<>();
    private HashMap<String, Tab> savedMoney = new HashMap<>();
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Main.json.keySet().forEach(name -> activeCoins.add((String) name));
        for(String money: Main.properties.getProperty("listMonnaies").split(","))
            savedMoney.put(money, addTab(money));

        Collections.sort(activeCoins);
        ObservableList<String> options =
                FXCollections.observableArrayList(activeCoins);
        listsCoins.getItems().clear();
        listsCoins.setItems(options);

        listsCoins.valueProperty().addListener(( ov,  coinNameOld,  coinName) ->{
            priceCoin.setText(Controller.this.addPrice((String)coinName, true));
        });
        autoCompletion.setOnKeyReleased(ke -> {
            System.out.println("setOnKeyReleased : " + ke.getCode() );
            Optional<String> result = listsCoins.getItems().stream().filter(name -> autoCompletion.getText()!= null
                    && !autoCompletion.getText().trim().isEmpty()
                    && ((String)name).toLowerCase().startsWith(autoCompletion.getText().toLowerCase())).findFirst();
            result.ifPresent(s ->{
                    listsCoins.getSelectionModel().select(s);
                    priceCoin.setText(addPrice(s, true));
                    if(ke.getCode().equals(KeyCode.ENTER)){
                        addTab(s);
                    }
            });
        });
        volumeCoin.textProperty().addListener(
              ( observable,  oldValue,
                                 newValue) -> {
                if (!newValue.matches("[\\d.]*")) {
                    volumeCoin.setText(newValue.replaceAll("[^\\d.]", ""));
                }else{
                    if(volumeCoin.getText().isEmpty())
                        volumeCoin.setText("0");
                    valeur.setText(String.valueOf(Double.parseDouble(volumeCoin.getText())*dollarValue)+" $");
                }
            });
        showEuro.setOnAction(eventEuro -> {refreshTab();});
        showDollar.setOnAction(eventDollar -> {refreshTab();});
        showBTC.setOnAction(eventBTC ->{refreshTab();});
        deleteBtn.setOnAction( eventDelete -> {
                savedMoney.clear();
                tabSelectedCoins.getTabs().clear();
                save();
        });
        saveBtn.setOnAction(eventSave-> {
                String money = (String) listsCoins.getSelectionModel().getSelectedItem();
                if(money != null && !money.isEmpty() && !savedMoney.containsKey(money) && activeCoins.contains(money)) {
                    savedMoney.put(money, addTab(money));
                }
                save();
            });
        priceCoinRefresh.setOnAction(eventRefreshCoin-> {
                String money = (String) listsCoins.getSelectionModel().getSelectedItem();
                if(money != null && !money.isEmpty()) {
                    priceCoin.setText(addPrice(money, true));
                }
            });
        tabSelectedCoins.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }

    protected void save(){
        System.out.println("Saving ...");
        try {
            String content =  ("listMonnaies=");
            List<String>memToken = new ArrayList<>();
            for(String token: savedMoney.keySet()){
                System.out.println("Save :" + token);
                if(token != null
                        && !token.trim().isEmpty()
                        && activeCoins.contains(token)
                        && !memToken.contains(token)) {
                    content += token + ",";
                    memToken.add(token);
                }
            }
                System.out.println("Save ALL :" + content);
            Files.write(Paths.get("liste.properties"), content.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isNotRefresh = true;

    private Tab addTab(String money) {
        System.out.println("Add Tab"+ money);
        if(!money.trim().isEmpty() && activeCoins.contains(money) && !savedMoney.containsKey(money)) {
            TabAutoRefresh tab = new TabAutoRefresh(money, this);
            tab.setOnClosed((eventTab)->{
                tabSelectedCoins.getTabs().remove(this);
                System.out.println("remove " + money);
                savedMoney.remove(money);
                save();
            });

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
            System.out.println(money);
            savedMoney.put(money,tab);
            return tab;
        }
        return null;
    }

    private void refreshTab() {
        System.out.println("refresh Tab");
        isNotRefresh = false;
        try {
            Main.initProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        savedMoney = new HashMap<>();
        tabSelectedCoins.getTabs().clear();
        for(String money: Main.properties.getProperty("listMonnaies").split(","))
            if(activeCoins.contains(money))
                savedMoney.put(money, addTab(money));
    }

    public static final String urlApiPrice = "https://min-api.cryptocompare.com/data/price?tsyms=BTC,USD,EUR&fsym=";
    private double dollarValue = 0 ;

    protected String addPrice(String coinName){
        return addPrice(coinName, false);
    }
    protected String addPrice(String coinName, boolean isPriceCoin){
       // System.out.println("addPrice" + coinName);
        JSONObject json = Main.getJson(urlApiPrice+coinName);
        StringBuilder result = new StringBuilder();
        Consumer<String> consumerCoins = key -> {
            if(key.getClass().equals(String.class) && key != null)
            {
                if( key.equals("USD") && showDollar.isSelected()){
                    if(isPriceCoin){
                        dollarValue = (double)json.get(key);
                        valeur.setText(String.valueOf(Double.parseDouble(volumeCoin.getText())*dollarValue)+" $");
                    }
                    result.append(json.get(key) + " $ ");                }
                if( key.equals("EUR") && showEuro.isSelected())
                    result.append(json.get(key) + " E ");
                if( key.equals("BTC") && showBTC.isSelected())
                    result.append(json.get(key) + " B ");
            }};
        json.keySet().forEach(consumerCoins);
        return result.toString();
    }
}
