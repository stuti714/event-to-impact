package com.eventtoimpact.india.service;

import com.eventtoimpact.india.model.UserAccount;
import com.eventtoimpact.india.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {
    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AccountService service = new AccountService(repository, passwordEncoder);

    @Test
    void registrationNormalizesUsernameAndStoresOnlyPasswordHash() {
        when(repository.existsByUsernameIgnoreCase("stuti_aiml")).thenReturn(false);
        when(passwordEncoder.encode("secure123")).thenReturn("bcrypt-hash");
        when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount account = service.register("  Stuti_AIML  ", "secure123");

        assertThat(account.getUsername()).isEqualTo("stuti_aiml");
        assertThat(account.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(account.getPasswordHash()).doesNotContain("secure123");
        assertThat(account.getCreatedAt()).isNotNull();
        verify(repository).save(account);
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(repository.existsByUsernameIgnoreCase("stuti")).thenReturn(true);

        assertThatThrownBy(() -> service.register("stuti", "secure123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void weakCredentialsAreRejected() {
        assertThatThrownBy(() -> service.register("ab", "123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
