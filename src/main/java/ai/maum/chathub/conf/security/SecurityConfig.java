package ai.maum.chathub.conf.security;

import ai.maum.chathub.conf.interceptor.ExecInfoInterceptor;
import ai.maum.chathub.conf.interceptor.ExtApiInterceptor;
import ai.maum.chathub.conf.security.cors.service.CorsDomainService;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.util.CryptoUtil;
import ai.maum.chathub.util.SystemUtil;
import ai.maum.chathub.api.apiuser.service.ApiUserService;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.execinfo.ExecInfoRepository;
import ai.maum.chathub.api.member.service.MemberDetailService;
import ai.maum.chathub.api.role.RoleRepository;
import ai.maum.chathub.api.user.UserRepository;
import ai.maum.chathub.conf.advisor.OriginalTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ServletRequestPathFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {
    @Value("${service.cors.origins}")
    private List<String> origins;

    @Value("${service.cors.methods}")
    private List<String> methods;

    @Value("${service.cors.headers}")
    private List<String> headers;

    @Value("${service.cors.credentials}")
    private boolean credentials;

    private final Environment env;
    private final ApplicationContext context;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ExecInfoRepository execInfoRepository;
    private final ApiUserService apiUserService;
    private final MemberDetailService memberDetailService;
    private final CorsDomainService corsDomainService;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();


//    @Override
//    public void addViewController(ViewControllerRegistry registry) {
//        registry.addRedirectViewController("/socket.io", "ws://localhost:9994");
//    }

    /**
     * 애플리케이션 전반적인 보안 설정을 적용한다.
     * 특정 URL에 대해 특정 권한만 접근 가능하게 하려면 hasRole()을 이용하여 설정한다.
     * 단, API 접근 권한에 대해서는 해당 컨트롤러에 @Access를 이용하여 설정한다.
     * @param http 보안 설정 정보
     * @return SecurityFilterChain
     * @author baekgol
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint authenticationEntryPoint = new AuthenticationEntryPointAdapter();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry = http
                .csrf().disable()
                .cors()
                .configurationSource(configurationSource())
                .and()
                .authorizeRequests()
//                .antMatchers(SecurityMeta.DOCUMENT_LOGIN_URL, "/resource/**", "/websocket/**", "/login/email4Sync, /maum-admin/**, /sso/**, /extapi/**")
                .antMatchers(SecurityMeta.DOCUMENT_LOGIN_URL
                        , "/resource/**"
                        , "/websocket/**"
                        , "/login/email4Sync"
                        , "/extapi/**"
                        , "/chatbotinfo/image/**"
                        , "/file/image/**"
//                        , "/doc/**"
//                        , "/swagger-ui/**"
//                        , "/v3/api-docs/**"
                )
                .permitAll()
                .antMatchers("/favicon.ico", "/error")
                .denyAll();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl docUrlConfig = urlRegistry
                .antMatchers("/doc/**", "/swagger-ui/**", "/v3/api-docs/**");

//        if(Arrays.asList(env.getActiveProfiles()).contains("prod"))
//            urlRegistry = docUrlConfig.denyAll();
//        else
//            urlRegistry = docUrlConfig.permitAll();

        if(Arrays.asList(env.getActiveProfiles()).contains("dev")
                || Arrays.asList(env.getActiveProfiles()).contains("local")
                || Arrays.asList(env.getActiveProfiles()).contains("docker-dev")
        )
            urlRegistry = docUrlConfig.permitAll();
        else
            urlRegistry = docUrlConfig.denyAll();


        return urlRegistry
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(new AccessDeniedHandlerAdapter())
                .and()
                .addFilterBefore(new JwtFilter(context.getBean(RequestMappingHandlerMapping.class),
//                                userRepository,
                                memberDetailService,
                                authenticationEntryPoint),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new APIKeyAuthFilter(apiUserService, memberDetailService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ServletRequestPathFilter(), JwtFilter.class)
                .build();
    }

    /**
     * 사용자 인증에 필요한 AuthenticationManager를 등록한다.
     * @return AuthenticationManager
     * @author baekgol
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        PasswordEncoderAdapter passwordEncoder = (PasswordEncoderAdapter) CryptoUtil.getPasswordEncoder();
        if(!passwordEncoder.isAlive()) SystemUtil.shutdownWithContext(context, BaseException.of(new NoSuchAlgorithmException()));
        provider.setPasswordEncoder(passwordEncoder);

        provider.setUserDetailsService(new UserDetailsServiceAdapter(userRepository, roleRepository));

        return new ProviderManager(provider);
    }

    /**
     * CORS 설정에 필요한 CorsConfigurationSource를 등록한다.
     * @return CorsConfigurationSource
     * @author baekgol
     */
    @Bean
    public CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsDomainService.getAllowedOrigins());
//        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(methods);
        configuration.setAllowedHeaders(headers);
        configuration.setAllowCredentials(credentials);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public void updateCorsConfigurations() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsDomainService.getAllowedOrigins());
        configuration.setAllowedMethods(methods);
        configuration.setAllowedHeaders(headers);
        configuration.setAllowCredentials(credentials);
        corsConfigurationSource.registerCorsConfiguration("/**", configuration);
    }

    /**
     * 자원 접근을 설정한다.
     * @param registry 자원 등록 정보
     * @author baekgol
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resource/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 인터셉터를 설정한다.
     * @param registry 인터셉터 등록 정보
     * @author baekgol
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ExecInfoInterceptor(execInfoRepository))
                .excludePathPatterns("/error", "/resource/**", "/swagger-ui/**", "/v3/api-docs/**", "/doc/**");
        registry.addInterceptor(new ExtApiInterceptor())
                .addPathPatterns("/extapi");
    }

    /**
     * 파라미터 해석을 위한 정보를 설정한다.
     * @param resolvers 리졸버 정보
     * @author baekgol
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new OriginalTokenResolver());
    }

    private String getAuthority(String role) {
        return role.replace("ROLE_", "");
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
////        WebMvcConfigurer.super.addCorsMappings(registry);
//        registry.addMapping("/sso/**")
////                .allowedOrigins("*")
//                .allowedOrigins("null")
//                .allowedMethods("*")
//                .allowedHeaders("*");
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
