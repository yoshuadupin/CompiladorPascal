program hola;
var 
    a,b,c,d:integer;
    e: boolean;
    asd: string;

function x2(var z:boolean;x:integer;var y:char):boolean;
var 
    a:integer;
begin
    a := 1;
    if ( a = 2) then
    begin
        e := true;
    end;
    x2 := (1 >2) and (3>4);
end;
begin
    a := 1;
    read(a);
    e := x2( (1 >2) and (3>4) , 1 * 2, 'a');
end.


