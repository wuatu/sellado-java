/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author crist
 */
public class Date {

    public static String getDateString() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        String dateString = sdf.format(date);
        return dateString;
    }
        
    
    public static String getHourString() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        String hourtring = sdf.format(date);
        return hourtring;
    }
    
    public static java.util.Date getDateParseStringToDate(String fecha, String hora) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        try {
            return sdf.parse(fecha+" "+hora);            
        } catch (ParseException ex) {
            Logger.getLogger(Date.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }    
    
    public static long getDateParseStringToLongTime(String fecha, String hora) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        try {
            return sdf.parse(fecha+" "+hora).getTime();            
        } catch (ParseException ex) {
            Logger.getLogger(Date.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    } 
    
}
