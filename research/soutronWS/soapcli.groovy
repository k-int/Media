#!/usr/bin/groovy

// @GrabResolver(name='es', root='https://oss.sonatype.org/content/repositories/releases')
// @Grab(group='com.gmongo', module='gmongo', version='0.9.2')
@Grapes([
  // @Grab(group='axis', module='axis', version='1.4'),
  // @Grab(group='javax.activation', module='activation', version='1.1.1'),
  // @Grab( group='javax.mail', module='mail', version='1.4.4')
  @Grab(group='org.codehaus.groovy.modules', module='groovyws', version='0.5.2')
])

import groovyx.net.ws.WSClient

println("Configure proxy...");
def proxy = new WSClient("http://clientdemo.soutron.net/Library/WebServices/SoutronAPI.svc?wsdl",this.class.classLoader)

println("Initialize...");
proxy.initialize()

println("search....");
def result = proxy.SearchCatalogues("House", // String q
                                    null, // String searchId
                                    null, // String lang
                                    null, // String userId
                                    null, // String office
                                    null, // String opac
                                    null, // String standard
                                    null, // String page
                                    null, // String pageSize
                                    null, // String material
                                    null, // String ctrt
                                    null, // String fields
                                    null, // String sort
                                    null, // String usersearchId
                                    null) // String inclDepRec

println("Got result: ${result}");

def r2 = new String(result)

println("Result as string: ${r2}");
