# Data_Mining_Project

We've got a lot of executable files, each doing a small piece of work. 
All input/output is defined from the arguments of the execution.

1) XML_to_TSV : Converts Lucian's XML file to a TSV file. It takes 3 arguments, one for input and two for output.  The first output file
contains "type 1" posts (a.k.a questions) while the second output file contains "type 2" posts (a.k.a.) answers

2) RowCounter : I needed a simple way to count those millions of lines... so here we go!

3) PostFilters : This class can filter posts based on i) the date of the posts and ii) whether the question is answered

4) TopPosts : This class filters the input and keeps the top k most popular posts. The popularity is measured in terms of "views_count"

5) TagCounter : This takes an input file and counts how many times each tag has occured.

6) JaccardGraph :  Calculates a graph based on weighted Jaccard similarity (https://en.wikipedia.org/wiki/Jaccard_index). The weight of each tag is how many times it has occured in the dataset. E.g. the weight of "js" is way more than the weight of "Kompics". 

7) Recommender : Given a set of tags, it finds the users who answered the k most similar questions. The silimarity is again based on
the weighted Jaccard index.

8) CountMap :  A map that counts how many times something has been added to it. E.g. if add("js) is invoked 10 times, then get("js) will return 10.

9) MyTreeMap : This is a sorted(desc) map of predefined size S. If more than S elements are added, it keeps the entries with the highest keys.

