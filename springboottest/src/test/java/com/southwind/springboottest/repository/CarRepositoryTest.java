package com.southwind.springboottest.repository;

import com.southwind.springboottest.entity.Car;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Car> page = carRepository.findAll(pageable);
        List<Car> cars = page.getContent();
        cars.forEach(System.out::println);
    }

}