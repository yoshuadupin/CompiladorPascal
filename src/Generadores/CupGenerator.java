/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generadores;

import java.io.File;

/**
 *
 * @author jorgecaballero
 */
public class CupGenerator {
    public static void Main() {
       String params[] = new String[5];
        
        params[0] = "-destdir";
        params[1] = "src/Analizadores/";
        params[2] = "-parser";
        params[3] = "parser";
        params[4] = "src/Generadores/A_Sintactico.cup";
        try {
            java_cup.Main.main(params);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    
}
