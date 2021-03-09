package com.example.demo01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
class Demo01ApplicationIT {

	private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
	private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

	private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
	private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

	@Autowired
	private MockMvc restCustomerMockMvc;


	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private EntityManager em;

	private Customer customer;

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Customer createEntity(EntityManager em) {
		Customer customer = new Customer(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);
		return customer;
	}

	/**
	 * Create an updated entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Customer createUpdatedEntity(EntityManager em) {
		Customer customer = new Customer(UPDATED_FIRST_NAME, UPDATED_LAST_NAME);
		return customer;
	}

	@BeforeEach
	public void initTest() {
		customer = createEntity(em);
	}

	@Test
	@Transactional
	void createCustomer() throws Exception {
		int databaseSizeBeforeCreate = customerRepository.findAll().size();
		// Create the Customer
		restCustomerMockMvc
				.perform(post("/customers").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customer)))
				.andExpect(status().isCreated());

		// Validate the Customer in the database
		List<Customer> customerList = customerRepository.findAll();
		assertThat(customerList).hasSize(databaseSizeBeforeCreate + 1);
		Customer testCustomer = customerList.get(customerList.size() - 1);
		assertThat(testCustomer.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
		assertThat(testCustomer.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
	}

	@Test
	@Transactional
	void getAllCustomers() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		// Get all the customerList
		restCustomerMockMvc
				.perform(get("/customers?sort=id,desc"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
				.andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
				.andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)));
	}


	@Test
	@Transactional
	void getCustomersWithLastName() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		// Get all the customerList
		restCustomerMockMvc
				.perform(get("/customers/?last-name={lastName}", DEFAULT_FIRST_NAME))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
				.andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
				.andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)));
	}

	@Test
	@Transactional
	void getCustomersWithLastNameStart() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		// Get all the customerList
		restCustomerMockMvc
				.perform(get("/customers/search?last-name={lastName}", "A"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
				.andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
				.andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)));
	}

	@Test
	@Transactional
	void getCustomer() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		// Get the customer
		restCustomerMockMvc
				.perform(get("/customers/{id}", customer.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.id").value(customer.getId().intValue()))
				.andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
				.andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME));
	}

	@Test
	@Transactional
	void getNonExistingCustomer() throws Exception {
		// Get the customer
		restCustomerMockMvc.perform(get("/customers/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void updateCustomer() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		int databaseSizeBeforeUpdate = customerRepository.findAll().size();

		// Update the customer
		Customer updatedCustomer = customerRepository.findById(customer.getId()).get();
		// Disconnect from session so that the updates on updatedCustomer are not directly saved in db
		em.detach(updatedCustomer);
		updatedCustomer.setFirstName(UPDATED_FIRST_NAME);
		updatedCustomer.setLastName(UPDATED_LAST_NAME);

		restCustomerMockMvc
				.perform(
						put("/customers/{id}", customer.getId()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(updatedCustomer))
				)
				.andExpect(status().isOk());

		// Validate the Customer in the database
		List<Customer> customerList = customerRepository.findAll();
		assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
		Customer testCustomer = customerList.get(customerList.size() - 1);
		assertThat(testCustomer.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
		assertThat(testCustomer.getLastName()).isEqualTo(UPDATED_LAST_NAME);
	}

	@Test
	@Transactional
	void updateNonExistingCustomer() throws Exception {
		int databaseSizeBeforeUpdate = customerRepository.findAll().size();

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restCustomerMockMvc
				.perform(put("/customers/10").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customer)))
				.andExpect(status().isCreated());

		// Validate the Customer in the database
		List<Customer> customerList = customerRepository.findAll();
		assertThat(customerList).hasSize(databaseSizeBeforeUpdate + 1);
	}

	@Test
	@Transactional
	void deleteCustomer() throws Exception {
		// Initialize the database
		customerRepository.saveAndFlush(customer);

		int databaseSizeBeforeDelete = customerRepository.findAll().size();

		// Delete the customer
		restCustomerMockMvc
				.perform(delete("/customers/{id}", customer.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		// Validate the database contains one less item
		List<Customer> customerList = customerRepository.findAll();
		assertThat(customerList).hasSize(databaseSizeBeforeDelete - 1);
	}
}
