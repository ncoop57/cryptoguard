#! /bin/sh

TAG=crypto_guard

if [ $# -eq 1 ]; then
	if [ "$1" = "--build" ]; then
		# Build the docker container
		docker build -t $TAG .
	fi
fi


# Run the docker container
docker run -t -d -v $(pwd)/:/home/gradle $TAG
