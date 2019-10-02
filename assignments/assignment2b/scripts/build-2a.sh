#!/usr/bin/env bash
cd ..
for file in `cat scripts/delete-files.txt`; do echo "rm -rf $file"; done
for file in `cat scripts/common-files.txt`; do echo "git checkout master $file"; done
