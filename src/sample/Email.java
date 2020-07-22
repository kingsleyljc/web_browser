package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Email implements Initializable {
    public Label testInfo;
    @FXML
    private Button login;
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    public int age =123;
    public boolean ok =false;
    public String name ="asdqq";
    public boolean getOK(){return ok;}
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Main.controllers.put(this.getClass().getSimpleName(),this);
    }
    public void login(ActionEvent  event) {
        if (correctly()==0) {testInfo.setText("Login Success, please reopen this window.");
            ok = true;}
        else if(correctly()==1) testInfo.setText("Login Over time limit,Please try again");
        else if(correctly()==2) testInfo.setText("Incorrect Password");
        System.out.println("login over");
    }
    int correctly(){
        Mail mail = new Mail(username.getText(),password.getText());
        return mail.test();
    }
    public String getUsername(){return username.getText();}
    public String getPassword(){return password.getText();}

}