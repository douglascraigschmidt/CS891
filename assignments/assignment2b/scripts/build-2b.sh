#!/usr/bin/env bash
cd ..
for file in `cat scripts/delete-files.txt`; do rm -rf $file; done
for file in `cat scripts/common-files.txt scripts/2[ab]-files.txt`; do git checkout master $file; done
