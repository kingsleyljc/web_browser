package sample;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.concurrent.Callable;

class Mail implements Callable<Integer> {
    private String name,password;
    private String code_name;
    private String code_password;
    private String content;
    private String sender;
    private String receiver;
    private String subject;
    Mail(String name,String password){
        this.name = name;
        this.password = password;
        code_name = Base64.getEncoder().encodeToString(name.getBytes());
        code_password = Base64.getEncoder().encodeToString(password.getBytes());
    }
    Mail(String name,String password,String content,String sender, String receiver,String subject){
        this.name = name;
        this.password = password;
        code_name = Base64.getEncoder().encodeToString(name.getBytes());
        code_password = Base64.getEncoder().encodeToString(password.getBytes());
        this.content=content;
        this.sender=sender;
        this.receiver=receiver;
        this.subject=subject;
    }

    @Override
    public Integer call() {
        return send(content,sender,receiver,subject);
    }

    int test(){ //测试用户名与密码的正确性
        while(true){
            try{
                System.out.println("testing---------------------");
                Socket socket = new Socket("smtp.qq.com",25);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp;
                System.out.println(reader.readLine());
                writer.println("Helo qq.com");
                System.out.println(reader.readLine());
                System.out.println(reader.readLine());
                System.out.println(reader.readLine());
                writer.println("auth login");
                System.out.println(reader.readLine());
                writer.println(code_name);
                System.out.println(reader.readLine());
                writer.println(code_password);
                tmp = reader.readLine();
                System.out.println("tmp:"+tmp);
                if (tmp.contains("535"))return 2;
                if (tmp.contains("235"))return 0;
                return 1;
            } catch (UnknownHostException e) {
                System.err.println("找不到域名");
            } catch (IOException e) {
                System.err.println("连接失败");
            }
        }
    }

    int send(String content,String sender, String receiver,String subject){
        try{
            Socket socket = new Socket("smtp.qq.com",25);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String tmp;
            System.out.println(reader.readLine());
            writer.println("Helo qq.com");
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            writer.println("auth login");
            System.out.println(reader.readLine());
            writer.println(code_name);
            System.out.println(reader.readLine());
            writer.println(code_password);
            System.out.println(reader.readLine());
            writer.println("MAIL FROM:<"+name+">");
            tmp = reader.readLine();
            System.out.println(tmp);
            if(!tmp.startsWith("250"))return 4;//发件人错误
            writer.println("RCPT TO:<"+receiver+">");
            tmp = reader.readLine();
            System.out.println(tmp);
            if(!tmp.startsWith("250"))return 5;//收件人错误
            writer.println("DATA");
            System.out.println(reader.readLine());
            writer.println("subject:"+subject);
            writer.println("from:"+sender);
            writer.println("to:" + receiver);
            writer.println("Content-Type: text/plain;charset=utf8");
            writer.println("");
            writer.println(content);
            writer.println(".");
            tmp=reader.readLine();
            System.out.println(tmp);
            if(tmp.startsWith("250"))return 0;
            return 1;

        } catch (UnknownHostException e) {
            System.err.println("找不到域名");
        } catch (IOException e) {
            System.err.println("连接失败");
        }
        return 2;

    }
}