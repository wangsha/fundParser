To run the program, the directory must contains four files:
1. a runnable jar: fundsParser.jar
2. timeslots.csv: time slot in mm/dd/yy format

example:
----------------
1/4/02
1/7/02
1/8/02
1/9/02
1/10/02
-------------
3. ticker_close.csv: ticker in 6 digits format
4. ticker_open.csv: ticker in 6 digits format
example:
---------------
162703
162711
163001
163109
163302
163303
163402
163406
----------------

IMPORTANT!
all the .csv files contains no header


Running command:
java -jar fundsParser.jar