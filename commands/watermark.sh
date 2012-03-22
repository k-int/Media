convert -size 140x80 xc:none -fill grey -gravity NorthWest -draw "text 10,10 'Copyright'" -gravity SouthEast -draw "text 5,15 'Copyright'" miff:- | composite -tile - logo.jpg  wmark_text_tiled.jpg



echo alternatively, a fancy label

convert -size 100x14 xc:none -gravity center \
          -stroke black -strokewidth 2 -annotate 0 'Faerie Dragon' \
          -background none -shadow 100x3+0+0 +repage \
          -stroke none -fill white     -annotate 0 'Faerie Dragon' \
          dragon.gif  +swap -gravity south -geometry +0-3 \
          -composite  anno_fancy.jpg
