package com.team.gym.controller;

import com.team.gym.dto.*;
import com.team.gym.errors.Unauthorized;
import com.team.gym.model.User;
import com.team.gym.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping
public class UserController {
    private final UserService users;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    public UserController(UserService users){ this.users = users; }

    /**
     * POST /auth/register -> búa til nýjan notanda
     *
     * @param req hlekkur {@link NyskraningRequest} sem inniheldur upplýsingar um innskráningu notanda
     * @return hlekkur {@link UserResponse} sem innheldur ID, SSN, og email nýskráðs notanda
     */
    @PostMapping("/auth/register")
    public UserResponse register(@RequestBody @Valid NyskraningRequest req){
        User u = users.register(req);
        String imageUrl = buildProfileImageUrl(u);

        return new UserResponse(
                u.getId(),
                u.getSsn(),
                u.getEmail(),
                imageUrl
        );
    }

    /**
     * POST /auth/login -> býr til JSESSIONID köku fyrir session
     *
     * @param req hlekkur {@link InnskraningRequest} sem inniheldur email og lykilorð notanda sem hyggst skrá sig inn
     * @param session núverandi hlekkur {@link HttpSession} notaður til að auðkenna notanda
     * @return hlekkur {@link UserResponse} sem inniheldur ID, SSN, og email auðkennds notanda
     * @throws Unauthorized ef auðkenni stemma ekki
     */
    @PostMapping("/auth/login")
    public UserResponse login(@RequestBody @Valid InnskraningRequest req, HttpSession session){
        User u = users.authenticate(req.email(), req.password());
        session.setAttribute("uid", u.getId());

        String imageUrl = buildProfileImageUrl(u);
        return new UserResponse(
                u.getId(),
                u.getSsn(),
                u.getEmail(),
                imageUrl
        );
    }

    /**
     * POST /auth/logout -> invaliderar sessionið svo notandi teljist skráður út
     *
     * @param session núverandi {@link HttpSession} bundið við innskráðan notanda
     */
    @PostMapping("/auth/logout")
    public void logout(HttpSession session){
        session.invalidate();
    }

    /**
     * GET /me -> innskráður notanda session
     *
     * @param session núverandi {@link HttpSession} bundið við innskráðan notanda
     * @return hlekkur {@link UserResponse} sem inniheldur ID, SSN, og email auðkennds notanda
     * @throws Unauthorized ef enginn notandi er innskráður eða session stemmir ekki
     */
    @GetMapping("/me")
    public UserResponse me(HttpSession session){
        Long uid = (Long) session.getAttribute("uid");
        if(uid == null) throw new Unauthorized();
        User u = users.get(uid);
        String imageUrl = buildProfileImageUrl(u);
        return new UserResponse(
                u.getId(),
                u.getSsn(),
                u.getEmail(),
                imageUrl
        );
    }

    /**
     * UC8 - Uppfæra upplýsingar notanda
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

        String imageUrl = buildProfileImageUrl(updated);

        return new UserResponse(
                updated.getId(),
                updated.getSsn(),
                updated.getEmail(),
                imageUrl
        );
    }

    /**
     * POST /me/profile-image: Hleður upp prófílmynd fyrir innskráðan notanda
     *
     * Tekur við myndaskrá (multipart/form-data) og vistar hana í geymslu
     * (Cloudflare R2). Ef notandi er þegar með prófílmynd er eldri mynd eytt.
     *
     * @param file    Multipart skrá sem inniheldur prófílmynd (t.d. JPG, PNG eða Webp)
     * @param session núverandi {@link HttpSession} bundið við innskráðan notanda
     * @return {@link ResponseEntity} sem inniheldur staðfestingu og lykil (object key)
     *         á vistuðu myndinni
     *
     * @throws Unauthorized ef enginn notandi er innskráður eða session er ekki gilt
     * @throws IllegalArgumentException ef skrá er ógild (t.d. of stór eða af rangri gerð)
     */
    @PostMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadProfileImage(
            @RequestPart("file") MultipartFile file,
            HttpSession session
    ) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();

        User updated = users.uploadProfileImage(uid, file);

        return ResponseEntity.ok(Map.of(
                "message", "profile_image_uploaded",
                "profileImageKey", updated.getProfileImageKey()
        ));
    }

    /**
     * UC9 - Eyða aðgangi notanda
     *
     * @param session núverandi session sem inniheldur auðkenni notanda
     * @return hlekkur {@link ResponseEntity} með HTTP status 204 (No Content) ef tókst að eyða aðgangi
     * @throws Unauthorized ef enginn notandi er innskráður
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();

        users.deleteAccount(uid);
        session.invalidate();

        return ResponseEntity.noContent().build();
    }

    private String buildProfileImageUrl(User user) {
        if (user.getProfileImageKey() != null && !user.getProfileImageKey().isBlank()) {
            return "https://" + bucket + "." + accountId + ".r2.dev/"
                    + user.getProfileImageKey();
        }

        // fallback avatar
        return "https://ui-avatars.com/api/?name="
                + user.getEmail()
                + "&background=random&color=fff&size=256";
    }

}
