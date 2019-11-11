/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 */
public class AnalizadorTipo {

    static String ambitoActual;
    static String tempTipo = "";
    static String tipoActual = "";
    static String tipoFuncion = "";
    static ArrayList<Element> nodosHoja = new ArrayList();
    static int numErrores = 0;
    static boolean inAFunction = false;
    static boolean debug = false;
    static TablaSimbolos tabla = new TablaSimbolos();
    static int offset;

    private static void recorrerArbol(Element nodoPadre, String linea, String columna) throws Exception {
        NodeList hijos = nodoPadre.getChildNodes();

        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            switch (nodeName) {
                case "ReadStatement": {
                    tipoActual = "";
                    recorrerArbol(nodo, linea, columna);
                    if (tipoActual.equals("boolean") || tipoActual.startsWith("Array")) {
                        String tipo = tipoActual;
                        tipoActual = "[string, integer, char]";
                        Element arg = (Element) nodo.getFirstChild();
                        linea = arg.getAttribute("Line");
                        columna = arg.getAttribute("Column");
                        throwIncompatibleTypeError(linea, columna, tipo);
                        tipoActual = tipo;
                    }
                    break;
                }
                case "WriteStatement": {
                    tipoActual = "";
                    NodeList lista = nodo.getChildNodes();
                    if (lista.getLength() > 1) {
                        recorrerArbol(nodo, linea, columna);
                        if (tipoActual.equals("boolean") || tipoActual.startsWith("Array")) {
                            String tipo = tipoActual;
                            tipoActual = "[string, integer, char]";
                            Element arg = (Element) nodo.getFirstChild();
                            linea = arg.getAttribute("Line");
                            columna = arg.getAttribute("Column");
                            throwIncompatibleTypeError(linea, columna, tipo);
                            tipoActual = tipo;
                        }
                    }

                    break;
                }
                case "VarDeclaration": {
                    String type = ((Element) nodo.getLastChild()).getAttribute("Value");
                    int size = Integer.parseInt(
                            ((Element) nodo.getLastChild()).getAttribute("Size")
                    );
                    NodeList idList = nodo.getElementsByTagName("ID");
                    for (int j = 0; j < idList.getLength(); j++) {
                        String ID = ((Element) idList.item(j)).getAttribute("Value");
                        Simbolo S = new Simbolo(ID, null, type, ambitoActual, true, false, false, offset);
                        tabla.add(S);
                        offset += size;
                    }
                    break;
                }
                case "inlineArg": {
                    String type = ((Element) nodo.getLastChild()).getAttribute("Value");
                    String strSize = ((Element) nodo.getLastChild()).getAttribute("Size");

                    int size = Integer.parseInt(strSize.isEmpty() ? "0" : strSize);
                    NodeList idList = nodo.getElementsByTagName("ID");
                    for (int j = 0; j < idList.getLength(); j++) {
                        if (tempTipo.isEmpty()) {
                            tempTipo += type;
                        } else {
                            tempTipo += "X" + type;
                        }
                        String ID = ((Element) idList.item(j)).getAttribute("Value");
                        Simbolo S = new Simbolo(ID, null, type, ambitoActual, false, false, true, offset);

                        tabla.add(S);
                        offset += size;
                    }
                    break;
                }
                case "ProcedureDeclaration": {
                    String ID = nodo.getAttribute("ID");
                    Simbolo S = new Simbolo(ID, null, "void -> void", "main", false, true, false, offset);
                    int indice = tabla.add(S);
                    ambitoActual = nodo.getAttribute("ID");
                    int backupOffset = offset;
                    offset = 0;
                    inAFunction = true;
                    recorrerArbol(nodo, linea, columna);
                    inAFunction = false;
                    if (!tempTipo.isEmpty()) {
                        tempTipo += " -> void";
                        S.setTipo(tempTipo);
                        tabla.replaceNode(S, indice);
                    }
                    tempTipo = "";
                    ambitoActual = "main";
                    offset = backupOffset;
                    break;
                }
                case "FunctionDeclaration": {
                    String ID = nodo.getAttribute("ID");
                    String type = nodo.getAttribute("Type");
                    Simbolo S = new Simbolo(ID, null, type, ambitoActual, false, true, false, offset);
                    int indice = tabla.add(S);
                    int backupOffset = offset;
                    offset = 0;
                    ambitoActual = nodo.getAttribute("ID");
                    inAFunction = true;
                    recorrerArbol(nodo, linea, columna);
                    inAFunction = false;
                    if (!tempTipo.isEmpty()) {
                        tempTipo += " -> " + type;
                        S.setTipo(tempTipo);
                    } else {
                        S.setTipo("void -> " + type);
                    }
                    tabla.replaceNode(S, indice);
                    tempTipo = "";
                    ambitoActual = "main";
                    offset = backupOffset;
                    break;
                }
                case "Literal": {
                    String type = nodo.getAttribute("Type");
                    linea = nodo.getAttribute("Line");
                    columna = nodo.getAttribute("Column");
                    if (!tipoActual.isEmpty() && !tipoActual.equals(type)) {
                        throwIncompatibleTypeError(linea, columna, type);
                    }
                    break;
                }
                case "Assignment": {
                    Element IdNode = (Element) nodo.getFirstChild();
                    String IdValex = IdNode.getAttribute("Value");
                    Simbolo S = tabla.getVariable(IdValex, ambitoActual);
                    if (!ambitoActual.equals("main") && S == null) {
                        S = tabla.getFunction(IdValex);
                    }
                    if (S == null) {
                        S = tabla.getVariable(IdValex, "main");
                    }
                    linea = IdNode.getAttribute("Line");
                    columna = IdNode.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(linea, columna, IdValex);
                    }
                    tipoActual = "";
                    recorrerArbol(nodo, linea, columna);
                    tipoActual = "";

                    break;
                }
                case "ID": {
                    String idValex = nodo.getAttribute("Value");
                    String parentName = nodo.getParentNode().getNodeName();
                    boolean programIsParent = parentName.equals("Program");
                    linea = nodo.getAttribute("Line");
                    columna = nodo.getAttribute("Column");
                    if (programIsParent) {
                        recorrerArbol(nodo, nodo.getAttribute("Line"), nodo.getAttribute("Column"));
                        break;
                    }

                    Simbolo S = tabla.getVariable(idValex, ambitoActual);

                    if (S == null) {
                        S = tabla.getVariable(idValex, "main");
                    }

                    if (inAFunction && tipoActual.isEmpty() && S == null) {
                        S = tabla.getFunction(idValex);
                        tipoActual = S.getTipo();
                        Element parent = (Element) nodo.getParentNode();
                        parent.setAttribute("Return", "true");
                    }

                    if (S == null) {
                        throwNotFoundError(linea, columna, idValex);
                    }

                    boolean isSameType = S.getTipo().equals(tipoActual);
                    if (!tipoActual.isEmpty() && !isSameType) {
                        String currentType = S.getTipo().split("\\.")[0];
                        throwIncompatibleTypeError(linea, columna, currentType);
                    } else {
                        if (tipoActual.isEmpty()) {
                            tipoActual = S.getTipo();
                        }
                    }
                    recorrerArbol(nodo, nodo.getAttribute("Line"), nodo.getAttribute("Column"));
                    break;
                }
                case "FunctionCall": {
                    String tipoBKP = tipoFuncion;
                    tipoFuncion = "";
                    Element functionId = (Element) nodo.getFirstChild();
                    String id = functionId.getAttribute("Value");
                    linea = functionId.getAttribute("Line");
                    columna = functionId.getAttribute("Column");
                    Simbolo S = tabla.getFunction(id);
                    String tipoRetorno = "";
                    if (S == null) {
                        throwNotFoundError(linea, columna, id);
                    }
                    tipoRetorno = S.getTipo().split(" -> ")[1];

                    if (nodo.getChildNodes().getLength() > 1) {
                        comprobarFuncion((Element) nodo.getLastChild());
                        tipoFuncion = tipoFuncion + " -> " + tipoRetorno;
                    } else {
                        tipoFuncion = "void -> " + tipoRetorno;
                    }
                    if (!tipoActual.isEmpty() && !tipoActual.equals(tipoRetorno)) {
                        throwIncompatibleTypeError(linea, columna, tipoRetorno);
                    }
                    if (!tipoFuncion.equals(S.getTipo())) {
                        throwFunctionArgsError(linea, columna, id);
                    }
                    tipoFuncion = tipoBKP;

                    break;
                }
                case "GreaterThan":
                case "LessThan":
                case "Equals":
                case "LessOrEqual":
                case "GreaterOrEqual":
                case "Different": {
                    if (!tipoActual.isEmpty() && tipoActual.equals("boolean")) {
                        String tipoActualTemp = tipoActual;
                        tipoActual = "";
                        comprobarTipos(nodo);
                        tipoActual = tipoActualTemp;
                    } else if (tipoActual.isEmpty()) {
                        comprobarTipos(nodo);
                        tipoActual = "";
                    } else {
                        throwIncompatibleTypeError(linea, columna, "boolean");
                    }

                    break;
                }
                case "AND":
                case "OR":
                case "NOT": {
                    if (!tipoActual.isEmpty() && tipoActual.equals("boolean")) {
                        recorrerArbol(nodo, linea, columna);
                        tipoActual = "";
                    } else if (tipoActual.isEmpty()) {
                        recorrerArbol(nodo, linea, columna);
                    } else {
                        throwIncompatibleTypeError(linea, columna, "boolean");
                    }
                    break;
                }
                case "IfStatement": {
                    linea = nodo.getAttribute("Line");
                    columna = nodo.getAttribute("Column");
                    String tipoBKP = tipoActual;
                    tipoActual = "boolean";
                    recorrerArbol(nodo, linea, columna);
                    tipoActual = tipoBKP;
                    break;
                }
                case "ARRAY": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    linea = nodo.getAttribute("Line");
                    columna = nodo.getAttribute("Column");

                    if (S == null) {
                        throwNotFoundError(linea, columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(linea, columna);
                    } else {
                        String tipo = S.getTipo().split("\\.")[1];
                        String tipoBKP = tipoActual;
                        if (tipoActual.isEmpty()) {
                            System.out.println("1");
                            tipoActual = "integer";
                            comprobarTipos(nodo);
                            tipoActual = tipo;
                        } else if (tipoActual.equals(tipo)) {
                            System.out.println("2");
                            tipoActual = "integer";
                            comprobarTipos(nodo);
                            tipoActual = tipoBKP;
                        } else {
                            throwIncompatibleTypeError(linea, columna, tipo);
                        }

                    }
                    break;
                }
                default: {
                    recorrerArbol(nodo, linea, columna);
                    break;
                }
            }
        }

    }

    public static TablaSimbolos llenarTablaSimbolos(Element nodoPadre) throws Exception {
        ambitoActual = "main";
        numErrores = 0;
        recorrerArbol(nodoPadre, "0", "0");
        if (numErrores > 0) {
            String message = "Abortando compilación, cantidad de errores encontrados %d. \n";
            message = String.format(message, numErrores);
            System.err.println(message);
        } else {
            Thread.sleep(1000);
            tabla.toString();
        }
        return tabla;
    }

    private static void comprobarTipos(Element nodoPadre) throws Exception {

        NodeList hijos = nodoPadre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            if (debug) {
                System.out.println("Comprobar Tipos - " + nodeName);
            }
            switch (nodeName) {
                case "Literal": {
                    String type = nodo.getAttribute("Type");
                    if (tipoActual.isEmpty()) {
                        tipoActual = type;
                    }
                    if (!tipoActual.equals(type)) {
                        String Line = nodo.getAttribute("Line");
                        String Column = nodo.getAttribute("Column");
                        throwIncompatibleTypeError(Line, Column, type);
                    }
                    break;
                }
                case "ID": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    if (S == null) {
                        String Line = nodo.getAttribute("Line");
                        String Column = nodo.getAttribute("Column");
                        throwNotFoundError(Line, Column, id);

                    } else {
                        boolean isSameType = S.getTipo().equals(tipoActual);
                        if (!tipoActual.isEmpty() && !isSameType) {

                            String Line = nodo.getAttribute("Line");
                            String Column = nodo.getAttribute("Column");
                            String currentType = S.getTipo().split("\\.")[0];
                            throwIncompatibleTypeError(Line, Column, currentType);

                        } else {
                            tipoActual = S.getTipo();
                        }
                    }
                    break;
                }
                case "Minus":
                case "Times":
                case "Div": {
                    comprobarArit(nodo);
                    break;
                }
                case "Plus": {
                    comprobarTipos(nodo);
                    break;
                }
                case "ARRAY": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    String linea = nodo.getAttribute("Line");
                    String columna = nodo.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(linea, columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(linea, columna);
                    } else {
                        String tipo = S.getTipo().split("\\.")[1];
                        String tipoBKP = tipoActual;
                        if (tipoActual.isEmpty()) {
                            tipoActual = "integer";
                            comprobarTipos(nodo);
                        } else if (tipoActual.equals(tipo)) {
                            tipoActual = "integer";
                            comprobarTipos(nodo);

                        } else {
                            throwIncompatibleTypeError(linea, columna, tipo);
                        }
                        tipoActual = tipoBKP;
                    }
                    break;
                }
                default: {
                    comprobarTipos(nodo);
                }
            }

        }
    }

    private static void comprobarArit(Element nodoPadre) throws Exception {
        NodeList hijos = nodoPadre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            if (debug) {
                System.out.println("Comprobar Arit - " + nodeName);
            }
            switch (nodeName) {
                case "Literal": {
                    String type = nodo.getAttribute("Type");
                    if (tipoActual.isEmpty()) {
                        tipoActual = type;
                    }
                    boolean isInteger = type.equals("integer");
                    if (!isInteger) {
                        String Line = nodo.getAttribute("Line");
                        String Column = nodo.getAttribute("Column");
                        throwIncompatibleTypeError(Line, Column, type);
                    }
                    break;
                }
                case "ID": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    if (S == null) {
                        String Line = nodo.getAttribute("Line");
                        String Column = nodo.getAttribute("Column");
                        throwNotFoundError(Line, Column, id);

                    } else {
                        boolean isInteger = S.getTipo().equals("integer");
                        if (!tipoActual.isEmpty() && !isInteger) {

                            String Line = nodo.getAttribute("Line");
                            String Column = nodo.getAttribute("Column");
                            String currentType = S.getTipo().split("\\.")[0];
                            throwIncompatibleTypeError(Line, Column, currentType);

                        } else {
                            tipoActual = "integer";
                        }
                    }
                    break;
                }
                case "ARRAY": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    String linea = nodo.getAttribute("Line");
                    String columna = nodo.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(linea, columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(linea, columna);
                    } else {
                        String tipo = S.getTipo().split("\\.")[1];
                        String tipoBKP = tipoActual;
                        boolean isInteger = tipo.equals("integer");
                        if (tipoActual.isEmpty()) {
                            tipoActual = "integer";
                            comprobarTipos(nodo);
                        } else if (isInteger) {
                            tipoActual = "integer";
                            comprobarTipos(nodo);

                        } else {
                            throwIncompatibleTypeError(linea, columna, tipo);
                        }
                        tipoActual = tipoBKP;
                    }
                    break;
                }
                default: {
                    comprobarArit(nodo);
                    break;
                }
            }

        }
    }

    private static void comprobarFuncion(Element nodoPadre) throws Exception {
        NodeList hijos = nodoPadre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            switch (nodeName) {
                case "ID": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = tabla.getVariable(id, ambitoActual);
                    if (S == null) {
                        String Line = nodo.getAttribute("Line");
                        String Column = nodo.getAttribute("Column");
                        throwNotFoundError(Line, Column, id);

                    }
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += S.getTipo();
                    } else {
                        tipoFuncion += "X" + S.getTipo();
                    }
                    break;
                }
                case "Literal": {
                    String tipo = nodo.getAttribute("Type");
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += tipo;
                    } else {
                        tipoFuncion += "X" + tipo;
                    }
                    break;
                }
                case "Minus":
                case "Times":
                case "Div": {
                    String tipoBKP = tipoActual;
                    tipoActual = "integer";
                    comprobarTipos(nodo);
                    tipoActual = tipoBKP;
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += "integer";
                    } else {
                        tipoFuncion += "Xinteger";
                    }
                    break;
                }
                case "Plus": {
                    String tipoBKP = tipoActual;
                    tipoActual = "";
                    comprobarTipos(nodo);

                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += tipoActual;
                    } else {
                        tipoFuncion += "X" + tipoActual;
                    }
                    tipoActual = tipoBKP;
                    break;
                }
                case "GreaterThan":
                case "LessThan":
                case "Equals":
                case "LessOrEqual":
                case "GreaterOrEqual":
                case "Different": {
                    String tipoActualTemp = tipoActual;
                    tipoActual = "";
                    comprobarTipos(nodo);
                    tipoActual = tipoActualTemp;
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += "boolean";
                    } else {
                        tipoFuncion += "Xboolean";
                    }
                    break;
                }
                case "AND":
                case "OR":
                case "NOT": {
                    String tipoActualTemp = tipoActual;
                    tipoActual = "boolean";
                    recorrerArbol(nodo, "0", "0");
                    tipoActual = tipoActualTemp;
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += "boolean";
                    } else {
                        tipoFuncion += "Xboolean";
                    }
                    break;
                }
                case "FunctionCall": {
                    String tipoBKP = tipoFuncion;
                    tipoFuncion = "";
                    Element functionId = (Element) nodo.getFirstChild();
                    String id = functionId.getAttribute("Value");
                    Simbolo S = tabla.getFunction(id);
                    String tipoRetorno = "";
                    String linea = functionId.getAttribute("Line");
                    String columna = functionId.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(linea, columna, id);
                    }
                    tipoRetorno = S.getTipo().split(" -> ")[1];

                    if (nodo.getChildNodes().getLength() > 1) {
                        comprobarFuncion((Element) nodo.getLastChild());
                        tipoFuncion = tipoFuncion + " -> " + tipoRetorno;
                    } else {
                        tipoFuncion = "void -> " + tipoRetorno;
                    }
                    if (!tipoActual.equals(tipoRetorno)) {
                        throwIncompatibleTypeError(linea, columna, tipoRetorno);
                    }
                    if (!tipoFuncion.equals(S.getTipo())) {
                        throwFunctionArgsError(linea, columna, id);
                    }
                    tipoFuncion = tipoBKP;
                    if (tipoFuncion.isEmpty()) {
                        tipoFuncion += tipoRetorno;
                    } else {
                        tipoFuncion += "X" + tipoRetorno;
                    }
                }
                default: {
                    comprobarFuncion(nodo);
                }
            }
        }

    }

    private static void throwNotFoundError(String linea, String columna, String ID) throws Exception {
        String error = " Error: Identificador no encontrado '%s' line:%s,col:%s";
        error = String.format(error, ID, linea, columna);
        if (debug) {
            throw new Exception(error);
        } else {
            System.err.println(error);
        }
        numErrores++;
    }

    private static void throwIncompatibleTypeError(String linea, String columna, String tipo) throws Exception {
        String error = "";
        if (tipo.equals("void")) {
            error = "Error: Asignacion invalida, los procedimientos no retornan valor line:%s,col:%s";
        } else {
            error = "Error: Tipos incompatibles, se esperaba '%s' pero se encontro '%s' line:%s,col:%s";
        }

        error = String.format(error, tipoActual, tipo, linea, columna);
        System.err.println(error);
        numErrores++;
    }

    private static void throwFunctionArgsError(String linea, String columna, String id) throws Exception {
        String error = " Error: Función no encontrado '%s' con los parámetros proporcionado line:%s,col:%s";
        error = String.format(error, id, linea, columna);

        System.err.println(error);

        numErrores++;
    }

    private static void throwIlegalExpresionError(String linea, String columna) throws Exception {
        String error = " Error: Expresión Ilegal line:%s,col:%s)";
        error = String.format(error, linea, columna);
        System.err.println(error);
        numErrores++;
    }

}
