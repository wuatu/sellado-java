/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PortComJSerial;

/**
 *
 * @author crist
 */
public class PortCOMSettings {
    public static int baudRate (String baudRateString){
        int baudRate=0;        
        if(baudRateString.equalsIgnoreCase("115200")){
            baudRate=115200;
        } else if(baudRateString.equalsIgnoreCase("19200")){
            baudRate=19200;
        } else if(baudRateString.equalsIgnoreCase("256000")){
            baudRate=256000;
        } else if(baudRateString.equalsIgnoreCase("38400")){
            baudRate=38400;
        } else if(baudRateString.equalsIgnoreCase("57600")){
            baudRate=57600;
        } else if(baudRateString.equalsIgnoreCase("9600")){
            baudRate=9600;
        } else{
            baudRate=0;
        }
        return baudRate;
    }
    
    public static int parity(String parityString){
        int parity=0;        
        if(parityString.equalsIgnoreCase("EVEN")){
            parity=2;
        } else if(parityString.equalsIgnoreCase("MARK")){
            parity=3;
        } else if(parityString.equalsIgnoreCase("NONE")){
            parity=0;
        } else if(parityString.equalsIgnoreCase("ODD")){
            parity=1;
        } else if(parityString.equalsIgnoreCase("SPACE")){
            parity=4;
        } else{
            parity=0;
        }
        return parity;
    }
    
    public static int stopBits (String stopBitsString){
        int stopBits=0;        
        if(stopBitsString.equalsIgnoreCase("1")){
            stopBits=1;
        } else if(stopBitsString.equalsIgnoreCase("1.5")){
            stopBits=3;
        } else if(stopBitsString.equalsIgnoreCase("2")){
            stopBits=2;
        } else{
            stopBits=0;
        }
        return stopBits;
    }
    
    public static int dataBits (String dataBitsString){
        int dataBits=0;        
        if(dataBitsString.equalsIgnoreCase("5")){
            dataBits=5;
        } else if(dataBitsString.equalsIgnoreCase("6")){
            dataBits=6;
        } else if(dataBitsString.equalsIgnoreCase("7")){
            dataBits=7;
        } else if(dataBitsString.equalsIgnoreCase("8")){
            dataBits=8;
        } else{
            dataBits=0;
        }
        return dataBits;
    }   
}
