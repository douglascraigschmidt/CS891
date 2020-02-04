package edu.vandy.app.ui.screens.main

import edu.vandy.simulator.managers.beings.BeingManager.Factory.Type.RUNNABLE_THREADS
import edu.vandy.simulator.managers.palantiri.PalantiriManager.Factory.Type.ARRAY_BLOCKING_QUEUE

class Assignment_1A_InstrumentedTests: InstrumentedTests() {
    override val beingManager = RUNNABLE_THREADS
    override val palantirManager = ARRAY_BLOCKING_QUEUE
}