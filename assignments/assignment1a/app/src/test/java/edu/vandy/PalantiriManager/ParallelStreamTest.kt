package edu.vandy.PalantiriManager

import org.junit.Test

class ParallelStreamTest {
    @Test
    fun doTest() {
        val x = ArrayList<Int>()
        (1..10).forEach {
            x.add(it)
        }

        x.parallelStream()
                .filter {it > 3}
                .forEach {
                    println("${Thread.currentThread().name} -> $it")
                }
    }
}
