package de.bitbordell.rundeck.plugin.maven.repository.service

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import de.bitbordell.rundeck.plugin.maven.repository.model.Login
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method


class HttpRequestService {

    static enum Reason implements FailureReason {
        NotFound, UnexepectedFailure
    }

    def handleXMLRequest(String url, Login login, Closure successHandler) {

        println "Doing xml get request for URL: ${url}"

        def http = new HTTPBuilder(url)

        handleLogin(login, http)

        // We're always parsing xml als plain text, so a reader is used for MetadataXpp3Reader.read()
        http.parser.'application/xml' = http.parser.'text/plain'

        http.request(Method.GET, ContentType.XML) { req ->

            response.success = successHandler

            response.failure = { resp ->
                throw new StepException("Status code: ${resp.status} -> URL: ${url}", Reason.UnexepectedFailure)
            }

            response.'404' = { resp ->
                throw new StepException("Not found -> URL:${url}", Reason.NotFound)
            }
        }
    }

    def handleFileRequest(String url, Login login, Closure successHandler) {

        println "Doing file get request for URL: ${url}"

        def http = new HTTPBuilder(url)

        handleLogin(login, http)

        http.request(Method.GET, ContentType.ANY) { req ->

            response.success = successHandler

            response.failure = { resp ->
                throw new StepException("Status code: ${resp.status} -> URL: ${url}", Reason.UnexepectedFailure)
            }

            response.'404' = { resp ->
                throw new StepException("Not found -> URL:${url}", Reason.NotFound)
            }
        }
    }

    private static void handleLogin(login, http) {
        if (login.user || login.password) {
            println 'Basic auth: enabled'
            http.auth.basic login.user, login.password
        } else {
            println 'Basic auth: disabled'
        }
    }
}
