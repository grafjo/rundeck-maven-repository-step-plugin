package de.bitbordell.rundeck.plugin.maven.repository.model

import spock.lang.Specification


class GavTest extends Specification {

    def 'Can construct a Gav object from configuration map'() {
        given:
        def configuration = [
                group   : 'org.acme',
                artifact: 'bla',
                version : '0.1.0'
        ]

        when:
        def result = Gav.fromMap(configuration)

        then:
        with(result) {
            group == 'org.acme'
            artifact == 'bla'
            version == '0.1.0'
        }
    }

    def 'Can construct Gav object from empty configuration map'() {
        given:
        def configuration = Collections.emptyMap()

        when:
        def result = Gav.fromMap(configuration)

        then:
        with(result) {
            group == null
            artifact == null
            version == null
        }
    }
}
