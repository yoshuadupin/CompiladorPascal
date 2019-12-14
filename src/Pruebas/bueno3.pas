program fibonacci;

function fib(n:integer): integer;
begin
    if (n <= 2) then
    begin
        result := 1
    end    
    else
    begin
        result := fib(n-1) + fib(n-2);
    end    
end;

var
    i:integer;

begin
    for i := 1 to 16 do
    begin
        write(fib(i), ', ');
    writeln('...');
    end
end.