package com.project.service;

import com.project.dto.CreateStudentRequest;
import com.project.dto.Response;
import com.project.dto.StudentsDTO;
import com.project.enums.AvailableStatus;
import com.project.exception.OurException;
import com.project.model.Class;
import com.project.model.Role;
import com.project.model.Students;
import com.project.model.Users;
import com.project.repository.ClassRepository;
import com.project.repository.RoleRepository;
import com.project.repository.StudentsRepository;
import com.project.repository.UsersRepository;
import com.project.security.AwsS3Service;
import com.project.ultis.Converter;
import com.project.ultis.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private UsersService usersService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;
    // Phương thức lấy tất cả sinh viên
    public Response getAllStudents() {
        Response response = new Response();
        try {
            List<Students> list = studentsRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
            List<StudentsDTO> listDTO = new ArrayList<>();
            if (!list.isEmpty()) {
                listDTO = list.stream()
                        .map(Converter::convertStudentToStudentDTO)
                        .collect(Collectors.toList());

                response.setStudentsDTOList(listDTO);
                response.setStatusCode(200);
                response.setMessage("Students fetched successfully");
            } else {
                response.setStudentsDTOList(listDTO);
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
            StudentsDTO studentsDTO = new StudentsDTO();
            if (student != null) {
                studentsDTO = Converter.convertStudentToStudentDTO(student);
                response.setStudentsDTO(studentsDTO);
                response.setStatusCode(200);
                response.setMessage("Student fetched successfully");
            } else {
                response.setStudentsDTO(studentsDTO);
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

    public Response findStudentByNameAndExpertise(Long classId, String name, String expertise) {
        Response response = new Response();
        try {
            List<Students> studentsList;
            List<StudentsDTO> listDTO = new ArrayList<>();
            // Kiểm tra classId và truy vấn theo name và expertise trong class
            if (classId == null) {
                response.setStudentsDTOList(null);
                response.setStatusCode(400);
                response.setMessage("Class ID cannot be null");
                return response;
            }

            // Nếu chỉ có classId (name và expertise đều là chuỗi rỗng)
            if ((name == null || name.isEmpty()) && (expertise == null || expertise.isEmpty())) {
                studentsList = studentsRepository.findStudentByClassId(classId, AvailableStatus.ACTIVE);
            }
            // Nếu có cả name và expertise
            else if (!isNullOrEmpty(name) && !isNullOrEmpty(expertise)) {
                studentsList = studentsRepository.findStudentByUserFullNameAndExpertiseAndClassId(name, expertise, AvailableStatus.ACTIVE, classId);
            }
            // Nếu chỉ có name
            else if (!isNullOrEmpty(name)) {
                studentsList = studentsRepository.findStudentByUserFullNameAndClassId(name, AvailableStatus.ACTIVE, classId);
            }
            // Nếu chỉ có expertise
            else if (!isNullOrEmpty(expertise)) {
                studentsList = studentsRepository.findByExpertiseAndClassId(expertise, AvailableStatus.ACTIVE, classId);
            } else {
                response.setStudentsDTOList(listDTO);
                response.setStatusCode(400);
                response.setMessage("Both name and expertise cannot be empty");
                return response;
            }


            if (!studentsList.isEmpty()) {
                listDTO = studentsList.stream()
                        .map(Converter::convertStudentToStudentDTO)
                        .collect(Collectors.toList());

                response.setStudentsDTOList(listDTO);
                response.setStatusCode(200);
                response.setMessage("Students fetched successfully");
            } else {
                response.setStudentsDTOList(listDTO);
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

    // Hàm kiểm tra chuỗi rỗng
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public Response updateStudent(Long userId, CreateStudentRequest updateRequest, MultipartFile avatarFile) {
        Response response = new Response();
        try {

            // Tìm kiếm user với userId và trạng thái ACTIVE
            Users updateUser = usersRepository.findByIdAndAvailableStatus(userId, AvailableStatus.ACTIVE);
            if (updateUser == null) {
                response.setStatusCode(400);
                response.setMessage("User not found");
                return response; // Trả về phản hồi nếu không tìm thấy user
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = awsS3Service.saveImageToS3(avatarFile);
                updateUser.setAvatar(avatarUrl);
                System.out.println("Avatar URL: " + avatarUrl); // Kiểm tra URL
            }

            // Kiểm tra Class
            Class aClass = classRepository.findById(updateRequest.getAClass().getId())
                    .orElseThrow(() -> new OurException("Class not found"));
            // Cập nhật thông tin Users
            updateUser.setUsername(updateRequest.getUsername());
            updateUser.setEmail(updateRequest.getEmail());
            updateUser.setFullName(updateRequest.getFullName());
            updateUser.setBirthDate(updateRequest.getBirthDate());
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

    public Response findStudentsNotInGroup(Long classId){
        Response response = new Response();
        try {
            List<Students> findStudents = studentsRepository.findStudentsThatAreNotInGroup(classId, AvailableStatus.ACTIVE);
            if (findStudents != null) {
                List<StudentsDTO> findStudentsDTO = findStudents.stream()
                        .map(Converter::convertStudentToStudentDTO)
                        .collect(Collectors.toList());
                response.setStudentsDTOList(findStudentsDTO);
                response.setStatusCode(200);
                response.setMessage("Successfully");
            } else {
                throw new OurException("Cannot find group");
            }
          } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occured during get group " + e.getMessage());
        }
        return response;
    }

    public Response importStudentsFromExcel(MultipartFile file){
        Response response = new Response();
        try{
            List<CreateStudentRequest> studentRequests = ExcelHelper.excelToStudents(file);

            List<String> errors = new ArrayList<>();
            for (CreateStudentRequest request : studentRequests) {
                try {
                    Response createResponse = createStudentFormExcel(request);
                    if (createResponse.getStatusCode() != 200) {
                        errors.add("Error creating student: " + request.getUsername() +
                                " [Username: " + request.getUsername() +
                                ", Email: " + request.getEmail() +
                                ", Password: " + request.getPassword() +
                                ", FullName: " + request.getFullName() +
                                ", BirthDate: " + request.getBirthDate() +
                                ", Address: " + request.getAddress() +
                                ", Phone: " + request.getPhone() +
                                ", Gender: " + request.getGender() +
                                ", Class Name: " + request.getClassName() +
                                ", Expertise: " + request.getExpertise() +
                                ", StudentCode: " + request.getStudentCode() + "]");
                    }
                } catch (OurException e) {
                    errors.add("Error creating student: " + request.getUsername() + " - " + e.getMessage());
                }
            }
            if (!errors.isEmpty()) {
                response.setStatusCode(400);
                response.setMessage(String.join(", ", errors));
            } else {
                response.setStatusCode(200);
                response.setMessage("All students created successfully");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during import: " + e.getMessage());
        }
        return response;
    }

    public Response createStudentFormExcel(CreateStudentRequest request){
        Response response = new Response();
        try{
            // Kiểm tra nếu username hoặc email đã tồn tại
            if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new OurException("Username already exists");
            }
            // Kiểm tra email
            if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new OurException("Email already exists");
            }
            if (usersRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new OurException("Phone already exists");
            }
            if (studentsRepository.findByStudentCode(request.getStudentCode()).isPresent()) {
                throw new OurException("StudentCode already exists");
            }
            // Kiểm tra Class
            Class aClass = classRepository.findByClassNameContainingIgnoreCaseAndAvailableStatus(request.getClassName(), AvailableStatus.ACTIVE);
            if (aClass == null) {
                throw new OurException("Class not found");
            }

            // Kiểm tra Role
            Role role = roleRepository.findByRoleName("STUDENT")
                    .orElseThrow(() -> new OurException("No role name"));

            // Mã hóa mật khẩu
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            // Tạo đối tượng User mới
            Users newUser = new Users();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(encodedPassword);
            newUser.setFullName(request.getFullName());
            newUser.setBirthDate(request.getBirthDate());
            newUser.setAddress(request.getAddress());
            newUser.setPhone(request.getPhone());
            newUser.setGender(request.getGender());
            newUser.setDateCreated(LocalDateTime.now());
            newUser.setRole(role);
            newUser.setAvailableStatus(AvailableStatus.ACTIVE);

            usersRepository.save(newUser);

            // Tạo đối tượng Student mới
            Students student = new Students();
            student.setUser(newUser);
            student.setExpertise(request.getExpertise());
            student.setStudentCode(request.getStudentCode());
            student.setDateCreated(LocalDate.now());
            student.setPoint(100);
            student.setAClass(aClass);
            student.setGroupRole(null);
            student.setAvailableStatus(AvailableStatus.ACTIVE);
            student.setGroup(null); // Để group_id null
            studentsRepository.save(student);

            newUser.setStudent(student);
            usersRepository.save(newUser);

            response.setStatusCode(200);
            response.setMessage("Student created successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during student creation: " + e.getMessage());
        }
        return response;
    }
}
