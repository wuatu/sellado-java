/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModbusTCPJamod;

import Utils.Date;
import Utils.HexToASCII;
import baseDeDatos.ConexionBaseDeDatosSellado;
import baseDeDatos.ConexionBaseDeDatosUnitec;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.net.TCPMasterConnection;
import sellado.Query;

/**
 *
 * @author crist
 */
public class ModbusTCP {

    TCPMasterConnection tcpMasterConnection = null; //the connection
    public Thread thread = null;
    public String error = null;
    int count = 0;

    public ModbusTCP(String nombre, String ip, int waitingTime, int inicioDireccionDeMemoriaLectura, int calibradorId) {
        try {

            /*
            int contadorDeLecturasRealizadas=7002;
            int seLeyoUnCodigo=7003; 0 si no se leyo codigo, 1 en caso contrario
            int cantidadDeRegistrosALeer=7004;            
            int codigoRegistroInicial=7005;
             */
            int contadorDeLecturasRealizadas = inicioDireccionDeMemoriaLectura; //7002
            int seLeyoUnCodigo = inicioDireccionDeMemoriaLectura + 1; //7003
            int totalDeRegistrosALeer = inicioDireccionDeMemoriaLectura + 2; //7004  
            int codigoRegistroInicial = inicioDireccionDeMemoriaLectura + 3; //7005

            InetAddress addr = null;
            int port = Modbus.DEFAULT_PORT;

            addr = InetAddress.getByName(ip);

            //System.out.println("asdsadasda" + addr);
            //2. Open the connection            
            tcpMasterConnection = new TCPMasterConnection(addr);
            tcpMasterConnection.setTimeout(500);
            tcpMasterConnection.setPort(port);
            tcpMasterConnection.connect();

            //ModbusTCPListener listener = new ModbusTCPListener(3);
            //listener.setPort(port);
            //listener.
            //listener.start();
            if (tcpMasterConnection.isConnected()) {
                System.out.println("Sensor validador conectado");
            } else {
                System.out.println("Sensor validador NO conected");
            }

            Runnable runable = new Runnable() {
                @Override
                public void run() {
                    int secondCodeReader = 0; //empieza a ecribir de la segunda iteración cuando i==1;
                    int cantidadDeRegistrosALeer = 0; //cantidad de registros a leer
                    boolean isLeido = false; //confirma si es un nuevo codigo
                    int numeroLecturaNuevo = 0;
                    int numeroLecturaAnterior = 0;
                    do {
                        // obtiene cantidad de registros a leer 
                        ReadMultipleRegistersRequest cantidadDeRegistrosALeerRequest = new ReadMultipleRegistersRequest(totalDeRegistrosALeer, 1); // obtiene cantidad de registros a leer 7004
                        ModbusTCPTransaction trans = new ModbusTCPTransaction(tcpMasterConnection);
                        trans.setRequest(cantidadDeRegistrosALeerRequest);
                        try {
                            trans.execute();
                        } catch (ModbusSlaveException ex) {
                            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Error ModbusSlaveException: " + ex);
                        } catch (ModbusException ex) {
                            Logger.getLogger(ModbusTCP.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Error ModbusException: " + ex);
                            /*Query.insertRegistroDev("Error ModbusTCP", "Error conexion sensor validador ModbusException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                            System.out.println("Alerta 6");
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Alerta 6");
                            alert.setHeaderText("Error de conexión sensor validador");
                            alert.setContentText("Sensor desconectado: " + ex.getMessage());
                            alert.showAndWait();
                            System.exit(1);
                             */
                        }

                        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
                        cantidadDeRegistrosALeer = res.getRegisters()[0].getValue();

                        //si cantidad de registros a leer es impar se debe sumar 1 para que quede par
                        if (cantidadDeRegistrosALeer % 2 != 0) {
                            cantidadDeRegistrosALeer = cantidadDeRegistrosALeer + 1;
                        }
                        // obtiene numero de lectura de registro a leer ref 7002
                        numeroLecturaNuevo = obtieneRegistros(contadorDeLecturasRealizadas, 1);

                        //obtiene registro que confirma si se leyo un codigo 7003
                        if (obtieneRegistros(seLeyoUnCodigo, 1) == 1) {
                            isLeido = true;
                        } else {
                            isLeido = false;
                        }

                        //System.out.println("numero lectura: " + numeroLecturaNuevo);
                        //System.out.println("cantidad de registros a leer: " + cantidadDeRegistrosALeer);
                        //System.out.println("Reading...");
                        if (numeroLecturaNuevo != numeroLecturaAnterior && isLeido == true) {
                            //obtiene codigo leido
                            System.out.println("");
                            System.out.println("*** Lector validador ***");
                            count++;
                            System.out.println("Contador de lecturas sensor validador: " + count);
                            numeroLecturaAnterior = numeroLecturaNuevo;
                            if (secondCodeReader == 1) {
                                ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(codigoRegistroInicial, cantidadDeRegistrosALeer); //desde donde comienza el registro del codigo 7005
                                trans.setRequest(req);
                                try {
                                    trans.execute();
                                    res = (ReadMultipleRegistersResponse) trans.getResponse();
                                } catch (ModbusSlaveException ex) {
                                    Query.insertRegistroDev("Error ModbusTCP", "Error conexion sensor validador ModbusSlaveException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                                } catch (ModbusException ex) {
                                    Query.insertRegistroDev("Error ModbusTCP", "Error conexion sensor validador ModbusException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
                                }

                                String hex = "";
                                for (int i = 0; i < res.getRegisters().length; i++) {
                                    hex = hex.concat(Integer.toHexString(res.getRegisters()[i].getValue()));
                                    //System.out.println(res.getRegisters()[i].getValue());
                                }

                                if (!hex.equalsIgnoreCase("")) {
                                    String codigo = Utils.HexToASCII.convertHexToASCII(hex);
                                    codigo = Utils.HexToASCII.limpiarString(codigo);                       
                                    //System.out.println("****** CODIGO LEIDO ******");
                                    System.out.println("Código lector validador: " + codigo);

                                    //inserta codigo en tabla lectorValidadpr_en_calibrador
                                    Query.insertLectorValidadorEnCalibrador(calibradorId, codigo, Date.getDateString(), Date.getHourString());

                                    //Verificar a traves de lector verificador, updatear valores is_verificado, is_before_time tabla registro_diario_caja_sellada
                                    Query.updateRegistroDiarioCajaCerradaCodigo(codigo, waitingTime);
                                } else {
                                    System.out.println("Lectura no valida o vacía");
                                }
                            }

                        }
                        secondCodeReader = 1;
                    } while (true);
                }
            };

            thread = new Thread(runable);
            thread.start();
            this.error = "Conectado correctamente lector validador ip: " + ip;

        } catch (Exception ex) {
            this.error = "Error conexion lector validador ip: " + ip + ", " + ex.getMessage();
            System.out.println("Error ModbusTCP" + "Error conexion sensor validador Exception: " + ex.getMessage());
            Query.insertRegistroDev("Error ModbusTCP", "Error conexion sensor validador Exception: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
        }
    }

    // obtiene cantidad de registros a leer     
    public int obtieneRegistros(int ref, int count) {
        ReadMultipleRegistersRequest cantidadDeRegistrosALeerRequest = new ReadMultipleRegistersRequest(ref, count); // obtiene cantidad de registros a leer                    
        ModbusTCPTransaction trans = new ModbusTCPTransaction(tcpMasterConnection);
        trans.setRequest(cantidadDeRegistrosALeerRequest);
        try {
            trans.execute();
        } catch (ModbusException ex) {
            Query.insertRegistroDev("Error ModbusTCP", "Error conexion sensor validador ModbusException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
        }
        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
        return res.getRegisters()[0].getValue();
    }

}
