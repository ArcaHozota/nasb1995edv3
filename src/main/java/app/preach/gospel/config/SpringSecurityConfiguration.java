package app.preach.gospel.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.listener.ProjectUserDetailsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * SpringSecurity配置クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {

	/**
	 * 除外するパス
	 */
	private static final String[] IGNORANCE_PATHS = { "/index.action", "/home/**", "/static/**",
			"/category/login-with-error", "/category/to-system-error", "/students/pre-login", "/hymns/pagination",
			"/hymns/get-info-id", "/hymns/get-records", "/hymns/kanumi-retrieve", "/hymns/common-retrieve",
			"/hymns/score-download" };

	/**
	 * ログインエラー処理
	 */
	@Resource
	private ProjectAuthenticationEntryPoint projectAuthenticationEntryPoint;

	/**
	 * ログインサービス
	 */
	@Resource
	private ProjectUserDetailsService projectUserDetailsService;

	@Bean
	protected AuthenticationManager authenticationManager(final @NotNull AuthenticationManagerBuilder authBuilder) {
		return authBuilder.authenticationProvider(this.daoAuthenticationProvider()).getObject();
	}

	@Bean
	protected DaoAuthenticationProvider daoAuthenticationProvider() {
		final ProjectDaoAuthenticationProvider provider = new ProjectDaoAuthenticationProvider();
		provider.setUserDetailsService(this.projectUserDetailsService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(BCryptVersion.$2A, 7));
		return provider;
	}

	@Bean
	protected SecurityFilterChain filterChain(final @NotNull HttpSecurity httpSecurity) throws Exception {
		httpSecurity
				.authorizeHttpRequests(authorize -> authorize.requestMatchers(IGNORANCE_PATHS).permitAll()
						.requestMatchers(
								ProjectURLConstants.URL_HYMNS_NAMESPACE.concat(ProjectURLConstants.URL_TO_EDITION))
						.hasAuthority("hymns%edition")
						.requestMatchers(
								ProjectURLConstants.URL_HYMNS_NAMESPACE.concat(ProjectURLConstants.URL_CHECK_DELETE))
						.hasAuthority("hymns%deletion")
						.requestMatchers(
								ProjectURLConstants.URL_STUDENTS_NAMESPACE.concat(ProjectURLConstants.URL_TO_EDITION))
						.hasAuthority("students%retrievEdition").anyRequest().authenticated())
				.csrf(csrf -> csrf.ignoringRequestMatchers(ProjectURLConstants.URL_STATIC_RESOURCE)
						.csrfTokenRepository(new CookieCsrfTokenRepository()))
				.exceptionHandling(handling -> {
					handling.authenticationEntryPoint(this.projectAuthenticationEntryPoint);
					handling.accessDeniedHandler((request, response, accessDeniedException) -> {
						response.sendError(HttpStatus.FORBIDDEN.value(),
								ProjectConstants.MESSAGE_SPRINGSECURITY_REQUIRED_AUTH);
						log.warn(ProjectConstants.MESSAGE_SPRINGSECURITY_REQUIRED_AUTH);
					});
				})
				.formLogin(formLogin -> formLogin
						.loginPage(ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_TO_LOGIN))
						.loginProcessingUrl(
								ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_LOGIN))
						.defaultSuccessUrl(ProjectURLConstants.URL_CATEGORY_NAMESPACE
								.concat(ProjectURLConstants.URL_TO_MAINMENU_WITH_LOGIN))
						.permitAll().usernameParameter("loginAcct").passwordParameter("userPswd"))
				.logout(logout -> logout
						.logoutUrl(ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_LOGOUT))
						.logoutSuccessUrl(
								ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_TO_LOGIN)));
		log.info(ProjectConstants.MESSAGE_SPRING_SECURITY);
		return httpSecurity.build();
	}

}
