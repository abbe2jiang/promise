
package org.dj.we.config;

import org.dj.we.domain.Author;
import org.dj.we.service.author.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;


@Service
@Primary
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    AuthorService authorService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Author author = authorService.getAuthorByUsername(s);
        if (author == null) {
            throw new UsernameNotFoundException("username=" + s + " not exists.");
        }

        String password = author.getPassword();
        return new org.springframework.security.core.userdetails.User(s, password, true, true, true, true, getSimpleGrantedAuthorities());
    }

    private Collection<SimpleGrantedAuthority> getSimpleGrantedAuthorities() {

        Collection<SimpleGrantedAuthority> collection = new HashSet<>();

        collection.add(new SimpleGrantedAuthority("ADMIN"));

        return collection;
    }


}

