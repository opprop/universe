package typecheck.topol;

import java.util.List;

import universe.qual.*;

// Use of EventList reveals a different problem, add a testcase!
// import ca.odell.glazedlists.EventList;

public class GenericsArrays {
    List<String[]> list;

    void m() {
        String[] s = null;
        list.add(s);
    }
}
