package com.k_int.media.augment;

class IMTiledPyramidInterface 
{
    /**
     * Tile function based on the needs of the ciim as a starting point.
     * Takes an existing image and produces a set of pyramid tile tiffs.
     *
     * @param input_file_name      the path and filename of the image to be cropped
     * @param output_file_name     the path and filename of the cropped version of the original image. This should end in .tif.
     *
     * @param width                the tile width for pyramid tiffs
     * @param height               the tile height for pyramid tiffs
     *
     * @param imagemagick_abs_path Imagemagick absolute path is required to avoid confusion between Imagemagick convert and Microsoft's convert
     *                             NOTE: All occurrences of '\' must be escaped. Value should end with '\\'
     *
     * @throws IMProcessingException
     */
    static def tilePyramid(input_file_name, output_file_name, width, height, imagemagick_abs_path)
    {
        return tilePyramid(input_file_name, output_file_name, width, height, 8, imagemagick_abs_path)
    }
    
    /**
     * Tile function based on the needs of the ciim as a starting point.
     * Takes an existing image and produces a set of pyramid tile tiffs.
     *
     * @param input_file_name      the path and filename of the image to be cropped
     * @param output_file_name     the path and filename of the cropped version of the original image. This should end in .tif.
     *
     * @param width                the tile width for pyramid tiffs
     * @param height               the tile height for pyramid tiffs
     * @param depth                the number of bits in a color sample within a pixel. Typical value is 8
     *
     * @param imagemagick_abs_path Imagemagick absolute path is required to avoid confusion between Imagemagick convert and Microsoft's convert 
     *                             NOTE: All occurrences of '\' must be escaped. Value should end with '\\'
     *
     * @throws IMProcessingException
     */
    static def tilePyramid(input_file_name, output_file_name, height, width, depth, imagemagick_abs_path){
        
        //convert grad1024.png -define tiff:tile-geometry=128x128 -depth 8 ptif:grad1024.tif
        if(!output_file_name.toUpperCase().endsWith('.TIF'))
        {
            throw new IMProcessingException("parameter output_file_name must end in '.tif', other extensions are not valid for the tilePyramid operation")
        }
        
        def geometry = "${width}x${height}"
        
        def resize_cmd_arr = [  "\"${imagemagick_abs_path}convert\"",
                                "${input_file_name}",
                                "-define",
                                "tiff:tile-geometry=${geometry}",
                                "-depth",
                                "${depth}",
                                "ptif:${output_file_name}"]

        tile_cmd_arr.each{ print "${it} " } /* DEBUG - print out the command we are executing */
        
        def tile_cmd = tile_cmd_arr.execute()
        
        def proc_response = resize_cmd.waitFor()
        
        if(tile_cmd.err.text)
        {
            throw new IMProcessingException(tile_cmd.err.text)
        }
    }
}