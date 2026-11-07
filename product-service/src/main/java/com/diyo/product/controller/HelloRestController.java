package com.diyo.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloRestController {

    @GetMapping("/grt")
    public String greet() {
        return "Hello World";
    }
}
