package io.educative.api.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.ToString;

@SpringBootApplication
public class RESTApplication extends SpringBootServletInitializer {

	@Autowired
	private StudentRepository repository;

	public static void startServer(String... args) {
		SpringApplication.run(RESTApplication.class, args);
	}

	public static void stopServer() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(RESTApplication.class)
				.web(WebApplicationType.NONE).run();
		SpringApplication.exit(ctx, new ExitCodeGenerator() {
			@Override
			public int getExitCode() {
				return 0;
			}
		});
	}

	@PostConstruct
	void init() {
		repository.save(new Student("John", "Doe", "male"));
		repository.save(new Student("Kelly", "Flower", "female"));
		repository.save(new Student("Json", "Ray", "male"));
	}
}

@RestController
@RequestMapping("/students")
class StudentController {

	private final StudentRepository repository;

	@Autowired
	public StudentController(StudentRepository repository) {
		this.repository = repository;
	}

	@SuppressWarnings("serial")
	@ResponseStatus(HttpStatus.NOT_FOUND)
	class StudentNotFoundException extends RuntimeException {

		public StudentNotFoundException() {
			super("Student does not exist");
		}
	}

	@GetMapping
	Collection<Student> readStudents(@RequestParam(name = "first_name", defaultValue = "") String firstName,
			@RequestParam(name = "last_name", defaultValue = "") String lastName,
			@RequestParam(name = "gender", defaultValue = "") String gender) {

		return this.repository.findAll().stream()
				.filter(e -> firstName.isEmpty() || firstName.trim().equalsIgnoreCase(e.getFirstName()))
				.filter(e -> lastName.isEmpty() || lastName.trim().equalsIgnoreCase(e.getLastName()))
				.filter(e -> gender.isEmpty() || gender.trim().equalsIgnoreCase(e.getGender()))
				.collect(Collectors.toList());

	}

	@GetMapping("/{id}")
	Student readStudent(@PathVariable Long id) {
		return this.repository.findById(id)
				.orElseThrow(StudentNotFoundException::new);
	}

	@PostMapping
	ResponseEntity<?> addStudent(@RequestBody Student student) {
		if (student.getFirstName() == null) {
			return ResponseEntity.badRequest().body("'first_name' is missing in request");
		}
		if (student.getLastName() == null) {
			return ResponseEntity.badRequest().body("'last_name' is missing in request");
		}
		if (student.getGender() == null) {
			return ResponseEntity.badRequest().body("'gender' is missing in request");
		}
		student.setId(null);
		Student result = this.repository.save(student);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(result.getId())
				.toUri();

		return ResponseEntity.created(location).body(result);
	}

	@PutMapping("/{id}")
	ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
		Student search = this.readStudent(id);
		if (search == null) {
			return ResponseEntity.badRequest().body(String.format("student with id '%s' not found", id));
		}
		return ResponseEntity.ok(this.repository.update(student)
				.orElseThrow(StudentNotFoundException::new));
	}

	@DeleteMapping("/{id}")
	ResponseEntity<?> deleteStudent(@PathVariable Long id) {
		if (StudentRepository.DEFAULT_STUDENTS.contains(id)) {
			return ResponseEntity.badRequest().body("cannot delete default student with id " + id);
		}
		this.repository.delete(id)
				.orElseThrow(StudentNotFoundException::new);
		return ResponseEntity.noContent().build();
	}

	/**
	 * JSON format:
	 *
	 * <pre>
	 * [
	 *   {
	 *       "first_name": "Sam",
	 *       "last_name": "Bailey",
	 *       "gender": "Female"
	 *   },
	 *   {
	 *       "first_name": "Sam",
	 *       "last_name": "Hudson",
	 *       "gender": "Male"
	 *   }
	 * ]
	 * </pre>
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/upload")
	ResponseEntity<?> addStudentByFormUpload(@RequestParam("file") MultipartFile file) throws IOException {
		try (InputStream stream = file.getInputStream()) {
			ObjectMapper mapper = new ObjectMapper();
			List<Student> students = mapper.readValue(stream, new TypeReference<List<Student>>() {
			});
			if (students.stream()
					.filter(e -> e.getFirstName() == null || e.getLastName() == null || e.getGender() == null)
					.findFirst().isPresent()) {
				return ResponseEntity.badRequest().body("uploaded data contains invalid entries - " + students);
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(repository.saveAll(students));
		}
	}
}

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class Student {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("gender")
	private String gender;

	public Student() {
	}

	public Student(String firstName, String lastName, String gender) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj ? true
				: (this.firstName.equalsIgnoreCase(((Student) obj).getFirstName())
						&& this.lastName.equalsIgnoreCase(((Student) obj).getLastName())
						&& this.gender.equalsIgnoreCase(((Student) obj).getGender()));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getGender() {
		return gender;
	}

}

@Repository
class StudentRepository {

	public static final List<Long> DEFAULT_STUDENTS = Arrays.asList(100L, 101L, 102L);

	Map<Long, Student> students = new HashMap<>();
	AtomicLong currentId = new AtomicLong(100);

	// Return all students
	public Collection<Student> findAll() {
		return students.values();
	}

	// Find the student with this id [ GET ]
	public Optional<Student> findById(Long id) {
		Student student = null;

		if (students.containsKey(id))
			student = students.get(id);
		return Optional.ofNullable(student);
	}

	// Find the student with this id [ GET ]
	public Optional<Student> findByFirstName(String name) {

		return students.entrySet().stream()
				.filter(e -> name.equalsIgnoreCase(e.getValue().getFirstName()))
				.map(Entry::getValue)
				.findFirst();
	}

	// Find the student with this id [ GET ]
	public Collection<Student> findByGender(String gender) {

		return students.entrySet().stream()
				.filter(e -> gender.equalsIgnoreCase(e.getValue().getGender()))
				.map(Entry::getValue)
				.collect(Collectors.toList());
	}

	public Collection<Student> saveAll(Collection<Student> students) {
		if (students != null) {
			return students.stream().map(this::save).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	// Save a new student [ ADD ]
	public Student save(Student student) {
		Optional<Student> match = students
				.entrySet().stream().filter(e -> e.getValue().getFirstName().equalsIgnoreCase(student.getFirstName())
						&& e.getValue().getLastName().equalsIgnoreCase(
								student.getLastName())
						&& e.getValue().getGender().equalsIgnoreCase(student.getGender()))
				.map(Entry::getValue)
				.findFirst();
		if (match.isPresent()) {
			return match.get();
		} else {
			student.setId(currentId.getAndIncrement());
			students.put(student.getId(), student);
			return student;
		}
	}

	// Update the student with this id [ PUT ]
	public Optional<Student> update(Student student) {
		Student currentStudent = students.get(student.getId());

		if (currentStudent != null) {
			students.put(student.getId(), student);
			currentStudent = students.get(student.getId());
		}
		return Optional.ofNullable(currentStudent);
	}

	// Delete student with this id [ DELETE ]
	public Optional<Student> delete(Long id) {
		if (DEFAULT_STUDENTS.contains(id)) {
			return Optional.empty();
		}
		Student currentStudent = students.get(id);

		if (currentStudent != null) {
			students.remove(id);
		}
		return Optional.ofNullable(currentStudent);
	}
}

@RestController
@RequestMapping("/auth/students")
class AuthenticatedStudentController {

	private final StudentRepository repository;

	@Autowired
	public AuthenticatedStudentController(StudentRepository repository) {
		this.repository = repository;
	}

	@SuppressWarnings("serial")
	@ResponseStatus(HttpStatus.NOT_FOUND)
	class StudentNotFoundException extends RuntimeException {

		public StudentNotFoundException() {
			super("Student does not exist");
		}
	}

	@GetMapping
	Collection<Student> readStudents(@RequestParam(name = "first_name", defaultValue = "") String firstName,
			@RequestParam(name = "last_name", defaultValue = "") String lastName,
			@RequestParam(name = "gender", defaultValue = "") String gender) {

		return this.repository.findAll().stream()
				.filter(e -> firstName.isEmpty() || firstName.trim().equalsIgnoreCase(e.getFirstName()))
				.filter(e -> lastName.isEmpty() || lastName.trim().equalsIgnoreCase(e.getLastName()))
				.filter(e -> gender.isEmpty() || gender.trim().equalsIgnoreCase(e.getGender()))
				.collect(Collectors.toList());

	}

	@GetMapping("/{id}")
	Student readStudent(@PathVariable Long id) {
		return this.repository.findById(id)
				.orElseThrow(StudentNotFoundException::new);
	}

	@PostMapping
	ResponseEntity<?> addStudent(@RequestBody Student student) {
		if (student.getFirstName() == null) {
			return ResponseEntity.badRequest().body("'first_name' is missing in request");
		}
		if (student.getLastName() == null) {
			return ResponseEntity.badRequest().body("'last_name' is missing in request");
		}
		if (student.getGender() == null) {
			return ResponseEntity.badRequest().body("'gender' is missing in request");
		}
		student.setId(null);
		Student result = this.repository.save(student);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(result.getId())
				.toUri();

		return ResponseEntity.created(location).body(result);
	}

	@PutMapping("/{id}")
	ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
		Student search = this.readStudent(id);
		if (search == null) {
			return ResponseEntity.badRequest().body(String.format("student with id '%s' not found", id));
		}
		return ResponseEntity.ok(this.repository.update(student)
				.orElseThrow(StudentNotFoundException::new));
	}

	@DeleteMapping("/{id}")
	ResponseEntity<?> deleteStudent(@PathVariable Long id) {
		if (StudentRepository.DEFAULT_STUDENTS.contains(id)) {
			return ResponseEntity.badRequest().body("cannot delete default student with id " + id);
		}
		this.repository.delete(id)
				.orElseThrow(StudentNotFoundException::new);
		return ResponseEntity.noContent().build();
	}

	/**
	 * JSON format:
	 *
	 * <pre>
	 * [
	 *   {
	 *       "first_name": "Sam",
	 *       "last_name": "Bailey",
	 *       "gender": "Female"
	 *   },
	 *   {
	 *       "first_name": "Sam",
	 *       "last_name": "Hudson",
	 *       "gender": "Male"
	 *   }
	 * ]
	 * </pre>
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/upload")
	ResponseEntity<?> addStudentByFormUpload(@RequestParam("file") MultipartFile file) throws IOException {
		try (InputStream stream = file.getInputStream()) {
			ObjectMapper mapper = new ObjectMapper();
			List<Student> students = mapper.readValue(stream, new TypeReference<List<Student>>() {
			});
			if (students.stream()
					.filter(e -> e.getFirstName() == null || e.getLastName() == null || e.getGender() == null)
					.findFirst().isPresent()) {
				return ResponseEntity.badRequest().body("uploaded data contains invalid entries - " + students);
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(repository.saveAll(students));
		}
	}
}

@Configuration
@EnableWebSecurity
class BasicAuthWebSecurity extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf(csrfCustomizer())
				.authorizeRequests().regexMatchers(".*/auth/.*").authenticated()
				.and()
				.httpBasic();
	}

	private Customizer<CsrfConfigurer<HttpSecurity>> csrfCustomizer() {
		return http -> http.requireCsrfProtectionMatcher(request -> request.getRequestURI().contains("csrf"));
	}

}
