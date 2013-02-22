#!/usr/bin/groovy

@Grapes([
  @Grab( 'org.ccil.cowan.tagsoup:tagsoup:1.2.1' )
])

import org.ccil.cowan.tagsoup.Parser

def rec = new URL( 'http://gp.soutron.net/ongoinglibrary332/WebServices/SoutronApi.svc/SearchCatalogues?q=Car&data=s0utr0n&&pageSize=100' ).withReader { r ->
  new XmlSlurper().parse( r )
}


println("id: ${rec.search_info.@id}");
println("total items: ${rec.search_info.@totalItems}");
println("max relevance: ${rec.search_info.@maxRelevanceRank}");
println("Reccount: ${rec.search_info.catalogs_view.ct.@count}");

rec.search_info.catalogs_view.ct.each { ct ->
  ct.ctlgs.each { ctl ->
    ctl.cat.each { catrec ->
      catrec.fs.f.each { fld ->
        fld.vs.v.each { v ->
          println("${fld.@name} = ${v.text()}");
        }
      }
      println('');
    }
  }
}
