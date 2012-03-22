import groovy.xml.MarkupBuilder

class IMRezizeInterface {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  static def resize(image_file_name, resize) {
    println("Resizing file ${image_file_name} to ${resize}");

    def resize_cmd_arr = ['convert',
                          image_file_name,
                          '-thumbnail',
                          '140x140>',
                          '-gravity',
                          'center',
                          '-crop',
                          '140x140+0+0',
                          '+repage',
                          image_file_name]

    def resize_cmd = resize_cmd_arr.execute()
    resize_cmd.waitFor()
    // println "wm return code: ${ resize_cmd.exitValue()}"
    // println "wm stderr: ${resize_cmd.err.text}"
    // println "wm stdout: ${resize_cmd.in.text}"
    // process.in.eachLine { line -> 
    //   println line 
    // }
  }
}
