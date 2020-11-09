/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import sellado.Query;

/**
 *
 * @author crist
 */
public class ConexionBaseDeDatosUnitec {
    /*
    private String url="jdbc:sqlserver://192.168.1.115:1433/databaseName=DB_Name;integratedSecurity=true;";
    private String url="jdbc:sqlserver://192.168.1.115:1433;database=DB_Name;integratedSecurity=true;";
    */
    
    private String database="UNITEC_DB";
    private String usuario="danich";
    private String password="danich";
    private String conexionUrl="jdbc:sqlserver://192.168.1.115:1433;"
            + "database="+database+";"
            + "user="+usuario+";"
            + "password="+password+";"
            + "loginTimeout=1";
    
    Connection conn;
    
    public ConexionBaseDeDatosUnitec(){
        try {
            //obtiene la conexion
            conn = DriverManager.getConnection(conexionUrl);
            if(conn!=null){
                System.out.println("Conexion a base de datos UNITEC establecida");
            }        
        } catch (SQLException ex) {
            System.out.println("Error un SQLException al conectar a base de datos UNITEC: "+ex.getMessage());
            Query.insertRegistroDev("Error base de datos unitec", "Error al crear conexion en base de datos Unitec: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            conn=null;
        }
    }
    
    public Connection getConnection(){
        return conn;
    }
    
    public void disconnection(){
        conn=null;
        if(conn==null){
            System.out.println("Conexion a base de datos UNITEC terminada");
        }
    }
}
