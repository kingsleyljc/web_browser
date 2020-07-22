package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    public WebView webview;
    public TextField WebsiteField;
    public Button Jump;
    public Button Back;
    public Button Forward;
    public Button store;
    private String email_name;
    private String email_password;
    private WebEngine webEngine;
    private boolean email_login = false;
    private final String reg = "((http|ftp|https):\\/\\/)?[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    private final String protocol = "(http|ftp|https):\\/\\/\\S*";
    void jump_to(){
        String tmp_input = WebsiteField.getText();
        Pattern p =Pattern.compile(reg);
        if (p.matcher(tmp_input).matches()){
            if (!Pattern.compile(protocol).matcher(tmp_input).matches()){
                tmp_input = "http://"+tmp_input;
            }
        }
        else if(tmp_input.startsWith("http://")||tmp_input.startsWith("https://")){ }
        else{
            tmp_input = "https://www.baidu.com/s?wd="+tmp_input;
        }
        webEngine.load(tmp_input);
    }
    private Tab stable_page;
    public TabPane bookMark;
    private WebHistory history;
    static Map<String, List<String>> formation(List<String>headers){
        Map<String, List<String>> tmp = new HashMap<>();
        tmp.put("Set-Cookie",headers);
        return tmp;
    }
    public static void load_cookie()  {
        try{
            Main.cookieMap.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("./cookie/cookie.txt")));
            Vector<String> urls = new Vector<>();
            Vector<String> cookies = new Vector<>();
            String tmptmp;
            while ((tmptmp=reader.readLine())!=null){
                if (tmptmp.startsWith("url:"))
                    urls.add(tmptmp.substring(tmptmp.indexOf(":")+1));
                else if(tmptmp.startsWith("cookies:"))
                    cookies.add(tmptmp.substring(tmptmp.indexOf(":")+1));
                else throw new IOException();
            }
            for (int i = 0; i < urls.size(); i++) {
                URI uri = URI.create(urls.elementAt(i));
                String []tmp_cookie = cookies.elementAt(i).split(";");
                Main.cookieMap.put(urls.elementAt(i), Arrays.asList(tmp_cookie));
                java.net.CookieHandler.getDefault().put(uri, formation(Arrays.asList(tmp_cookie)));

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void setCookie(String url,String cookies)  {
        String []tmp_cookie = cookies.split(";");
        Main.cookieMap.put(url,Arrays.asList(tmp_cookie));
        try {
            PrintWriter writer= new PrintWriter(new OutputStreamWriter(new FileOutputStream("./cookie/cookie.txt")));
            for (Map.Entry<String, List<String>> a:Main.cookieMap.entrySet()){
                writer.println("url:"+a.getKey());
                String output = "";
                for (String tmp:a.getValue()){
                    output+=tmp+";";
                }
                writer.println("cookies:"+output);
            }
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookMark.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        Forward.setDisable(true);
        Back.setDisable(true);
        Main.controllers.put(this.getClass().getSimpleName()+Math.random(),this);//在Main里备案
        int num = Main.website_num;
        webEngine =  webview.getEngine();
        history = webEngine.getHistory();
        Main.tab_name.put(num,webEngine.titleProperty());
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        System.out.println(newState);
                        if (newState != Worker.State.SUCCEEDED) { //jump to标志的变化
                            if (Jump.getStyleClass().contains("goto"))
                                Jump.getStyleClass().remove("goto");
                            if (!Jump.getStyleClass().contains("loading"))
                                Jump.getStyleClass().add("loading");
                        }
                        if (newState == Worker.State.SUCCEEDED) {//jump to标志的变化
                            WebsiteField.setText(webEngine.getLocation());
                            if (Jump.getStyleClass().contains("loading"))
                            Jump.getStyleClass().remove("loading");
                            if (!Jump.getStyleClass().contains("goto"))
                            Jump.getStyleClass().add("goto");
                            try {
                                String cook = CookieHandler.getDefault().get(new URI(webEngine.getLocation()),new HashMap<>()).get("Cookie").get(0);
                                setCookie(webEngine.getLocation(),cook);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if (newState == Worker.State.CANCELLED){//jump to标志的变化
                            if (Jump.getStyleClass().contains("loading"))
                                Jump.getStyleClass().remove("loading");
                            if (!Jump.getStyleClass().contains("goto"))
                                Jump.getStyleClass().add("goto");
                        }
                        else if (newState == Worker.State.FAILED){//jump to标志的变化
                            if (Jump.getStyleClass().contains("loading"))
                                Jump.getStyleClass().remove("loading");
                            if (!Jump.getStyleClass().contains("goto"))
                                Jump.getStyleClass().add("goto");
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Information Dialog");
                                alert.setHeaderText(null);
                                alert.setContentText("Connection Over Time. Please try again.");
                                alert.showAndWait();
                            });
                        }
                        if (history.getCurrentIndex()==0)Back.setDisable(true);
                        else Back.setDisable(false);
                        if (history.getCurrentIndex()==history.getEntries().size()-1)Forward.setDisable(true);
                        else Forward.setDisable(false);
                    }
                });
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("hello:"+newValue);
                File file = new File("./download");
                String[] downloadableExtensions = {".doc", ".xls", ".zip", ".exe", ".rar", ".pdf", ".jar", ".png", ".jpg", ".gif"};
                for(String downloadAble : downloadableExtensions) {
                    if (newValue.endsWith(downloadAble)) {
                        try {
                            if(!file.exists()) {
                                file.mkdir();
                            }
                            single_downloader(newValue);
                            break;
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        try {
            load_bookmark();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        load_cookie();
        webEngine.load("https://baidu.com/");
    }
    public void go_back(ActionEvent event){
        history.go(-1);
    }
    public void go_forward(ActionEvent event){
        history.go(1);
    }
    public void go_home(ActionEvent event){
        webEngine.load("http://www.baidu.com");
    }
    public void go_refresh(ActionEvent event){
        webEngine.reload();
    }
    public void jump_botton(ActionEvent event){
        jump_to();
    }
    public void email_module(ActionEvent event){
        System.out.println("???");
        if(!email_login)
            login_email_module();
        else
            send_email();
    }
    public void setEmail(String name,String password){
        email_login = true;
        email_name = name;
        email_password = password;
    }
    public void login_email_module(){
        try{
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/email.fxml"));
            Parent root = loader.load();
            stage.setTitle("Email Login");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();
            while(!Main.controllers.containsKey("Email"));
            Email email = (Email)Main.controllers.get("Email");
            Thread th1 = new Thread(new Content(email,this));
            th1.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void send_email(){
        try{
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/send_mail.fxml"));
            Parent root = loader.load();
            stage.setTitle("Email Send");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void keyRelease(javafx.scene.input.KeyEvent keyEvent) {
        if (KeyCode.ENTER.equals(keyEvent.getCode())){
            System.out.println("enter");
            jump_to();
        }
    }
    public void click_website(MouseEvent mouseEvent) {
        WebsiteField.selectAll();
    }
    public void single_downloader(String newValue){
        try{
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/single_download.fxml"));
//            Single_download_control single_download_control = new Single_download_control(newValue);
            loader.setController(new Single_download_control(newValue));
            Parent root = loader.load();
            stage.setTitle("Downloader");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public void open_downloader(ActionEvent event) {
        try{
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/Download_page.fxml"));
            loader.setController(new Download_control());
            Parent root = loader.load();
            stage.setTitle("Downloader");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    static boolean bookmark_lock = false;
    public void add_bookmark(ActionEvent event) throws IOException {
        String url = webEngine.getLocation();
        String name = webEngine.getTitle();
        addBookmark(url,name);
        bookmark_lock = true;
        for (Object controller:Main.controllers.keySet()){
            ((Controller)Main.controllers.get(controller)).load_bookmark();
        }
        bookmark_lock = false;
    }
    public void load_bookmark() throws IOException {
        bookmark_url.clear();
        bookmark_name.clear();
        File path = new File("./bookmark/bookmark.txt");
        if (!path.exists())return;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String tmp;
        while ((tmp=reader.readLine())!=null){
            if (tmp.contains("url"))
                bookmark_url.add(tmp.substring(tmp.indexOf(":")+1));
            else if (tmp.contains("name"))
                bookmark_name.add(tmp.substring(tmp.indexOf(":")+1));
        }
        bookMark.getTabs().clear();
        for (int i = 0; i <bookmark_url.size() ; i++) {
            String url = bookmark_url.elementAt(i);
            String name = bookmark_name.elementAt(i);
            BookTab tab = new BookTab(url,name);
            tab.setClosable(true);
            bookMark.getTabs().add(tab);
            tab.setText(name);
            tab.setOnSelectionChanged(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (!bookmark_lock){
                        webEngine.load(tab.url);
                    }
                }
            });
            tab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    try {
                        deleteBookmark(tab.url,tab.name);
                        bookmark_lock = true;
                        for (Object controller:Main.controllers.keySet()){
                            System.out.println("main:");
                            ((Controller)Main.controllers.get(controller)).load_bookmark();
                        }
                        bookmark_lock = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void deleteBookmark(String url,String name) throws IOException {
        bookmark_url.remove(url);
        bookmark_name.remove(name);
        File path = new File("./bookmark/bookmark.txt");
        if (!path.exists())
            path.createNewFile();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path)));
        for (int i = 0; i <bookmark_url.size() ; i++) {
            String uu = bookmark_url.elementAt(i);
            String nn = bookmark_name.elementAt(i);
            writer.println("url:"+uu);
            writer.println("name:"+nn);
        }
        writer.close();
    }
    public void addBookmark(String url,String name) throws IOException {
        bookmark_url.add(url);
        bookmark_name.add(name);
        File path = new File("./bookmark/bookmark.txt");
        if (!path.exists())
            path.createNewFile();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path)));

        for (int i = 0; i <bookmark_url.size() ; i++) {
            String uu = bookmark_url.elementAt(i);
            String nn = bookmark_name.elementAt(i);
            writer.println("url:"+uu);
            writer.println("name:"+nn);
        }
        writer.close();
    }
    static Vector<String>bookmark_url = new Vector<>();
    static Vector<String>bookmark_name= new Vector<>();
}
class BookTab extends Tab{
    String url;
    String name;
    BookTab(String url,String name){
      super();
      this.setClosable(false);
      this.url = url;
      this.name = name;
    }
}
class Content implements Runnable{
    Email email;
    Controller controller;
    Content(Email email,Controller controller){
        this.email = email;this.controller = controller;
    }
    @Override
    public void run() {
        try{
            System.out.println("begin");
            while(!email.getOK()){
                Thread.sleep(300);
            }
            controller.setEmail(email.getUsername(),email.getPassword());
            Main.Main_data.put("email_username",email.getUsername());
            Main.Main_data.put("email_password",email.getPassword());
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("over");
    }
}