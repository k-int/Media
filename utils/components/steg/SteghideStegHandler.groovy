

class SteghideStegHandler {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  def hide(identifier, file) {
    println("embed ${properties} in ${file}");
    def process = "exiv2".execute()
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
    def process = "exiv2".execute()
    process.in.eachLine { line -> 
      println line 
    }
  }
}
