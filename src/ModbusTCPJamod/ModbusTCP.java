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
                    String codigo = "";
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
                                count++;
                                codigoAnterior = codigo;


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
