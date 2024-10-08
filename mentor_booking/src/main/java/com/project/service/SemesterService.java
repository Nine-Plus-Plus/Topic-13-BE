package com.project.service;

import com.project.dto.ClassDTO;
import com.project.model.Class;
import com.project.dto.Response;
import com.project.dto.SemesterDTO;
import com.project.exception.OurException;
import com.project.model.Semester;
import com.project.repository.ClassRepository;
import com.project.repository.SemesterRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.project.ultis.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private ClassRepository classRepository;

    public Response createSemester(SemesterDTO createRequest) {
        Response response = new Response();
        try {
            if (semesterRepository.findBySemesterName(createRequest.getSemesterName()).isPresent()) {
                throw new OurException("Semester has already existed");
            }
            Semester semester = new Semester();
            semester.setDateCreated(LocalDateTime.now());
            semester.setSemesterName(createRequest.getSemesterName());
            semesterRepository.save(semester);
            if (semester.getId() > 0) {
                SemesterDTO dto = Converter.convertSemesterToSemesterDTO(semester);
                dto.setClasses(createRequest.getClasses());
                response.setSemesterDTO(dto);
                response.setStatusCode(201);
                response.setMessage("Semester added successfully");
            }

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during semester creation: " + e.getMessage());
        }

        return response;
    }

    // phương thức tìm tất cả Semester
    public Response getAllSemesters(){
        Response response = new Response();
        try {
            List<Semester> semesterList = semesterRepository.findAll();
            if (!semesterList.isEmpty()) {
                List<SemesterDTO> semesterListDTO = semesterList
                        .stream()
                        .map(Converter::convertSemesterToSemesterDTO)
                        .collect(Collectors.toList());
                response.setSemesterDTOList(semesterListDTO);
                response.setStatusCode(200);
                response.setMessage("Semester fetched successfully");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all semesters " + e.getMessage());
        }
        return response;
    }

    // phương thức tìm Semester theo Id
    public Response getSemesterById(Long id){
        Response response = new Response();
        try {
            Semester findSemester = semesterRepository.findById(id).orElse(null);
            if (findSemester != null) {
                SemesterDTO dto = Converter.convertSemesterToSemesterDTO(findSemester);
                response.setSemesterDTO(dto);
                response.setStatusCode(200);
                response.setMessage("Successfully");
            }else throw new OurException("Cannot find semester");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get semester " + e.getMessage());
        }
        return response;
    }

    // phương thức cập nhập mới Semester
    public Response updateSemester(Long id, Semester newSemester){
        Response response = new Response();
        try {
            Semester presentSemester = semesterRepository.findById(id)
                    .orElseThrow(() -> new OurException("Cannot find semester with id: "+id));
            if (semesterRepository.findBySemesterName(newSemester.getSemesterName()).isPresent()) {
                throw new OurException("Semester has already existed");
            }
            presentSemester.setSemesterName(newSemester.getSemesterName());
            semesterRepository.save(presentSemester);
            SemesterDTO dto = Converter.convertSemesterToSemesterDTO(presentSemester);
            response.setSemesterDTO(dto);
            response.setStatusCode(200);
            response.setMessage("Semester updated successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while updating semester: " + e.getMessage());
        }
        return response;
    }

    // phương thức xóa Semester
    public Response deleteSemester(Long id) {
        Response response = new Response();
        try {
            Semester deleteSemester = semesterRepository.findById(id)
                    .orElseThrow(() -> new OurException("Cannot find semester with id: " + id));
            semesterRepository.delete(deleteSemester);
            response.setStatusCode(200);
            response.setMessage("Semester deleted successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while deleting semester: " + id);
        }
        return response;
    }
}
