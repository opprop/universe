import universe.qual.Self;
import universe.qual.*;
import java.util.List;
class E {
	// Type parameter expects @Peer + @Peer Object = @Peer Object
	List<Object> le;// If only actual underlying type is the subtype of the declared underlying type, then the qualifier is useless?!
}
public class Member {
     
    E e = new E();
    //:: fixable-error: (assignment.type.incompatible)
    @Rep List<@Rep Object> l = e.le;
    
    //@Peer List<@Peer Object> l2 = e.le;

    /*
    @Peer List<@Peer String> ln;
    @Peer List<@Any String> ln;
    @Peer List<@Rep String> ln2 = ln;*/
    
    /*
    @Peer List<@Peer Object> ln;
    @Peer List<@Rep Object> ln2 = ln;*/

}
