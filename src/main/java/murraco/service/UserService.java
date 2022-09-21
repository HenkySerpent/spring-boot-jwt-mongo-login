package murraco.service;

import lombok.RequiredArgsConstructor;
import murraco.exception.CustomException;
import murraco.model.AppUser;
import murraco.repository.UserRepository;
import murraco.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
            if (!optionalAppUser.isPresent()) {
                throw new UsernameNotFoundException("User '" + username + "' not found");
            }
            AppUser appUser = optionalAppUser.get();
            return jwtTokenProvider.createToken(username, appUser.getAppUserRoles());

        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(AppUser appUser) {
        if (!userRepository.existsByUsername(appUser.getUsername())) {
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
            userRepository.save(appUser);
            return jwtTokenProvider.createToken(appUser.getUsername(), appUser.getAppUserRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    public AppUser search(String username) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);

        if (!optionalAppUser.isPresent()) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }
        AppUser appUser = optionalAppUser.get();
        return appUser;
    }

    public AppUser whoami(HttpServletRequest req) {
        Optional<AppUser> optionalAppUser =userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        if (!optionalAppUser.isPresent()) {
            throw new UsernameNotFoundException("User  not found");
        }
        return optionalAppUser.get();
    }

    public String refresh(String username) {
        Optional<AppUser> optionalAppUser =userRepository.findByUsername(username);
        if (!optionalAppUser.isPresent()) {
            throw new UsernameNotFoundException("User  not found");
        }
        return jwtTokenProvider.createToken(username,optionalAppUser.get().getAppUserRoles());
    }

}
