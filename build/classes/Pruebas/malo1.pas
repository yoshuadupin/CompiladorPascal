procedure e;   { e cannot access a, b, c, and d }
begin
  :
end;

procedure a;
   procedure b;
   begin
     c:=1;  {illegal}
     e:=1;  {legal}
   end;
   procedure c;
   begin
     b:=1;  {legal}
     e:=1;  {legal}
   end;
begin
  :
  b:=2; {legal}
  c:=2; {legal}
  e:=2; {legal}
end;

procedure d;
begin
  :
  b:=1; {illegal}
  c:=1; {illegal}
  a:=1; {legal}
  e:=1; {legal}
end;

begin
  :
  b:=2; {illegal}
  c:=2; {illegal}
  a:=2; {legal}
  d:=2; {legal}
  e:=2; {legal}
end.