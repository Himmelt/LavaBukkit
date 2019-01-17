mvn package -DskipTests
gradlew reobfToSRG
java -jar build\lavabukkit.jar -Xmx2048M