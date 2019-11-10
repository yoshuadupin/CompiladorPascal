/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.*;
import jflex.*;
import Generadores.*;
import java.io.Reader;

/**
 *
 * @author jorgecaballero
 */
public class PascalCompiler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        LexerGenerator.main(args);
        CupGenerator.main(args);
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader("./src/Pruebas/good1.pas"));
            Lexer lexer = new Lexer(reader);
                       
            parser cupParser = new parser(lexer);
            cupParser.parse();

            
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println(ex);
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println(ex);
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
