@Grab(group='axis', module='axis', version='1.4')
import org.apache.axis.wsdl.WSDL2Java
class Generator {
static def generate(){
WSDL2Java.main("http://clientdemo.soutron.net/Library/WebServices/SoutronAPI.svc")
}
}
Generator.generate()
