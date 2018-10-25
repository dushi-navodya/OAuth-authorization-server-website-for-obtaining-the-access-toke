package com.dulakshi.ssd_oauth.controller;

import com.dulakshi.ssd_oauth.model.User;
import com.dulakshi.ssd_oauth.service.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class OAuthController {

    @Autowired
    private OAuthService oAuthService;

    @GetMapping("")
    public String index(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/signin";
        } else {
            return "redirect:/home";
        }
    }


    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/signin";
        } else {
            model.addAttribute("name", session.getAttribute("name"));
            model.addAttribute("profile_picture", "https://graph.facebook.com/" + session.getAttribute("userid") + "/picture?width=350&height=350");
            return "index";
        }
    }

    @GetMapping("/img")
    @ResponseBody
    public ResponseEntity<byte[]> img(HttpSession session) {
//        if (!isAuthenticated(session)) {
//            return "redirect:/signin";
//        } else {
//        }
//        return "data:image/png;base64,"+oAuthService.generateImg(session);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

//        System.out.println("data:image/png;base64,"+oAuthService.generateImg(session));

        return new ResponseEntity<byte[]>(oAuthService.generateImg(session).toByteArray(), headers, HttpStatus.CREATED);
    }

    @GetMapping("/signin")
    public String signin(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            return "redirect:/home";
        } else {
            model.addAttribute("authorization_url", oAuthService.getAuthorizationURL());
            return "signin";
        }
    }


    @GetMapping("/redirection")
    public String redirection(HttpSession session, @RequestParam("code") String code, Model model) {

        String access_token = oAuthService.getToken(code);
        if (!access_token.equals("")) {
            User user = oAuthService.getUserInfo(access_token, session);

            if (user == null) {
                model.addAttribute("error", "Error retrieving user data!.");
                return "error";
            } else {
                return "redirect:/home";
            }
        } else {
            model.addAttribute("error", "Error retrieving access token!.");
            return "error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("username");
        session.invalidate();
        return "redirect:/signin";
    }

    private boolean isAuthenticated(HttpSession session) {
        if (session.getAttribute("userid") == null) {
            return false;
        } else {
            return true;
        }
    }
}
