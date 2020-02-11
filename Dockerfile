FROM gradle:6.1.1-jdk8

RUN apt-get update && apt-get upgrade -y

CMD tail -f /dev/null
