# Experimental

FROM dockerfile/java
ADD ./target/universal/pokey-2.0-SNAPSHOT.tgz /opt
WORKDIR /opt/pokey-2.0-SNAPSHOT
EXPOSE 9000
RUN /opt/pokey-2.0-SNAPSHOT/bin/pokey
