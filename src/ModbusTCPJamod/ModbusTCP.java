/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModbusTCPJamod;

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
import net.wimpi.modbus.net.TCPMasterConnection;
import sellado.Query;

/**
 *
 * @author crist
 */
public class ModbusTCP {

    ModbusTCPTransaction trans = null;
    TCPMasterConnection tcpMasterConnection = null; //the connection
    public Thread thread = null;    

    public ModbusTCP(String nombre, String ip, int waitingTime) {
        try {
            InetAddress addr = null;
            int port = Modbus.DEFAULT_PORT;

            addr = InetAddress.getByName(ip);

            System.out.println(addr);

            //2. Open the connection
            tcpMasterConnection = new TCPMasterConnection(addr);
            tcpMasterConnection.setPort(port);
            tcpMasterConnection.connect();

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
                        //ref = 7004
                        //registroInicial=7004
                        ReadMultipleRegistersRequest cantidadDeRegistrosALeerRequest = new ReadMultipleRegistersRequest(7004, 1); // obtiene cantidad de registros a leer                    
                        trans = new ModbusTCPTransaction(tcpMasterConnection);
                        trans.setRequest(cantidadDeRegistrosALeerRequest);
                        try {
                            trans.execute();
                        } catch (ModbusException ex) {
                            System.out.println("Alerta 6");
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Alerta 6");
                            alert.setHeaderText("Error de conexión sensor validador");
                            alert.setContentText("Sensor desconectado: " + ex.getMessage());
                            alert.showAndWait();
                            System.exit(1);
                        }
                        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
                        cantidadDeRegistrosALeer = res.getRegisters()[0].getValue();

                        //si cantidad de registros a leer es impar se debe sumar 1 para que quede par
                        if (cantidadDeRegistrosALeer % 2 != 0) {
                            cantidadDeRegistrosALeer = cantidadDeRegistrosALeer + 1;
                        }
                        // obtiene numero de lectura de registro a leer ref 7002
                        numeroLecturaNuevo = obtieneRegistros(7002, 1);

                        //obtiene registro que confirma si se leyo un codigo
                        if (obtieneRegistros(7003, 1) == 1) {
                            isLeido = true;
                        } else {
                            isLeido = false;
                        }

                        //System.out.println("numero lectura: " + numeroLecturaNuevo);
                        //System.out.println("cantidad de registros a leer: " + cantidadDeRegistrosALeer);
                        //System.out.println("Reading...");
                        if (numeroLecturaNuevo != numeroLecturaAnterior && isLeido == true) {
                            //obtiene codigo leido
                            numeroLecturaAnterior = numeroLecturaNuevo;
                            if (secondCodeReader == 1) {
                                ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(7005, cantidadDeRegistrosALeer);
                                trans.setRequest(req);
                                try {
                                    trans.execute();
                                } catch (ModbusSlaveException ex) {
                                    System.out.println("Alerta 1");
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Alerta 1");
                                    alert.setHeaderText("Error sensor validador");
                                    alert.setContentText(ex.getMessage());
                                    alert.showAndWait();
                                    System.exit(1);
                                } catch (ModbusException ex) {
                                    System.out.println("Alerta 2");
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Alerta 2");
                                    alert.setHeaderText("Error sensor validador");
                                    alert.setContentText(ex.getMessage());
                                    alert.showAndWait();
                                    System.exit(1);
                                }
                                res = (ReadMultipleRegistersResponse) trans.getResponse();
                                res = (ReadMultipleRegistersResponse) trans.getResponse();
                                String hex = "";
                                for (int i = 0; i < res.getRegisters().length; i++) {
                                    hex = hex.concat(Integer.toHexString(res.getRegisters()[i].getValue()));
                                    //System.out.println(res.getRegisters()[i].getValue());
                                }
                                String codigo = HexToASCII.convertHexToASCII(hex);
                                //System.out.println("****** CODIGO LEIDO ******");
                                System.out.println("Código: " + codigo);                                
                                
                                //Verificar a traves de lector verificador, updatear valores is_verificado, is_before_time tabla registro_diario_caja_sellada
                                Query.updateRegistroDiarioCajaCerradaCodigo(codigo, waitingTime);
                                
                            }

                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            System.out.println("Alerta 3");
                            alert.setTitle("Alerta 3");
                            alert.setHeaderText("Error sensor validador");
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                            System.exit(1);
                        }
                        secondCodeReader = 1;
                    } while (true);
                }
            };

            /*
            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Alerta");
                            alert.setHeaderText("Error sensor validador");
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                            ex.printStackTrace();
                            System.exit(1);
             */
            thread = new Thread(runable);
            thread.start();

        } catch (Exception ex) {
            System.out.println("Alerta 4");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta 4");
            alert.setHeaderText("Error sensor validador");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    // obtiene cantidad de registros a leer     
    public int obtieneRegistros(int ref, int count) {
        ReadMultipleRegistersRequest cantidadDeRegistrosALeerRequest = new ReadMultipleRegistersRequest(ref, count); // obtiene cantidad de registros a leer                    
        trans = new ModbusTCPTransaction(tcpMasterConnection);
        trans.setRequest(cantidadDeRegistrosALeerRequest);
        try {
            trans.execute();
        } catch (ModbusException ex) {
            System.out.println("Alerta 6");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta 6");
            alert.setHeaderText("Error de conexión sensor validador");
            alert.setContentText("Sensor desconectado: " + ex.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
        return res.getRegisters()[0].getValue();
    }

}
