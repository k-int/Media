#!/usr/bin/groovy

@GrabResolver(name='es', root='https://oss.sonatype.org/content/repositories/releases')
// @Grab(group='com.gmongo', module='gmongo', version='0.9.2')
@Grapes([
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
  @Grab(group='com.gmongo', module='gmongo', version='0.9.2'),
  @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.0'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0')
])

def starttime = System.currentTimeMillis();
println("Startup.. Initialising at ${starttime}");

def cs = new ConnectorSubsystem();

cs.removeAll();

// cs.registerOAIConnector(shortcode:'ncmg',baseuri:'http://www.culturegrid.org.uk/dpp/oai', prefix:'pnds_dcap_raw',setname:'PN:NCMG:*');
// cs.registerOAIConnector('ncmg','http://www.culturegrid.org.uk/dpp/oai','pnds_dcap_raw','PN:NCMG:*');

// Use the named param syntax insead to supply some optional params.
cs.registerConnector([
                      connector:'OAIConnector',
                      shortcode:'ncmg',
                      baseuri:'http://www.culturegrid.org.uk/dpp/oai',
                      setname:'PN:NCMG:*',
                      prefix:'pnds_dcap_raw',
                      maxbatch:100]);

println(cs.listConnectors());

cs.syncRepository('ncmg');

println("Completed after ${System.currentTimeMillis() - starttime}ms");

