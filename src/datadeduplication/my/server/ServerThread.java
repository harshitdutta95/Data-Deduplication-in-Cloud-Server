/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datadeduplication.my.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Mpaul24
 */
public class ServerThread extends Thread{
    
    ServerSocket ss;
    Socket s1;
    DataInputStream din;
    DataOutputStream dout;
    Server form;
    
    ServerThread(Server form,Socket s1){
        
        this.form = form;
        this.s1 = s1;
        JOptionPane.showMessageDialog(form, "Server Started");
        //ss = new ServerSocket(2400);
        start();
        
    }
    
    @Override
    public void run(){
        
        //s1 = ss.accept();
        openReader();
        
    }

    private void openReader() {
        try {
            din = new DataInputStream(s1.getInputStream());
            dout = new DataOutputStream(s1.getOutputStream());
            if(din.readUTF().equals("signup")){
                System.out.println("hello");
                String name = din.readUTF();
                String email = din.readUTF();
                String password = din.readUTF();
                String gender = din.readUTF();
                Server.db.getstatemet().execute("insert into clients(name,email,password,gender,tablename) values('"+name+"','"+email+"','"+password+"','"+gender+"','"+name+"');");
                Server.db.getstatemet().execute("create table "+name+"(id serial primary key,filetag text,filename text,size text,token int,path text)");

            }else{
                String username = din.readUTF();
                String password = din.readUTF();
                form.username_tf.setText(username);
                ResultSet rs = Server.db.getstatemet().executeQuery("Select * from clients where name='"+username+"' and password='"+password+"';");
                if(rs.next())
                {
                    dout.writeBoolean(true);
                }else{
                    dout.writeBoolean(false);
                }
                
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void recFile() {
        FileRecThread f = new FileRecThread(din, dout);
    }
    
    
    class FileRecThread extends Thread{
        DataInputStream din;
        DataOutputStream dout;
        public FileRecThread(DataInputStream din, DataOutputStream dout) {
            this.din = din;
            this.dout = dout;
            start();
        }
        
        boolean fn = false;boolean x ;
        FileOutputStream fout;
        File f;
        byte[] buffer = new byte[2048];
        int len = 0;
        @Override
        public void run(){
            try {
                System.out.println("Ready to Recieve");
                long size = din.readLong();
                String file_tag = din.readUTF();
                String file_name = din.readUTF();
                Server.db.getstatemet().execute("insert into "+form.username_tf.getText()+"(filetag,filename,size,token,path) values('"+file_tag+"','"+file_name+"',"+size+",0,'"+Server.path_encrypted+"\\"+file_tag+"');");
                Server.db.getstatemet().execute("insert into maintable(filetag,filename,size,noofuser,path) values('"+file_tag+"','"+file_name+"',"+size+",1,'"+Server.path_encrypted+"\\"+file_tag+"');");

                fout = new FileOutputStream(new File(Server.path_encrypted+"\\"+file_tag));
                do{
                    len = din.readInt();
                    Thread.sleep(10);
                    if(len>0 && len<=2048){
                        din.readFully(buffer,0,len);
                        fout.write(buffer,0,len);
                    }
                }while(len==2048);
                len = 0;
                fout.flush();
                fout.close();
                
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
   
    public void response(){
        try {
            boolean valid;
            String username = din.readUTF(); //to check username exists
            ResultSet rs = Server.db.getstatemet().executeQuery("select * from clients where name='"+username+"';");
            if (rs.next())
                valid = true;
            else
                valid=false;
            if(valid){
                boolean exists;
                dout.writeBoolean(valid);
                String file_name = din.readUTF();
                form.filename_tf.setText(file_name);
                String file_tag = din.readUTF();//to check file exixts
                rs=Server.db.getstatemet().executeQuery("select * from "+username+" where filetag='"+file_tag+"';");
                if(rs.next())
                    exists = false;
                else
                    exists =  true;
                if(exists){
                    
                    rs=Server.db.getstatemet().executeQuery("select * from maintable where filetag='"+file_tag+"';");
                    if(rs.next())
                    {
                        String path1="";
                        int filesize1;
                        filesize1 = rs.getInt("size");
                        path1= rs.getString("path");
                        Server.db.getstatemet().executeUpdate("update maintable set noofuser=noofuser+1 where filetag='"+file_tag+"';");
                        Server.db.getstatemet().executeUpdate("insert into "+form.username_tf.getText()+"(filetag,filename,size,token,path) values('"+file_tag+"','"+form.filename_tf.getText()+"','"+filesize1+"',0,'"+path1+"');");
                        JOptionPane.showMessageDialog(form, "Path Added");
                        exists=false;
                        dout.writeBoolean(exists);
                    }else{
                        exists=true;
                        dout.writeBoolean(exists);
                            
                    }
                }else{
                    dout.writeBoolean(exists);
                    JOptionPane.showMessageDialog(form, "File Exists");
                }
            }else{
                dout.writeBoolean(valid);
                JOptionPane.showMessageDialog(form, "Invalid User");
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
