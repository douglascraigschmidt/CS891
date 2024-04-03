package edu.vandy.recommender.client.proxies;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping
import test.admin.hasMethodAnnotation

class TimerAPITest {

    @Test
    fun getTimings() {
        TimerAPI::class.java.hasMethodAnnotation(
            method = "getTimings",
            params = arrayOf(String::class.java),
            annotationClass = GetMapping::class.java,
            validate = { it.value[0] == "routename" },
            onlyAnnotation = true
        )
    }
}