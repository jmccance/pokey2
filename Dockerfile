####################
# Pokey Dockerfile #
####################

FROM dockerfile/java
MAINTAINER Joel McCance

### BEGIN INSTALL ###

ADD ./target/universal/pokey-2.0-SNAPSHOT.tgz /opt
EXPOSE 9000

### END INSTALL ###

ENTRYPOINT ["/opt/pokey-2.0-SNAPSHOT/bin/pokey"]
