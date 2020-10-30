package xyz.haijin.test.controller;

import xyz.haijin.annotation.Controller;
import xyz.haijin.annotation.RequestMapping;
import xyz.haijin.web.view.DispatchActionConstant;
import xyz.haijin.web.view.View;

@Controller("/h")
public class HelloController {

    @RequestMapping("hello")
    public View hello(){
        System.out.println("Hello World");

        View view = new View("index.jsp","hello","Hello World", DispatchActionConstant.REDIRECT);
        return view;
    }

}
