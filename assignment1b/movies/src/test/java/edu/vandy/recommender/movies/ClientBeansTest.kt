package edu.vandy.recommender.movies

import edu.vandy.recommender.movies.client.ClientBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import server.hasMethodAnnotation

class ClientBeansTest {
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