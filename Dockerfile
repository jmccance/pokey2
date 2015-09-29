FROM heroku/jvm

RUN apt-get update && apt-get install -y --no-install-recommends \
  nodejs \
  npm \
  && rm -rf /var/lib/apt/lists/*

RUN ln -s $(which nodejs) /usr/bin/node

COPY . /tmp
WORKDIR /tmp
RUN ./scripts/dist \
  && tar zxvf ./target/universal/pokey-2.0-SNAPSHOT.tgz -C /app \
  && rm -rf /tmp/*

EXPOSE 9000
CMD ["/app/pokey-2.0-SNAPSHOT/bin/pokey"]
