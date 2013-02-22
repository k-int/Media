#!/usr/bin/groovy

@Grapes([
  @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.7.2')
])

import wslite.soap.*


println("Configure proxy...");
// def proxy = new SOAPClient("http://clientdemo.soutron.net/Library/WebServices/SoutronAPI.svc?wsdl")
def proxy = new SOAPClient("http://clientdemo.soutron.net/Library/WebServices/SoutronAPI.svc/soap");

println("search....");
def result = proxy.send(SOAPAction: 'Soutron.SoutronGlobal.SoutronGlobalApi/ISoutronGlobalApi/SearchCatalogues') {
  envelopeAttributes "xmlns:s": "Soutron.SoutronGlobal.SoutronGlobalApi"
  body {
    's:SearchCatalogues' {
      q('House')
      // searchId
      // lang
      // userId
      // office
      // opac
      // standard
      // page
      // pageSize
      // material
      // ctrt
      // fields
      // sort
      // usersearchId
      // inclDepRec
    }
  }
}


println("Got result: ${result}");

// def r2 = new String(result)
// println("Result as string: ${r2}");

println("Result class: ${result.dump}")
println("Result: ${result.body}")
byte[] bytes = result.body.text().getBytes()
println(bytes);
println(new String(bytes));
// response.httpResponse.statusCode == 200
// response.soapVersion == SOAPVersion.V1_1

