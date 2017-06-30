
package my.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Mpaul24
 */
public class Database {
    Connection c;
    Statement mystate;
    public Database(){
        try {
            Class.forName("org.postgresql.Driver");
            c=DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","harshitdutta");
            mystate =  c.createStatement();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Statement getstatemet(){
        return mystate;
    }
    
     public void connectNewUser(String username1,String password1){
        try{
            System.out.println("Found"+username1+password1);
            
            ResultSet rs2 = mystate.executeQuery("Select * from login where username='"+username1+"';");
            
            if(!rs2.next()){// now rs points to the first row
                mystate.executeUpdate("insert into login(username,password,usertable) values('"+username1+"','"+password1+"','"+username1+"');");
                mystate.execute("create table "+username1+"(id serial primary key, filename text not null, tokens int);");
            }else{
                System.out.println("Database exists "+rs2.getString("username"));
            }
         /**       String table_name = rs.getString("usertable");//getting the tablename from the query
                rs = mystate.executeQuery("SELECT to_regclass('"+table_name+"');");//check if table for the username already exists
               
                while(rs.next()){
                    value = rs.getString(1);//if there is no table for the username value will be equal to null
                    break;
                }
                if(value==null){//table doesnt exists
                    mystate.execute("create table "+table_name+" (id serial primary key not null,"
                    + "file_name text not null,"
                    + "tokens int not null)");
                }
                //table is created or it exists
                System.out.println("Table "+table_name+" created!!!");
                
                mystate.executeUpdate("insert into "+table_name+"(file_name,tokens) values('sdasdasd',7)");
                rs=mystate.executeQuery("select * from "+table_name+" order by id asc");
                while(rs.next()){
                    System.out.println(rs.getInt("id")+","+rs.getString("file_name")+","+rs.getString("tokens"));
                }
                
            }else{
                System.out.println("Username do not exist!!!");
            }
//            ResultSet myresult=mystate.executeQuery("select * from newtable order by id asc");
//            while(myresult.next()){
//                System.out.println(myresult.getInt("id")+","+myresult.getString("name")+","+myresult.getString("address")+","+myresult.getString("salary"));
//            }
            */
            }catch(SQLException e){
                e.printStackTrace();
            } 
     }
}
