
PROGRAM FunctionRecursion(INPUT, OUTPUT);
{ ------------------------------ factorial con recursion ------------------------------ }
VAR
    A :INTEGER;
    B, C, D :REAL;

{ ------------- Functions Definitions -------------- }
FUNCTION Factorial(X :INTEGER) :REAL;
BEGIN
    IF X <= 1 THEN
        Factorial := 1
    ELSE
        Factorial := X * Factorial(X-1);
END;

FUNCTION Avg(X, Y, Z :REAL) :REAL;
BEGIN
    AVG := (X + Y + Z) / 3
END;
{ --------------- End of Functions ---------------- }

{ ----------------- Main program ----------------- }
BEGIN
    WRITE('Enter a number: ');
    READLN(A);
    WRITELN('The Factorial of ', A,' = ', Factorial(A):0:0)

    WRITE('Ingrese 3 numeros: ');
    READLN(B, C, D);
    WRITELN('El promedio es= ', Avg(B, C, D):0:2)
END.