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
public class SemanticParser {

    static String ambitoActual;
    static TablaSimbolos ts = new TablaSimbolos();
    static int offset;
    static String tempType = "";
    static ArrayList<Element> nodosHoja = new ArrayList();
    static String tipoActual = "";
    static String tipoFuncion = "";
    static int numErrores = 0;
    static boolean inAFunction = false;
    static boolean debug = false;

    public static TablaSimbolos llenarTablaSimbolos(Element nodoPadre) throws Exception {
        ambitoActual = "main";
        numErrores = 0;
        recorrerArbol(nodoPadre, "0", "0");
        if (numErrores > 0) {
            System.err.println("------------------------------------------------------------------------");
            String message = "Se encontraron %d error(es), abortando compilación. \n";
            message = String.format(message, numErrores);
            System.err.println(message);
        } else {
            System.err.println("------------------------------------------------------------------------");
            Thread.sleep(1000);
            ts.toString();
        }
        return ts;
    }

    private static void recorrerArbol(Element nodoPadre, String Linea, String Columna) throws Exception {
        NodeList hijos = nodoPadre.getChildNodes();

        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            if (debug) {
                System.out.println("RecorrerArbol - " + nodeName);
            }
            switch (nodeName) {
                case "ReadStatement": {
                    tipoActual = "";
                    recorrerArbol(nodo, Linea, Columna);
                    if (tipoActual.equals("boolean") || tipoActual.startsWith("Array")) {
                        String tipo = tipoActual;
                        tipoActual = "[string, integer, char]";
                        Element arg = (Element) nodo.getFirstChild();
                        Linea = arg.getAttribute("Line");
                        Columna = arg.getAttribute("Column");
                        throwIncompatibleTypeError(Linea, Columna, tipo);
                        tipoActual = tipo;
                    }
                    break;
                }
                case "WriteStatement": {
                    tipoActual = "";
                    NodeList lista = nodo.getChildNodes();
                    if (lista.getLength() > 1) {
                        recorrerArbol(nodo, Linea, Columna);
                        if (tipoActual.equals("boolean") || tipoActual.startsWith("Array")) {
                            String tipo = tipoActual;
                            tipoActual = "[string, integer, char]";
                            Element arg = (Element) nodo.getFirstChild();
                            Linea = arg.getAttribute("Line");
                            Columna = arg.getAttribute("Column");
                            throwIncompatibleTypeError(Linea, Columna, tipo);
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
                        ts.Add(S);
                        offset += size;
                    }
                    break;
                }
                case "inlineArg": {
                    String type = ((Element) nodo.getLastChild()).getAttribute("Value");
                    String strSize = ((Element) nodo.getLastChild()).getAttribute("Size");
                    String isPointer = ((Element) nodo.getLastChild()).getAttribute("isPointer");

                    int size = Integer.parseInt(strSize.isEmpty() ? "0" : strSize);
                    NodeList idList = nodo.getElementsByTagName("ID");
                    for (int j = 0; j < idList.getLength(); j++) {
                        if (tempType.isEmpty()) {
                            tempType += type;
                        } else {
                            tempType += "X" + type;
                        }
                        String ID = ((Element) idList.item(j)).getAttribute("Value");
                        Simbolo S = new Simbolo(ID, null, type, ambitoActual, false, false, true, offset);
                        if (isPointer.equals("true")) {
                            S.setByRef(true);
                        } else {
                            S.setByRef(false);
                        }
                        ts.Add(S);
                        offset += size;
                    }
                    break;
                }
                case "ProcedureDeclaration": {
                    String ID = nodo.getAttribute("ID");
                    Simbolo S = new Simbolo(ID, null, "void -> void", "main", false, true, false, offset);
                    int indice = ts.Add(S);
                    ambitoActual = nodo.getAttribute("ID");
                    int backupOffset = offset;
                    offset = 0;
                    inAFunction = true;
                    recorrerArbol(nodo, Linea, Columna);
                    inAFunction = false;
                    if (!tempType.isEmpty()) {
                        tempType += " -> void";
                        S.setTipo(tempType);
                        ts.replaceNode(S, indice);
                    }
                    tempType = "";
                    ambitoActual = "main";
                    offset = backupOffset;
                    break;
                }
                case "FunctionDeclaration": {
                    String ID = nodo.getAttribute("ID");
                    String type = nodo.getAttribute("Type");
                    Simbolo S = new Simbolo(ID, null, type, "null", false, true, false, offset);
                    int indice = ts.Add(S);
                    int backupOffset = offset;
                    offset = 0;
                    ambitoActual = nodo.getAttribute("ID");
                    inAFunction = true;
                    recorrerArbol(nodo, Linea, Columna);
                    inAFunction = false;
                    if (!tempType.isEmpty()) {
                        tempType += " -> " + type;
                        S.setTipo(tempType);
                    } else {
                        S.setTipo("void -> " + type);
                    }
                    ts.replaceNode(S, indice);
                    tempType = "";
                    ambitoActual = "main";
                    offset = backupOffset;
                    break;
                }
                case "Literal": {
                    String type = nodo.getAttribute("Type");
                    Linea = nodo.getAttribute("Line");
                    Columna = nodo.getAttribute("Column");
                    if (!tipoActual.isEmpty() && !tipoActual.equals(type)) {
                        throwIncompatibleTypeError(Linea, Columna, type);
                    }
                    break;
                }
                case "Assignment": {
                    Element IdNode = (Element) nodo.getFirstChild();
                    String IdValex = IdNode.getAttribute("Value");
                    Simbolo S = ts.getVariable(IdValex, ambitoActual);
                    if (!ambitoActual.equals("main") && S == null) {
                        S = ts.getFunction(IdValex);
                    }
                    if (S == null) {
                        S = ts.getVariable(IdValex, "main");
                    }
                    Linea = IdNode.getAttribute("Line");
                    Columna = IdNode.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(Linea, Columna, IdValex);
                    }
                    tipoActual = "";
                    recorrerArbol(nodo, Linea, Columna);
                    tipoActual = "";

                    break;
                }
                case "ID": {
                    String idValex = nodo.getAttribute("Value");
                    String parentName = nodo.getParentNode().getNodeName();
                    boolean programIsParent = parentName.equals("Program");
                    Linea = nodo.getAttribute("Line");
                    Columna = nodo.getAttribute("Column");
                    if (programIsParent) {
                        recorrerArbol(nodo, nodo.getAttribute("Line"), nodo.getAttribute("Column"));
                        break;
                    }

                    Simbolo S = ts.getVariable(idValex, ambitoActual);

                    if (S == null) {
                        S = ts.getVariable(idValex, "main");
                    }

                    if (inAFunction && tipoActual.isEmpty() && S == null) {
                        S = ts.getFunction(idValex);
                        tipoActual = S.getTipo();
                        Element parent = (Element) nodo.getParentNode();
                        parent.setAttribute("Return", "true");
                    }

                    if (S == null) {
                        throwNotFoundError(Linea, Columna, idValex);
                    }

                    boolean isSameType = S.getTipo().equals(tipoActual);
                    if (!tipoActual.isEmpty() && !isSameType) {
                        String currentType = S.getTipo().split("\\.")[0];
                        throwIncompatibleTypeError(Linea, Columna, currentType);
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
                    Linea = functionId.getAttribute("Line");
                    Columna = functionId.getAttribute("Column");
                    Simbolo S = ts.getFunction(id);
                    String tipoRetorno = "";
                    if (S == null) {
                        throwNotFoundError(Linea, Columna, id);

                    }
                    tipoRetorno = S.getTipo().split(" -> ")[1];

                    if (nodo.getChildNodes().getLength() > 1) {
                        comprobarFuncion((Element) nodo.getLastChild());
                        tipoFuncion = tipoFuncion + " -> " + tipoRetorno;
                    } else {
                        tipoFuncion = "void -> " + tipoRetorno;
                    }
                    if (!tipoActual.isEmpty() && !tipoActual.equals(tipoRetorno)) {
                        throwIncompatibleTypeError(Linea, Columna, tipoRetorno);
                    }
                    if (!tipoFuncion.equals(S.getTipo())) {
                        throwFunctionArgsError(Linea, Columna, id);
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
                        String tipoActualBKP = tipoActual;
                        tipoActual = "";
                        comprobarTipos(nodo);
                        tipoActual = tipoActualBKP;
                    } else if (tipoActual.isEmpty()) {
                        comprobarTipos(nodo);
                        tipoActual = "";
                    } else {
                        throwIncompatibleTypeError(Linea, Columna, "boolean");
                    }

                    break;
                }
                case "AND":
                case "OR":
                case "NOT": {
                    if (!tipoActual.isEmpty() && tipoActual.equals("boolean")) {
                        recorrerArbol(nodo, Linea, Columna);
                        tipoActual = "";
                    } else if (tipoActual.isEmpty()) {
                        recorrerArbol(nodo, Linea, Columna);
                    } else {
                        throwIncompatibleTypeError(Linea, Columna, "boolean");
                    }
                    break;
                }
                case "IfStatement": {
                    Linea = nodo.getAttribute("Line");
                    Columna = nodo.getAttribute("Column");
                    String tipoBKP = tipoActual;
                    tipoActual = "boolean";
                    recorrerArbol(nodo, Linea, Columna);
                    tipoActual = tipoBKP;
                    break;
                }
                case "ARRAY": {
                    String id = nodo.getAttribute("Value");
                    Simbolo S = ts.getVariable(id, ambitoActual);
                    Linea = nodo.getAttribute("Line");
                    Columna = nodo.getAttribute("Column");

                    if (S == null) {
                        throwNotFoundError(Linea, Columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(Linea, Columna);
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
                            throwIncompatibleTypeError(Linea, Columna, tipo);
                        }

                    }
                    break;
                }
                default: {
                    recorrerArbol(nodo, Linea, Columna);
                    break;
                }
            }
        }

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
                    Simbolo S = ts.getVariable(id, ambitoActual);
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
                    Simbolo S = ts.getVariable(id, ambitoActual);
                    String Linea = nodo.getAttribute("Line");
                    String Columna = nodo.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(Linea, Columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(Linea, Columna);
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
                            throwIncompatibleTypeError(Linea, Columna, tipo);
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
                    Simbolo S = ts.getVariable(id, ambitoActual);
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
                    Simbolo S = ts.getVariable(id, ambitoActual);
                    String Linea = nodo.getAttribute("Line");
                    String Columna = nodo.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(Linea, Columna, id);
                    } else if (!S.getTipo().startsWith("Array")) {
                        throwIlegalExpresionError(Linea, Columna);
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
                            throwIncompatibleTypeError(Linea, Columna, tipo);
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
                    Simbolo S = ts.getVariable(id, ambitoActual);
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
                    String tipoActualBKP = tipoActual;
                    tipoActual = "";
                    comprobarTipos(nodo);
                    tipoActual = tipoActualBKP;
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
                    String tipoActualBKP = tipoActual;
                    tipoActual = "boolean";
                    recorrerArbol(nodo, "0", "0");
                    tipoActual = tipoActualBKP;
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
                    Simbolo S = ts.getFunction(id);
                    String tipoRetorno = "";
                    String Linea = functionId.getAttribute("Line");
                    String Columna = functionId.getAttribute("Column");
                    if (S == null) {
                        throwNotFoundError(Linea, Columna, id);
                    }
                    tipoRetorno = S.getTipo().split(" -> ")[1];

                    if (nodo.getChildNodes().getLength() > 1) {
                        comprobarFuncion((Element) nodo.getLastChild());
                        tipoFuncion = tipoFuncion + " -> " + tipoRetorno;
                    } else {
                        tipoFuncion = "void -> " + tipoRetorno;
                    }
                    if (!tipoActual.equals(tipoRetorno)) {
                        throwIncompatibleTypeError(Linea, Columna, tipoRetorno);
                    }
                    if (!tipoFuncion.equals(S.getTipo())) {
                        throwFunctionArgsError(Linea, Columna, id);
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

    private static void throwNotFoundError(String Linea, String Columna, String ID) throws Exception {
        String errorMessage = "(%s,%s) Error: Identificador no encontrado '%s'";
        errorMessage = String.format(errorMessage, Linea, Columna, ID);
        if (debug) {
            throw new Exception(errorMessage);
        } else {
            System.err.println(errorMessage);
        }
        numErrores++;
    }

    private static void throwIncompatibleTypeError(String Linea, String Columna, String tipo) throws Exception {
        String errorMessage = "";
        if (tipo.equals("void")) {
            errorMessage = "(%s,%s) Error: Asignacion invalida, los procedimientos no retornan valor";
        } else {
            errorMessage = "(%s,%s) Error: Tipos incompatibles, se esperaba '%s' pero se encontro '%s'";
        }

        errorMessage = String.format(errorMessage, Linea, Columna, tipoActual, tipo);

        if (debug) {
            throw new Exception(errorMessage);
        } else {
            System.err.println(errorMessage);
        }
        numErrores++;
    }

    private static void throwFunctionArgsError(String Linea, String Columna, String id) throws Exception {
        String errorMessage = "(%s,%s) Error: Función no encontrado '%s' con los parámetros proporcionado";
        errorMessage = String.format(errorMessage, Linea, Columna, id);
        if (debug) {
            throw new Exception(errorMessage);
        } else {
            System.err.println(errorMessage);
        }
        numErrores++;
    }

    private static void throwIlegalExpresionError(String Linea, String Columna) throws Exception {
        String errorMessage = "(%s,%s) Error: Expresión Ilegal";
        errorMessage = String.format(errorMessage, Linea, Columna);
        if (debug) {
            throw new Exception(errorMessage);
        } else {
            System.err.println(errorMessage);
        }
        numErrores++;
    }

}
