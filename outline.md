## NEED
* Issue ID: comments_affectiveness (CA), all
* Issue politeness: CA amalgamation
	* Go through comments by issue id
	* start as null
	* at first comment, set to polite/impolite as needed
	* at next instance of opposite, switch to mixed (if next != current) and exit
* Issue fixing time: all
* Project name: CA

## Program
* Link them on issue ID
* issue object:
	* id
	* politeness
	* fixing time
	* name
* Read in CA file -> line by line
	* on first line, or in change of issue_id, add new issue
		* id
		* name
		* initial politeness
		* lookup fixing time via query
	* otherwise (i.e. same issue_id)
		* compare current politeness to previous; set accordingly
* Output issues
