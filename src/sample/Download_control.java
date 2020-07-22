package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Deque;
import java.util.ResourceBundle;
import java.util.Vector;

public class Download_control  implements Initializable {
    public ListView load_list;
    class HBoxCell extends HBox {
        Label mission_name = new Label();
        Label progress = new Label();
        Button stop = new Button();
        Button cancel = new Button();
        String filename;
        int pos;
        HBoxCell(SimpleStringProperty a, SimpleStringProperty b,int pos,SimpleStringProperty c){//a是filename c是url
            super();
            this.pos = pos;
            this.setSpacing(5);
            filename = a.getValue();
            mission_name.textProperty().bind(a);
            mission_name.setMaxWidth(Double.MAX_VALUE);
            mission_name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(mission_name, Priority.ALWAYS);
            progress.textProperty().bind(b);
            progress.setMaxWidth(Double.MAX_VALUE);
            stop.setText("Start");
            stop.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    try{
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/single_download.fxml"));
                        loader.setController(new Single_download_control(c.getValue(),pos));
                        Parent root = loader.load();
                        stage.setTitle("Downloader");
                        stage.setScene(new Scene(root, 400, 400));
                        stage.show();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            cancel.setText("Cancel");
            cancel.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    try{
                        try{
                            Main.deleteTempFile(filename);

                            load_ui();
                        }
                        catch (Exception ee){
                            ee.printStackTrace();
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

            });
            this.getChildren().addAll(mission_name,progress,stop,cancel);
        }
    }
    void load_ui() throws Exception{
        left_progress.clear();
        left_filename.clear();
        left_pos.clear();
        left_mission.clear();
        File path = new File("./history");
        File[] tempList = path.listFiles();

            for (File file:tempList){
                if(file.exists()){
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String tmp;
                    while ((tmp=reader.readLine())!=null){
                        System.out.println(tmp);
                        if(tmp.contains("Url:")){
                            left_mission.add(tmp.substring(4));
                        }
                        else if(tmp.contains("Pos:")){
                            left_pos.add(tmp.substring(4));
                        }
                        else if(tmp.contains("Progress:")){
                            left_progress.add(tmp.substring(tmp.indexOf(":")+1));
                        }
                        else if(tmp.contains("Filename:")){
                            left_filename.add(tmp.substring(tmp.indexOf(":")+1));
                        }
                    }
                    reader.close();
                }
            }
        ObservableList<HBoxCell> data = FXCollections.observableArrayList();
        int len = left_pos.size();
        for (int i = 0;i<len;i++){
            SimpleStringProperty c = new SimpleStringProperty(left_mission.elementAt(i));
            SimpleStringProperty b = new SimpleStringProperty(left_progress.elementAt(i));
            SimpleStringProperty a = new SimpleStringProperty(left_filename.elementAt(i));
            data.addAll(new HBoxCell(a,b,Integer.parseInt(left_pos.elementAt(i)),c));
        }
        load_list.setItems(data);
    }

    Vector<String> left_mission= new Vector<>();
    Vector<String> left_pos= new Vector<>();
    Vector<String> left_progress= new Vector<>();
    Vector<String> left_filename= new Vector<>();
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try{
            load_ui();
        }
        catch (Exception e){
            System.out.println("读入失败，请重试");
            return;
        }

    }

}
