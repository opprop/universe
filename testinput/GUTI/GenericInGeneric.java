import java.util.*;

class G<T> {
}

class F<Y, X extends G<Y>>{

}

public class GenericInGeneric{

    F f = new F<String, G<String>>();

}
