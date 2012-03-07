@Grab(group='net.sf.json-lib', module='json-lib', version='2.4', classifier='jdk15')
@Grab(group='commons-lang', module='commons-lang', version='2.4')
@Grab(group='xom', module='xom', version='1.1')

import net.sf.json.*
import net.sf.json.xml.*

def xmlfile = new File('test1.xml')

// see http://json-lib.sourceforge.net/apidocs/
def xml = xmlfile.text
XMLSerializer xmlSerializer = new XMLSerializer();  
JSON json = xmlSerializer.read( xml);  
System.out.println( json.toString(2) );  
