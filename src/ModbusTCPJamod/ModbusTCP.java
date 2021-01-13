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
import java.util.ArrayList;
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
    public Thread threadQuery = null;
    public String error = null;
    int count = 0;
    String codigo = "";
    int countThread = 0;

    ArrayList<ThreadQuery> threadQuerys = new ArrayList();
    ArrayList<ThreadQuery> threadQuerysEnEjecucion = new ArrayList();

    public ModbusTCP(String nombre, String ip, int waitingTime, int inicioDireccionDeMemoriaLectura, int calibradorId) {
        try {

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
                    ejecutaThreadQuery();
                    codigo = "";
                    String codigoAnterior = "NA";
                    do {
                        ModbusTCPTransaction trans = new ModbusTCPTransaction(tcpMasterConnection);
                        ReadMultipleRegistersResponse res = null;

                        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(codigoRegistroInicial, 4); //desde donde comienza el registro del codigo 7005
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
                        }
                        hex = hex.replace(" ", "");

                        if (!hex.equalsIgnoreCase("") || !hex.equalsIgnoreCase("0")) {
                            if (hex.length() % 2 != 0) {
                                return;
                            }

                            codigo = Utils.HexToASCII.convertHexToASCII(hex);

                            if (codigo == null) {
                                return;
                            }
                            codigo = Utils.HexToASCII.limpiarString(codigo);

                            if (!codigo.equalsIgnoreCase(codigoAnterior)) {
                                codigoAnterior = codigo;

                                count++;
                                countThread++;

                                ThreadQuery threadQuery = new ThreadQuery(codigo, calibradorId, waitingTime, countThread);
                                threadQuerys.add(threadQuery);
                                threadQuery.start();

                            }
                        } else {
                            System.out.println("Lectura no valida o vacía");
                        }
                        //}

                        //secondCodeReader = 1;
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

    public void ejecutaThreadQuery() {
        if (threadQuerysEnEjecucion.size() < 50) {
            if (threadQuerys.size() > 0) {
                threadQuerys.get(0).start();
            }
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

    public class ThreadQuery extends Thread {

        int calibradorId;
        int waitingTime;
        String codigo;
        int countThread;

        public ThreadQuery(String codigo, int calibradorId, int waitingTime, int countThread) {
            this.calibradorId = calibradorId;
            this.waitingTime = waitingTime;
            this.codigo = codigo;
            this.countThread = countThread;
        }

        public void run() {
            System.out.println("Thread running: " + countThread);
            ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
            //select registro diario             
            Query.existCodigoRegistroDiarioCajaSelladaByLectorValidador(conn, codigo, count);

            //inserta codigo en tabla lectorValidadpr_en_calibrador
            Query.insertLectorValidadorEnCalibrador(conn, calibradorId, codigo, Date.getDateString(), Date.getHourString());

            //Verificar a traves de lector verificador, updatear valores is_verificado, is_before_time tabla registro_diario_caja_sellada
            Query.updateRegistroDiarioCajaCerradaCodigo(conn, codigo, waitingTime);
            try {
                conn.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
            }
            conn.disconnection();
            conn = null;
            removeThreadByCodigo(codigo);
            System.out.println("Thread running: " + countThread + "finished");
        }

        public void removeThreadByCodigo(String codigo) {
            for (int i = 0; i < threadQuerysEnEjecucion.size(); i++) {
                if (threadQuerysEnEjecucion.get(i).codigo.equalsIgnoreCase(codigo)) {
                    threadQuerysEnEjecucion.remove(i);
                }
            }
        }
    }

}
