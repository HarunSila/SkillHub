package com.skillhub.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 *  Dieser Controller leitet alle Anfragen auf die Startseite weiter, welche in src/main/resources/static/index.html liegt.
 *  Dies ist notwendig, da Angular Routing verwendet wird und die Angular App sonst nicht richtig funktioniert.
 * 
 *  Anfragen auf die API (REST-Controller) werden nicht weitergeleitet, da diese mit einem /<Präfix>/ beginnen und somit nicht von diesem Controller abgefangen werden.
 */

@Controller
public class WebController {
    @GetMapping(value = "/{path:[^\\.]*}", produces = "text/html") 
    public String forward(@PathVariable String path) {
        return "forward:/index.html";
    }
}
