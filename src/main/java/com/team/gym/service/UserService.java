package com.team.gym.service;

import com.team.gym.dto.BreytaNotandaRequest;
import com.team.gym.dto.NyskraningRequest;
import com.team.gym.model.User;
import com.team.gym.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo){
        this.repo = repo;
    }

    public User register(NyskraningRequest req){
        String email = req.email().toLowerCase();
        if(repo.existsByEmail(email)) throw new IllegalArgumentException("email_taken");
        if(repo.existsBySsn(req.ssn())) throw new IllegalArgumentException("ssn_taken");

        User u = new User();
        u.setSsn(req.ssn());
        u.setEmail(email);
        u.setPassword(req.password());
        return repo.save(u);
    }

    public User authenticate(String email, String password){
        var u = repo.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("invalid_credentials"));
        if(!u.getPassword().equals(password)){
            throw new IllegalArgumentException("invalid_credentials");
        }
        return u;
    }
    public User get(Long id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not_found"));
    }

    /**
     * Uppfærir reikningsupplýsingar notanda eftir gefini beiðni.
     *
     * @param uid Auðkennisnúmer notanda sem á að uppfæra.
     * @param req {@link BreytaNotandaRequest} hlutur sem inniheldur nýju kennitöluna, tölvupóstinn og lykilorðið.
     * @return Uppfærður {@link User} entity eftir að breytingar eru vistaðar.
     *
     * @throws RuntimeException ef enginn notandi finnst með gefið auðkennisnúmer.
     * @throws IllegalArgumentException ef tölvupósturinn eða kennitalan eru nú þegar bundin öðrum notanda.
     */
    public User updateUser(Long uid, BreytaNotandaRequest req) {
        User u = repo.findById(uid).orElseThrow(() -> new RuntimeException("not_found"));

        if (!u.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("email_taken");
        }
        if (!u.getSsn().equals(req.ssn()) && repo.existsBySsn(req.ssn())) {
            throw new IllegalArgumentException("ssn_taken");
        }
        u.setEmail(req.email());
        u.setSsn(req.ssn());
        u.setPassword(req.password());
        return repo.save(u);
    }
}
