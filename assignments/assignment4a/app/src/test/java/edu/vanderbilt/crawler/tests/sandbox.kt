package edu.vanderbilt.crawler.tests

import org.junit.Test

/**
 * Created by monte on 03/11/17.
 */
class Sandbox {
    @Test
    fun testRound() {
        var input = 1540
        println("$input -> ${round(input)}")
        input = 1499
        println("$input -> ${round(input)}")
        input = 2501
        println("$input -> ${round(input)}")
        input = 2601
        println("$input -> ${round(input)}")
        input = 0
        println("$input -> ${round(input)}")
        input = 1
        println("$input -> ${round(input)}")
        input = 499
        println("$input -> ${round(input)}")
        input = 500
        println("$input -> ${round(input)}")
        input = 501
        println("$input -> ${round(input)}")
    }

    fun round(value: Int): Int {
        val result = Math.round(value.toFloat() / 1000) * 1000
        return result
    }
}
