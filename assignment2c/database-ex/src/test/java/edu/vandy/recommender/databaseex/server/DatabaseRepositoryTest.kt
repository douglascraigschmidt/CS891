package edu.vandy.recommender.databaseex.server

import edu.vandy.recommender.databaseex.repository.DatabaseRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

//@WebMvcTest
//@ContextConfiguration(classes = [DatabaseApplication::class, ])
//@ComponentScan(basePackageClasses = [DatabaseApplication::class])
class DatabaseRepositoryTest {

    @Test
    fun `DatabaseRepository is implemented correctly`() {
        DatabaseRepository::class.java.declaredMethods.filter {
            it.name.toByteArray().toList().let { it == a || it == b }
        }.size.let {
            assertThat(it).isEqualTo(2)
        }
    }

    val a = listOf<Byte>(102,105,110,100,66,121,73,100,67,111,110,116,97,105,110,105,110,103,73,103,110,111,114,101,67,97,115,101,79,114,100,101,114,66,121,73,100,65,115,99)
    val b = listOf<Byte>(102,105,110,100,65,108,108,66,121,79,114,100,101,114,66,121,73,100,65,115,99)
}