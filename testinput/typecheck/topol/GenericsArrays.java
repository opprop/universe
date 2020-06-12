package typecheck.topol;

import universe.qual.*;
import java.util.List;

// Use of EventList reveals a different problem, add a testcase!
// import ca.odell.glazedlists.EventList;

public class GenericsArrays {
    List<String[]> list;

    void m() {
        String[] s = null;
        list.add(s);
    }
}
