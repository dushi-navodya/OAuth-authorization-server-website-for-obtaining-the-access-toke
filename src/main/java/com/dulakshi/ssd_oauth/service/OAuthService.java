package com.dulakshi.ssd_oauth.service;

import com.dulakshi.ssd_oauth.model.User;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

@Service
public class OAuthService {

    @Value("${app.oauth.clientid}")
    private String clientId;

    @Value("${app.oauth.secret}")
    private String secret;

    @Value("${app.oauth.authorization.url}")
    private String authorizationURL;

    @Value("${app.oauth.token.url}")
    private String tokenURL;

    @Value("${app.oauth.resource.url}")
    private String resourceURL;

    @Value("${app.oauth.redirection.url}")
    private String redirectionURL;

    public String getAuthorizationURL() {
        return authorizationURL + "?client_id=" + clientId + "&redirect_uri=" + redirectionURL + "&response_type=code";
    }

    public String getToken(String code) {
        try {
            byte[] encodedBytes = Base64.encodeBase64((clientId + ":" + secret).getBytes());
            String base64ClientIdSecret = new String(encodedBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("method", "post");
            headers.add("Authorization", "Basic " + base64ClientIdSecret);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("client_id", clientId);
            map.add("client_secret", secret);
            map.add("redirect_uri", redirectionURL);
            map.add("code", code);
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenURL, request, String.class);

            JSONObject object = (JSONObject) new JSONParser().parse(response.getBody());
            return object.get("access_token").toString();

        } catch (Exception e) {
            System.out.println("getToken");
            System.out.println(e.getMessage());
        }
        return "";
    }

    public User getUserInfo(String access_token, HttpSession session) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("method", "get");

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("fields", "id,name");
            map.add("access_token", access_token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(resourceURL, request, String.class);

            JSONObject object = (JSONObject) new JSONParser().parse(response.getBody());

            User user = new User(object.get("id").toString(), object.get("name").toString());
            session.setAttribute("userid", user.getId());
            session.setAttribute("name", user.getName());
            return user;

        } catch (Exception e) {
            System.out.println("getUserInfo");
            System.out.println(e.getMessage());
        }
        return null;
    }


    public ByteArrayOutputStream generateImg(HttpSession session) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            String imageUrl = "https://graph.facebook.com/" + session.getAttribute("userid") + "/picture?width=350&height=350";
            BufferedImage a = ImageIO.read(new URL(imageUrl));
            BufferedImage b = ImageIO.read(new File(System.getProperty("user.dir"), "frame.png"));
            BufferedImage c = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics g = c.getGraphics();
            g.drawImage(a, 0, 0, null);
            g.drawImage(b, 0, 0, null);

            os = new ByteArrayOutputStream();
            ImageIO.write(c, "png", os);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return os;
    }


}
