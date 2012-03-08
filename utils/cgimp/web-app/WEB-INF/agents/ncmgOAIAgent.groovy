package com.k_int.media;

import com.gmongo.GMongo
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import grails.converters.*
// import groovy.text.Template
// import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.commons.logging.LogFactory
import java.util.zip.*;
import groovy.xml.MarkupBuilder
import org.springframework.beans.factory.InitializingBean
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


class ncmgOAIAgent {

  private static final log = LogFactory.getLog(this)
  
  // handlers have access to the repository mongo service.. suggest you use http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/
  def getAgentName() {
    "Nottinhgam City Museums and Galleries OAI Agent"
  }

  def getRevision() {
    1
  }

  def isActive() {
    return true
  }

  def process(properties, ctx, otherlog) {
    
    // Get a handle to the local mongo service
    def mongo = new com.gmongo.GMongo();
    def db = mongo.getDB("gatherer")

    def repository_client_service = ctx.getBean('repositoryClientService')

    def feed_baseurl = 'http://localhost:8080/repository/upload.json'
    def feed_identity = 'admin'
    def feed_credentials = 'password'
    log.debug("Assemble repository client to ${feed_baseurl} - ${feed_identity}/${feed_credentials}");
    def aggregator_service = new HTTPBuilder( feed_baseurl )
    aggregator_service.auth.basic feed_identity, feed_credentials


    def ncmg_gatherer_agent_info = db.agents.findOne(identifier: 'mcmg_gatherer_agent_info')

    if ( ncmg_gatherer_agent_info == null ) {
      ncmg_gatherer_agent_info = [:]
    }

    def props =[:]
    props.reccount = 0;

    // def oai_endpoint = new HTTPBuilder( 'http://culturegrid.org.uk/dpp/oai' )
    def oai_endpoint = new RESTClient( 'http://www.culturegrid.org.uk/dpp/oai' )
    // oai_endpoint.auth.basic model.dppUser, model.dppPass

    def rt = fetchOAIPage(repository_client_service, oai_endpoint, aggregator_service, null, props)

    while ( ( rt != null ) && ( rt.length() > 0 ) ) {
      try {
        Thread.sleep(5000);
      }
      catch ( Exception e ) {
      }

      log.debug("Iterating using resumption token");
      rt = fetchOAIPage(repository_client_service, oai_endpoint, aggregator_service, rt, props);
    }

    db.agents.save(ncmg_gatherer_agent_info);
  }

  def fetchOAIPage(repository_client_service, 
                   oai_endpoint, 
                   aggregator_service,
                   resumption_token, 
                   props) {

    def result = null;

    // oai_endpoint.request(GET,XML) {request ->
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
                      'metadataPrefix':'pnds_dcap_raw', 
                      'set' : "PN:NCMG:*" ]  // from, until,...
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
          repository_client_service.uploadStream(db,aggregator_service, 'nmcg')

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

  // http://localhost:28017/gatherer/records/?limit=1 to see an example record in the mongo http interface
  def processEntry(rec, db, log) {
    // rec['aggregator.internal.id'], rec['dc.related.link'], 
    def local_info = db.records.findOne(identifier: rec['aggregator.internal.id'])
    if ( local_info == null ) {
      local_info = [:]
      local_info.identifier=rec['aggregator.internal.id']
      local_info.descriptive_record=rec
      db.records.save(local_info);
    }
    else {
      log.debug("found ${rec['aggregator.internal.id']}, image is at ${rec['dc.related.link']}");
    }
    test2();
    log.debug("Call construct zip");
    buildXml(rec)
    // log.debug("Call build zip");
    // zip(rec)
    log.debug("done");
  }

  def test2() {
    log.info("fred");
    554
  }

  def zip(rec) {
    log.debug("Build zip");
    File f = new File("/tmp/t.zip")
    if ( f.exists() ) {
      log.debug("Delete existing")
      f.delete() 
    }
    FileOutputStream fos = new FileOutputStream(f)
    def zipStream = new ZipOutputStream(fos) 

    def file = new File("/tmp/t") 
    def entry = new ZipEntry(file.name) 
    zipStream.putNextEntry(entry) 
    zipStream << new FileInputStream(file)
    zipStream.closeEntry()
    zipStream.close();
    log.debug("constructZip Completed");
  }

  def buildXml(rec) {
    log.debug("Build xml");
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.'description'('xmlns': 'http://purl.org/mla/pnds/pndsdc/',
                     'xmlns:dc' : 'http://purl.org/dc/elements/1.1/',
                     'xmlns:dcterms':'http://purl.org/dc/terms/',
                     'xmlns:e20cl':'http://www.20thcenturylondon.org.uk/',
                     'xmlns:pnds_dc': 'http://purl.org/mla/pnds/pndsdc/',
                     'xmlns:pndsterms':'http://purl.org/mla/pnds/terms/',
                     'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance',
                     'xsi:schemaLocation': 'http://www.peoplesnetwork.gov.uk/schema/CultureGrid_Item http://www.peoplesnetwork.gov.uk/schema/CultureGrid_Item.xsd'  ) {
      'dc:identifier'('1977-5750')
      'dc:title'('title')
      'dc:description'('descr')
      'dc:publisher'('descr')
      'dc:type'('descr')
      'dcterms:rightsholder'('descr')
      'dc:subject'('subject')
    }
    def result_xml = writer.toString()
    log.debug(result_xml)
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
