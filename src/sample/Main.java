package sample;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    public static Map<String,Object>controllers = new HashMap<String,Object>();
    public static Map<String,String>Main_data = new HashMap<String,String>();
    public static ExecutorService exexcutor = Executors.newCachedThreadPool();
    public static int website_num = 0;
    public static void increase_website_num(){
        website_num++;
    }
    public static int getWebsite_num(){
        return website_num;
    }

    public static Map<String, List<String>> cookieMap = new LinkedHashMap<>();
    public static Map<Integer,ReadOnlyStringProperty> tab_name = new HashMap<>();
//    public static DownloadHunter downloader = new DownloadHunter();
    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/sample/tabpane.fxml"));
        primaryStage.setTitle("KingBrowser");
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
    public static void deleteTempFile(String filename){
        File log = new File("./history/"+filename+".txt");
        if(log.exists()){
            log.delete();
            System.out.println("已删除log");
        }
        File tmp = new File("./download/"+filename+"tmp");
        if(tmp.exists()){
            tmp.delete();
            System.out.println("已删除临时下载文件");

        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
