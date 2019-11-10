program if8;
var
  i, j, k : byte;
begin
  write('Enter a value for i = '); readln(i);
  write('Enter a value for j = '); readln(j);
  write('Enter a value for k = '); readln(k);
  if ((i>3) and (j>4)) or (k>5) then
  begin
    writeln('Yeah !!');
    writeln('Now change the bracket orders and run it again !');
  end;
end.