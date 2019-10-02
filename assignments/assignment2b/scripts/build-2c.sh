#!/usr/bin/env bash
cd ..
for file in `cat scripts/delete-files.txt`; do echo "rm -rf $file"; done
for file in `cat scripts/common-files.txt scripts/2[abc]-files.txt`; do echo "git checkout master $file"; done
