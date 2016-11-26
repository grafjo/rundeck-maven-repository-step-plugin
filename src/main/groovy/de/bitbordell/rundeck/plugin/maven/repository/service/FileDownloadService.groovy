package de.bitbordell.rundeck.plugin.maven.repository.service

import de.bitbordell.rundeck.plugin.maven.repository.model.Login

class FileDownloadService {

    private HttpRequestService httpRequestHandler

    FileDownloadService(HttpRequestService httpRequestHandler) {
        this.httpRequestHandler = httpRequestHandler
    }

    File downloadFile(mavenUrl, Login login) {

        def tempFile = File.createTempFile("maven-artifact", ".tmp")

        def downloadArtifact = { resp, responseStream ->
            def outputStream = new FileOutputStream(tempFile)
            outputStream << responseStream
            outputStream.close()
        }

        httpRequestHandler.handleFileRequest(mavenUrl, login, downloadArtifact)

        return tempFile
    }

}
