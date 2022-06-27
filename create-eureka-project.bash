mkdir spring-cloud
cd spring-cloud

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=eureka-server \
--package-name=se.magnus.springcloud \
--groupId=se.magnus.springcloud \
--version=1.0.0-SNAPSHOT \
eureka-server

cd ..