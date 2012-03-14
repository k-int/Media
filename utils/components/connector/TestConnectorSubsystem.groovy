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

cs.registerOAIConnector('test1','http://some.where');
cs.registerOAIConnector('test2','else.where','prefix','setname');
cs.registerOAIConnector('test3','else.where','prefix','setname','user','pass');

println("Test named parameters");
cs.registerOAIConnector(shortcode:'test4',baseuri:'test4base');

println("Completed after ${System.currentTimeMillis() - starttime}ms");

