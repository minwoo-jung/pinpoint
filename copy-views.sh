VERSION="0.0.4-SNAPSHOT"
cp ./src/main/webapp/WEB-INF/views/*.jsp ./target/pinpoint-web-$VERSION/WEB-INF/views
cp ./src/main/webapp/WEB-INF/web.xml ./target/pinpoint-web-$VERSION/WEB-INF
cp ./src/main/webapp/*.html ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/components ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/scripts ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/styles ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/views ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/images ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/fonts ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/components_v1 ./target/pinpoint-web-$VERSION