import xml.etree.cElementTree as ET
import pandas as pd
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("-r", "--reputation-threshold", dest = "reputation_threshold", type=int, default = 2, help="Sets the minimum reputation of considered users")
parser.add_argument("-u", "--users-file", dest = "users_xml", default = "Users.xml", help="Path to Users.xml file")
parser.add_argument("-p", "--posts-file", dest = "posts_xml", default = "Posts.xml", help="Path to Posts.xml file")
parser.add_argument("-c", "--comments-file", dest = "comments_xml", default = "Comments.xml", help="Path to Comments.xml file")
parser.add_argument("-t", "--test", dest = "test", type=int, default = None, help="Test with the specified number of lines for each file")
parser.add_argument("-o", "--output-destination", dest = "output_file", default = "users-to-qid.csv", help="Output file destination")



args = parser.parse_args()



# Takes the users-xml file and creates a dict with for each user_id in another dict
def parse_users():
    user_dict = dict()


    context = ET.iterparse(args.users_xml, events=("start", "end"))
    context = iter(context)

    root = None

    # loops through the rows
    for event, elem in context:
        
        if elem.tag == 'users':
            if event == 'start':
                root = elem
            else:
                return user_dict

        elif event == 'start':
            # Filter out inactive users
            reputation = int(elem.get('Reputation'))
            if reputation >= args.reputation_threshold:
                init_dict = dict({'Questions': [], 'Answers': [], 'Comments': []})
                user_dict[int(elem.get('Id'))] = init_dict
            
            
            #TEST
            if args.test and int(elem.get('Id')) > args.test:
                return user_dict

        # delete parsed rows
        elif event == "end":
            elem.clear()
            root.clear()



# Takes the users-xml file, parses it line by line, creates a map from answerId to QuestionID
# and adds QuestionIDs and AnswerIDs into the user-dict
def handle_posts(user_dict):

    map = dict()

    context = ET.iterparse(args.posts_xml, events=("start", "end"))
    context = iter(context)

    root = None


    # loops through the rows
    for event, elem in context:

        if elem.tag == 'posts':
            if event == 'start':
                root = elem
                
            else:
                return map, user_dict

        elif event == 'start':
        
            post_type = int(elem.get('PostTypeId'))
            post_id = int(elem.get('Id'))
            print(post_id)
            
            #TEST
            if args.test and post_id > args.test:
                return map, user_dict
            
            user_id = elem.get('OwnerUserId')
            last_editor_id = elem.get('LastEditorUserId')


            if post_type == 2:
                parent_id = int(elem.get('ParentId'))
                map[post_id] = parent_id # ~10mins

                # Add ParentID to userID -> Answers
                if user_id and int(user_id) in user_dict:
                    user_dict[int(user_id)]['Answers'].append(parent_id)
                
                if last_editor_id and int(last_editor_id) in user_dict:
                    user_dict[int(last_editor_id)]['Answers'].append(parent_id)

            elif post_type == 1:
                # Add PostTypeId to userID -> Questions
                if user_id and int(user_id) in user_dict:
                    user_dict[int(user_id)]['Questions'].append(post_id)

                # Add Last editors if found
                if last_editor_id and last_editor_id) != user_id and int(last_editor_id) in user_dict:
                    user_dict[int(last_editor_id)]['Questions'].append(post_id)

        
        # delete parsed rows
        elif event == "end":
            elem.clear()
            root.clear()

def handle_comments(user_dict, answer_question_map):

    map = dict()

    context = ET.iterparse(args.comments_xml, events=("start", "end"))
    context = iter(context)

    root = None

    # loops through the rows
    for event, elem in context:

        if elem.tag == 'comments':
            if event == 'start':
                root = elem
                
            else:
                return user_dict

        elif event == 'start':
        
            comment_id = int(elem.get('Id'))
            post_id = int(elem.get('PostId'))
            user_id = elem.get('UserId')
            print(comment_id)
            
            #TEST
            if args.test and comment_id > args.test:
                return user_dict

            question_id = post_id
            if post_id in answer_question_map:
                question_id = answer_question_map[post_id]
            
            if user_id and int(user_id) in user_dict:
                user_dict[int(user_id)]['Comments'].append(question_id)

                
        # delete parsed rows
        elif event == "end":
            elem.clear()
            root.clear()

# Transforms a dictionary into according pandas DataFrame file and exports it to csv
def dict_to_pandas(data):
    print("Converting to DataFrame")
    #df = pd.DataFrame.to_dict(data, orient='index', columns=['Questions', 'Answers', 'Comments'])
    df = pd.DataFrame.from_dict(data, orient='index')
    print("Saving as .csv")
    return df.to_csv(args.output_file)



print("Creating initial user table")
user_dict = parse_users()
print("Posts in Progress..")
map, user_dict = handle_posts(user_dict)
print("Comments in progress..")
user_dict = handle_comments(user_dict, map)
dict_to_pandas(user_dict)