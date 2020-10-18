/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PortComJSerial;

import ModbusTCPJamod.ModbusTCP;
import Utils.Date;
import dk.thibaut.serial.SerialChannel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import dk.thibaut.serial.SerialPort;
import dk.thibaut.serial.enums.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.xml.bind.DatatypeConverter;
import baseDeDatos.ConexionBaseDeDatosSellado;
import baseDeDatos.ConexionBaseDeDatosUnitec;
import sellado.Query;
import sellado.Sellado;
import sellado.models.Caja;

/**
 *
 * @author crist
 */
public class PortCOM {

    SerialPort portCom;
    Thread thread = null;
    ConexionBaseDeDatosSellado conn = null;
    Statement statement = null;
    ConexionBaseDeDatosUnitec connUnitec = null;

    public PortCOM(String calibrador, String linea, String tag, String nombre, String port, BaudRate baudRate, Parity parity, StopBits stopBits, DataBits dataBits, String timeout) {

        // Get a new instance of SerialPort by opening a port.
        Runnable runableCom = new Runnable() {
            @Override
            public void run() {
                try {
                    portCom = SerialPort.open(port);
                    portCom.setTimeout(Integer.parseInt(timeout));
                    portCom.setConfig(baudRate, parity, stopBits, dataBits);
                } catch (IOException ex) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error de conexión");
                        alert.setHeaderText("Error al conectar puerto " + port);
                        alert.setContentText("Error al conectar dispositivo " + tag);
                        alert.showAndWait();
                    });

                    Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (true) {
                    InputStream istream;
                    try {
                        istream = portCom.getInputStream();
                        // Read some data using a stream
                        byte[] byteBuffer = new byte[4096];
                        int n = istream.read(byteBuffer);
                        if (n != 0) {
                            String hexStr = DatatypeConverter.printHexBinary(byteBuffer);
                            hexStr = hexStr.substring(0, ((n * 2)));
                            //System.out.println(asHexStr);
                            String codigo = Utils.HexToASCII.convertHexToASCII(hexStr);
                            codigo = codigo.substring(1, codigo.length() - 1);
                            //System.out.println("****** CODIGO LEIDO ******");
                            System.out.println("Código: " + codigo);
                            System.out.println("hacer una cosa");
                            if (tag == "LECTOR") {
                                conn = new ConexionBaseDeDatosSellado();
                                try {
                                    statement = conn.getConnection().createStatement();
                                } catch (SQLException ex) {
                                    System.out.println("Error al crear conexion en portCom statement: " + ex.getMessage());
                                    Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //Consultar codigo de barra en base de datos externa, obtiene caja por el codigo
                                Caja caja = getCajaPorCodigoUnitec(codigo);                                                          

                                //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)                                
                                ResultSet resultSetUsuariosEnLinea = Query.getRegistroDiarioUsuariosEnLinea(conn, port, Date.getDateString());

                                //envia código leido a base de datos. Crea registro diario de tabla registro_diario_caja_sellada (cuando llega un código de barras tipo DataMatrix)
                                Query.crearRegistroDiarioCajaSellada(conn, resultSetUsuariosEnLinea, caja, codigo);

                                conn.getConnection().close();
                                conn.disconnection();

                            } else if (tag == "RFID") {
                                conn = new ConexionBaseDeDatosSellado();
                                try {
                                    statement = conn.getConnection().createStatement();
                                } catch (SQLException ex) {
                                    System.out.println("Error al crear conexion en portCom statement: " + ex.getMessage());
                                    Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //obtener usuario por codigo rfid
                                ResultSet resultSetUsuario = Query.getUsuarioPorRFID(conn, codigo);

                                //obtener rfid, linea, y calibrador desde portCOM
                                ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibradorWherePortCOM(conn, port);

                                //verificar usuario en linea, actualizar fecha_termino
                                Query.updateFechaTerminoUsuarioEnLinea(conn, resultSetUsuario);

                                //crear usuario en linea
                                Query.insertUsuarioEnLinea(conn, resultSetUsuario, resultSetRFID);

                                conn.getConnection().close();
                                conn.disconnection();
                            }
                        }
                        Thread.sleep(500);
                    } catch (IOException ex) {
                        System.out.println("error IOException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        System.out.println("error InterruptedException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println("error SQLException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        thread = new Thread(runableCom);
        thread.start();
    }

    private Caja getCajaPorCodigoUnitec(String codigo) {
        connUnitec = new ConexionBaseDeDatosUnitec();
        Caja caja = Query.getCajaPorCodigo(connUnitec, codigo);
        try {
            connUnitec.getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        connUnitec.disconnection();
        return caja;
    }
}
