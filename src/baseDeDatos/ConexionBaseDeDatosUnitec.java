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
    private String database = "UNITEC_DB";
    private String usuario = "danich";
    private String password = "danich";
    private String conexionUrl = "jdbc:sqlserver://192.168.1.115:1433;"
            + "database=" + database + ";"
            + "user=" + usuario + ";"
            + "password=" + password + ";"
            + "loginTimeout=1";

    Connection conn;
    public String error;

    public ConexionBaseDeDatosUnitec() {
        int i = 1;
        while (i < 1) {
            try {
                //obtiene la conexion
                conn = DriverManager.getConnection(conexionUrl);
                if (conn != null) {
                    System.out.println("Conexion a base de datos UNITEC establecida");
                    break;
                }
            } catch (SQLException ex) {
                this.error = ex.getMessage();
                System.out.println("Error un SQLException al conectar a base de datos UNITEC: " + ex.getMessage());
                Query.insertRegistroDev("Error base de datos unitec", "Error al crear conexion en base de datos Unitec: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                conn = null;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ConexionBaseDeDatosUnitec.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void disconnection() {
        conn = null;
        if (conn == null) {
            System.out.println("Conexion a base de datos UNITEC terminada");
        }
    }
}
