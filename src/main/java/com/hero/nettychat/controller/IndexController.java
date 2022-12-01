package com.hero.nettychat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private Environment env;

    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        String port = env.resolvePlaceholders("${server.port}");
        return "hello world port : " +port;
    }

    @RequestMapping("/index")
    public String index() {
        return "index";
    }
}
