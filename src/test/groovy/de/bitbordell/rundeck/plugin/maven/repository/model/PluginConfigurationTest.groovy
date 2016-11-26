package de.bitbordell.rundeck.plugin.maven.repository.model

import spock.lang.Specification

class PluginConfigurationTest extends Specification {

    def 'can read configuration with login'() {
        given:
        def configuration = [
                group          : 'org.acme',
                artifact       : 'bla',
                version        : '0.1.0',
                packaging      : 'jar',
                repository     : 'releases',
                url            : 'http://my.maven.repo/service/local/repositories',
                user           : 'hans',
                password       : 'dampf',
                destinationPath: '/tmp/destination_path'
        ]

        when:
        def result = PluginConfiguration.fromMap(configuration)

        then:
        with(result) {
            gav == new Gav(group: 'org.acme', artifact: 'bla', version: '0.1.0')
            packaging == 'jar'
            repository == 'releases'
            url == 'http://my.maven.repo/service/local/repositories'
            login == new Login(user: 'hans', password: 'dampf')
            destinationPath == '/tmp/destination_path'
        }
    }

    def 'can read configuration without login'() {
        given:
        def configuration = [
                group          : 'org.acme',
                artifact       : 'bla',
                version        : '0.1.0',
                packaging      : 'jar',
                repository     : 'releases',
                url            : 'http://my.maven.repo/service/local/repositories',
                destinationPath: '/tmp/destination_path'
        ]

        when:
        def result = PluginConfiguration.fromMap(configuration)

        then:
        with(result) {
            gav == new Gav(group: 'org.acme', artifact: 'bla', version: '0.1.0')
            packaging == 'jar'
            repository == 'releases'
            url == 'http://my.maven.repo/service/local/repositories'
            login == Login.empty()
            destinationPath == '/tmp/destination_path'
        }
    }

    def 'can read empty input'() {
        given:
        def configuration = Collections.emptyMap()

        when:
        def result = PluginConfiguration.fromMap(configuration)

        then:
        with(result) {
            assert gav == Gav.empty()
            assert packaging == null
            assert repository == null
            assert url == null
            assert login == Login.empty()
            assert destinationPath == null
        }
    }
}
