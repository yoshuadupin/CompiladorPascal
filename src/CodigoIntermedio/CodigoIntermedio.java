/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CodigoIntermedio;

import Analizadores.Simbolo;
import Analizadores.TablaSimbolos;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author UNICOMER
 */
public class CodigoIntermedio {

    static int tempCont = 0;
    static int etiqCont = 0;
    TablaCuadruplos cuadruplos = new TablaCuadruplos();
    TablaSimbolos tabla = new TablaSimbolos();
    String opracionActual = "";
    String ambitoActual = "main";

    public CodigoIntermedio(TablaSimbolos tabla) {
        this.tabla = tabla;
    }

    public void recorrer(Element rootNode) throws Exception {
        if (rootNode.getParentNode().getNodeName().equals("#document")) {
            rootNode.setAttribute("Siguiente", etiqNueva());
        }
        NodeList hijos = rootNode.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();
            String siguiente = rootNode.getAttribute("Siguiente");
            nodo.setAttribute("Siguiente", siguiente);
            switch (nodeName) {

                case "ProcedureDeclaration": {
                    cuadruplos.genEtiq(nodo.getAttribute("ID"));
                    recorrer(nodo);
                    cuadruplos.gen("RET", "", "", "");
                    break;
                }
                case "FunctionDeclaration": {
                    cuadruplos.genEtiq(nodo.getAttribute("ID"));
                    this.recorrer(nodo);
                    break;
                }
                case "Body": {
                    if (nodo.getParentNode().getNodeName().equals("Block")) {
                        cuadruplos.genEtiq("main");
                    }
                    recorrer(nodo);

                    break;
                }
                case "FunctionCall": {
                    cuadruploFuncCall(nodo);
                    break;
                }
                case "IfStatement": {

                    cuadIf(nodo);
                    break;
                }
                case "WhileStatement": {

                    cuadWhile(nodo);
                    break;
                }
                case "RepeatStatement": {
                    cuadRepeat(nodo);
                    break;
                }
                case "ForStatement": {
                    cuadFor(nodo);
                    break;
                }
                case "ReadStatement": {
                    Element arg = (Element) nodo.getFirstChild();
                    String argName = arg.getNodeName();
                    switch (argName) {
                        case "ID": {
                            String argValue = arg.getAttribute("Value");
                            cuadruplos.gen("READ", "", "", argValue);
                            break;
                        }
                        case "ARRAY": {
                            cuadArreglo(arg);
                            String temp = this.getTemp();
                            String argValue = arg.getAttribute("Value");
                            cuadruplos.gen("READ[]", temp, "", argValue);
                            break;
                        }
                    }

                    break;
                }
                case "WriteStatement": {
                    NodeList lista = nodo.getChildNodes();
                    Element arg1 = (Element) lista.item(0);
                    String arg1Value = arg1.getAttribute("Value");
                    Element arg2 = null;
                    String arg2Name = "";
                    if (lista.getLength() > 1) {
                        arg2 = (Element) lista.item(1);
                        arg2Name = arg2.getNodeName();
                        String argValue = "";
                        switch (arg2Name) {
                            case "ID": {
                                argValue = arg2.getAttribute("Value");
                                break;
                            }
                            case "ARRAY": {
                                cuadArreglo(arg2);
                                String temp = getTemp();
                                argValue = temp;
                                break;
                            }
                        }
                        cuadruplos.gen("WRITE", arg1Value, "", "");
                        cuadruplos.gen("WRITE", argValue, "", "");
                    } else {
                        cuadruplos.gen("WRITE", arg1Value, "", "");
                    }
                    break;
                }
                case "Assignment": {
                    cuadAsignacion(nodo);
                    break;
                }
                case "AND":
                case "OR":
                case "NOT": {
                    cuadRelacional(nodo);
                    break;
                }
                case "GreaterThan":
                case "GreaterOrEqual":
                case "Equals":
                case "LessOrEqual":
                case "LessThan":
                case "Different": {
                    cuadExpresiones(nodo);
                    break;
                }
                case "Minus":
                case "Times":
                case "Div":
                case "Plus": {
                    cuadAritmetico(nodo);
                    break;
                }
                default: {
                    recorrer(nodo);
                    break;
                }
            }
        }

        if (rootNode.getParentNode().getNodeName().equals("#document")) {
            cuadruplos.genEtiq("$" + rootNode.getAttribute("Siguiente"));
        }

    }

    private void cuadRelacional(Element node) throws Exception {
        String nodeName = node.getNodeName();

        switch (nodeName) {
            case "AND": {
                cuadAnd(node);
                break;
            }
            case "OR": {
                cuadOr(node);
                break;
            }
            case "NOT": {
                cuadNot(node);
                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                cuadExpresiones(node);
                break;
            }

        }
    }

    private void cuadOr(Element nodo) throws Exception {
        Element arg1 = (Element) nodo.getFirstChild();
        Element arg2 = (Element) nodo.getLastChild();

        String arg1Name = arg1.getNodeName();
        String arg2Name = arg2.getNodeName();
        String VerdaderaE = nodo.getAttribute("Verdadera");
        String falsaE = nodo.getAttribute("Falsa");

        arg1.setAttribute("Verdadera", VerdaderaE);
        arg1.setAttribute("Falsa", etiqNueva());

        switch (arg1Name) {
            case "ID": {
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + falsa);
                cuadruplos.genGOTO("$" + verdadera);
                break;
            }
            case "Literal": {
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + falsa);
                cuadruplos.genGOTO("$" + verdadera);
                break;
            }
            case "ARRAY": {
                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + falsa);
                cuadruplos.genGOTO("$" + verdadera);
                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                break;
            }
            default: {
                cuadRelacional(arg1);
                break;
            }

        }
        cuadruplos.genEtiq(arg1.getAttribute("Falsa"));
        arg2.setAttribute("Verdadera", VerdaderaE);
        arg2.setAttribute("Falsa", falsaE);

        switch (arg2Name) {
            case "ID": {
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "Literal": {
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "ARRAY": {
                cuadArreglo(arg2);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                cuadExpresiones(arg2);
                break;
            }
            default: {
                cuadRelacional(arg2);
                break;
            }
        }
    }

    private void cuadAnd(Element nodo) throws Exception {

        Element arg1 = (Element) nodo.getFirstChild();
        Element arg2 = (Element) nodo.getLastChild();

        String arg1Name = arg1.getNodeName();
        String arg2Name = arg2.getNodeName();

        String VerdaderaE = nodo.getAttribute("Verdadera");
        String falsaE = nodo.getAttribute("Falsa");

        arg1.setAttribute("Verdadera", etiqNueva());
        arg1.setAttribute("Falsa", falsaE);
        switch (arg1Name) {
            case "ID": {

                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "Literal": {
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "ARRAY": {
                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);

                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                cuadExpresiones(arg1);
                break;
            }
            default: {
                cuadRelacional(arg1);
                break;
            }

        }

        cuadruplos.genEtiq(arg1.getAttribute("Verdadera"));
        arg2.setAttribute("Verdadera", VerdaderaE);
        arg2.setAttribute("Falsa", falsaE);

        switch (arg2Name) {
            case "ID": {
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "Literal": {

                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "ARRAY": {
                cuadArreglo(arg2);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);

                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                cuadExpresiones(arg2);
                break;
            }
            default: {
                cuadRelacional(arg2);
                break;
            }

        }
    }

    private void cuadFor(Element nodo) throws Exception {
        NodeList lista = nodo.getChildNodes();
        Element assignment = (Element) lista.item(0);
        Element end = (Element) lista.item(1);
        Element body = (Element) lista.item(2);

        cuadAsignacion(assignment);
        Cuadruplo cuad = cuadruplos.getCuad(cuadruplos.cuadruplos.size() - 1);
        String iterator = cuad.resultado;

        String comienzo = etiqNueva();
        cuadruplos.genEtiq("$" + comienzo);
        String siguienteE = nodo.getAttribute("Siguiente");
        String verdaderaE = etiqNueva();
        String siguienteS1 = etiqNueva();
        body.setAttribute("Siguiente", siguienteS1);

        String endName = end.getNodeName();
        switch (endName) {
            case "Literal":
            case "ID": {
                String temp1 = nuevoTemp();
                cuadruplos.gen(":=", iterator, temp1);
                String idValex = end.getAttribute("Value");
                String temp2 = nuevoTemp();
                cuadruplos.gen(":=", idValex, temp2);

                cuadruplos.gen("if<=", temp1, temp2, "$" + verdaderaE);
                cuadruplos.genGOTO("$" + siguienteE);
                break;
            }
            case "ARRAY": {
                cuadArreglo(end);
                String temp = getTemp();
                String temp1 = nuevoTemp();
                cuadruplos.gen(":=", iterator, temp1);

                cuadruplos.gen("if<=", temp1, temp, "$" + verdaderaE);
                cuadruplos.genGOTO("$" + siguienteE);
                break;
            }
            default: {
                cuadAritmetico(end);
                String temp = this.getTemp();
                String temp1 = nuevoTemp();
                cuadruplos.gen(":=", iterator, temp1);

                cuadruplos.gen("if<=", temp1, temp, "$" + verdaderaE);
                cuadruplos.genGOTO("$" + siguienteE);
                break;
            }
        }
        cuadruplos.genEtiq("$" + verdaderaE);

        recorrer(body);
        cuadruplos.genEtiq("$" + siguienteS1);
        String temp1 = nuevoTemp();
        cuadruplos.gen(":=", iterator, temp1);
        String temp2 = nuevoTemp();
        cuadruplos.gen(":=", "1", temp2);

        cuadruplos.gen("+", temp1, temp2, iterator);
        cuadruplos.genGOTO("$" + comienzo);
    }

    private void cuadRepeat(Element nodo) throws Exception {

        Element body = (Element) nodo.getFirstChild();
        Element expression = (Element) nodo.getLastChild();
        String nodeName = expression.getNodeName();

        String verdaderaE = nodo.getAttribute("Siguiente");
        String falsaE = etiqNueva();
        String siguienteS1 = etiqNueva();

        body.setAttribute("Siguiente", siguienteS1);

        cuadruplos.genEtiq(falsaE);
        recorrer(body);
        cuadruplos.genEtiq("$" + siguienteS1);

        expression.setAttribute("Falsa", falsaE);
        expression.setAttribute("Verdadera", verdaderaE);
        switch (nodeName) {
            case "ID": {
                String arg1Value = expression.getAttribute("Value");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);
                break;
            }
            case "Literal": {
                String arg1Value = expression.getAttribute("Value");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);

                break;
            }
            case "ARRAY": {
                cuadArreglo(expression);
                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);
                break;
            }
            default: {
                cuadRelacional(expression);
                break;
            }
        }
    }

    private void cuadWhile(Element nodo) throws Exception {

        Element expression = (Element) nodo.getFirstChild();
        Element body = (Element) nodo.getLastChild();

        String nodeName = expression.getNodeName();

        String comienzo = etiqNueva();
        String verdaderaE = etiqNueva();
        String falsaE = nodo.getAttribute("Siguiente");

        cuadruplos.genEtiq("$" + comienzo);

        switch (nodeName) {
            case "ID": {
                String arg1Value = expression.getAttribute("Value");

                cuadruplos.gen("if=", arg1Value, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);
                break;
            }
            case "Literal": {
                String arg1Value = expression.getAttribute("Value");

                cuadruplos.gen("if=", arg1Value, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);
                break;
            }
            case "ARRAY": {
                cuadArreglo(expression);
                String temp = getTemp();

                cuadruplos.gen("if=", temp, "1", "$" + verdaderaE);
                cuadruplos.genGOTO("$" + falsaE);
                break;
            }
            default: {
                expression.setAttribute("Verdadera", verdaderaE);
                expression.setAttribute("Falsa", falsaE);
                cuadRelacional(expression);
                break;
            }
        }
        cuadruplos.genEtiq("$" + verdaderaE);
        body.setAttribute("Siguiente", comienzo);

        recorrer(body);

        cuadruplos.genGOTO("$" + comienzo);
    }

    private void cuadIf(Element nodo) throws Exception {

        NodeList lista = nodo.getChildNodes();
        Element expression = (Element) lista.item(0);
        Element body = (Element) lista.item(1);
        Element elseIf = null;
        String elseIfName = "";

        String verdaderaE = etiqNueva();
        String falsaE = etiqNueva();
        String siguienteE = nodo.getAttribute("Siguiente");
        if (lista.getLength() > 2) {
            elseIf = (Element) lista.item(2);
            elseIfName = elseIf.getNodeName();
        }
        if (expression.getNodeName().equals("Literal") || expression.getNodeName().equals("ID")) {
            String newTemp = nuevoTemp();
            cuadruplos.gen(":=", expression.getAttribute("Value"), newTemp);
            String temp = nuevoTemp();
            cuadruplos.gen(":=", "1", temp);

            cuadruplos.gen("if=", newTemp, temp, "$" + verdaderaE);
            cuadruplos.genGOTO("$" + falsaE);
        } else {
            expression.setAttribute("Verdadera", verdaderaE);
            expression.setAttribute("Falsa", falsaE);
            cuadRelacional(expression);
        }

        cuadruplos.genEtiq("$" + verdaderaE);
        body.setAttribute("Siguiente", siguienteE);

        recorrer(body);

        cuadruplos.genGOTO("$" + siguienteE);
        cuadruplos.genEtiq("$" + falsaE);

        switch (elseIfName) {
            case "IfStatement": {
                elseIf.setAttribute("Siguiente", siguienteE);
                cuadIf(elseIf);

                break;
            }
            case "Body": {
                elseIf.setAttribute("Siguiente", siguienteE);
                recorrer(elseIf);

            }
        }

    }

    private void cuadNot(Element nodo) throws Exception {

        Element arg1 = (Element) nodo.getFirstChild();
        String arg1Name = arg1.getNodeName();
        String VerdaderaE = nodo.getAttribute("Falsa");
        String falsaE = nodo.getAttribute("Verdadera");

        arg1.setAttribute("Verdadera", falsaE);
        arg1.setAttribute("Falsa", VerdaderaE);

        switch (arg1Name) {
            case "ID": {

                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "Literal": {

                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);
                break;
            }
            case "ARRAY": {

                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "$" + verdadera);
                cuadruplos.genGOTO("$" + falsa);

                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                cuadExpresiones(arg1);
                break;
            }
            default: {
                cuadRelacional(arg1);
                break;
            }

        }
    }

    public void cuadExpresiones(Element nodo) throws Exception {

        Element arg1 = (Element) nodo.getFirstChild();
        Element arg2 = (Element) nodo.getLastChild();
        Element parent = (Element) nodo.getParentNode();

        String arg1Name = arg1.getNodeName();
        String arg2Name = arg2.getNodeName();

        String t1 = "";
        String t2 = "";
        String tResultado = "";
        String op = nodo.getAttribute("Value");

        String VerdaderaE = nodo.getAttribute("Verdadera");
        String falsaE = nodo.getAttribute("Falsa");

        if (arg1Name.equals("ID") || arg1Name.equals("Literal")) {
            t1 = nuevoTemp();
            cuadruplos.gen(":=", arg1.getAttribute("Value"), t1);
        } else if (arg1Name.equals("ARRAY")) {
            cuadArreglo(arg1);
            t1 = getTemp();
        } else {
            cuadAritmetico(arg1);
            t1 = getTemp();
        }

        if (arg2Name.equals("ID") || arg2Name.equals("Literal")) {
            t2 = nuevoTemp();
            cuadruplos.gen(":=", arg2.getAttribute("Value"), t2);
        } else if (arg2Name.equals("ARRAY")) {
            cuadArreglo(arg2);
            t2 = getTemp();
        } else {
            cuadAritmetico(arg2);
            t2 = getTemp();
        }
        cuadruplos.gen("if" + op, t1, t2, "$" + VerdaderaE);
        cuadruplos.genGOTO("$" + falsaE);
    }

    public void cuadAsignacion(Element nodo) throws Exception {
        Element arg1 = (Element) nodo.getFirstChild();
        Element arg2 = (Element) nodo.getLastChild();

        String temp1 = "";
        String temp2 = "";

        switch (arg2.getNodeName()) {
            case "ID":
            case "Literal": {
                String temp = arg2.getAttribute("Value");
                temp2 = nuevoTemp();
                cuadruplos.gen(":=", temp, temp2);
                break;
            }
            case "FunctionCall": {
                //cuadruploFuncCall(arg2);
                //temp2 = "RET";
                break;
            }
            case "AND":
            case "OR":
            case "NOT":
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                arg2.setAttribute("Verdadera", etiqNueva());
                arg2.setAttribute("Falsa", etiqNueva());

                cuadRelacional(arg2);
                String newTemp = nuevoTemp();

                String asignacion = etiqNueva();
                cuadruplos.genEtiq(arg2.getAttribute("Verdadera"));
                cuadruplos.gen(":=", "1", "", newTemp);
                cuadruplos.genGOTO("$" + asignacion);

                cuadruplos.genEtiq(arg2.getAttribute("Falsa"));
                cuadruplos.gen(":=", "0", "", newTemp);
                cuadruplos.genEtiq("$" + asignacion);
                temp2 = getTemp();

                break;
            }
            default: {
                cuadAritmetico(arg2);
                temp2 = getTemp();
                break;
            }
        }

        if (arg1.getNodeName().equals("ID")) {
            String isReturn = nodo.getAttribute("Return");
            if (isReturn.isEmpty()) {
                temp1 = arg1.getAttribute("Value");
                cuadruplos.gen(":=", temp2, temp1);
            } else {
                cuadruplos.gen("FRET", temp2, "", "");
            }

        } else if (arg1.getNodeName().equals("ARRAY")) {
            Element arg = (Element) arg1.getFirstChild();
            String argName = arg.getNodeName();
            String valex = arg1.getAttribute("Value");
            Simbolo S = tabla.getVariable(valex, ambitoActual);
            String tipo = S.getTipo();
            String indiceInicial = tipo.split("\\.")[2];
            if (argName.equals("ID") || argName.equals("Literal")) {
                String tempArg1 = nuevoTemp();
                cuadruplos.gen(":=", arg.getAttribute("Value"), tempArg1);
                String tempArg2 = nuevoTemp();
                cuadruplos.gen(":=", indiceInicial, tempArg2);
                String nuevo = nuevoTemp();
                cuadruplos.gen("-", tempArg1, tempArg2, nuevo);
                String temp = getTemp();
                tempArg2 = nuevoTemp();
                cuadruplos.gen(":=", getTamañoTipo(tipo), tempArg2);
                nuevo = nuevoTemp();
                cuadruplos.gen("*", temp, tempArg2, nuevo);
                temp = getTemp();
                cuadruplos.gen("[]=", temp, temp2, valex);
            } else {
                cuadAritmetico(arg);
                String temp = getTemp();
                String tempArg2 = nuevoTemp();
                cuadruplos.gen(":=", indiceInicial, tempArg2);
                String nuevoTemp = nuevoTemp();
                cuadruplos.gen("-", temp, tempArg2, nuevoTemp);
                temp = getTemp();
                tempArg2 = nuevoTemp();
                cuadruplos.gen(":=", getTamañoTipo(tipo), tempArg2);
                nuevoTemp = nuevoTemp();
                cuadruplos.gen("*", temp, tempArg2, nuevoTemp);
                temp = this.getTemp();
                cuadruplos.gen("[]=", temp, temp2, valex);
            }
        }
    }

    public void cuadAritmetico(Element nodo) throws Exception {
        String nodeName = nodo.getNodeName();

        switch (nodeName) {
            case "Div":
            case "Minus":
            case "Times":
            case "Plus": {
                Element arg1 = (Element) nodo.getFirstChild();
                Element arg2 = (Element) nodo.getLastChild();
                String arg1Nombre = arg1.getNodeName();
                String arg2Nombre = arg2.getNodeName();
                boolean arg1EsArreglo = arg1Nombre.equals("ARRAY");
                boolean arg2EsArreglo = arg2Nombre.equals("ARRAY");
                boolean arg1EsFinal = arg1Nombre.equals("Literal") || arg1Nombre.equals("ID") || arg1EsArreglo;
                boolean arg2EsFinal = arg2Nombre.equals("Literal") || arg2Nombre.equals("ID") || arg2EsArreglo;
                if (arg1EsFinal && arg2EsFinal) {
                    if (arg1EsArreglo && arg2EsArreglo) {
                        cuadAritmetico(arg1);
                        String tempArg1 = getTemp();
                        cuadAritmetico(arg2);
                        String tempArg2 = getTemp();

                        String temp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");

                        cuadruplos.gen(operacion, tempArg1, tempArg2, temp);
                    } else if (arg1EsArreglo) {
                        cuadAritmetico(arg1);
                        String tempArg = getTemp();

                        String temp = arg2.getAttribute("Value");
                        String operacion = nodo.getAttribute("Value");

                        String newTemp = nuevoTemp();
                        cuadruplos.gen(operacion, tempArg, temp, newTemp);
                    } else if (arg2EsArreglo) {
                        cuadAritmetico(arg2);
                        String tempArg = getTemp();

                        String temp = arg1.getAttribute("Value");
                        String operacion = nodo.getAttribute("Value");

                        String newTemp = nuevoTemp();
                        cuadruplos.gen(operacion, temp, tempArg, newTemp);
                    } else {
                        String temp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");
                        cuadruplos.gen(operacion, arg1.getAttribute("Value"), arg2.getAttribute("Value"), temp);
                    }
                } else if (arg1EsFinal) {
                    if (arg1EsArreglo) {
                        cuadAritmetico(arg1);
                        String tempArg1 = getTemp();
                        cuadAritmetico(arg2);
                        String tempArg2 = getTemp();

                        String temp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");
                        cuadruplos.gen(operacion, tempArg1, tempArg2, temp);
                    } else {
                        cuadAritmetico(arg2);
                        String ultimo = getTemp();
                        String newTemp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");
                        cuadruplos.gen(operacion, ultimo, arg1.getAttribute("Value"), newTemp);
                    }
                } else if (arg2EsFinal) {
                    if (arg2EsArreglo) {
                        cuadAritmetico(arg1);
                        String tempArg1 = getTemp();
                        cuadAritmetico(arg2);
                        String tempArg2 = getTemp();

                        String temp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");

                        cuadruplos.gen(operacion, tempArg1, tempArg2, temp);
                    } else {
                        cuadAritmetico(arg1);
                        String ultimo = getTemp();
                        String newTemp = nuevoTemp();
                        String operacion = nodo.getAttribute("Value");

                        cuadruplos.gen(operacion, ultimo, arg2.getAttribute("Value"), newTemp);

                    }
                } else {
                    cuadAritmetico(arg1);
                    String lugar1 = getTemp();
                    cuadAritmetico(arg2);
                    String lugar2 = getTemp();
                    String temp = nuevoTemp();
                    String operacion = nodo.getAttribute("Value");
                    cuadruplos.gen(operacion, lugar1, lugar2, temp);
                }
                break;
            }
            case "ARRAY": {
                cuadArreglo(nodo);
                break;
            }
        }

    }

    public void cuadArreglo(Element nodo) throws Exception {
        Element arg = (Element) nodo.getFirstChild();
        String argName = arg.getNodeName();
        String operacion = "=[]";
        String IDArray = nodo.getAttribute("Value");
        Simbolo S = tabla.getVariable(IDArray, ambitoActual);
        String tipo = S.getTipo();
        String indiceInicial = tipo.split("\\.")[2];
        if (argName.equals("ID") || argName.equals("Literal")) {
            String temp1 = nuevoTemp();
            cuadruplos.gen(":=", arg.getAttribute("Value"), temp1);
            String temp2 = nuevoTemp();
            cuadruplos.gen(":=", indiceInicial, temp2);
            String newTemp = nuevoTemp();
            cuadruplos.gen("-", temp1, temp2, newTemp);

            temp1 = getTemp();
            temp2 = nuevoTemp();
            cuadruplos.gen(":=", getTamañoTipo(tipo.split("\\.")[1]), temp2);
            newTemp = nuevoTemp();
            cuadruplos.gen("*", temp1, temp2, newTemp);
            temp2 = this.getTemp();
            newTemp = nuevoTemp();
            cuadruplos.gen(operacion, IDArray, temp2, newTemp);
        } else {
            cuadAritmetico(arg);
            String temp = getTemp();
            String temp2 = nuevoTemp();
            cuadruplos.gen(":=", indiceInicial, temp2);
            String newTemp = nuevoTemp();
            cuadruplos.gen("-", temp, temp2, newTemp);
            temp = getTemp();
            temp2 = nuevoTemp();
            cuadruplos.gen(":=", getTamañoTipo(tipo.split("\\.")[1]), temp2);
            newTemp = nuevoTemp();
            cuadruplos.gen("*", temp, temp2, newTemp);
            temp = getTemp();
            newTemp = nuevoTemp();
            cuadruplos.gen(operacion, IDArray, temp, newTemp);
        }
    }

    private void cuadruploFuncCall(Element functionNode) throws Exception {
        Element funcId = (Element) functionNode.getFirstChild();
        Element funcArgsNode = (Element) functionNode.getLastChild();
        if (funcArgsNode.getNodeName().equals("Arguments")) {
            NodeList funcArgs = funcArgsNode.getChildNodes();
            ArrayList<String> parameters = new ArrayList();
            for (int i = 0; i < funcArgs.getLength(); i++) {
                Element currentNode = (Element) funcArgs.item(i);
                String argumentName = currentNode.getNodeName();
                switch (argumentName) {
                    case "ID":
                    case "Literal": {
                        String temp = nuevoTemp();
                        cuadruplos.gen(":=", currentNode.getAttribute("Value"), temp);
                        parameters.add(temp);
                        break;
                    }
                    case "ARRAY": {
                        cuadArreglo(currentNode);
                        String temp = this.getTemp();
                        parameters.add(temp);
                        break;
                    }
                    case "AND":
                    case "OR":
                    case "NOT":
                    case "GreaterThan":
                    case "LessThan":
                    case "Equals":
                    case "LessOrEqual":
                    case "GreaterOrEqual":
                    case "Different": {
                        currentNode.setAttribute("Verdadera", etiqNueva());
                        currentNode.setAttribute("Falsa", etiqNueva());

                        cuadRelacional(currentNode);
                        String newTemp = nuevoTemp();

                        String asignacion = etiqNueva();
                        cuadruplos.genEtiq(currentNode.getAttribute("Verdadera"));
                        cuadruplos.gen(":=", "1", "", newTemp);
                        cuadruplos.genGOTO("$" + asignacion);

                        cuadruplos.genEtiq(currentNode.getAttribute("Falsa"));
                        cuadruplos.gen(":=", "0", "", newTemp);
                        cuadruplos.genEtiq("$" + asignacion);
                        parameters.add(newTemp);
                        break;
                    }

                    case "Div":
                    case "Minus":
                    case "Times":
                    case "Plus": {
                        cuadAritmetico(currentNode);
                        String temp = getTemp();
                        parameters.add(temp);
                        break;
                    }
                }
            }

            for (String Param : parameters) {
                cuadruplos.genParam(Param);
            }

            String functionName = funcId.getAttribute("Value");
            cuadruplos.genCall(functionName);
        }

    }

    private static String getEtiqueta() {
        return "etiq" + etiqCont;
    }

    private static String etiqNueva() {
        return "etiq" + ++etiqCont;
    }

    private static String getTemp() {
        return "@t" + tempCont;
    }

    private String nuevoTemp() {
        return "@t" + ++tempCont;
    }

    @Override
    public String toString() {
        return cuadruplos.toString();
    }
    
    public TablaCuadruplos getTablaCuadruplo(){
        return this.cuadruplos;
    }

    public static String getTamañoTipo(String tipo) {
        if (tipo.equals("char") || tipo.equals("boolean")) {
            return "1";
        } else {
            return "4";
        }
    }
    
    

}
