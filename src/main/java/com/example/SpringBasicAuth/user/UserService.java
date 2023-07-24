package com.example.SpringBasicAuth.user;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SpringBasicAuth.system.exception.ObjectNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    public User findById(Integer id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("user", id));
    }

    public User save(User newUser) {
        newUser.setPassword(this.passwordEncoder.encode(newUser.getPassword()));
        return this.userRepository.save(newUser);
    }

    /**
     * We are not using this update to change user password.
    */
    public User update(Integer id, User update) {
        User oldUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("user", id));
        oldUser.setUsername(update.getUsername());
        oldUser.setEnabled(update.isEnabled());
        oldUser.setRoles(update.getRoles());
        return this.userRepository.save(oldUser);
    }

    public User updatePassword(UserPrincipal user, String newPassword) {
        User userInfo = user.getUser();
        User oldUser = this.userRepository.findById(userInfo.getId()).orElseThrow(() -> new ObjectNotFoundException("user", userInfo.getId()));
        oldUser.setPassword(this.passwordEncoder.encode(newPassword));
        return this.userRepository.save(oldUser);
    }

    public boolean oldPasswordIsValid(UserPrincipal user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public void delete(Integer id) {
        this.userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("user", id));
        this.userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username) 
                .map(user -> new UserPrincipal(user)) 
                .orElseThrow(() -> new UsernameNotFoundException("username " + username + " is not found."));
    }

}
