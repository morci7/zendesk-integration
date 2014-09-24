package zendesk

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import grails.converters.JSON
import groovyx.net.http.RESTClient

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZendeskAPI {

    static Logger log = LoggerFactory.getLogger(ZendeskAPI)

    def client
    def user
    def password

    static final int STATUS_NEW = 0
    static final int STATUS_OPEN = 1
    static final int STATUS_PENDING = 2
    static final int STATUS_SOLVED = 3
    static final int STATUS_CLOSED = 4

    ZendeskAPI(baseURL) {
        client = new RESTClient(baseURL)
    }

    def postBinaryFile(String url, String type, InputStream content, args = null) {
        client.auth.basic user, password
        def res = client.request(POST) {
            uri.path = url
            requestContentType = type
            contentType = JSON
            body = content
            if (args?.query) {
                uri.query = args.query
            }
            if (args?.headers) {
                for (entry in args.headers) {
                    headers."${entry.key}" = entry.value
                }
            }

            response.'401' = { resp ->
                throw new AuthFailureException("Cannot make binary post request")
            }

            response.'201' = { resp ->
                log.debug "Zendesk API post binary file to $url returned 201 Created"
                return resp
            }
            response.'200' = { resp, json ->
                log.debug "Zendesk API post binary file to $url returned 200 OK"
                return json
            }
        }
        return res
    }

    def postRequest(url, payload, args = null) {
        client.auth.basic user, password
        def res = client.request(POST) {
            uri.path = url
            contentType = '*/*'
            requestContentType = JSON
            body = payload
            if (args?.query) {
                uri.query = args.query
            }
            if (args?.headers) {
                for (entry in args.headers) {
                    headers."${entry.key}" = entry.value
                }
            }

            response.'401' = { resp ->
                throw new AuthFailureException("Cannot make post request")
            }

            response.'201' = { resp ->
                log.debug "Zendesk API post to $url returned 201 Created"
                return resp
            }
        }
        return res
    }

    def getRequest(url, args = null) {
        client.auth.basic user, password
        def res = client.request(GET, JSON) {
            uri.path = url
            contentType = '*/*'
//            requestContentType = JSON
            if (args?.query) {
                uri.query = args.query
            }
            if (args?.headers) {
                for (entry in args.headers) {
                    headers."${entry.key}" = entry.value
                }
            }

            response.'401' = { resp ->
                throw new AuthFailureException("Cannot make get request")
            }

            response.'200' = { resp, json ->
                log.debug "Zendesk API get to $url returned 200"
                return json
            }
        }
        log.debug "Zendesk response was: ${res}"
        return res
    }

    String uploadAttachment(String filename, String contentType, InputStream content) {
        def resp = postBinaryFile("/api/v1/uploads.json", contentType, content, [query:[filename:filename]])
        if (resp) {
            return resp.upload.id // return the ID
        }

        log.error "Could not create Zendesk attachment: ${resp.status} (${resp.statusLine})"
        return null
    }

    /**
     * Create a new request (as an end-user)
     *
     * Args contains:
     *   subject - subject of the ticket
     *   description - description of the ticket
     *   priority (optional) - priority of the ticket
     *   fields (optional) - map of field ID (string like '849433') and value
     *   uploads (optional) - list of upload ID strings
     */
    def createTicket(Map args) {
        log.debug "Creating Zendesk ticket with values: ${args}"
        def body = [
            ticket : [
                subject : args.subject,
                description : args.description
            ]
        ]
        if (args.tags) {
            body.ticket.'set-tags' = args.tags
        }
        if (args.priority) {
            body.ticket.priority = args.priority
        }
        if (args.fields) {
            body.ticket.fields = [:]
            body.ticket.fields.putAll(args.fields)
        }
        if (args.uploads) {
            body.ticket.attachments = args.attachments
        }

        // NOTE: we post JSON to .xml because of the 406 status bug in Zendesk API!
        def resp = postRequest('/requests.xml', body, [headers:['X-On-Behalf-Of':args.onBehalfOf]])
        if (resp.status == 201) {
            return resp.getLastHeader('Location')
        }

        log.error "Could not create zendesk issue: ${resp.status} (${resp.statusLine})"
        return null
    }

    def search(query) {
        def resp = getRequest('/api/v2/search.json', [query:[query:query]])
        if (resp != null) {
            return resp
        }

        log.error "Could not search zendesk tickets"
        return null
    }

    def getRequests() {
        def resp = getRequest('/requests.json')
        if (resp != null) {
            return resp
        }

        log.error "Could not get zendesk tickets"
        return null
    }
}
