package GUTI;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSolver;
import constraintsolver.Lattice;
import util.PrintUtils;

import javax.lang.model.element.AnnotationMirror;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUTIConstraintSolver extends ConstraintSolver {

    @Override
    protected Serializer<?, ?> createSerializer(String value, Lattice lattice) {
        return new GUTIConstraintSerializer<>(value, lattice);
    }

    @Override
    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        result = inferMissingConstraint(result);
        PrintUtils.printResult(result);
        if (collectStatistic) {
            writeInferenceResult("gut-inference-----result.txt", result);
        }
        return new DefaultInferenceSolution(result);
    }

    public static void writeInferenceResult(String filename, Map<Integer, AnnotationMirror> result) {
        String writePath = new File(new File("").getAbsolutePath()).toString() + File.separator + filename;
        StringBuilder sb = new StringBuilder();

        recordKeyValue(sb, "total_number", String.valueOf(result.size()));

        try {
            PrintWriter pw = new PrintWriter(writePath);
            pw.write(sb.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recordKeyValue(StringBuilder sb, String key, String value) {
        sb.append(key + "," + value + "\n");
    }
}
