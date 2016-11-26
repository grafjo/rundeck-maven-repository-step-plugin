package de.bitbordell.rundeck.plugin.maven.repository.model

import spock.lang.Specification


class LoginTest extends Specification {

    def 'Can construct a Login object from configuration map'() {
        given:

        def configuration = [
                user    : 'hans',
                password: 'dampf'
        ]

        when:
        def result = Login.fromMap(configuration)

        then:
        with(result) {
            user == 'hans'
            password == 'dampf'
        }
    }

    def 'Can construct a Login object from empty configuration map'() {
        given:

        def configuration = Collections.emptyMap()

        when:
        def result = Login.fromMap(configuration)

        then:
        with(result) {
            user == null
            password == null
        }
    }
}
