package com.bdu.laborder.controller;

import com.bdu.laborder.common.BussinessCode;
import com.bdu.laborder.common.Constant;
import com.bdu.laborder.common.Result;
import com.bdu.laborder.common.ResultGenerator;
import com.bdu.laborder.entity.User;
import com.bdu.laborder.service.LoginService;
import com.bdu.laborder.utils.JwtUtils;
import com.bdu.laborder.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author Qi
 * @data 2020/12/13 0:32
 */
@RestController
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RedisUtil redisUtil;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        String loginName = user.getLoginName().replace(" ","");
        String password = user.getPassword().replace(" ","");
        System.out.println("====username: "+loginName+"  ====password: "+password+"  ====  ");
        //判断用户名密码是否为空
        if (loginName.isEmpty()||password.isEmpty()){
            return ResultGenerator.returnCodeMessage(BussinessCode.RESULT_LOGIN_NULL);
        }
        //根据用户名密码判断是否存在用户
        User loginUser = loginService.login(loginName,password);

        if (loginUser != null){
            //验证登录成功
            String token ;
            //在缓存中查找用户token
            Object redisToken = redisUtil.get("token :"+loginUser.getUserId());
            if(redisToken != null) {
                token = (String) redisToken;
            }else {
                //其他数据以map集合存放在token中
                Map<String, Object> dataMap = new HashMap<>();
//            dataMap.put("roles",loginUser.getRoles());
//            dataMap.put("realName", loginUser.getRealName());
//            dataMap.put("avatar", loginUser.getAvatar());
                //生成token并存入数据返回
                token = jwtUtils.createJwt(Integer.toString(loginUser.getUserId()), loginUser.getLoginName(), dataMap);
                redisUtil.set("token :"+loginUser.getUserId(),token,3600);

            }

            //判断用户是否为首次登录
            if (Constant.IS_FIRST_LOGIN.equals(loginUser.getIsFirstlogin())){
                //  返回信息 进入修改密码页面
                return ResultGenerator.returnCodeMessage(BussinessCode.IS_FIRST_LOGIN,token);
            }
            return ResultGenerator.returnCodeMessage(BussinessCode.RESULT_GLOBAL_SUCCESS, token);
        }
       return ResultGenerator.error(BussinessCode.RESULT_LOGIN_FAIL);
    }

    @PostMapping("/user/info")
    public Result getUserInfo(String token) {
        User user = loginService.selectUserByToken(token);
        if (user == null) {
            return ResultGenerator.error(BussinessCode.RESULT_INFO_FAIL);
        }
        return ResultGenerator.returnCodeMessage(BussinessCode.RESULT_GLOBAL_SUCCESS, user);
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        //根据token 获取用户
        // 获取token
        String token = request.getHeader("X-Token");
        if (token == null){
            token = request.getHeader("Token");
        }
        // 解析token 获取id
        String userId = jwtUtils.parseJwt(token).getId();
        // 删除token
        redisUtil.del("token :"+userId);
        return ResultGenerator.returnCodeMessage(BussinessCode.RESULT_GLOBAL_SUCCESS);
    }


}
