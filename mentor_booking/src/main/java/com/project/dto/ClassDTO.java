
package com.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassDTO {
    private Long id;
    private String className;
    private SemesterDTO semester;
    private LocalDateTime dateCreated;
    private List<StudentsDTO> students;
    private MentorsDTO mentor; 
}
