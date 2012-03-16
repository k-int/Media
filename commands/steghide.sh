cp input1.jpg input1b.jpg
cp input2.png input2b.png

echo hide id in input1b.jpg
steghide embed -cf input1b.jpg -ef key.txt

echo hide id in input2b.jpg
steghide embed -cf input2b.png -ef key.txt

echo Extracting form input1b.jpg
steghide extract -sf input1b.jpg

echo Extracting form input2b.jpg
steghide extract -sf input2b.png
