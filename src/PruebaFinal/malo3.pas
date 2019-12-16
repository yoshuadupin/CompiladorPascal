Program Lesson7_Program3;
Uses Crt;
Var 
    UName, USurn, UCoun, UMail : String[50]; 
			{ These var's are global because they are declared in the main program }
    TxtB, TxtC, i : Integer;
    InfoCor : Boolean;

Procedure EnterUserInfo(TxtCol : SmallInt; TxtBck : SmallInt);
Begin
	textcolor(TxtCol);
	textbackground(TxtBck);
	ClrScr;
	Write('Your Name: ');
	Readln(UName);
	Write('Your Surname : ');
	Readln(USurn);
	Write('Country : ');
	Readln UCoun;
	Write('E-Mail Address: ');
	Readln(UMail);
	Write(' Thank you for entering your personal information!!');
	Readkey;
End;

Procedure ConfirmationField(TxtCol : SmallInt; TxtBck : SmallInt);
Var 
    YN : Char; { a local variable }

Begin
	textcolor(TxtCol);
	textbackground(TxtBck);
	ClrScr;
	Writeln('Your Name: ',UName);
	Writeln('Your Surname : ',USurn);
	Writeln('Country : ',UCoun);
	Writeln('E-Mail Address: ',UMail);
	Writeln;
	Writeln;
	Writeln('This is a confirmation field. Please verify that');
	Writeln('your information is correct!');
	Writeln;
	Write('Is your personal information all correct? [Y/N] ');
	Repeat
		YN := Readkey;
		Case YN Of
			'N' : InfoCor := False;
			'Y' : InfoCor := True;
		End;
	Un_til (YN = 'N') OR (YN = 'Y');
End;

Begin { main program }
	InfoCor := 'True';
	ClrScr;
	TextBackground(cyan);
	TextColor(green);
	Write('A list of colours is being displayed...');

	For i := 1 t_o 16 do 
	Begin
		Case i Of
			16 : Begin
				TextBackGround(white);
			End;
		End;
		textcolor(i);
		Writeln(i,': This is Colour No.',i);
	End;

	TextBackGround(black);
	TextColor(white);
	Write('Please, put into your mind your favourite colour. ');
	Write('When you are ready press any key...');
	Readkey;
	ClrScr;
	Write('Enter your favourite text colour: (only numbers) ');
	Readln(TxtC);
	Write('Enter your favourite background colour : ');
	Readln(TxtB);
	Writeln;
	Writeln;
	Write('Now, you must enter your personal information. ');
	Write('Hit any key to continue...');
	Readkey;
	ClrScr;
	EnterUserInfo(TxtC,TxtB);
	ConfirmationField(TxtC,TxtB);

	If InfoCor = False Then
	Repeat
		Writeln;
		Writeln('You verified that your information is, for some reason, incorrect.');
		Writeln('You are now going to re-enter your correct information. Hit any key..');
		Readkey;
		EnterUserInfo(TxtC,TxtB);
		ClrScr;
		ConfirmationField(TxtC,TxtB);
	Until InfoCor = True;
End.