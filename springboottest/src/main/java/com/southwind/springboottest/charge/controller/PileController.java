package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

//@RestController
//@RequestMapping("/api/pile")
public class PileController {

    @Autowired
    private ChargePileRepository pileRepository;

    @GetMapping("/findAll")
    public List<ChargePile> findAll() {
        return pileRepository.findAll();
    }

    @PostMapping("/save")
    public ChargePile save(@RequestBody ChargePile pile) {
        pile.setUpdatedAt(new Date());
        return pileRepository.save(pile);
    }

    @GetMapping("/findById/{id}")
    public ChargePile findById(@PathVariable Long id) {
        return pileRepository.findById(id).orElse(null);
    }

    @PutMapping("/update")
    public ChargePile update(@RequestBody ChargePile pile) {
        if (pile.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        pile.setUpdatedAt(new Date());
        return pileRepository.save(pile);
    }

    @PutMapping("/heartbeat/{pileNo}")
    public ChargePile heartbeat(@PathVariable String pileNo) {
        ChargePile pile = pileRepository.findByPileNo(pileNo).orElseThrow(() -> new IllegalArgumentException("桩不存在"));
        pile.setLastHeartbeat(new Date());
        pile.setStatus("IDLE");
        pile.setUpdatedAt(new Date());
        return pileRepository.save(pile);
    }

    @DeleteMapping("/deleteById/{id}")
    public String deleteById(@PathVariable Long id) {
        pileRepository.deleteById(id);
        return "success";
    }
}
