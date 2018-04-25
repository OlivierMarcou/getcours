package net.arkaine;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Tab;

public class TabAutoRefresh extends Tab{

    public TabAutoRefresh(String money, Controller parent){
        super(money + " " + parent.addPrice(money));
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                int i;
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }

                    // Update the GUI on the JavaFX Application Thread
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            TabAutoRefresh.this.setText(money + " " + parent.addPrice(money));
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
