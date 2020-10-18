/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author crist
 */
public class ConexionBaseDeDatosUnitec {
    private String database="UNITEC_DB";
    private String usuario="danich";
    private String password="danich";
    private String url="jdbc:mysql://192.168.1.115:3306/"+database+"?useUnicode=true&use"
            +"HDBCCompilantTimezoneShift=true&useLegacyDatetimeCode=false&"
            +"serverTimeZone=UTC";
    
    Connection conn=null;
    
    public ConexionBaseDeDatosUnitec(){
        try {
            //obtiene el driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //obtiene la conexion
            conn = DriverManager.getConnection(url,usuario,password);
            if(conn!=null){
                System.out.println("Conexion a base de datos UNITEC establecida");
            }
        } catch (ClassNotFoundException e){
            System.out.println("Error ClassNotFoundException al conectar a base de datos UNITEC: "+e.getMessage());
        } catch (SQLException ex) {
            System.out.println("Error un SQLException al conectar a base de datos UNITEC: "+ex.getMessage());
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
