/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

import CodigoIntermedio.Cuadruplo;
import CodigoIntermedio.TablaCuadruplos;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author UNICOMER
 */
public class CodigoFinal {

    static TablaSimbolos tablaSimb = null;
    static TablaCuadruplos tablaCuad = null;

    ArrayList<String> data = new ArrayList();
    ArrayList<String> text = new ArrayList();//cambiar por cuerpo
    HashMap<Integer, String> labelControl = new HashMap();
    HashMap<String, Boolean> tempVivos = new HashMap();
    HashMap<String, String> tempEquivalente = new HashMap();
    int contMens = 0;

    public CodigoFinal(TablaSimbolos ts, TablaCuadruplos tc) {
        this.tablaSimb = ts;
        this.tablaCuad = tc;
        this.generarVariablesGlobales();
        this.print();

    }

    public void generarVariablesGlobales() {
        data.add(".data");
        for (Simbolo S : tablaSimb.getSimbolos()) {
            if (S.getAmbito().equals("main") && S.isVariable()) {
                String tipo = S.getTipo();
                switch (tipo) {
                    case "boolean":
                    case "integer": {
                        agregarVariableEntero(S.getId());
                        break;
                    }
                    case "char": {
                        agregarVariableCaracter(S.getId());
                        break;
                    }

                }
                if (tipo.startsWith("Array")) {
                    String[] list = tipo.split("\\.");
                    int space = Integer.parseInt(list[3]) - Integer.parseInt(list[2]) + 1;
                    if (list[1].equals("boolean") || list[1].equals("integer")) {
                        space *= 4;
                    }
                    agregarVariableArreglo(S.getId(), space);
                }
            }
        }
    }

    public void agregarVariableEntero(String varName) {
        varName = "_" + varName;
        String varType = "word";
        String varValue = "0";
        String fila = " %s:\t.%s %s";
        data.add(String.format(fila, varName, varType, varValue));
    }

    public void agregarVariableCaracter(String varName) {
        varName = "_" + varName;
        String varType = "byte";
        String varValue = "' '";
        String fila = " %s:\t.%s %s";
        data.add(String.format(fila, varName, varType, varValue));
    }

    void agregarVariableArreglo(String varName, int space) {
        varName = "_" + varName;
        String varType = "space";
        String varValue = space + "";
        String fila = " %s:\t.%s %s";
        data.add(String.format(fila, varName, varType, varValue));
    }

    public String agregarMensaje(String message) {
        String varName = "_msg" + ++contMens;
        String varType = "asciiz";
        String varValue = message.replaceAll("'", "\"");
        String row = " %s:\t.%s %s";
        data.add(String.format(row, varName, varType, varValue));
        return varName;
    }

    public void agregarWriteTemp(Cuadruplo C, String temp) {
        String row = "%s %s, %s";
        text.add(String.format(row, "li", "$v0", "1"));
        text.add(String.format(row, "lw", "$a0", "(" + temp + ")"));
        text.add("syscall");
    }

    public void generarWrite(Cuadruplo C, String ambitoActual) throws Exception {
        String param = C.getArg1();
        if (param.startsWith("@t")) {
            String temp = this.ObtenerTempLibre(param);
            this.agregarWriteTemp(C, temp);
        } else if (param.contains("'")) {
            String messageName = this.agregarMensaje(param);
            this.agregarMensaje(messageName);
        } else {
            Simbolo S = tablaSimb.getVariable(param, ambitoActual);
            if (S == null && !ambitoActual.equals("main")) {
                S = tablaSimb.getVariable(param, "main");
            }
            String tipo = S.getTipo();
            if (tipo.equals("string") || tipo.equals("char")) {
                this.agregarWriteString(param);
            } else {
                this.agregarWrite(param);
            }
        }
    }
    
      void agregarWrite(String variableName) {
        String row = "%s %s, %s";
        text.add(String.format(row, "li", "$v0", "1"));
        text.add(String.format(row, "lw", "$a0", "_"+variableName));
        text.add("syscall");
    }
    
    public void agregarWriteString(String variableName){
        String row = "%s %s, %s";
        text.add(String.format(row, "li", "$v0", "4"));
        text.add(String.format(row, "lw", "$a0", "_"+variableName));
        text.add("syscall");
    }
    
     void agregarWriteMensaje(String messageName) {
        String row = "%s %s, %s";
        text.add(String.format(row, "li", "$v0", "4"));
        text.add(String.format(row, "la", "$a0", messageName));
        text.add("syscall");
    }

    private String ObtenerTempLibre(String arg1) {
        String temp = tempEquivalente.get(arg1);
        tempEquivalente.remove(arg1);
        this.tempVivos.put(temp, false);
        return temp;
    }
    
    public void print(){
        for (String instruccion : data) {
            System.out.println(instruccion);
        }
    }
}
