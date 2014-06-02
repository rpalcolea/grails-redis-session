grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
    }
    dependencies {
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:1.0.1") {
            export = false
        }
        compile(":redis:1.5.5", ":database-session:1.2.1", ":webxml:1.4.1")

        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
