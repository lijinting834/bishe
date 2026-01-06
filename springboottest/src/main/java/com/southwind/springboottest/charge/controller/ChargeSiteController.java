package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeSite;
import com.southwind.springboottest.charge.repository.ChargeSiteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/site")
public class ChargeSiteController {

    private final ChargeSiteRepository siteRepository;

    public ChargeSiteController(ChargeSiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    // 前端用的是 findAll
    @GetMapping("/findAll")
    public List<ChargeSite> findAll() {
        return siteRepository.findAll();
    }

    // 新增
    @PostMapping("/save")
    public ChargeSite save(@RequestBody ChargeSite site) {
        return siteRepository.save(site);
    }

    // 更新（前端用 PUT /update）
    @PutMapping("/update")
    public ChargeSite update(@RequestBody ChargeSite site) {
        if (site.getId() == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        return siteRepository.save(site);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        siteRepository.deleteById(id);
        return "success";
    }
}

