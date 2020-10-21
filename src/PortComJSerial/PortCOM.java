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
    public Thread thread = null;
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
                    /*
                    //alerta funciona alerta
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error de conexión");
                        alert.setHeaderText("Error al conectar puerto " + port);
                        alert.setContentText("Error al conectar dispositivo " + tag);
                        alert.showAndWait();
                    });
                     */

                    Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (true) {
                    InputStream istream;
                    try {
                        istream = portCom.getInputStream();
                        // Read some data using a stream
                        byte[] byteBuffer = new byte[1024];
                        int n = istream.read(byteBuffer);
                        if (n != 0) {
                            String hexStr = DatatypeConverter.printHexBinary(byteBuffer);
                            System.out.println("hexStr: " + hexStr);
                            hexStr = hexStr.substring(0, ((n * 2)));
                            //System.out.println(asHexStr);
                            String codigo = Utils.HexToASCII.hexToAscii(hexStr);
                            //codigo = codigo.substring(0, codigo.length() - 1);
                            System.out.println("****** Lectura ******");
                            System.out.println("hex: " + hexStr);
                            System.out.println("Código: " + codigo);
                            System.out.println("port: " + port);
                            System.out.println("tag: " + tag);
                            if (tag == "LECTOR") {
                                conn = new ConexionBaseDeDatosSellado();

                                conn = new ConexionBaseDeDatosSellado();
                                try {
                                    statement = conn.getConnection().createStatement();
                                } catch (SQLException ex) {
                                    System.out.println("Error al crear conexion en portCom statement: " + ex.getMessage());
                                    Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //Consultar codigo de barra en base de datos externa, obtiene caja por el codigo
                                Caja caja = getCajaPorCodigoUnitec(codigo);

                                if (caja != null) {
                                    //obtiene calibrador y lector a traves de lector
                                    ResultSet resultSetGetLectorByPort = Query.getLectorByPort(conn, port);
                                    if (resultSetGetLectorByPort != null) {
                                        //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)                                
                                        ResultSet resultSetUsuariosEnLinea = Query.getRegistroDiarioUsuariosEnLinea(conn, resultSetGetLectorByPort, Date.getDateString());
                                        if (resultSetUsuariosEnLinea != null) {
                                            //envia código leido a base de datos. Crea registro diario de tabla registro_diario_caja_sellada (cuando llega un código de barras tipo DataMatrix)
                                            Query.crearRegistroDiarioCajaSellada(conn, resultSetUsuariosEnLinea, resultSetGetLectorByPort, caja, codigo);
                                        }
                                    }
                                }
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

                                if (resultSetUsuario != null) {

                                    ResultSet getUsuarioEnLinea = Query.getUsuarioEnLinea(conn, resultSetUsuario);

                                    if (getUsuarioEnLinea != null) {
                                        //verificar usuario en linea, actualizar fecha_termino
                                        Query.updateFechaTerminoUsuarioEnLinea(conn, resultSetUsuario);
                                    }

                                    //obtener rfid, linea, y calibrador desde portCOM
                                    ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibradorWherePortCOM(conn, port);

                                    if (resultSetRFID != null) {
                                        //crear usuario en linea
                                        Query.insertUsuarioEnLinea(conn, resultSetUsuario, resultSetRFID);
                                    }
                                }

                                conn.getConnection().close();
                                conn.disconnection();
                            }
                        }
                        Thread.sleep(100);
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
        //Caja caja = Query.getCajaPorCodigo(connUnitec, codigo);
        Caja caja = Query.getCajaPorCodigo(conn, codigo);
        try {
            connUnitec.getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        connUnitec.disconnection();
        return caja;
    }
}
