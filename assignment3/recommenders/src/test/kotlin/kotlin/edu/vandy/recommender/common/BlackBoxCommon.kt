package edu.vandy.recommender.common

import edu.vandy.recommender.common.CosineSimilarityUtils.sumOfCosines
import edu.vandy.recommender.common.model.Ranking
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
object BlackBoxCommon {
    fun getRecommendationsSingleBlackBox(
        vectorMap: MutableMap<String, List<Double>>,
        size: Int = 1_000,
        block: (String, Int) -> List<Ranking>
    ) {
        repeat(1) {
            val attributes = 20
            val max = Random.nextInt(20, max(20, size / 2))
            val map = (1..size).associate {
                val vector =
                    (1..attributes).map { Random.nextDouble(-0.9, 0.9) }
                "movie$it" to vector
            }.toMutableMap()

            val input =
                map.entries.toList()[Random.nextInt(0, map.entries.size)]

            every { vectorMap.entries } answers { map.entries }
            every { vectorMap[input.key] } answers { map[input.key] }
            every { vectorMap.containsKey(any()) } answers { true }

            val expected =
                vectorMap.entries
                    .asSequence()
                    .filterNot { it.key == input.key }
                    .map {
                        val similarity =
                            CosineSimilarityUtils.cosineSimilarity(
                                input.value,
                                it.value,
                                false
                            )
                        Ranking(it.key, similarity)
                    }
                    .sortedByDescending { it.cosineSimilarity }
                    .distinct()
                    .take(max)
                    .toList()
                    .map { it }

            assertThat(expected.firstOrNull { it.title == input.key }).isNull()
            assertThat(expected).hasSize(max)
            assertThat(expected.distinct()).hasSameSizeAs(expected)

            val results: List<Ranking>

            results = block(input.key, max)

            try {
                assertThat(results).isEqualTo(expected)
            } finally {
                clearMocks(vectorMap)
                clearAllMocks()
            }
        }

        clearAllMocks()
    }

    fun getRecommendationsMultipleBlackBoxTest(
        vectorMap: MutableMap<String, List<Double>>,
        size: Int = 1_000,
        runTestCode: (List<String>, Int) -> List<Ranking>
    ) {
        repeat(1) {
            val attributes = 20
            val map = (1..size).associate {
                val vector =
                    (1..attributes).map { Random.nextDouble(-0.9, 0.9) }
                "movie$it" to vector
            }.toMutableMap()

            check(size / 2 > 2)
            val inputCount = Random.nextInt(20, 21)
            val input = mutableListOf<String>()
            repeat(inputCount) {
                var key: String
                do {
                    key = map.randomEntry.key
                } while (input.contains(key))
                input.add(key)
            }

            val max = Random.nextInt(2, size)

            assertThat(input.size == input.distinct().size)

            every { vectorMap.entries } answers { map.entries }
            every { vectorMap[any()] } answers {
                map[firstArg()]
            }
            every { vectorMap.containsKey(any()) } answers { true }

            val expected: List<Ranking>
            val t1 = measureTime {
                expected = vectorMap
                    .asSequence()
                    .filterNot { input.contains(it.key) }
                    .map { entry ->
                            val similarity =
                                sumOfCosines(
                                    entry.value,
                                    input,
                                    vectorMap,
                                    true
                                )
                            Ranking(entry.key, similarity)
                        }
                    .sortedByDescending { it.cosineSimilarity }
                    .distinctBy { it.title }
                    .take(max)
                    .toList()
            }

            assertThat(expected.map { it.title }).doesNotContainAnyElementsOf(
                input
            )
            check(expected.size == min(map.size - input.size, max))
            check(expected.distinct().size == expected.size)

            println("input = ${input.size} map = ${map.size} max = $max ... ")

            val results: List<Ranking>
            val t2 = measureTime {
                results = runTestCode(input, max)
            }

            if (t1 < t2) {
                println("[Kotlin]: $t1")
                println("Solution: $t2")
                println("[Kotlin] is faster")
            } else {
                println("Kotlin: $t1")
                println("[Solution]: $t2")
                println("[Solution] is faster")
            }

            if (results != expected) {
                when {
                    results.intersect(input.toSet()).isNotEmpty() ->
                        fail<String>("The returned recommended list contains already watched titles.")

                    results.distinct().size != results.size ->
                        fail<String>("The returned recommended list contains duplicate titles.")

                    results.size < expected.size ->
                        fail<String>("The returned recommended list contains too few items.")

                    results.size > expected.size ->
                        fail<String>("The returned recommended list contains too many items.")

                    else ->
                        fail<String>(
                            "The returned recommended list does not contain the expected items\n" +
                                    "and/or the expected items are not in the expected order."
                        )
                }
            }

            clearMocks(vectorMap)
            clearAllMocks()

        }

        clearAllMocks()
    }

    private val <T> Collection<T>.randomIndex: Int
        get() = Random.nextInt(0, size)

    private val <T> Collection<T>.randomEntry: T
        get() = elementAt(randomIndex)

    private val <S, T> Map<S, T>.randomEntry: Map.Entry<S, T>
        get() = entries.randomEntry
}