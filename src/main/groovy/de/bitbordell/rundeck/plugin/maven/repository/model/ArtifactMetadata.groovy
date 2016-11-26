package de.bitbordell.rundeck.plugin.maven.repository.model

import groovy.transform.builder.Builder

@Builder
class ArtifactMetadata {
    String downloadUrl
    String packaging
    String name
    String version
}
