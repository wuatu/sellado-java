/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PortComJSerial;

import dk.thibaut.serial.enums.BaudRate;
import dk.thibaut.serial.enums.DataBits;
import dk.thibaut.serial.enums.Parity;
import dk.thibaut.serial.enums.StopBits;

/**
 *
 * @author crist
 */
public class PortCOMSettings {
    public static BaudRate baudRate (String baudRateString){
        BaudRate baudRate=null;        
        if(baudRateString.equalsIgnoreCase("115200")){
            baudRate=BaudRate.B115200;
        } else if(baudRateString.equalsIgnoreCase("19200")){
            baudRate=BaudRate.B19200;
        } else if(baudRateString.equalsIgnoreCase("256000")){
            baudRate=BaudRate.B256000;
        } else if(baudRateString.equalsIgnoreCase("38400")){
            baudRate=BaudRate.B38400;
        } else if(baudRateString.equalsIgnoreCase("57600")){
            baudRate=BaudRate.B57600;
        } else if(baudRateString.equalsIgnoreCase("9600")){
            baudRate=BaudRate.B9600;
        } else{
            baudRate=BaudRate.UNKNOWN;
        }
        return baudRate;
    }
    
    public static Parity parity(String parityString){
        Parity parity=null;        
        if(parityString.equalsIgnoreCase("EVEN")){
            parity=Parity.EVEN;
        } else if(parityString.equalsIgnoreCase("MARK")){
            parity=Parity.MARK;
        } else if(parityString.equalsIgnoreCase("NONE")){
            parity=Parity.NONE;
        } else if(parityString.equalsIgnoreCase("ODD")){
            parity=Parity.ODD;
        } else if(parityString.equalsIgnoreCase("SPACE")){
            parity=Parity.SPACE;
        } else{
            parity=Parity.UNKNOWN;
        }
        return parity;
    }
    
    public static StopBits stopBits (String stopBitsString){
        StopBits stopBits=null;        
        if(stopBitsString.equalsIgnoreCase("ONE")){
            stopBits=StopBits.ONE;
        } else if(stopBitsString.equalsIgnoreCase("ONE_HALF")){
            stopBits=StopBits.ONE_HALF;
        } else if(stopBitsString.equalsIgnoreCase("TWO")){
            stopBits=StopBits.TWO;
        } else{
            stopBits=StopBits.UNKNOWN;
        }
        return stopBits;
    }
    
    public static DataBits dataBits (String dataBitsString){
        DataBits dataBits=null;        
        if(dataBitsString.equalsIgnoreCase("D5")){
            dataBits=DataBits.D5;
        } else if(dataBitsString.equalsIgnoreCase("D7")){
            dataBits=DataBits.D7;
        } else if(dataBitsString.equalsIgnoreCase("D8")){
            dataBits=DataBits.D8;
        } else{
            dataBits=DataBits.UNKNOWN;
        }
        return dataBits;
    }
    
    public static String baudRateString (BaudRate baudRate){
        String baudRateString=null;        
        if(baudRate.equals(BaudRate.B115200)){
            baudRateString="115200";
        } else if(baudRate.equals(BaudRate.B19200)){
            baudRateString="19200";
        } else if(baudRate.equals(BaudRate.B256000)){
            baudRateString="256000";
        } else if(baudRate.equals(BaudRate.B38400)){
            baudRateString="38400";
        } else if(baudRate.equals(BaudRate.B57600)){
            baudRateString="57600";
        } else if(baudRate.equals(BaudRate.B9600)){
            baudRateString="9600";
        } else{
            baudRateString="UNKNOWN";
        }
        return baudRateString;
    }
}
