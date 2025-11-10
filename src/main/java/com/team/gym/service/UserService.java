package com.team.gym.service;

import com.team.gym.dto.BreytaNotandaRequest;
import com.team.gym.dto.NyskraningRequest;
import com.team.gym.model.User;
import com.team.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.team.gym.repository.BookingRepository;

@Service
public class UserService {
    private final UserRepository repo;
    private final BookingRepository bookings;

    public UserService(UserRepository repo, BookingRepository bookings){
        this.repo = repo; this.bookings = bookings;
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

    @Transactional
    public void deleteAccount(Long userId) {
        // Ensure user exists (optional but nice)
        if (!repo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found");
        }

        // 1) Delete all bookings for this user
        bookings.deleteByUserId(userId);

        // 2) Delete the user record
        repo.deleteById(userId);
    }

}
