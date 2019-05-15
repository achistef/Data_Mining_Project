

## users-to-qid: Creation of user to question map
`users-to-qid.csv` is a script which goes through stack-overflow's user, posts and comments data to map question IDs to users who contributed actively with either the question, an answer or a comment.

The script itself first creates a dictionary for users. The number of users can be reduced by setting a reputation threshold which. For example if the threshold is set to 100, only users with a higher reputation are considered. The higher the reputation of users the more active they are normally.

As a second step the script takes the Posts.xml to walk through it row by row and first, create a dictionary which maps AnswerIDs to the parent QuestionID. Furthermore it adds QuestionIDs and AnswerIDs into the corresponding array.

The thirds step handles the Comments. If a comment is of and answer, it first gets the AnswerID. Then it puts the QuestionID into the array for Comments at the specific user.

If those steps are done, the script will convert it into a pandas Dataframe to then convert it to a `.csv` for now.

### The final table
The structure of the final table is the following:

| UserID 	| Questions 	| Answers   	| Comments    |
|--------	|-----------	|-----------	|-------------|
| 1      	| [3,6,8]   	| [3,5,8,9] 	| [3,2,5,9,7] |
| 2      	| []        	| []        	| [4,5,10]    |
| 3      	| [4,7]     	| []        	| []          |

Important! All the IDs are QuestionsIDs! The ID `3` in answers means that user 1 wrote and answer(with another ID) for Question with QuestionID 3!

### Usage
The script needs flags for each input .xml file to work properly. Additionally one can set a test flag which takes a number of lines to test each file with. Additionally the reputation threshold can be set to reduce the size of the table and only take the more active users.
Example usage:

`$ python users-to-qid.py -u 'path/to/Users.xml' -p 'path/to/PostsWithoutBody.xml' -c 'path/to/Comments.xml'`
