package de.bitbordell.rundeck.plugin.maven.repository.service

import de.bitbordell.rundeck.plugin.maven.repository.TestDomain
import de.bitbordell.rundeck.plugin.maven.repository.model.PluginConfiguration
import de.bitbordell.rundeck.plugin.maven.repository.model.Gav
import de.bitbordell.rundeck.plugin.maven.repository.model.Login
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader
import spock.lang.Specification

class MetadataServiceTest extends Specification {

    HttpRequestService httpRequestHandlerMock
    MetadataXpp3Reader xmlMetadatareaderMock
    TestDomain testDomain
    MetadataService sut


    def setup() {

        testDomain = new TestDomain()
        httpRequestHandlerMock = Mock(HttpRequestService)
        xmlMetadatareaderMock = Mock(MetadataXpp3Reader)

        sut = new MetadataService(httpRequestHandlerMock, xmlMetadatareaderMock)
    }

    def 'creates artifact download link for release: LATEST'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'org.acme', artifact: 'bla', version: 'LATEST'),
                packaging: 'jar',
                repository: 'releases',
                url: 'http://my.maven.repo/service/local/repositories',
                login: new Login(user: 'hans', password: 'dampf')
        )

        mockArtifactMetadataRequest(configuration.login)

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'bla'
            packaging == 'jar'
            version == '0.2.0'
            downloadUrl == 'http://my.maven.repo/service/local/repositories/releases/org/acme/bla/0.2.0/bla-0.2.0.jar'
        }
    }

    def 'creates artifact download link for release: RELEASE'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'org.acme', artifact: 'bla', version: 'RELEASE'),
                packaging: 'jar',
                repository: 'releases',
                url: 'http://my.maven.repo/service/local/repositories',
                login: new Login(user: 'hans', password: 'dampf')
        )

        mockArtifactMetadataRequest(configuration.login)

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'bla'
            packaging == 'jar'
            version == '0.2.0'
            downloadUrl == 'http://my.maven.repo/service/local/repositories/releases/org/acme/bla/0.2.0/bla-0.2.0.jar'
        }
    }

    def 'creates artifact download link for release: 0.2.0'() {

        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'org.acme', artifact: 'bla', version: '0.2.0'),
                packaging: 'jar',
                repository: 'releases',
                url: 'http://my.maven.repo/service/local/repositories',
                login: new Login(user: 'hans', password: 'dampf')
        )

        mockArtifactMetadataRequest(configuration.login)

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'bla'
            packaging == 'jar'
            version == '0.2.0'
            downloadUrl == 'http://my.maven.repo/service/local/repositories/releases/org/acme/bla/0.2.0/bla-0.2.0.jar'
        }
    }

    def 'creates artifact download link for snapshot: LATEST'() {
        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'org.acme', artifact: 'bla', version: 'LATEST'),
                packaging: 'jar',
                repository: 'snapshots',
                url: 'http://my.maven.repo/service/local/repositories',
                login: new Login(user: 'hans', password: 'dampf')
        )

        mockSnapshotArtifactMetadataRequest(configuration.login)
        mockSnapshotVersionMetadataRequest(configuration.login)

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'bla'
            packaging == 'jar'
            version == '0.2.0-SNAPSHOT'
            downloadUrl == 'http://my.maven.repo/service/local/repositories/snapshots/org/acme/bla/0.2.0-SNAPSHOT/bla-0.2.0-20160329.230343-8.jar'
        }
    }

    def 'creates artifact download link for snapshot: 0.2.0-SNAPSHOT'() {
        given:
        def configuration = new PluginConfiguration(
                gav: new Gav(group: 'org.acme', artifact: 'bla', version: '0.2.0-SNAPSHOT'),
                packaging: 'jar',
                repository: 'snapshots',
                url: 'http://my.maven.repo/service/local/repositories',
                login: new Login(user: 'hans', password: 'dampf')
        )

        mockSnapshotArtifactMetadataRequest(configuration.login)
        mockSnapshotVersionMetadataRequest(configuration.login)

        when:
        def result = sut.getArtifactMetaData(configuration)

        then:
        with(result) {
            name == 'bla'
            packaging == 'jar'
            version == '0.2.0-SNAPSHOT'
            downloadUrl == 'http://my.maven.repo/service/local/repositories/snapshots/org/acme/bla/0.2.0-SNAPSHOT/bla-0.2.0-20160329.230343-8.jar'
        }
    }

    private void mockArtifactMetadataRequest(Login login) {
        def metadataXML = testDomain.readArtifactMetadataXML()
        httpRequestHandlerMock.handleXMLRequest('http://my.maven.repo/service/local/repositories/releases/org/acme/bla/maven-metadata.xml', login, _) >> { arguments ->
            def successHandler = arguments[2]
            def response = [contentType: 'application/xml', entity: [contentLength: '5000']]
            successHandler(response, metadataXML)
        }
        xmlMetadatareaderMock.read(metadataXML) >> testDomain.createArtifactMetadata()
    }

    private void mockSnapshotVersionMetadataRequest(Login login) {
        def snapshotVersionMetadataXML = testDomain.readSnapshotVersionMetadataXML();
        httpRequestHandlerMock.handleXMLRequest('http://my.maven.repo/service/local/repositories/snapshots/org/acme/bla/0.2.0-SNAPSHOT/maven-metadata.xml', login, _) >> { arguments ->
            def successHandler = arguments[2]
            def response = [contentType: 'application/xml', entity: [contentLength: '6200']]
            successHandler(response, snapshotVersionMetadataXML)
        }
        xmlMetadatareaderMock.read(snapshotVersionMetadataXML) >> testDomain.createSnapshotVersionMetadata()
    }

    private void mockSnapshotArtifactMetadataRequest(Login login) {
        def snapshotArtifactMetadataXML = testDomain.readSnapshotArtifactMetadataXML()
        httpRequestHandlerMock.handleXMLRequest('http://my.maven.repo/service/local/repositories/snapshots/org/acme/bla/maven-metadata.xml', login, _) >> { arguments ->
            def successHandler = arguments[2]
            def response = [contentType: 'application/xml', entity: [contentLength: '5200']]
            successHandler(response, snapshotArtifactMetadataXML)
        }
        xmlMetadatareaderMock.read(snapshotArtifactMetadataXML) >> testDomain.createSnapshotArtifactMetadata()
    }
}
