package io.pivotal.workshop.pcc.sizer.controller;

import io.pivotal.workshop.pcc.sizer.generator.GeneratorType;
import io.pivotal.workshop.pcc.sizer.repository.RepositoryType;
import io.pivotal.workshop.pcc.sizer.service.SizerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@AllArgsConstructor
@RestController
public class SizerRestController {

    private final SizerService sizerService;

    @PostMapping("/load")
    @ResponseStatus(CREATED)
    public void load(@RequestParam GeneratorType generatorType,
                           @RequestParam RepositoryType repositoryType,
                           @RequestParam(defaultValue = "5000") int numberOfRecords) {
        sizerService.load(generatorType, repositoryType, numberOfRecords);
    }

    @PostMapping("/accurate")
    @ResponseStatus(CREATED)
    public String accurate(@RequestParam GeneratorType generatorType,
                           @RequestParam RepositoryType repositoryType,
                           @RequestParam(defaultValue = "5000") int numberOfRecords) {
        return sizerService.accurate(generatorType, repositoryType, numberOfRecords);
    }

    @PostMapping("/estimate")
    @ResponseStatus(CREATED)
    public String estimate(@RequestParam GeneratorType generatorType,
                           @RequestParam RepositoryType repositoryType,
                           @RequestParam(defaultValue = "5000") int expectedNumberOfRecords) {
        return sizerService.estimate(generatorType, repositoryType, expectedNumberOfRecords);
    }
}
