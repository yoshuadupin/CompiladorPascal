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
    private boolean variable = false;
    private boolean funcion = false;
    private boolean parametro = false;
    private int offset  ;

    
    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    

    
    public Simbolo() {
        this.id = "";
        this.valor = "";
        this.tipo = "";
        this.variable = false;
        this.funcion = false;
        this.parametro = false;
        this.offset = 0;
        
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
    
    
    
    public Simbolo(String id, String valor, String tipo, String ambito, boolean variable, boolean funcion, boolean parametro, int offset) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.ambito = ambito;
        this.variable = variable;
        this.funcion = funcion;
        this.parametro = parametro;
        this.offset = offset;
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

    public int getOS() {
        return offset;
    }

    public void setOS(int offset) {
        this.offset = offset;
    }
}
