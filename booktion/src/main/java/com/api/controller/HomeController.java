package com.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class HomeController{

    @GetMapping("/")
    public String index(){
        return "forward:/index.html";
    }


    @GetMapping("/leaderboard")
    public String leaderboard(){
        return "forward:/leaderboard.html";
    }


}









