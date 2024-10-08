
package com.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicDTO {
    private Long id;
    private String topicName;
    private String context;
    private String problems;
    private String actor;
    private String requirement;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private ProjectsDTO project;
}
