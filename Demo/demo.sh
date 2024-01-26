/usr/bin/time -v -o >(grep "Maximum resident set size (kbytes):" | awk '{print $6}' > memory.txt) \
   java -Xmx100g -jar ../dist/ProGQL.jar \
   -file bfsdemo.txt \
   -db postgres/case1 \
   -output case1 > demo.log 2>&1
