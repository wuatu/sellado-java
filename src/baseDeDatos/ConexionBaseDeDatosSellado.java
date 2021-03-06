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
public class ConexionBaseDeDatosSellado {

    private String database = "danich_sellado";
    private String usuario = "root";
    private String password = "D@nich155";
    private String url = "jdbc:mysql://127.0.0.1:3306/" + database + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    Connection conn = null;
    public String error = null;

    public ConexionBaseDeDatosSellado() {
        int i = 1;
        //reintentos de conexion
        while (i < 5) {
            try {
                try {
                    //obtiene el driver
                    //Class.forName("com.mysql.cj.jdbc.Driver");
                    //obtiene la conexion
                    conn = DriverManager.getConnection(url, usuario, password);
                    if (conn != null) {
                        //System.out.println("Conexion a base de datos 'Sellado' establecida");
                        break;
                    }
                } catch (SQLException ex) {
                    this.error = "Error al intentar conectar a base de datos Sellado: " + ex.getMessage();
                    System.out.println("Ocurre un SQLException al conectar a base de datos 'Sellado': " + ex.getMessage());
                }
                Thread.sleep(2000);
                i++;
            } catch (InterruptedException ex) {
                Logger.getLogger(ConexionBaseDeDatosSellado.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void disconnection() {
        conn = null;
        if (conn == null) {
            //System.out.println("Conexion a base de datos 'Sellado' terminada");
        }
    }

}
