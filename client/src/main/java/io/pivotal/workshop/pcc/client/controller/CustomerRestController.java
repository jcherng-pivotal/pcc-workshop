package io.pivotal.workshop.pcc.client.controller;

import io.pivotal.workshop.pcc.entity.Customer;
import io.pivotal.workshop.pcc.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@AllArgsConstructor
@RestController
public class CustomerRestController {

    private final CustomerRepository repository;

    @PostMapping("/customers")
    @ResponseStatus(CREATED)
    public void create(@RequestBody Customer customer) {
        repository.save(customer);
    }

    @GetMapping("/customers/{customerId}")
    @ResponseStatus(OK)
    public Customer read(@PathVariable("customerId") String customerId) {
        return repository.findById(customerId).orElse(null);
    }

    @PutMapping("/customers/{customerId}")
    @ResponseStatus(OK)
    public Customer update(@PathVariable("customerId") String customerId,
                           @RequestBody Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/customers/{customerId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("customerId") String customerId) {
        repository.deleteById(customerId);
    }
}
