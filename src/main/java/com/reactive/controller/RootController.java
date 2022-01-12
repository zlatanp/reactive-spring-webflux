package com.reactive.controller;

import com.reactive.service.FluxAndMonoGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class RootController {

    @Autowired
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService;

    @GetMapping(path = "/flux", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getFluxNames() {
        var names = fluxAndMonoGeneratorService.namesFlux()
                .subscribe();

        return List.of("null");
    }

    @GetMapping(path = "/mono", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMonoName() {
        var names = fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> {
                });

        return "null";
    }
}
