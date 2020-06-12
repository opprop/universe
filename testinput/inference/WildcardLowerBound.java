import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

public class WildcardLowerBound {

    private Set _classNames = new LinkedHashSet();

    Set extract() {
        return true ? _classNames : Collections.EMPTY_SET;
    }
}
