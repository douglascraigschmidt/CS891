package edu.vandy.app.ui.screens.main

import edu.vandy.simulator.managers.beings.BeingManager.Factory.Type.COMPLETION_SERVICE
import edu.vandy.simulator.managers.palantiri.PalantiriManager.Factory.Type.REENTRANT_LOCK_HASH_MAP_SIMPLE_SEMAPHORE

class Assignment_3A_InstrumentedTests : InstrumentedTests() {
    override val beingManager = COMPLETION_SERVICE
    override val palantirManager = REENTRANT_LOCK_HASH_MAP_SIMPLE_SEMAPHORE
}