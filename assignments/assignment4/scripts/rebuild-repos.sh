cleanDir()
{
	find . \( -name .idea -o -name build -o -name .gradle -name log -o -name logs \) -exec rm -r {} +;
	find . -type f -a \( -name '*.iml' \) -exec rm {} +;
}

branches="1a 1b 2a 2b 2c 3a 3b 4"
root=crawler-2018

( 
	cd ${root}

	for branch in ${branches}; do
		echo "*** checking out branch $branch ***"
		git checkout $branch
		echo "*** cleaning project ***"
		cleanDir
		echo "*** getting latest scripts ***"
		git checkout master scripts
		echo "*** rebuilding branch $branch ***"
		./scripts/build-$branch
		echo "*** adding any new files ***"
		git add .
		echo "*** git status after rebuild ***"
		git status
		echo "*** committing changes ***"
		git commit -m"Rebuilt branch from master"
		echo "*** pushing changes ***"
		git push
		echo "*** done ***"
	done

	echo "*** checking out master branch ***"
	git checkout master
)
