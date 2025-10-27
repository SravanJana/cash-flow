package com.sravan.moneymanager.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/status")
    public String healthCheck(){
        return "Application is running ";
    }

    @GetMapping("/Hello")
    public String hello(){
        return "Hello world";
    }




}
