package de.bitbordell.rundeck.plugin.maven.repository.service

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import de.bitbordell.rundeck.plugin.maven.repository.model.ArtifactMetadata
import de.bitbordell.rundeck.plugin.maven.repository.model.PluginConfiguration
import de.bitbordell.rundeck.plugin.maven.repository.model.Login
import org.apache.maven.artifact.repository.metadata.Metadata
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader

class MetadataService {

    private HttpRequestService httpRequestHandler
    private MetadataXpp3Reader metadataReader

    MetadataService(HttpRequestService httpRequestHandler, MetadataXpp3Reader metadataReader) {
        this.httpRequestHandler = httpRequestHandler
        this.metadataReader = metadataReader
    }

    ArtifactMetadata getArtifactMetaData(PluginConfiguration configuration) {

        def baseUrl = baseUrl(configuration)
        def metadataUrl = artifactMavenMetadataUrl(baseUrl)
        def metadata = requestMetaData(metadataUrl, configuration.login)
        def selectedVersion = getSelectedVersion(configuration, metadata)

        if (!metadata.versioning.versions.contains(selectedVersion)) {
            throw new StepException("Requested artifact version=${selectedVersion} doesn't exists in repository=${configuration.repository} - going to die!", Reason.VersionNotFound)
        }

        def artifactMetaData = ArtifactMetadata.builder()
                .name(configuration.gav.artifact)
                .packaging(configuration.packaging)


        if (selectedVersion.endsWith('SNAPSHOT') || metadata.versioning.versions.any({ it.endsWith("SNAPSHOT") })) {
            println "detected request for version=${selectedVersion} and repositoryType=snapshots"

            def snapshotVersion = selectedVersion
            def snapshotVersionMetadata = requestMetaData(snapshotMavenMetadataUrl(baseUrl, snapshotVersion), configuration.login)
            artifactMetaData.version(snapshotVersion)
                    .downloadUrl(snapshotArtifactUrl(configuration, baseUrl, snapshotVersion, snapshotVersionMetadata))

        } else {
            println "detected request for version=${selectedVersion} and repositoryType=releases"

            def releaseVersion = selectedVersion
            artifactMetaData.version(releaseVersion)
                    .downloadUrl(releaseArtifactUrl(configuration, baseUrl, releaseVersion))

        }

        artifactMetaData.build()
    }

    private static String baseUrl(PluginConfiguration configuration) {
        def groupAsPath = configuration.gav.group.replace('.', '/')
        "${configuration.url}/${configuration.repository}/${groupAsPath}/${configuration.gav.artifact}"
    }

    private static String getSelectedVersion(PluginConfiguration configuration, Metadata metadata) {

        def version = configuration.gav.version

        if (version == 'LATEST') {
            metadata.versioning.latest
        } else if (version == 'RELEASE') {
            metadata.versioning.release
        } else {
            version
        }
    }

    private Metadata requestMetaData(String metadataUrl, Login login) {

        def metadata

        def successHandler = { resp, responseStream ->
            println("Content-type: ${resp.contentType}, Content-length: ${resp.entity.contentLength}");
            metadata = metadataReader.read(responseStream)
        }

        httpRequestHandler.handleXMLRequest(metadataUrl, login, successHandler)

        return metadata
    }

    private static String releaseArtifactUrl(PluginConfiguration configuration, String baseUrl, String releaseVersion) {

        "${baseUrl}/${releaseVersion}/${configuration.gav.artifact}-${releaseVersion}.${configuration.packaging}"
    }

    private
    static String snapshotArtifactUrl(PluginConfiguration configuration, String baseUrl, String snapshotVersion, Metadata metadata) {

        def timestamp = metadata.versioning.snapshot.timestamp
        def buildNumber = metadata.versioning.snapshot.buildNumber

        "${baseUrl}/${snapshotVersion}/${configuration.gav.artifact}-${snapshotVersion.replace('-SNAPSHOT', '')}-${timestamp}-${buildNumber}.${configuration.packaging}"
    }

    private static String snapshotMavenMetadataUrl(String baseUrl, String snapshotVersion) {
        "${baseUrl}/${snapshotVersion}/maven-metadata.xml"
    }

    private static String artifactMavenMetadataUrl(String baseUrl) {
        "${baseUrl}/maven-metadata.xml"
    }

    static enum Reason implements FailureReason {
        VersionNotFound
    }

}
