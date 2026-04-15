package com.team.gym.service;

import com.team.gym.dto.BreytaNotandaRequest;
import com.team.gym.dto.NyskraningRequest;
import com.team.gym.model.User;
import com.team.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.team.gym.repository.BookingRepository;

@Service
public class UserService {
    private final UserRepository repo;
    private final BookingRepository bookings;
    private final ProfileImageService profileImageService;

    public UserService(UserRepository repo, BookingRepository bookings, ProfileImageService profileImageService) {
        this.repo = repo;
        this.bookings = bookings;
        this.profileImageService = profileImageService;
    }

    /**
     * Stofnar aðgang fyrir notanda
     *
     * @param req requesta fyrir nýskráningu sem inniheldur SSN, email, og lykilorð
     * @return nýjum vistuðum {@link User} notanda
     * @throws IllegalArgumentException ef email eða SSN er nú þegar bundið öðrum notanda
     */
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

    /**
     * Auðkennir notanda sem hyggst skrá sig inn
     *
     * @param email tölvupóstfang notanda
     * @param password lykilorð notanda
     * @return hlekkur {@link User} fyrir auðkenndan notanda
     * @throws IllegalArgumentException ef email og lykilorð stemma ekki
     */
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

    public User uploadProfileImage(Long userId, MultipartFile file) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));

        String oldKey = user.getProfileImageKey();

        String newKey = profileImageService.uploadUserProfileImage(userId, file);

        user.setProfileImageKey(newKey);
        User saved = repo.save(user);

        if (oldKey != null) {
            profileImageService.delete(oldKey);
        }

        return saved;
    }

    /**
     * Eyðir aðgangi notanda
     *
     * @param userId auðkenni notanda sem á að eyða
     * @throws ResponseStatusException með status 404 ef notandi er ekki til
     */
    @Transactional
    public void deleteAccount(Long userId) {
        if (!repo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found");
        }

        bookings.deleteByUserId(userId);

        repo.deleteById(userId);
    }

}
