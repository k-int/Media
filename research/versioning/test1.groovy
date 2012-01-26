@Grab(group='net.sf.json-lib', module='json-lib', version='2.4', classifier='jdk15')

import net.sf.json.JSONObject
            
def json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },array:[1,2]}";  
def jsonObject = JSONObject.fromObject( json );  

print jsonObject
