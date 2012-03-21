

class SteghideStegHandler {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  def hide(identifier, filepath) {

    listFile(filepath);

    // Create a file containing the identifier
    def identifier_filename = "${filepath}.stegid"
    def identifier_file = new File(identifier_filename);
    identifier_file << "${identifier}\n";

    // Create a work directory
    println("embed ${identifier_filename} in ${filepath}");
    
    def steg_cmd = "steghide embed -v -ef ${identifier_filename} -p media -cf ${filepath}"
    println("run ${steg_cmd}");
    def process = steg_cmd.execute()

    process.err.eachLine { line ->
      println line
    }

    process.in.eachLine { line -> 
      println line 
    }

    listFile(filepath);

    // May try process.withWriter { writer -> <cr> writer << 'test text' }
    // identifier_file.delete();
  }

  def listFile(filepath) {
    def p2 = "ls -la ${filepath}".execute()
    p2.in.eachLine { line ->
      println line
    }
  }
  /**
   * Extract any xmp metadata from the given file
   */
  def extract(file) {
    println("extract XMP from ${file}");
    def inputfile = "input1b.jpg"
    def process = "steghide extract -cf ${inputfile} -p \"\" -xf ${key.txt}".execute()
    process.in.eachLine { line -> 
      println line 
    }
  }
}
