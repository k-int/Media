

class ConnectorSubsystem {

  def mongo = new com.gmongo.GMongo()
  def db = mongo.getDB("media_rcs")

  def registerOAIConnector(Map args=[:]) {
    println("Named parameter version..");
  }

  def registerOAIConnector(shortcode, baseuri, prefix='oai_dc', setname=null, un=null, pw=null) {
    println("registerRemoteOAIRepository ${baseuri}");
    registerConnector([shortcode:shortcode, 
                       baseuri:baseuri,
                       setname:setname, 
                       prefix:prefix, 
                       un:un, 
                       pw:pw,
                       connector:'OAIConnector']);
  }

  def registerConnector(Map params) {
    println("Looking up existing connector with shortcode ${params.shortcode}");
    def existing_entry = db.connectors.findOne(shortcode:params.shortcode)
    if ( existing_entry ) {
      println("Update existing connector");
    }
    else {
      println("Register new connector");
      db.connectors.save(params);
    }
  }

  def listConnectors() {
    def result = []
    db.connectors.find().each { c ->
      result.add(c);
    }
    result
  }

  def syncAll() {
  }

  def removeAll() {
    db.connectors.remove([:]);
  }

  def syncRepository(shortcode) {
    def existing_entry = db.connectors.findOne(shortcode:shortcode)
    if ( existing_entry ) {
      println("sync with ${shortcode} - Get instance of ${existing_entry.connector}");
      GroovyClassLoader gcl = new GroovyClassLoader();
      def connector_class = gcl.loadClass(existing_entry.connector);
      def connector = connector_class.newInstance();
      connector.sync(existing_entry);
      // Update any returned params
      println("Updating entry: ${existing_entry}");
      db.connectors.update(params);
    }
    else {
      throw new Exception("Unknown Connector Shortcode ${shortcode}");
    }
  }
}
