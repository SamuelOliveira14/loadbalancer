#!/bin/bash

main=$1
args="${@:2}"

if [ -z "$main" ]; then
    echo "Must pass a main file: $0 <package.ClassName> args.."
    exit 1
fi

javac -d ./class ./**/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

java -cp ./class "$main" "$args"