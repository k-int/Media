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


class exampleSolrAgent {

  private static final log = LogFactory.getLog(this)
  
  // handlers have access to the repository mongo service.. suggest you use http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/
  def getAgentName() {
    "Example SOLR Agent"
  }

  def getRevision() {
    1
  }

  def isActive() {
    return false
  }

  def process(properties, ctx, otherlog) {
    
    println "This is the example solr harvest agent code.......process..."

    // Get a handle to the local mongo service
    def mongo = new com.gmongo.GMongo();
    def db = mongo.getDB("gatherer")

    def ncmg_gatherer_agent_info = db.agents.findOne(identifier: 'mcmg_gatherer_agent_info')

    if ( ncmg_gatherer_agent_info == null ) {
      ncmg_gatherer_agent_info = [:]
    }

    // Set up a solr instance we will use to iterate through the entries in the ncmg collection
    def solr_instance = new org.apache.solr.client.solrj.impl.CommonsHttpSolrServer("http://www.culturegrid.org.uk/index")

    ModifiableSolrParams solr_params = new ModifiableSolrParams();
    // solr_params.set("q", "record_type:institution AND institution_sector:Museums")
    solr_params.set("q", 'dcterms.isPartOf:"NCMG"')
    solr_params.set("start", 0);
    solr_params.set("rows", "50");

    println "solr params : ${solr_params}"
    QueryResponse response = solr_instance.query(solr_params);

    SolrDocumentList sdl = response.getResults();
    int record_count = sdl.getNumFound();
    log.debug("Query returns ${record_count} documents for NCMG");

    int start = 0;

    // Limit in testing
    if ( record_count > 100 )
      record_count = 100;

    while ( ( response.getResults().size() > 0 ) && ( start < record_count ) ) {
      log.debug("Processing ${response.getResults().size()}");
      response.getResults().each{ rec ->
        start++
        processEntry(rec, db, log);
        log.debug("internal ID: ${rec['aggregator.internal.id']}");
        def t = test2()
        log.debug("Result of test2: ${t}");
      }

      log.debug("Processed ${start} out of ${record_count}");

      // Load next batch
      if ( start < record_count ) {
        log.debug("Requesting 50 from ${start}");
        solr_params.set("start", start);
        response = solr_instance.query(solr_params);
        sdl = response.getResults();
      }
    }

    db.agents.save(ncmg_gatherer_agent_info);
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
