/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sellado;

import baseDeDatos.ConexionBaseDeDatosSellado;
import PortComJSerial.PortCOM;
import Utils.Date;
import baseDeDatos.ConexionBaseDeDatosUnitec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import sellado.models.Caja;

/**
 *
 * @author crist
 */
public class Query {

    //private static ConexionBaseDeDatosSellado conn = null;
    public static ResultSet getRFIDJoinLineaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from rfid inner join linea on rfid.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRFIDJoinLineaJoinCalibradorWherePortCOM(ConexionBaseDeDatosSellado conn, String portCom) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid inner join linea on rfid.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id where = '" + portCom + "' limit 1");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static ResultSet getLectoresJoinLineaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from lector inner join linea on linea.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void updateFechaTerminoUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario) {

        try {
            while (resultSetUsuario.next()) {
                String query = "update registro_diario_usuario_en_linea set fecha_termino = ?, hora_termino where id_usuario = ? and fecha_termino=''";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                preparedStmt.setString(1, Date.getDateString());
                preparedStmt.setString(2, Date.getHourString());
                preparedStmt.setInt(3, resultSetUsuario.getInt("id_usuario"));
                preparedStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println("Error tipo SQLException portCOM metodo updateFechaTerminoUsuarioEnLinea: " + ex.getMessage());
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario, ResultSet resultSetRFID) {
        try {
            while (resultSetUsuario.next()) {
                while (resultSetRFID.next()) {
                    // the mysql insert statement
                    String query = " insert into registro_diario_caja_sellada ("
                            + "id_linea, "
                            + "nombre_linea, "
                            + "id_lector, "
                            + "nombre_lector, "
                            + "ip_lector,"
                            + "id_usuario,"
                            + "usuario_rut,"
                            + "nombre_usuario,"
                            + "apellido_usuario,"
                            + "rfid_usuario,"
                            + "fecha_inicio,"
                            + "id_calibrador,"
                            + "nombre_calibrador"
                            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setString(1, resultSetRFID.getString("linea.nombre"));
                    preparedStmt.setInt(2, resultSetRFID.getInt("lector.id"));
                    preparedStmt.setString(3, resultSetRFID.getString("lector.nombre"));
                    preparedStmt.setString(4, resultSetRFID.getString("lector.ip"));
                    preparedStmt.setInt(5, resultSetUsuario.getInt("usuario.id"));
                    preparedStmt.setString(6, resultSetRFID.getString("usuario.rut"));
                    preparedStmt.setString(7, resultSetRFID.getString("usuario.nombre"));
                    preparedStmt.setString(8, resultSetRFID.getString("usuario.apellido"));
                    preparedStmt.setString(9, resultSetRFID.getString("usuario.rfid"));
                    preparedStmt.setString(10, Date.getDateString());
                    preparedStmt.setInt(12, resultSetRFID.getInt("calibrador.id"));
                    preparedStmt.setString(13, resultSetRFID.getString("calibrador.nombre"));
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ResultSet getRegistroDiarioUsuariosEnLinea(ConexionBaseDeDatosSellado conn, String port, String fecha) {
        try {
            Statement statement = conn.getConnection().createStatement();

            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from registro_diario_usuario_en_linea "
                    + "where port='(" + port + ")' and fecha_inicio like '(" + fecha + ")%' and fecha_termino=''");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    public static ResultSet getRegistroDiarioCajaSellada(ConexionBaseDeDatosSellado conn, String codigo) {
        try {
            String fecha=Date.getDateString();
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from registro_diario_caja_sellada"
                    + "where codigo_de_barra='" + codigo + "' and fecha_sellado >= '"+fecha+"' and fecha_validacion=''"
                            + "order by fecha_sellado desc, hora_sellado desc limit 1");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void crearRegistroDiarioCajaSellada(ConexionBaseDeDatosSellado conn, ResultSet resultSet, Caja caja, String codigo) {
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    // the mysql insert statement
                    String query = " insert into registro_diario_caja_sellada ("
                            + "id_calibrador, "
                            + "nombre_calibrador, "
                            + "id_linea, "
                            + "nombre_linea, "
                            + "id_rfid,"
                            + "nombre_rfid,"
                            + "ip_rfid,"
                            + "id_lector,"
                            + "nombre_lector,"
                            + "ip_lector,"
                            + "id_usuario,"
                            + "rut_usuario,"
                            + "nombre_usuario,"
                            + "apellido_usuario,"
                            + "codigo_de_barra,"
                            + "id_caja,"
                            + "envase_caja,"
                            + "variedad_caja,"
                            + "categoria_caja,"
                            + "calibre_caja,"
                            + "correlativo_caja,"
                            + "ponderacion_caja,"
                            + "fecha_sellado,"
                            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setInt(1, resultSet.getInt("id_calibrador"));
                    preparedStmt.setString(2, resultSet.getString("nombre_calibrador"));
                    preparedStmt.setInt(3, resultSet.getInt("id_linea"));
                    preparedStmt.setString(4, resultSet.getString("nombre_linea"));
                    preparedStmt.setInt(5, resultSet.getInt("id_rfid"));
                    preparedStmt.setString(6, resultSet.getString("nombre_rfid"));
                    preparedStmt.setString(7, resultSet.getString("ip_rfid"));
                    preparedStmt.setInt(8, resultSet.getInt("id_lector"));
                    preparedStmt.setString(9, resultSet.getString("nombre_lector"));
                    preparedStmt.setString(10, resultSet.getString("ip_lector"));
                    preparedStmt.setInt(11, resultSet.getInt("id_usuario"));
                    preparedStmt.setString(12, resultSet.getString("rut_usuario"));
                    preparedStmt.setString(13, resultSet.getString("nombre_usuario"));
                    preparedStmt.setString(14, resultSet.getString("apellido_usuario"));
                    preparedStmt.setString(15, codigo);
                    preparedStmt.setInt(16, caja.getId());
                    preparedStmt.setString(17, caja.getEnvase());
                    preparedStmt.setString(18, caja.getVariedad());
                    preparedStmt.setString(19, caja.getCategoria());
                    preparedStmt.setString(20, caja.getCalibre());
                    preparedStmt.setString(21, caja.getCorrelativo());
                    preparedStmt.setString(22, caja.getPonderacion());
                    preparedStmt.setString(23, Date.getDateString());

                    // execute the preparedstatement
                    preparedStmt.execute();

                }
            } catch (SQLException ex) {
                Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static ResultSet getUsuarioPorRFID(ConexionBaseDeDatosSellado conn, String codigoRFID) {
        try {
            Statement statement = conn.getConnection().createStatement();

            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from usuario where rfid='" + codigoRFID + "' limit 1");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void crearUsuarioEnLínea(ConexionBaseDeDatosSellado conn) {
        // the mysql insert statement
        String query = " insert into registro_diario_usuario_en_linea (first_name, last_name, date_created, is_admin, num_points)"
                + " values (?, ?, ?, ?, ?)";
    }

    public static ResultSet getLectorValidador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from lectorValidador");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Caja getCajaPorCodigo(ConexionBaseDeDatosUnitec conn, String codigo) {
        try {
            Statement statement = conn.getConnection().createStatement();
            //Obtener registro caja por codigo
            ResultSet resultSet = statement.executeQuery("select * from caja where codigo = '" + codigo + "' ");
            Caja caja = null;
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String envase = resultSet.getString("envase");
                String variedad = resultSet.getString("variedad");
                String categoria = resultSet.getString("categoria");
                String calibre = resultSet.getString("calibre");
                String correlativo = resultSet.getString("correlativo");
                String ponderacion = resultSet.getString("ponderacion");
                caja = new Caja(id, envase, variedad, categoria, calibre, correlativo, ponderacion);
            }
            return caja;

        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void updateRegistroDiarioCajaCerradaCodigo(String codigo, int waitingTime) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        ResultSet resultSet = getRegistroDiarioCajaSellada(conn,codigo);
        try {
            String horaSellado=null;
            String fechaSellado=null;
            while (resultSet.next()){
                horaSellado=resultSet.getString("hora_sellado");
                fechaSellado=resultSet.getString("fecha_sellado");
            }
            java.util.Date dateSellado=Date.getDateParseStringToDate(fechaSellado, horaSellado);
            java.util.Date dateValidacion=new java.util.Date();
            int tiempoTranscurridoEnMinutos=(int) ((dateValidacion.getTime()-dateSellado.getTime())/60000);
            boolean isBeforeTime=false;
            if(tiempoTranscurridoEnMinutos > waitingTime){
                isBeforeTime=true;
            }
            String query = "update registro_diario_caja_sellada set fecha_validacion = ?, hora_validacion = ?, is_verificado = ?, is_before_time = ? where codigo_de_barra = ? and fecha_termino=''";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
            preparedStmt.setString(1, Date.getDateString());
            preparedStmt.setString(2, Date.getHourString());
            preparedStmt.setBoolean(3, true);
            preparedStmt.setBoolean(4,isBeforeTime);
            preparedStmt.setString(5, codigo);
            preparedStmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Error tipo SQLException portCOM metodo updateFechaTerminoUsuarioEnLinea: " + ex.getMessage());
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn.getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.disconnection();
    }

    public static int getWaitingTime(ConexionBaseDeDatosSellado conn) {
        try {
            int waitingTime = -1;
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from configuracion");
            while (resultSet.next()) {
                waitingTime = resultSet.getInt("waiting_time");
            }
            return waitingTime;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

}
