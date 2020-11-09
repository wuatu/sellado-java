/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PortComJSerial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Scanner;
import sellado.Query;
import sellado.Sellado;
import sellado.models.CajaSellado;
import sellado.models.CajaUnitec;

/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 *
 */
public class TwoWaySerialComm {

    int count = 0;
    public CommPort commPort;
    //public Thread thread;
    ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();

    public TwoWaySerialComm() {
        super();
    }

    void connect(String calibrador, String linea, String tag, String nombre, String port, int baudRate, int parity, int stopBits, int dataBits, String timeout) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                /*
                System.out.println("baudrate: "+baudRate);
                System.out.println("dataBits: "+dataBits);
                System.out.println("stopBits: "+stopBits);
                System.out.println("parity: "+parity);
                 */
                serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
                //serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                //OutputStream out = serialPort.getOutputStream();

                //(thread = new Thread(new SerialWriter(out))).start();
                serialPort.addEventListener(new SerialReader(in, tag, port));
                serialPort.notifyOnDataAvailable(true);

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    /**
     * Handles the input coming from the serial port. A new line character is
     * treated as the end of a block in this example.
     */
    public class SerialReader implements SerialPortEventListener {

        private InputStream in;
        private byte[] buffer = new byte[1024];
        String tag;
        String port;
        String codigo;

        public SerialReader(InputStream in, String tag, String port) {
            this.in = in;
            this.tag = tag;
            this.port = port;
        }

        public void serialEvent(SerialPortEvent arg0) {
            int data;

            try {
                int len = 0;
                while ((data = in.read()) > -1) {
                    if (data == '\n') {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                codigo = new String(buffer, 0, len);

                ejecutarAccionLectorRFID(codigo, tag, port);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        public String getCodigo() {
            return this.codigo;
        }

    }

    public void ejecutarAccionLectorRFID(String cod, String tag, String port) {

        String codigo = Utils.HexToASCII.limpiarString(cod);
        for (int i = 0; i < codigo.length(); i++) {
            System.out.println("codigoql: " + codigo.charAt(i));
        }
        //System.out.println("****** Lectura ******");
        //System.out.println("hex: " + hexStr);
        System.out.println("Código: " + codigo);
        //System.out.println("port: " + port);
        //System.out.println("tag: " + tag);

        if (tag == "LECTOR") {
            count++;
            System.out.println("contador de lector: " + count);
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
                            Query.insertRegistroProduccion("err", "Apertura cierre de turno es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
                        }
                    } else {
                        System.out.println("resultSetUsuariosEnLinea es nulo");
                        Query.insertRegistroProduccion("err", "Usuarios en linea es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
                    }
                } else {
                    System.out.println("resultSetGetLectorByPort es nulo");
                    Query.insertRegistroProduccion("err", "Lector Port es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
                }
            } else {
                System.out.println("cajaUnitec es nulo");
                Query.insertRegistroProduccion("err", "Caja UNITEC es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
            }
            /*
                                conn.getConnection().close();
                                conn.disconnection();
                                conn = null;
             */
        } else if (tag == "RFID") {
            //ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
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
                    Query.insertRegistroProduccion("err", "Apertura cierre de turno es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
                }
            } else {
                System.out.println("resultSetUsuario es nulo");
                Query.insertRegistroProduccion("err", "Usuario es nulo", Utils.Date.getDateString(), Utils.Date.getHourString());
            }
            /*
                                conn.getConnection().close();
                                conn.disconnection();
                                conn = null;
             */
        }
    }

    /**
     *
     */
    public static class SerialWriter implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                int c = 0;
                while ((c = System.in.read()) > -1) {
                    this.out.write(c);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
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
        //ConexionBaseDeDatosUnitec connUnitec = new ConexionBaseDeDatosUnitec();
        CajaUnitec caja = Query.getCajaPorCodigoUnitec(conn, codigo);
        /*try {
            if(connUnitec.getConnection()!=null){
                connUnitec.getConnection().close();
            }            
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom", "Error al cerrar conexion en base de datos Unitec: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);            
        }
        connUnitec.disconnection();
        connUnitec = null;
         */
        return caja;
    }
}
