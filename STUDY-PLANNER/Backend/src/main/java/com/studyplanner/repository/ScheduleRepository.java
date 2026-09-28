package com.studyplanner.repository;

import com.studyplanner.model.StudySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<StudySchedule, Long> {
    List<StudySchedule> findByUserIdOrderBySessionOrderAsc(Long userId);
    void deleteByUserId(Long userId);
    long countByUserIdAndIsBreakFalse(Long userId);
    long countByUserIdAndIsBreakFalseAndStatus(Long userId, String status);
}
