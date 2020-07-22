package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DownloadHunter{
    public Downloader downloader;
//    public Progressor progressor;
    public Scheduler scheduler;
    public String filename;
    private int pos;
    private String goal;
    public Future<Integer> result;
    DownloadHunter(String goal,int pos){
        this.goal = goal;
        this.pos = pos;
        filename = goal.substring(goal.lastIndexOf('/')+1);
        System.out.println("---------------:"+filename);
    }
    void hunter_download(){
        downloader = new Downloader(goal,pos);
        scheduler = new Scheduler(downloader);
        Thread thread2 = new Thread(scheduler);
        thread2.start();
        result = Main.exexcutor.submit(downloader);
    }

}
class Downloader implements  Callable<Integer> {
    String url;
    String filename;
    private int pos = 0;
    boolean cancel = false;
    int content_length = 1;
    boolean start = false;
    boolean download_over = false;
    boolean install_over = false;
    boolean control = true;
    BooleanProperty _control = new SimpleBooleanProperty(true);
    byte[] arr;
    int origin;
    Downloader(String a,int b){url=a;origin=b;}
    public void stopControl(){
        control = false;
        _control.setValue(false);
    }
    public void makeCanel(){
        cancel = true;
    }
    int[] getPos(){
        int [] situation = new int[3];
        situation[0] = 0;
        if (download_over)situation[0] = 1;
        if(install_over)situation[0]=2;
        situation[1] = pos+origin;
        situation[2] = content_length+origin;
        return situation;
    }
    int download(String url){
        try{
            URL goal_url = new URL(url);
            filename = url.substring(url.lastIndexOf('/')+1);
            HttpURLConnection connection = (HttpURLConnection)goal_url.openConnection();
            connection.setRequestProperty("RANGE","bytes="+origin+"-");
            connection.setRequestProperty("Accept-Encoding", "identity");
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            content_length = connection.getContentLength();
            if(connection.getHeaderField("Content-Disposition")!=null){
                filename = connection.getHeaderField("Content-Disposition");
                filename = new String(filename.getBytes("ISO-8859-1"), "GBK");
                filename = URLDecoder.decode(filename.substring(filename.indexOf("filename=")+9),"UTF-8");
                filename = filename.replace("\"","");
            }
            System.out.println("响应码："+connection.getResponseCode());
            System.out.println(connection.getHeaderFields());
            System.out.println("length:"+connection.getContentLength());
            System.out.println("filename:@"+filename+"@");
            if (content_length==-1){
                int len;
                byte[] bytes = new byte[1024];
                File file = new File("./Download/"+filename);
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file,true));
                while((len = in.read(bytes))!=-1){
                    out.write(bytes,0,len);
                }
                out.flush();
                install_over = true;
                out.close();

            }
            else {
                arr = new byte[content_length];
                start = true;
                while(pos<content_length&&control){
                    int byte_read = in.read(arr,pos,content_length-pos);
                    if (byte_read==-1)break;
                    pos+=byte_read;
                }
                if(cancel){
                    System.out.println("cancel了");
                    return -1;
                }
                if (!control){
                    download_over = true;
                    install_over = true;
                    throw new InterruptedException("cut");
                }
                if(pos!=content_length){
                    throw new Exception("Only read "+pos +"bytes;  Expected:"+content_length+"bytes");
                }
                download_over = true;
                File file = new File("./Download/"+filename+"tmp");
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file,true));
                out.write(arr);
                out.flush();
                install_over = true;
                out.close();
                File final_file = new File("./Download/"+filename);
                file.renameTo(final_file);
                clearLog();
            }
            return -1;
        }catch (InterruptedException e){
            System.out.println("下载已暂停。");
            File file = new File("./Download/"+filename+"tmp");
            try{
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file,true));
                out.write(arr,0,pos);
                out.flush();
                out.close();
            }
            catch (Exception ee){
                ee.printStackTrace();
            }
            pos += origin;
            content_length+=origin;
            writeLog();
        }
        catch (MalformedURLException e) {
            System.out.println("目标URL错误，请检查URL");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法建立连接");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            System.out.println("Downloader 结束了");
            install_over = true;
            download_over = true;
            stopControl();
            return pos;
//            return -1;
        }
    }

    void clearLog(){
        try{
            File log = new File("./history/"+filename+".txt");
            if(log.exists()){
                log.delete();System.out.println("已删除log");
            }

        }
        catch (Exception ee){
            ee.printStackTrace();
        }
    }
    void writeLog(){
        try{
            File file = new File("./history/"+filename+".txt");
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("Url:"+url+"\r\n");
            writer.write("Filename:"+filename+"\r\n");
            writer.write("Pos:"+pos+"\r\n");
            writer.write("Progress:"+(pos*100.0/content_length)+"%\r\n");
            writer.flush();
            writer.close();
        }
        catch (Exception ee){
            ee.printStackTrace();
        }
    }

    @Override
    public Integer call() throws Exception {
        return download(url);
    }
}
class Scheduler implements Runnable{
    Downloader downloader;
    Scheduler(Downloader downloader){
        this.downloader = downloader;
    }
    DoubleProperty progress = new SimpleDoubleProperty(0);
    public void run() {
        try{
            Thread.sleep(2000);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        int []situation = downloader.getPos();
        while (situation[0]!=2){
            if (situation[0]==0){
                double tmp = situation[1]*100.0/situation[2];
                progress.setValue(tmp);
                System.out.println("下载进度:"+tmp+"%");
            }
            try{
                Thread.sleep(1000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            situation = downloader.getPos();
        }
        System.out.println("Scheduler 结束了,如未结束，请输入任意字符");
    }
}
//class Progressor implements Runnable{
//    Downloader downloader;
//    Progressor(Downloader downloader){
//        this.downloader = downloader;
//    }
//    public void run(){
//        while(downloader._control.getValue()){
//            if(scanner.hasNextInt()){
//                int tmp = scanner.nextInt();
//                if(tmp==2){
//                    downloader.stopControl();
//                    break;
//                }
//            }
//        }
//        System.out.println("Progressor 结束了");
//    }
//
//}