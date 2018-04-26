package net.arkaine;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Tab;

import javafx.scene.text.Text;

public class TabAutoRefresh extends Tab{

    public TabAutoRefresh(String money, Controller parent){
        super(money + " \n" + parent.addPrice(money));
        setStyle(
                "-fx-font-weight: bold;");
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                int i;
                while(true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                    }

                    // Update the GUI on the JavaFX Application Thread
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            String prices = parent.addPrice(money);
                            if(!prices.trim().isEmpty()) {
                                TabAutoRefresh.this.setStyle("-fx-text-base-color: black;");
                                TabAutoRefresh.this.setText(money + " \n" + prices);
                            }
                            else
                                TabAutoRefresh.this.setStyle("-fx-text-base-color: red;");
                        }
                    });

                }
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}
