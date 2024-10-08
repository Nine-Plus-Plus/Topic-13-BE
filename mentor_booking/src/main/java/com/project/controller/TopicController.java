/*

 */
package com.project.controller;

import com.project.dto.Response;
import com.project.dto.TopicDTO;
import com.project.model.Topic;
import com.project.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Thịnh Đạt
 */
@RestController
@RequestMapping("/api")
public class TopicController {
    
    @Autowired
    private TopicService topicService;
    
    @PostMapping("/admin/create-topic")
    public ResponseEntity<Response> createTopic(@RequestBody TopicDTO createRequest){
        Response response = topicService.createTopic(createRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @GetMapping("/admin/get-all-topics")
    public ResponseEntity<Response> getAllTopics(){
        Response response = topicService.getAllTopics();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @GetMapping("/admin/get-topic-by-id/{id}")
    public ResponseEntity<Response> getTopicById(@PathVariable Long id){
        Response response = topicService.getTopicById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @PutMapping("/admin/update-topic/{id}")
    public ResponseEntity<Response> updateTopic(@PathVariable Long id, @RequestBody Topic newTopic){
        Response response = topicService.updateTopic(id, newTopic);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @DeleteMapping("/admin/delete-topic/{id}")
    public ResponseEntity<Response> deleteTopic(@PathVariable Long id){
        Response response = topicService.deleteTopic(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}