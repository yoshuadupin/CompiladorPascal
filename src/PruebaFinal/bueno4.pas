program SIMPLE_PROCEDURES (input,output);
var 
    time, distance, speed : real;

procedure display_title;
begin
    writeln('This program calculates the distance travelled based');
    writeln('on two variables entered from the keyboard, speed and');
    writeln('time.')
end;

procedure get_choice;
begin
    writeln('Please enter the speed in MPH');
    readln( speed );
    writeln('Please enter the time in hours');
    readln( time )
end;

procedure calculate_distance;
begin
    distance := speed * time
end;

procedure display_answer;
begin
    writeln('The distance travelled is ', distance:5:2,' miles.')
end;

begin {This is the actual start of the program}
    display_title;
    get_choice;
    calculate_distance;
    display_answer
 end. 