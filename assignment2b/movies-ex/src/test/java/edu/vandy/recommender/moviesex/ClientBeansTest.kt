package edu.vandy.recommender.moviesex

import edu.vandy.recommender.moviesex.client.ClientBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import test.admin.AssignmentTests
import test.admin.hasMethodAnnotation

class ClientBeansTest: AssignmentTests() {
    @Test
    fun `getMoviesRestTemplate has correct annotation`() {
        assertThat(
            ClientBeans::class.java.hasMethodAnnotation(
                "getMoviesRestTemplate",
                annotationClass = Bean::class.java,
                onlyAnnotation = true,
            )
        ).isTrue
    }

//    @Test
//    fun testParameters() {
//        TransformController::class.java
//            .declaredMethods.find {
//                it.name.endsWith("applyTransform")
//            }!!.apply {
//                val p =
//                    listOf(RequestPart::class.java, RequestParam::class.java)
//                repeat(p.size) {
//                    assertThat(parameters[it].declaredAnnotations).hasSize(1)
//                    assertThat(
//                        parameters[it].isAnnotationPresent(
//                            p[(it + 1).mod(
//                                2
//                            )]
//                        )
//                    ).isTrue
//                }
//            }
//    }
}