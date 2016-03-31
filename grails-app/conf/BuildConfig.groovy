grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        grailsPlugins()
        mavenCentral()
    }
    dependencies {
    }

    plugins {
        build(":tomcat:7.0.52.1",
                ":release:3.0.1",
                ":rest-client-builder:1.0.3") {
            export = false
            excludes "svn"
        }
        compile(":redis:1.6.6", ":database-session:1.2.1", ":webxml:1.4.1")        {
            excludes "svn"
        }


    }
}
