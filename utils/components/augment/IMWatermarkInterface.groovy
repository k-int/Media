import groovy.xml.MarkupBuilder

class IMWatermarkInterface {
  
  /**
   * Embed the identified properties as XMP attributes in the given file.
   */
  static def watermark(image_file_name, watermark_text) {
    println("Watermarking file ${image_file_name} with text ${watermark_text}");

    // def cmd = "convert -size 140x80 xc:none -fill grey -gravity NorthWest -draw \"text 10,10 \'${watermark_text}\'\" -gravity SouthEast -draw \"text 5,15 \'${watermark_text}\'\" miff:- | composite -tile - ${image_file_name} ${image_file_name}"

    // convert -size 140x80 xc:none -fill grey -gravity NorthWest -draw "text 10,10 'Copyright'" -gravity SouthEast -draw "text 5,15 'Copyright'" miff:- | composite -tile - logo.jpg  wmark_text_tiled.jpg

    def conv_cmd_arr = [ 'convert', 
                         '-size',
                         ' 140x80', 
                         'xc:none', 
                         '-fill',
                         'grey', 
                         '-gravity', 
                         'NorthWest', 
                         '-draw',
                         "text 10,10 \'${watermark_text}\'", 
                         '-gravity',
                         ' SouthEast', 
                         '-draw',
                         "text 5,15 \'${watermark_text}\'", 
                         'miff:-']

    def wm_cmd_arr = "composite -tile - ${image_file_name} ${image_file_name}"

    println("${conv_cmd_arr} | ${wm_cmd_arr}");

    def conv_proc = conv_cmd_arr.execute()
    def wm_proc = wm_cmd_arr.execute();

    println("${conv_proc} | ${wm_proc}");

    conv_proc | wm_proc

    wm_proc.waitFor()

    println "wm return code: ${ wm_proc.exitValue()}"
    println "wm stderr: ${wm_proc.err.text}"
    println "wm stdout: ${wm_proc.in.text}"
    // process.in.eachLine { line -> 
    //   println line 
    // }
  }
}
