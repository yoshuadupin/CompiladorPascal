PROGRAM Scores2(INPUT,OUTPUT);

VAR
    NumberOfClasses: INTEGER;
    Score :ARRAY[1..NumberOfClasses] OF REAL;
    Average, SumOfScores :REAL;
    Index :INTEGER;
    Tab: STRING;

BEGIN
    NumberOfClasses = 6;
    Tab = '         '; { 9 spaces }
    { Read the scores array }
    { --------------------- }
    FOR Index := 1 TO NumberOfClasses DO
        BEGIN
            WRITE('Enter score for class #', Index,': ');
            READLN(Score[Index])
        END;
{ Calculate the sum }
{ ----------------- }
    SumOfScores := 0;
    FOR Index := 1 TO NumberOfClasses DO
        SumOfScores := SumOfScores + Score[Index];
    { Calculate the average }
    { --------------------- }
    Average := SumOfScores / NumberOfClasses;
    { Display Results }
    { --------------- }
    WRITELN;
    WRITELN(Tab,'CLASS #');
    WRITE('      '); { 6 spaces }
    
    FOR Index := 1 TO NumberOfClasses DO
        WRITE(Index:7);
    WRITELN;
    WRITE(Tab);
    FOR Index := 1 TO NumberOfClasses DO
        WRITE('-------');
    WRITELN;
    WRITE('SCORES ');
    FOR Index := 1 TO NumberOfClasses DO
        WRITE(Score[Index]:7:2);
    WRITELN;
    WRITE(Tab);
    FOR Index := 1 TO NumberOfClasses DO
        WRITE('-------');
    WRITELN;
    WRITELN(Tab,'Sum of scores = ', SumOfScores:0:2);
    WRITELN(Tab,'Average of scores = ', Average:0:2);
    WRITELN;
    WRITELN('Press ENTER to continue..');
    READLN
END.