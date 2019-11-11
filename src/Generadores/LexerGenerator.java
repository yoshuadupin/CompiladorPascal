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
public class LexerGenerator {
    public static void Main() {
        String paramsLexer[] = new String[3];
        paramsLexer[0] = "-d";
        paramsLexer[1] = "src/Analizadores/";
        paramsLexer[2] = "src/Generadores/A_Lexico.flex";
        try {
            jflex.Main.generate(paramsLexer);
        } catch (Exception e) {
        }
    }
    
    public static void generateLexer(String path) {
        File file = new File(path);
        jflex.Main.generate(file);
    }
}
