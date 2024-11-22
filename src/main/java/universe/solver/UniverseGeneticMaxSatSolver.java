package universe.solver;

import static io.jenetics.engine.EvolutionResult.toBestEvolutionResult;
import static io.jenetics.engine.Limits.bySteadyFitness;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.geneticmaxsat.GeneticMaxSatSolver;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;

import io.jenetics.IntegerGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.Optimize;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;

import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

public class UniverseGeneticMaxSatSolver extends GeneticMaxSatSolver {
    public UniverseGeneticMaxSatSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            MaxSatFormatTranslator formatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    /** The fitness function in this case is the count of {@link universe.qual.Rep} */
    public int fitness(final int[] chromosome) {
        IPBSolver solver = org.sat4j.maxsat.SolverFactory.newDefault();
        WDimacsReader reader = new WDimacsReader(new WeightedMaxSatDecorator(solver));
        Map<Integer, AnnotationMirror> solutions;
        int fitness_count = 0;

        String WCNFModInput = changeSoftWeights(chromosome, this.wcnfFileContent, false);

        InputStream stream =
                new ByteArrayInputStream(WCNFModInput.getBytes(StandardCharsets.UTF_8));

        try {
            solver = (IPBSolver) reader.parseInstance(stream);
        } catch (ContradictionException | IOException | ParseFormatException e) {
            System.out.println(e);
        }

        try {
            if (solver.isSatisfiable()) {
                solutions = decode(solver.model());

                List<AnnotationMirror> sol = new ArrayList<>(solutions.values());

                for (AnnotationMirror sol_0 : sol) {
                    if (sol_0.toString().equals("@universe.qual.Rep")) {
                        fitness_count += 1;
                    }
                }
            } else {
                System.out.println("UNSAT at " + chromosome[0]);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // System.out.println("Rep count: " + fitness_count);

        return fitness_count;
    }

    @Override
    public void fit() {
        final Engine<IntegerGene, Integer> engine =
                Engine.builder(
                                this::fitness,
                                Codecs.ofVector(IntRange.of(0, 700), this.allSoftWeightsCount))
                        .populationSize(500)
                        .offspringFraction(0.7)
                        .survivorsSelector(new RouletteWheelSelector<>())
                        .offspringSelector(new TournamentSelector<>())
                        .optimize(Optimize.MAXIMUM)
                        .alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6))
                        .build();

        final EvolutionStatistics<Integer, ?> statistics = EvolutionStatistics.ofNumber();

        final EvolutionResult<IntegerGene, Integer> best_res =
                engine.stream()
                        .limit(bySteadyFitness(7))
                        .limit(100)
                        .peek(statistics)
                        .collect(toBestEvolutionResult());

        System.out.println(statistics);
        System.out.println("Genotype length: " + best_res.genotypes().length());
        System.out.println("Best Phenotype: " + best_res.bestPhenotype());
        System.out.println("Worst Phenotype: " + best_res.worstPhenotype());
    }
}
