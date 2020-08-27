package GUT.lostno;

import universe.qual.*;

public class LostNo {
    //:: error: (uts.explicit.lost.forbidden)
    @Lost Object lo;

    @Rep Object ro = new @Rep Object();

    void foo(@Rep LostNo rln) {
        // Lost as the result of viewpoint adaptation
        // must still be allowed.
        @Any Object a = rln.ro;
    }
}
