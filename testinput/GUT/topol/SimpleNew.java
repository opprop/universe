package GUT.topol;

import GUT.qual.Any;
import GUT.qual.Peer;
import GUT.qual.Rep;

/**
 *
 * @author wmdietl
 */
public class SimpleNew {
    // OK
    @Peer Object p = new @Peer Object();
    @Rep Object r = new @Rep Object();

    //:: error: (uts.new.ownership)
    @Any Object a = new @Any Object();
}
