package ml.apes.rh.security;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.personnel.PersonnelRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final PersonnelRepository repository;

    @Override
    public UserDetails loadUserByUsername(String matricule) throws UsernameNotFoundException {
        Personnel p = repository
            .findByMatricule(matricule)
            .orElseThrow(() -> new UsernameNotFoundException("Matricule inconnu"));
        List<GrantedAuthority> auths = new ArrayList<>();
        if (p.getRole() != null) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + p.getRole().getNom()));
            p.getRole()
                .getAutorisations()
                .forEach(a -> auths.add(new SimpleGrantedAuthority(a.getNom())));
        }
        return User.builder()
            .username(p.getMatricule())
            .password(p.getMotDePasse())
            .authorities(auths)
            .disabled(!p.isActif() || p.isDecede())
            .build();
    }
}
