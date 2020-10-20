/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author crist
 */
public class ConexionBaseDeDatosSellado {
    private String database="danich_sellado";
    private String usuario="root";
    private String password="";
    private String url="jdbc:mysql://127.0.0.1:3306/"+database+"?useUnicode=true&use"
            +"HDBCCompilantTimezoneShift=true&useLegacyDatetimeCode=false&"
            +"serverTimeZone=UTC";
    
    Connection conn=null;
    
    public ConexionBaseDeDatosSellado(){
        try {
            //obtiene el driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //obtiene la conexion
            conn = DriverManager.getConnection(url,usuario,password);
            if(conn!=null){
                System.out.println("Conexion a base de datos 'Sellado' establecida");
            }
        } catch (ClassNotFoundException e){
            System.out.println("Error ClassNotFoundException al conectar a base de datos 'Sellado': "+e.getMessage());
        } catch (SQLException ex) {
            System.out.println("Ocurre un SQLException al conectar a base de datos 'Sellado': "+ex.getMessage());
        }
    }
    
    public Connection getConnection(){
        return conn;
    }
    
    public void disconnection(){
        conn=null;
        if(conn==null){
            System.out.println("Conexion a base de datos 'Sellado' terminada");
        }
    }
    
}
