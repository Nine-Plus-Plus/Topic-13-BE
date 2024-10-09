
package com.project.repository;


import com.project.enums.AvailableStatus;
import com.project.model.Class;
import com.project.model.Mentors;
import com.project.model.Semester;

import java.util.List;
import java.util.Optional;

import com.project.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<com.project.model.Class, Long>{
    Optional<com.project.model.Class> findByMentorId(Long mentorId);

    Optional<com.project.model.Class> findBySemesterId(Long semesterId);

    Optional<com.project.model.Class> findByClassName(String className);
    boolean existsByClassNameAndSemesterId(String className, Long semesterId);

    @Query("SELECT c FROM Class c WHERE c.semester.id = :semesterId AND u.availableStatus = :availableStatus")
    List<Class> findClassBySemesterId(@Param("semesterId") Long semesterId, @Param("availableStatus") AvailableStatus status );

    List<Class> findByAvailableStatus(AvailableStatus availableStatus);

    @Query("SELECT c FROM Users c WHERE c.id = :id AND c.availableStatus = :availableStatus")
    Class findByIdAndAvailableStatus(@Param("id") Long id, @Param("availableStatus") AvailableStatus status);
}
