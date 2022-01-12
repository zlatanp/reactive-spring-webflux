package com.reactive.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class FluxAndMonoGeneratorService {
    public Flux<String> namesFlux() {    // publisher
        return Flux.fromIterable(List.of("alex", "ben", "cloe")) // db or remote service call
                .map(s -> s.toUpperCase())
                .log();
    }

    public Mono<String> nameMono() {     // publisher
        return Mono.just("alex").log();
    }
}
