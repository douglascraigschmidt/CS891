#!/usr/bin/env bash
#for file in `cat delete-files.txt`; do cd ..; rm -rf $file; done
for file in `cat delete-files.txt`; do cd ..; echo "rm -rf $file"; done
