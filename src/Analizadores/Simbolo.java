/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

/**
 *
 */
public class Simbolo {
    private String id;
    private String valor;
    private String tipo;
    private String ambito;
    private int refIndex = -1;
    private boolean variable = false;
    private boolean funcion = false;
    private boolean parametro = false;
    private boolean byRef = false;
    private int posicionMemoria;

    public int getRefIndex() {
        return refIndex;
    }

    public void setRefIndex(int refIndex) {
        this.refIndex = refIndex;
    }
    
    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public boolean isByRef() {
        return byRef;
    }

    public void setByRef(boolean byRef) {
        this.byRef = byRef;
    }

    
    public Simbolo() {
        this.id = "";
        this.valor = "";
        this.tipo = "";
        this.variable = false;
        this.funcion = false;
        this.parametro = false;
        this.posicionMemoria = 0;
        
    }
    
     public Simbolo(String id, String valor, String tipo) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.ambito = null;
    }
    
    public Simbolo(String id, String valor, String tipo, String ambito) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.ambito = ambito;
    }
    
    
    
    public Simbolo(String id, String valor, String tipo, String ambito, boolean variable, boolean funcion, boolean parametro, int posicionMemoria) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.ambito = ambito;
        this.variable = variable;
        this.funcion = funcion;
        this.parametro = parametro;
        this.posicionMemoria = posicionMemoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isVariable() {
        return variable;
    }

    public void setVariable(boolean variable) {
        this.variable = variable;
    }

    public boolean isFuncion() {
        return funcion;
    }

    public void setFuncion(boolean funcion) {
        this.funcion = funcion;
    }

    public boolean isParametro() {
        return parametro;
    }

    public void setParametro(boolean parametro) {
        this.parametro = parametro;
    }

    public int getPosicionMemoria() {
        return posicionMemoria;
    }

    public void setPosicionMemoria(int posicionMemoria) {
        this.posicionMemoria = posicionMemoria;
    }
}
