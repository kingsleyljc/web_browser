package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabController {

    public TabPane tab_pane;
    public Tab tab2;

    public void add_tab(Event event) throws IOException {
        if (!tab2.isSelected())return;
        Tab tab = new Tab();
        tab.setContent(FXMLLoader.load(getClass().getResource("/sample/first.fxml")));
        int num = Main.website_num;
        tab_pane.getTabs().add(tab);
//        while (Main.tab_name.size()==0);
        tab.textProperty().bind(Main.tab_name.get(num));
        tab_pane.getTabs().remove(tab2);
        tab_pane.getSelectionModel().select(tab);
        tab_pane.getTabs().add(tab2);
        Main.website_num++;
    }
}
