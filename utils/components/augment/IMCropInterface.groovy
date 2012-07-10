package com.k_int.media.augment;

class IMCropInterface {
  
  /**
  * Crop function based on the needs of the ciim as a starting point. 
  * Takes an existing image and produces a new one which has been cropped according to the specified params.
  * 
  * @param input_file_name      the path and filename of the image to be cropped
  * @param output_file_name     the path and filename of the cropped version of the original image
  * 
  * @param top                  the number of pixels to shave off the top of the image
  * @param right                the number of pixels to shave off the right of the image
  * @param bottom               the number of pixels to shave off the bottom of the image
  * @param left                 the number of pixels to shave off the left of the image
  * 
  * @param imagemagick_abs_path Imagemagick absolute path is required to avoid confusion between Imagemagick convert and Microsoft's convert 
  *                             NOTE: All occurrences of '\' must be escaped. Value should end with '\\'
  *                             
  * @throws IMProcessingException
  */
 static def crop(input_file_name, output_file_name, top, right, bottom, left, imagemagick_abs_path) {
   
   //println("Cropping file ${input_file_name} removing ${top},${right},${bottom},${left} (top,right,bottom,left)");

   def image = IMIdentifyInterface.getImageInfo(input_file_name, imagemagick_abs_path)
   
   /* (new image width)x(new image height)+(left indent from original image)+(top indent from original image)
    * 
    * ie.
    * 
    * 50x40+10+10
    * 
    * Means:
    * 
    * A new image will be created which is 50px wide and 40px high, starting at 10px from the left and 10px from the top of the original image
    * 
    * */
   
   def geometry = "${image.width - right - left}x${image.height - bottom - top}+${left}+${top}"
   
   def crop_cmd_arr = [ "${imagemagick_abs_path}convert",
                        "${input_file_name}",
                        '-crop',
                        "${geometry}",
                        "${output_file_name}"]
   
   //crop_cmd_arr.each{ print "${it} " } /* DEBUG - print out the command we are executing */
   
   def crop_cmd = crop_cmd_arr.execute()
   
   crop_cmd.waitFor()
   
   if(crop_cmd.err.text)
   {
      throw new IMProcessingException(crop_cmd.err.text)
   }
 }
}
