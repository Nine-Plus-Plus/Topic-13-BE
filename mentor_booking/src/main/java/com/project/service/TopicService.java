package com.project.service;

import com.project.dto.Response;
import com.project.dto.TopicDTO;
import com.project.enums.AvailableStatus;
import com.project.exception.OurException;
import com.project.model.Mentors;
import com.project.model.Semester;
import com.project.model.Topic;
import com.project.repository.ClassRepository;
import com.project.repository.MentorsRepository;
import com.project.repository.SemesterRepository;
import com.project.repository.TopicRepository;
import com.project.ultis.Converter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thịnh Đạt
 */
@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private MentorsRepository mentorsRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private ClassRepository classRepository;

    public Response createTopic(TopicDTO createRequest) {
        Response response = new Response();

        try {
            if (topicRepository.findByTopicName(createRequest.getTopicName()).isPresent()) {
                throw new OurException("Topic has already existed");
            }
            Mentors mentor = new Mentors();
            Semester semester = new Semester();
            if (createRequest.getMentorsDTO() != null) {
                mentor = mentorsRepository.findById(createRequest.getMentorsDTO().getId())
                        .orElseThrow(() -> new OurException("Cannot find mentor id"));
            }
            if (createRequest.getSemesterDTO() != null) {
                semester = semesterRepository.findById(createRequest.getSemesterDTO().getId())
                        .orElseThrow(() -> new OurException("Cannot find semester id"));
            }

            Topic topic = new Topic();
            topic.setTopicName(createRequest.getTopicName());
            topic.setContext(createRequest.getContext());
            topic.setProblems(createRequest.getProblems());
            topic.setActor(createRequest.getActor());
            topic.setRequirement(createRequest.getRequirement());
            topic.setNonFunctionRequirement(createRequest.getNonFunctionRequirement());
            topic.setDateCreated(LocalDateTime.now());
            topic.setDateUpdated(LocalDateTime.now());

            topic.setMentor(mentor);
            topic.setSemester(semester);

            topic.setAvailableStatus(AvailableStatus.ACTIVE);
            topicRepository.save(topic);

            if (topic.getId() > 0) {
                TopicDTO dto = Converter.convertTopicToTopicDTO(topic);
                response.setTopicDTO(dto);
                response.setStatusCode(200);
                response.setMessage("Topic added successfully");
            }

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during topic creation: " + e.getMessage());
        }

        return response;
    }

    public Response getAllTopics() {
        Response response = new Response();
        try {
            List<Topic> topicLists = topicRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
            List<TopicDTO> topicListDTO = null;
            if (topicLists != null) {
                topicListDTO = topicLists.stream()
                        .map(Converter::convertTopicToTopicDTO)
                        .collect(Collectors.toList());
                response.setTopicDTOList(topicListDTO);
                response.setStatusCode(200);
                response.setMessage("Topics fetched successfully");
            } else {
                response.setTopicDTOList(topicListDTO);
                throw new OurException("Cannot find any topic");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occured during get all topics " + e.getMessage());
        }
        return response;
    }

    public Response getTopicById(Long id) {
        Response response = new Response();
        try {
            Topic findTopic = topicRepository.findByIdAndAvailableStatus(id, AvailableStatus.ACTIVE);
            TopicDTO dto = Converter.convertTopicToTopicDTO(findTopic);
            response.setTopicDTO(dto);
            if (findTopic != null) {
                response.setStatusCode(200);
                response.setMessage("Successfully");
            } else {
                response.setStatusCode(400);
                response.setMessage("No data found");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occured during get topic " + e.getMessage());
        }
        return response;
    }

    public Response updateTopic(Long id, Topic newTopic) {
        Response response = new Response();
        try {
            Topic presentTopic = topicRepository.findById(id)
                    .orElseThrow(() -> new OurException("Cannot find topic with id: " + id));
            if (topicRepository.findByTopicName(newTopic.getTopicName()).isPresent()
                    && newTopic.getTopicName().equals(presentTopic.getTopicName()) == false) {
                throw new OurException("Semester has already existed");
            }
            Mentors mentor = new Mentors();
            Semester semester = new Semester();
            if (newTopic.getMentor() != null) {
                mentor = mentorsRepository.findById(newTopic.getMentor().getId())
                        .orElseThrow(() -> new OurException("Cannot find mentor id"));
            }
            if (newTopic.getSemester() != null) {
                semester = semesterRepository.findById(newTopic.getSemester().getId())
                        .orElseThrow(() -> new OurException("Cannot find semester id"));
            }

            presentTopic.setTopicName(newTopic.getTopicName());
            presentTopic.setContext(newTopic.getContext());
            presentTopic.setProblems(newTopic.getProblems());
            presentTopic.setActor(newTopic.getActor());
            presentTopic.setRequirement(newTopic.getRequirement());
            presentTopic.setNonFunctionRequirement(newTopic.getNonFunctionRequirement());
            presentTopic.setDateUpdated(LocalDateTime.now());
            presentTopic.setMentor(mentor);
            presentTopic.setSemester(semester);

            topicRepository.save(presentTopic);

            TopicDTO dto = Converter.convertTopicToTopicDTO(presentTopic);
            response.setTopicDTO(dto);
            response.setStatusCode(200);
            response.setMessage("Topic updated successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while updating topic: " + e.getMessage());
        }
        return response;
    }

    public Response deleteTopic(Long id) {
        Response response = new Response();
        try {
            Topic deleteTopic = topicRepository.findById(id)
                    .orElseThrow(() -> new OurException("Cannot find topic with id: " + id));
            deleteTopic.setAvailableStatus(AvailableStatus.DELETED);
            topicRepository.save(deleteTopic);

            response.setStatusCode(200);
            response.setMessage("Topic deleted successfully");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while deleting topic: " + id);
        }
        return response;
    }

    public Response getTopicBySemesterId(Long semesterId) {
        Response response = new Response();
        try {
            List<Topic> topicList = topicRepository.findTopicsBySemesterIdAndAvailableStatus(semesterId, AvailableStatus.ACTIVE);
            if (topicList != null) {
                List<TopicDTO> topicListDTO = topicList.stream()
                        .map(Converter::convertTopicToTopicDTO)
                        .collect(Collectors.toList());
                response.setTopicDTOList(topicListDTO);
                response.setStatusCode(200);
                response.setMessage("Topic fetched successfully");
            } else {
                throw new OurException("Cannot find topics with semester ID: " + semesterId);
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all topics " + e.getMessage());
        }
        return response;
    }

    public Response getTopicByName(String topicName) {
        Response response = new Response();
        try {
            List<Topic> topicList = topicRepository.findByTopicNameContainingIgnoreCaseAndAvailableStatus(topicName, AvailableStatus.ACTIVE);
            if (topicList != null) {
                List<TopicDTO> topicListDTO = topicList.stream()
                        .map(Converter::convertTopicToTopicDTO)
                        .collect(Collectors.toList());
                response.setTopicDTOList(topicListDTO);
                response.setStatusCode(200);
                response.setMessage("Topic fetched successfully");
            } else {
                response.setTopicDTOList(new ArrayList<>());
                throw new OurException("Cannot find topics with the input: " + topicName);
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all topics " + e.getMessage());
        }
        return response;
    }

    public Response getUnchosenTopicsInClass(Long classId) {
        Response response = new Response();
        try {
            List<TopicDTO> topicListDTO = new ArrayList<>();
            if (classRepository.findById(classId).isPresent()) {
                List<Topic> topicList = topicRepository.findUnchosenTopicsInClass(classId);
                if (topicList != null) {
                    topicListDTO = topicList.stream()
                            .map(Converter::convertTopicToTopicDTO)
                            .collect(Collectors.toList());
                    response.setTopicDTOList(topicListDTO);
                    response.setStatusCode(200);
                    response.setMessage("Topics fetched successfully");
                }
            }else{
                throw new OurException("Cannot find class by id: "+classId);
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all topics " + e.getMessage());
        }
        return response;
    }
}
