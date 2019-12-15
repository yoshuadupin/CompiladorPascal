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
import java.util.Map;

/**
 *
 * @author UNICOMER
 */
public class CodigoFinal {

    static TablaSimbolos tablaSimb = null;
    static TablaCuadruplos tablaCuad = null;

    ArrayList<String> data = new ArrayList();
    ArrayList<String> cuerpo = new ArrayList();
    HashMap<Integer, String> resultadoControl = new HashMap();
    HashMap<String, Boolean> tempVivos = new HashMap();
    HashMap<String, String> tempEquivalente = new HashMap();
    int contMens = 0;
    int paramCount = 0;

    public CodigoFinal(TablaSimbolos ts, TablaCuadruplos tc) throws Exception {
        this.tablaSimb = ts;
        this.tablaCuad = tc;
        for (int i = 0; i <= 9; i++) {
            String temp = "$t" + i;
            tempVivos.put(temp, false);
        }
        this.generarVariablesGlobales();
        recorrerTablaCuadruplos();
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

    public void agregarWriteTemp(Cuadruplo cuad, String temp) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", "$v0", "1"));
        cuerpo.add(String.format(row, "lw", "$a0", "(" + temp + ")"));
        cuerpo.add("syscall");
    }

    public void generarWrite(Cuadruplo cuad, String ambitoActual) throws Exception {
        String param = cuad.getArg1();
        if (param.startsWith("@t")) {
            String temp = this.obtenerTempLibre(param);
            this.agregarWriteTemp(cuad, temp);
        } else if (param.contains("'")) {
            String messageName = this.agregarMensaje(param);
            this.agregarWriteString(messageName);
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

    public void agregarEtiqueta(String nombre) {
        cuerpo.add(nombre + ":");
    }

    void agregarWrite(String nombreVariable) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", "$v0", "1"));
        cuerpo.add(String.format(row, "lw", "$a0", "_" + nombreVariable));
        cuerpo.add("syscall");
    }

    public void agregarWriteString(String nombreVariable) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", "$v0", "4"));
        cuerpo.add(String.format(row, "lw", "$a0", nombreVariable));
        cuerpo.add("syscall");
    }

    void agregarWriteMensaje(String messageName) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", "$v0", "4"));
        cuerpo.add(String.format(row, "la", "$a0", messageName));
        cuerpo.add("syscall");
    }

    public void generarRead(Cuadruplo C, String ambitoActual) throws Exception {
        String variable = C.getResultado();

        Simbolo S = tablaSimb.getVariable(variable, ambitoActual);
        if (S == null && !ambitoActual.equals("main")) {
            S = tablaSimb.getVariable(variable, "main");
        }
        String tipo = S.getTipo();
        if (tipo.equals("string") || tipo.equals("char")) {
            //cft.addWriteStringStatement(variable);
        } else {
            agregarRead(variable);
        }

    }

    void agregarRead(String variable) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", "$v0", "5"));
        cuerpo.add("syscall");
        cuerpo.add(String.format(row, "sw", "$v0", "_" + variable));
    }

    void generarMove(String destino, String fuente) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "move", destino, fuente));
    }

    private String obtenerTempLibre(String arg1) {
        String temp = tempEquivalente.get(arg1);
        tempEquivalente.remove(arg1);
        this.tempVivos.put(temp, false);
        return temp;
    }

    void generarAsignacionTemp(String resultado, String temp) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "sw", temp, "_" + resultado));
    }

    private String getTempDisponible(String previousTemp) {
        for (Map.Entry<String, Boolean> entry : tempVivos.entrySet()) {
            String key = entry.getKey();
            boolean estaVivo = entry.getValue();
            if (!estaVivo) {
                tempVivos.put(key, true);
                this.tempEquivalente.put(previousTemp, key);
                return key;
            }
        }
        return "";
    }

    void generarAsignacionNumero(String arg1, String storeTemp) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "li", storeTemp, arg1));
    }

    void generarAsignacionVarEnLoad(String Arg1, String Arg2) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "lw", Arg2, "_" + Arg1));
    }

    void generarObtenerDelArreglo(String arg1, String arg2, String arrayAddress, String tempResultado) {
        String row = "%s %s, %s";
        String row2 = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "la", arrayAddress, "_" + arg1));
        cuerpo.add(String.format(row2, "add", tempResultado, arg2, arrayAddress));
    }

    void generarGuardarEnArreglo(String indice, String valor, String resultado, String arrayAddress) {
        String row = "%s %s, %s";
        String row2 = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "la", arrayAddress, "_" + resultado));
        cuerpo.add(String.format(row2, "add", arrayAddress, indice, arrayAddress));
        cuerpo.add(String.format(row, "sw", valor, "(" + arrayAddress + ")"));
    }

    void generarAdd(String Arg1, String Arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "add", resultado, Arg1, Arg2));
    }

    void generarSub(String Arg1, String Arg2, String tempAvailable) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "sub", tempAvailable, Arg1, Arg2));
    }

    void generarMul(String Arg1, String Arg2, String result) {
        String row = "%s %s, %s ,%s";
        cuerpo.add(String.format(row, "mul", result, Arg1, Arg2));
    }

    void generarDiv(String Arg1, String Arg2, String result) {
        String row = "%s %s, %s , %s";
        cuerpo.add(String.format(row, "div", Arg1, Arg2, result));
    }

    void generarB(String resultado) {
        cuerpo.add("b " + resultado);
    }

    void generarBGT(String arg1, String arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "bgt", arg1, arg2, resultado));
    }

    void generarBLT(String Arg1, String Arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "blt", Arg1, Arg2, resultado));
    }

    void generarBGE(String Arg1, String Arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "bge", Arg1, Arg2, resultado));
    }

    void generarBLE(String Arg1, String Arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "ble", Arg1, Arg2, resultado));
    }

    void generateBE(String Arg1, String Arg2, String resultado) {
        String row = "%s %s, %s, %s";
        cuerpo.add(String.format(row, "beq", Arg1, Arg2, resultado));
    }

    void generateJal(String arg1) {
        String row = "%s %s";
        cuerpo.add(String.format(row, "jal", arg1));
    }

    void generateParam(String arg1, int paramCount) {
        String row = "%s %s, %s";
        cuerpo.add(String.format(row, "move", "$a" + paramCount, arg1));
    }

    public void recorrerTablaCuadruplos() throws Exception {
        String ambitoActual = "";
        cuerpo.add(".text");
        cuerpo.add(".globl main");

        for (int i = 0; i < tablaCuad.getCuadruplos().size(); i++) {
            if (resultadoControl.containsKey(i)) {
                agregarEtiqueta(resultadoControl.get(i));
            }
            Cuadruplo cuad = tablaCuad.getCuad(i);
            String op = cuad.getOperacion();
            String arg1 = cuad.getArg1();
            String arg2 = cuad.getArg2();
            String resultado = cuad.getResultado();
            switch (op) {

                case "ETIQ": {
                    ambitoActual = cuad.getArg1();
                    agregarEtiqueta(cuad.getArg1());
                    if (ambitoActual.equals("main")) {
                        generarMove("$fp", "$sp");

                    } else {
                        //funciones
                    }
                    break;
                }
                case "WRITE": {
                    generarWrite(cuad, ambitoActual);
                    break;
                }
                case "READ": {
                    generarRead(cuad, ambitoActual);
                    break;
                }
                case ":=": {
                    if (arg1.contains("@t")) {
                        String tempArg1 = this.getTemp(arg1);
                        if (resultado.contains("@t")) {
                            String tempResultado = this.obtenerTempLibre(resultado);
                            generarMove(tempArg1, tempResultado);
                        } else {
                            generarAsignacionTemp(resultado, tempArg1);
                        }
                    } else if (arg1.matches("[0-9]+")) {
                        String temp = this.getTempDisponible(resultado);
                        generarAsignacionNumero(arg1, temp);
                    } else if (arg1.equals("true") || arg1.equals("false")) {
                        String temp = this.getTempDisponible(resultado);
                        if (arg1.equals("true")) {
                            generarAsignacionNumero("1", temp);
                        } else {
                            generarAsignacionNumero("0", temp);
                        }
                    } else if (arg1.equals("RET")) {
                        generarMove("$v0", "_" + resultado);
                    } else {
                        String temp = this.getTempDisponible(resultado);
                        generarAsignacionVarEnLoad(arg1, temp);
                    }
                    break;
                }
                case "=[]": {
                    String tempAvailable = this.getTempDisponible(resultado);
                    String address = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarObtenerDelArreglo(arg1, Arg2, address, tempAvailable);
                    break;
                }
                case "[]=": {
                    String arrayAdress = this.getTempDisponible(resultado);
                    String indice = this.obtenerTempLibre(arg1);
                    String valor = this.obtenerTempLibre(arg2);
                    generarGuardarEnArreglo(indice, valor, resultado, arrayAdress);
                    break;
                }
                case "+": {
                    String tempAvailable = this.getTempDisponible(resultado);
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarAdd(Arg1, Arg2, tempAvailable);
                    if (!resultado.contains("@t")) {
                        generarAsignacionTemp(resultado, tempAvailable);
                    }
                    break;
                }
                case "-": {
                    String tempAvailable = this.getTempDisponible(resultado);
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarSub(Arg1, Arg2, tempAvailable);
                    break;
                }
                case "*": {
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    String ArgResultado = this.getTempDisponible(resultado);
                    if (resultado.contains("@t")) {
                        generarMul(Arg1, Arg2, ArgResultado);
                    } else {
                        generarMul(Arg1, Arg2, ArgResultado);
                        generarAsignacionTemp(resultado, ArgResultado);
                        this.obtenerTempLibre(resultado);
                    }
                    break;
                }
                case "/": {
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    String ArgResultado = this.getTempDisponible(resultado);

                    if (resultado.contains("@t")) {
                        generarDiv(Arg1, Arg2, ArgResultado);
                    } else {
                        generarDiv(Arg1, Arg2, ArgResultado);
                        generarAsignacionTemp(resultado, ArgResultado);
                        this.obtenerTempLibre(resultado);
                    }
                    break;
                }
                case "GOTO": {
                    //String resultado = resultadoControl.get(Integer.parseInt(arg1));
                    generarB(arg1);
                    break;
                }
                case "if>": {
                    //  String resultado = resultadoControl.get(Integer.parseInt(resultado));
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarBGT(Arg1, Arg2, resultado);
                    break;
                }
                case "if<": {
                    //String resultado = resultadoControl.get(Integer.parseInt(resultado));
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarBLT(Arg1, Arg2, resultado);
                    break;
                }
                case "if>=": {
                    //  String resultado = resultadoControl.get(Integer.parseInt(resultado));
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarBGE(Arg1, Arg2, resultado);
                    break;
                }
                case "if<=": {
                    //String resultado = resultadoControl.get(Integer.parseInt(resultado));
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generarBLE(Arg1, Arg2, resultado);
                    break;
                }
                case "if=": {
                    String Arg1 = this.obtenerTempLibre(arg1);
                    String Arg2 = this.obtenerTempLibre(arg2);
                    generateBE(Arg1, Arg2, resultado);
                    break;
                }
                case "CALL": {
                    generateJal(arg1);
                    paramCount = 0;
                    break;
                }
                case "PARAM": {
                    generateParam(arg1, paramCount);
                    paramCount++;
                    break;
                }
            }
            /*if (i==tc.getSize()-1 && op.equals("GOTO")) {
                String resultado = resultadoControl.get(Integer.parseInt(arg1));
                cft.addresultado(resultado);
            }*/
        }
        cuerpo.add("li $v0, 10");
        cuerpo.add("syscall");
    }

    public void print() {
        for (String instruccion : data) {
            System.out.println(instruccion);
        }
        for (String instruccion : cuerpo) {
            System.out.println(instruccion);
        }

    }

    private String getTemp(String arg1) {
        return tempEquivalente.get(arg1);
    }
}
