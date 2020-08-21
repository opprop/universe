package universe;

import java.lang.reflect.Constructor;

import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSerializer;
import constraintsolver.Lattice;

public class GUTIConstraintSerializer<S, T> extends ConstraintSerializer<S, T> {


    @SuppressWarnings("unchecked")
    public GUTIConstraintSerializer(String backEndType, Lattice lattice) {
        super(backEndType, lattice);
        try {
            String refinedBackEndType = backEndType;
            if (backEndType.contains("MaxSat")) {
                refinedBackEndType = backEndType.replace("maxsatbackend.", "");
            }
            Class<?> classObjectOfRealSerializer = Class.forName("GUTI.GUTI" + refinedBackEndType + "Serializer");
            Constructor<T> constructor = (Constructor<T>) classObjectOfRealSerializer
                    .getConstructor(Lattice.class);
            realSerializer = (Serializer<S, T>) constructor.newInstance(lattice);
        } catch (Exception e) {
            System.out.println("Error: Can't find constructor for: ");
            e.printStackTrace();
        }
    }

}
