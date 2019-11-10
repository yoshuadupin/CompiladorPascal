program hola;
var 
    a,b: integer;
    c: Array [1..10] of integer;
    e :boolean;
begin
    c[1] := 10;
    if (false) then
    begin
        a:=1+1;
        write('meow');
    end
    else if((c[1] > 1) and (3<4)) then
    begin
        write('write');
    end
    else
    begin
        write('watever');
    end;
    {for a := 10  to 20 do
    begin
      writeln('value of a: ', a);
    end;
    

    a := 11;
    repeat
        writeln('value of a: ', a);
        a := a + 1
    until a = 20;

    a := 12;
    while  a < 20  do
   
    begin
        writeln('value of a: ', a);
        a := a + 1;
    end;}
    write('das');
end.