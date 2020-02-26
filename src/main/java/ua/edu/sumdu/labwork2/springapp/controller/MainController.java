package ua.edu.sumdu.labwork2.springapp.controller;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api")
public class MainController {

    @RequestMapping(
            path = "/test",
            method = RequestMethod.GET)
    public String test (@RequestParam(name = "album") String album, @RequestParam(name = "artist") String artist) {
        System.out.println("ok" + album + artist);
        return "ok" + artist + album;
    }

    @RequestMapping(path = "/test2/{abc}")
    public String getSmth (@PathVariable(name = "abc") String xyz) {
        return "test2 " + xyz;
    }
}
