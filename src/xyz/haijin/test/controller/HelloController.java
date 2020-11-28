package xyz.haijin.test.controller;

import xyz.haijin.annotation.Controller;
import xyz.haijin.annotation.Encoding;
import xyz.haijin.annotation.RequestMapping;
import xyz.haijin.ioc.annoation.IocResource;
import xyz.haijin.json.JSONObject;
import xyz.haijin.test.entity.User;
import xyz.haijin.test.entity.WallpaperInfo;
import xyz.haijin.test.entity.WallpaperMsg;
import xyz.haijin.test.service.IUserService;
import xyz.haijin.web.context.WebContext;
import xyz.haijin.web.view.DispatchActionConstant;
import xyz.haijin.web.view.View;

import java.util.List;

@Controller
public class HelloController {

    @IocResource
    private IUserService userService;

    @RequestMapping("hello")
    @Encoding("gbk")
    public JSONObject<WallpaperMsg> hello() throws Exception {
//        System.out.println(name);
        String name = new WebContext().getRequest().getParameter("name");
        System.out.println("name:"+name);
        System.out.println(userService);
//        String data = userService.findOrder(name).toString();
        WallpaperMsg wallpaperMsg = new WallpaperMsg();
        wallpaperMsg.setList(userService.findOrder(name));

        JSONObject<WallpaperMsg> jsonObject = new JSONObject<WallpaperMsg>(wallpaperMsg);
        System.out.println(jsonObject);
//        View view = new View("index.jsp","hello", DispatchActionConstant.FORWARD,data);
        return jsonObject;
    }

}
