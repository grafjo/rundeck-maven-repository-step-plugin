package de.bitbordell.rundeck.plugin.maven.repository

import org.apache.maven.artifact.repository.metadata.Metadata
import org.apache.maven.artifact.repository.metadata.Snapshot
import org.apache.maven.artifact.repository.metadata.SnapshotVersion
import org.apache.maven.artifact.repository.metadata.Versioning

class TestDomain {

    static InputStream readArtifactMetadataXML() {
        new FileInputStream('src/test/resources/mock/releases/org/acme/bla/maven-metadata.xml')
    }

    static InputStream readSnapshotArtifactMetadataXML() {
        new FileInputStream('src/test/resources/mock/snapshots/org/acme/bla/maven-metadata.xml')
    }

    static InputStream readSnapshotVersionMetadataXML() {
        new FileInputStream('src/test/resources/mock/snapshots/org/acme/bla/0.2.0-SNAPSHOT/maven-metadata.xml')
    }


    static Metadata createSnapshotArtifactMetadata() {
        def versioning = new Versioning(latest: '0.2.0-SNAPSHOT', versions: ['0.1.0-SNAPSHOT', '0.2.0-SNAPSHOT'])
        new Metadata(groupId: 'org.acme', artifactId: 'bla', versioning: versioning)
    }

    static Metadata createSnapshotVersionMetadata() {
        def snapshot = new Snapshot(timestamp: '20160329.230343', buildNumber: 8)
        def snapshotVersions = [
                new SnapshotVersion(extension: 'jar', version: '0.2.0-20160329.230343-8', updated: '20160329230343'),
                new SnapshotVersion(extension: 'pom', version: '0.2.0-20160329.230343-8', updated: '20160329230343'),
                new SnapshotVersion(classifier: 'sources', extension: 'jar', version: '0.2.0-20160329.230343-8', updated: '20160329230343'),
        ]
        def versioning = new Versioning(latest: '0.2.0-SNAPSHOT', snapshot: snapshot, snapshotVersions: snapshotVersions)
        new Metadata(groupId: 'org.acme', artifactId: 'bla', version: '0.2.0-SNAPSHOT', versioning: versioning)
    }

    static Metadata createArtifactMetadata() {
        def versioning = new Versioning(latest: '0.2.0', release: '0.2.0', versions: ['0.1.0', '0.2.0'])
        new Metadata(groupId: 'org.acme', artifactId: 'bla', versioning: versioning)
    }
}
