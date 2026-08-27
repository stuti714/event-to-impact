package com.eventtoimpact.india.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/")
    public String home() { return "redirect:/welcome"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/welcome")
    public String welcome() { return "welcome"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }
}
