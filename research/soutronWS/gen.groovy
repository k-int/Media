#!/usr/bin/groovy

// @GrabResolver(name='es', root='https://oss.sonatype.org/content/repositories/releases')
// @Grab(group='com.gmongo', module='gmongo', version='0.9.2')
@Grapes([
  @Grab(group='axis', module='axis', version='1.4'),
  @Grab(group='javax.activation', module='activation', version='1.1.1'),
  @Grab( group='javax.mail', module='mail', version='1.4.4')
])


import org.apache.axis.wsdl.WSDL2Java
class Generator {
static def generate(){
WSDL2Java.main("http://clientdemo.soutron.net/Library/WebServices/SoutronAPI.svc?wsdl");
}
}
Generator.generate()
