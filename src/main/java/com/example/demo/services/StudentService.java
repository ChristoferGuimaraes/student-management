package com.example.demo.services;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.StudentDTO;
import com.example.demo.entities.StudentEntity;
import com.example.demo.repositories.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Objects;
import java.util.Optional;


@Transactional
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    public StudentService(StudentRepository studentRepository, ModelMapper modelMapper) {
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
    }

    // Do the conversion of an Entity to a DTO
    private StudentDTO toStudentDTO(StudentEntity studentEntity) {
        return modelMapper.map(studentEntity, StudentDTO.class);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllStudents(PageRequest pageRequest) {
        Page<Object> page =  studentRepository.findAll(pageRequest).map(this::toStudentDTO);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getStudentById(Long studentId) {
        boolean exists = studentRepository.existsById(studentId);
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student with id " + studentId + " does not exists!");
        }
        Optional<StudentDTO> studentDTO = studentRepository.findById(studentId).map(this::toStudentDTO);
        return ResponseEntity.status(HttpStatus.OK).body(studentDTO);
    }


    public ResponseEntity<Object> addNewStudent(StudentDTO student) {
        Boolean studentByEmailExists = studentRepository.existsStudentByEmail(student.getEmail());

        if (studentByEmailExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This e-mail is already taken!");
        }

        StudentEntity studentEntity = new StudentEntity(student);

        studentRepository.save(studentEntity);

        StudentEntity findEntity = studentRepository.findById(studentEntity.getId()).orElseThrow();

        return ResponseEntity.status(HttpStatus.CREATED).body(toStudentDTO(findEntity));
    }


    public ResponseEntity<Object> deleteStudent(Long studentId) {
        boolean exists = studentRepository.existsById(studentId);
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student with id " + studentId + " does not exists!");
        }
        studentRepository.deleteById(studentId);
        return ResponseEntity.status(HttpStatus.OK).body("Student with id " + studentId + " was excluded!");
    }


    public ResponseEntity<Object> updateStudent(Long studentId, String firstName, String lastName, String email) {
        Optional<StudentEntity> studentEntity = studentRepository.findById(studentId);

        if (studentEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student with id " + studentId + " does not exists!");
        }

        if (firstName != null && firstName.length() > 0 && !Objects.equals(studentEntity.get().getFirstName(), firstName)) {
            studentEntity.get().setFirstName(firstName);
        }

        if (lastName != null && lastName.length() > 0 && !Objects.equals(studentEntity.get().getLastName(), lastName)) {
            studentEntity.get().setLastName(lastName);
        }

        if (email != null) {
            if (email.length() > 10) {
                if (Objects.equals(studentEntity.get().getEmail(), email)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This e-mail is already taken!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid e-mail! Min. 10 characters!");
            }
            studentEntity.get().setEmail(email);
        }
        return ResponseEntity.status(HttpStatus.OK).body(toStudentDTO(studentEntity.get()));
    }

}
