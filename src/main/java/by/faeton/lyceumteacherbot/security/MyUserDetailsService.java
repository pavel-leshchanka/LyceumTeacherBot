package by.faeton.lyceumteacherbot.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private static final TelegramUser DEFAULT_USER = TelegramUser.builder()
        .userLevel(UserLevel.USER)
        .telegramUserId(0L)
        .build();
    private static final MyUserDetails MY_DEFAULT_USER_DETAILS = new MyUserDetails(DEFAULT_USER);
    private final TelegramUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByTelegramId(Long.parseLong(username))
            .map(MyUserDetails::new)
            .orElse(MY_DEFAULT_USER_DETAILS);
    }
}
