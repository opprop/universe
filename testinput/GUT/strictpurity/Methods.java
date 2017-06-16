package GUT.strictpurity;

import GUT.qual.Pure;
/**
 *
 * @author wmdietl
 */
public class Methods {

    int x;
    @Pure void m() {
        //:: error: (purity.assignment.forbidden)
        this.x = 5;
    }
}
