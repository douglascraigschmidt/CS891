package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseControllerTimed;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static edu.vandy.recommender.common.Constants.EndPoint.TIMED;

/**
 * The Spring WebFlux controller for the {@link ParallelFluxService}
 * that handles timed method invocations.
 */
@RestController
@RequestMapping(TIMED)
public class ParallelFluxControllerTimed
    extends BaseControllerTimed<Flux<Ranking>> {
}
