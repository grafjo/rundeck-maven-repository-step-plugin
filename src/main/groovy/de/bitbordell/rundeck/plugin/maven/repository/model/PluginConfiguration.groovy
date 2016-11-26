package de.bitbordell.rundeck.plugin.maven.repository.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(includeNames = true)
@EqualsAndHashCode
class PluginConfiguration {

    Gav gav
    String packaging
    String repository
    String url
    Login login
    String destinationPath
    String destinationFilename

    static PluginConfiguration fromMap(Map<String, Object> configuration) {

        def currentConfiguration = new PluginConfiguration(
                gav: Gav.fromMap(configuration),
                packaging: configuration.get('packaging'),
                repository: configuration.get('repository'),
                login: Login.fromMap(configuration),
                url: configuration.get('url'),
                destinationPath: configuration.get('destinationPath'),
                destinationFilename: configuration.get('destinationFilename')
        )

        println "Current configuration=${currentConfiguration}"
        return currentConfiguration
    }

}
