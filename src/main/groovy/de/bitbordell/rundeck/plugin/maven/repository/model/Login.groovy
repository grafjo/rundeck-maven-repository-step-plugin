package de.bitbordell.rundeck.plugin.maven.repository.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, excludes = 'password')
@EqualsAndHashCode
class Login {
    String user
    String password

    static Login empty() {
        new Login()
    }

    static Login fromMap(Map<String, Object> configuration) {
        new Login(
                user: configuration.get('user'),
                password: configuration.get('password')
        )
    }
}
