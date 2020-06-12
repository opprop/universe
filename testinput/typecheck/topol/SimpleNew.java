package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

/**
 *
 * @author wmdietl
 */
public class SimpleNew {
    // OK
    @Peer Object p = new @Peer Object();
    @Rep Object r = new @Rep Object();

    // :: error: (uts.new.ownership)
    @Any Object a = new @Any Object();
}
