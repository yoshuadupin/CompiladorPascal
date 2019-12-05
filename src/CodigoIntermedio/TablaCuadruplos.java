/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CodigoIntermedio;

import CodigoIntermedio.Cuadruplo;
import java.util.ArrayList;

/**
 *
 * @author UNICOMER
 */
public class TablaCuadruplos {

    ArrayList<Cuadruplo> cuadruplos = new ArrayList<>();

    //Generador para operaciones con  4 instrucciones
    public int gen(String op, String arg1, String arg2, String resultado) {
        int indice = cuadruplos.size();
        cuadruplos.add(new Cuadruplo(indice, op, arg1, arg2, resultado));
        return indice;
    }

    //Generador para 2 instrucciones
    public int gen(String op, String arg1, String resultado) {
        int indice = cuadruplos.size();
        cuadruplos.add(new Cuadruplo(indice, op, arg1, resultado));
        return cuadruplos.size() - 1;
    }

    //GENERAR ETIQUETA
    public int genEtiq(String labelName) {
        int indice = cuadruplos.size();
        cuadruplos.add(new Cuadruplo(indice, "ETIQ", labelName, "", ""));
        return cuadruplos.size() - 1;
    }
    //GOTO
    public int genGOTO(String destination) {
        int indice = cuadruplos.size();
        cuadruplos.add(new Cuadruplo(indice, "GOTO", destination, "", ""));
        return indice;
    }

    @Override
    public String toString() {
        String acum = "";
        String Headers = "%-10s %-10s %-10s %-10s %-10s";
        acum += String.format(Headers, "Cuadruplo", "Operacion", "Arg1", "Arg2", "Resultado") + "\n";
        for (int i = 0; i < cuadruplos.size(); i++) {
            Cuadruplo C = cuadruplos.get(i);
            acum += (C) + "\n";
        }
        acum += "\n";

        return acum;
    }

}
