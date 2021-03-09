package com.example.demo01;


import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class Demo01Application {

	private static final Logger log = LoggerFactory.getLogger(Demo01Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Demo01Application.class, args);
	}

	@Bean
	public CommandLineRunner demo(CustomerRepository repository) {
		return (args) -> {
			// save a few customers
			repository.save(new Customer("Jack", "Bauer"));
			repository.save(new Customer("Chloe", "O'Brian"));
			repository.save(new Customer("Kim", "Bauer"));
			repository.save(new Customer("David", "Palmer"));
			repository.save(new Customer("Michelle", "Dessler"));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (Customer customer : repository.findAll()) {
				log.info(customer.toString());
			}
			log.info("");

			// fetch an individual customer by ID
			Optional<Customer> customer = repository.findById(1L);
			log.info("Customer found with findById(1L):");
			log.info("--------------------------------");
			log.info(customer.isPresent() ? customer.get().toString() : "");
			log.info("");

			// fetch customers by last name
			log.info("Customer found with findByLastName('Bauer'):");
			log.info("--------------------------------------------");
			repository.findByLastName("Bauer").forEach(bauer -> log.info(bauer.toString()));
			// for (Customer bauer : repository.findByLastName("Bauer")) {
			//  log.info(bauer.toString());
			// }
			log.info("");
		};
	}

}

@RestController
@RequestMapping("/customers")
class CustomerController {

	private final CustomerRepository repository;

	public CustomerController(CustomerRepository repository){
		this.repository = repository;
	}

	@GetMapping("/")
	public List<Customer> findByLastName(@RequestParam(name = "last-name") String lastName){

		return repository.findByLastName(lastName);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> findById(@PathVariable("id") long id){

		return repository.findById(id)
				.map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping
    public List<Customer> findAll(){

        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<Customer> findByLastNameStartsWith(@RequestParam(name = "last-name") String lastName){

        return repository.findByLastNameStartsWithIgnoreCase(lastName);
    }

	@PostMapping
	public ResponseEntity<Customer> newCustomer(@RequestBody Customer newCustomer) {

		return new ResponseEntity<>(repository.save(newCustomer), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Customer> replaceCustomer(@RequestBody Customer newCustomer, @PathVariable Long id) {

		return repository.findById(id)
				.map(customer -> {
					customer.setFirstName(newCustomer.getFirstName());
					customer.setLastName(newCustomer.getLastName());
					return new ResponseEntity<>(repository.save(customer), HttpStatus.OK);
				})
				.orElseGet(() -> {
					newCustomer.setId(id);
					return new ResponseEntity<>(repository.save(newCustomer), HttpStatus.CREATED);
				});
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		repository.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

interface CustomerRepository extends JpaRepository<Customer, Long> {

	List<Customer> findByLastName(String lastName);

    List<Customer> findByLastNameStartsWithIgnoreCase(String lastName);
}

@Entity
@Data
class Customer {

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;

	private String firstName;

	private String lastName;

	protected Customer() {}

	public Customer(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    public void setName(String name) {
        String[] parts = name.split(" "); // what about "Joe W. Smith" ?
        this.firstName = parts[0];
        this.lastName = (parts.length > 1) ? parts[1] : "";
    }

	@Override
	public String toString() {
		return String.format(
				"Customer[id=%d, firstName='%s', lastName='%s']",
				id, firstName, lastName);
	}
}