package com.project.service;

import com.project.dto.*;
import com.project.enums.AvailableStatus;
import com.project.enums.MentorScheduleStatus;
import com.project.exception.OurException;
import com.project.model.MentorSchedule;
import com.project.model.Mentors;
import com.project.model.Skills;
import com.project.model.Users;
import com.project.repository.MentorScheduleRepository;
import com.project.repository.MentorsRepository;
import com.project.repository.SkillsRepository;
import com.project.repository.UsersRepository;
import com.project.security.AwsS3Service;
import com.project.ultis.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorsService {

    @Autowired
    private MentorsRepository mentorsRepository;

    @Autowired
    private SkillsRepository skillsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MentorScheduleService mentorScheduleService;

    @Autowired
    private MentorScheduleRepository mentorScheduleRepository;

    @Autowired
    private AwsS3Service awsS3Service;

    // Phương thức lấy tất cả mentors
    public Response getAllMentors() {
        Response response = new Response();
        List<MentorsDTO> mentorsDTOList = new ArrayList<>();
        try {
            List<Mentors> mentorsList = mentorsRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
            mentorsDTOList = mentorsList
                    .stream()
                    .map(Converter::convertMentorToMentorDTO)
                    .collect(Collectors.toList());
            if (!mentorsDTOList.isEmpty()) {
                response.setMentorsDTOList(mentorsDTOList);
                response.setStatusCode(200);
                response.setMessage("Mentors fetched successfully");
            } else {
                response.setMentorsDTOList(mentorsDTOList);
                response.setMessage("No data found");
                response.setStatusCode(400);
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching mentors: " + e.getMessage());
        }
        return response;
    }

    // Phương thức lấy mentor theo ID
    public Response getMentorById(Long id) {
        Response response = new Response();
        MentorsDTO mentorsDTO = new MentorsDTO();
        try {
            Mentors mentor = mentorsRepository.findByIdAndAvailableStatus(id, AvailableStatus.ACTIVE);
            if (mentor != null) {
                mentorsDTO = Converter.convertMentorToMentorDTO(mentor);
                response.setMentorsDTO(mentorsDTO);
                response.setStatusCode(200);
                response.setMessage("Mentor fetched successfully");
            } else {
                response.setMentorsDTO(mentorsDTO);
                response.setStatusCode(400);
                response.setMessage("data not found");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching mentor: " + e.getMessage());
        }

        return response;
    }


    public Response updateMentor(Long userId, CreateMentorRequest updateRequest, MultipartFile avatarFile) {
        Response response = new Response();
        try {
            // Tìm kiếm user với userId và trạng thái ACTIVE
            Users updateUser = usersRepository.findByIdAndAvailableStatus(userId, AvailableStatus.ACTIVE);
            if (updateUser == null) {
                response.setStatusCode(400);
                response.setMessage("User not found");
                return response; // Trả về phản hồi nếu không tìm thấy user
            }

            // Tìm kiếm Mentor dựa trên userId
            Mentors mentorUpdate = mentorsRepository.findByUser_Id(updateUser.getId());
            if (mentorUpdate == null) {
                response.setStatusCode(400);
                response.setMessage("Mentor not found");
                return response; // Trả về phản hồi nếu không tìm thấy mentor
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = awsS3Service.saveImageToS3(avatarFile);
                updateUser.setAvatar(avatarUrl);
                System.out.println("Avatar URL: " + avatarUrl); // Kiểm tra URL
            }

            // Cập nhật thông tin người dùng hiện có
            updateUser.setUsername(updateRequest.getUsername());
            updateUser.setEmail(updateRequest.getEmail());
            updateUser.setFullName(updateRequest.getFullName());
            updateUser.setBirthDate(updateRequest.getBirthDate());
            updateUser.setAddress(updateRequest.getAddress());
            updateUser.setPhone(updateRequest.getPhone());
            updateUser.setGender(updateRequest.getGender());
            updateUser.setDateUpdated(LocalDateTime.now());
            usersRepository.save(updateUser);

            // Cập nhật thông tin mentor
            mentorUpdate.setMentorCode(updateRequest.getMentorCode());
            mentorUpdate.setDateUpdated(LocalDate.now());
            mentorUpdate.setStar(updateRequest.getStar());
            mentorUpdate.setTotalTimeRemain(updateRequest.getTotalTimeRemain());

            // Cập nhật danh sách kỹ năng (skills)
            List<SkillsDTO> skillsListDTO = updateRequest.getSkills();
            List<Skills> skillsList = skillsListDTO.stream()
                    .map(Converter::convertSkillDTOToSkill)
                    .collect(Collectors.toList());
            mentorUpdate.setSkills(skillsList);
            mentorsRepository.save(mentorUpdate);

            MentorsDTO mentorsDTO = Converter.convertMentorToMentorDTO(mentorUpdate);
            response.setMentorsDTO(mentorsDTO);
            response.setStatusCode(200);
            response.setMessage("Mentor updated successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while updating mentor: " + e.getMessage());
        }
        return response;
    }

    // Hàm kiểm tra chuỗi rỗng
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public Response findMentorWithNameAndSkillsAndAvaibility(String name, List<Long> skillIds,  LocalDateTime availableFrom, LocalDateTime availableTo) {
        Response response = new Response();
        try {
            List<MentorsDTO> mentorsDTOList = new ArrayList<>();

            // 1. Nếu cả name, skills, và availability đều rỗng
            if (isNullOrEmpty(name) && (skillIds == null || skillIds.isEmpty()) && availableFrom == null && availableTo == null) {
                List<Mentors> mentorsList = mentorsRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 2. Nếu chỉ có name
            else if (!isNullOrEmpty(name) && (skillIds == null || skillIds.isEmpty()) && availableFrom == null && availableTo == null) {
                List<Mentors> mentorsList = mentorsRepository.findByName(name, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 3. Nếu chỉ có skills
            else if (((skillIds != null)) && (isNullOrEmpty(name)) && availableFrom == null && availableTo == null) {
                // Tìm các đối tượng Skills dựa trên skillIds
                List<Skills> skillsList = skillsRepository.findAllById(skillIds);
                if (skillsList.isEmpty()) {
                    response.setStatusCode(400);
                    response.setMessage("Skills not found.");
                    return response;
                }
                List<Mentors> mentorsList = mentorsRepository.findBySkills(skillsList, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 4. Nếu chỉ có availableTo
            else if (isNullOrEmpty(name) && (skillIds == null || skillIds.isEmpty()) && availableFrom == null && availableTo != null) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByAvailableTo(availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 5. Nếu chỉ có availableFrom
            else if (isNullOrEmpty(name) && (skillIds == null || skillIds.isEmpty()) && availableFrom != null && availableTo == null) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByAvailableFrom(availableFrom, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 6. Nếu có cả name và skills
            else if (!isNullOrEmpty(name) && (skillIds != null && !skillIds.isEmpty()) && availableFrom == null && availableTo == null) {
                // Tìm các đối tượng Skills dựa trên skillIds
                List<Skills> skillsList = skillsRepository.findAllById(skillIds);
                if (skillsList.isEmpty()) {
                    response.setStatusCode(400);
                    response.setMessage("Skills not found.");
                    return response;
                }
                List<Mentors> mentorsList = mentorScheduleRepository.findByNameAndSkills(name, skillsList, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 7. Nếu có cả name và availableTo
            else if (!isNullOrEmpty(name) && availableTo != null && availableFrom == null && (skillIds == null || skillIds.isEmpty()) ) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameAndAvailableTo(name, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 8. Nếu có cả name và availableFrom
            else if (!isNullOrEmpty(name) && availableTo == null && availableFrom != null && (skillIds == null || skillIds.isEmpty()) ) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameAndAvailableFrom(name, availableFrom, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 9. Nếu có cả skills và availableTo
            else if (isNullOrEmpty(name) && availableTo != null && availableFrom == null && (skillIds != null || !skillIds.isEmpty()) ) {
                // Tìm các đối tượng Skills dựa trên skillIds
                List<Skills> skillsList = skillsRepository.findAllById(skillIds);
                if (skillsList.isEmpty()) {
                    response.setStatusCode(400);
                    response.setMessage("Skills not found.");
                    return response;
                }
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsBySkillsAndAvailableTo(skillsList, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 10. Nếu có cả skills và availableFrom
            else if (isNullOrEmpty(name) && availableTo == null && availableFrom != null && (skillIds != null || !skillIds.isEmpty()) ) {
                // Tìm các đối tượng Skills dựa trên skillIds
                List<Skills> skillsList = skillsRepository.findAllById(skillIds);
                if (skillsList.isEmpty()) {
                    response.setStatusCode(400);
                    response.setMessage("Skills not found.");
                    return response;
                }
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsBySkillsAndAvailableFrom(skillsList, availableFrom, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 11. Nếu có cả availableTo và availableFrom
            else if (isNullOrEmpty(name) && availableTo != null && availableFrom != null && (skillIds == null || skillIds.isEmpty()) ) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByAvailableFromAndTo(availableFrom, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 12. Nếu có cả name, skills, availableTo
            else if (!isNullOrEmpty(name) && availableTo != null && availableFrom == null && (skillIds != null || !skillIds.isEmpty())) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameSkillsAndAvailableTo(name, skillIds, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 13. Nếu có cả name, skills, availableFrom
            else if (!isNullOrEmpty(name) && skillIds != null && !skillIds.isEmpty() && availableFrom != null) {
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameSkillsAndAvailableFrom(name, skillIds, availableFrom, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 14. Nếu có cả skills, availableTo, availableFrom
            else if (skillIds != null && !skillIds.isEmpty() && availableFrom != null && availableTo != null) {
                // Kiểm tra nếu availableFrom không sau availableTo
                if (availableFrom.isAfter(availableTo)) {
                    response.setStatusCode(400);
                    response.setMessage("availableFrom cannot be after availableTo.");
                    return response;
                }

                // Lấy danh sách các mentor dựa trên kỹ năng và khoảng thời gian có sẵn
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsBySkillsAndAvailableFromTo(skillIds, availableFrom, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 15. Nếu có cả name, availableFrom và availableTo
            else if (!isNullOrEmpty(name) && availableFrom != null && availableTo != null) {
                // Kiểm tra nếu availableFrom không sau availableTo
                if (availableFrom.isAfter(availableTo)) {
                    response.setStatusCode(400);
                    response.setMessage("availableFrom cannot be after availableTo.");
                    return response;
                }

                // Lấy danh sách các mentor dựa trên tên và khoảng thời gian có sẵn
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameAndAvailableFromTo(name, availableFrom, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }
            // 16. Nếu có cả name, skills, availableTo, availableFrom
            else if (!isNullOrEmpty(name) && skillIds != null && !skillIds.isEmpty() && availableFrom != null && availableTo != null) {
                // Kiểm tra nếu availableFrom không sau availableTo
                if (availableFrom.isAfter(availableTo)) {
                    response.setStatusCode(400);
                    response.setMessage("availableFrom cannot be after availableTo.");
                    return response;
                }

                // Lấy danh sách các mentor dựa trên tên, kỹ năng, và khoảng thời gian có sẵn
                List<Mentors> mentorsList = mentorScheduleRepository.findMentorsByNameSkillsAvailableFromAndTo(name, skillIds, availableFrom, availableTo, AvailableStatus.ACTIVE);
                mentorsDTOList = mentorsList
                        .stream()
                        .map(Converter::convertMentorToMentorDTO)
                        .collect(Collectors.toList());
            }

            if (mentorsDTOList.isEmpty()) {
                response.setStatusCode(400);
                response.setMessage("No mentors found.");
            } else {

                List<MentorScheduleDTO> mentorScheduleDTOList = null;
                for (MentorsDTO m : mentorsDTOList) {
                    List<MentorScheduleDTO> scheduleDTOList = mentorScheduleService.findAllMentorScheduleByMentor(m.getId());
                    m.setMentorSchedules(scheduleDTOList);
                }

                response.setStatusCode(200);
                response.setMentorsDTOList(mentorsDTOList);
                response.setMessage("Mentors found successfully.");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while fetching mentor: " + e.getMessage());
        }
        return response;
    }

    public UsersDTO getMentorInformation(Long mentorId) {
        Response response = new Response();

        UsersDTO usersDTO = new UsersDTO();
        Mentors mentor = mentorsRepository.findByIdAndAvailableStatus(mentorId, AvailableStatus.ACTIVE);
        if (mentor == null) {
            return usersDTO;
        }
        usersDTO.setId(mentor.getUser().getId());
        usersDTO.setFullName(mentor.getUser().getFullName());
        usersDTO.setAvatar(mentor.getUser().getAvatar());
        usersDTO.setAddress(mentor.getUser().getAddress());
        usersDTO.setBirthDate(mentor.getUser().getBirthDate());
        usersDTO.setGender(mentor.getUser().getGender());
        usersDTO.setEmail(mentor.getUser().getEmail());

        response.setUsersDTO(usersDTO);
        return usersDTO;
    }

    public List<SkillsDTO> getSkillsByMentor(Long mentorId){
        List<SkillsDTO> skillsDTOList = new ArrayList<>();
        List<Skills> skillsList = mentorsRepository.findByIdAndAvailableStatus(mentorId,AvailableStatus.ACTIVE).getSkills();
        skillsDTOList = skillsList
                .stream()
                .map(Converter::convertSkillToSkillDTO)
                .collect(Collectors.toList());
        return skillsDTOList;
    }
}
