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
 *
 * @author crist
 */
public class PortCOM {

    public TwoWaySerialComm twoWaySerialComm;

    public PortCOM(String calibrador, String linea, String tag, String nombre, String port, int baudRate, int parity, int stopBits, int dataBits, String timeout) throws Exception {

// Get a new instance of SerialPort by opening a port.
/*
        System.out.println("twoWaySerialComm lector baudrate: " + baudRate);
        System.out.println("twoWaySerialComm lector dataBits: " + dataBits);
        System.out.println("twoWaySerialComm lector stopBits: " + stopBits);
        System.out.println("twoWaySerialComm lector parity: " + parity);
*/

        twoWaySerialComm = new TwoWaySerialComm();
        twoWaySerialComm.connect(calibrador, linea, tag, nombre, port, baudRate, parity, stopBits, dataBits, timeout);
    }
}
