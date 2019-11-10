function yesno : boolean;
var c : char;
begin
  write('Are you sure (Y/N) ? ');
  repeat
  until (c='Y') or (c='N');
  writeln(c);
  if c='Y' then yesno:=true else yesno:=false;
end;

var
  x : boolean; { Don't be tricked, this is a LOCAL variable of main block }
begin
  writeln('Discard all changes made');
  x:=yesno;
  if x=true then
    writeln('Changes discarded !');
  else
    writeln('Changes saved !');

  writeln('Quitting');
  x:=yesno;
  if x then 
  begin
    writeln('Process terminated !');
    halt;
  end;
  
  writeln('Quit cancelled !');
  writeln('Continuing process ...');
end.