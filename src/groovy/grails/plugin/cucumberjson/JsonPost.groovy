package grails.plugin.cucumberjson

import grails.util.Environment
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST

/**
 * User: gcurrey
 * Date: 19/06/12
 * Time: 11:29 AM
 */
class JsonPost extends Closure {

    static final CONFIG_NAME = "Config.groovy"
    static final CONFIG_PATH = ["grails-app", "conf", CONFIG_NAME].join(File.separator)

    private final Binding binding;
    private def configObject

    JsonPost(Object theTarget, Binding theBinding) {
        super(theTarget)
        binding = theBinding;

        configObject = new ConfigSlurper(Environment.TEST.name).parse(new File(CONFIG_PATH).toURL())
    }

    protected doCall(Object[] args) {

        def urlBase = configObject.grails.serverURL ?: System.getProperty("grails.testing.functional.baseUrl")

        if (!urlBase) {
            throw new Exception("Unable to find the url grails is running on.  I tried config->grails.serverURL and -Dgrails.testing.functional.baseURL.  Please set one of these variables")
        } else {

            def http = new HTTPBuilder("${urlBase}${args[0]}")
            def jsonRequest = binding.getVariable("jsonRequest");
            def theBinding = binding;

            http.request(POST, ContentType.JSON) { req ->
                body = jsonRequest

                response.success = { resp, json ->
                    def statusCode = resp.statusLine.statusCode
                    theBinding.setVariable("responseCode", statusCode)
                    theBinding.setVariable("jsonResponse", json)
                }
            }
        }
    }
}