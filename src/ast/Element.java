/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pascal.ast;

/**
 *
 * @author jorgecaballero
 */
public class Element {
    public Expression exp;
    public Element el;

    public Element(Expression exp) {
        this.exp = exp;
    }

    public Element(Element el, Expression exp) {
        this.exp = exp;
        this.el = el;
    }
    
    
}
