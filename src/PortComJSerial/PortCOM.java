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
import sellado.models.CajaSellado;
import sellado.models.CajaUnitec;

/**
 *
 * @author crist
 */
public class PortCOM {

    SerialPort portCom;
    public Thread thread = null;

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
                    //inserta registro dev
                    Query.insertRegistroDev("Error serial port", "Error al conectar puerto: " + port + ", dispositivo: " + tag, Utils.Date.getDateString(), Utils.Date.getHourString());
                    //alerta funciona alerta
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
                                ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
                                //Consultar codigo de barra en base de datos externa, obtiene caja por el codigo
                                CajaUnitec cajaUnitec = getCajaPorCodigoUnitec(conn, codigo);

                                if (cajaUnitec != null) {
                                    //obtiene calibrador y lector a traves de lector
                                    ResultSet resultSetGetLectorByPort = Query.getLectorByPort(conn, port);
                                    if (resultSetGetLectorByPort != null) {
                                        //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)                                
                                        ResultSet resultSetUsuariosEnLinea = Query.getRegistroDiarioUsuariosEnLinea(conn, resultSetGetLectorByPort, Date.getDateString());
                                        if (resultSetUsuariosEnLinea != null) {
                                            //busca caja en base de datos sellado para obtener ponderación de caja
                                            CajaSellado cajaSellado = Query.getCajaPorCodigoSellado(conn, cajaUnitec.getEnvase(), cajaUnitec.getCategoria(), cajaUnitec.getCalibre());

                                            //obtener id de apertura_cierre_de_turno
                                            ResultSet resultSetAperturaCierreDeTurno = Query.getAperturaCierreDeTurno(conn);
                                            if (resultSetAperturaCierreDeTurno != null) {
                                                //envia código leido a base de datos. Crea registro diario de tabla registro_diario_caja_sellada (cuando llega un código de barras tipo DataMatrix)
                                                Query.insertRegistroDiarioCajaSellada(conn, resultSetUsuariosEnLinea, resultSetGetLectorByPort, resultSetAperturaCierreDeTurno, cajaSellado, codigo);
                                            } else {
                                                System.out.println("resultSetAperturaCierreDeTurno es nulo");
                                            }
                                        } else {
                                            System.out.println("resultSetUsuariosEnLinea es nulo");
                                        }
                                    } else {
                                        System.out.println("resultSetGetLectorByPort es nulo");
                                    }
                                } else {
                                    System.out.println("cajaUnitec es nulo");
                                }
                                conn.getConnection().close();
                                conn.disconnection();
                                conn = null;
                            } else if (tag == "RFID") {
                                ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
                                //obtener usuario por codigo rfid
                                ResultSet resultSetUsuario = Query.getUsuarioPorRFID(conn, codigo);
                                if (resultSetUsuario != null) {
                                    //obtener id de apertura_cierre_de_turno
                                    ResultSet resultSetAperturaCierreDeTurno = Query.getAperturaCierreDeTurno(conn);
                                    if (resultSetAperturaCierreDeTurno != null) {
                                        ResultSet resultSetUsuarioEnLineaPorFecha = Query.getUsuarioEnLineaPorFecha(conn, resultSetUsuario);
                                        if (resultSetUsuarioEnLineaPorFecha != null) {
                                            //verificar si usuario en linea se esta registrando nuevamente en misma linea para restringir registro
                                            if (!Query.isUsuarioEnLineaEnMismaLinea(conn, resultSetUsuarioEnLineaPorFecha, port)) {
                                                //actualizar fecha_termino
                                                Query.updateFechaTerminoUsuarioEnLinea(conn, resultSetUsuario);
                                                insertarUsuarioEnLinea(conn, resultSetUsuario, resultSetAperturaCierreDeTurno, port);
                                            } else {
                                                System.out.println("isUsuarioEnLineaEnMismaLinea usuario ya esta asignado a esta línea");
                                            }
                                        } else {
                                            System.out.println("getUsuarioEnLinea es nulo por tanto se debe agregar usuario a linea");
                                            insertarUsuarioEnLinea(conn, resultSetUsuario, resultSetAperturaCierreDeTurno, port);
                                        }
                                    } else {
                                        System.out.println("resultSetAperturaCierreDeTurno es nulo");
                                    }
                                } else {
                                    System.out.println("resultSetUsuario es nulo");
                                }
                                conn.getConnection().close();
                                conn.disconnection();
                                conn = null;
                            }
                        }
                        Thread.sleep(100);
                    } catch (IOException ex) {
                        Query.insertRegistroDev("Error PortCom", "Error IOException: " + ex.getMessage() + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                        System.out.println("error IOException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Query.insertRegistroDev("Error PortCom", "Error InterruptedException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                        System.out.println("error InterruptedException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        Query.insertRegistroDev("Error PortCom", "Error SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                        System.out.println("error SQLException portCom: " + ex.getMessage());
                        Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        thread = new Thread(runableCom);

        thread.start();
    }

    public void insertarUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario, ResultSet resultSetAperturaCierreDeTurno, String port) {
        //obtener rfid, linea, y calibrador desde portCOM
        ResultSet resultSetRFID = Query.getRFIDJoinLineaJoinCalibradorWherePortCOM(conn, port);

        if (resultSetRFID != null) {
            //crear usuario en linea
            Query.insertUsuarioEnLinea(conn, resultSetUsuario, resultSetRFID, resultSetAperturaCierreDeTurno);
        } else {
            System.out.println("resultSetRFID es nulo");
        }
    }

    private CajaUnitec getCajaPorCodigoUnitec(ConexionBaseDeDatosSellado conn, String codigo) {
        ConexionBaseDeDatosUnitec connUnitec = new ConexionBaseDeDatosUnitec();
        CajaUnitec caja = Query.getCajaPorCodigoUnitec(conn, codigo);
        try {
            connUnitec.getConnection().close();
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom", "Error al cerrar conexion en base de datos Unitec: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        connUnitec.disconnection();
        connUnitec = null;
        return caja;
    }
}
