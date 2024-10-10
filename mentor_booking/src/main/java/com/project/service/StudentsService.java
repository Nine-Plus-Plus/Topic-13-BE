package com.project.service;

import com.project.dto.CreateStudentRequest;
import com.project.dto.Response;
import com.project.dto.StudentsDTO;
import com.project.enums.AvailableStatus;
import com.project.exception.OurException;
import com.project.model.Class;
import com.project.model.Students;
import com.project.model.Users;
import com.project.repository.ClassRepository;
import com.project.repository.StudentsRepository;
import com.project.repository.UsersRepository;
import com.project.ultis.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentsService {

    @Autowired
    private StudentsRepository studentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ClassRepository classRepository;


    // Phương thức lấy tất cả sinh viên
    public Response getAllStudents() {
        Response response = new Response();
        try {
            List<Students> list = studentsRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
            if (!list.isEmpty()) {
                List<StudentsDTO> listDTO = list.stream()
                        .map(Converter::convertStudentToStudentDTO)
                        .collect(Collectors.toList());

                response.setStudentsDTOList(listDTO);
                response.setStatusCode(200);
                response.setMessage("Students fetched successfully");
            } else {
                response.setStudentsDTOList(null);
                response.setStatusCode(400);
                response.setMessage("No data found");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching students: " + e.getMessage());
        }
        return response;
    }

    // Phương thức lấy sinh viên theo ID
    public Response getStudentById(Long id) {
        Response response = new Response();
        try {
            Students student = studentsRepository.findByIdAndAvailableStatus(id, AvailableStatus.ACTIVE);
            if (student != null) {
                StudentsDTO studentsDTO = Converter.convertStudentToStudentDTO(student);
                response.setStudentsDTO(studentsDTO);
                response.setStatusCode(200);
                response.setMessage("Student fetched successfully");
            } else {
                response.setStudentsDTO(null);
                response.setStatusCode(400);
                response.setMessage("No data found");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching student: " + e.getMessage());
        }
        return response;
    }

    public Response findStudentByNameAndExpertise(String name, String expertise) {
        Response response = new Response();
        try {
            List<Students> studentsList;

            if (name != null && !name.isEmpty() && expertise != null && !expertise.isEmpty()) {
                studentsList = studentsRepository.findStudentByUserFullNameAndExpertise(name, expertise, AvailableStatus.ACTIVE);
            } else if (name != null && !name.isEmpty()) {
                studentsList = studentsRepository.findStudentByUserFullName(name, AvailableStatus.ACTIVE);
            } else if (expertise != null && !expertise.isEmpty()) {
                studentsList = studentsRepository.findByExpertise(expertise, AvailableStatus.ACTIVE);
            } else {
                response.setUsersDTOList(null);
                response.setStatusCode(400);
                response.setMessage("Both name and expertise cannot be empty");
                return response;
            }
            if (!studentsList.isEmpty()) {
                List<StudentsDTO> listDTO = studentsList.stream()
                        .map(Converter::convertStudentToStudentDTO)
                        .collect(Collectors.toList());

                response.setStudentsDTOList(listDTO);
                response.setStatusCode(200);
                response.setMessage("Students fetched successfully");
            } else {
                response.setUsersDTOList(null);
                response.setStatusCode(400);
                response.setMessage("No data found");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching student: " + e.getMessage());
        }
        return response;
    }

    public Response updateStudent(Long userId, CreateStudentRequest updateRequest) {
        Response response = new Response();
        try {

            // Tìm kiếm user với userId và trạng thái ACTIVE
            Users updateUser = usersRepository.findByIdAndAvailableStatus(userId, AvailableStatus.ACTIVE);
            if (updateUser == null) {
                response.setStatusCode(400);
                response.setMessage("User not found");
                return response; // Trả về phản hồi nếu không tìm thấy user
            }

            // Kiểm tra Class
            Class aClass = classRepository.findById(updateRequest.getAClass().getId())
                    .orElseThrow(() -> new OurException("Class not found"));
            // Cập nhật thông tin Users
            updateUser.setUsername(updateRequest.getUsername());
            updateUser.setEmail(updateRequest.getEmail());
            updateUser.setFullName(updateRequest.getFullName());
            updateUser.setBirthDate(updateRequest.getBirthDate());
            updateUser.setAvatar(updateRequest.getAvatar());
            updateUser.setAddress(updateRequest.getAddress());
            updateUser.setPhone(updateRequest.getPhone());
            updateUser.setGender(updateRequest.getGender());
            updateUser.setDateUpdated(LocalDateTime.now());
            updateUser.setAvailableStatus(AvailableStatus.ACTIVE);
            usersRepository.save(updateUser);
                // Tạo đối tượng Student mới
            // Tìm kiếm và cập nhật Students
            Students updateStudent = studentsRepository.findByUser_Id(updateUser.getId());
            if (updateStudent == null) {
                response.setStatusCode(400);
                response.setMessage("Student not found");
                return response; // Trả về phản hồi nếu không tìm thấy student
            }
            updateStudent.setUser(updateUser);
            updateStudent.setExpertise(updateRequest.getExpertise());
            updateStudent.setStudentCode(updateRequest.getStudentCode());
            updateStudent.setDateUpdated(LocalDate.now());
            updateStudent.setAClass(aClass);
            updateStudent.setAvailableStatus(AvailableStatus.ACTIVE);
            updateStudent.setGroup(null); // Đặt group là null nếu cần thiết
            studentsRepository.save(updateStudent);

            // Chuyển đổi đối tượng student sang DTO
            StudentsDTO studentsDTO = Converter.convertStudentToStudentDTO(updateStudent);
            response.setStudentsDTO(studentsDTO);
            response.setStatusCode(200);
            response.setMessage("Student updated successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching student: " + e.getMessage());
        }
        return response;
    }
}
