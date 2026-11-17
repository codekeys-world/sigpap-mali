package ml.apes.rh.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import ml.apes.rh.auth.Dtos.DemandeReinitialisation;
import ml.apes.rh.auth.Dtos.LoginRequest;
import ml.apes.rh.auth.Dtos.LoginResponse;
import ml.apes.rh.auth.Dtos.ReinitialiserMotDePasse;
import ml.apes.rh.auth.ReinitialisationMotDePasseService;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.personnel.Dtos.ChangerMotDePasse;
import ml.apes.rh.personnel.Dtos.ProfilPersonnel;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.personnel.PersonnelService;
import ml.apes.rh.security.Acces;
import ml.apes.rh.security.ActivityLogFilter;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.JwtService;
import ml.apes.rh.security.LoginAttemptService;
import ml.apes.rh.security.PublicFormProtectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PersonnelRepository personnelRepository;
    private final JwtService jwtService;
    private final CurrentUserService currentUser;
    private final LoginAttemptService loginAttempts;
    private final PublicFormProtectionService publicForms;
    private final PersonnelService personnelService;
    private final ReinitialisationMotDePasseService reinitialisation;
    private final boolean cookieSecure;
    private final long expirationMinutes;

    public AuthController(
        AuthenticationManager authenticationManager,
        PersonnelRepository personnelRepository,
        JwtService jwtService,
        CurrentUserService currentUser,
        LoginAttemptService loginAttempts,
        PublicFormProtectionService publicForms,
        PersonnelService personnelService,
        ReinitialisationMotDePasseService reinitialisation,
        @Value("${app.auth-cookie-secure:false}") boolean cookieSecure,
        @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.authenticationManager = authenticationManager;
        this.personnelRepository = personnelRepository;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
        this.loginAttempts = loginAttempts;
        this.publicForms = publicForms;
        this.personnelService = personnelService;
        this.reinitialisation = reinitialisation;
        this.cookieSecure = cookieSecure;
        this.expirationMinutes = expirationMinutes;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest req,
        HttpServletRequest http
    ) {
        String matricule = req.matricule().trim();
        publicForms.login(http.getRemoteAddr());
        http.setAttribute(ActivityLogFilter.ATTR_MATRICULE, matricule); // pour le journal (le contexte est encore anonyme)
        // lève BadCredentialsException / DisabledException, traitées par GlobalExceptionHandler
        loginAttempts.verifier(matricule); // 5 échecs => verrouillage 15 min
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(matricule, req.motDePasse())
            );
        } catch (BadCredentialsException e) {
            loginAttempts.echec(matricule);
            throw e;
        }
        loginAttempts.succes(matricule);
        Personnel p = personnelRepository.findByMatricule(matricule).orElseThrow();
        String role = p.getRole() != null ? p.getRole().getNom() : "";
        http.setAttribute(ActivityLogFilter.ATTR_ROLE, role);
        List<String> auts =
            p.getRole() == null
                ? List.of()
                : p
                      .getRole()
                      .getAutorisations()
                      .stream()
                      .map(a -> a.getNom())
                      .sorted()
                      .toList();
        String jwt = jwtService.generer(matricule, role, req.seSouvenirDeMoi());
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from("APES_AUTH", jwt)
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Lax")
            .path("/api");
        if (req.seSouvenirDeMoi()) {
            long duree = Math.max(expirationMinutes * 60_000L, Duration.ofDays(30).toMillis());
            cookie.maxAge(Duration.ofMillis(duree));
        }
        LoginResponse profil = new LoginResponse(
            p.getSlug(),
            p.getMatricule(),
            p.getNom(),
            p.getPrenom(),
            p.getPhoto(),
            role,
            auts,
            p.isMotDePasseTemporaire()
        );
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.build().toString())
            .body(profil);
    }

    @GetMapping("/csrf")
    public String csrf(CsrfToken csrfToken) {
        return csrfToken.getToken();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("APES_AUTH", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Lax")
            .path("/api")
            .maxAge(Duration.ZERO)
            .build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/mot-de-passe-oublie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void demanderReinitialisation(
        @Valid @RequestBody DemandeReinitialisation req,
        HttpServletRequest http
    ) {
        publicForms.forgot(http.getRemoteAddr(), req.email());
        if (org.springframework.util.StringUtils.hasText(req.siteWeb())) return;
        reinitialisation.demander(req.email());
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reinitialiserMotDePasse(
        @Valid @RequestBody ReinitialiserMotDePasse req,
        HttpServletRequest http
    ) {
        publicForms.reset(http.getRemoteAddr());
        reinitialisation.reinitialiser(req.jeton(), req.nouveauMotDePasse(), req.confirmation());
    }

    /** Profil de l'utilisateur connecté. */
    @GetMapping("/moi")
    @PreAuthorize(Acces.TOUS)
    public Personnel moi() {
        return currentUser.get();
    }

    @GetMapping("/auteurs/{matricule}")
    @PreAuthorize(Acces.LECTURE_AUDIT)
    public Dtos.AuteurAudit auteurAudit(@PathVariable String matricule) {
        return personnelRepository
            .findAuteurByMatricule(matricule)
            .map(auteur ->
                new Dtos.AuteurAudit(auteur.getMatricule(), auteur.getPrenom(), auteur.getNom())
            )
            .orElseGet(() -> new Dtos.AuteurAudit(matricule, "", ""));
    }

    @PutMapping("/moi")
    @PreAuthorize(Acces.MODIFIER_PROFIL)
    public Personnel modifierMonProfil(@Valid @RequestBody ProfilPersonnel req) {
        return personnelService.modifierMonProfil(req);
    }

    /** Changement de son propre mot de passe (ancien mot de passe exigé). */
    @PostMapping("/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.TOUS)
    public void changerMotDePasse(@Valid @RequestBody ChangerMotDePasse req) {
        personnelService.changerMotDePasse(req);
    }
}
