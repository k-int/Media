package com.k_int.media.augment;

class IMIdentifyInterface 
{
 /**
  * Executes a lookup against the specified image and returns the details (such as height & width) of the image
  * 
  * @param image_file_name      the name of the image to collect information on
  *
  * @param imagemagick_abs_path Imagemagick absolute path is required to avoid confusion between Imagemagick convert and Microsoft's convert 
  *                             NOTE: All occurrences of '\' must be escaped. Value should end with '\\'
  *                             
  * @returns an image object containing values such as height and width 
  * 
  * @throws IMProcessingException
  */
 static def getImageInfo(image_file_name, imagemagick_abs_path) {
     
    /* See: http://www.imagemagick.org/script/escape.php for a list of other characters to get information from the image 
     * 
     * %h = height, 
     * %w = width,
     * %b = filesize (bytes)
     * %t = filename (without the extension / suffix)
     * %e = file extension / suffix
     * the pipe character '|' is used as a seperator so I can easily split the response later
     */
    def format = '"%h|%w|%b|%t|%e"' 
     
    def identify_file_arr = [   "\"${imagemagick_abs_path}identify\"",
                                 '-format',
                                 "${format}",
                                 "${image_file_name}"]

    //execute request
    def identify_file_proc = identify_file_arr.execute()
    
    //retrieve response
    def proc_response = identify_file_proc.waitFor()
    
    if(identify_file_proc.err.text)
    {
        println("Error returned whilst looking up details for the file ${image_file_name} : ${identify_file_proc.err.text}")
        
        throw new IMProcessingException(identify_file_proc.err.text)
    }
     
    def delimited_details = identify_file_proc.text.trim()
   
    def image = [:] //instantiate object map
    
    image.height = Integer.parseInt(delimited_details.split('\\|')[0])
    image.width = Integer.parseInt(delimited_details.split('\\|')[1])
    image.filesize = delimited_details.split('\\|')[2]
    image.filename  = delimited_details.split('\\|')[3]
    image.extension  = delimited_details.split('\\|')[4]
    
    return image
 }
}