package edu.vandy.simulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.model.implementation.components.BeingComponent;
import edu.vandy.simulator.model.implementation.components.SimulatorComponent;
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.PalantirSnapshot;

import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.ACQUIRING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.DONE;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.ERROR;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.GAZING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.IDLE;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.RELEASING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.WAITING;

public class TestHelper {
    static List<Pair<BeingManager.Factory.Type, PalantiriManager.Factory.Type>>
    buildAllPairs() {
        return Arrays.stream(BeingManager.Factory.Type.values())
                .filter(it -> it.clazz != BeingManager.Factory.NoManager.class)
                .flatMap(bType ->
                        Arrays.stream(PalantiriManager.Factory.Type.values())
                                .filter(it -> it.clazz != PalantiriManager.Factory.NoManager.class)
                                .map(pType -> new Pair<>(bType, pType)))
                .collect(Collectors.toList());
    }

    static void testStrategyPairs(
            List<Pair<BeingManager.Factory.Type,
                    PalantiriManager.Factory.Type>> pairs,
            int beingCount,
            int palantirCount,
            int iterations,
            float animationSpeed,
            int gazingRangeMin,
            int gazingRangeMax) {
        for (int i = 0; i < pairs.size(); i++) {
            System.out.println("\n************ START [" +
                    (i+1) + "/" + pairs.size() + "] *************\n");
            testStrategy(
                    pairs.get(i).first,
                    pairs.get(i).second,
                    beingCount,
                    palantirCount,
                    iterations,
                    animationSpeed,
                    gazingRangeMin,
                    gazingRangeMax);
            System.out.println("\n************ END [" +
                    (i+1) + "/" + pairs.size() + "] *************\n");
        }
    }

    static void testStrategy(
            BeingManager.Factory.Type beingManager,
            PalantiriManager.Factory.Type palantirManager,
            int beingCount,
            int palantirCount,
            int iterations,
            float animationSpeed,
            int gazingRangeMin,
            int gazingRangeMax) {
        ModelParameters modelParameters =
                new ModelParameters(
                        beingManager,
                        palantirManager,
                        beingCount,
                        palantirCount,
                        iterations,
                        animationSpeed,
                        gazingRangeMin,
                        gazingRangeMax);
        HashMap<String, Error> allErrors = new HashMap<>();

        System.out.println(modelParameters);

        buildModelAndRun(modelParameters,
                snapshot -> allErrors.putAll(
                        validateSnapshot(beingCount, palantirCount, snapshot)));

        if (allErrors.size() > 0) {
            System.out.println("Found " + allErrors.size() +
                    "errors for Model:\n" + modelParameters);
            allErrors.values().forEach(it -> System.out.println(it.msg));
            throw new IllegalStateException("FAILED model test");
        }
    }

    static private Map<String, Error> validateSnapshot(
            int beingCount,
            int palantirCount,
            ModelSnapshot model) {

        HashMap<String, Error> errors = new HashMap<>();

        // A cancelling or cancelled model state will never be
        // in a valid state so there is nothing to check.
        if (model.getSimulator().getState() == SimulatorComponent.State.CANCELLING ||
                model.getSimulator().getState() == SimulatorComponent.State.CANCELLED) {
            return errors;
        }

        Map<Long, BeingSnapshot> beings = model.getBeings();
        Map<Long, PalantirSnapshot> palantiri = model.getPalantiri();

        // Being validation includes:
        // 1. Check that the expected number of beings exist.
        // 2. Check that valid previous -> current state is valid.
        // 3. Check that being palantirId references a valid palantir.
        // 4. Check that gazing beings have a valid palantir id.
        // 5. Check that only 1 being has any given palantir id.
        // 6. Check for starved beings.
        // 7. Check for unfair gazing bias.

        // Being count should match settings being count value.
        if (beings.size() != beingCount) {
            addError(errors, -1, -1,
                    ErrorType.BeingCountError.format(
                            beings.size(), beingCount));
        }

        palantiri.values().forEach(it -> {
            if (it.getBeingId() != -1L) {
                // This palantir should not be owned by any other beings.
                // i.e., this being should only own a single palantir.
                long count =
                        beings.values().stream()
                                .filter(b -> b.getPalantirId() == it.getId())
                                .count();
                if (count > 1) {
                    addError(errors,
                            it.getBeingId(),
                            it.getId(),
                            ErrorType.PalantirMultipleUseError.format(
                                    it.getId(),
                                    count));
                }
            }
        });

        beings.values().stream().forEach(it -> {
            // Being should only be moving to a valid new state.
            if (!validateBeingState(it)) {
                addError(errors,
                        it.getId(),
                        -1,
                        ErrorType.BeingIllegalStateTransitionError.format(
                                it.getId(),
                                (it.getPrevState() != null ? it.getPrevState() : "null"),
                                it.getState()));
            }

            // We only check for model consistency when a being
            // is in the BUSY state (which breaks down into the
            // 3 states ACQUIRING, GAZING, and RELEASING.
            switch (it.getState()) {
                case ACQUIRING:
                case RELEASING:
                case GAZING: {
                    validateGazingBeing(it, model, errors);
                }
                default:
            }

            // Palantir validation includes:
            // 1. Check that the expected number of palantiri exist.
            // 2. Check that valid previous -> current state is valid.
            // 3. Check that palantir beingId references a valid being.
            // 4. Check that acquired palantir is only allocated to 1 being.
            // 5. Check that palantir usage is fair.
            // 6. Check that all palantir are being used.

            // Palantir count should match settings palantir count value.
            if (palantiri.size() != palantirCount) {
                addError(errors, -1, -1,
                        ErrorType.PalantirCountError.format(
                                palantiri.size(), palantirCount));
            }
        });

        return errors;
    }

    static private boolean validateBeingState(BeingSnapshot snapshot) {
        BeingComponent.State s1 = snapshot.getPrevState();
        BeingComponent.State s2 = snapshot.getState();

        if (s1 == s2) {
            return true;
        }

        if (s2 == null) {
            return false;
        }

        switch (s2) {
            case HOLDING:
                return s1 == null || s1 == DONE;
            case IDLE:
                return s1 == null ||
                        s1 == RELEASING ||
                        s1 == ACQUIRING ||
                        s1 == WAITING ||
                        s1 == GAZING ||
                        s1 == DONE;
            case WAITING:
                return s1 == IDLE || s1 == RELEASING;
            case ACQUIRING:
                return s1 == IDLE || s1 == WAITING;
            case GAZING:
                return s1 == ACQUIRING;
            case RELEASING:
                return s1 == GAZING;
            case DONE:
                return s1 == IDLE || s1 == WAITING || s1 == ERROR;
            case CANCELLED:
            case ERROR:
                return true;
            default: {
                // Framework error.
                return false;
            }
        }
    }

    static private void buildModelAndRun(ModelParameters modelParameters,
                                  Consumer<ModelSnapshot> observe) {
        buildModelAndRun(
                modelParameters.beingManager,
                modelParameters.palantirManager,
                modelParameters.beingCount,
                modelParameters.palantirCount,
                modelParameters.iterations,
                modelParameters.animationSpeed,
                modelParameters.gazingRangeMin,
                modelParameters.gazingRangeMax,
                observe);
    }

    static private void buildModelAndRun(
            BeingManager.Factory.Type beingManager,
            PalantiriManager.Factory.Type palantirManager,
            int beingCount,
            int palantirCount,
            int iterations,
            float animationSpeed,
            int gazingRangeMin,
            int gazingRangeMax,
            Consumer<ModelSnapshot> observer) {

        Simulator simulator = new Simulator(observer::accept);

        simulator.buildModel(beingManager,
                palantirManager,
                beingCount,
                palantirCount,
                iterations);
        Controller.setGazingTimeRange(gazingRangeMin, gazingRangeMax);
        Controller.setSimulationSpeed(animationSpeed);
        simulator.start();
        // Added call to force a shutdown in case the student forgets to
        // shutdown an executor which will hang the JVM between the end
        // of the unit tests and then start of the instrumented tests. This
        // is only an issue when the tests are being run from the autograder.
        simulator.shutdown();
    }

    // Local function for adding errors to error list.
    static void addError(Map<String, Error> map, long beingId, long palantirId, String msg) {
        if (map.containsKey(msg)) {
            map.get(msg).count++;
        } else {
            map.put(msg, new Error(beingId, palantirId, msg, 1));
        }
    }

    static private void validateGazingBeing(
            BeingSnapshot it,
            ModelSnapshot snapshot,
            HashMap<String, Error> errors) {
        Map<Long, PalantirSnapshot> palantiri = snapshot.getPalantiri();

        // Palantir id should be set.
        if (it.getPalantirId() == -1L) {
            addError(errors,
                    it.getId(),
                    -1,
                    ErrorType.BeingNoPalantirId.format(it.getId(), it.getState()));
        } else {
            // The palantir should have an owner.
            PalantirSnapshot palantir = palantiri.get(it.getPalantirId());
            if (palantir == null) {
                addError(errors,
                        it.getId(),
                        it.getPalantirId(),
                        ErrorType.BeingPalantirIdInvalid.format(
                                it.getId(), it.getPalantirId()));
            } else {
                // The being's palantir should have this being as an owner.
                if (palantir.getBeingId() == -1L) {
                    addError(errors,
                            it.getId(),
                            it.getPalantirId(),
                            ErrorType.BeingPalantirDoesNotHaveBeingIdSet
                                    .format(
                                            it.getId(),
                                            it.getPalantirId(),
                                            palantir.getId()));
                } else if (palantir.getBeingId() != it.getId()) {
                    addError(errors,
                            it.getId(),
                            it.getPalantirId(),
                            ErrorType.BeingPalantirDoesNotHaveCorrectBeingId.format(
                                    it.getId(),
                                    it.getPalantirId(),
                                    palantir.getId(),
                                    palantir.getBeingId()));
                }

                // No other palantir should have this being as an owner,
                // i.e., this being should only own a single palantir.
                long count = palantiri.values().stream()
                        .filter(p -> p.getBeingId() == it.getId())
                        .count();
                if (count > 1) {
                    addError(errors,
                            it.getId(),
                            it.getPalantirId(),
                            ErrorType.BeingMultipleUseError.format(
                                    it.getId(),
                                    count));
                }
            }
        }
    }

    // List of all error format strings
    enum ErrorType {
        // used
        BeingCountError("Invalid being count: %d but should be %d."),
        BeingStarvationError("Being starvation: being %d."),
        // used
        BeingIllegalStateTransitionError("Illegal being state transition: being %d %s -> %s."),
        // used
        BeingPalantirIdInvalid("Being %d acquired an unknown palantir %d."),
        // used
        BeingNoPalantirId("Being %d is in state %s but has no associated palantir id"),
        BeingUnfairError("Being %d has not had fair access to palantiri."),
        // used
        BeingMultipleUseError("Being %d is using %d palantiri."),
        PalantirCountError("Invalid palantir count: %d but should be %d."),
        PalantirUnfairError("Palantir %d has not had fair gazing usage."),
        PalantirUnusedError("Palantir %d has never been used."),
        PalantirIllegalTransitionStateError("Illegal palantir state transition: palantir %d %s -> %s."),
        // used
        PalantirMultipleUseError("Palantir %d has been assigned to more than %d beings."),
        // used
        PalantirUsedByWrongBeing("Palantir %d is assigned to being %d but this being is gazing into palantir %d"),
        BeingPalantirDoesNotHaveCorrectBeingId("Being %d owns palantir %d but palantir %d is owned by being %d."),
        BeingPalantirDoesNotHaveBeingIdSet("Being %d owns palantir %d but palantir %d beingId is -1.");

        String error;

        ErrorType(String value) {
            error = value;
        }

        @Override
        public String toString() {
            return error;
        }

        String format(Object... args) {
            return String.format(error, args);
        }
    }

    static class Pair<T1, T2> {
        T1 first;
        T2 second;

        Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    // Helper classes
    static class Error {
        int count;
        long beingId;
        long palantirId;
        String msg;

        Error(long beingId,
              long palantirId,
              String msg,
              int count) {
            this.beingId = beingId;
            this.palantirId = palantirId;
            this.msg = msg;
            this.count = count;
        }
    }

    static class ModelParameters {
        BeingManager.Factory.Type beingManager;
        PalantiriManager.Factory.Type palantirManager;
        int beingCount;
        int palantirCount;
        int iterations;
        float animationSpeed;
        int gazingRangeMin;
        int gazingRangeMax;

        ModelParameters(
                BeingManager.Factory.Type beingManager,
                PalantiriManager.Factory.Type palantirManager,
                int beingCount,
                int palantirCount,
                int iterations,
                float animationSpeed,
                int gazingRangeMin,
                int gazingRangeMax) {

            this.beingManager = beingManager;
            this.palantirManager = palantirManager;
            this.beingCount = beingCount;
            this.palantirCount = palantirCount;
            this.iterations = iterations;
            this.animationSpeed = animationSpeed;
            this.gazingRangeMin = gazingRangeMin;
            this.gazingRangeMax = gazingRangeMax;
        }

        @Override
        public String toString() {
            return "ModelParameters: \n" +
                    "\tbeingManagerType:    " + beingManager + "\n" +
                    "\tpalantirManagerType: " + palantirManager + "\n" +
                    "\tbeingCount:          " + beingCount + "\n" +
                    "\tpalantirCount:       " + palantirCount + "\n" +
                    "\titerations:          " + iterations + "\n" +
                    "\tsimulationSpeed:     " + animationSpeed + "\n" +
                    "\tgazingRange:         [" + gazingRangeMin + ".." + gazingRangeMax + "]\n";
        }
    }
}
