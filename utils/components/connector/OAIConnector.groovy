import com.gmongo.GMongo
import groovy.xml.MarkupBuilder
import org.apache.commons.logging.LogFactory
import java.util.zip.*;
import groovy.xml.MarkupBuilder
import groovy.util.slurpersupport.GPathResult
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset
import groovyx.net.http.RESTClient
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset


class OAIConnector {

  private static final log = LogFactory.getLog(this)

  def sync(oai_connector_info) {
    println("OAIConnector::sync(${oai_connector_info})");

    // Get a handle to the local mongo service
    // def mongo = new com.gmongo.GMongo();
    // def db = mongo.getDB("media_rcs")

    def repo_baseurl = 'http://localhost:8080/repository/upload.json'
    def repo_identity = 'admin'
    def repo_credentials = 'password'
    log.debug("Assemble repository client to ${repo_baseurl} - ${repo_identity}/${repo_credentials}");

    def aggregator_service = new HTTPBuilder( repo_baseurl )
    aggregator_service.auth.basic repo_identity, repo_credentials


    // def oai_connector_info = db.agents.findOne(identifier: 'mcmg_gatherer_agent_info')

    if ( oai_connector_info == null ) {
      println("No connector info... abort");
      return;
    }

    def props =[:]
    props.reccount = 0;

    // def oai_endpoint = new HTTPBuilder( 'http://culturegrid.org.uk/dpp/oai' )
    def oai_endpoint = new RESTClient( 'http://www.culturegrid.org.uk/dpp/oai' )
    // oai_endpoint.auth.basic model.dppUser, model.dppPass

    def rt = fetchOAIPage(oai_endpoint, aggregator_service, null, props, oai_connector_info.prefix, oai_connector_info.setname)

    while ( ( rt != null ) && ( rt.length() > 0 ) ) {
      try {
        Thread.sleep(5000);
      }
      catch ( Exception e ) {
      }

      log.debug("Iterating using resumption token");
      rt = fetchOAIPage(oai_endpoint, aggregator_service, rt, props, oai_connector_info.prefix, oai_connector_info.setname);
    }

    db.agents.save(oai_connector_info);
  }

  def fetchOAIPage(oai_endpoint, 
                   aggregator_service,
                   resumption_token, 
                   props,
                   prefix,
                   setname) {

    def result = null;

    oai_endpoint.request(GET) {request ->

      // uri.path = '/ajax/services/search/web'
      if ( resumption_token != null ) {
        log.debug("Processing with resumption token...");
        uri.query = [ 'verb':'ListRecords', 
                      'resumptionToken':resumption_token, 
                      ]  // from, until,...
      }
      else {
        log.debug("Initial harvest - no resumption token");
        uri.query = [ 'verb':'ListRecords', 
                      'metadataPrefix':prefix,
                      'set': setname ]  // from, until,...
      }

      request.getParams().setParameter("http.socket.timeout", new Integer(5000))
      headers.Accept = 'application/xml'
      // headers.'User-Agent' = 'GroovyHTTPBuilderTest/1.0'
      // headers.'Referer' = 'http://blog.techstacks.com/'
      response.success = { resp, xml ->
        // log.debug( "Server Response: ${resp.statusLine}" )
        // log.debug( "Server Type: ${resp.getFirstHeader('Server')}" )
        // log.debug( "content type: ${resp.headers.'Content-Type'}" )

        xml?.ListRecords?.record.each { rec ->
          // log.debug("Record under xml ${rec.toString()}");
          def builder = new StreamingMarkupBuilder()
          // log.debug("record: ${builder.bindNode(rec.metadata.description).toString()}")
          def new_record = builder.bindNode(rec.metadata.children()[0]).toString()
          log.debug("submit record[${props.reccount++}]")

          byte[] db = new_record.getBytes('UTF-8')
          uploadStream(db,aggregator_service, 'nmcg')

          try {
            Thread.sleep(500);
          }
          catch ( Exception e ) {
          }
        }

        result = xml?.ListRecords?.resumptionToken?.toString()
      }

      response.failure = { resp ->
        log.debug( resp.statusLine )
      }
    }

    log.debug("fetch page returning ${result}.");

    result
  }

  def uploadStream(document_bytes,target_service, data_provider) {

    log.debug("About to make post request");

    try {
      byte[] resource_to_deposit = document_bytes

      log.debug("Length of input stream is ${resource_to_deposit.length}");

      target_service.request(POST) {request ->
        requestContentType = 'multipart/form-data'

        // Much help taken from http://evgenyg.wordpress.com/2010/05/01/uploading-files-multipart-post-apache/
        def multipart_entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipart_entity.addPart( "owner", new StringBody( data_provider, "text/plain", Charset.forName( "UTF-8" )))  // Owner

        def uploaded_file_body_part = new org.apache.http.entity.mime.content.ByteArrayBody(resource_to_deposit, 'text/xml', 'filename')
        multipart_entity.addPart( "upload", uploaded_file_body_part)

        request.entity = multipart_entity;

        response.success = { resp, data ->
          log.debug("response status: ${resp.statusLine}")
          log.debug("Response data code: ${data?.code}");
        }

        response.failure = { resp ->
          log.error("Failure - ${resp}");
        }
      }
    }
    catch ( Exception e ) {
      log.error("Unexpected exception trying to read remote stream",e)
    }
    finally {
      log.debug("uploadStream try block completed");
    }
    log.debug("uploadStream completed");
  }

}


/*

<culturegrid_item:description xmlns:culturegrid_item="http://www.peoplesnetwork.gov.uk/schema/CultureGrid_Item" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:e20cl="http://www.20thcenturylondon.org.uk" xmlns:pnds_dc="http://purl.org/mla/pnds/pndsdc/" xmlns:pndsterms="http://purl.org/mla/pnds/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.peoplesnetwork.gov.uk/schema/CultureGrid_Item http://www.peoplesnetwork.gov.uk/schema/CultureGrid_Item.xsd">
<dc:identifier>1977-5750</dc:identifier>
<dc:title>Painting</dc:title>
<dc:description>
Painting. Nairn by S.J. Lamorna Birch. Original painting for LMS poster
</dc:description>
<dc:subject>painting</dc:subject>
<dc:subject>artwork</dc:subject>
<dc:type encSchemeURI="http://purl.org/dc/terms/DCMIType">PhysicalObject</dc:type>
<dcterms:license valueURI="http://creativecommons.org/licenses/by-nc-sa/2.5/">Creative Commons Licence</dcterms:license>
<dcterms:rightsHolder>ScienceMuseum</dcterms:rightsHolder>
<dcterms:isPartOf>National Railway Museum</dcterms:isPartOf>
</culturegrid_item:description>


<description xmlns="http://purl.org/mla/pnds/pndsdc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:e20cl="http://www.20thcenturylondon.org.uk/" xmlns:pndsterms="http://purl.org/mla/pnds/terms/">
<dc:identifier>1977-5750</dc:identifier>
<dc:identifier/>
<dc:title>Painting</dc:title>
<dc:description>
Painting. Nairn by S.J. Lamorna Birch. Original painting for LMS poster
</dc:description>
<dc:publisher/>
<dc:type>PhysicalObject</dc:type>
<dcterms:rightsHolder/>
<dcterms:isPartOf>NRM - Pictorial Collection (Railway)</dcterms:isPartOf>
<dcterms:license valueURI="http://creativecommons.org/licenses/by-nc-sa/2.5/">Creative Commons Licence</dcterms:license>
<dc:subject>painting</dc:subject>
<dc:subject>artwork</dc:subject>
<pndsterms:extension/>
</description>
*/
