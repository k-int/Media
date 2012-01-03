@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

import com.gmongo.GMongo
import grails.converters.*
import groovy.text.Template
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset

class eventSheffieldAgent {
  
  // handlers have access to the repository mongo service.. suggest you use http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/
  def getAgentName() {
    "Event Sheffield"
  }

  def getRevision() {
    1
  }

  def isActive() {
    return true
  }

  def process(properties, ctx, log) {
    println "This is the event sheffield agent code.......process..."

    // Get a handle to the local mongo service
    def mongo = new com.gmongo.GMongo();
    def db = mongo.getDB("gatherer")

    def event_sheffield_gatherer_agent_info = db.agents.findOne(identifier: 'event_sheffield_gatherer_agent_info')

    if ( event_sheffield_gatherer_agent_info == null ) {
      event_sheffield_gatherer_agent_info = [:]
    }



    db.agents.save(event_sheffield_gatherer_agent_info);
  }


  def firstSearch() {
    def simple_search = new HTTPBuilder( 'http://www.welcometosheffield.co.uk/dms-connect/search' )
    try {
      def response = simple_search.post(
      body: [
        startdate: "",
        enddate: "",
        // contentType: groovyx.net.http.ContentType.TEXT,
        contentType: "text/html; charset=UTF8",
        requestContentType: URLENC
      ]) {  resp, parsed_page ->
        def links = parsed_page.depthFirst().findAll{ it.name() == 'A' }
        println "links: ${links.size()}"
        // links.each { row ->
        //   def uri = "${row.@href}"
        //   def internal_id = uri.substring(uri.firstIndexOf('=')+1, uri.firstIndexOf('&'))
        //   println "Processing ${internal_id}"
        //   println "${row.@href} internal id is ${internal_id}"
        //   def current_record = all_records[internal_id]
        //   if ( current_record != null ) {
        //     // Already in memory
        //   }
        //   else {
        //     current_record = reader.readRecord(internal_id)
        //     all_records[internal_id]  = current_record;
        //   }
        // }
      }
    }
    catch ( Exception e ) {
        println "Problem ${e}"
        e.printStackTrace()
    }
    finally {
    }
  }
}
