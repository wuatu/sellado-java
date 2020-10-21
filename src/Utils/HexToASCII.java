/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

/**
 *
 * @author crist
 */
public class HexToASCII {

    public static String convertHexToASCII(String hex) {
        if (!compruebaHex(hex)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int n = Integer.valueOf(s, 16);
            // Step-3 Cast the integer value to char
            builder.append((char) n);
        }

        //System.out.println("Hex = " + hex);
        //System.out.println("ASCII = " + builder.toString());
        return builder.toString();
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            if (str.startsWith("0")) {
                continue;
            } else {
                output.append((char) Integer.parseInt(str, 16));
            }

        }

        return output.toString();
    }

    public static boolean compruebaHex(String hex) {
        if (hex.length() % 2 != 0) {
            System.err.println("Invlid hex string.");
            return false;
        }
        return true;
    }
}
