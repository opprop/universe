/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package GUT.topol;

import GUT.qual.Any;
import GUT.qual.Peer;
import GUT.qual.Rep;

/**
 *
 * @author wmdietl
 */
public class SimpleSub {
    // OK
    @Peer Object p = new @Peer Object();
    @Rep Object r = new @Rep Object();
    @Any Object a1 = p;
    @Any Object a2 = r;

    void OK() {
        p = this;
        p = null;
        r = null;
        a1 = null;
        a1 = this;
    }

    void errors() {
        //:: error: (assignment.type.incompatible)
        r = a1;
        //:: error: (assignment.type.incompatible)
        p = a1;
        //:: error: (assignment.type.incompatible)
        r = p;
        //:: error: (assignment.type.incompatible)
        p = r;
    }
}
