package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.AlarmRecord;
import com.southwind.springboottest.charge.repository.AlarmRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmRecordRepository alarmRepository;

    @GetMapping("/findAll")
    public List<AlarmRecord> findAll(@RequestParam(value = "handled", required = false) Boolean handled) {
        if (handled == null) {
            return alarmRepository.findAll();
        }
        return alarmRepository.findByHandled(handled);
    }

    @PutMapping("/handle/{id}")
    public AlarmRecord handle(@PathVariable Long id) {
        AlarmRecord ar = alarmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("告警不存在"));
        ar.setHandled(true);
        return alarmRepository.save(ar);
    }
}

