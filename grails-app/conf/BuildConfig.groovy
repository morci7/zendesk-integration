grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile( 'org.codehaus.groovy.modules.http-builder:http-builder:0.7') {
            excludes 'groovy', 'xml-apis', 'xerces'
        }
    }

    plugins {
        build ':release:3.0.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
