/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analizadores;

import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 */
public class TablaSimbolos {

    public ArrayList<Simbolo> Simbolos = new ArrayList();
    JTable jt;
    String formatHeader = "|%-20s |%-60s |%-15s |%-15s  |%-15s |%-18s";
    String formatBody = "|%-20s |%-60s |%-15s |%-15s |%-15s |%-18s";

    public ArrayList<Simbolo> getSimbolos() {
        return Simbolos;
    }

    
    
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
        /*
        JFrame f = new JFrame(); 
        String[] headers = {
                "IDENTIFICADOR",
                "TIPO",
                "AMBITO",
                "ES VARIABLE",
                "ES PARAMETRO",
                "OFFSET"};
        int rowcount = 0;
        for (Simbolo S: Simbolos) {
           rowcount++;
        }//for de filas de la tabla
        String[][] body = new String[rowcount][6];
        int x = 0;

        for (Simbolo S: Simbolos) {
            int y = 0;
            body[x][y] = S.getId();
            body[x][y+1] = S.getTipo();
            body[x][y+2] = S.getAmbito();
            body[x][y+3] = String.valueOf(S.isVariable());
            body[x][y+4] = String.valueOf(S.isParametro());
            body[x][y+5] = String.valueOf(S.getOS());
            x++;
            //yabla[x][y] = {{"" + S.getId(), "" + S.getTipo(), "" + S.getAmbito(), "" + String.valueOf(S.isVariable()), "" + String.valueOf(S.isParametro()), "" + String.valueOf(S.getPosicionMemoria())}};
        }
        jt = new JTable(body,headers);
        JScrollPane sp=new JScrollPane(jt);
        f.add(sp);
        f.setSize(800,300);    
        f.setVisible(true);
        */
        return "";
    }
}
