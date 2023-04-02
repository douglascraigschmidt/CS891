package edu.vandy.recommender.databaseex

import edu.vandy.recommender.databaseex.client.ClientBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import test.admin.AssignmentTests

class ClientBeansTest : AssignmentTests() {
    @Test
    fun `clientBeans is implemented correctly`() {
        assertThat(
            with(ClientBeans::class.java.declaredMethods) {
                all {
                    it.declaredAnnotations.count {
                        it.annotationClass.java == Bean::class.java
                    } == 1
                }
            }).isTrue
    }
}