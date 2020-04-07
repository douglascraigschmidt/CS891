package admin

import admin.TestHelper.ErrorType.*
import edu.vandy.simulator.Controller
import edu.vandy.simulator.Simulator
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager
import edu.vandy.simulator.model.implementation.components.BeingComponent
import edu.vandy.simulator.model.implementation.components.BeingComponent.State.*
import edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.CANCELLED
import edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.CANCELLING
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import edu.vandy.simulator.model.interfaces.ModelObserver
import java.util.*
import java.util.function.Consumer

object TestHelper {
    fun buildAllPairs() =
            BeingManager.Factory.Type.values().flatMap { being ->
                PalantiriManager.Factory.Type.values().map { palantiri ->
                    Pair(being, palantiri)
                }
            }

    fun testStrategyPairs(
            pairs: List<Pair<BeingManager.Factory.Type, PalantiriManager.Factory.Type>>,
            beingCount: Int,
            palantirCount: Int,
            threadCount: Int,
            iterations: Int,
            animationSpeed: Float,
            gazingRangeMin: Int,
            gazingRangeMax: Int) {
        for (i in pairs.indices) {
            println("\n************ START [" +
                    (i + 1) + "/" + pairs.size + "] *************\n")
            testStrategy(
                    pairs[i].first,
                    pairs[i].second,
                    beingCount,
                    threadCount,
                    palantirCount,
                    iterations,
                    animationSpeed,
                    gazingRangeMin,
                    gazingRangeMax)
            println("\n************ END [" +
                    (i + 1) + "/" + pairs.size + "] *************\n")
        }
    }

    fun testStrategy(
            beingManager: BeingManager.Factory.Type,
            palantirManager: PalantiriManager.Factory.Type,
            beingCount: Int,
            palantirCount: Int,
            threadCount: Int,
            iterations: Int,
            animationSpeed: Float,
            gazingRangeMin: Int,
            gazingRangeMax: Int) {

        val modelParameters = ModelParameters(
                beingManager,
                palantirManager,
                beingCount,
                palantirCount,
                threadCount,
                iterations,
                animationSpeed,
                gazingRangeMin, gazingRangeMax)

        val allErrors = HashMap<String, Error?>()

        buildModelAndRun(modelParameters,
                Consumer { snapshot ->
                    allErrors.putAll(validateSnapshot(beingCount, palantirCount, snapshot))
                })

        if (allErrors.size > 0) {
            println("Found " + allErrors.size + "errors for Model:\n" + modelParameters)
            allErrors.values.forEach(Consumer { println(it!!.msg) })
            throw IllegalStateException("FAILED model test")
        }
    }

    private fun validateSnapshot(beingCount: Int, palantirCount: Int, model: ModelSnapshot)
            : Map<String, Error?> {

        val errors = HashMap<String, Error?>()

        // A cancelling or cancelled model state will never be
        // in a valid state so there is nothing to check.
        if (model.simulator.state == CANCELLING ||
                model.simulator.state == CANCELLED) {
            return errors
        }

        val beings = model.beings
        val palantiri = model.palantiri

        // Being validation includes:
        // 1. Check that the expected number of beings exist.
        // 2. Check that valid previous -> current state is valid.
        // 3. Check that being palantirId references a valid palantir.
        // 4. Check that gazing beings have a valid palantir id.
        // 5. Check that only 1 being has any given palantir id.
        // 6. Check for starved beings.
        // 7. Check for unfair gazing bias.

        // Being count should match settings being count value.
        if (beings.size != beingCount) {
            addError(errors, -1, -1, BeingCountError.format(beings.size, beingCount))
        }

        palantiri.values.forEach { palantir ->
            if (palantir.beingId != -1L) {
                // This palantir should not be owned by any other beings.
                // i.e., this being should only own a single palantir.
                val count = beings.values.count { being ->
                    being.palantirId == palantir.id
                }

                if (count > 1) {
                    addError(errors, palantir.beingId, palantir.id,
                            PalantirMultipleUseError.format(palantir.id, count))
                }
            }
        }

        beings.values.stream().forEach { being ->
            // Being should only be moving to a valid new state.
            if (!validateBeingState(being)) {
                addError(errors, being.id, -1,
                        BeingIllegalStateTransitionError.format(
                                being.id,
                                if (being.prevState != null) being.prevState else "null",
                                being.state))
            }

            when (being.state) {
                ACQUIRING, RELEASING, GAZING -> validateGazingBeing(being, model, errors)
                else -> Unit
            }

            // Palantir validation includes:
            // 1. Check that the expected number of palantiri exist.
            // 2. Check that valid previous -> current state is valid.
            // 3. Check that palantir beingId references a valid being.
            // 4. Check that acquired palantir is only allocated to 1 being.
            // 5. Check that palantir usage is fair.
            // 6. Check that all palantir are being used.

            // Palantir count should match settings palantir count value.
            if (palantiri.size != palantirCount) {
                addError(errors, -1, -1, PalantirCountError.format(palantiri.size, palantirCount))
            }
        }

        return errors
    }

    private fun validateBeingState(snapshot: BeingSnapshot): Boolean {
        val s1 = snapshot.prevState
        val s2 = snapshot.state

        if (s1 == s2) {
            return true
        }

        return if (s2 == null) {
            false
        } else when (s2) {
            HOLDING -> s1 == null || s1 == DONE
            IDLE -> s1 == null || s1 == RELEASING || s1 == ACQUIRING || s1 == WAITING || s1 == GAZING || s1 == DONE
            WAITING -> s1 == IDLE || s1 == RELEASING
            ACQUIRING -> s1 == IDLE || s1 == WAITING
            GAZING -> s1 == ACQUIRING
            RELEASING -> s1 == GAZING
            DONE -> s1 == IDLE || s1 == WAITING || s1 == ERROR
            BeingComponent.State.CANCELLED, ERROR -> true
            else -> {
                // Framework error.
                false
            }
        }
    }

    private fun buildModelAndRun(modelParameters: ModelParameters, observe: Consumer<ModelSnapshot>) {
        buildModelAndRun(
                modelParameters.beingManager,
                modelParameters.palantirManager,
                modelParameters.beingCount,
                modelParameters.palantirCount,
                modelParameters.threadCount,
                modelParameters.iterations,
                modelParameters.animationSpeed,
                modelParameters.gazingRangeMin,
                modelParameters.gazingRangeMax,
                observe)
    }

    private fun buildModelAndRun(
            beingManager: BeingManager.Factory.Type,
            palantirManager: PalantiriManager.Factory.Type,
            beingCount: Int,
            palantirCount: Int,
            threadCount: Int,
            iterations: Int,
            animationSpeed: Float,
            gazingRangeMin: Int,
            gazingRangeMax: Int,
            observer: Consumer<ModelSnapshot>) {
        val simulator = Simulator(ModelObserver { t: ModelSnapshot -> observer.accept(t) })
        simulator.buildModel(beingManager,
                palantirManager,
                beingCount,
                palantirCount,
                threadCount,
                iterations)
        Controller.setGazingTimeRange(gazingRangeMin, gazingRangeMax)
        Controller.setSimulationSpeed(animationSpeed)

        simulator.start()

        // Added call to force a shutdown in case the student forgets to
        // shutdown an executor which will hang the JVM between the end
        // of the unit tests and then start of the instrumented tests. This
        // is only an issue when the tests are being run from the autograder.
        simulator.shutdown()
    }

    // Local function for adding errors to error list.
    fun addError(map: MutableMap<String, Error?>, beingId: Long, palantirId: Long, msg: String) {
        if (map.containsKey(msg)) {
            map[msg]!!.count++
        } else {
            map[msg] = Error(beingId, palantirId, msg, 1)
        }
    }

    private fun validateGazingBeing(beingSnapshot: BeingSnapshot, snapshot: ModelSnapshot, errors: HashMap<String, Error?>) {
        val palantiri = snapshot.palantiri
        // Palantir id should be set.
        with(beingSnapshot) {
            if (palantirId == -1L) {
                addError(errors,
                        id,
                        -1,
                        BeingNoPalantirId.format(id, state))
            } else { // The palantir should have an owner.
                val palantir = palantiri[palantirId]
                if (palantir == null) {
                    addError(errors,
                            id,
                            palantirId,
                            BeingPalantirIdInvalid.format(id, palantirId))
                } else { // The being's palantir should have this being as an owner.
                    if (palantir.beingId == -1L) {
                        addError(errors,
                                id,
                                palantirId,
                                BeingPalantirDoesNotHaveBeingIdSet.format(id, palantirId, palantir.id))
                    } else if (palantir.beingId != id) {
                        addError(errors,
                                id,
                                palantirId,
                                BeingPalantirDoesNotHaveCorrectBeingId.format(
                                        id,
                                        palantirId,
                                        palantir.id,
                                        palantir.beingId))
                    }

                    // No other palantir should have this being as an owner,
                    // i.e., this being should only own a single palantir.
                    val count = palantiri.values.count { it.beingId == id }

                    if (count > 1) {
                        addError(errors, id, palantirId, BeingMultipleUseError.format(id, count))
                    }
                }
            }
        }
    }

    // List of all error format strings
    internal enum class ErrorType(var error: String) {
        // used
        BeingCountError("Invalid being count: %d but should be %d."),
        BeingStarvationError("Being starvation: being %d."),  // used
        BeingIllegalStateTransitionError("Illegal being state transition: being %d %s -> %s."),  // used
        BeingPalantirIdInvalid("Being %d acquired an unknown palantir %d."),  // used
        BeingNoPalantirId("Being %d is in state %s but has no associated palantir id"), BeingUnfairError("Being %d has not had fair access to palantiri."),  // used
        BeingMultipleUseError("Being %d is using %d palantiri."), PalantirCountError("Invalid palantir count: %d but should be %d."), PalantirUnfairError("Palantir %d has not had fair gazing usage."), PalantirUnusedError("Palantir %d has never been used."), PalantirIllegalTransitionStateError("Illegal palantir state transition: palantir %d %s -> %s."),  // used
        PalantirMultipleUseError("Palantir %d has been assigned to more than %d beings."),  // used
        PalantirUsedByWrongBeing("Palantir %d is assigned to being %d but this being is gazing into palantir %d"), BeingPalantirDoesNotHaveCorrectBeingId("Being %d owns palantir %d but palantir %d is owned by being %d."), BeingPalantirDoesNotHaveBeingIdSet("Being %d owns palantir %d but palantir %d beingId is -1.");

        override fun toString(): String {
            return error
        }

        fun format(vararg args: Any?): String {
            return String.format(error, *args)
        }

    }

    class Pair<T1, T2> internal constructor(var first: T1, var second: T2)

    // Helper classes
    class Error(var beingId: Long,
                var palantirId: Long,
                var msg: String,
                var count: Int)

    internal class ModelParameters(
            var beingManager: BeingManager.Factory.Type,
            var palantirManager: PalantiriManager.Factory.Type,
            var beingCount: Int,
            var palantirCount: Int,
            var threadCount: Int,
            var iterations: Int,
            var animationSpeed: Float,
            var gazingRangeMin: Int,
            var gazingRangeMax: Int) {
        override fun toString(): String {
            return "ModelParameters: \n" +
                    "\tbeingManagerType:    " + beingManager + "\n" +
                    "\tpalantirManagerType: " + palantirManager + "\n" +
                    "\tbeingCount:          " + beingCount + "\n" +
                    "\tthreadCount:         " + beingCount + "\n" +
                    "\tpalantirCount:       " + palantirCount + "\n" +
                    "\titerations:          " + iterations + "\n" +
                    "\tsimulationSpeed:     " + animationSpeed + "\n" +
                    "\tgazingRange:         [" + gazingRangeMin + ".." + gazingRangeMax + "]\n"
        }

    }
}