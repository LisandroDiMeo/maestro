#!/bin/bash

input=$1
output=$2

if [ -z "$input" ] || [ -z "$output" ]; then
    echo "Paths shouldn't be empty"
    exit 1
else
    dot -Tsvg "$input" "$output"
fi

