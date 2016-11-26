package de.bitbordell.rundeck.plugin.maven.repository.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true)
@EqualsAndHashCode
class Gav {
    String group
    String artifact
    String version

    static Gav empty() {
        new Gav()
    }

    static Gav fromMap(Map<String, Object> configuration) {
        new Gav(
                group: configuration.get('group'),
                artifact: configuration.get('artifact'),
                version: configuration.get('version')
        )
    }
}
