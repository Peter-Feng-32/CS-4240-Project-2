#!/bin/bash
if [ "$1" = "naive" ]; then
  javac -d build @sources.txt
  java -cp ./build/ Backend "naive" "$2" "$3"
elif [ "$1" = "intra" ]; then
  javac -d build @sources.txt
  java -cp ./build/ Backend "intra" "$2" "$3"
else
  echo "First argument must be 'intra' or 'naive'"
fi