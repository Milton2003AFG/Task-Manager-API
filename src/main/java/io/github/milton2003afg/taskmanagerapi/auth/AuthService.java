package io.github.milton2003afg.taskmanagerapi.auth;

import io.github.milton2003afg.taskmanagerapi.exception.InvalidCredentialsException;
import io.github.milton2003afg.taskmanagerapi.exception.UserAlreadyExistsException;
import io.github.milton2003afg.taskmanagerapi.security.JwtService;
import io.github.milton2003afg.taskmanagerapi.user.User;
import io.github.milton2003afg.taskmanagerapi.user.UserRepository;
import io.github.milton2003afg.taskmanagerapi.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService (UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(UserRegisterRequest request){
        String email = request.email().trim().toLowerCase();
        boolean userExist = userRepository.existsByEmail(email);
        if (userExist){
            throw new UserAlreadyExistsException("The user with email: " + email + " already exists");
        }

        String passwordHashed = passwordEncoder.encode(request.password());
        User user = new User(email, passwordHashed);
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public TokenResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }
}
