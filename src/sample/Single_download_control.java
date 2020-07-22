package sample;

import com.sun.javaws.progress.Progress;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Deque;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Single_download_control  implements Initializable {

    public ProgressBar progress;
    public String file_url;
    public DownloadHunter downloadHunter;
    public int pos = 0;
    public Label filename_label;
    public Button start_stop;

    Single_download_control(String tmp){
        file_url = tmp;
    }
    Single_download_control(String tmp,int pos){
        file_url = tmp;
        this.pos = pos;
    }
    private void start(){
        System.out.println("file_url:"+file_url);
        downloadHunter = new DownloadHunter(file_url,pos);
        filename_label.setText(file_url.substring(file_url.lastIndexOf('/')+1));
        downloadHunter.hunter_download();
        downloadHunter.scheduler.progress.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("进度："+newValue.doubleValue());
                Platform.runLater(() -> {progress.setProgress(newValue.doubleValue()/100);});
            }
        });
        downloadHunter.downloader._control.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("new value:"+newValue);
                if (!save&&!newValue){
                    progress.setProgress(1);
                    Platform.runLater(() -> {((Stage)progress.getScene().getWindow()).close();
                    String warning = "Download Finished.";
                        if (cancel){
                            warning = "Download has been cancelled.";
                            Main.deleteTempFile(filename_label.getText());
                        }
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText(warning);
                        alert.showAndWait();
                    });

                }
            }
        });
    }
    boolean save = false;
    boolean start = true;
    public void initialize(URL url, ResourceBundle resourceBundle) {
        start();
    }
    public void start_or_stop(ActionEvent event){
        if (start){
            save = true;
            start = false;
            downloadHunter.downloader.stopControl();
            try {
                pos = downloadHunter.result.get(8, TimeUnit.SECONDS);
            }
            catch (Exception e){
                e.printStackTrace();
                pos = -1;
            }
            System.out.println("ui的pos："+pos);
            progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
        else {
            save = false;
            start = true;
            start();
        }

    }
    public void write_the_log(ActionEvent event) {
        save = true;
        downloadHunter.downloader.stopControl();
        Platform.runLater(() -> {((Stage)progress.getScene().getWindow()).close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("The download record has been saved. You can continue downloading next time!");

            alert.showAndWait();});
//        downloadHunter.downloader.writeLog();
    }
    boolean cancel = false;
    public void cancel_mission(ActionEvent event) {
        cancel = true;
        downloadHunter.downloader.stopControl();
        downloadHunter.downloader.makeCanel();
    }
}
