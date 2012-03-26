import groovy.xml.MarkupBuilder

class ExivMetadataInterface {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  def embed(identifier, owner, target) {

    def xmpfile_name = "${target}.xmp"
    makeXMPFile(identifier,owner,xmpfile_name);

    def cmd = "exiv2 -v -i X insert ${xmpfile_name} ${target}"
    def process = cmd.execute()
    process.in.eachLine { line -> 
      println line 
    }

     // May try process.withWriter { writer -> <cr> writer << 'test text' }
  }

  /**
   * Extract any xmp metadata from the given file
   */
  def extract(file) {
    println("extract XMP from ${file}");
    def process = "exiv2 -PXkv ${file}".execute()
    process.in.eachLine { line -> 
      println line 
    }
  }

  def makeXMPFile(identifier,owner,xmpfile_name) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.'x:xmpmeta'('xmlns:x': 'adobe:ns:meta/', 
                    'xmlns:rdf' : 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
                    'xmlns:xmp' : 'http://ns.adobe.com/xap/1.0/',
                    'xmlns:media' : 'http://k-int.com/ns/media') {
      xml.'rdf:RDF'() {
        xml.'rdf:Description'('rdf:about':"uri:media:${identifier}") {
          xml.'xmp:CreatorTool'() {
            mkp.yield('MEDIA Project XMP Metadata Embedding Agent')
          }
          xml.'media:owner'() {
            mkp.yield("http://media/owner/${owner}")
          }
          xml.'media:license'() {
            mkp.yield("http://media/license/${identifier}")
          }
        }
      }
    }
    new File(xmpfile_name) << writer.toString();
  }
}
