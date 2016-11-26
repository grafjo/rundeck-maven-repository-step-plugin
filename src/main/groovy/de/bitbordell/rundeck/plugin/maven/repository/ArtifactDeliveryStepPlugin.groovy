package de.bitbordell.rundeck.plugin.maven.repository

import com.dtolabs.rundeck.core.common.INodeEntry
import com.dtolabs.rundeck.core.execution.service.FileCopierException
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.core.plugins.configuration.Describable
import com.dtolabs.rundeck.core.plugins.configuration.Description
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder
import com.dtolabs.rundeck.plugins.util.PropertyBuilder
import de.bitbordell.rundeck.plugin.maven.repository.model.ArtifactMetadata
import de.bitbordell.rundeck.plugin.maven.repository.model.PluginConfiguration
import de.bitbordell.rundeck.plugin.maven.repository.service.FileDownloadService
import de.bitbordell.rundeck.plugin.maven.repository.service.HttpRequestService
import de.bitbordell.rundeck.plugin.maven.repository.service.MetadataService
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader

@Plugin(name = "maven-repository-artifact-delivery-step", service = ServiceNameConstants.WorkflowStep)
class ArtifactDeliveryStepPlugin implements StepPlugin, Describable {

    private static final Integer DEBUG = 5

    private MetadataService metadataHandler
    private FileDownloadService fileDownloadHandler

    ArtifactDeliveryStepPlugin() {
        def httpRequestHandler = new HttpRequestService()
        this.metadataHandler = new MetadataService(httpRequestHandler, new MetadataXpp3Reader())
        this.fileDownloadHandler = new FileDownloadService(httpRequestHandler)
    }

    ArtifactDeliveryStepPlugin(MetadataService metadataHandler, FileDownloadService fileDownloadHandler) {
        this.metadataHandler = metadataHandler
        this.fileDownloadHandler = fileDownloadHandler
    }

    @Override
    Description getDescription() {

        return DescriptionBuilder.builder()
                .name('maven-repository-artifact-delivery-step')
                .title('Maven Repository: Artifact Delivery')
                .description('Copy an artifact from a maven repository to a destination on the remote nodes.')
                .property(PropertyBuilder.builder().required(true).string('group').description('Group of the requested artifact').build())
                .property(PropertyBuilder.builder().required(true).string('artifact').description('The artifact').build())
                .property(PropertyBuilder.builder().required(true).string('version').description('Version of the requested artifact').build())
                .property(PropertyBuilder.builder().required(true).string('packaging').description('Packaging of the requested artifact').build())
                .property(PropertyBuilder.builder().required(true).string('repository').description('Repository name where the artifact is stored - e.g. releases or snapshots').build())
                .property(PropertyBuilder.builder().required(true).string('url').description('Url of the maven artifact repository').scope(PropertyScope.Project).build())
                .property(PropertyBuilder.builder().required(false).string('user').description('user of maven repository').scope(PropertyScope.Project).build())
                .property(PropertyBuilder.builder().required(false).string('password').description('password of maven repository').scope(PropertyScope.Project).renderingAsPassword().build())
                .property(PropertyBuilder.builder().required(true).string('destinationPath').description('path on the remote node where the artifact will be copyed to'))
                .property(PropertyBuilder.builder().required(false).string('destinationFilename').description('fileName of the copyed artifact on remote node - leave empty to use original name'))
                .build()
    }

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {

        def LOGGER = context.getLogger()

        LOGGER.log(DEBUG, 'Parsing configuration')
        def pluginConfiguration = PluginConfiguration.fromMap(configuration)

        LOGGER.log(DEBUG, 'Building artifact download link')
        def artifactMetaData = metadataHandler.getArtifactMetaData(pluginConfiguration)

        LOGGER.log(DEBUG, 'Starting file download')
        def artifact = fileDownloadHandler.downloadFile(artifactMetaData.downloadUrl, pluginConfiguration.getLogin())

        def destinationPath = destinationPath(pluginConfiguration, artifactMetaData)

        def copyFileToRemoteNode = { INodeEntry node ->
            try {
                LOGGER.log(DEBUG, "Copying to node=${node} path=${destinationPath} with size=${artifact.size()}")
                context.getFramework()
                        .getExecutionService()
                        .fileCopyFile(context.getExecutionContext(), artifact, node, destinationPath)
                LOGGER.log(DEBUG, "Copying to node=${node} completed")

            } catch (FileCopierException e) {
                throw new StepException("Failed to copy artifact to node=${node.getNodename()}", e, Reason.CopyArtifactFailed)
            }
        }

        LOGGER.log(DEBUG, "Distributing file to ${context.getNodes().nodes.size()} nodes")
        context.getNodes().each(copyFileToRemoteNode)

        LOGGER.log(DEBUG, "Deleting temp file=${artifact.absolutePath}")
        artifact.delete()
    }

    private static String destinationPath(PluginConfiguration pluginConfiguration, ArtifactMetadata artifactMetadata) {

        String fileName = fileName(pluginConfiguration.destinationFilename, artifactMetadata)
        String destinationPath = trimLastSlash(pluginConfiguration.destinationPath)

        return "${destinationPath}/${fileName}"
    }

    private static String fileName(String destionationFilename, ArtifactMetadata artifactMetadata) {
        destionationFilename ? destionationFilename : "${artifactMetadata.name}-${artifactMetadata.version}.${artifactMetadata.packaging}"
    }

    private static String trimLastSlash(String path) {
        path.endsWith('/') ? path.substring(0, path.lastIndexOf('/')) : path
    }

    static enum Reason implements FailureReason {
        CopyArtifactFailed
    }
}
