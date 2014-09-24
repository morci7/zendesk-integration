class ZendeskIntegrationGrailsPlugin {
    def version = '0.1'
    def grailsVersion = '2.3 > *'
    def pluginExcludes = [
        "grails-app/controllers/TestController.groovy"
    ]
    def title = 'Zendesk Integration Plugin'
    def author = 'Albert Morcillo'
    def authorEmail = 'morci7@gmail.com'
    def description = 'Zendesk integration for Grails'
    def documentation = 'http://grails.org/plugin/zendesk-integration'
    def license = 'APACHE'
    def issueManagement = [ system: 'JIRA', url: 'http://jira.grails.org/browse/GPMYPLUGIN' ]
    def scm = [ url: 'http://svn.codehaus.org/grails-plugins/' ]
}
