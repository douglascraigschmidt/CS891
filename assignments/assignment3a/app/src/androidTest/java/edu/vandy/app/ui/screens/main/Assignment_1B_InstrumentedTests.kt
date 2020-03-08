package edu.vandy.app.ui.screens.main

import edu.vandy.simulator.managers.beings.BeingManager.Factory.Type.RUNNABLE_THREADS
import edu.vandy.simulator.managers.palantiri.PalantiriManager.Factory.Type.SPIN_LOCK_SEMAPHORE

class Assignment_1B_InstrumentedTests : InstrumentedTests() {
    override val beingManager = RUNNABLE_THREADS
    override val palantirManager = SPIN_LOCK_SEMAPHORE
}