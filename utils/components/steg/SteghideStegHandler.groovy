

class SteghideStegHandler {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  def hide(identifier, file) {

    // Create a work directory
    println("embed ${properties} in ${file}");
    def inputfile = "input1b.jpg"
    def keyfile = "key.txt"
    def process = "steghide embed -cf ${inputfile} -p "" -ef ${key.txt}".execute()
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
    def inputfile = "input1b.jpg"
    def process = "steghide extract -cf ${inputfile} -p "" -xf ${key.txt}".execute()
    process.in.eachLine { line -> 
      println line 
    }
  }
}
