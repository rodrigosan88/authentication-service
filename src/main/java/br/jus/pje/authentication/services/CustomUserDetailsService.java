package br.jus.pje.authentication.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = null;
		
		if(username.equals("foouser")) {
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(authority);
			user = new User(username, "$2a$10$cyf5NfobcruKQ8XGjUJkEegr9ZWFqaea6vjpXWEaSqTa2xL9wjgQC", authorities);
		}
		return user;
	}

}
