package edu.vandy.simulator

import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantirManager
import edu.vandy.simulator.model.implementation.components.BeingComponent.State
import edu.vandy.simulator.model.implementation.components.SimulatorComponent
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import edu.vandy.simulator.model.interfaces.ModelObserver

fun buildAllPairs(): List<Pair<BeingManager.Factory.Type, PalantirManager.Factory.Type>> {
    return BeingManager.Factory.Type.values()
            .filter {
                it.clazz !=
                        BeingManager.Factory.NoManager::class.java
            }
            .flatMap { bType ->
                PalantirManager.Factory.Type.values()
                        .filter {
                            it.clazz != PalantirManager.Factory.NoManager::class.java
                        }
                        .map { pType ->
                            Pair(bType, pType)
                        }
            }
            .toList()
}

fun testStrategyPairs(
        pairs: List<Pair<BeingManager.Factory.Type,
                PalantirManager.Factory.Type>>,
        beingCount: Int,
        palantirCount: Int,
        iterations: Int,
        animationSpeed: Float,
        gazingRangeMin: Int,
        gazingRangeMax: Int) {
    pairs.forEachIndexed { i, pair ->
        println("\n************ START [${i + 1}/${pairs.count()}] *************\n")
        testStrategy(
                pair.first,
                pair.second,
                beingCount,
                palantirCount,
                iterations,
                animationSpeed,
                gazingRangeMin,
                gazingRangeMax)
        println("************ END [${i + 1}/${pairs.count()}] *************")
    }
}

fun testStrategy(
        beingManager: BeingManager.Factory.Type,
        palantirManager: PalantirManager.Factory.Type,
        beingCount: Int,
        palantirCount: Int,
        iterations: Int,
        animationSpeed: Float,
        gazingRangeMin: Int,
        gazingRangeMax: Int) {

    val modelParameters =
            ModelParameters(
                    beingManager,
                    palantirManager,
                    beingCount,
                    palantirCount,
                    iterations,
                    animationSpeed,
                    gazingRangeMin,
                    gazingRangeMax)
    val allErrors: MutableMap<String, Error> = mutableMapOf()

    println("$modelParameters")

    buildModelAndRun(modelParameters) { snapshot ->
        val errors =
                validateSnapshot(beingCount,
                                 palantirCount,
                                 snapshot)
        allErrors.putAll(errors)
    }

    if (allErrors.count() > 0) {
        println("Found ${allErrors.count()} " +
                "errors for Model:\n$modelParameters")
        allErrors.forEach {
            println(it.value.msg)
        }
        throw IllegalStateException("FAILED model test")
    }
}

private fun buildModelAndRun(
        modelParameters: ModelParameters,
        observe: (snapshot: ModelSnapshot) -> Unit) {
    buildModelAndRun(
            modelParameters.beingManagerType,
            modelParameters.palantirManagerType,
            modelParameters.beingCount,
            modelParameters.palantirCount,
            modelParameters.iterations,
            modelParameters.simulationSpeed,
            modelParameters.gazingRangeMin,
            modelParameters.gazingRangeMax,
            observe)
}

private fun buildModelAndRun(
        beingManagerType: BeingManager.Factory.Type,
        palantirManagerType: PalantirManager.Factory.Type,
        beingCount: Int,
        palantirCount: Int,
        iterations: Int,
        simulationSpeed: Float,
        gazingRangeMin: Int,
        gazingRangeMax: Int,
        observe: (snapshot: ModelSnapshot) -> Unit) {

    val simulator =
            Simulator(ModelObserver { snapshot -> observe(snapshot) })

    simulator.buildModel(beingManagerType,
                         palantirManagerType,
                         beingCount,
                         palantirCount,
                         iterations)
    Controller.setGazingTimeRange(gazingRangeMin, gazingRangeMax)
    Controller.setSimulationSpeed(simulationSpeed)
    simulator.start()
}

private fun validateSnapshot(
        beingCount: Int,
        palantirCount: Int,
        model: ModelSnapshot): Map<String, Error> {

    val errors: MutableMap<String, Error> = mutableMapOf()

    // A cancelling or cancelled model state will never be
    // in a valid state so there is nothing to check.
    if (model.simulator.state == SimulatorComponent.State.CANCELLING ||
        model.simulator.state == SimulatorComponent.State.CANCELLED) {
        return errors
    }

    val beings = model.beings
    val palantiri = model.palantiri

    // Local function for adding errors to error list.
    fun addError(beingId: Long, palantirId: Long, msg: String) {
        errors[msg]?.let {
            it.count++
        } ?: errors.put(msg, Error(beingId, palantirId, msg, 1))
    }

    // Being validation includes:
    // 1. Check that the expected number of beings exist.
    // 2. Check that valid previous -> current state is valid.
    // 3. Check that being palantirId references a valid palantir.
    // 4. Check that gazing beings have a valid palantir id.
    // 5. Check that only 1 being has any given palantir id.
    // 6. Check for starved beings.
    // 7. Check for unfair gazing bias.

    // Being count should match settings being count value.
    if (beings.count() != beingCount) {
        addError(-1, -1,
                 ErrorType.BeingCountError.format(
                         beings.count(), beingCount))
    }

    palantiri.values.forEach {
        if (it.beingId != -1L) {
            // This palantir should not be owned by any other beings.
            // i.e., this being should only own a single palantir.
            val count = beings.filterValues { b ->
                b.palantirId == it.id
            }.count()
            if (count > 1) {
                addError(it.beingId,
                         it.id,
                         ErrorType.PalantirMultipleUseError.format(
                                 it.id.toInt(),
                                 count))
            }
        }
    }

    beings.values.forEach {
        // Being should only be moving to a valid new state.
        if (!validateBeingState(it)) {
            addError(it.id,
                     -1,
                     ErrorType.BeingIllegalStateTransitionError.format(
                             it.id.toInt(), it.prevState?.name ?: "null", it.state.name))
        }

        // We only check for model consistency when a being
        // is in the BUSY state (which breaks down into the
        // 3 states ACQUIRING, GAZING, and RELEASING.
        when (it.state) {
            State.ACQUIRING,
            State.RELEASING,
            State.GAZING -> {
                validateGazingBeing(it, model, errors)
            }
            else -> {
            }
        }

        // Palantir validation includes:
        // 1. Check that the expected number of palantiri exist.
        // 2. Check that valid previous -> current state is valid.
        // 3. Check that palantir beingId references a valid being.
        // 4. Check that acquired palantir is only allocated to 1 being.
        // 5. Check that palantir usage is fair.
        // 6. Check that all palantir are being used.

        // Palantir count should match settings palantir count value.
        if (palantiri.count() != palantirCount) {
            addError(-1, -1,
                     ErrorType.PalantirCountError.format(
                             palantiri.count(), palantirCount))
        }
    }

    return errors
}

private fun validateGazingBeing(
        it: BeingSnapshot,
        snapshot: ModelSnapshot,
        errors: MutableMap<String, Error>) {
    val palantiri = snapshot.palantiri

    // Local function for adding errors to error list.
    fun addError(beingId: Long, palantirId: Long, msg: String) {
        errors[msg]?.let {
            it.count++
        } ?: errors.put(msg, Error(beingId, palantirId, msg, 1))
    }

    // Palantir id should be set.
    if (it.palantirId == -1L) {
        addError(it.id,
                 -1,
                 ErrorType.BeingNoPalantirId.format(it.id.toInt(), it.state))
    } else {
        // The palantir should have an owner.
        val palantir = palantiri[it.palantirId]
        if (palantir == null) {
            addError(it.id,
                     it.palantirId,
                     ErrorType.BeingPalantirIdInvalid.format(
                             it.id.toInt(), it.palantirId.toInt()))
        } else {
            // The being's palantir should have this being as an owner.
            if (palantir.beingId == -1L) {
                addError(it.id,
                         it.palantirId,
                         ErrorType.BeingPalantirDoesNotHaveBeingIdSet
                                 .format(
                                         it.id.toInt(),
                                         it.palantirId.toInt(),
                                         palantir.id))
            } else if (palantir.beingId != it.id) {
                addError(it.id,
                         it.palantirId,
                         ErrorType.BeingPalantirDoesNotHaveCorrectBeingId.format(
                                 it.id.toInt(),
                                 it.palantirId.toInt(),
                                 palantir.id,
                                 palantir.beingId))
            }

            // No other palantir should have this being as an owner,
            // i.e., this being should only own a single palantir.
            val count = palantiri.filterValues { p -> p.beingId == it.id }.count()
            if (count > 1) {
                addError(it.id,
                         it.palantirId,
                         ErrorType.BeingMultipleUseError.format(
                                 it.id.toInt(),
                                 count))
            }
        }
    }
}

private fun validateBeingState(snapshot: BeingSnapshot): Boolean {
    val s1 = snapshot.prevState
    val s2 = snapshot.state
    if (s1 == s2) {
        return true
    }

    return when (s2) {
        State.HOLDING -> s1 == null || s1 == State.DONE
        State.IDLE -> s1 == null ||
                      s1 == State.RELEASING ||
                      s1 == State.ACQUIRING ||
                      s1 == State.WAITING ||
                      s1 == State.GAZING ||
                      s1 == State.DONE
        State.WAITING -> s1 == State.IDLE || s1 == State.RELEASING
        State.ACQUIRING -> s1 == State.IDLE || s1 == State.WAITING
        State.GAZING -> s1 == State.ACQUIRING
        State.RELEASING -> s1 == State.GAZING
        State.DONE -> s1 == State.IDLE || s1 == State.WAITING || s1 == State.ERROR
        State.CANCELLED,
        State.ERROR -> {
            true
        }
        null -> {
            // Framework error.
            error("State should never be null")
        }
        else -> {
            // Framework error.
            error("State $s2 is not supported by simulatorView.")
        }
    }
}

// Helper classes
data class Error(
        val beingId: Long,
        val palantirId: Long,
        val msg: String,
        var count: Int)

data class ModelParameters(
        val beingManagerType: BeingManager.Factory.Type,
        val palantirManagerType: PalantirManager.Factory.Type,
        val beingCount: Int = 10,
        val palantirCount: Int = 6,
        val iterations: Int = 10,
        val simulationSpeed: Float = 0f,
        val gazingRangeMin: Int = 0,
        val gazingRangeMax: Int = 0) {
    override fun toString(): String {
        return "ModelParameters: \n" +
               "\tbeingManagerType:    $beingManagerType,\n" +
               "\tpalantirManagerType: $palantirManagerType,\n" +
               "\tbeingCount:          $beingCount,\n" +
               "\tpalantirCount:       $palantirCount,\n" +
               "\titerations:          $iterations\n" +
               "\tsimulationSpeed:     $simulationSpeed\n" +
               "\tgazingRange:         [${gazingRangeMin}..${gazingRangeMax}]\n"
    }
}

// List of all error format strings
enum class ErrorType(private val error: String) {
    // used
    BeingCountError(
            "Invalid being count: %d but should be %d."),
    BeingStarvationError(
            "Being starvation: being %d."),
    // used
    BeingIllegalStateTransitionError(
            "Illegal being state transition: being %d %s -> %s."),
    // used
    BeingPalantirIdInvalid(
            "Being %d acquired an unknown palantir %d."),
    // used
    BeingNoPalantirId(
            "Being %d is in state %s but has no associated palantir id"),
    BeingUnfairError(
            "Being %d has not had fair access to palantiri."),
    // used
    BeingMultipleUseError(
            "Being %d is using %d palantiri."),
    PalantirCountError(
            "Invalid palantir count: %d but should be %d."),
    PalantirUnfairError(
            "Palantir %d has not had fair gazing usage."),
    PalantirUnusedError(
            "Palantir %d has never been used."),
    PalantirIllegalTransitionStateError(
            "Illegal palantir state transition: palantir %d %s -> %s."),
    // used
    PalantirMultipleUseError(
            "Palantir %d has been assigned to more than %d beings."),
    // used
    PalantirUsedByWrongBeing(
            "Palantir %d is assigned to being %d but this being is gazing into palantir %d"),
    BeingPalantirDoesNotHaveCorrectBeingId(
            "Being %d owns palantir %d but palantir %d is owned by being %d."),
    BeingPalantirDoesNotHaveBeingIdSet(
            "Being %d owns palantir %d but palantir %d beingId is -1.");

    override fun toString(): String = error

    fun format(vararg args: Any) = error.format(*args)
}
