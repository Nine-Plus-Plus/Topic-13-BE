package com.project.controller;

import com.project.dto.*;
import com.project.model.Users;
import com.project.repository.SkillsRepository;
import com.project.service.MentorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MentorsController {

    @Autowired
    private MentorsService mentorsService;

    @Autowired
    private SkillsRepository skillsRepository;

    // Lấy tất cả mentors
    @GetMapping("/admin/get-all-mentors")
    public ResponseEntity<Response> getAllMentors() {
        Response response = mentorsService.getAllMentors();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Lấy mentor theo ID
    @GetMapping("/admin/get-mentor-by-id/{id}")
    public ResponseEntity<Response> getMentorById(@PathVariable Long id) {
        Response response = mentorsService.getMentorById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping(value = "/admin/update-mentor/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Response> updateMentor(
            @PathVariable Long id,
            @RequestPart("mentor") CreateMentorRequest updateMentor,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        Response response = mentorsService.updateMentor(id, updateMentor, avatarFile);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/get-mentor-by-name-skills/")
    public ResponseEntity<Response> getMentorByNameAndSkillsAndAvaibility(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime availableFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime availableTo) {
        Response response = mentorsService.findMentorWithNameAndSkillsAndAvaibility(name.trim(), skillIds, availableFrom, availableTo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
