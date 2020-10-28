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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author crist
 */
public class Sellado extends Application {

    ArrayList<PortCOM> portCOMArray = new ArrayList<>();
    ArrayList<ModbusTCP> modbusTCPArray = new ArrayList<>();
    PortCOM portCOM = null;
    ModbusTCP modbusTCP = null;
    private static String TagLector = "LECTOR";
    private static String TagRFID = "RFID";

    @Override
    public void start(Stage primaryStage) {

        //obtener tiempo maximo de espera de caja 
        int waitingTime = Query.getWaitingTime();

        //obtener lector validador
        ResultSet resultSetLectorValidador = Query.getLectorValidador();
        //crear hilo lector validador
        //crearThreadLectorValidador(resultSetLectorValidador, waitingTime);

        //obtener lectores en linea de tabla "linea" 
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        ResultSet resultSetLectores = Query.getLectoresJoinLineaJoinCalibrador(conn);
        //crear hilo por cada lector        
        crearThreadPorCadaLector(resultSetLectores);
        try {
            conn.getConnection().close();
        } catch (SQLException ex1) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
        }
        conn.disconnection();
        conn = null;

        //obtener RFID de tabla "rfid"
        conn = new ConexionBaseDeDatosSellado();
        ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibrador(conn);
        //crear hilo por cada rfid
        crearThreadPorCadaRFID(resultSetRFID);
        try {
            conn.getConnection().close();
        } catch (SQLException ex1) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
        }
        conn.disconnection();
        conn = null;

        System.out.println("Configuracion inicial realizada satisfactoriamente");

        Image image1 = new Image(getClass().getResourceAsStream("/images/logo.png"));
        ImageView imageView1 = new ImageView(image1);
        imageView1.setX(50);
        imageView1.setY(50);

        StackPane root = new StackPane();
        root.getChildren().add(imageView1);

        Scene scene = new Scene(root, 600, 250);

        primaryStage.setTitle("***Danich*** conexión a periféricos");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnHiding(event -> {
            System.out.println("Closing Stage");
            for (PortCOM portCOM : portCOMArray) {
                if (portCOM != null) {
                    if (portCOM.thread != null) {
                        portCOM.thread.stop();
                    }
                }
            }

            for (ModbusTCP modbusTCP : modbusTCPArray) {
                if (modbusTCP != null) {
                    if (modbusTCP.thread != null) {
                        modbusTCP.thread.stop();
                    }
                }
            }
        });
    }

    private void crearThreadPorCadaRFID(ResultSet resultSetLectores) {
        try {
            while (resultSetLectores.next()) {
                String calibradorId = resultSetLectores.getString("calibrador.id");
                String lineaId = resultSetLectores.getString("linea.id");
                String nombre = resultSetLectores.getString("nombre");
                String port = resultSetLectores.getString("ip");
                String baudRate = resultSetLectores.getString("baudRate");
                String parity = resultSetLectores.getString("parity");
                String stopBits = resultSetLectores.getString("stopBits");
                String dataBits = resultSetLectores.getString("dataBits");
                String timeout = "2000";

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
                portCOMArray.add(portCOM);
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener RFID SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener RFID: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadPorCadaLector(ResultSet resultSetLectores) {
        try {
            while (resultSetLectores.next()) {
                String calibradorId = resultSetLectores.getString("calibrador.id");
                String lineaId = resultSetLectores.getString("linea.id");
                String nombre = resultSetLectores.getString("nombre");
                String port = resultSetLectores.getString("ip");
                String baudRate = resultSetLectores.getString("baudRate");
                String parity = resultSetLectores.getString("parity");
                String stopBits = resultSetLectores.getString("stopBits");
                String dataBits = resultSetLectores.getString("dataBits");
                String timeout = "2000";

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
                portCOMArray.add(portCOM);
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener lector SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener lectores: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadLectorValidador(ResultSet resultSetLector, int waitingTime) {
        try {
            while (resultSetLector.next()) {
                String nombre = resultSetLector.getString("nombre");
                String ip = resultSetLector.getString("ip");
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener lector validador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
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
