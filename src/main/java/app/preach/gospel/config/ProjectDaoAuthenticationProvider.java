package app.preach.gospel.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import app.preach.gospel.common.ProjectConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ログイン認証サービス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ProjectDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails,
            final @NotNull UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            this.logger.warn("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(
                    this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                            ProjectConstants.MESSAGE_SPRINGSECURITY_REQUIRED_AUTH));
        }
        final String presentedPassword = authentication.getCredentials().toString();
        if (!this.getPasswordEncoder().matches(presentedPassword, userDetails.getPassword())) {
            this.logger.warn("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(
                    this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                            ProjectConstants.MESSAGE_SPRINGSECURITY_LOGINERROR4));
        }
    }

}
