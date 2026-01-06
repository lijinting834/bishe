package com.southwind.springboottest;

import com.southwind.springboottest.entity.Car;
import com.southwind.springboottest.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

@SpringBootTest
class SpringboottestApplicationTests {
    @Autowired
    private CarRepository repository;

    @Test
    void contextLoads() {
        PageRequest pageRequest = PageRequest.of(0, 6);
        Page<Car> page = repository.findAll(pageRequest);
        int i = 0;
    }

    @Test
    void save() {
        Car car = new Car();
        car.setName("Model S");
        car.setBrand("Tesla");
        car.setType("Performance");
        car.setOrigin("USA");
        car.setPrice(new BigDecimal("79999.99"));
        Car car1 = repository.save(car);
        System.out.println(car1);
    }

    @Test
    void findById() {
        Car car = repository.findById(1).orElse(null);
        System.out.println(car);
    }

    @Test
    void update() {
        Car car = new Car();
        car.setId(117);
        car.setName("测试车型");
        car.setBrand("测试品牌");
        car.setType("测试配置");
        car.setOrigin("测试产地");
        car.setPrice(new BigDecimal("12345.67"));
        Car car1 = repository.save(car);
        System.out.println(car1);
    }

    @Test
    void delete() {
        repository.deleteById(117);
    }
}