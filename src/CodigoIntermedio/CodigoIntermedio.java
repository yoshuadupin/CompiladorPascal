/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CodigoIntermedio;

import Analizadores.Simbolo;
import Analizadores.TablaSimbolos;
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
        NodeList hijos = rootNode.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Element nodo = (Element) hijos.item(i);
            String nodeName = nodo.getNodeName();

            switch (nodeName) {

                case "ProcedureDeclaration": {
                    recorrer(nodo);
                    break;
                }
                case "FunctionDeclaration": {
                    recorrer(nodo);
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
                    recorrer(nodo);
                    break;
                }
                case "IfStatement": {

                    cuadIf(nodo);
                    break;
                }
                case "WhileStatement": {
                    recorrer(nodo);
                    break;
                }
                case "RepeatStatement": {
                    recorrer(nodo);
                    break;
                }
                case "ForStatement": {
                    recorrer(nodo);
                    break;
                }
                case "ReadStatement": {
                    recorrer(nodo);

                    break;
                }
                case "WriteStatement": {
                    recorrer(nodo);
                    break;
                }
                case "Assignment": {
                    cuadAssignment(nodo);
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

    }

    private void cuadRelacional(Element node) throws Exception {
        String nodeName = node.getNodeName();

        switch (nodeName) {
            case "AND": {
                cuadruploAnd(node);
                break;
            }
            case "OR": {
                cuadruploOr(node);
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

    private void cuadruploOr(Element nodo) throws Exception {
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
                /*
                String arg1Value = arg1.getAttribute("Value");
                arg1.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg1Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + falsa);
                cuadruplos.genGOTO("@" + verdadera);
                break;
            }
            case "Literal": {
                /*
                String arg1Value = arg1.getAttribute("Value");
                arg1.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg1Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");*/
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + falsa);
                cuadruplos.genGOTO("@" + verdadera);
                break;
            }
            case "ARRAY": {
                /*
                cuadruploArray(arg1);
                String temp = this.getTemp();
                arg1.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", temp, "1", "@");
                Cuadruplos.GEN_GOTO("@");*/
                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "@" + falsa);
                cuadruplos.genGOTO("@" + verdadera);
                break;
            }
            case "GreaterThan":
            case "LessThan":
            case "Equals":
            case "LessOrEqual":
            case "GreaterOrEqual":
            case "Different": {
                //cuadExpresiones(arg1);
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
        //int M1 = Cuadruplos.getSize();

        switch (arg2Name) {
            case "ID": {
                /*
                String arg2Value = arg2.getAttribute("Value");
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg2Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");*/
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "Literal": {
                /*
                String arg2Value = arg2.getAttribute("Value");
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg2Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");*/
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "ARRAY": {
                /*
                cuadArreglo(arg2);
                String temp = this.getTemp();
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", temp, "1", "@");
                Cuadruplos.GEN_GOTO("@");*/
                cuadArreglo(arg2);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
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
        /*
        this.completa(M1, arg1.getAttribute("listaF"));
        String listaFusionada = this.fusiona(arg1.getAttribute("listaV"), arg2.getAttribute("listaV"));
        nodo.setAttribute("listaV", listaFusionada);
        nodo.setAttribute("listaF", arg2.getAttribute("listaF"));
         */
    }

    private void cuadruploAnd(Element nodo) throws Exception {

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
                /*
                String arg1Value = arg1.getAttribute("Value");
                arg1.setAttribute("listaV", crearLista(cuadruplos.getSize()));
                arg1.setAttribute("listaF", crearLista(cuadruplos.getSize() + 1));
                cuadruplos.gen("if=", arg1Value, "1", "@");
                cuadruplos.GEN_GOTO("@");
                 */
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "Literal": {
                /*
                String arg1Value = arg1.getAttribute("Value");
                arg1.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg1Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "ARRAY": {
                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);

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
        //gen("ETIQ", E1.verdadera);
        //E2.verdadera = E.verdadera;
        //E2.falsa = E.falsa;

        switch (arg2Name) {
            case "ID": {
                /*
                String arg2Value = arg2.getAttribute("Value");
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg2Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "Literal": {
                /*
                String arg2Value = arg2.getAttribute("Value");
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg2Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg2Value = arg2.getAttribute("Value");
                String verdadera = arg2.getAttribute("Verdadera");
                String falsa = arg2.getAttribute("Falsa");
                cuadruplos.gen("if=", arg2Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "ARRAY": {
                /*
                cuadArreglo(arg2);
                String temp = this.getTemp();
                arg2.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg2.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", temp, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                cuadArreglo(arg2);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);

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
        /*
        this.completa(M1, arg1.getAttribute("listaV"));
        String listaFusionada = this.fusiona(arg1.getAttribute("listaF"), arg2.getAttribute("listaF"));
        nodo.setAttribute("listaF", listaFusionada);
        nodo.setAttribute("listaV", arg2.getAttribute("listaV"));
         */

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
            //expression.setAttribute("listaV", this.crearLista(Cuadruplos.getSize()));
            //expression.setAttribute("listaF", this.crearLista(Cuadruplos.getSize() + 1));

            cuadruplos.gen("if=", newTemp, temp, "@" + verdaderaE);
            cuadruplos.genGOTO("@" + falsaE);
        } else {
            expression.setAttribute("Verdadera", verdaderaE);
            expression.setAttribute("Falsa", falsaE);
            cuadRelacional(expression);
        }

        cuadruplos.genEtiq(verdaderaE);
        body.setAttribute("Siguiente", siguienteE);

        recorrer(body);

        cuadruplos.genGOTO("@" + siguienteE);
        cuadruplos.genEtiq("@" + falsaE);
        /*
        int N1 = cuadruplos.genGOTO("@");
        nodo.setAttribute("listaF", crearLista(N1));
        int M2 = cuadruplos.getSize();

        this.completa(M1, expression.getAttribute("listaV"));
        this.completa(M2, expression.getAttribute("listaF"));
         */
        switch (elseIfName) {
            case "IfStatement": {
                elseIf.setAttribute("Siguiente", siguienteE);
                cuadIf(elseIf);
                /*
                String listaF = elseIf.getAttribute("listaF");
                nodo.setAttribute("listaF", fusiona(listaF, nodo.getAttribute("listaF")));
                 */
                break;
            }
            case "Body": {
                elseIf.setAttribute("Siguiente", siguienteE);
                recorrer(elseIf);
                int M3 = Cuadruplos.GEN_GOTO("@");
                String listaF = nodo.getAttribute("listaF");
                nodo.setAttribute("listaF", fusiona(listaF, this.crearLista(M3)));
                break;
            }
        }

        int endOfIf = Cuadruplos.getSize();
        this.completa(endOfIf, nodo.getAttribute("listaF"));
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
                /*
                String arg1Value = arg1Node.getAttribute("Value");
                arg1Node.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1Node.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg1Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "Literal": {
                /*
                String arg1Value = arg1Node.getAttribute("Value");
                arg1Node.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1Node.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", arg1Value, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                String arg1Value = arg1.getAttribute("Value");
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");
                cuadruplos.gen("if=", arg1Value, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);
                break;
            }
            case "ARRAY": {
                /*
                cuadArreglo(arg1);
                String temp = this.getTemp();
                arg1Node.setAttribute("listaV", crearLista(Cuadruplos.getSize()));
                arg1Node.setAttribute("listaF", crearLista(Cuadruplos.getSize() + 1));
                Cuadruplos.GEN("if=", temp, "1", "@");
                Cuadruplos.GEN_GOTO("@");
                 */
                cuadArreglo(arg1);
                String verdadera = arg1.getAttribute("Verdadera");
                String falsa = arg1.getAttribute("Falsa");

                String temp = getTemp();
                cuadruplos.gen("if=", temp, "1", "@" + verdadera);
                cuadruplos.genGOTO("@" + falsa);

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
        /*
        nodo.setAttribute("listaV", this.crearLista(Cuadruplos.getSize()));
        nodo.setAttribute("listaF", this.crearLista(Cuadruplos.getSize() + 1));
         */
        cuadruplos.gen("if" + op, t1, t2, "@" + VerdaderaE);
        cuadruplos.genGOTO("@" + falsaE);
    }

    public void cuadAssignment(Element nodo) throws Exception {
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
                //int M1 = cuadruplos.getSize();
                String asignacion = etiqNueva();
                cuadruplos.genEtiq(arg2.getAttribute("Verdadera"));
                cuadruplos.gen(":=", "1", "", newTemp);
                cuadruplos.genGOTO("@" + asignacion);
                //int M2 = cuadruplos.getSize();
                cuadruplos.genEtiq(arg2.getAttribute("Falsa"));
                cuadruplos.gen(":=", "0", "", newTemp);
                cuadruplos.genEtiq("@" + asignacion);
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
                        //String temp = nuevoTemp();
                        //cuadruplos.gen(":=", arg2.getAttribute("Value"), temp);

                        String temp = arg2.getAttribute("Value");
                        String operacion = nodo.getAttribute("Value");

                        String newTemp = nuevoTemp();
                        cuadruplos.gen(operacion, tempArg, temp, newTemp);
                    } else if (arg2EsArreglo) {
                        cuadAritmetico(arg2);
                        String tempArg = getTemp();
                        //String temp = nuevoTemp();
                        // cuadruplos.gen(":=", arg1.getAttribute("Value"), temp);

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

    private static String getEtiqueta() {
        return "etiq" + etiqCont;
    }

    private static String etiqNueva() {
        return "etiq" + ++etiqCont;
    }

    private static String getTemp() {
        return "t" + tempCont;
    }

    private String nuevoTemp() {
        return "t" + ++tempCont;
    }

    @Override
    public String toString() {
        return cuadruplos.toString();
    }

    public static String getTamañoTipo(String tipo) {
        if (tipo.equals("char") || tipo.equals("boolean")) {
            return "1";
        } else {
            return "4";
        }
    }

}
