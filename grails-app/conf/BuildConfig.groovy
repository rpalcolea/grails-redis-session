grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.repos.ow.url = "http://artifactory.owteam.com/artifactory/ow-v2"
grails.project.repos.ow.username = "jenkins-dev"
grails.project.repos.ow.password = "{DESede}+oLx3dET0kdyvlXMeGJjpQ=="
grails.project.target.level = 1.6
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenRepo "https://repo.grails.org/grails/plugins"
        mavenCentral()
        mavenRepo "http://artifactory.owteam.com/artifactory/simple/ow-v2"
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
        compile(":redis:1.5.5", ":database-session:1.2.1", ":webxml:1.4.1")        {
            excludes "svn"
        }


    }
}
