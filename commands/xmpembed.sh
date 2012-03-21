cp BLDIDHH005685.jpg test1.jpg


echo Embedding xmp metadata in test1.jpg
exiv2 -v -i X insert ./test1.xmp ./test1.jpg

echo Extract
exiv2 -eiX ./test1.jpg

echo Print values from xmp
exiv2 -PX /home/media/media/images/4f69f64f8f4a2936a2512c8c
exiv2 -PXxgklnycv /home/media/media/images/4f69f64f8f4a2936a2512c8c > t

echo Trivial list key:value
exiv2 -PXkv /home/media/media/images/4f69f64f8f4a2936a2512c8c

