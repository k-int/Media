import com.gmongo.GMongo
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import grails.converters.*
import groovy.text.Template
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder

class ncmgAgent {
  
  // handlers have access to the repository mongo service.. suggest you use http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/
  def getAgentName() {
    "Nottinhgam City Museums and Galleries Agent"
  }

  def getRevision() {
    1
  }

  def process(properties, ctx, log) {
    println "This is the NCMG agent code.......process..."

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
    solr_params.set("q", "record_type:institution AND institution_sector:Museums")
    solr_params.set("start", 0);
    solr_params.set("rows", "50");

    println "solr params : ${solr_params}"
    QueryResponse response = solr_instance.query(solr_params);
    SolrDocumentList sdl = response.getResults();
    int start = 0;

    while ( response.getResults().size() > 0 ) {
      log.debug("Processing ${response.getResults().size()}");
      response.getResults().each{ rec ->
        start++
        log.debug("${rec['aggregator.internal.id']}");
      }

      // Load next batch
      solr_params.set("start", start);
      response = solr_instance.query(solr_params);
      sdl = response.getResults();
    }

    db.agents.save(ncmg_gatherer_agent_info);
  }
}
