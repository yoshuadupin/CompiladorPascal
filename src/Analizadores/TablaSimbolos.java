/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

import java.util.ArrayList;

/**
 *
 */
public class TablaSimbolos {

    public ArrayList<Simbolo> Simbolos = new ArrayList();
    String formatHeader = "|%-20s |%-60s |%-15s |%-15s  |%-15s |%-18s";
    String formatBody = "|%-20s |%-60s |%-15s |%-15s |%-15s |%-18s";

   
    
    public Simbolo getSimbolo(int index) throws Exception {
        if(index >= 0 && index < Simbolos.size()){
            return Simbolos.get(index);
        } else {
            throw new Exception("Symbol not found");
        }
    }

     public int add(Simbolo S) throws Exception {
        int itemIndex = this.getSymbolIndex(S);
        if(itemIndex > 0) {
            throw new Exception("Ya existe un elemento " + S.getId() + " en el ambito " + S.getAmbito());
        } else {
            Simbolos.add(S);
        }
        return Simbolos.size() -1;
    }
    
     public Simbolo getVariable(String Id) throws Exception {
        for(Simbolo S : Simbolos){
            if(S.getId().equals(Id) && S.isVariable()){
                return S;
            }
        }  
        return null;
    }
     
     public Simbolo getFunction(String Id) throws Exception {
        for(Simbolo S : Simbolos){
            if(S.getId().equals(Id) && S.isFuncion()){
                return S;
            }
        }  
        return null;
    }
     
    public Simbolo getVariable(String Id, String ambito) throws Exception {
        for(Simbolo S : Simbolos){
            if(S.getId().equals(Id) && (S.isVariable() || S.isParametro()) && S.getAmbito().equals(ambito)){
                return S;
            }
        }  
        return null;
    }
    
    public int getSymbolIndex(Simbolo S){
        for (int i = 0; i < Simbolos.size(); i++) {
            Simbolo St = Simbolos.get(i);
            boolean hasSameName = S.getId().equals(St.getId());
            boolean hasSameScope = S.getAmbito().equals(St.getAmbito());
            if( hasSameName && hasSameScope){
                return i;
            }
        }
        return -1;
    } 
    
    public void replaceNode(Simbolo S, int index) {
        Simbolos.set(index, S);
    }
    
    private boolean hasSameParameters(Simbolo S1, Simbolo S2) {
        return false;
    }
    
    public void clear(){
        Simbolos.clear();
    }
    
    @Override
    public String toString() {
        String headers = String.format(
                formatHeader,
                "IDENTIFICADOR",
                "TIPO",
                "AMBITO",
                "ES VARIABLE",
                "ES PARAMETRO",
                "OFFSET"
        );
        System.out.println(headers);
        for (Simbolo S: Simbolos) {
            String output = String.format(
                    formatBody,
                    S.getId(),
                    S.getTipo(),
                    S.getAmbito(),
                    String.valueOf(S.isVariable()),
                    String.valueOf(S.isParametro()),
                    String.valueOf(S.getOS())
            );
            System.out.println(output);
        }

        return "";
    }
}
