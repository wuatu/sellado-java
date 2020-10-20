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
            /*           
                        
            
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid inner join linea on rfid.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id where ip ='" + portCom + "' limit 1");
             */
            String query = "select * from rfid inner join linea on rfid.fk_linea = linea.id inner join lector on lector.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id where rfid.ip=? limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, portCom);
            ResultSet resultSet = preparedStmt.executeQuery();

            if (!isEmptyResultSet(resultSet, "Existe linea para este puerto:" + portCom, "No existe linea para este puerto: " + portCom)) {
                return resultSet;
            }

        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static boolean isEmptyResultSet(ResultSet resultSet, String res, String err) {
        int i = 0;
        try {
            while (resultSet.next()) {
                //System.out.println("nombre: " + resultSet.getString("nombre"));
                i++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (i == 0) {
            System.out.println(err);
            return true;
        }
        System.out.println(res);
        return false;
    }

    public static ResultSet getLectoresJoinLineaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            ResultSet resultSet = statement.executeQuery("select * from lector inner join linea on lector.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void updateFechaTerminoUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario) {

        try {
            resultSetUsuario.beforeFirst();
            while (resultSetUsuario.next()) {
                String query = "update registro_diario_usuario_en_linea set fecha_termino = ?, hora_termino = ? where id_usuario = ? and fecha_termino=''";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                preparedStmt.setString(1, Date.getDateString());
                preparedStmt.setString(2, Date.getHourString());
                preparedStmt.setInt(3, resultSetUsuario.getInt("id"));
                preparedStmt.executeUpdate();
            }

        } catch (SQLException ex) {
            System.out.println("Error tipo SQLException portCOM metodo updateFechaTerminoUsuarioEnLinea: " + ex.getMessage());
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario, ResultSet resultSetRFID) {
        try {
            resultSetUsuario.beforeFirst();
            resultSetRFID.beforeFirst();
            System.out.println("llegue a instertatr");
            while (resultSetUsuario.next()) {
                System.out.println("entre set usuario");
                while (resultSetRFID.next()) {
                    System.out.println("entre rfid");
                    // the mysql insert statement
                    String query = " insert into registro_diario_usuario_en_linea (id_linea,nombre_linea,id_lector,nombre_lector,ip_lector,id_usuario,usuario_rut,nombre_usuario,apellido_usuario,rfid_usuario,fecha_inicio,hora_inicio,fecha_termino,hora_termino,id_calibrador,nombre_calibrador)"
                            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setInt(1, resultSetRFID.getInt("linea.id"));
                    preparedStmt.setString(2, resultSetRFID.getString("linea.nombre"));
                    preparedStmt.setInt(3, resultSetRFID.getInt("lector.id"));
                    preparedStmt.setString(4, resultSetRFID.getString("lector.nombre"));
                    preparedStmt.setString(5, resultSetRFID.getString("lector.ip"));
                    preparedStmt.setInt(6, resultSetUsuario.getInt("id"));
                    preparedStmt.setString(7, resultSetUsuario.getString("rut"));
                    preparedStmt.setString(8, resultSetUsuario.getString("nombre"));
                    preparedStmt.setString(9, resultSetUsuario.getString("apellido"));
                    preparedStmt.setString(10, resultSetUsuario.getString("rfid"));
                    preparedStmt.setString(11, Date.getDateString());
                    preparedStmt.setString(12, Date.getHourString());
                    preparedStmt.setString(13, "");
                    preparedStmt.setString(14, "");
                    preparedStmt.setInt(15, resultSetRFID.getInt("calibrador.id"));
                    preparedStmt.setString(16, resultSetRFID.getString("calibrador.nombre"));
                    preparedStmt.execute();
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
            String fecha = Date.getDateString();
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from registro_diario_caja_sellada"
                    + "where codigo_de_barra='" + codigo + "' and fecha_sellado >= '" + fecha + "' and fecha_validacion=''"
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
            String query = "select * from usuario where rfid='" + codigoRFID + "' limit 1";
            PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            //Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = preparedStatement.executeQuery();

            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            //ResultSet resultSet = statement.executeQuery("select * from usuario where rfid='" + codigoRFID + "' limit 1");
            if (!isEmptyResultSet(resultSet, "Existe usuario por codigo RFID: " + codigoRFID, "No existe usuario por codigo RFID: " + codigoRFID)) {
                return resultSet;
            }

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
            ResultSet resultSet = statement.executeQuery("select * from lector_validador");
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

    public static ResultSet getUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario) {
        try {
            ResultSet resultSet = null;
            resultSetUsuario.beforeFirst();
            while (resultSetUsuario.next()) {
                Statement statement = conn.getConnection().createStatement();
                resultSet = statement.executeQuery("select * from registro_diario_usuario_en_linea where id_usuario = '" + resultSetUsuario.getString("id") + "' and fecha_termino = '' and hora_termino = '' order by fecha_inicio <= '" + Date.getDateString() + "' desc limit 1");
                if (!isEmptyResultSet(resultSet, "Usuario en línea encontrado", "No existe registro para usuario en línea ")) {
                    return resultSet;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void updateRegistroDiarioCajaCerradaCodigo(String codigo, int waitingTime) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        ResultSet resultSet = getRegistroDiarioCajaSellada(conn, codigo);
        try {
            String horaSellado = null;
            String fechaSellado = null;
            while (resultSet.next()) {
                horaSellado = resultSet.getString("hora_sellado");
                fechaSellado = resultSet.getString("fecha_sellado");
            }
            java.util.Date dateSellado = Date.getDateParseStringToDate(fechaSellado, horaSellado);
            java.util.Date dateValidacion = new java.util.Date();
            int tiempoTranscurridoEnMinutos = (int) ((dateValidacion.getTime() - dateSellado.getTime()) / 60000);
            boolean isBeforeTime = false;
            if (tiempoTranscurridoEnMinutos > waitingTime) {
                isBeforeTime = true;
            }
            String query = "update registro_diario_caja_sellada set fecha_validacion = ?, hora_validacion = ?, is_verificado = ?, is_before_time = ? where codigo_de_barra = ? and fecha_termino=''";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
            preparedStmt.setString(1, Date.getDateString());
            preparedStmt.setString(2, Date.getHourString());
            preparedStmt.setBoolean(3, true);
            preparedStmt.setBoolean(4, isBeforeTime);
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
                waitingTime = resultSet.getInt("max_wait_time");
            }
            return waitingTime;
        } catch (SQLException ex) {
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

}
