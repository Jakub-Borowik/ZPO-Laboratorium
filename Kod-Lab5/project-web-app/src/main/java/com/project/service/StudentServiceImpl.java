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
import com.project.model.Student;

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

    @SuppressWarnings("null")
    @Override
    public void deleteStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
        logger.info("REQUEST -> DELETE {}", resourcePath);
        restClient
                .delete()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .toBodilessEntity();
    }

    @Override
    public Page<Student> getStudenci(Pageable pageable) {
        URI uri = ServiceUtil.getURI(getResourcePath(), pageable);
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public Optional<Student> getStudentByNrIndeksu(String nrIndeksu) {
        URI uri = ServiceUtil.getUriComponent(getResourcePath())
                .queryParam("nrIndeksu", nrIndeksu)
                .build().toUri();
        logger.info("REQUEST -> GET {}", uri);
        try {
            Student student = restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .body(Student.class);
            return Optional.ofNullable(student);
        } catch (HttpException e) {
            logger.warn("Nie znaleziono studenta o indeksie: {}", nrIndeksu);
            return Optional.empty();
        }
    }

    @Override
    public Page<Student> searchByNrIndeksuStartsWith(String nrIndeksu, Pageable pageable) {
        URI uri = ServiceUtil.getUriComponent(getResourcePath(), pageable)
                .queryParam("poczatekIndeksu", nrIndeksu)
                .build().toUri();
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public Page<Student> searchByNazwiskoStartsWith(String nazwisko, Pageable pageable) {
        URI uri = ServiceUtil.getUriComponent(getResourcePath(), pageable)
                .queryParam("nazwisko", nazwisko)
                .build().toUri();
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @SuppressWarnings("null")
    private Page<Student> getPage(URI uri) {
        return restClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<Student>>() {
                });
    }
}