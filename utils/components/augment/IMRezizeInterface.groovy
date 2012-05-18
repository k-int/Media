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
  
  /**
  * Resize function based on the needs of the CIIM as a starting point
  * 
  * @param input_file_name      String      absolute path and filename of the image to be resized. NOTE: All occurrences of '\' must be escaped
  * @param output_file_name     String      absolute path and filename of the resized version of the original image. NOTE: All occurrences of '\' must be escaped
  * @param resize_height        Integer     optional field, the height to resize the image to
  * @param resize_width         Integer     optional field, the width to resize the image to
  * 
  * @param ignore_aspect_ratio  Boolean     defines if the image should be distorted to fit the specified dimensions exactly (true), or scaled to fit WITHIN the dimensions (false)
  * 
  * REQUIRED FOR WINDOWS DEPLOYMENT
  * @param imagemagick_abs_path Imagemagick absolute path is required to avoid confusion between Imagemagick convert and Microsoft's convert 
  *                             (for non windows this value can be null). NOTE: All occurrences of '\' must be escaped. Value should end with '\\'
  *                             
  * @throws IMProcessingException
  */
 static def resize(input_file_name, output_file_name, resize_height, resize_width, ignore_aspect_ratio, imagemagick_abs_path) {
   
   println("Resizing file ${input_file_name} to ${resize_height ? resize_height : 'unspecified'} x ${resize_width ? resize_width : 'unspecified'} (h x w)");

   def resize_value
   
   if(resize_height && resize_width) //resize to box
   {
       resize_value = resize_height + 'x' + resize_width;
       
       if(ignore_aspect_ratio.toBoolean() == true)
       {
           resize_value += "!"
       }
   }
   else if(resize_height) //% scale by height
   {
       def image = IMIdentifyInterface.getImageInfo(input_file_name, imagemagick_abs_path)

       //if we have a value then we can proceed
       if(image?.height && image.height > 0)
       {  
           resize_value = (resize_height / image.height * 100) + "%";
       }
       else
       {
           throw new IMProcessingException("Failed to retrieve the height of the original image")
       }
   }
   else if(resize_width) //% scale by width
   {
       def image = IMIdentifyInterface.getImageInfo(input_file_name, imagemagick_abs_path)
       
       //if we have a value then we can proceed
       if(image?.width && image.width > 0)
       {  
           resize_value = (resize_width / image.width * 100) + "%";
       }
       else
       {
           throw new IMProcessingException("Failed to retrieve the width of the original image")
       }
   }
   else //neither height nor width specified so cannot resize
   {
       throw new IMProcessingException("Unable to resize image as neither a height nor width was supplied")  
   }
   
   if(resize_value)
   {
       def resize_cmd_arr = [  "\"${imagemagick_abs_path}convert\"",
                               "${input_file_name}",
                               "-resize",
                               "${resize_value}",
                               "${output_file_name}"]
              
       resize_cmd_arr.each{ print "${it} " } /* DEBUG - print out the command we are executing */
       
       def resize_cmd = resize_cmd_arr.execute()
       
       if(resize_cmd.err.text)
       {
           throw new IMProcessingException(resize_cmd.err.text)
       }
       
       def proc_response = resize_cmd.waitFor()
   }
 }
}
