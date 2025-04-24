package banking.managment;


import java.sql.*;

public class Connn {
    Connection connection;
    Statement statement;
    public Connn(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project","root","joyalam@1430");
            statement = connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}