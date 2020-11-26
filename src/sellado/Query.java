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
import sellado.models.CajaSellado;
import sellado.models.CajaUnitec;

/**
 *
 * @author crist
 */
public class Query {

    public static ResultSet getRfidSalidaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid_salida inner join calibrador on fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener RFIDJoinLineaJoinCalibrador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRfidRegistroColaborador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid_registro_colaborador");
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getRfidRegistroColaborador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRFIDJoinLineaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid inner join linea on rfid.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener RFIDJoinLineaJoinCalibrador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getLectoresJoinLineaJoinCalibrador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from lector inner join linea on lector.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id");
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getLectoresJoinLineaJoinCalibrador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRFIDJoinLineaJoinCalibradorWherePortCOM(ConexionBaseDeDatosSellado conn, String portCom) {
        try {
            String query = "select * from rfid inner join linea on rfid.fk_linea = linea.id inner join calibrador on linea.fk_calibrador = calibrador.id where rfid.ip=? limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, portCom);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (!isEmptyResultSet(resultSet, "Existe linea para este puerto:" + portCom, "No existe linea para este puerto: " + portCom)) {
                return resultSet;
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener RFIDJoinLineaJoinCalibradorWherePortCOM SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
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
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener updateFechaTerminoUsuarioEnLinea SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            System.out.println("Error tipo SQLException portCOM metodo updateFechaTerminoUsuarioEnLinea: " + ex.getMessage());
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertUsuarioEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario, ResultSet resultSetRFID, ResultSet resultSetAperturaCierreDeTurno) {
        try {
            resultSetUsuario.beforeFirst();
            resultSetRFID.beforeFirst();
            resultSetAperturaCierreDeTurno.beforeFirst();
            while (resultSetAperturaCierreDeTurno.next()) {
                while (resultSetUsuario.next()) {
                    while (resultSetRFID.next()) {
                        System.out.println("entre rfid");
                        String query = " insert into registro_diario_usuario_en_linea (id_linea,nombre_linea,id_rfid,nombre_rfid,ip_rfid,id_usuario,usuario_rut,nombre_usuario,apellido_usuario,rfid_usuario,fecha_inicio,hora_inicio,fecha_termino,hora_termino,id_calibrador,nombre_calibrador,id_apertura_cierre_de_turno)"
                                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                        preparedStmt.setInt(1, resultSetRFID.getInt("linea.id"));
                        preparedStmt.setString(2, resultSetRFID.getString("linea.nombre"));
                        preparedStmt.setInt(3, resultSetRFID.getInt("rfid.id"));
                        preparedStmt.setString(4, resultSetRFID.getString("rfid.nombre"));
                        preparedStmt.setString(5, resultSetRFID.getString("rfid.ip"));
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
                        preparedStmt.setInt(17, resultSetAperturaCierreDeTurno.getInt("id"));
                        preparedStmt.execute();
                        Query.insertRegistroProduccion("ok", "Se registra colaborador: " + resultSetUsuario.getString("rut") + ", " + resultSetUsuario.getString("nombre") + " " + resultSetUsuario.getString("apellido") + " en calibrador: " + resultSetRFID.getString("calibrador.nombre") + " "
                                + " en linea: " + resultSetRFID.getString("linea.nombre") + " ", Utils.Date.getDateString(), Utils.Date.getHourString());
                    }
                }
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al insertar insertUsuarioEnLinea SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Query.insertRegistroProduccion("Error PortCom Query", "Error al insertar insertUsuarioEnLinea SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ResultSet getRegistroDiarioUsuariosEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetLector, String fecha) {
        try {
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            resultSetLector.beforeFirst();
            while (resultSetLector.next()) {
                String query = "select DISTINCT * from registro_diario_usuario_en_linea "
                        + "where id_calibrador = ? and id_linea = ? and fecha_inicio <= ? and fecha_termino=''";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                preparedStmt.setInt(1, resultSetLector.getInt("calibrador.id"));
                preparedStmt.setInt(2, resultSetLector.getInt("linea.id"));
                preparedStmt.setString(3, fecha);
                ResultSet resultSet = preparedStmt.executeQuery();
                if (!isEmptyResultSet(resultSet, "Usuario(s) en linea encontrado(s)", "Usuario(s) en linea NO encontrado(s)")) {
                    Query.insertRegistroProduccion("ok", "Usuario(s) en linea encontrado(s)", Utils.Date.getDateString(), Utils.Date.getHourString());
                    return resultSet;
                }
                Query.insertRegistroProduccion("err", "Usuario(s) en linea NO encontrado(s)", Utils.Date.getDateString(), Utils.Date.getHourString());
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getRegistroDiarioUsuariosEnLinea SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getLectorByPort(ConexionBaseDeDatosSellado conn, String port) {
        try {
            String query = "select * from lector inner join linea on lector.fk_linea=linea.id inner join calibrador on linea.fk_calibrador=calibrador.id where ip = ? limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, port);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se obtuvo lector por puerto: " + port, "No se obtuvo lector por puerto: " + port)) {
                return resultSet;
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getLectorByPort SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRfidByPort(ConexionBaseDeDatosSellado conn, String port) {
        try {
            String query = "select * from rfid inner join linea on rfid.fk_linea=linea.id inner join calibrador on linea.fk_calibrador=calibrador.id where ip = ? limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, port);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se obtuvo RFID por puerto: " + port, "No se obtuvo RFID por puerto: " + port)) {
                return resultSet;
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getRFIDByPort SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRfidSalidaByPort(ConexionBaseDeDatosSellado conn, String port) {
        try {
            //si no retorna quiere decir que el puerto proviene de otra tabla rfid
            String query = "select * from rfid_salida where ip=? limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, port);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se obtuvo RFID salida por puerto: " + port, "No se obtuvo RFID salida por puerto: " + port)) {
                return resultSet;
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getRFIDByPort SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getRegistroDiarioCajaSellada(ConexionBaseDeDatosSellado conn, String codigo) {
        try {
            for (int i = 0; i < codigo.length(); i++) {
                System.out.println(codigo.charAt(i));
            }
            System.out.println(codigo.length());
            System.out.println("fecha sellado:" + Date.getDateString());
            String fecha = Date.getDateString();
            String query = "select * from registro_diario_caja_sellada where codigo_de_barra= ? and fecha_validacion='' order by fecha_sellado desc, hora_sellado desc limit 1";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStmt.setString(1, codigo);
            //preparedStmt.setString(2, fecha);
            ResultSet resultSet = preparedStmt.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se encontro registro diario caja sellada para codigo:" + codigo, "No se encontro registro diario caja sellada para codigo:" + codigo)) {
                return resultSet;
            }
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getRegistroDiarioCajaSellada SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void insertRegistroDiarioCajaSellada(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuariosEnLinea, ResultSet resultSetGetLectorByPort, ResultSet crearRegistroDiarioCajaSellada, CajaSellado caja, String codigo) {
        try {
            resultSetUsuariosEnLinea.beforeFirst();
            resultSetGetLectorByPort.beforeFirst();
            crearRegistroDiarioCajaSellada.beforeFirst();
            while (crearRegistroDiarioCajaSellada.next()) {
                while (resultSetGetLectorByPort.next()) {
                    while (resultSetUsuariosEnLinea.next()) {
                        // the mysql insert statement
                        String query = " insert into registro_diario_caja_sellada (id_calibrador, "
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
                                + "hora_sellado,"
                                + "fecha_validacion,"
                                + "hora_validacion,"
                                + "id_apertura_cierre_de_turno)"
                                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        // create the mysql insert preparedstatement
                        PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                        preparedStmt.setInt(1, resultSetUsuariosEnLinea.getInt("id_calibrador"));
                        preparedStmt.setString(2, resultSetUsuariosEnLinea.getString("nombre_calibrador"));
                        preparedStmt.setInt(3, resultSetUsuariosEnLinea.getInt("id_linea"));
                        preparedStmt.setString(4, resultSetUsuariosEnLinea.getString("nombre_linea"));
                        preparedStmt.setInt(5, resultSetUsuariosEnLinea.getInt("id_rfid"));
                        preparedStmt.setString(6, resultSetUsuariosEnLinea.getString("nombre_rfid"));
                        preparedStmt.setString(7, resultSetUsuariosEnLinea.getString("ip_rfid"));
                        preparedStmt.setInt(8, resultSetGetLectorByPort.getInt("id"));
                        preparedStmt.setString(9, resultSetGetLectorByPort.getString("nombre"));
                        preparedStmt.setString(10, resultSetGetLectorByPort.getString("ip"));
                        preparedStmt.setInt(11, resultSetUsuariosEnLinea.getInt("id_usuario"));
                        preparedStmt.setString(12, resultSetUsuariosEnLinea.getString("usuario_rut"));
                        preparedStmt.setString(13, resultSetUsuariosEnLinea.getString("nombre_usuario"));
                        preparedStmt.setString(14, resultSetUsuariosEnLinea.getString("apellido_usuario"));
                        preparedStmt.setString(15, codigo);
                        if (caja != null) {
                            preparedStmt.setInt(16, caja.getId());
                        } else {
                            preparedStmt.setInt(16, -1);
                        }
                        preparedStmt.setString(17, caja.getEnvase());
                        preparedStmt.setString(18, caja.getVariedad());
                        preparedStmt.setString(19, caja.getCategoria());
                        preparedStmt.setString(20, caja.getCalibre());
                        preparedStmt.setString(21, caja.getCorrelativo());
                        preparedStmt.setInt(22, caja.getPonderacion());
                        preparedStmt.setString(23, Date.getDateString());
                        preparedStmt.setString(24, Date.getHourString());
                        preparedStmt.setString(25, "");
                        preparedStmt.setString(26, "");
                        preparedStmt.setInt(27, crearRegistroDiarioCajaSellada.getInt("id"));
                        preparedStmt.execute();
                        Query.insertRegistroProduccion("ok",
                                "Se inserta envase: " + caja.getEnvase() + ""
                                + ", código: " + codigo + ""
                                + ", colaborador: " + resultSetUsuariosEnLinea.getString("usuario_rut") + ""
                                + " " + resultSetUsuariosEnLinea.getString("nombre_usuario") + ""
                                + " " + resultSetUsuariosEnLinea.getString("apellido_usuario") + ""
                                + ", calibrador: " + resultSetUsuariosEnLinea.getString("nombre_calibrador") + ""
                                + ", linea: " + resultSetUsuariosEnLinea.getString("nombre_linea"),
                                Utils.Date.getDateString(),
                                Utils.Date.getHourString());

                    }
                }
            }
        } catch (SQLException ex) {
            Query.insertRegistroProduccion("Error PortCom Query", "Error al insertar insertRegistroDiarioCajaSellada SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Query.insertRegistroDev("Error PortCom Query", "Error al insertar insertRegistroDiarioCajaSellada SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ResultSet getAperturaCierreDeTurno(ConexionBaseDeDatosSellado conn) {
        try {
            String query = "select * from apertura_cierre_de_turno where fecha_cierre='' and hora_cierre='' limit 1";
            PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se obtuvo apertura/cierre de turno", "No se obtuvo apertura/cierre de turno")) {
                return resultSet;
            }

        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getAperturaCierreDeTurno SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getUsuarioPorRFID(ConexionBaseDeDatosSellado conn, String codigoRFID) {
        try {
            String query = "select * from usuario where rfid='" + codigoRFID + "' limit 1";
            PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = preparedStatement.executeQuery();
            //Obtener registro diario de tabla registro_diario_usuario_en_linea (cuando llega un código de barras tipo DataMatrix)
            if (!isEmptyResultSet(resultSet, "Existe usuario por codigo RFID: " + codigoRFID, "No existe usuario por codigo RFID: " + codigoRFID)) {
                Query.insertRegistroProduccion("ok", "Se encuentra colaborador por RFID: " + codigoRFID, Utils.Date.getDateString(), Utils.Date.getHourString());
                return resultSet;
            }
            Query.insertRegistroProduccion("obs", "No se encuentra colaborador por RFID: " + codigoRFID, Utils.Date.getDateString(), Utils.Date.getHourString());

        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getUsuarioPorRFID SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getLectorValidador(ConexionBaseDeDatosSellado conn) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from lector_validador");
            return resultSet;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getLectorValidador SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static CajaSellado getCajaPorCodigoSellado(ConexionBaseDeDatosSellado conn, String envaseUnitec, String categoriaUnitec, String calibreUnitec) {
        try {
            String query = "select * from caja where envase like '%" + envaseUnitec + "%' limit 1";
            PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!isEmptyResultSet(resultSet, "Se encotró envase de caja por codigo:" + envaseUnitec, "No se encotró envase de caja por codigo:" + envaseUnitec)) {
                resultSet.beforeFirst();
                CajaSellado caja = null;
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String envase = resultSet.getString("envase");
                    String variedad = resultSet.getString("variedad");
                    String categoria = resultSet.getString("categoria");
                    String calibre = resultSet.getString("calibre");
                    String correlativo = resultSet.getString("correlativo");
                    int ponderacion = resultSet.getInt("ponderacion");
                    caja = new CajaSellado(id, envase, variedad, categoria, calibre, correlativo, ponderacion);
                }
                return caja;
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getCajaPorCodigoSellado SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static CajaUnitec getCajaPorCodigoUnitec(ConexionBaseDeDatosUnitec conn, String codigo) {
        try {
            if (conn.getConnection() != null) {
                String query = "select * from Danich_DatosCajas where Cod_Caja =" + codigo + "";
                PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (!isEmptyResultSet(resultSet, "Se encotró caja por codigo:" + codigo, "No se encotró caja por codigo:" + codigo)) {
                    resultSet.beforeFirst();
                    CajaUnitec caja = null;
                    while (resultSet.next()) {
                        String Cod_Caja = resultSet.getString("Cod_Caja");
                        String Codigo_Confection = resultSet.getString("Codigo_Confection");
                        String Confection = resultSet.getString("Confection");
                        String Codigo_Embalaje = resultSet.getString("Codigo_Embalaje");
                        String Embalaje = resultSet.getString("Embalaje");
                        String Codigo_Envase = resultSet.getString("Codigo_Envase");
                        String Envase = resultSet.getString("Envase");
                        String Categoria = resultSet.getString("Categoria");
                        String Categoria_Timbrada = resultSet.getString("Categoria_Timbrada");
                        String Codigo_Calibre = resultSet.getString("Codigo_Calibre");
                        String Calibre = resultSet.getString("Calibre");
                        caja = new CajaUnitec(Cod_Caja, Codigo_Confection, Confection, Codigo_Embalaje, Embalaje, Codigo_Envase, Envase, Categoria, Categoria_Timbrada, Codigo_Calibre, Calibre);
                    }
                    if (caja != null) {
                        Query.insertRegistroProduccion("ok", "Se encuentra envase: " + caja.getEnvase() + " por código: " + codigo, Utils.Date.getDateString(), Utils.Date.getHourString());
                        return caja;
                    }
                    Query.insertRegistroProduccion("err", "No se pudo encontrar caja en base de datos UNITEC por código: " + codigo, Utils.Date.getDateString(), Utils.Date.getHourString());
                }
            } else {
                Query.insertRegistroProduccion("err", "Error conexion a base de datos unic no establecida: ", Utils.Date.getDateString(), Utils.Date.getHourString());
                Query.insertRegistroDev("err", "Error conexion a base de datos unic no establecida: ", Utils.Date.getDateString(), Utils.Date.getHourString());
                return null;
            }
        } catch (SQLException ex) {
            Query.insertRegistroProduccion("err", "Error al obtener getCajaPorCodigoUnitec SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Query.insertRegistroDev("err", "Error al obtener getCajaPorCodigoUnitec SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static ResultSet getUsuarioEnLineaPorFecha(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuario) {
        try {
            ResultSet resultSet = null;
            resultSetUsuario.beforeFirst();
            while (resultSetUsuario.next()) {
                String query = "select * from registro_diario_usuario_en_linea where id_usuario = '" + resultSetUsuario.getString("id") + "' and fecha_termino = '' and hora_termino = '' order by fecha_inicio <= '" + Date.getDateString() + "' desc limit 1";
                PreparedStatement preparedStatement = conn.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                resultSet = preparedStatement.executeQuery();
                if (!isEmptyResultSet(resultSet, "Usuario en línea encontrado", "No existe registro para usuario en línea ")) {
                    return resultSet;
                }
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getUsuarioEnLineaPorFecha SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean isUsuarioEnLineaEnMismaLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetUsuarioEnLineaPorFecha, String port) {
        try {
            resultSetUsuarioEnLineaPorFecha.beforeFirst();
            while (resultSetUsuarioEnLineaPorFecha.next()) {
                if (resultSetUsuarioEnLineaPorFecha.getString("ip_rfid").equalsIgnoreCase(port)) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener isUsuarioEnLineaEnMismaLinea SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void updateRegistroDiarioCajaCerradaCodigo(String codigo, int waitingTime) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        ResultSet resultSet = getRegistroDiarioCajaSellada(conn, codigo);
        try {
            String horaSellado = null;
            String fechaSellado = null;
            resultSet.beforeFirst();
            while (resultSet.next()) {
                horaSellado = resultSet.getString("hora_sellado");
                fechaSellado = resultSet.getString("fecha_sellado");

                System.out.println("hora_sellado: " + horaSellado);
                System.out.println("fecha_Sellado: " + fechaSellado);

                java.util.Date dateSellado = Date.getDateParseStringToDate(fechaSellado, horaSellado);
                java.util.Date dateValidacion = new java.util.Date();
                int tiempoTranscurridoEnMinutos = (int) ((dateValidacion.getTime() - dateSellado.getTime()) / 60000);
                boolean isBeforeTime = false;
                if (tiempoTranscurridoEnMinutos > waitingTime) {
                    isBeforeTime = true;
                }
                String query = "update registro_diario_caja_sellada set fecha_validacion = ?, hora_validacion = ?, is_verificado = ?, is_before_time = ? where codigo_de_barra = ? and fecha_validacion='' ORDER BY id DESC;";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                preparedStmt.setString(1, Date.getDateString());
                preparedStmt.setString(2, Date.getHourString());
                preparedStmt.setBoolean(3, true);
                preparedStmt.setBoolean(4, isBeforeTime);
                preparedStmt.setString(5, codigo);
                preparedStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error update updateRegistroDiarioCajaCerradaCodigo SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
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

    public static void insertLectorValidadorEnCalibrador(int calibradorId, String codigo, String fecha, String hora) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from lectorValidador_en_calibrador where fk_calibrador='" + calibradorId + "'");
            if (isEmptyResultSet(resultSet, "Se obtuvo lector validador de calibrador por id: " + calibradorId, "No se pudo obtener lector validador de calibrador por id: " + calibradorId)) {
                String query = " insert into lectorValidador_en_calibrador (codigo,fecha, hora,fk_calibrador)"
                        + " values (?, ?, ?, ?, ?)";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                preparedStmt.setString(1, codigo);
                preparedStmt.setString(2, fecha);
                preparedStmt.setString(3, hora);
                preparedStmt.setInt(4, calibradorId);
                preparedStmt.execute();
            } else {
                String query = "update lectorValidador_en_calibrador set codigo = ?, fecha = ?, hora=? where fk_calibrador = ?";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                System.out.println(codigo);
                System.out.println(fecha);
                System.out.println(hora);
                System.out.println(calibradorId);
                preparedStmt.setString(1, codigo);
                preparedStmt.setString(2, fecha);
                preparedStmt.setString(3, hora);
                preparedStmt.setInt(4, calibradorId);
                preparedStmt.executeUpdate();
            }

        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn.getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.disconnection();
        conn = null;
    }

    public static int getWaitingTime() {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        try {
            int waitingTime = -1;
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from configuracion");
            while (resultSet.next()) {
                waitingTime = resultSet.getInt("max_wait_time");
            }
            try {
                conn.getConnection().close();
            } catch (SQLException ex1) {
                Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
            }
            conn.disconnection();
            conn = null;
            return waitingTime;
        } catch (SQLException ex) {
            Query.insertRegistroDev("Error PortCom Query", "Error al obtener getWaitingTime SQLException: " + ex.getMessage(), Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Sellado.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn.getConnection().close();
        } catch (SQLException ex1) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
        }
        conn.disconnection();
        conn = null;
        return -1;
    }

    public static void insertRegistroDev(String nombre, String registro, String fecha, String hora) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        try {
            String query = " insert into registro_dev (nombre,registro,fecha, hora)"
                    + " values (?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
            preparedStmt.setString(1, nombre);
            preparedStmt.setString(2, registro);
            preparedStmt.setString(3, fecha);
            preparedStmt.setString(4, hora);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn.getConnection().close();
        } catch (SQLException ex1) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
        }
        conn.disconnection();
        conn = null;
    }

    public static void insertRegistroProduccion(String nombre, String registro, String fecha, String hora) {
        ConexionBaseDeDatosSellado conn = new ConexionBaseDeDatosSellado();
        try {
            System.out.println("hora:" + hora);
            String query = " insert into registro_produccion (id_colaborador, nombre_colaborador, apellido_colaborador, registro, fecha, hora)"
                    + " values (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
            preparedStmt.setInt(1, 0);
            preparedStmt.setString(2, nombre);
            preparedStmt.setString(3, "apellido");
            preparedStmt.setString(4, registro);
            preparedStmt.setString(5, fecha);
            preparedStmt.setString(6, hora);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(PortCOM.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn.getConnection().close();
        } catch (SQLException ex1) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex1);
        }
        conn.disconnection();
        conn = null;
    }

    public static void insertLectorEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetGetLectorByPort, String codigo, String fecha, String hora) {
        try {
            resultSetGetLectorByPort.beforeFirst();
            while (resultSetGetLectorByPort.next()) {
                String lineaId = resultSetGetLectorByPort.getString("linea.id");
                String lectorId = resultSetGetLectorByPort.getString("lector.id");
                Statement statement = conn.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery("select * from lector_en_linea where fk_lector='" + lectorId + "'");
                if (isEmptyResultSet(resultSet, "Se obtuvo lector de linea por id linea: " + lineaId, "No se pudo obtener lector de linea por id linea: " + lineaId)) {
                    String query = " insert into lector_en_linea (codigo,fecha, hora,fk_linea,fk_lector)"
                            + " values (?, ?, ?, ?, ?)";
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setString(1, codigo);
                    preparedStmt.setString(2, fecha);
                    preparedStmt.setString(3, hora);
                    preparedStmt.setString(4, lineaId);
                    preparedStmt.setString(5, lectorId);
                    preparedStmt.execute();
                } else {
                    String query = "update lector_en_linea set codigo = ?, fecha = ?, hora=? where fk_linea = ? and fk_lector=?";
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setString(1, codigo);
                    preparedStmt.setString(2, fecha);
                    preparedStmt.setString(3, hora);
                    preparedStmt.setString(4, lineaId);
                    preparedStmt.setString(5, lectorId);
                    preparedStmt.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertRfidEnLinea(ConexionBaseDeDatosSellado conn, ResultSet resultSetGetRfidByPort, String codigo, String fecha, String hora) {
        try {
            resultSetGetRfidByPort.beforeFirst();
            while (resultSetGetRfidByPort.next()) {
                String lineaId = resultSetGetRfidByPort.getString("linea.id");
                String rfidId = resultSetGetRfidByPort.getString("rfid.id");
                Statement statement = conn.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery("select * from rfid_en_linea where fk_rfid='" + rfidId + "'");
                if (isEmptyResultSet(resultSet, "Se obtuvo RFID de linea por id linea: " + lineaId, "No se pudo obtener lector de linea por id linea: " + lineaId)) {
                    String query = " insert into rfid_en_linea (codigo,fecha, hora,fk_linea,fk_rfid)"
                            + " values (?, ?, ?, ?, ?)";
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    preparedStmt.setString(1, codigo);
                    preparedStmt.setString(2, fecha);
                    preparedStmt.setString(3, hora);
                    preparedStmt.setString(4, lineaId);
                    preparedStmt.setString(5, rfidId);
                    preparedStmt.execute();
                } else {
                    String query = "update rfid_en_linea set codigo = ?, fecha = ?, hora=? where fk_linea = ? and fk_rfid=?";
                    PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                    System.out.println(codigo);
                    System.out.println(fecha);
                    System.out.println(hora);
                    System.out.println(lineaId);
                    System.out.println(rfidId);
                    preparedStmt.setString(1, codigo);
                    preparedStmt.setString(2, fecha);
                    preparedStmt.setString(3, hora);
                    preparedStmt.setString(4, lineaId);
                    preparedStmt.setString(5, rfidId);
                    preparedStmt.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertRfidSalidaEnCalibrador(ConexionBaseDeDatosSellado conn, String calibradorId, String codigo, String fecha, String hora) {
        try {
            Statement statement = conn.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from rfid_en_linea where fk_calibrador='" + calibradorId + "'");
            if (isEmptyResultSet(resultSet, "Se obtuvo RFID de linea por id linea: " + calibradorId, "No se pudo obtener lector de linea por id linea: " + calibradorId)) {
                String query = " insert into rfid_en_linea (codigo,fecha, hora,fk_calibrador)"
                        + " values (?, ?, ?, ?)";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                preparedStmt.setString(1, codigo);
                preparedStmt.setString(2, fecha);
                preparedStmt.setString(3, hora);
                preparedStmt.setString(4, calibradorId);
                preparedStmt.execute();
            } else {
                String query = "update rfid_en_linea set codigo = ?, fecha = ?, hora=? where fk_calibrador = ?";
                PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
                System.out.println(codigo);
                System.out.println(fecha);
                System.out.println(hora);
                System.out.println(calibradorId);
                preparedStmt.setString(1, codigo);
                preparedStmt.setString(2, fecha);
                preparedStmt.setString(3, hora);
                preparedStmt.setString(4, calibradorId);
                preparedStmt.executeUpdate();
            }

        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertRegistroRfid(ConexionBaseDeDatosSellado conn, String codigo, String fecha, String hora) {
        try {

            String query = " insert into registro_rfid (codigo)"
                    + " values (?)";
            PreparedStatement preparedStmt = conn.getConnection().prepareStatement(query);
            preparedStmt.setString(1, codigo);
            preparedStmt.execute();
            Query.insertRegistroProduccion("ok", "Código RFID registro de colaborador: " + codigo, Utils.Date.getDateString(), Utils.Date.getHourString());
        } catch (SQLException ex) {
            Query.insertRegistroProduccion("err", "No se pudo obtener registro RFID registro de colaborador", Utils.Date.getDateString(), Utils.Date.getHourString());
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
