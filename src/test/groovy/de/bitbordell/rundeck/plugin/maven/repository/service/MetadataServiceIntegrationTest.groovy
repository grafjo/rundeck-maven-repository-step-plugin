package de.bitbordell.rundeck.plugin.maven.repository.service

import de.bitbordell.rundeck.plugin.maven.repository.model.PluginConfiguration
import de.bitbordell.rundeck.plugin.maven.repository.model.Gav
import de.bitbordell.rundeck.plugin.maven.repository.model.Login
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader
import spock.lang.Specification

class MetadataServiceIntegrationTest extends Specification {

    MetadataService sut

    def setup() {
        sut = new MetadataService(new HttpRequestService(), new MetadataXpp3Reader())
    }

    def 'creates artifact download link for maven central'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'junit', artifact: 'junit', version: '4.12'),
                packaging: 'jar',
                repository: 'maven2',
                url: 'https://repo1.maven.org',
                login: Login.empty()
        )

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'junit'
            packaging == 'jar'
            version == '4.12'
            downloadUrl == 'https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar'
        }
    }

    def 'creates artifact download link for nexus release repository'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'junit', artifact: 'junit', version: '4.12'),
                packaging: 'jar',
                repository: 'releases/content',
                url: 'https://oss.sonatype.org/service/local/repositories',
                login: Login.empty()
        )

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'junit'
            packaging == 'jar'
            version == '4.12'
            downloadUrl == 'https://oss.sonatype.org/service/local/repositories/releases/content/junit/junit/4.12/junit-4.12.jar'
        }
    }

    def 'creates artifact download link for nexus snapshot repository'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'junit', artifact: 'junit', version: '5.0-SNAPSHOT'),
                packaging: 'jar',
                repository: 'snapshots/content',
                url: 'https://oss.sonatype.org/service/local/repositories',
                login: Login.empty()
        )

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'junit'
            packaging == 'jar'
            version == '5.0-SNAPSHOT'
            downloadUrl == 'https://oss.sonatype.org/service/local/repositories/snapshots/content/junit/junit/5.0-SNAPSHOT/junit-5.0-20150111.191949-1.jar'
        }
    }

}
