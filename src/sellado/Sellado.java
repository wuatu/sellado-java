/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sellado;

import baseDeDatos.ConexionBaseDeDatosSellado;
import ModbusTCPJamod.ModbusTCP;
import PortComJSerial.PortCOM;
import PortComJSerial.PortCOMSettings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author crist
 */
public class Sellado extends Application {

    PortCOM portCOM = null;
    ModbusTCP modbusTCP = null;
    ConexionBaseDeDatosSellado conn = null;
    private static String TagLector = "LECTOR";
    private static String TagRFID = "RFID";

    @Override
    public void start(Stage primaryStage) {

        conn = new ConexionBaseDeDatosSellado();
        
        try {            
            //obtener tiempo maximo de espera de caja 
            int waitingTime=Query.getWaitingTime(conn);
            
            //obtener lector validador
            ResultSet resultSetLectorValidador = Query.getLectorValidador(conn);
            //crear hilo lector validador
            crearThreadLectorValidador(resultSetLectorValidador,waitingTime);
            
            //obtener lectores en linea de tabla "linea" 
            ResultSet resultSetLectores = Query.getLectoresJoinLineaJoinCalibrador(conn);
            //crear hilo por cada lector
            crearThreadPorCadaLector(resultSetLectores);

            //obtener RFID de tabla "rfid"
            ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibrador(conn);
            //crear hilo por cada rfid
            crearThreadPorCadaRFID(resultSetRFID);                           
            
            conn.getConnection().close();
            conn.disconnection();
   
        } catch (SQLException ex) {
            System.out.println("Error tipo SQLException: " + ex.getMessage());
        }
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void crearThreadPorCadaRFID(ResultSet resultSetLectores) {
        try {
            while (resultSetLectores.next()) {
                String calibradorId = resultSetLectores.getString("calibrador.id");
                String lineaId = resultSetLectores.getString("linea.id");
                String nombre = resultSetLectores.getString("nombre");
                String port = resultSetLectores.getString("port");
                String baudRate = resultSetLectores.getString("baudRate");
                String parity = resultSetLectores.getString("parity");
                String stopBits = resultSetLectores.getString("stopBits");
                String dataBits = resultSetLectores.getString("dataBits");
                String timeout = resultSetLectores.getString("timeout");

                //creación de hilo RFID de códigos
                portCOM = new PortCOM(
                        calibradorId,
                        lineaId,
                        TagRFID,
                        nombre,
                        port,
                        PortCOMSettings.baudRate(baudRate),
                        PortCOMSettings.parity(parity),
                        PortCOMSettings.stopBits(stopBits),
                        PortCOMSettings.dataBits(dataBits),
                        timeout);
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener RFID: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadPorCadaLector(ResultSet resultSetLectores) {
        try {
            while (resultSetLectores.next()) {
                String calibradorId = resultSetLectores.getString("calibrador.id");
                String lineaId = resultSetLectores.getString("linea.id");
                String nombre = resultSetLectores.getString("LECTOR");
                String port = resultSetLectores.getString("port");
                String baudRate = resultSetLectores.getString("baudRate");
                String parity = resultSetLectores.getString("parity");
                String stopBits = resultSetLectores.getString("stopBits");
                String dataBits = resultSetLectores.getString("dataBits");
                String timeout = resultSetLectores.getString("timeout");

                //creación de hilo lector de códigos
                portCOM = new PortCOM(
                        calibradorId,
                        lineaId,
                        TagLector,
                        nombre,
                        port,
                        PortCOMSettings.baudRate(baudRate),
                        PortCOMSettings.parity(parity),
                        PortCOMSettings.stopBits(stopBits),
                        PortCOMSettings.dataBits(dataBits),
                        timeout);
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener lectores: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void crearThreadLectorValidador(ResultSet resultSetLector, int waitingTime){
        try {
            while (resultSetLector.next()) {
                String nombre = resultSetLector.getString("nombre");
                String ip = resultSetLector.getString("ip");
                
                //creación de hilo lector validador
                modbusTCP=new ModbusTCP(nombre, ip, waitingTime);
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener lector validador: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
