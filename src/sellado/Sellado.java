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
import baseDeDatos.ConexionBaseDeDatosUnitec;
import gnu.io.SerialPort;
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
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private static String TagRFIDSalida = "RFID_SALIDA";
    private static String TagRFIDRegistroColaborador = "RFID_REGISTRO_COLABORADOR";
    private TextArea textArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {

        Image image1 = new Image(getClass().getResourceAsStream("/images/logo.png"));
        ImageView imageView1 = new ImageView(image1);
        imageView1.setX(50);
        imageView1.setY(50);
        textArea.setEditable(false);
        textArea.setPrefHeight(300);
        textArea.setScrollTop(Double.MAX_VALUE);
        textArea.setStyle("-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00;");
        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(imageView1, textArea);

        StackPane root = new StackPane();
        root.getChildren().add(vBox);

        Scene scene = new Scene(root, 600, 500);

        primaryStage.setTitle("***Danich*** conexión a dispositivos");
        primaryStage.setScene(scene);
        primaryStage.show();

        //ConexionBaseDeDatosUnitec connUnitec = new ConexionBaseDeDatosUnitec();
        //Query a=new Query();
        //a.getCajaPorCodigoUnitec(connUnitec, "20000712");
        //this.textArea.setText(a.texto);
        Sistema sistema = new Sistema();
        sistema.start();

        primaryStage.setOnHiding(event -> {
            System.out.println("Closing Stage");

            if (sistema != null) {
                sistema.stop();
            }

            for (PortCOM portCOM : portCOMArray) {
                if (portCOM != null) {
                    if (portCOM.twoWaySerialComm.commPort != null) {
                        portCOM.twoWaySerialComm.commPort.close();
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

    public class Sistema extends Thread {

        public void run() {

            textArea.setText("Iniciando sistema, favor espere...");

            ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();

            if (conn.error != null) {
                textArea.setText(textArea.getText() + "\n" + conn.error);
                this.stop();
            }

            //obtener lector validador        
            ResultSet resultSetLectorValidador = Query.getLectorValidador(conn);

            if (resultSetLectorValidador != null) {
                try {
                    resultSetLectorValidador.beforeFirst();
                } catch (SQLException ex) {
                    Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
                }
                textArea.setText(textArea.getText() + "\n" + "- Iniciando lector validador...");
                //crear hilo lector validador                
                crearThreadLectorValidador(conn, resultSetLectorValidador);
                try {
                    conn.getConnection().close();
                } catch (SQLException ex1) {
                    System.out.println(Query.class.getName());
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
                }
                conn.disconnection();
                conn = null;
            }

            //obtener lectores en linea de tabla "linea" 
            conn = new ConexionBaseDeDatosSellado();
            ResultSet resultSetLectores = Query.getLectoresJoinLineaJoinCalibrador(conn);

            if (resultSetLectores != null) {
                textArea.setText(textArea.getText() + "\n" + "- Iniciando lectores de línea...");
                //crear hilo por cada lector        
                crearThreadPorCadaLector(resultSetLectores);
                try {
                    conn.getConnection().close();
                } catch (SQLException ex1) {
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
                }
                conn.disconnection();
                conn = null;
            }

            //obtener RFID de tabla "rfid"            
            conn = new ConexionBaseDeDatosSellado();
            ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibrador(conn);
            if (resultSetRFID != null) {
                textArea.setText(textArea.getText() + "\n" + "- Iniciando RFID de líneas...");
                //crear hilo por cada rfid
                crearThreadPorCadaRFID(resultSetRFID);
                try {
                    conn.getConnection().close();
                } catch (SQLException ex1) {
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
                }
                conn.disconnection();
                conn = null;
            }

            //obtener RFID de tabla "rfid_salida"
            conn = new ConexionBaseDeDatosSellado();
            ResultSet resultSetRfidSalida = Query.getRfidSalidaJoinCalibrador(conn);
            if (resultSetRfidSalida != null) {
                try {
                    resultSetRfidSalida.beforeFirst();
                } catch (SQLException ex) {
                    Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
                }
                textArea.setText(textArea.getText() + "\n" + "- Iniciando RFID salida calibrador...");
                //crear hilo por cada rfid
                crearThreadPorCadaRfidSalida(resultSetRfidSalida);
                try {
                    conn.getConnection().close();
                } catch (SQLException ex1) {
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
                }
                conn.disconnection();
                conn = null;
            }

            //obtener RFID de tabla "rfid_registro_colaborador"
            conn = new ConexionBaseDeDatosSellado();
            ResultSet resultSetRfidRegistroColaborador = Query.getRfidRegistroColaborador(conn);

            if (resultSetRfidRegistroColaborador != null) {
                try {
                    resultSetRfidRegistroColaborador.beforeFirst();
                } catch (SQLException ex) {
                    Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
                }
                textArea.setText(textArea.getText() + "\n" + "- Iniciando RFID de registro de colaboradores...");
                //crear hilo por cada rfid
                crearThreadRfidRegistroColaborador(resultSetRfidRegistroColaborador);
                try {
                    conn.getConnection().close();
                } catch (SQLException ex1) {
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
                }
                conn.disconnection();
                conn = null;
            }

            System.out.println("Configuracion inicial realizada satisfactoriamente");
            textArea.setText(textArea.getText() + "\n");
            textArea.setText(textArea.getText() + "\n" + "****************************************************");
            textArea.setText(textArea.getText() + "\n" + "Configuración inicial finalizada");
            textArea.setText(textArea.getText() + "\n" + "****************************************************");
            this.stop();
        }
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

                if (portCOM.twoWaySerialComm.connUnitec.error != null) {
                    this.textArea.setText(this.textArea.getText() + "\n" + portCOM.twoWaySerialComm.connUnitec.error + " RFID, puerto: " + port);
                }
                llenarLog();
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener RFID SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener RFID: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
                if (portCOM.twoWaySerialComm.connUnitec.error != null) {
                    this.textArea.setText(this.textArea.getText() + "\n" + portCOM.twoWaySerialComm.connUnitec.error + " Lector, puerto: " + port);
                }
                llenarLog();
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener lector SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener lectores: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadPorCadaRfidSalida(ResultSet resultSetLectores) {
        try {
            while (resultSetLectores.next()) {
                String calibradorId = resultSetLectores.getString("calibrador.id");
                String nombre = resultSetLectores.getString("nombre");
                String port = resultSetLectores.getString("ip");
                String baudRate = resultSetLectores.getString("baudRate");
                String parity = resultSetLectores.getString("parity");
                String stopBits = resultSetLectores.getString("stopBits");
                String dataBits = resultSetLectores.getString("dataBits");
                String timeout = "2000";

                portCOM = new PortCOM(
                        calibradorId,
                        TagRFIDSalida,
                        nombre,
                        port,
                        PortCOMSettings.baudRate(baudRate),
                        PortCOMSettings.parity(parity),
                        PortCOMSettings.stopBits(stopBits),
                        PortCOMSettings.dataBits(dataBits),
                        timeout);
                portCOMArray.add(portCOM);
                if (portCOM.twoWaySerialComm.connUnitec.error != null) {
                    this.textArea.setText(this.textArea.getText() + "\n" + portCOM.twoWaySerialComm.connUnitec.error + " RFID Salida, puerto: " + port);
                }
                llenarLog();

            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener RFID SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener RFID: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadRfidRegistroColaborador(ResultSet resultSetRfidRegistroColaborador) {
        try {
            while (resultSetRfidRegistroColaborador.next()) {
                //String calibradorId = resultSetRfidRegistroColaborador.getString("calibrador.id");
                String nombre = resultSetRfidRegistroColaborador.getString("nombre");
                String port = resultSetRfidRegistroColaborador.getString("ip");
                String baudRate = resultSetRfidRegistroColaborador.getString("baudRate");
                String parity = resultSetRfidRegistroColaborador.getString("parity");
                String stopBits = resultSetRfidRegistroColaborador.getString("stopBits");
                String dataBits = resultSetRfidRegistroColaborador.getString("dataBits");
                String timeout = "2000";

                portCOM = new PortCOM(
                        TagRFIDRegistroColaborador,
                        nombre,
                        port,
                        PortCOMSettings.baudRate(baudRate),
                        PortCOMSettings.parity(parity),
                        PortCOMSettings.stopBits(stopBits),
                        PortCOMSettings.dataBits(dataBits),
                        timeout);
                portCOMArray.add(portCOM);
                if (portCOM.twoWaySerialComm.connUnitec.error != null) {
                    this.textArea.setText(this.textArea.getText() + "\n" + portCOM.twoWaySerialComm.connUnitec.error + " RFID Salida, puerto: " + port);
                }
                llenarLog();

            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener RFID SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener RFID: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearThreadLectorValidador(ConexionBaseDeDatosSellado conn, ResultSet resultSetLector) {
        try {
            while (resultSetLector.next()) {
                String nombre = resultSetLector.getString("nombre");
                String ip = resultSetLector.getString("ip");
                int waitingTime = resultSetLector.getInt("max_wait_time");
                int registroInicial = resultSetLector.getInt("registro_inicial_lectura");
                int calibrador = resultSetLector.getInt("fk_calibrador");
                System.out.println("registro inicial: " + registroInicial);
                //creación de hilo lector validador
                modbusTCP = new ModbusTCP(nombre, ip, waitingTime, registroInicial, calibrador);
                modbusTCPArray.add(modbusTCP);
                if (modbusTCP.error != null) {
                    this.textArea.setText(this.textArea.getText() + "\n" + modbusTCP.error);
                }
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error Sellado", "Error al obtener lector validador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error al obtener lector validador: " + ex.getMessage());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void llenarLog() {
        for (String error : portCOM.twoWaySerialComm.erroresString) {
            this.textArea.setText(this.textArea.getText() + "\n" + error);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
