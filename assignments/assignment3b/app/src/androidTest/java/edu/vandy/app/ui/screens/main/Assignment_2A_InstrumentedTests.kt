package edu.vandy.app.ui.screens.main

import edu.vandy.simulator.managers.beings.BeingManager.Factory.Type.EXECUTOR_SERVICE
import edu.vandy.simulator.managers.palantiri.PalantiriManager.Factory.Type.SPIN_LOCK_SEMAPHORE

class Assignment_2A_InstrumentedTests : InstrumentedTests() {
    override val beingManager = EXECUTOR_SERVICE
    override val palantirManager = SPIN_LOCK_SEMAPHORE
}