package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeSite;
import com.southwind.springboottest.charge.repository.ChargeSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/api/site")
public class SiteController {

    @Autowired
    private ChargeSiteRepository siteRepository;

    @GetMapping("/findAll")
    public List<ChargeSite> findAll() {
        return siteRepository.findAll();
    }

    @PostMapping("/save")
    public ChargeSite save(@RequestBody ChargeSite site) {
        return siteRepository.save(site);
    }

    @GetMapping("/findById/{id}")
    public ChargeSite findById(@PathVariable Long id) {
        return siteRepository.findById(id).orElse(null);
    }

    @PutMapping("/update")
    public ChargeSite update(@RequestBody ChargeSite site) {
        if (site.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return siteRepository.save(site);
    }

    @DeleteMapping("/deleteById/{id}")
    public String deleteById(@PathVariable Long id) {
        siteRepository.deleteById(id);
        return "success";
    }
}
