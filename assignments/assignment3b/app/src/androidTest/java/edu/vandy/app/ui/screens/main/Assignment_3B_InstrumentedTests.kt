package edu.vandy.app.ui.screens.main

import edu.vandy.simulator.managers.beings.BeingManager.Factory.Type.COMPLETION_SERVICE
import edu.vandy.simulator.managers.palantiri.PalantiriManager.Factory.Type.CONCURRENT_MAP_FAIR_SEMAPHORE

class Assignment_3B_InstrumentedTests : InstrumentedTests() {
    override val beingManager = COMPLETION_SERVICE
    override val palantirManager = CONCURRENT_MAP_FAIR_SEMAPHORE
}