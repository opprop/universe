package GUT.topol;

import GUT.qual.*;

public class Boxing {
    Integer I = 5;

    void m() {
        Integer I = new Integer(3);
        int i = 5;
        I = i;
        i = I;
    }

    void mrep() {
        @Rep Integer I = new @Rep Integer(3);
        int i = 5;

        I = i;
        i = I;
    }

    void k(){
        int i;
    }
 
    void ops() {
        
        I += 9;
        // Compound assignment works on primitive types, therefore
        // @Rep qualifier is ignored.
        I += new @Rep Integer(3);
    }

}
