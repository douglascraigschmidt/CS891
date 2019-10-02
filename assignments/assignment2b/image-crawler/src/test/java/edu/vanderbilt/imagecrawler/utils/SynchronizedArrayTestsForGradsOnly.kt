package edu.vanderbilt.imagecrawler.utils

import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
class SynchronizedArrayTestsForGradsOnly : SynchronizedArrayTests() {
    override val runTest = isGradAssignment()
}