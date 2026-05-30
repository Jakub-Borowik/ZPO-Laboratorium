package com.project.service;

import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.project.exception.HttpException;
import com.project.model.Projekt;
import com.project.model.Student;
import com.project.repository.StudentRepository;

@Service
public class StudentServiceImpl implements StudentService {
    private static final Logger logger = LoggerFactory.getLogger(ZadanieServiceImpl.class);
    private final RestClient restClient;

    // @Autowired
    public StudentServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getResourcePath() {
        return "/api/studenci";
    }

    private String getResourcePath(Integer id) {
        return String.format("%s/%d", getResourcePath(), id);
    }

    @Override
    public Optional<Student> getStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
        logger.info("REQUEST -> GET {}", resourcePath);
        @SuppressWarnings("null")
        Student student = restClient
                .get()
                .uri(resourcePath) // można też używać .uri("/api/projekty/{projektId}", projektId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(Student.class);
        return Optional.ofNullable(student);
    }

    @SuppressWarnings("null")
    @Override
    public Student setStudent(Student student) {
        if (student.getStudentId() != null) { // modyfikacja istniejącego projektu
            String resourcePath = getResourcePath(student.getStudentId());
            logger.info("REQUEST -> PUT {}", resourcePath);
            restClient
                    .put()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            return student;
        } else { // utworzenie nowego projektu
            // po dodaniu projektu zwracany jest w nagłówku Location - link do utworzonego
            // zasobu
            String resourcePath = getResourcePath();
            logger.info("REQUEST -> POST {}", resourcePath);
            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            URI location = response.getHeaders().getLocation();
            logger.info("REQUEST (location) -> GET {}", location);
            return restClient
                    .get()
                    .uri(location)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .body(Student.class);
        }
    }

    @Override
    public void deleteStudent(Integer studentId) {
        studentRepository.deleteById(studentId);
    }

    @Override
    public Page<Student> getStudenci(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Optional<Student> getStudentByNrIndeksu(String nrIndeksu) {
        return studentRepository.findByNrIndeksu(nrIndeksu);
    }

    @Override
    public Page<Student> searchByNrIndeksuStartsWith(String nrIndeksu, Pageable pageable) {
        return studentRepository.findByNrIndeksuStartsWith(nrIndeksu, pageable);
    }

    @Override
    public Page<Student> searchByNazwiskoStartsWith(String nazwisko, Pageable pageable) {
        return studentRepository.findByNazwiskoStartsWithIgnoreCase(nazwisko, pageable);
    }
}