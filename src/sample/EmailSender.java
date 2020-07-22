package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class EmailSender implements Initializable{
    public TextField eFrom;
    public TextField eTo;
    public TextField eTitle;
    public TextArea eContent;
    @FXML
    public boolean ok =false;
    public Label sendedInfo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Main.controllers.put(this.getClass().getSimpleName(),this);
    }


    public void sendEmail(ActionEvent event) {
        sendedInfo.setText("Sending...");
        String username = Main.Main_data.get("email_username");
        String password = Main.Main_data.get("email_password");
        String sender = eFrom.getText();
        String receiver = eTo.getText();
        String subject = eTitle.getText();
        String content = eContent.getText();
        Mail tmp_email = new Mail(username,password,content,sender,receiver,subject);
        Future<Integer> result = Main.exexcutor.submit(tmp_email);
        try{
            int tmp = result.get(5, TimeUnit.SECONDS);
            if(tmp==0)sendedInfo.setText("Posted Successfully");
            else if(tmp==1)sendedInfo.setText("Failed, please try again");
            else if(tmp==2)sendedInfo.setText("Connection failed");
            else if(tmp==4)sendedInfo.setText("Incorrect sender address");
            else if(tmp==5)sendedInfo.setText("Incorrect receiver address");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            sendedInfo.setText("Failed, please try again");
        } catch (TimeoutException e) {
            sendedInfo.setText("Connection failed");
        }
    }

}
class UIController1 implements Runnable{
    Label label;
    Future<Integer> result;
    UIController1(Future<Integer>a,Label b){
        label = b;
        result = a;
    }
    public void run(){
        label.setText("Sending...");
        try{
            int tmp = result.get(5, TimeUnit.SECONDS);
            if(tmp==0)label.setText("Posted Successfully");
            if(tmp==1)label.setText("Failed, please try again");
            if(tmp==2)label.setText("Connection failed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            label.setText("Failed, please try again");
        } catch (TimeoutException e) {
            label.setText("Connection failed");
        }

    }
}