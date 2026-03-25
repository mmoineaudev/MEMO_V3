The file ActivityTracker.java is a working activity tracking CLI that I use everyday at work. Use it as a functional reference since it already has 80% of the need fulfilled.

It allows a fast activity logging that I use as a journal so I don't forget anything.
It displays all previously created files at start, ordered by date DESC so that the user sees the last activities at first.

I calculates time sums so I know when I did my hours.

***I would like it transcoded into a maven java swing based GUI, enriched with a search functionality and weekly time sums***

My main nominal usage would be to start it in the morning, read last activity done. Then search for all lines related to that activity to copy paste html links, verify when i started, and the overall time spent for this activity.
Then I often add todos during daily meetings for later.
Then I go back on my last activity, create a new entry with same description, adding new things to do as comment. I create the entry with a time 0 since i'm just starting. 
Hours later, i'll compare the time with the time of creation of the previous entry. I create a new entry with same description, precisions in comments, and 0.5 if i spent all morning on this activity. 
Then i shut down the computer. 
On reopening in the afternoon, those new lignes should be present at the end of the list after all previously entered activities. 

This application is to be seen as a horodated notebook, allowing automatic time sums per subject, per date range.
Main functionality in terms of criticality are : 
* the new entry edition, it should be composed of large text area as it can be long. The new entry edition should propose reusing one of last 10 distinct previously used activity description.
* the search functionality should be efficient and appliable to all or part of the csv columns. Result should appear in a popup with a integrated time sum.
* the whole history should always be opened, there is no performance constraint about this since even with 100 files it wont take a second. 
* The time sums per activity description (example of a description :'JIRA-1234 Label') should be calculated per day and per week and accessible easily for the user, in a popup.
* All panels should be resizable by the user. 
* The produced csv files should be stored in a folder defined in configuration, that folder should be created if missing. Default value is '~/.MEMO/'
