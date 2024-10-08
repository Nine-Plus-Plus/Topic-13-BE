
package com.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SemesterDTO {
    private Long id;
    private String semesterName;
    private LocalDateTime dateCreated;
    private List<Class> classes;
}
