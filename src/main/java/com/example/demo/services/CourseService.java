package com.example.demo.services;

import com.example.demo.dto.CourseDTO;
import com.example.demo.entities.CourseEntity;
import com.example.demo.repositories.CourseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    public CourseService(CourseRepository courseRepository, ModelMapper modelMapper) {
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
    }

    private CourseDTO toCourseDTO(CourseEntity courseEntity) {
        return modelMapper.map(courseEntity, CourseDTO.class);
    }

    public ResponseEntity<Object> getAllCourses(PageRequest pageRequest) {
        Page<Object> page = courseRepository.findAll(pageRequest).map(this::toCourseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    public ResponseEntity<Object> findCourseByName(String name) {
        Optional<CourseEntity> nameCourse = courseRepository.findByName(name);

        if (nameCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Course " + name + " does not exists in database!");
        }

        return ResponseEntity.status(HttpStatus.OK).body(nameCourse.stream().map(this::toCourseDTO));
    }

    @Transactional
    public ResponseEntity<Object> addNewCourse(CourseDTO courseDTO) {
        Optional<CourseEntity> nameCourse = courseRepository.findByName(courseDTO.getName());

        if (nameCourse.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This name is already registered!");
        }

        if (courseDTO.getName() == null || courseDTO.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more fields are blank or null!");
        }

        if (courseDTO.getTeacherName() == null || courseDTO.getTeacherName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more fields are blank or null!");
        }

        if (courseDTO.getClassNumber() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more fields are blank or null!");
        }

        if (courseDTO.getStartDate() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more fields are blank or null!");
        }

        CourseEntity entity = new CourseEntity(courseDTO);
        courseRepository.save(entity);

        return ResponseEntity.status(HttpStatus.OK).body(courseDTO);
    }

    @Transactional
    public ResponseEntity<Object> deleteCourseByName(String courseName) {
        Optional<CourseEntity> name = courseRepository.findByName((courseName));

        if (name.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Course '" + courseName + "' does not exists in database!");
        }

        courseRepository.deleteById(name.get().getId());

        return ResponseEntity.status(HttpStatus.OK).body("Course '" + courseName + "' deleted successfully");
    }


    @Transactional
    public ResponseEntity<Object> updateCourse(String name, String courseName, String teacherName, Integer classNumber) {
        Optional<CourseEntity> courseEntity = courseRepository.findByName(name);
        Optional<CourseEntity> courseOptional = courseRepository.findByName(courseName);


        if (courseEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course '" + name + "' does not exists in database!");
        }

        if (courseOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Course '" + courseName + "' already exists in database!");
        }

        if (courseName != null && courseName.length() > 0) {

            courseEntity.get().setName(courseName);
        }

        if (teacherName != null && teacherName.length() > 0) {
            courseEntity.get().setTeacherName(teacherName);
        }

        if (classNumber != null && classNumber > 0) {
            courseEntity.get().setClassNumber(classNumber);
        }

        return ResponseEntity.status(HttpStatus.OK).body(toCourseDTO(courseEntity.get()));

    }
}