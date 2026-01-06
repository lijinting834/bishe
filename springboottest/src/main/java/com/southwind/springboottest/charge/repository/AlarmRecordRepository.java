package com.southwind.springboottest.charge.repository;

import com.southwind.springboottest.charge.entity.AlarmRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRecordRepository extends JpaRepository<AlarmRecord, Long> {
    List<AlarmRecord> findByHandled(Boolean handled);
}
