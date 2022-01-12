package com.reactive;

import com.reactive.service.FluxAndMonoGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class ReactiveSpringWebfluxApplicationTests {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        //	given

        //	when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        //	then
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CLOE")
                .verifyComplete();
    }

}
