package br.jus.pje.authentication.configs;

import java.security.KeyPair;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import br.jus.pje.authentication.model.SecurityProperties;

@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(SecurityProperties.class)
public class AuthenticationServiceConfig extends AuthorizationServerConfigurerAdapter {

//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
// 
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
//        oauthServer.tokenKeyAccess("permitAll()")
//          .checkTokenAccess("isAuthenticated()");
//    }
// 
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//          .withClient("fooclient")
//          .secret(passwordEncoder.encode("secret"))
//          .authorizedGrantTypes("authorization_code")
//          .scopes("user_info")
//          .autoApprove(true) 
//          .redirectUris("http://www.google.com"); 
//    }	

	private final DataSource dataSource;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final SecurityProperties securityProperties;
	private final UserDetailsService userDetailsService;

	private JwtAccessTokenConverter jwtAccessTokenConverter;
	private TokenStore tokenStore;

	@Autowired
	public AuthenticationServiceConfig(DataSource dataSource, 
										PasswordEncoder passwordEncoder,
                                        AuthenticationManager authenticationManager,
                                        SecurityProperties securityProperties,
                                        UserDetailsService userDetailsService) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityProperties = securityProperties;
        this.userDetailsService = userDetailsService;
    }

	@Bean
	public TokenStore tokenStore() {
		if (tokenStore == null) {
			tokenStore = new JwtTokenStore(jwtAccessTokenConverter());
		}
		return tokenStore;
	}

	@Bean
	public DefaultTokenServices tokenServices(final TokenStore tokenStore,
			final ClientDetailsService clientDetailsService) {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setTokenStore(tokenStore);
		tokenServices.setClientDetailsService(clientDetailsService);
		tokenServices.setAuthenticationManager(this.authenticationManager);
		return tokenServices;
	}

	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		if (jwtAccessTokenConverter != null) {
			return jwtAccessTokenConverter;
		}

		SecurityProperties.JwtProperties jwtProperties = securityProperties.getJwt();
		KeyPair keyPair = keyPair(jwtProperties, keyStoreKeyFactory(jwtProperties));

		jwtAccessTokenConverter = new JwtAccessTokenConverter();
		jwtAccessTokenConverter.setKeyPair(keyPair);
		return jwtAccessTokenConverter;
	}

	@Override
	public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(this.dataSource);
	}

	@Override
	public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.authenticationManager(this.authenticationManager).accessTokenConverter(jwtAccessTokenConverter())
				.userDetailsService(this.userDetailsService).tokenStore(tokenStore());
	}

	@Override
	public void configure(final AuthorizationServerSecurityConfigurer oauthServer) {
		oauthServer.passwordEncoder(this.passwordEncoder).tokenKeyAccess("permitAll()")
				.checkTokenAccess("isAuthenticated()");
	}

	private KeyPair keyPair(SecurityProperties.JwtProperties jwtProperties, KeyStoreKeyFactory keyStoreKeyFactory) {
		return keyStoreKeyFactory.getKeyPair(jwtProperties.getKeyPairAlias(),
				jwtProperties.getKeyPairPassword().toCharArray());
	}

	private KeyStoreKeyFactory keyStoreKeyFactory(SecurityProperties.JwtProperties jwtProperties) {
		return new KeyStoreKeyFactory(jwtProperties.getKeyStore(), jwtProperties.getKeyStorePassword().toCharArray());
	}

}
