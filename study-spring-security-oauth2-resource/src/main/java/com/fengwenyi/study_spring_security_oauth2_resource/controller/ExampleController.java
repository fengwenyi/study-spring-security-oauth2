package com.fengwenyi.study_spring_security_oauth2_resource.controller;

import com.fengwenyi.api_result.helper.ResultHelper;
import com.fengwenyi.api_result.model.ResultModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erwin Feng
 * @since 2019-08-21 17:54
 */
@RestController
public class ExampleController {

    @RequestMapping("/")
    public ResultModel selectAll() {
        String [] data = {"Zhangsan", "Lisi", "Wangwu"};
        return ResultHelper.success("Success", data);
    }

    @RequestMapping("/insert")
    public ResultModel index(String name) {
        return ResultHelper.success("Success", name);
    }

}
