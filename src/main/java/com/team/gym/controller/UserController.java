package com.team.gym.controller;

import com.team.gym.dto.*;
import com.team.gym.errors.Unauthorized;
import com.team.gym.model.User;
import com.team.gym.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {
    private final UserService users;

    public UserController(UserService users){ this.users = users; }

    //POST /auth/register -> búa til nýjan notanda
    @PostMapping("/auth/register")
    public UserResponse register(@RequestBody @Valid NyskraningRequest req){
        User u = users.register(req);
        return new UserResponse(u.getId(), u.getSsn(), u.getEmail());
    }

    //POST /auth/login -> býr til JSESSIONID köku fyrir session
    @PostMapping("/auth/login")
    public UserResponse login(@RequestBody @Valid InnskraningRequest req, HttpSession session){
        User u = users.authenticate(req.email(), req.password());
        session.setAttribute("uid", u.getId());
        return new UserResponse(u.getId(), u.getSsn(), u.getEmail());
    }

    //POST /auth/logout -> invaliderar sessionið svo notandi teljist skráður út
    @PostMapping("/auth/logout")
    public void logout(HttpSession session){
        session.invalidate();
    }

    //GET /me -> innskráður notanda session
    @GetMapping("/me")
    public UserResponse me(HttpSession session){
        Long uid = (Long) session.getAttribute("uid");
        if(uid == null) throw new Unauthorized();
        User u = users.get(uid);
        return new UserResponse(u.getId(), u.getSsn(), u.getEmail());
    }

    /**
     * Uppfærir upplýsingar um innskráðan notanda
     *
     * @param req     Breiðni sem inniheldur kennitölu, tölvupóst og lykilorð.
     * @param session Núverandi HTTP session notað til að auðkenna innskráðan notanda.
     * @return {@link UserResponse} hlutur með uppfærðum upplýsingum um notanda.
     * @throws Unauthorized ef enginn notandi er skráður inn eða session er ekki gilt.
     */
    @PutMapping("/me")
    public UserResponse updateUser(@RequestBody @Valid BreytaNotandaRequest req, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();
        User updated = users.updateUser(uid, req);
        return new UserResponse(updated.getId(), updated.getSsn(), updated.getEmail());
    }
}
