package br.edu.unifacisa.adocao;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/auth")
class AuthController {
    private final Users users;
    private final PasswordEncoder encoder;
    private final org.springframework.core.env.Environment environment;
    AuthController(Users users,PasswordEncoder encoder,org.springframework.core.env.Environment environment) { this.users=users; this.encoder=encoder; this.environment=environment; }

    @GetMapping("/info") Map<String,Boolean> info() { return Map.of("demo",environment.matchesProfiles("demo")); }

    @GetMapping("/csrf") Map<String,String> csrf(CsrfToken token) {
        return Map.of("token",token.getToken(),"headerName",token.getHeaderName());
    }
    @GetMapping("/me") UserView me(Authentication auth) { return UserView.of(current(auth)); }
    AppUser current(Authentication auth) {
        if(auth==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Entre na sua conta.");
        return users.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
    UserView register(@Valid @RequestBody RegisterInput input,HttpServletRequest req,HttpServletResponse res) {
        String email=input.email().trim().toLowerCase(Locale.ROOT);
        if(users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"E-mail já cadastrado.");
        if(input.password().getBytes(StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A senha deve ter até 72 bytes em UTF-8.");
        AppUser u=new AppUser(); u.name=input.name().trim(); u.email=email;
        u.passwordHash=encoder.encode(input.password()); u.role=input.role();
        users.saveAndFlush(u); signIn(u,req,res); return UserView.of(u);
    }
    @PostMapping("/login") UserView login(@Valid @RequestBody LoginInput input,HttpServletRequest req,HttpServletResponse res) {
        AppUser u=users.findByEmail(input.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        if(u==null || input.password().getBytes(StandardCharsets.UTF_8).length>72 || !encoder.matches(input.password(),u.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"E-mail ou senha incorretos.");
        signIn(u,req,res); return UserView.of(u);
    }
    private void signIn(AppUser u,HttpServletRequest req,HttpServletResponse res) {
        HttpSession old=req.getSession(false); if(old!=null) old.invalidate();
        var context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u.email,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.role))));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context,req,res);
    }
}
