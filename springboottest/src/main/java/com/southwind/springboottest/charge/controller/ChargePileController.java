package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/pile")
public class ChargePileController {

    private final ChargePileRepository pileRepository;

    public ChargePileController(ChargePileRepository pileRepository) {
        this.pileRepository = pileRepository;
    }

    // 兼容前端：/api/pile/list
    @GetMapping("/list")
    public List<ChargePile> list() {
        return pileRepository.findAll();
    }

    // 兼容前端：/api/pile/findAll
    @GetMapping("/findAll")
    public List<ChargePile> findAll() {
        return pileRepository.findAll();
    }

    // 只匹配纯数字，避免把 findAll / list 这种当成 id
    @GetMapping("/{id:\\d+}")
    public ChargePile findById(@PathVariable Long id) {
        return pileRepository.findById(id).orElse(null);
    }

    @PostMapping("/save")
    public ChargePile save(@RequestBody ChargePile pile) {
        pile.setUpdatedAt(new Date());
        return pileRepository.save(pile);
    }

    @DeleteMapping("/delete/{id:\\d+}")
    public String delete(@PathVariable Long id) {
        pileRepository.deleteById(id);
        return "success";
    }
}
