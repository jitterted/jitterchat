# JitterChat - A Twitch Chat Plugin for IntelliJ IDEA

You can see an intro video about this project here: https://www.youtube.com/watch?v=j-9GtV5rbuI

## To Do

* Make file node display nicer (short file name)  
  * Dynamically update comment count for file node (maybe?)
* Add username to comment information (i.e., who submitted the comment)
  * Also add how many (total? active?) comments in the chat view for the user
  * Show comments by user (instead of file)

> [ROOT]
  > file1
    > comment 1 @ line 10
    > comment 2 @ line 15
  > file2
    > comment 1
  > file3 
    > comment 1


* Try again to put something in the gutter to indicate there's a comment (look at Bookmark)

* Implement action for "connect to Twitch" <-> "disconnect from Twitch" button toggle

* Add small text area for me to send chat messages out - but I need to be logged in as "jitterted"

## Fix
* Constrain line number to be valid for given file
