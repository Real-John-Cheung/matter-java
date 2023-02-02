mkdir -p processingLibrary/matter/library/
cp -R lib/build/libs/matter.jar processingLibrary/matter/library/
cp -R docs/api processingLibrary/matter/reference
cp -R lib/src/main/java/matter processingLibrary/matter/src
zip -vr forProcessing.zip processingLibrary/ -x "*.DS_Store"
rm -R processingLibrary